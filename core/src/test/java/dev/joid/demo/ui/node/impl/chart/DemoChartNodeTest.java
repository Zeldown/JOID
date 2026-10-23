package dev.joid.demo.ui.node.impl.chart;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.demo.DemoFont;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.cache.TemporaryCache;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataDebug;
import dev.joid.lib.ui.node.impl.chart.ChartNode.ChartAxis;
import dev.joid.lib.ui.node.impl.chart.ChartNode.ChartData;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

public class DemoChartNodeTest {

	@ClassRule
	public static final TemporaryCache CACHE = new TemporaryCache();

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

	@Test
	public void drawsAnAxisWithASingleLabel() {
		final MsdfFont previous = DemoFont.MONTSERRAT;
		DemoFont.MONTSERRAT = DevFont.MONTSERRAT;
		try {
			final DemoChartNode chart = DemoChartNode.create(0D, 0D, 400D, 200D).xAxis(ChartAxis.x("weekday", "Mon")).yAxis(ChartAxis.y("requests")).data("api", ChartData.create().add("Mon", 50));
			this.bridges.open(new ChartUI(chart));
			this.bridges.getRender().beginFrame();
			chart.draw(0D, 0D);
			Assert.assertTrue(this.bridges.getRender().getDraws().stream().allMatch(draw -> Double.isFinite(draw.getLeft()) && Double.isFinite(draw.getRight())));
		} finally {
			DemoFont.MONTSERRAT = previous;
		}
	}

	@UIDataDebug(profiler = false, hotreload = false)
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class ChartUI extends UI {

		private final DemoChartNode chart;

		@Override
		public void init() {
			super.add(this.chart);
		}

	}

}