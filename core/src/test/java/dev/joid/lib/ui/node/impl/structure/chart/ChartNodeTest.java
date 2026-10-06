package dev.joid.lib.ui.node.impl.structure.chart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.render.RecordingRenderBridge.Draw;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.Node;
import dev.joid.lib.ui.node.impl.structure.chart.ChartNode.ChartAxis;
import dev.joid.lib.ui.node.impl.structure.chart.ChartNode.ChartAxis.XChartAxis;
import dev.joid.lib.ui.node.impl.structure.chart.ChartNode.ChartAxis.YChartAxis;
import dev.joid.lib.ui.node.impl.structure.chart.ChartNode.ChartData;

public class ChartNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void startsWithoutAxes() {
		final BarChartNode chart = new BarChartNode(0D, 0D, 300D, 200D);
		Assert.assertNull(chart.getXAxis());
		Assert.assertNull(chart.getYAxis());
		Assert.assertFalse(chart.isLoaded());
	}

	@Test
	public void takesEachAxis() {
		final XChartAxis x = ChartAxis.x("weekday", "Mon");
		final YChartAxis y = ChartAxis.y("requests");
		final BarChartNode chart = new BarChartNode(0D, 0D, 300D, 200D);
		Assert.assertSame(chart, chart.axis(x));
		Assert.assertSame(x, chart.getXAxis());
		Assert.assertNull(chart.getYAxis());
		Assert.assertSame(chart, chart.axis(y));
		Assert.assertSame(y, chart.getYAxis());
	}

	@Test
	public void takesBothAxesAtOnce() {
		final XChartAxis x = ChartAxis.x("weekday", "Mon");
		final YChartAxis y = ChartAxis.y("requests");
		final BarChartNode chart = new BarChartNode(0D, 0D, 300D, 200D);
		Assert.assertSame(chart, chart.axis(x, y));
		Assert.assertSame(x, chart.getXAxis());
		Assert.assertSame(y, chart.getYAxis());
	}

	@Test
	public void keepsTheSeriesOnItsXAxis() {
		final XChartAxis x = ChartAxis.x("weekday", "Mon", "Tue");
		final ChartData api = ChartData.create().add("Mon", 120);
		final BarChartNode chart = new BarChartNode(0D, 0D, 300D, 200D).axis(x);
		Assert.assertSame(chart, chart.data("api", api));
		Assert.assertSame(api, chart.getData("api"));
		Assert.assertSame(api, x.get("api"));
		Assert.assertSame(x.getDataMap(), chart.getDataMap());
		Assert.assertEquals(Arrays.asList("Mon", "Tue"), new ArrayList<>(chart.getLabels()));
	}

	@Test
	public void removesASeries() {
		final BarChartNode chart = new BarChartNode(0D, 0D, 300D, 200D).axis(ChartAxis.x("weekday", "Mon")).data("api", ChartData.create().add("Mon", 120)).data("web", ChartData.create().add("Mon", 80));
		Assert.assertSame(chart, chart.remove("api"));
		Assert.assertNull(chart.getData("api"));
		Assert.assertEquals(1, chart.getDataMap().size());
	}

	@Test(expected = IllegalStateException.class)
	public void refusesDataWithoutAnXAxis() {
		new BarChartNode(0D, 0D, 300D, 200D).data("api", ChartData.create());
	}

	@Test(expected = IllegalStateException.class)
	public void refusesToRemoveDataWithoutAnXAxis() {
		new BarChartNode(0D, 0D, 300D, 200D).remove("api");
	}

	@Test
	public void loadsOnceEverySeriesIsFilled() {
		final BarChartNode chart = new BarChartNode(0D, 0D, 300D, 200D).axis(ChartAxis.x("weekday", "Mon"), ChartAxis.y("requests"));
		Assert.assertFalse(chart.isLoaded());
		chart.data("api", ChartData.create().add("Mon", 120));
		Assert.assertTrue(chart.isLoaded());
		chart.data("web", ChartData.create());
		Assert.assertFalse(chart.isLoaded());
		chart.getData("web").add("Mon", 80);
		Assert.assertTrue(chart.isLoaded());
	}

	@Test
	public void needsAYAxisToLoad() {
		final BarChartNode chart = new BarChartNode(0D, 0D, 300D, 200D).axis(ChartAxis.x("weekday", "Mon")).data("api", ChartData.create().add("Mon", 120));
		Assert.assertFalse(chart.isLoaded());
		chart.axis(ChartAxis.y("requests"));
		Assert.assertTrue(chart.isLoaded());
	}

	@Test
	public void waitsForItsNodeToMount() {
		final boolean[] ready = {false};
		final BarChartNode chart = new BarChartNode(0D, 0D, 300D, 200D).wait(node -> ready[0]);
		chart.axis(ChartAxis.x("weekday", "Mon"), ChartAxis.y("requests")).data("api", ChartData.create().add("Mon", 120));
		Assert.assertFalse(chart.isLoaded());
		ready[0] = true;
		Assert.assertTrue(chart.isLoaded());
	}

	@Test
	public void measuresEverySeries() {
		final BarChartNode chart = new BarChartNode(0D, 0D, 300D, 200D).axis(ChartAxis.x("weekday", "Mon", "Tue")).data("api", ChartData.create().add("Mon", 1).add("Tue", 5)).data("web", ChartData.create().add("Mon", 3).add("Tue", 9));
		Assert.assertEquals(9D, chart.getMax().doubleValue(), 0D);
		Assert.assertEquals(1D, chart.getMin().doubleValue(), 0D);
		Assert.assertEquals(4.5D, chart.getAverage().doubleValue(), 0D);
	}

	@Test
	public void measuresOneSeries() {
		final BarChartNode chart = new BarChartNode(0D, 0D, 300D, 200D).axis(ChartAxis.x("weekday", "Mon", "Tue")).data("api", ChartData.create().add("Mon", 1).add("Tue", 5)).data("web", ChartData.create().add("Mon", 3).add("Tue", 9));
		Assert.assertEquals(5D, chart.getMax("api").doubleValue(), 0D);
		Assert.assertEquals(1D, chart.getMin("api").doubleValue(), 0D);
		Assert.assertEquals(3D, chart.getAverage("api").doubleValue(), 0D);
		Assert.assertEquals(9D, chart.getMax("web").doubleValue(), 0D);
	}

	@Test
	public void spreadsTheScaleOfAFlatSeries() {
		final BarChartNode chart = new BarChartNode(0D, 0D, 300D, 200D).axis(ChartAxis.x("weekday", "Mon", "Tue")).data("api", ChartData.create().add("Mon", 4).add("Tue", 4));
		Assert.assertEquals(8D, chart.getMax().doubleValue(), 0D);
		Assert.assertEquals(0D, chart.getMin().doubleValue(), 0D);
		Assert.assertEquals(8D, chart.getMax("api").doubleValue(), 0D);
		Assert.assertEquals(0D, chart.getMin("api").doubleValue(), 0D);
	}

	@Test
	public void givesAUnitScaleToASeriesOfZeros() {
		final BarChartNode chart = new BarChartNode(0D, 0D, 300D, 200D).axis(ChartAxis.x("weekday", "Mon")).data("api", ChartData.create().add("Mon", 0));
		Assert.assertEquals(1D, chart.getMax().doubleValue(), 0D);
		Assert.assertEquals(0D, chart.getMin().doubleValue(), 0D);
		Assert.assertEquals(1D, chart.getMax("api").doubleValue(), 0D);
		Assert.assertEquals(0D, chart.getMin("api").doubleValue(), 0D);
	}

	@Test
	public void givesAUnitScaleWithoutSeries() {
		final BarChartNode chart = new BarChartNode(0D, 0D, 300D, 200D).axis(ChartAxis.x("weekday", "Mon"));
		Assert.assertEquals(1D, chart.getMax().doubleValue(), 0D);
		Assert.assertEquals(0D, chart.getMin().doubleValue(), 0D);
		Assert.assertEquals(0D, chart.getAverage().doubleValue(), 0D);
	}

	@Test
	public void drawsItsSeriesOnceLoaded() {
		final BarChartNode chart = new BarChartNode(100D, 100D, 300D, 200D).axis(ChartAxis.x("weekday", "Mon", "Tue", "Wed"), ChartAxis.y("requests")).data("api", ChartData.create().add("Mon", 50).add("Tue", 100).add("Wed", 25));
		this.bridges.open(new NodeUI(chart)).frame();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertEquals(3, draws.size());
		ChartNodeTest.assertBox(draws.get(0), 100D, 200D, 200D, 300D);
		ChartNodeTest.assertBox(draws.get(1), 200D, 100D, 300D, 300D);
		ChartNodeTest.assertBox(draws.get(2), 300D, 250D, 400D, 300D);
	}

	@Test
	public void drawsNothingWhileLoading() {
		final BarChartNode chart = new BarChartNode(100D, 100D, 300D, 200D).wait(node -> false);
		chart.axis(ChartAxis.x("weekday", "Mon"), ChartAxis.y("requests")).data("api", ChartData.create().add("Mon", 50));
		this.bridges.open(new NodeUI(chart)).frame();
		Assert.assertTrue(this.bridges.getRender().getDraws().isEmpty());
	}

	@Test
	public void namesItsAxes() {
		Assert.assertEquals("weekday", ChartAxis.x("weekday", "Mon").getName());
		Assert.assertEquals("requests", ChartAxis.y("requests").getName());
	}

	@Test
	public void keepsTheLabelsInTheirOrder() {
		final XChartAxis x = ChartAxis.x("weekday", "Wed", "Mon", "Tue", "Mon");
		Assert.assertEquals(Arrays.asList("Wed", "Mon", "Tue"), new ArrayList<>(x.getLabelSet()));
		Assert.assertTrue(x.getDataMap().isEmpty());
	}

	@Test
	public void replacesItsLabels() {
		final XChartAxis x = ChartAxis.x("weekday", "Mon", "Tue");
		Assert.assertSame(x, x.labelSet("Sat", "Sun"));
		Assert.assertEquals(Arrays.asList("Sat", "Sun"), new ArrayList<>(x.getLabelSet()));
	}

	@Test
	public void copiesAnOrderedLabelSet() {
		final Set<String> labels = new LinkedHashSet<>(Arrays.asList("Sun", "Sat"));
		final XChartAxis x = ChartAxis.x("weekday", "Mon");
		Assert.assertSame(x, x.labelSet(labels));
		labels.add("Fri");
		Assert.assertEquals(Arrays.asList("Sun", "Sat"), new ArrayList<>(x.getLabelSet()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAnUnorderedLabelSet() {
		ChartAxis.x("weekday", "Mon").labelSet(new HashSet<>(Arrays.asList("Sat", "Sun")));
	}

	@Test
	public void storesAndRemovesTheSeries() {
		final ChartData api = ChartData.create();
		final XChartAxis x = ChartAxis.x("weekday", "Mon");
		Assert.assertSame(x, x.data("api", api));
		Assert.assertSame(api, x.get("api"));
		Assert.assertSame(x, x.remove("api"));
		Assert.assertNull(x.get("api"));
	}

	@Test
	public void formatsWithoutPrefixOrSuffixByDefault() {
		final YChartAxis y = ChartAxis.y("requests");
		Assert.assertNull(y.getPrefix());
		Assert.assertNull(y.getSuffix());
	}

	@Test
	public void keepsAPrefixAndASuffix() {
		final YChartAxis y = ChartAxis.y("price");
		Assert.assertSame(y, y.prefix("$"));
		Assert.assertSame(y, y.suffix(" req"));
		Assert.assertEquals("$", y.getPrefix());
		Assert.assertEquals(" req", y.getSuffix());
	}

	@Test
	public void startsWithoutValues() {
		final ChartData data = ChartData.create();
		Assert.assertTrue(data.isEmpty());
		Assert.assertTrue(data.getDataMap().isEmpty());
		Assert.assertEquals(0D, data.getMax().doubleValue(), 0D);
		Assert.assertEquals(0D, data.getMin().doubleValue(), 0D);
		Assert.assertEquals(0D, data.getAverage().doubleValue(), 0D);
	}

	@Test
	public void addsAndRemovesValues() {
		final ChartData data = ChartData.create();
		Assert.assertSame(data, data.add("Mon", 120));
		Assert.assertFalse(data.isEmpty());
		Assert.assertTrue(data.has("Mon"));
		Assert.assertEquals(120, data.get("Mon"));
		Assert.assertSame(data, data.remove("Mon"));
		Assert.assertFalse(data.has("Mon"));
		Assert.assertNull(data.get("Mon"));
		Assert.assertTrue(data.isEmpty());
	}

	@Test
	public void measuresItsValues() {
		final ChartData data = ChartData.create().add("Mon", 2).add("Tue", 8.5D).add("Wed", 5L);
		Assert.assertEquals(8.5D, data.getMax().doubleValue(), 0D);
		Assert.assertEquals(2D, data.getMin().doubleValue(), 0D);
		Assert.assertEquals(5.1666D, data.getAverage().doubleValue(), 0.0001D);
	}

	@Test
	public void readsTheGivenMap() {
		final Map<String, Number> values = new HashMap<>();
		values.put("Mon", 3);
		final ChartData data = ChartData.create(values);
		Assert.assertSame(values, data.getDataMap());
		Assert.assertEquals(3, data.get("Mon"));
	}

	@Test
	public void replacesItsMap() {
		final Map<String, Number> values = new HashMap<>();
		values.put("Sun", 7);
		final ChartData data = ChartData.create().add("Mon", 1);
		Assert.assertSame(data, data.dataMap(values));
		Assert.assertFalse(data.has("Mon"));
		Assert.assertEquals(7D, data.getMax().doubleValue(), 0D);
	}

	@Test
	public void staysEmptyWithoutAMap() {
		Assert.assertTrue(ChartData.create(null).isEmpty());
		Assert.assertTrue(ChartData.create().dataMap(null).isEmpty());
	}

	private static void assertBox(final Draw draw, final double left, final double top, final double right, final double bottom) {
		Assert.assertEquals(left, draw.getLeft(), 1E-3D);
		Assert.assertEquals(top, draw.getTop(), 1E-3D);
		Assert.assertEquals(right, draw.getRight(), 1E-3D);
		Assert.assertEquals(bottom, draw.getBottom(), 1E-3D);
	}

	@UIData(background = false)
	public static final class NodeUI extends UI {

		private final Node[] nodes;

		private NodeUI(final Node... nodes) {
			this.nodes = nodes;
		}

		@Override
		public void init() {
			super.add(this.nodes);
		}

	}

	public static final class BarChartNode extends ChartNode {

		private BarChartNode(final double x, final double y, final double width, final double height) {
			super(x, y, width, height);
		}

		@Override
		public void draw(final double mouseX, final double mouseY) {
			if (!super.isLoaded()) {
				return;
			}

			final double width = super.getWidth() / super.getLabels().size();
			final ChartData data = super.getData("api");
			int index = 0;
			for (final String label : super.getLabels()) {
				final double height = data.get(label).doubleValue() / super.getMax().doubleValue() * super.getHeight();
				DrawUtils.SHAPE.drawRect(super.getX() + index * width, super.getY() + super.getHeight() - height, width, height, new Color(0.2F, 0.4F, 0.6F, 1F));
				index++;
			}
		}

	}

}