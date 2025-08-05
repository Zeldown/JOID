package be.zeldown.joid.demo.ui.chart.node;

import java.util.Map.Entry;

import javax.vecmath.Vector2d;

import be.zeldown.joid.demo.DemoFont;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.draw.DrawUtils;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.opengl.GLHelper;
import be.zeldown.joid.lib.ui.node.impl.structure.chart.ChartNode;
import be.zeldown.joid.lib.utils.align.Align;
import lombok.NonNull;

public class DemoChartNode extends ChartNode {

	private boolean smooth = true;

	protected DemoChartNode(final double x, final double y, final double width, final double height) {
		super(x, y, width, height);
	}

	public static @NonNull DemoChartNode create(final double x, final double y, final double width, final double height) {
		return new DemoChartNode(x, y, width, height);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), new Color(163, 163, 163));

		if (!super.isLoaded()) {
			DrawUtils.TEXT.drawText(super.getX() + super.dw(2), super.getY() + super.dh(2) - TextInfo.create(DemoFont.MONTSERRAT, 50).getHeight() / 2, "NO DATA", TextInfo.create(DemoFont.MONTSERRAT, 50, Color.WHITE), Align.CENTER, Align.START);
			return;
		}

		final Number min = super.getMin();
		final Number max = super.getMax();

		double ox = super.getX();
		Vector2d last = null;
		final double offset = super.getWidth() / (super.getLabels().size() - 1);
		for (final String label : super.getLabels()) {
			for (final Entry<String, ChartData> entry : super.getDataMap().entrySet()) {
				final ChartData data = entry.getValue();
				final Number value = data.get(label);

				final double oy = super.getY() + super.getHeight() - super.getHeight() * (value.doubleValue() - min.doubleValue()) / (max.doubleValue() - min.doubleValue());

				if (last != null) {
					final Vector2d start = new Vector2d(last);
					final Vector2d startControl = new Vector2d(last.x + offset / 3, last.y);
					final Vector2d end = new Vector2d(ox, oy);
					final Vector2d endControl = new Vector2d(ox - offset / 3, oy);
					if (this.smooth) {
						DrawUtils.SHAPE.drawCurvedLine(Color.RED, 2F, start, startControl, end, endControl);
					} else {
						DrawUtils.SHAPE.drawLine(Color.RED, 2F, start, end);
					}
				}

				GLHelper.translateZ(1);
				DrawUtils.SHAPE.drawCircle(ox, oy, Color.WHITE, 7);
				GLHelper.translateZ(-1);

				last = new Vector2d(ox, oy);
				ox += offset;
			}
		}
	}

	public final @NonNull DemoChartNode smooth(final boolean smooth) {
		this.smooth = smooth;
		return this;
	}

}
