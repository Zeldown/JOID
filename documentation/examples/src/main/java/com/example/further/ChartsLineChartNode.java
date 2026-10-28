package com.example.further;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.impl.chart.ChartNode;
import dev.joid.lib.utils.align.Align;
import dev.joid.lib.utils.geom.Point;

// #region node
public class ChartsLineChartNode extends ChartNode {

	private static final Color ELEMENT = new Color(38, 43, 53).lock();
	private static final Color MUTED   = new Color(139, 147, 163).lock();
	private static final Color ACCENT  = new Color(91, 140, 255).lock();

	protected ChartsLineChartNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static ChartsLineChartNode create(final double x, final double y, final double width, final double height) {
		return new ChartsLineChartNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 18F).color(ChartsLineChartNode.MUTED);
		final double min = super.getMin().doubleValue();
		final double max = super.getMax().doubleValue();
		final double left = super.getX() + 60D;
		final double top = super.getY() + 15D;
		final double width = super.getWidth() - 85D;
		final double height = super.getHeight() - 60D;
		final double step = super.getLabels().size() > 1 ? width / (super.getLabels().size() - 1) : 0D;

		final String high = super.getYAxis().format(Math.round(max));
		final String low = super.getYAxis().format(Math.round(min));

		final Color background = ChartsLineChartNode.ELEMENT;
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), background);
		DrawUtils.TEXT.drawText(super.getX() + 8D, top, high, info, Align.START, Align.CENTER);
		DrawUtils.TEXT.drawText(super.getX() + 8D, top + height, low, info, Align.START, Align.CENTER);

		int column = 0;
		for (final String label : super.getLabels()) {
			DrawUtils.TEXT.drawText(left + step * column, top + height + 12D, label, info, Align.CENTER, Align.START);
			column++;
		}

		int series = 0;
		for (final ChartData data : super.getDataMap().values()) {
			final Color color = series % 2 == 0 ? ChartsLineChartNode.ACCENT : ChartsLineChartNode.MUTED;
			Point previous = null;
			column = 0;
			for (final String label : super.getLabels()) {
				if (data.has(label)) {
					final double ratio = (data.get(label).doubleValue() - min) / (max - min);
					final Point point = Point.of(left + step * column, top + height - height * ratio);
					if (previous != null) {
						DrawUtils.SHAPE.drawLine(color, 2F, previous, point);
					}
					DrawUtils.SHAPE.drawCircle(point.getX(), point.getY(), color, 4D);
					previous = point;
				}
				column++;
			}
			series++;
		}
	}

}
// #endregion