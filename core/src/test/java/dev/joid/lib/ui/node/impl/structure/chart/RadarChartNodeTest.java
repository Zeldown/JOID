package dev.joid.lib.ui.node.impl.structure.chart;

import java.util.Arrays;
import java.util.List;

import javax.vecmath.Vector2d;

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
import dev.joid.lib.ui.node.impl.structure.chart.RadarChartNode.RadarChartData;

public class RadarChartNodeTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	@Test
	public void startsWithoutData() {
		final StarChartNode chart = new StarChartNode(0D, 0D, 200D, 200D);
		Assert.assertTrue(chart.getDataList().isEmpty());
		Assert.assertFalse(chart.isLoaded());
	}

	@Test
	public void keepsTheDataInTheirOrder() {
		final RadarChartData speed = RadarChartData.create("speed", 3);
		final RadarChartData power = RadarChartData.create("power", 5);
		final StarChartNode chart = new StarChartNode(0D, 0D, 200D, 200D);
		Assert.assertSame(chart, chart.data(speed));
		Assert.assertSame(chart, chart.data(power));
		Assert.assertEquals(Arrays.asList(speed, power), chart.getDataList());
	}

	@Test
	public void removesAValue() {
		final RadarChartData speed = RadarChartData.create("speed", 3);
		final RadarChartData power = RadarChartData.create("power", 5);
		final StarChartNode chart = new StarChartNode(0D, 0D, 200D, 200D).data(speed).data(power);
		Assert.assertSame(chart, chart.remove(speed));
		Assert.assertEquals(Arrays.asList(power), chart.getDataList());
	}

	@Test
	public void clearsItsValues() {
		final StarChartNode chart = new StarChartNode(0D, 0D, 200D, 200D).data(RadarChartData.create("speed", 3)).data(RadarChartData.create("power", 5));
		Assert.assertSame(chart, chart.clear());
		Assert.assertTrue(chart.getDataList().isEmpty());
	}

	@Test
	public void loadsWithThreeValues() {
		final StarChartNode chart = new StarChartNode(0D, 0D, 200D, 200D).data(RadarChartData.create("speed", 3)).data(RadarChartData.create("power", 5));
		Assert.assertFalse(chart.isLoaded());
		chart.data(RadarChartData.create("range", 4));
		Assert.assertTrue(chart.isLoaded());
	}

	@Test
	public void waitsForEveryValue() {
		final RadarChartData range = RadarChartData.create("range");
		final StarChartNode chart = new StarChartNode(0D, 0D, 200D, 200D).data(RadarChartData.create("speed", 3)).data(RadarChartData.create("power", 5)).data(range);
		Assert.assertFalse(chart.isLoaded());
		range.value(4);
		Assert.assertTrue(chart.isLoaded());
	}

	@Test
	public void waitsForItsNodeToMount() {
		final boolean[] ready = {false};
		final StarChartNode chart = new StarChartNode(0D, 0D, 200D, 200D).wait(node -> ready[0]);
		chart.data(RadarChartData.create("speed", 3)).data(RadarChartData.create("power", 5)).data(RadarChartData.create("range", 4));
		Assert.assertFalse(chart.isLoaded());
		ready[0] = true;
		Assert.assertTrue(chart.isLoaded());
	}

	@Test
	public void measuresItsValues() {
		final StarChartNode chart = new StarChartNode(0D, 0D, 200D, 200D).data(RadarChartData.create("speed", 2)).data(RadarChartData.create("power", 8.5D)).data(RadarChartData.create("range", 5L));
		Assert.assertEquals(8.5D, chart.getMax().doubleValue(), 0D);
		Assert.assertEquals(2D, chart.getMin().doubleValue(), 0D);
		Assert.assertEquals(5.1666D, chart.getAverage().doubleValue(), 0.0001D);
	}

	@Test
	public void spreadsTheScaleOfFlatValues() {
		final StarChartNode chart = new StarChartNode(0D, 0D, 200D, 200D).data(RadarChartData.create("speed", 4)).data(RadarChartData.create("power", 4));
		Assert.assertEquals(8D, chart.getMax().doubleValue(), 0D);
		Assert.assertEquals(0D, chart.getMin().doubleValue(), 0D);
	}

	@Test
	public void givesAUnitScaleToZeros() {
		final StarChartNode chart = new StarChartNode(0D, 0D, 200D, 200D).data(RadarChartData.create("speed", 0));
		Assert.assertEquals(1D, chart.getMax().doubleValue(), 0D);
		Assert.assertEquals(0D, chart.getMin().doubleValue(), 0D);
	}

	@Test
	public void givesAUnitScaleToValuesUpToZero() {
		final StarChartNode chart = new StarChartNode(0D, 0D, 200D, 200D).data(RadarChartData.create("speed", -4)).data(RadarChartData.create("power", 0));
		Assert.assertEquals(1D, chart.getMax().doubleValue(), 0D);
		Assert.assertEquals(-4D, chart.getMin().doubleValue(), 0D);
	}

	@Test
	public void givesAUnitScaleWithoutData() {
		final StarChartNode chart = new StarChartNode(0D, 0D, 200D, 200D);
		Assert.assertEquals(1D, chart.getMax().doubleValue(), 0D);
		Assert.assertEquals(0D, chart.getMin().doubleValue(), 0D);
		Assert.assertEquals(0D, chart.getAverage().doubleValue(), 0D);
	}

	@Test
	public void drawsItsValuesOnceLoaded() {
		final StarChartNode chart = new StarChartNode(100D, 100D, 200D, 200D).data(RadarChartData.create("speed", 8)).data(RadarChartData.create("power", 4)).data(RadarChartData.create("range", 8)).data(RadarChartData.create("armor", 4));
		this.bridges.open(new NodeUI(chart)).frame();
		final List<Draw> draws = this.bridges.getRender().getDraws();
		Assert.assertEquals(1, draws.size());
		Assert.assertEquals(30, draws.get(0).getXs().length);
		Assert.assertEquals(100D - Math.sqrt(5D) / 2D, draws.get(0).getTop(), 1E-3D);
		Assert.assertEquals(300D + Math.sqrt(5D) / 2D, draws.get(0).getBottom(), 1E-3D);
		Assert.assertEquals(150D - Math.sqrt(5D) / 4D, draws.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(250D + Math.sqrt(5D) / 4D, draws.get(0).getRight(), 1E-3D);
	}

	@Test
	public void drawsTheLoadingSkeletonWhileLoading() {
		final StarChartNode chart = new StarChartNode(100D, 100D, 200D, 200D).wait(node -> false);
		chart.data(RadarChartData.create("speed", 3)).data(RadarChartData.create("power", 5)).data(RadarChartData.create("range", 4));
		this.bridges.open(new NodeUI(chart)).frame();
		final Color loading = Color.LOADING();
		final List<Draw> draws = this.bridges.getRender().getDraws(loading.r, loading.g, loading.b);
		Assert.assertEquals(1, draws.size());
		Assert.assertEquals(100D, draws.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(100D, draws.get(0).getTop(), 1E-3D);
		Assert.assertEquals(300D, draws.get(0).getRight(), 1E-3D);
		Assert.assertEquals(300D, draws.get(0).getBottom(), 1E-3D);
	}

	@Test
	public void startsWithoutValue() {
		final RadarChartData data = RadarChartData.create("speed");
		Assert.assertEquals("speed", data.getLabel());
		Assert.assertNull(data.getValue());
		Assert.assertTrue(data.isEmpty());
	}

	@Test
	public void startsWithAValue() {
		final RadarChartData data = RadarChartData.create("speed", 3);
		Assert.assertEquals("speed", data.getLabel());
		Assert.assertEquals(3, data.getValue());
		Assert.assertFalse(data.isEmpty());
	}

	@Test
	public void replacesItsValueAndItsLabel() {
		final RadarChartData data = RadarChartData.create("speed", 3);
		Assert.assertSame(data, data.value(7.5D));
		Assert.assertSame(data, data.label("power"));
		Assert.assertEquals(7.5D, data.getValue());
		Assert.assertEquals("power", data.getLabel());
	}

	@Test
	public void forgetsItsValue() {
		final RadarChartData data = RadarChartData.create("speed", 3);
		Assert.assertSame(data, data.clear());
		Assert.assertNull(data.getValue());
		Assert.assertTrue(data.isEmpty());
		Assert.assertEquals("speed", data.getLabel());
	}

	@Test
	public void measuresTheValuesAlreadyKnown() {
		final StarChartNode chart = new StarChartNode(0D, 0D, 200D, 200D).data(RadarChartData.create("speed", 2)).data(RadarChartData.create("power", 8)).data(RadarChartData.create("range"));
		Assert.assertEquals(8D, chart.getMax().doubleValue(), 0D);
		Assert.assertEquals(2D, chart.getMin().doubleValue(), 0D);
		Assert.assertEquals(5D, chart.getAverage().doubleValue(), 0D);
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

	public static final class StarChartNode extends RadarChartNode<RadarChartData> {

		private StarChartNode(final double x, final double y, final double width, final double height) {
			super(x, y, width, height);
		}

		@Override
		public void draw(final double mouseX, final double mouseY) {
			if (!super.isLoaded()) {
				return;
			}

			final int size = super.getDataList().size();
			final Vector2d[] points = new Vector2d[size];
			for (int i = 0; i < size; i++) {
				final double radius = super.getDataList().get(i).getValue().doubleValue() / super.getMax().doubleValue() * super.dh(2D);
				final double angle = 2D * Math.PI * i / size - Math.PI / 2D;
				points[i] = new Vector2d(super.getX() + super.dw(2D) + radius * Math.cos(angle), super.getY() + super.dh(2D) + radius * Math.sin(angle));
			}
			DrawUtils.SHAPE.drawPolygon(new Color(0.2F, 0.4F, 0.6F, 1F), points);
		}

	}

}