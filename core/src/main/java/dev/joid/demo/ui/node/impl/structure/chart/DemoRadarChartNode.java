package dev.joid.demo.ui.node.impl.structure.chart;

import dev.joid.demo.DemoFont;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.ui.node.impl.structure.chart.RadarChartNode;
import dev.joid.lib.ui.node.impl.structure.chart.RadarChartNode.RadarChartData;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.point.Point;
import lombok.NonNull;

public class DemoRadarChartNode extends RadarChartNode<RadarChartData> {

	private static final Color INK = new Color(153, 153, 153);

	protected DemoRadarChartNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull DemoRadarChartNode create(final double x, final double y, final double width, final double height) {
		return new DemoRadarChartNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final int size = super.getDataList().size();

		Point[] points = new Point[size];
		for (int i = 0; i < size; i++) {
			points[i] = this.getPoint(i, super.dh(2));
		}
		DrawUtils.SHAPE.drawPolygon(Color.WHITE, points);

		points = new Point[size];
		for (int i = 0; i < size; i++) {
			final Number value = super.getDataList().get(i).getValue();
			final double p = value.doubleValue() / super.getMax().doubleValue();
			points[i] = this.getPoint(i, super.dh(2) * p * 0.8D);
		}

		DrawUtils.SHAPE.drawPolygon(DemoRadarChartNode.INK.withAlpha(0.5F), points);

		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.pushState();
		render.getState().lineSmooth(true).lineWidth(3F);
		DrawUtils.SHAPE.drawShape(DrawMode.LINE_LOOP, DemoRadarChartNode.INK, points);
		render.popState();

		for (int i = 0; i < size; i++) {
			final Point point = this.getPoint(i, super.dh(2) + 12D);
			DrawUtils.TEXT.drawText(point.getX(), point.getY(), super.getDataList().get(i).getLabel(), TextInfo.create(DemoFont.MONTSERRAT, 16, DemoRadarChartNode.INK), Align.CENTER, Align.CENTER);
		}
	}

	public final Point getPoint(final int index, final double radius) {
		final int size = super.getDataList().size();
		final double angle = 2 * Math.PI / size;

		final double cx = super.getX() + super.dw(2);
		final double cy = super.getY() + super.dh(2);

		final Point[] points = new Point[size];
		for (int i = 0; i < size; i++) {
			final double theta = (i + size / 2) * angle;
			final double x = cx + radius * Math.cos(theta);
			final double y = cy + radius * Math.sin(theta);
			points[i] = Point.create(x, y);
		}

		final Point[] sorted = new Point[size];
		sorted[0] = points[0];
		for (int i = 1; i < size; i++) {
			sorted[i] = points[size - i];
		}

		return sorted[index];
	}

}