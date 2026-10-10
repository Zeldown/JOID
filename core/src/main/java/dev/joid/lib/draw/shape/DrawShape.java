package dev.joid.lib.draw.shape;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector4f;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.matrix.PixelGrid.Span;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.render.tessellator.EdgeSmoothing;
import dev.joid.lib.render.tessellator.Tessellator;
import dev.joid.lib.shader.impl.CircleShader;
import dev.joid.lib.shader.impl.RoundedShader;
import dev.joid.lib.shader.impl.ShadowShader;
import dev.joid.lib.utils.bezier.Bezier;
import lombok.Getter;
import lombok.NonNull;

public final class DrawShape {

	@Getter
	private static DrawShape instance;

	public DrawShape() {
		if (DrawShape.instance != null) {
			throw new RuntimeException("Attempted to create a duplicate instance of DrawShape.");
		}
		DrawShape.instance = this;
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
		if (blur <= 0F) {
			this.drawRoundedRect(x, y, width, height, color, radius);
			return;
		}

		final double spread = blur * 1.5D;
		final Color shadow = color.isGradient() ? color.gradient.getStartColor() : color;
		ShadowShader.use(radius, blur, (float) x, (float) y, (float) (x + width), (float) (y + height), () -> {
			this.drawPoints(DrawMode.POLYGON, shadow, false, new Vector2d(x - spread, y + height + spread), new Vector2d(x + width + spread, y + height + spread), new Vector2d(x + width + spread, y - spread), new Vector2d(x - spread, y - spread));
		});
	}

	public void drawCircle(final double x, final double y, final @NonNull Color color, final double radius) {
		CircleShader.use((float) radius, (float) x, (float) y, () -> {
			if (color.isGradient()) {
				CircleShader.inst().gradient(color.gradient, new Vector4f((float) (x - radius), (float) (y - radius), (float) (x + radius), (float) (y + radius)));
			}

			this.drawPoints(DrawMode.POLYGON, color.isGradient() ? Color.WHITE : color, false, new Vector2d(x - radius, y + radius), new Vector2d(x + radius, y + radius), new Vector2d(x + radius, y - radius), new Vector2d(x - radius, y - radius));
		});
	}

	public void drawBorder(final double x, final double y, final double x2, final double y2, final @NonNull Color color) {
		this.drawBorder(x, y, x2, y2, color, 1D);
	}

	public void drawBorder(final double x, final double y, final double x2, final double y2, final @NonNull Color color, final double stroke) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final PixelGrid grid = render.getPixelGrid();
		final IShader rounded = RoundedShader.inst().getShader();
		if (!grid.isAligned() && render.getState().getShader() == null && rounded != null && rounded.isActive()) {
			this.drawOutline(Math.min(x, x2) - stroke, Math.min(y, y2) - stroke, Math.max(x, x2) + stroke, Math.max(y, y2) + stroke, color, stroke);
			return;
		}

		final double left = grid.snapX(x);
		final double top = grid.snapY(y);
		final double right = grid.snapX(x2);
		final double bottom = grid.snapY(y2);
		final double outerLeft = grid.snapWidth(x, -stroke);
		final double outerRight = grid.snapWidth(x2, stroke);
		final Color horizontal = DrawShape.cover(color, stroke * grid.getScaleY());
		final Color vertical = DrawShape.cover(color, stroke * grid.getScaleX());
		this.drawEdges(outerLeft, grid.snapHeight(y, -stroke), outerRight, top, horizontal);
		this.drawEdges(outerLeft, top, left, bottom, vertical);
		this.drawEdges(outerLeft, bottom, outerRight, grid.snapHeight(y2, stroke), horizontal);
		this.drawEdges(right, top, outerRight, bottom, vertical);
	}

	public void drawPolygon(final @NonNull Color color, final @NonNull Vector2d @NonNull... points) {
		this.drawShape(DrawMode.POLYGON, color, points);
	}

	public void drawLine(final @NonNull Color color, final @NonNull Vector2d @NonNull... points) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.pushState();
		try {
			render.getState().lineSmooth(true);
			this.drawShape(DrawMode.LINE_STRIP, color, points);
		} finally {
			render.popState();
		}
	}

	public void drawLine(final @NonNull Color color, final float stroke, final @NonNull Vector2d @NonNull... points) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.pushState();
		try {
			render.getState().lineWidth(stroke);
			this.drawLine(color, points);
		} finally {
			render.popState();
		}
	}

	public void drawDashedLine(final @NonNull Color color, final int pattern, final float stroke, final @NonNull Vector2d @NonNull... points) {
		final List<Vector2d> dashes = new ArrayList<>();
		for (int i = 0; i + 1 < points.length; i++) {
			final Vector2d start = points[i];
			final Vector2d end = points[i + 1];
			final double length = Math.sqrt(Math.pow(end.x - start.x, 2D) + Math.pow(end.y - start.y, 2D));
			for (double offset = 0D; offset < length; offset += pattern * 2D) {
				final double from = offset / length;
				final double to = Math.min(length, offset + pattern) / length;
				dashes.add(new Vector2d(start.x + (end.x - start.x) * from, start.y + (end.y - start.y) * from));
				dashes.add(new Vector2d(start.x + (end.x - start.x) * to, start.y + (end.y - start.y) * to));
			}
		}

		if (dashes.isEmpty()) {
			return;
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.pushState();
		try {
			render.getState().lineWidth(stroke).lineSmooth(true);
			this.drawShape(DrawMode.LINES, color, dashes.toArray(new Vector2d[0]));
		} finally {
			render.popState();
		}
	}

	public void drawCurvedLine(final @NonNull Color color, final @NonNull Vector2d start, final @NonNull Vector2d end, final @NonNull Vector2d control) {
		Vector2d last = start;
		final int steps = Math.max(1, (int) Math.ceil(Math.hypot(control.x - start.x, control.y - start.y) + Math.hypot(end.x - control.x, end.y - control.y)));
		for (int step = 1; step <= steps; step++) {
			final Vector2d point = Bezier.quadratic((float) step / steps, start, end, control);
			DrawUtils.SHAPE.drawLine(color, last, point);
			last = point;
		}
	}

	public void drawCurvedLine(final @NonNull Color color, final float stroke, final @NonNull Vector2d start, final @NonNull Vector2d end, final @NonNull Vector2d control) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.pushState();
		try {
			render.getState().lineWidth(stroke);
			this.drawCurvedLine(color, start, end, control);
		} finally {
			render.popState();
		}
	}

	public void drawCurvedLine(final @NonNull Color color, final @NonNull Vector2d start, final @NonNull Vector2d startControl, final @NonNull Vector2d end, final @NonNull Vector2d endControl) {
		Vector2d last = start;
		final int steps = Math.max(1, (int) Math.ceil(Math.hypot(startControl.x - start.x, startControl.y - start.y) + Math.hypot(endControl.x - startControl.x, endControl.y - startControl.y) + Math.hypot(end.x - endControl.x, end.y - endControl.y)));
		for (int step = 1; step <= steps; step++) {
			final Vector2d point = Bezier.cubic((float) step / steps, start, startControl, end, endControl);
			DrawUtils.SHAPE.drawLine(color, last, point);
			last = point;
		}
	}

	public void drawCurvedLine(final @NonNull Color color, final float stroke, final @NonNull Vector2d start, final @NonNull Vector2d startControl, final @NonNull Vector2d end, final @NonNull Vector2d endControl) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.pushState();
		try {
			render.getState().lineWidth(stroke);
			this.drawCurvedLine(color, start, startControl, end, endControl);
		} finally {
			render.popState();
		}
	}

	public void drawShape(final @NonNull DrawMode mode, final @NonNull Color color, final @NonNull Vector2d @NonNull... points) {
		final boolean smooth = mode == DrawMode.POLYGON && EdgeSmoothing.isConvex(points) && (!BridgeHandler.RENDER.get().getPixelGrid().isAligned() || DrawShape.isSlanted(points));
		this.drawPoints(mode, color, smooth, points);
	}

	public void drawRawRect(final double x, final double y, final double width, final double height) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final Tessellator tessellator = Tessellator.inst();
		final PixelGrid grid = render.getPixelGrid();
		final Span horizontal = grid.spanX(x, width);
		final Span vertical = grid.spanY(y, height);
		render.getModelView().push();
		render.pushState();
		try {
			render.getState().blend(BlendState.NORMAL).texture(null);
			tessellator.start(DrawMode.POLYGON);
			tessellator.addVertex(horizontal.getStart(), vertical.getEnd(), 0D);
			tessellator.addVertex(horizontal.getEnd(), vertical.getEnd(), 0D);
			tessellator.addVertex(horizontal.getEnd(), vertical.getStart(), 0D);
			tessellator.addVertex(horizontal.getStart(), vertical.getStart(), 0D);
			tessellator.draw();
		} finally {
			render.popState();
			render.getModelView().pop();
		}
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
				RoundedShader.inst().gradient(color.gradient, new Vector4f((float) left, (float) top, (float) right, (float) bottom));
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

		final IShader rounded = RoundedShader.inst().getShader();
		if (render.getState().getShader() != null || rounded == null || !rounded.isActive()) {
			this.drawPoints(DrawMode.POLYGON, color, true, new Vector2d(left, bottom), new Vector2d(right, bottom), new Vector2d(right, top), new Vector2d(left, top));
			return;
		}

		final double minX = Math.min(left, right);
		final double minY = Math.min(top, bottom);
		final double maxX = Math.max(left, right);
		final double maxY = Math.max(top, bottom);
		RoundedShader.use(0F, (float) (minX + 0.5D), (float) (minY + 0.5D), (float) (maxX - 0.5D), (float) (maxY - 0.5D), () -> {
			RoundedShader.inst().aligned(false);
			if (color.isGradient()) {
				RoundedShader.inst().gradient(color.gradient, new Vector4f((float) minX, (float) minY, (float) maxX, (float) maxY));
			}

			this.drawQuad(minX - 1D, minY - 1D, maxX + 1D, maxY + 1D, color.isGradient() ? Color.WHITE : color);
		});
	}

	private void drawOutline(final double left, final double top, final double right, final double bottom, final @NonNull Color color, final double stroke) {
		RoundedShader.use(0F, (float) (left + 0.5D), (float) (top + 0.5D), (float) (right - 0.5D), (float) (bottom - 0.5D), () -> {
			RoundedShader.inst().aligned(false);
			RoundedShader.inst().stroke((float) Math.max(stroke - 0.5D, 1E-3D));
			if (color.isGradient()) {
				RoundedShader.inst().gradient(color.gradient, new Vector4f((float) left, (float) top, (float) right, (float) bottom));
			}

			this.drawQuad(left - 1D, top - 1D, right + 1D, bottom + 1D, color.isGradient() ? Color.WHITE : color);
		});
	}

	private void drawQuad(final double left, final double top, final double right, final double bottom, final @NonNull Color color) {
		this.drawPoints(DrawMode.POLYGON, color, false, new Vector2d(left, bottom), new Vector2d(right, bottom), new Vector2d(right, top), new Vector2d(left, top));
	}

	private void drawPoints(final @NonNull DrawMode mode, final @NonNull Color color, final boolean smooth, final @NonNull Vector2d @NonNull... points) {
		double minX = Double.POSITIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		for (final Vector2d point : points) {
			minX = Math.min(minX, point.x);
			minY = Math.min(minY, point.y);
			maxX = Math.max(maxX, point.x);
			maxY = Math.max(maxY, point.y);
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		final Tessellator tessellator = Tessellator.inst();
		render.getModelView().push();
		render.pushState();
		try {
			render.getState().blend(BlendState.NORMAL).texture(null);
			color.bind(() -> {
				if (smooth) {
					final Color resolved = color.isGradient() ? Color.WHITE : color.update();
					EdgeSmoothing.polygon(resolved.r, resolved.g, resolved.b, resolved.a, points);
					return;
				}

				tessellator.start(mode);
				for (final Vector2d point : points) {
					tessellator.addVertex(point.x, point.y, 0D);
				}
				tessellator.draw();
			}, new Vector4f((float) minX, (float) minY, (float) maxX, (float) maxY));
		} finally {
			render.popState();
			render.getModelView().pop();
		}
	}

	private static boolean isSlanted(final @NonNull Vector2d @NonNull... points) {
		for (int i = 0; i < points.length; i++) {
			final Vector2d start = points[i];
			final Vector2d end = points[(i + 1) % points.length];
			if (start.x != end.x && start.y != end.y) {
				return true;
			}
		}
		return false;
	}

	private static Color cover(final Color color, final double coverage) {
		return Math.abs(coverage) >= 1D ? color : color.copyAlpha((float) (color.a * Math.abs(coverage)));
	}

}