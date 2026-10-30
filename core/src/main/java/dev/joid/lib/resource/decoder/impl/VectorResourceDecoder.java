package dev.joid.lib.resource.decoder.impl;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.renderer.NullPlatformSupport;
import com.github.weisj.jsvg.renderer.output.Output;
import com.github.weisj.jsvg.renderer.output.impl.Graphics2DOutput;
import com.github.weisj.jsvg.renderer.output.impl.RenderingHintsUtil;
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
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class VectorResourceDecoder implements IResourceDecoder {

	private static final double          BUCKET = 1.25D;
	private static final ExecutorService RASTER = Executors.newSingleThreadExecutor(ThreadUtils.daemonFactory("ResourceVector"));

	@Getter private final Asset           asset;
	private final Map<Long, ITexture>     textures = new LinkedHashMap<>(8, 0.75F, true);
	private final AtomicReference<Raster> pending  = new AtomicReference<>();

	private ITexture    texture;
	private SVGDocument document;

	private volatile boolean rendering;

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
		try (final InputStream stream = this.asset.open()) {
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
	public void clear(final @NonNull ResourceData resource) {
		for (final ITexture cached : this.textures.values()) {
			cached.delete();
		}

		this.textures.clear();
		this.document = null;
		this.pending.set(null);
	}

	@Override
	public void update(final @NonNull ResourceData resource) {
		final Raster raster = this.pending.getAndSet(null);
		if (raster != null && raster.document == this.document) {
			this.store(resource, BridgeHandler.RENDER.get().createTexture().allocate(raster.width, raster.height).upload(raster.pixels));
		}
	}

	@Override
	public void request(final @NonNull ResourceData resource, final int width, final int height, final boolean async, final boolean resizing) {
		if (this.textures.isEmpty()) {
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
		final int[] pixels = VectorResourceDecoder.rasterize(document, width, height);
		ImageUtils.bleedAlpha(pixels, width, height);
		return pixels;
	}

	private static int[] rasterize(final SVGDocument document, final int width, final int height) {
		final int factor = (int) Math.max(2D, Math.min(8D, Math.floor(Math.sqrt(16777216D / ((double) width * height)))));
		final int tile = 2048 / factor;
		final int[] pixels = new int[width * height];
		for (int top = 0; top < height; top += tile) {
			for (int left = 0; left < width; left += tile) {
				VectorResourceDecoder.rasterizeTile(document, factor, pixels, width, height, left, top, Math.min(tile, width - left), Math.min(tile, height - top));
			}
		}
		return pixels;
	}

	private static void rasterizeTile(final SVGDocument document, final int factor, final int[] pixels, final int width, final int height, final int left, final int top, final int tileWidth, final int tileHeight) {
		final FloatSize size = document.size();
		final BufferedImage image = new BufferedImage(tileWidth * factor, tileHeight * factor, BufferedImage.TYPE_INT_ARGB_PRE);
		final Graphics2D graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
		graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		graphics.translate(-left * factor, -top * factor);
		graphics.scale((double) width * factor / size.width, (double) height * factor / size.height);
		RenderingHintsUtil.setupSVGRenderingHints(graphics);

		final VectorOutput output = new VectorOutput(graphics);
		document.renderWithPlatform(NullPlatformSupport.INSTANCE, output, new ViewBox(size));
		output.dispose();

		final int[] samples = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		for (int y = 0; y < tileHeight; y++) {
			for (int x = 0; x < tileWidth; x++) {
				pixels[(top + y) * width + left + x] = VectorResourceDecoder.average(samples, (y * tileWidth * factor + x) * factor, tileWidth * factor, factor);
			}
		}
	}

	private static int average(final int[] samples, final int start, final int stride, final int factor) {
		final int count = factor * factor;
		if (VectorResourceDecoder.isUniform(samples, start, stride, factor)) {
			return VectorResourceDecoder.unpremultiply((samples[start] >>> 24) * count, (samples[start] >> 16 & 0xFF) * count, (samples[start] >> 8 & 0xFF) * count, (samples[start] & 0xFF) * count, count);
		}

		int alpha = 0;
		int red = 0;
		int green = 0;
		int blue = 0;
		for (int row = start; row < start + stride * factor; row += stride) {
			for (int index = row; index < row + factor; index++) {
				alpha += samples[index] >>> 24;
				red += samples[index] >> 16 & 0xFF;
				green += samples[index] >> 8 & 0xFF;
				blue += samples[index] & 0xFF;
			}
		}
		return VectorResourceDecoder.unpremultiply(alpha, red, green, blue, count);
	}

	private static boolean isUniform(final int[] samples, final int start, final int stride, final int factor) {
		for (int row = start; row < start + stride * factor; row += stride) {
			for (int index = row; index < row + factor; index++) {
				if (samples[index] != samples[start]) {
					return false;
				}
			}
		}
		return true;
	}

	private static int unpremultiply(final int alpha, final int red, final int green, final int blue, final int count) {
		if (alpha == 0) {
			return 0;
		}

		return (alpha + count / 2) / count << 24 | VectorResourceDecoder.unpremultiply(red, alpha) << 16 | VectorResourceDecoder.unpremultiply(green, alpha) << 8 | VectorResourceDecoder.unpremultiply(blue, alpha);
	}

	private static int unpremultiply(final int color, final int alpha) {
		return Math.min(255, (color * 255 + alpha / 2) / alpha);
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

	private static final class VectorOutput extends Graphics2DOutput {

		private VectorOutput(final Graphics2D graphics) {
			super(graphics);
		}

		@Override
		public void setStroke(final Stroke stroke) {
			super.setStroke(stroke instanceof BasicStroke ? new VectorStroke(0.1D / VectorOutput.getScale(super.transform()), (BasicStroke) stroke) : stroke);
		}

		@Override
		public Output createChild() {
			return new VectorOutput((Graphics2D) super.graphics().create());
		}

		private static double getScale(final AffineTransform transform) {
			return Math.max(Math.sqrt(transform.getScaleX() * transform.getScaleX() + transform.getShearY() * transform.getShearY()), Math.sqrt(transform.getShearX() * transform.getShearX() + transform.getScaleY() * transform.getScaleY()));
		}

	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	private static final class VectorStroke implements Stroke {

		private final double      tolerance;
		private final BasicStroke stroke;

		@Override
		public Shape createStrokedShape(final Shape shape) {
			final Path2D.Double outline = new Path2D.Double(Path2D.WIND_NON_ZERO);
			final double radius = this.stroke.getLineWidth() / 2D;
			if (radius > 0D) {
				for (final Polyline polyline : this.dash(VectorStroke.flatten(shape, this.tolerance))) {
					this.outline(outline, polyline, radius);
				}
			}
			return outline;
		}

		private List<Polyline> dash(final List<Polyline> polylineList) {
			final float[] pattern = this.stroke.getDashArray();
			if (pattern == null) {
				return polylineList;
			}

			double period = 0D;
			for (final float length : pattern) {
				period += length;
			}

			if (period <= 0D) {
				return polylineList;
			}

			final List<Polyline> dashList = new ArrayList<>();
			for (final Polyline polyline : polylineList) {
				if (polyline.size < 2) {
					dashList.add(polyline);
					continue;
				}

				int index = 0;
				boolean on = true;
				double remaining = pattern[0];
				double phase = (this.stroke.getDashPhase() % period + period) % period;
				while (phase >= remaining) {
					phase -= remaining;
					index = (index + 1) % pattern.length;
					remaining = pattern[index];
					on = !on;
				}
				remaining -= phase;

				final boolean startsOn = on;
				final int first = dashList.size();
				boolean toggled = false;
				Polyline dash = on ? Polyline.dash(polyline.x(0), polyline.y(0)) : null;
				if (dash != null) {
					dashList.add(dash);
				}

				final int segments = polyline.closed ? polyline.size : polyline.size - 1;
				for (int segment = 0; segment < segments; segment++) {
					final int next = (segment + 1) % polyline.size;
					final double startX = polyline.x(segment);
					final double startY = polyline.y(segment);
					final double deltaX = polyline.x(next) - startX;
					final double deltaY = polyline.y(next) - startY;
					final double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
					double travelled = 0D;
					while (length - travelled > remaining) {
						travelled += remaining;
						final double x = startX + deltaX * travelled / length;
						final double y = startY + deltaY * travelled / length;
						if (on) {
							dash.add(x, y);
							dash = null;
						} else {
							dash = Polyline.dash(x, y);
							dashList.add(dash);
						}

						index = (index + 1) % pattern.length;
						remaining = pattern[index];
						on = !on;
						toggled = true;
					}

					remaining -= length - travelled;
					if (on) {
						dash.add(polyline.x(next), polyline.y(next));
					}
				}

				if (!toggled && startsOn) {
					dashList.set(first, polyline);
				} else if (toggled && polyline.closed && startsOn && on) {
					dashList.remove(dashList.size() - 1);
					dashList.set(first, dash.append(dashList.get(first)));
				}
			}
			return dashList;
		}

		private void outline(final Path2D.Double outline, final Polyline polyline, final double radius) {
			final int size = polyline.size;
			final int cap = this.stroke.getEndCap();
			if (size == 1) {
				if (polyline.lined && !polyline.closed) {
					VectorStroke.dot(outline, polyline.x(0), polyline.y(0), radius, cap, this.tolerance);
				}
				return;
			}

			final int segments = polyline.closed ? size : size - 1;
			for (int segment = 0; segment < segments; segment++) {
				final int next = (segment + 1) % size;
				final double startX = polyline.x(segment);
				final double startY = polyline.y(segment);
				final double deltaX = polyline.x(next) - startX;
				final double deltaY = polyline.y(next) - startY;
				final double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
				final double directionX = deltaX / length;
				final double directionY = deltaY / length;
				final double before = !polyline.closed && segment == 0 && cap == BasicStroke.CAP_SQUARE ? radius : 0D;
				final double after = !polyline.closed && segment == segments - 1 && cap == BasicStroke.CAP_SQUARE ? radius : 0D;
				final double fromX = startX - directionX * before;
				final double fromY = startY - directionY * before;
				final double toX = polyline.x(next) + directionX * after;
				final double toY = polyline.y(next) + directionY * after;
				final double normalX = -directionY * radius;
				final double normalY = directionX * radius;
				VectorStroke.polygon(outline, fromX + normalX, fromY + normalY, toX + normalX, toY + normalY, toX - normalX, toY - normalY, fromX - normalX, fromY - normalY);
			}

			for (int vertex = polyline.closed ? 0 : 1; vertex < (polyline.closed ? size : size - 1); vertex++) {
				this.join(outline, polyline, (vertex + size - 1) % size, vertex, (vertex + 1) % size, radius);
			}

			if (!polyline.closed && cap == BasicStroke.CAP_ROUND) {
				VectorStroke.disc(outline, polyline.x(0), polyline.y(0), radius, this.tolerance);
				VectorStroke.disc(outline, polyline.x(size - 1), polyline.y(size - 1), radius, this.tolerance);
			}
		}

		private void join(final Path2D.Double outline, final Polyline polyline, final int previous, final int vertex, final int next, final double radius) {
			final double x = polyline.x(vertex);
			final double y = polyline.y(vertex);
			if (this.stroke.getLineJoin() == BasicStroke.JOIN_ROUND) {
				VectorStroke.disc(outline, x, y, radius, this.tolerance);
				return;
			}

			final double inX = x - polyline.x(previous);
			final double inY = y - polyline.y(previous);
			final double outX = polyline.x(next) - x;
			final double outY = polyline.y(next) - y;
			final double inLength = Math.sqrt(inX * inX + inY * inY);
			final double outLength = Math.sqrt(outX * outX + outY * outY);
			final double cross = (inX * outY - inY * outX) / (inLength * outLength);
			final double dot = (inX * outX + inY * outY) / (inLength * outLength);
			if (cross == 0D) {
				return;
			}

			final double side = cross > 0D ? -radius : radius;
			final double firstX = x - inY / inLength * side;
			final double firstY = y + inX / inLength * side;
			final double secondX = x - outY / outLength * side;
			final double secondY = y + outX / outLength * side;
			final double half = Math.sqrt((1D + dot) / 2D);
			if (this.stroke.getLineJoin() == BasicStroke.JOIN_MITER && half > 0D && 1D / half <= this.stroke.getMiterLimit()) {
				final double bisectorX = firstX + secondX - 2D * x;
				final double bisectorY = firstY + secondY - 2D * y;
				final double scale = radius / half / Math.sqrt(bisectorX * bisectorX + bisectorY * bisectorY);
				VectorStroke.polygon(outline, x, y, firstX, firstY, x + bisectorX * scale, y + bisectorY * scale, secondX, secondY);
				return;
			}

			VectorStroke.polygon(outline, x, y, firstX, firstY, secondX, secondY);
		}

		private static List<Polyline> flatten(final Shape shape, final double tolerance) {
			final List<Polyline> polylineList = new ArrayList<>();
			final double[] coordinates = new double[6];
			Polyline current = null;
			double startX = 0D;
			double startY = 0D;
			for (final PathIterator iterator = shape.getPathIterator(null, tolerance); !iterator.isDone(); iterator.next()) {
				switch (iterator.currentSegment(coordinates)) {
				case PathIterator.SEG_MOVETO:
					startX = coordinates[0];
					startY = coordinates[1];
					current = new Polyline(startX, startY);
					polylineList.add(current);
					break;
				case PathIterator.SEG_LINETO:
					if (current == null) {
						current = new Polyline(startX, startY);
						polylineList.add(current);
					}
					current.add(coordinates[0], coordinates[1]);
					break;
				default:
					if (current != null) {
						current.close();
					}
					current = null;
					break;
				}
			}
			return polylineList;
		}

		private static void dot(final Path2D.Double outline, final double x, final double y, final double radius, final int cap, final double tolerance) {
			if (cap == BasicStroke.CAP_ROUND) {
				VectorStroke.disc(outline, x, y, radius, tolerance);
			} else if (cap == BasicStroke.CAP_SQUARE) {
				VectorStroke.polygon(outline, x - radius, y - radius, x + radius, y - radius, x + radius, y + radius, x - radius, y + radius);
			}
		}

		private static void disc(final Path2D.Double outline, final double x, final double y, final double radius, final double tolerance) {
			final int count = (int) Math.max(8D, Math.min(1024D, Math.ceil(Math.PI / StrictMath.acos(Math.max(0D, 1D - tolerance / radius)))));
			final double[] points = new double[count * 2];
			for (int point = 0; point < count; point++) {
				final double angle = 2D * Math.PI * point / count;
				points[point * 2] = x + radius * StrictMath.cos(angle);
				points[point * 2 + 1] = y + radius * StrictMath.sin(angle);
			}
			VectorStroke.polygon(outline, points);
		}

		private static void polygon(final Path2D.Double outline, final double... points) {
			final int count = points.length / 2;
			double area = 0D;
			for (int point = 0; point < count; point++) {
				final int next = (point + 1) % count;
				area += points[point * 2] * points[next * 2 + 1] - points[next * 2] * points[point * 2 + 1];
			}

			if (area == 0D) {
				return;
			}

			for (int point = 0; point < count; point++) {
				final int index = area > 0D ? point : count - 1 - point;
				if (point == 0) {
					outline.moveTo(points[index * 2], points[index * 2 + 1]);
				} else {
					outline.lineTo(points[index * 2], points[index * 2 + 1]);
				}
			}
			outline.closePath();
		}

		private static final class Polyline {

			private int      size;
			private boolean  lined;
			private boolean  closed;
			private double[] points;

			private Polyline(final double x, final double y) {
				this.points = new double[] {x, y, 0D, 0D};
				this.size   = 1;
			}

			private static Polyline dash(final double x, final double y) {
				final Polyline dash = new Polyline(x, y);
				dash.lined = true;
				return dash;
			}

			private void add(final double x, final double y) {
				this.lined = true;
				if (x == this.x(this.size - 1) && y == this.y(this.size - 1)) {
					return;
				}

				if (this.points.length == this.size * 2) {
					this.points = Arrays.copyOf(this.points, this.size * 4);
				}

				this.points[this.size * 2] = x;
				this.points[this.size * 2 + 1] = y;
				this.size++;
			}

			private void close() {
				this.closed = true;
				if (this.size > 1 && this.x(this.size - 1) == this.x(0) && this.y(this.size - 1) == this.y(0)) {
					this.size--;
				}
			}

			private Polyline append(final Polyline polyline) {
				for (int point = 0; point < polyline.size; point++) {
					this.add(polyline.x(point), polyline.y(point));
				}
				return this;
			}

			private double x(final int point) {
				return this.points[point * 2];
			}

			private double y(final int point) {
				return this.points[point * 2 + 1];
			}

		}

	}

}