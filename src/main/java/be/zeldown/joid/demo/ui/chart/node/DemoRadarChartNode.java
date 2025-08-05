package be.zeldown.joid.demo.ui.chart.node;

import javax.vecmath.Vector2d;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.opengl.GLHelper;
import be.zeldown.joid.lib.opengl.GLHelper.GlAttrib;
import be.zeldown.joid.lib.ui.node.impl.structure.chart.RadarChartNode;
import be.zeldown.joid.lib.ui.node.impl.structure.chart.RadarChartNode.RadarChartData;
import lombok.NonNull;

public class DemoRadarChartNode extends RadarChartNode<RadarChartData> {

	protected DemoRadarChartNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull DemoRadarChartNode create(final double x, final double y, final double width, final double height) {
		return new DemoRadarChartNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final int size = super.getDataList().size();

		Vector2d[] points = new Vector2d[size];
		for (int i = 0; i < size; i++) {
			points[i] = this.getPoint(i, super.dh(2));
		}
		DrawUtils.SHAPE.drawPolygon(new Color(163, 163, 163), points);

		points = new Vector2d[size];
		for (int i = 0; i < size; i++) {
			final Number value = super.getDataList().get(i).getValue();
			final double p = value.doubleValue() / super.getMax().doubleValue();
			points[i] = this.getPoint(i, super.dh(2) * p * 0.8);
		}

		DrawUtils.SHAPE.drawPolygon(new Color(89, 34, 30), points);

		GLHelper.pushAttrib(GlAttrib.GL_LINE_SMOOTH, GlAttrib.GL_LINE_WIDTH);
		GLHelper.enable(GL11.GL_LINE_SMOOTH);
		GLHelper.lineWidth(6F);
		DrawUtils.SHAPE.drawShape(GL11.GL_LINE_LOOP, new Color(239, 57, 38), points);
		GLHelper.popAttrib();
	}

	public final Vector2d getPoint(final int index, final double radius) {
		final int size = super.getDataList().size();
		final double angle = 2 * Math.PI / size;

		final double cx = super.getX() + super.dw(2);
		final double cy = super.getY() + super.dh(2);

		final Vector2d[] points = new Vector2d[size];
		for (int i = 0; i < size; i++) {
			final double theta = (i + size / 2) * angle;
			final double x = cx + radius * Math.cos(theta);
			final double y = cy + radius * Math.sin(theta);
			points[i] = new Vector2d(x, y);
		}

		final Vector2d[] sorted = new Vector2d[size];
		sorted[0] = points[0];
		for (int i = 1; i < size; i++) {
			sorted[i] = points[size - i];
		}

		return sorted[index];
	}

}