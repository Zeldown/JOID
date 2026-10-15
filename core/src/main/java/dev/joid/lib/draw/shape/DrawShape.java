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
		final Color shadow = color.isGradient() ? color.getGradient().getStartColor() : color;
		ShadowShader.use(radius, blur, (float) x, (float) y, (float) (x + width), (float) (y + height), () -> {
			ShadowShader.inst().sides(roundedLeft, roundedTop, roundedRight, roundedBottom);
			this.drawPoints(DrawMode.POLYGON, shadow, false, Point.create(x - spread, y + height + spread), Point.create(x + width + spread, y + height + spread), Point.create(x + width + spread, y - spread), Point.create(x - spread, y - spread));
		});
	}

	public void drawCircle(final double x, final double y, final @NonNull Color color, final double radius) {
		CircleShader.use((float) radius, (float) x, (float) y, () -> {
			if (color.isGradient()) {
				CircleShader.inst().gradient(color.getGradient(), BoundingBox.create(x - radius, y - radius, radius * 2D, radius * 2D));
			}

			this.drawPoints(DrawMode.POLYGON, color.isGradient() ? Color.WHITE : color, false, Point.create(x - radius, y + radius), Point.create(x + radius, y + radius), Point.create(x + radius, y - radius), Point.create(x - radius, y - radius));
		});
	}

	public void drawBorder(final double x, final double y, final double x2, final double y2, final @NonNull Color color) {
		this.drawBorder(x, y, x2, y2, color, 1D);
	}

	public void drawBorder(final double x, final double y, final double x2, final double y2, final @NonNull Color color, final double stroke) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final PixelGrid grid = render.getPixelGrid();
		if (!grid.isAligned() && render.getState().getShader() == null && RoundedShader.inst().isAvailable()) {
			this.drawOutline(Math.min(x, x2) - stroke, Math.min(y, y2) - stroke, Math.max(x, x2) + stroke, Math.max(y, y2) + stroke, color, stroke);
			return;
		}

		final double left = grid.snapX(x);
		final double top = grid.snapY(y);
		final double right = grid.snapX(x2);
		final double bottom = grid.snapY(y2);
		final double outerLeft = grid.snapWidth(x, -stroke);
		final double outerTop = grid.snapHeight(y, -stroke);
		final double outerRight = grid.snapWidth(x2, stroke);
		final double outerBottom = grid.snapHeight(y2, stroke);
		final Color edge = color.isGradient() ? Color.WHITE : color;
		final Color horizontal = DrawShape.cover(edge, stroke * grid.getScaleY());
		final Color vertical = DrawShape.cover(edge, stroke * grid.getScaleX());
		color.bind(() -> {
			this.drawEdges(outerLeft, outerTop, outerRight, top, horizontal);
			this.drawEdges(outerLeft, top, left, bottom, vertical);
			this.drawEdges(outerLeft, bottom, outerRight, outerBottom, horizontal);
			this.drawEdges(right, top, outerRight, bottom, vertical);
		}, BoundingBox.create(Math.min(outerLeft, outerRight), Math.min(outerTop, outerBottom), Math.abs(outerRight - outerLeft), Math.abs(outerBottom - outerTop)));
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
				dashes.add(Point.create(start.getX() + (end.getX() - start.getX()) * from, start.getY() + (end.getY() - start.getY()) * from));
				dashes.add(Point.create(start.getX() + (end.getX() - start.getX()) * to, start.getY() + (end.getY() - start.getY()) * to));
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
		BridgeHandler.RENDER.get().withState(render -> render.getModelView().with(matrix -> {
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
		}));
	}

	private void drawRounded(final double x, final double y, final double width, final double height, final @NonNull Color color, final float radius, final boolean roundedLeft, final boolean roundedTop, final boolean roundedRight, final boolean roundedBottom, final double stroke) {
		final PixelGrid grid = BridgeHandler.RENDER.get().getPixelGrid();
		final double left = grid.snapX(x);
		final double top = grid.snapY(y);
		final double right = grid.snapRight(x, x + width);
		final double bottom = grid.snapBottom(y, y + height);
		RoundedShader.use(radius, (float) (left + (roundedLeft ? radius : 0)), (float) (top + (roundedTop ? radius : 0)), (float) (right - (roundedRight ? radius : 0)), (float) (bottom - (roundedBottom ? radius : 0)), () -> {
			RoundedShader.inst().aligned(grid.isAligned());
			RoundedShader.inst().stroke((float) stroke);
			if (color.isGradient()) {
				RoundedShader.inst().gradient(color.getGradient(), BoundingBox.create(left, top, right - left, bottom - top));
			}

			if (grid.isAligned()) {
				this.drawRect(left, top, right - left, bottom - top, color.isGradient() ? Color.WHITE : color);
			} else {
				this.drawQuad(left - 1D, top - 1D, right + 1D, bottom + 1D, color.isGradient() ? Color.WHITE : color);
			}
		});
	}

	private void drawEdges(final double left, final double top, final double right, final double bottom, final @NonNull Color color) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		if (render.getPixelGrid().isAligned()) {
			this.drawQuad(left, top, right, bottom, color);
			return;
		}

		if (render.getState().getShader() != null || !RoundedShader.inst().isAvailable()) {
			this.drawPoints(DrawMode.POLYGON, color, true, Point.create(left, bottom), Point.create(right, bottom), Point.create(right, top), Point.create(left, top));
			return;
		}

		final double minX = Math.min(left, right);
		final double minY = Math.min(top, bottom);
		final double maxX = Math.max(left, right);
		final double maxY = Math.max(top, bottom);
		RoundedShader.use(0F, (float) (minX + 0.5D), (float) (minY + 0.5D), (float) (maxX - 0.5D), (float) (maxY - 0.5D), () -> {
			RoundedShader.inst().aligned(false);
			if (color.isGradient()) {
				RoundedShader.inst().gradient(color.getGradient(), BoundingBox.create(minX, minY, maxX - minX, maxY - minY));
			}

			this.drawQuad(minX - 1D, minY - 1D, maxX + 1D, maxY + 1D, color.isGradient() ? Color.WHITE : color);
		});
	}

	private void drawOutline(final double left, final double top, final double right, final double bottom, final @NonNull Color color, final double stroke) {
		RoundedShader.use(0F, (float) (left + 0.5D), (float) (top + 0.5D), (float) (right - 0.5D), (float) (bottom - 0.5D), () -> {
			RoundedShader.inst().aligned(false);
			RoundedShader.inst().stroke((float) Math.max(stroke - 0.5D, 1E-3D));
			if (color.isGradient()) {
				RoundedShader.inst().gradient(color.getGradient(), BoundingBox.create(left, top, right - left, bottom - top));
			}

			this.drawQuad(left - 1D, top - 1D, right + 1D, bottom + 1D, color.isGradient() ? Color.WHITE : color);
		});
	}

	private void drawQuad(final double left, final double top, final double right, final double bottom, final @NonNull Color color) {
		this.drawPoints(DrawMode.POLYGON, color, false, Point.create(left, bottom), Point.create(right, bottom), Point.create(right, top), Point.create(left, top));
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
		BridgeHandler.RENDER.get().withState(render -> render.getModelView().with(matrix -> {
			render.getState().blend(BlendState.NORMAL).texture(null);
			color.bind(() -> {
				if (smooth) {
					final Color resolved = color.isGradient() ? Color.WHITE : color.update();
					EdgeSmoothing.polygon(resolved.getRedF(), resolved.getGreenF(), resolved.getBlueF(), resolved.getAlphaF(), points);
					return;
				}

				tessellator.start(mode);
				for (final Point point : points) {
					tessellator.addVertex(point.getX(), point.getY(), 0D);
				}
				tessellator.draw();
			}, box);
		}));
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