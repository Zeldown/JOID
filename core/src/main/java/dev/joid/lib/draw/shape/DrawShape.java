package dev.joid.lib.draw.shape;

import java.util.ArrayList;
import java.util.List;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.matrix.PixelGrid.Span;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.color.Color;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.EdgeSmoothing;
import dev.joid.lib.render.tessellator.Tessellator;
import dev.joid.lib.shader.impl.CircleShader;
import dev.joid.lib.shader.impl.RoundedShader;
import dev.joid.lib.shader.impl.ShadowShader;
import dev.joid.lib.utils.geom.Bezier;
import dev.joid.lib.utils.geom.BoundingBox;
import dev.joid.lib.utils.geom.Point;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DrawShape {

	private static final DrawShape INSTANCE = new DrawShape();

	public static @NonNull DrawShape inst() {
		return DrawShape.INSTANCE;
	}

	public void drawRect(final double x, final double y, final double width, final double height, final @NonNull Color color) {
		if (width == 0D || height == 0D) {
			return;
		}

		final PixelGrid grid = BridgeHandler.RENDER.get().getPixelGrid();
		final Span horizontal = grid.spanX(x, width);
		final Span vertical = grid.spanY(y, height);
		this.drawEdges(horizontal.getStart(), vertical.getStart(), horizontal.getEnd(), vertical.getEnd(), DrawShape.cover(color, horizontal.getCoverage() * vertical.getCoverage()));
	}

	public void drawRoundedRect(final double x, final double y, final double width, final double height, final @NonNull Color color, final float radius) {
		this.drawRoundedRect(x, y, width, height, color, radius, true, true, true, true);
	}

	public void drawRoundedRect(final double x, final double y, final double width, final double height, final @NonNull Color color, final float radius, final boolean roundedLeft, final boolean roundedTop, final boolean roundedRight, final boolean roundedBottom) {
		this.drawRounded(x, y, width, height, color, radius, roundedLeft, roundedTop, roundedRight, roundedBottom, 0D);
	}

	public void drawRoundedBorder(final double x, final double y, final double width, final double height, final @NonNull Color color, final float radius) {
		this.drawRoundedBorder(x, y, width, height, color, radius, 1D);
	}

	public void drawRoundedBorder(final double x, final double y, final double width, final double height, final @NonNull Color color, final float radius, final double stroke) {
		this.drawRounded(x, y, width, height, color, radius, true, true, true, true, stroke);
	}

	public void drawShadow(final double x, final double y, final double width, final double height, final @NonNull Color color, final float radius, final float blur) {
		this.drawShadow(x, y, width, height, color, radius, true, true, true, true, blur);
	}

	public void drawShadow(final double x, final double y, final double width, final double height, final @NonNull Color color, final float radius, final boolean roundedLeft, final boolean roundedTop, final boolean roundedRight, final boolean roundedBottom, final float blur) {
		if (blur <= 0F) {
			this.drawRoundedRect(x, y, width, height, color, radius, roundedLeft, roundedTop, roundedRight, roundedBottom);
			return;
		}

		final double spread = blur * 1.5D;
		final Color resolved = color.update();
		final Color shadow = resolved.isGradient() ? resolved.getGradient().getStartColor() : resolved;
		ShadowShader.use(radius, blur, (float) x, (float) y, (float) (x + width), (float) (y + height), () -> {
			ShadowShader.inst().sides(roundedLeft, roundedTop, roundedRight, roundedBottom);
			this.drawPoints(DrawMode.POLYGON, shadow, false, Point.of(x - spread, y + height + spread), Point.of(x + width + spread, y + height + spread), Point.of(x + width + spread, y - spread), Point.of(x - spread, y - spread));
		});
	}

	public void drawCircle(final double x, final double y, final @NonNull Color color, final double radius) {
		final Color resolved = color.update();
		CircleShader.use((float) radius, (float) x, (float) y, () -> {
			if (resolved.isGradient()) {
				CircleShader.inst().gradient(resolved.getGradient(), BoundingBox.create(x - radius, y - radius, radius * 2D, radius * 2D));
			}

			this.drawPoints(DrawMode.POLYGON, resolved.isGradient() ? Color.WHITE : resolved, false, Point.of(x - radius, y + radius), Point.of(x + radius, y + radius), Point.of(x + radius, y - radius), Point.of(x - radius, y - radius));
		});
	}

	public void drawBorder(final double x, final double y, final double width, final double height, final @NonNull Color color) {
		this.drawBorder(x, y, width, height, color, 1D);
	}

	public void drawBorder(final double x, final double y, final double width, final double height, final @NonNull Color color, final double stroke) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final PixelGrid grid = render.getPixelGrid();
		if (!grid.isAligned() && render.getState().getShader() == null && RoundedShader.inst().isAvailable()) {
			this.drawOutline(Math.min(x, x + width), Math.min(y, y + height), Math.max(x, x + width), Math.max(y, y + height), color, (float) Math.max(stroke - grid.getPixelSize() / 2D, 1E-3D));
			return;
		}

		final double left = grid.snapX(x);
		final double top = grid.snapY(y);
		final double right = grid.snapX(x + width);
		final double bottom = grid.snapY(y + height);
		final double innerLeft = grid.snapWidth(x, stroke);
		final double innerTop = grid.snapHeight(y, stroke);
		final double innerRight = grid.snapWidth(x + width, -stroke);
		final double innerBottom = grid.snapHeight(y + height, -stroke);
		final Color resolved = color.update();
		final Color edge = resolved.isGradient() ? Color.WHITE : resolved;
		final Color horizontal = DrawShape.cover(edge, stroke * grid.getScaleY());
		final Color vertical = DrawShape.cover(edge, stroke * grid.getScaleX());
		resolved.bind(() -> {
			this.drawEdges(left, top, right, innerTop, horizontal);
			this.drawEdges(left, innerTop, innerLeft, innerBottom, vertical);
			this.drawEdges(left, innerBottom, right, bottom, horizontal);
			this.drawEdges(innerRight, innerTop, right, innerBottom, vertical);
		}, BoundingBox.create(Math.min(left, right), Math.min(top, bottom), Math.abs(right - left), Math.abs(bottom - top)));
	}

	public void drawPolygon(final @NonNull Color color, final @NonNull Point @NonNull... points) {
		this.drawShape(DrawMode.POLYGON, color, points);
	}

	public void drawLine(final @NonNull Color color, final @NonNull Point @NonNull... points) {
		BridgeHandler.RENDER.get().withState(render -> {
			render.getState().lineSmooth(true);
			this.drawShape(DrawMode.LINE_STRIP, color, points);
		});
	}

	public void drawLine(final @NonNull Color color, final float stroke, final @NonNull Point @NonNull... points) {
		BridgeHandler.RENDER.get().withState(render -> {
			render.getState().lineWidth(stroke);
			this.drawLine(color, points);
		});
	}

	public void drawDashedLine(final @NonNull Color color, final int pattern, final float stroke, final @NonNull Point @NonNull... points) {
		if (pattern <= 0) {
			throw new IllegalArgumentException("The dash pattern " + pattern + " is not positive, pass a length above 0");
		}

		final List<Point> dashes = new ArrayList<>();
		for (int i = 0; i + 1 < points.length; i++) {
			final Point start = points[i];
			final Point end = points[i + 1];
			final double length = Math.sqrt(Math.pow(end.getX() - start.getX(), 2D) + Math.pow(end.getY() - start.getY(), 2D));
			for (double offset = 0D; offset < length; offset += pattern * 2D) {
				final double from = offset / length;
				final double to = Math.min(length, offset + pattern) / length;
				dashes.add(Point.of(start.getX() + (end.getX() - start.getX()) * from, start.getY() + (end.getY() - start.getY()) * from));
				dashes.add(Point.of(start.getX() + (end.getX() - start.getX()) * to, start.getY() + (end.getY() - start.getY()) * to));
			}
		}

		if (dashes.isEmpty()) {
			return;
		}

		BridgeHandler.RENDER.get().withState(render -> {
			render.getState().lineWidth(stroke).lineSmooth(true);
			this.drawShape(DrawMode.LINES, color, dashes.toArray(new Point[0]));
		});
	}

	public void drawCurvedLine(final @NonNull Color color, final @NonNull Point start, final @NonNull Point control, final @NonNull Point end) {
		final int steps = Math.max(1, (int) Math.ceil(Math.hypot(control.getX() - start.getX(), control.getY() - start.getY()) + Math.hypot(end.getX() - control.getX(), end.getY() - control.getY())));
		final Point[] points = new Point[steps + 1];
		points[0] = start;
		for (int step = 1; step <= steps; step++) {
			points[step] = Bezier.quadratic((float) step / steps, start, control, end);
		}
		this.drawLine(color, points);
	}

	public void drawCurvedLine(final @NonNull Color color, final float stroke, final @NonNull Point start, final @NonNull Point control, final @NonNull Point end) {
		BridgeHandler.RENDER.get().withState(render -> {
			render.getState().lineWidth(stroke);
			this.drawCurvedLine(color, start, control, end);
		});
	}

	public void drawCurvedLine(final @NonNull Color color, final @NonNull Point start, final @NonNull Point startControl, final @NonNull Point endControl, final @NonNull Point end) {
		final int steps = Math.max(1, (int) Math.ceil(Math.hypot(startControl.getX() - start.getX(), startControl.getY() - start.getY()) + Math.hypot(endControl.getX() - startControl.getX(), endControl.getY() - startControl.getY()) + Math.hypot(end.getX() - endControl.getX(), end.getY() - endControl.getY())));
		final Point[] points = new Point[steps + 1];
		points[0] = start;
		for (int step = 1; step <= steps; step++) {
			points[step] = Bezier.cubic((float) step / steps, start, startControl, endControl, end);
		}
		this.drawLine(color, points);
	}

	public void drawCurvedLine(final @NonNull Color color, final float stroke, final @NonNull Point start, final @NonNull Point startControl, final @NonNull Point endControl, final @NonNull Point end) {
		BridgeHandler.RENDER.get().withState(render -> {
			render.getState().lineWidth(stroke);
			this.drawCurvedLine(color, start, startControl, endControl, end);
		});
	}

	public void drawShape(final @NonNull DrawMode mode, final @NonNull Color color, final @NonNull Point @NonNull... points) {
		final boolean smooth = mode == DrawMode.POLYGON && EdgeSmoothing.isConvex(points) && (!BridgeHandler.RENDER.get().getPixelGrid().isAligned() || DrawShape.isSlanted(points));
		this.drawPoints(mode, color, smooth, points);
	}

	public void drawRawRect(final double x, final double y, final double width, final double height) {
		BridgeHandler.RENDER.get().withState(render -> {
			final Tessellator tessellator = Tessellator.inst();
			final PixelGrid grid = render.getPixelGrid();
			final Span horizontal = grid.spanX(x, width);
			final Span vertical = grid.spanY(y, height);
			render.getState().blend(BlendState.NORMAL).texture(null);
			tessellator.start(DrawMode.POLYGON);
			tessellator.addVertex(horizontal.getStart(), vertical.getEnd(), 0D);
			tessellator.addVertex(horizontal.getEnd(), vertical.getEnd(), 0D);
			tessellator.addVertex(horizontal.getEnd(), vertical.getStart(), 0D);
			tessellator.addVertex(horizontal.getStart(), vertical.getStart(), 0D);
			tessellator.draw();
		});
	}

	private void drawRounded(final double x, final double y, final double width, final double height, final @NonNull Color color, final float radius, final boolean roundedLeft, final boolean roundedTop, final boolean roundedRight, final boolean roundedBottom, final double stroke) {
		if (width == 0D || height == 0D) {
			return;
		}

		final PixelGrid grid = BridgeHandler.RENDER.get().getPixelGrid();
		final double left = grid.snapX(x);
		final double top = grid.snapY(y);
		final double right = grid.snapRight(x, x + width);
		final double bottom = grid.snapBottom(y, y + height);
		final float corner = (float) Math.min(radius, Math.min(Math.abs(right - left), Math.abs(bottom - top)) / 2D);
		this.drawRoundedShape(corner, (float) (left + (roundedLeft ? corner : 0F)), (float) (top + (roundedTop ? corner : 0F)), (float) (right - (roundedRight ? corner : 0F)), (float) (bottom - (roundedBottom ? corner : 0F)), left, top, right, bottom, color, (float) stroke, grid.isAligned());
	}

	private void drawEdges(final double left, final double top, final double right, final double bottom, final @NonNull Color color) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		if (render.getPixelGrid().isAligned()) {
			this.drawQuad(left, top, right, bottom, color);
			return;
		}

		if (render.getState().getShader() != null || !RoundedShader.inst().isAvailable()) {
			this.drawPoints(DrawMode.POLYGON, color, true, Point.of(left, bottom), Point.of(right, bottom), Point.of(right, top), Point.of(left, top));
			return;
		}

		this.drawOutline(Math.min(left, right), Math.min(top, bottom), Math.max(left, right), Math.max(top, bottom), color, 0F);
	}

	private void drawOutline(final double left, final double top, final double right, final double bottom, final @NonNull Color color, final float stroke) {
		final double pixel = BridgeHandler.RENDER.get().getPixelGrid().getPixelSize();
		this.drawRoundedShape(0F, (float) (left + pixel / 2D), (float) (top + pixel / 2D), (float) (right - pixel / 2D), (float) (bottom - pixel / 2D), left, top, right, bottom, color, stroke, false);
	}

	private void drawRoundedShape(final float radius, final float x1, final float y1, final float x2, final float y2, final double left, final double top, final double right, final double bottom, final @NonNull Color color, final float stroke, final boolean aligned) {
		final double pixel = BridgeHandler.RENDER.get().getPixelGrid().getPixelSize();
		final Color resolved = color.update();
		RoundedShader.use(radius, x1, y1, x2, y2, () -> {
			RoundedShader.inst().aligned(aligned);
			RoundedShader.inst().stroke(stroke);
			if (resolved.isGradient()) {
				RoundedShader.inst().gradient(resolved.getGradient(), BoundingBox.create(left, top, right - left, bottom - top));
			}

			if (aligned) {
				this.drawRect(left, top, right - left, bottom - top, resolved.isGradient() ? Color.WHITE : resolved);
			} else {
				this.drawQuad(left - pixel, top - pixel, right + pixel, bottom + pixel, resolved.isGradient() ? Color.WHITE : resolved);
			}
		});
	}

	private void drawQuad(final double left, final double top, final double right, final double bottom, final @NonNull Color color) {
		this.drawPoints(DrawMode.POLYGON, color, false, Point.of(left, bottom), Point.of(right, bottom), Point.of(right, top), Point.of(left, top));
	}

	private void drawPoints(final @NonNull DrawMode mode, final @NonNull Color color, final boolean smooth, final @NonNull Point @NonNull... points) {
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		for (final Point point : points) {
			minX = Math.min(minX, point.getX());
			minY = Math.min(minY, point.getY());
			maxX = Math.max(maxX, point.getX());
			maxY = Math.max(maxY, point.getY());
		}

		final Tessellator tessellator = Tessellator.inst();
		final BoundingBox box = BoundingBox.create(minX, minY, maxX - minX, maxY - minY);
		final Color resolved = color.update();
		BridgeHandler.RENDER.get().withState(render -> {
			render.getState().blend(BlendState.NORMAL).texture(null);
			resolved.bind(() -> {
				if (smooth) {
					final Color vertex = resolved.isGradient() ? Color.WHITE : resolved;
					EdgeSmoothing.polygon(vertex.getRedF(), vertex.getGreenF(), vertex.getBlueF(), vertex.getAlphaF(), points);
					return;
				}

				tessellator.start(mode);
				for (final Point point : points) {
					tessellator.addVertex(point.getX(), point.getY(), 0D);
				}
				tessellator.draw();
			}, box);
		});
	}

	private static boolean isSlanted(final @NonNull Point @NonNull... points) {
		for (int i = 0; i < points.length; i++) {
			final Point start = points[i];
			final Point end = points[(i + 1) % points.length];
			if (start.getX() != end.getX() && start.getY() != end.getY()) {
				return true;
			}
		}
		return false;
	}

	private static Color cover(final Color color, final double coverage) {
		return Math.abs(coverage) >= 1D ? color : color.withAlpha((float) (color.getAlphaF() * Math.abs(coverage)));
	}

}