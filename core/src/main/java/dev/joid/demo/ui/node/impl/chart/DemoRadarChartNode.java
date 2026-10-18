package dev.joid.demo.ui.node.impl.chart;

import dev.joid.demo.DemoFont;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.render.tessellator.DrawMode;
import dev.joid.lib.ui.node.impl.chart.RadarChartNode;
import dev.joid.lib.ui.node.impl.chart.RadarChartNode.RadarChartData;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.Point;
import lombok.NonNull;

public class DemoRadarChartNode extends RadarChartNode<RadarChartData> {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected DemoRadarChartNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull DemoRadarChartNode create(final double x, final double y, final double width, final double height) {
		return new DemoRadarChartNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final int size = super.getDataList().size();

		final Point[] outline = new Point[size];
		for (int i = 0; i < size; i++) {
			outline[i] = this.getPoint(i, super.dh(2));
		}
		DrawUtils.SHAPE.drawPolygon(Color.WHITE, outline);

		final Point[] points = new Point[size];
		for (int i = 0; i < size; i++) {
			final Number value = super.getDataList().get(i).getValue();
			final double p = value.doubleValue() / super.getMax().doubleValue();
			points[i] = this.getPoint(i, super.dh(2) * p * 0.8D);
		}

		DrawUtils.SHAPE.drawPolygon(DemoRadarChartNode.INK.withAlpha(0.5F), points);

		BridgeHandler.RENDER.get().withState(render -> {
			render.getState().lineSmooth(true).lineWidth(3F);
			DrawUtils.SHAPE.drawShape(DrawMode.LINE_LOOP, DemoRadarChartNode.INK, points);
		});

		for (int i = 0; i < size; i++) {
			final Point point = this.getPoint(i, super.dh(2) + 12D);
			DrawUtils.TEXT.drawText(point.getX(), point.getY(), super.getDataList().get(i).getLabel(), TextInfo.create(DemoFont.MONTSERRAT, 16).color(DemoRadarChartNode.INK), Align.CENTER, Align.CENTER);
		}
	}

	private Point getPoint(final int index, final double radius) {
		final int size = super.getDataList().size();
		final double theta = ((size - index) % size + size / 2) * (2 * Math.PI / size);
		return Point.of(super.getX() + super.dw(2) + radius * Math.cos(theta), super.getY() + super.dh(2) + radius * Math.sin(theta));
	}

}