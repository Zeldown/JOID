package dev.joid.demo.ui.node.impl.chart;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.demo.DemoFont;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.impl.chart.ChartNode.ChartAxis;
import dev.joid.lib.ui.node.impl.chart.ChartNode.ChartData;

public class DemoChartNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@BeforeClass
	public static void loadTheDevFont() {
		DevFont.load();
	}

	@Test
	public void skipsTheLabelsASeriesHasNoValueFor() {
		final MsdfFont previous = DemoFont.MONTSERRAT;
		DemoFont.MONTSERRAT = DevFont.MONTSERRAT;
		try {
			final DemoChartNode chart = DemoChartNode.create(0D, 0D, 400D, 200D).xAxis(ChartAxis.x("weekday", "Mon", "Tue", "Wed")).yAxis(ChartAxis.y("requests")).data("api", ChartData.create().add("Mon", 50).add("Wed", 25));
			this.bridges.open(new ChartUI(chart));
			this.bridges.getRender().beginFrame();
			chart.draw(0D, 0D);
			Assert.assertFalse(this.bridges.getRender().getDraws().isEmpty());
		} finally {
			DemoFont.MONTSERRAT = previous;
		}
	}

	public static final class ChartUI extends UI {

		private final DemoChartNode chart;

		private ChartUI(final DemoChartNode chart) {
			this.chart = chart;
		}

		@Override
		public void init() {
			super.add(this.chart);
		}

	}

}