package com.example.further;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.impl.chart.RadarChartNode;
import dev.joid.lib.ui.node.impl.chart.RadarChartNode.RadarChartData;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.Point;

// #region node
public class ChartsRadarChartNode extends RadarChartNode<RadarChartData> {

	private static final Color INK = new Color(153, 153, 153).lock();

	protected ChartsRadarChartNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static ChartsRadarChartNode create(final double x, final double y, final double size) {
		return new ChartsRadarChartNode(x, y, size, size);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 16F).color(ChartsRadarChartNode.INK);
		final int size = super.getDataList().size();
		final Point[] outline = new Point[size];
		final Point[] values = new Point[size];
		for (int i = 0; i < size; i++) {
			final RadarChartData data = super.getDataList().get(i);
			final double ratio = data.getValue().doubleValue() / super.getMax().doubleValue();
			outline[i] = this.getPoint(i, super.dw(2D));
			values[i] = this.getPoint(i, super.dw(2D) * ratio);
			final Point label = this.getPoint(i, super.dw(2D) + 16D);
			DrawUtils.TEXT.drawText(label.getX(), label.getY(), data.getLabel(), info, Align.CENTER, Align.CENTER);
		}

		DrawUtils.SHAPE.drawPolygon(Color.WHITE, outline);
		DrawUtils.SHAPE.drawPolygon(ChartsRadarChartNode.INK.withAlpha(0.5F), values);
	}

	private Point getPoint(final int index, final double radius) {
		final double angle = index * 2D * Math.PI / super.getDataList().size() - Math.PI / 2D;
		final double centerX = super.getX() + super.dw(2D);
		final double centerY = super.getY() + super.dh(2D);
		return Point.of(centerX + radius * Math.cos(angle), centerY + radius * Math.sin(angle));
	}

}
// #endregion