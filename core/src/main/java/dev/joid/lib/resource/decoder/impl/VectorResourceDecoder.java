package dev.joid.lib.resource.decoder.impl;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.view.FloatSize;
import com.github.weisj.jsvg.view.ViewBox;

import dev.joid.lib.asset.Asset;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.resource.ResourceData;
import dev.joid.lib.resource.decoder.IResourceDecoder;
import dev.joid.lib.utils.image.ImageUtils;
import dev.joid.lib.utils.thread.ThreadUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

public class VectorResourceDecoder implements IResourceDecoder {

	private static final double          BUCKET = 1.25D;
	private static final ExecutorService RASTER = Executors.newSingleThreadExecutor(ThreadUtils.daemonFactory("ResourceVector"));

	@Getter private final Asset           asset;
	private final Map<Long, ITexture>     textures;
	private final AtomicReference<Raster> pending;

	private ITexture    texture;
	private SVGDocument document;

	private volatile boolean rendering;

	protected VectorResourceDecoder(final Asset asset) {
		this.asset = asset;
		this.textures = new LinkedHashMap<>(8, 0.75F, true);
		this.pending = new AtomicReference<>();
	}

	public static @NonNull VectorResourceDecoder create(final @NonNull Asset asset) {
		return new VectorResourceDecoder(asset);
	}

	@Override
	public void prepare(final @NonNull ResourceData resource) {
		this.texture = ResourceData.placeholder();
		resource.texture(this.texture);
	}

	@Override
	public void decode(final @NonNull ResourceData resource) {
		try (InputStream stream = this.asset.open()) {
			this.document = new SVGLoader().load(stream, null, LoaderContext.createDefault());
		} catch (final IOException exception) {
			throw new UncheckedIOException("The SVG " + this.asset.getUniqueId() + " cannot be read", exception);
		}

		if (this.document == null) {
			throw new IllegalArgumentException(this.asset.getUniqueId() + " is not an SVG document that can be parsed");
		}

		final FloatSize size = this.document.size();
		final int width = Math.max(1, (int) Math.ceil(size.width));
		final int height = Math.max(1, (int) Math.ceil(size.height));
		resource.width(width).height(height).data(new int[][] {VectorResourceDecoder.render(this.document, width, height)});
	}

	@Override
	public void upload(final @NonNull ResourceData resource) {
		resource.uploadTextures();
		this.store(resource, this.texture);
	}

	@Override
	public void request(final @NonNull ResourceData resource, final int width, final int height, final boolean async, final boolean resizing) {
		if (this.document == null || this.texture == null || this.textures.isEmpty()) {
			return;
		}

		final double fit = Math.min(1D, 4096D / Math.max(width, height));
		final int exactWidth = Math.max(1, (int) Math.round(width * fit));
		final int exactHeight = Math.max(1, (int) Math.round(height * fit));
		if (this.show(resource, exactWidth, exactHeight)) {
			return;
		}

		if (async && resizing) {
			final ITexture closest = this.closest(exactWidth, exactHeight);
			if (closest != null) {
				this.show(resource, closest.getWidth(), closest.getHeight());
				return;
			}

			final int bucketWidth = (int) Math.ceil(exactWidth * VectorResourceDecoder.BUCKET);
			final int bucketHeight = (int) Math.ceil(exactHeight * VectorResourceDecoder.BUCKET);
			if (!this.show(resource, bucketWidth, bucketHeight)) {
				this.schedule(bucketWidth, bucketHeight);
			}
			return;
		}

		if (async) {
			this.schedule(exactWidth, exactHeight);
		} else {
			this.store(resource, BridgeHandler.RENDER.get().createTexture().allocate(exactWidth, exactHeight).upload(VectorResourceDecoder.render(this.document, exactWidth, exactHeight)));
		}
	}

	@Override
	public void update(final @NonNull ResourceData resource) {
		final Raster raster = this.pending.getAndSet(null);
		if (raster != null && raster.document == this.document) {
			this.store(resource, BridgeHandler.RENDER.get().createTexture().allocate(raster.width, raster.height).upload(raster.pixels));
		}
	}

	@Override
	public void clear(final @NonNull ResourceData resource) {
		for (final ITexture cached : this.textures.values()) {
			cached.delete();
		}

		this.textures.clear();
		this.document = null;
		this.pending.set(null);
	}

	@Override
	public boolean isSettled() {
		return !this.rendering && this.pending.get() == null;
	}

	@Override
	public boolean isMipmappable() {
		return false;
	}

	private boolean show(final ResourceData resource, final int width, final int height) {
		final ITexture cached = this.textures.get(VectorResourceDecoder.key(width, height));
		if (cached == null) {
			return false;
		}

		this.texture = cached;
		resource.texture(cached);
		return true;
	}

	private ITexture closest(final int width, final int height) {
		ITexture closest = null;
		for (final ITexture cached : this.textures.values()) {
			if (cached.getWidth() >= width && cached.getHeight() >= height && cached.getWidth() <= width * VectorResourceDecoder.BUCKET * VectorResourceDecoder.BUCKET && (closest == null || cached.getWidth() < closest.getWidth() || cached.getWidth() == closest.getWidth() && cached.getHeight() < closest.getHeight())) {
				closest = cached;
			}
		}
		return closest;
	}

	private void store(final ResourceData resource, final ITexture texture) {
		this.textures.put(VectorResourceDecoder.key(texture.getWidth(), texture.getHeight()), texture);
		this.texture = texture;
		resource.texture(texture);
		if (this.textures.size() > 8) {
			final Long eldest = this.textures.keySet().iterator().next();
			this.textures.remove(eldest).delete();
		}
	}

	private void schedule(final int width, final int height) {
		if (this.rendering || this.pending.get() != null) {
			return;
		}

		this.rendering = true;
		final SVGDocument document = this.document;
		VectorResourceDecoder.RASTER.execute(() -> {
			try {
				this.pending.set(new Raster(width, height, VectorResourceDecoder.render(document, width, height), document));
			} finally {
				this.rendering = false;
			}
		});
	}

	private static int[] render(final SVGDocument document, final int width, final int height) {
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		final FloatSize size = document.size();
		graphics.scale(width / size.width, height / size.height);
		document.render((Component) null, graphics, new ViewBox(size));
		graphics.dispose();

		final int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
		ImageUtils.bleedAlpha(pixels, width, height);
		return pixels;
	}

	private static long key(final int width, final int height) {
		return (long) width << 32 | height & 0xFFFFFFFFL;
	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class Raster {

		private final int         width;
		private final int         height;
		private final int[]       pixels;
		private final SVGDocument document;

	}

}