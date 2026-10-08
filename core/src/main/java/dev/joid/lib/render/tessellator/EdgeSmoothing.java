package dev.joid.lib.render.tessellator;

import javax.vecmath.Vector2d;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EdgeSmoothing {

	public static boolean isConvex(final @NonNull Vector2d @NonNull... points) {
		if (points.length < 3) {
			return false;
		}

		double sign = 0D;
		for (int i = 0; i < points.length; i++) {
			final Vector2d a = points[i];
			final Vector2d b = points[(i + 1) % points.length];
			final Vector2d c = points[(i + 2) % points.length];
			final double cross = (b.x - a.x) * (c.y - b.y) - (b.y - a.y) * (c.x - b.x);
			if (cross == 0D) {
				continue;
			}

			if (sign != 0D && Math.signum(cross) != sign) {
				return false;
			}
			sign = Math.signum(cross);
		}
		return sign != 0D;
	}

	public static void polygon(final float red, final float green, final float blue, final float alpha, final @NonNull Vector2d @NonNull... points) {
		EdgeSmoothing.draw(red, green, blue, alpha, null, points);
	}

	public static void rect(final double left, final double top, final double right, final double bottom, final @NonNull double[] uv, final float red, final float green, final float blue, final float alpha) {
		final double[] mapping = {uv[0], (uv[2] - uv[0]) / (right - left), 0D, uv[1], 0D, (uv[3] - uv[1]) / (bottom - top), left, top};
		EdgeSmoothing.draw(red, green, blue, alpha, mapping, new Vector2d(left, bottom), new Vector2d(right, bottom), new Vector2d(right, top), new Vector2d(left, top));
	}

	private static void draw(final float red, final float green, final float blue, final float alpha, final double[] mapping, final @NonNull Vector2d @NonNull... points) {
		final int count = points.length;
		double centerX = 0D;
		double centerY = 0D;
		for (final Vector2d point : points) {
			centerX += point.x / count;
			centerY += point.y / count;
		}

		final double[] normalX = new double[count];
		final double[] normalY = new double[count];
		for (int i = 0; i < count; i++) {
			final Vector2d start = points[i];
			final Vector2d end = points[(i + 1) % count];
			final double length = Math.hypot(end.x - start.x, end.y - start.y);
			double x = length == 0D ? 0D : (end.y - start.y) / length;
			double y = length == 0D ? 0D : -(end.x - start.x) / length;
			if ((centerX - start.x) * x + (centerY - start.y) * y > 0D) {
				x = -x;
				y = -y;
			}
			normalX[i] = x;
			normalY[i] = y;
		}

		double width = Double.POSITIVE_INFINITY;
		for (int i = 0; i < count; i++) {
			if (normalX[i] == 0D && normalY[i] == 0D) {
				continue;
			}

			double depth = 0D;
			for (final Vector2d point : points) {
				depth = Math.max(depth, (points[i].x - point.x) * normalX[i] + (points[i].y - point.y) * normalY[i]);
			}
			width = Math.min(width, depth);
		}

		final double inset = Math.min(0.5D, width / 2D);
		final float core = (float) Math.min(1D, width);
		final double[] innerX = new double[count];
		final double[] innerY = new double[count];
		final double[] outerX = new double[count];
		final double[] outerY = new double[count];
		for (int i = 0; i < count; i++) {
			final int previous = (i + count - 1) % count;
			final double dot = normalX[previous] * normalX[i] + normalY[previous] * normalY[i];
			final double scale = 1D / Math.max(0.25D, 1D + dot);
			final double offsetX = (normalX[previous] + normalX[i]) * scale;
			final double offsetY = (normalY[previous] + normalY[i]) * scale;
			innerX[i] = points[i].x - offsetX * inset;
			innerY[i] = points[i].y - offsetY * inset;
			outerX[i] = points[i].x + offsetX * 0.5D;
			outerY[i] = points[i].y + offsetY * 0.5D;
		}

		final Tessellator tessellator = Tessellator.inst();
		tessellator.start(DrawMode.TRIANGLES);
		for (int i = 1; i + 1 < count; i++) {
			EdgeSmoothing.vertex(tessellator, innerX[0], innerY[0], mapping, red, green, blue, alpha * core);
			EdgeSmoothing.vertex(tessellator, innerX[i], innerY[i], mapping, red, green, blue, alpha * core);
			EdgeSmoothing.vertex(tessellator, innerX[i + 1], innerY[i + 1], mapping, red, green, blue, alpha * core);
		}

		for (int i = 0; i < count; i++) {
			final int next = (i + 1) % count;
			EdgeSmoothing.vertex(tessellator, innerX[i], innerY[i], mapping, red, green, blue, alpha * core);
			EdgeSmoothing.vertex(tessellator, innerX[next], innerY[next], mapping, red, green, blue, alpha * core);
			EdgeSmoothing.vertex(tessellator, outerX[next], outerY[next], mapping, red, green, blue, 0F);
			EdgeSmoothing.vertex(tessellator, innerX[i], innerY[i], mapping, red, green, blue, alpha * core);
			EdgeSmoothing.vertex(tessellator, outerX[next], outerY[next], mapping, red, green, blue, 0F);
			EdgeSmoothing.vertex(tessellator, outerX[i], outerY[i], mapping, red, green, blue, 0F);
		}
		tessellator.draw();
	}

	private static void vertex(final @NonNull Tessellator tessellator, final double x, final double y, final double[] mapping, final float red, final float green, final float blue, final float alpha) {
		tessellator.setColor(red, green, blue, alpha);
		if (mapping != null) {
			tessellator.setTextureUV(mapping[0] + (x - mapping[6]) * mapping[1] + (y - mapping[7]) * mapping[2], mapping[3] + (x - mapping[6]) * mapping[4] + (y - mapping[7]) * mapping[5]);
		}
		tessellator.addVertex(x, y, 0D);
	}

}