package dev.joid.demo.ui.chart.node;

import javax.vecmath.Vector2d;

import dev.joid.demo.DemoFont;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.vertex.DrawMode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.ui.node.impl.structure.chart.RadarChartNode;
import dev.joid.lib.ui.node.impl.structure.chart.RadarChartNode.RadarChartData;
import dev.joid.lib.utils.align.Align;
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

		Vector2d[] points = new Vector2d[size];
		for (int i = 0; i < size; i++) {
			points[i] = this.getPoint(i, super.dh(2));
		}
		DrawUtils.SHAPE.drawPolygon(Color.WHITE, points);

		points = new Vector2d[size];
		for (int i = 0; i < size; i++) {
			final Number value = super.getDataList().get(i).getValue();
			final double p = value.doubleValue() / super.getMax().doubleValue();
			points[i] = this.getPoint(i, super.dh(2) * p * 0.8D);
		}

		DrawUtils.SHAPE.drawPolygon(DemoRadarChartNode.INK.copyAlpha(0.5F), points);

		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.pushState();
		render.lineSmooth(true);
		render.lineWidth(3F);
		DrawUtils.SHAPE.drawShape(DrawMode.LINE_LOOP, DemoRadarChartNode.INK, points);
		render.popState();

		for (int i = 0; i < size; i++) {
			final Vector2d point = this.getPoint(i, super.dh(2) + 12D);
			DrawUtils.TEXT.drawText(point.x, point.y, super.getDataList().get(i).getLabel(), TextInfo.create(DemoFont.MONTSERRAT, 16, DemoRadarChartNode.INK), Align.CENTER, Align.CENTER);
		}
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