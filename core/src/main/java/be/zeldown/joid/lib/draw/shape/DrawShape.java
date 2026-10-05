package be.zeldown.joid.lib.draw.shape;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector4f;

import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.render.IRenderBridge;
import be.zeldown.joid.lib.bridge.render.state.BlendState;
import be.zeldown.joid.lib.bridge.render.vertex.DrawMode;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.render.tessellator.Tessellator;
import be.zeldown.joid.lib.shader.impl.CircleShader;
import be.zeldown.joid.lib.shader.impl.RoundedShader;
import be.zeldown.joid.lib.utils.bezier.Bezier;
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

	public void drawLine(final @NonNull Color color, final @NonNull Vector2d @NonNull... points) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.lineSmooth(true);
		this.drawShape(DrawMode.LINE_STRIP, color, points);
		render.lineSmooth(false);
	}

	public void drawPolygon(final @NonNull Color color, final @NonNull Vector2d @NonNull... points) {
		this.drawShape(DrawMode.POLYGON, color, points);
	}

	public void drawRawRect(final double x, final double y, final double width, final double height) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		final Tessellator tessellator = Tessellator.inst();
		render.pushMatrix();
		render.blend(BlendState.NORMAL);
		render.resetTexture();
		tessellator.start(DrawMode.POLYGON);
		tessellator.addVertex(x, y + height, 0D);
		tessellator.addVertex(x + width, y + height, 0D);
		tessellator.addVertex(x + width, y, 0D);
		tessellator.addVertex(x, y, 0D);
		tessellator.draw();
		render.blend(BlendState.DISABLED);
		render.popMatrix();
	}

	public void drawCircle(final double x, final double y, final @NonNull Color color, final double radius) {
		final double diameter = radius * 2D;
		CircleShader.use((float) radius, (float) x, (float) y, () -> {
			this.drawRect(x - radius, y - radius, diameter, diameter, color);
		});
	}

	public void drawLine(final @NonNull Color color, final float stroke, final @NonNull Vector2d @NonNull... points) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.lineWidth(stroke);
		this.drawLine(color, points);
		render.lineWidth(1F);
	}

	public void drawBorder(final double x, final double y, final double x2, final double y2, final @NonNull Color color) {
		this.drawBorder(x, y, x2, y2, color, 1D);
	}

	public void drawRect(final double x, final double y, final double width, final double height, final @NonNull Color color) {
		this.drawPolygon(color, new Vector2d(x, y + height), new Vector2d(x + width, y + height), new Vector2d(x + width, y), new Vector2d(x, y));
	}

	public void drawFilledBorder(final double x, final double y, final double x2, final double y2, final @NonNull Color color) {
		this.drawFilledBorder(x, y, x2, y2, color, 1D);
	}

	public void drawShape(final @NonNull DrawMode mode, final @NonNull Color color, final @NonNull Vector2d @NonNull... points) {
		double minX = Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double maxY = Double.MIN_VALUE;

		for (final Vector2d point : points) {
			minX = Math.min(minX, point.x);
			minY = Math.min(minY, point.y);
			maxX = Math.max(maxX, point.x);
			maxY = Math.max(maxY, point.y);
		}

		final IRenderBridge render = BridgeHandler.RENDER.get();
		final Tessellator tessellator = Tessellator.inst();
		render.pushMatrix();
		render.blend(BlendState.NORMAL);
		render.resetTexture();
		color.bind(() -> {
			tessellator.start(mode);
			for (final Vector2d point : points) {
				tessellator.addVertex(point.x, point.y, 0D);
			}
			tessellator.draw();
		}, new Vector4f((float) minX, (float) minY, (float) maxX, (float) maxY));
		render.blend(BlendState.DISABLED);
		render.popMatrix();
	}

	public void drawBorder(final double x, final double y, final double x2, final double y2, final @NonNull Color color, final double stroke) {
		this.drawRect(x, y - stroke, x2 - x, stroke, color);
		this.drawRect(x - stroke, y, stroke, y2 - y, color);
		this.drawRect(x, y2, x2 - x, stroke, color);
		this.drawRect(x2, y, stroke, y2 - y, color);
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
		render.lineWidth(stroke);
		render.lineSmooth(true);
		this.drawShape(DrawMode.LINES, color, dashes.toArray(new Vector2d[0]));
		render.lineSmooth(false);
		render.lineWidth(1F);
	}

	public void drawFilledBorder(final double x, final double y, final double x2, final double y2, final @NonNull Color color, final double stroke) {
		this.drawRect(x - stroke, y - stroke, x2 - x + stroke + stroke, stroke, color);
		this.drawRect(x - stroke, y, stroke, y2 - y, color);
		this.drawRect(x - stroke, y2, x2 - x + stroke + stroke, stroke, color);
		this.drawRect(x2, y, stroke, y2 - y, color);
	}

	public void drawCurvedLine(final @NonNull Color color, final @NonNull Vector2d start, final @NonNull Vector2d end, final @NonNull Vector2d control) {
		Vector2d last = start;
		final double distance = Math.sqrt(Math.pow(end.x - start.x, 2D) + Math.pow(end.y - start.y, 2D));
		for (float t = 0F; t < 1F; t += 1F / distance) {
			final Vector2d point = Bezier.quadratic(t, start, end, control);
			DrawUtils.SHAPE.drawLine(color, last, point);
			last = point;
		}
	}

	public void drawRoundedRect(final double x, final double y, final double width, final double height, final @NonNull Color color, final float radius) {
		RoundedShader.use(radius, (float) (x + radius), (float) (y + radius), (float) (x + width - radius), (float) (y + height - radius), () -> {
			this.drawRect(x, y, width, height, color);
		});
	}

	public void drawCurvedLine(final @NonNull Color color, final float stroke, final @NonNull Vector2d start, final @NonNull Vector2d end, final @NonNull Vector2d control) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.lineWidth(stroke);
		this.drawCurvedLine(color, start, end, control);
		render.lineWidth(1F);
	}

	public void drawCurvedLine(final @NonNull Color color, final @NonNull Vector2d start, final @NonNull Vector2d startControl, final @NonNull Vector2d end, final @NonNull Vector2d endControl) {
		Vector2d last = start;
		final double distance = Math.sqrt(Math.pow(end.x - start.x, 2D) + Math.pow(end.y - start.y, 2D));
		for (float t = 0F; t < 1F; t += 1F / distance) {
			final Vector2d point = Bezier.cubic(t, start, startControl, end, endControl);
			DrawUtils.SHAPE.drawLine(color, last, point);
			last = point;
		}
	}

	public void drawCurvedLine(final @NonNull Color color, final float stroke, final @NonNull Vector2d start, final @NonNull Vector2d startControl, final @NonNull Vector2d end, final @NonNull Vector2d endControl) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.lineWidth(stroke);
		this.drawCurvedLine(color, start, startControl, end, endControl);
		render.lineWidth(1F);
	}

	public void drawRoundedRect(final double x, final double y, final double width, final double height, final @NonNull Color color, final float radius, final boolean roundedLeft, final boolean roundedTop, final boolean roundedRight, final boolean roundedBottom) {
		RoundedShader.use(radius, (float) (x + (roundedLeft ? radius : 0)), (float) (y + (roundedTop ? radius : 0)), (float) (x + width - (roundedRight ? radius : 0)), (float) (y + height - (roundedBottom ? radius : 0)), () -> {
			this.drawRect(x, y, width, height, color);
		});
	}

}