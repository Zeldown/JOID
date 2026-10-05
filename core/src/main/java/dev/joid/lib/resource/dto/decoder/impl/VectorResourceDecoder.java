package dev.joid.lib.resource.dto.decoder.impl;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
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
import dev.joid.lib.resource.dto.ResourceData;
import dev.joid.lib.resource.dto.decoder.IResourceDecoder;
import dev.joid.lib.utils.image.ImageUtils;
import dev.joid.lib.utils.thread.ThreadUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class VectorResourceDecoder implements IResourceDecoder {

	private static final int             CACHE    = 8;
	private static final int             MAX_SIZE = 4096;
	private static final long            STABLE   = 200000000L;
	private static final double          BUCKET   = 1.25D;
	private static final ExecutorService RASTER   = Executors.newSingleThreadExecutor(ThreadUtils.daemonFactory("ResourceVector"));

	private final Asset                   asset;
	private final Map<Long, ITexture>     textures;
	private final AtomicReference<Raster> pending;

	private SVGDocument document;
	private ITexture    texture;

	private int  requestedWidth;
	private int  requestedHeight;
	private long changeTime;

	private volatile boolean rendering;

	public VectorResourceDecoder(final @NonNull Asset asset) {
		this.asset = asset;
		this.textures = new LinkedHashMap<>(VectorResourceDecoder.CACHE, 0.75F, true);
		this.pending = new AtomicReference<>();
	}

	@Override
	public void init(final @NonNull ResourceData resource) {}

	@Override
	public void prepare(final @NonNull ResourceData resource) {
		this.texture = BridgeHandler.RENDER.get().createTexture().allocate(1, 1).upload(new int[] {0}, 1, 1);
		resource.texture(this.texture);
	}

	@Override
	public void decode(final @NonNull ResourceData resource) {
		try (InputStream stream = this.asset.open()) {
			this.document = new SVGLoader().load(stream, null, LoaderContext.createDefault());
		} catch (final IOException exception) {
			throw new RuntimeException("Unable to read the SVG of " + this.asset.getUniqueId(), exception);
		}

		if (this.document == null) {
			throw new RuntimeException("Failed to parse the SVG of " + this.asset.getUniqueId());
		}

		final FloatSize size = this.document.size();
		resource.width(Math.max(1, (int) Math.ceil(size.width)));
		resource.height(Math.max(1, (int) Math.ceil(size.height)));
		resource.data(new int[][] {this.render(resource.getWidth(), resource.getHeight())});
	}

	@Override
	public void upload(final @NonNull ResourceData resource) {
		this.texture.allocate(resource.getWidth(), resource.getHeight()).upload(resource.getData()[0], resource.getWidth(), resource.getHeight());
		this.store(resource, this.texture);
	}

	@Override
	public void request(final @NonNull ResourceData resource, final int width, final int height, final boolean async) {
		if (this.document == null || this.texture == null || this.textures.isEmpty()) {
			return;
		}

		final double fit = Math.min(1D, (double) VectorResourceDecoder.MAX_SIZE / Math.max(width, height));
		final int exactWidth = Math.max(1, (int) Math.round(width * fit));
		final int exactHeight = Math.max(1, (int) Math.round(height * fit));
		final long now = BridgeHandler.CLOCK.get().nanoTime();
		final boolean first = this.requestedWidth == 0;
		if (exactWidth != this.requestedWidth || exactHeight != this.requestedHeight) {
			this.requestedWidth = exactWidth;
			this.requestedHeight = exactHeight;
			this.changeTime = now;
		}

		if (this.show(resource, exactWidth, exactHeight)) {
			return;
		}

		if (!first && async && now - this.changeTime < VectorResourceDecoder.STABLE) {
			if (this.texture.getWidth() >= exactWidth && this.texture.getHeight() >= exactHeight && this.texture.getWidth() <= exactWidth * VectorResourceDecoder.BUCKET * VectorResourceDecoder.BUCKET) {
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
			this.store(resource, BridgeHandler.RENDER.get().createTexture().allocate(exactWidth, exactHeight).upload(this.render(exactWidth, exactHeight), exactWidth, exactHeight));
		}
	}

	@Override
	public void update(final @NonNull ResourceData resource) {
		final Raster raster = this.pending.getAndSet(null);
		if (raster != null) {
			this.store(resource, BridgeHandler.RENDER.get().createTexture().allocate(raster.width, raster.height).upload(raster.pixels, raster.width, raster.height));
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
	public boolean isScalable() {
		return true;
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

	private void store(final ResourceData resource, final ITexture texture) {
		this.textures.put(VectorResourceDecoder.key(texture.getWidth(), texture.getHeight()), texture);
		this.texture = texture;
		resource.texture(texture);
		if (this.textures.size() > VectorResourceDecoder.CACHE) {
			final Long eldest = this.textures.keySet().iterator().next();
			this.textures.remove(eldest).delete();
		}
	}

	private void schedule(final int width, final int height) {
		if (this.rendering || this.pending.get() != null) {
			return;
		}

		this.rendering = true;
		VectorResourceDecoder.RASTER.execute(() -> {
			try {
				this.pending.set(new Raster(width, height, this.render(width, height)));
			} finally {
				this.rendering = false;
			}
		});
	}

	private int[] render(final int width, final int height) {
		final SVGDocument current = this.document;
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		if (current != null) {
			final FloatSize size = current.size();
			graphics.scale(width / size.width, height / size.height);
			current.render((Component) null, graphics, new ViewBox(size));
		}
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

		private final int   width;
		private final int   height;
		private final int[] pixels;

	}

}