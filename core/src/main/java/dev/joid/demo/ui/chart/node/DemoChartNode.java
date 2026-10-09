package dev.joid.demo.ui.chart.node;

import java.util.Map.Entry;

import javax.vecmath.Vector2d;

import dev.joid.demo.DemoFont;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.ui.node.impl.structure.chart.ChartNode;
import dev.joid.lib.utils.align.Align;
import lombok.NonNull;

public class DemoChartNode extends ChartNode {

	private static final Color   INK    = new Color(153, 153, 153);
	private static final Color[] SERIES = {new Color(153, 153, 153), new Color(85, 85, 85)};

	private boolean smooth = true;

	protected DemoChartNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull DemoChartNode create(final double x, final double y, final double width, final double height) {
		return new DemoChartNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 14, DemoChartNode.INK);
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.WHITE);

		if (!super.isLoaded()) {
			DrawUtils.TEXT.drawText(super.getX() + super.dw(2), super.getY() + super.dh(2), "No data", TextInfo.create(DemoFont.MONTSERRAT, 24, DemoChartNode.INK), Align.CENTER, Align.CENTER);
			return;
		}

		final double min = super.getMin().doubleValue();
		final double max = super.getMax().doubleValue();
		final double left = super.getX() + 50D;
		final double top = super.getY() + 15D;
		final double width = super.getWidth() - 65D;
		final double height = super.getHeight() - 40D;
		final double offset = width / (super.getLabels().size() - 1);

		DrawUtils.TEXT.drawText(super.getX() + 8D, top, super.getYAxis().format(max), info, Align.START, Align.CENTER);
		DrawUtils.TEXT.drawText(super.getX() + 8D, top + height, super.getYAxis().format(min), info, Align.START, Align.CENTER);

		int labelIndex = 0;
		for (final String label : super.getLabels()) {
			DrawUtils.TEXT.drawText(left + offset * labelIndex, top + height + 8D, label, info, Align.CENTER, Align.START);
			labelIndex++;
		}

		int seriesIndex = 0;
		for (final Entry<String, ChartData> entry : super.getDataMap().entrySet()) {
			final Color color = DemoChartNode.SERIES[seriesIndex % DemoChartNode.SERIES.length];
			Vector2d last = null;
			double ox = left;
			for (final String label : super.getLabels()) {
				final double oy = top + height - height * (entry.getValue().get(label).doubleValue() - min) / (max - min);
				final Vector2d point = new Vector2d(ox, oy);
				if (last != null) {
					if (this.smooth) {
						DrawUtils.SHAPE.drawCurvedLine(color, 2F, last, new Vector2d(last.x + offset / 3D, last.y), point, new Vector2d(ox - offset / 3D, oy));
					} else {
						DrawUtils.SHAPE.drawLine(color, 2F, last, point);
					}
				}

				BridgeHandler.RENDER.get().translate(0D, 0D, 1D);
				DrawUtils.SHAPE.drawCircle(ox, oy, color, 4D);
				BridgeHandler.RENDER.get().translate(0D, 0D, -1D);

				last = point;
				ox += offset;
			}
			seriesIndex++;
		}
	}

	public final @NonNull DemoChartNode smooth(final boolean smooth) {
		this.smooth = smooth;
		return this;
	}

}