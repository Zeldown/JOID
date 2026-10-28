package com.example.catalog;

import com.example.ExampleFont;
import com.example.further.ChartsLineChartNode;
import com.example.further.ChartsRadarChartNode;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.obj.ObjModel;
import dev.joid.lib.resource.Resource;
import dev.joid.lib.ui.node.impl.chart.ChartNode.ChartAxis;
import dev.joid.lib.ui.node.impl.chart.ChartNode.ChartData;
import dev.joid.lib.ui.node.impl.chart.RadarChartNode.RadarChartData;
import dev.joid.lib.ui.node.impl.layout.ContainerNode;
import dev.joid.lib.ui.node.impl.layout.FlexNode;
import dev.joid.lib.ui.node.impl.layout.GridNode;
import dev.joid.lib.ui.node.impl.layout.ReorderableFlexNode;
import dev.joid.lib.ui.node.impl.model.ModelNode;
import dev.joid.lib.ui.node.impl.model.ModelViewerNode;
import dev.joid.lib.ui.node.impl.progress.ProgressNode;
import dev.joid.lib.ui.node.impl.resource.ResourceNode;
import dev.joid.lib.ui.node.impl.resource.ResourcePlayerNode;
import dev.joid.lib.ui.node.impl.shape.CircleNode;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

public class NodesCatalogUI extends CatalogUI {

	private static final ObjModel CUBE = ObjModel.of("cube", "models/cube.obj", Resource.of("models/texture.png"));

	private final long start = BridgeHandler.CLOCK.get().currentTimeMillis();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(ExampleFont.REGULAR, 28F).color(CatalogUI.TEXT);

		super.card(0).body(card -> RectNode.create(90, 70, 160, 160).color(CatalogUI.INK).hoveredColor(CatalogUI.ACTIVE).attach(card)).attach(this);

		super.card(1).body(card -> CircleNode.create(90, 70, 160).color(CatalogUI.INK).hoveredColor(CatalogUI.ACTIVE).attach(card)).attach(this);

		super.card(2).body(card -> TextNode.create(0, 0, 340, 300).text(Text.create("Text", info).align(Align.CENTER)).attach(card)).attach(this);

		super.card(3).body(card -> ResourceNode.create(30, 71, 280, 158).resource(Resource.of("images/placeholder.png")).hoveredResource(Resource.of("models/texture.png")).attach(card)).attach(this);

		super.card(4).body(card -> ResourcePlayerNode.create(30, 71, 280, 158).resource(Resource.of("images/animation.gif")).loop(true).attach(card)).attach(this);

		super.card(5).body(card -> {
			ProgressNode
			.create(40, 135, 260, 30)
			.track(RectNode.create(0, 0, 0, 0).color(CatalogUI.ELEMENT))
			.fill(RectNode.create(0, 0, 0, 0).color(CatalogUI.INK))
			.progress(() -> this.getProgress())
			.attach(card);
		})
		.attach(this);

		super.card(6).body(card -> ModelNode.create(100, 80, 140, 140).rotationYaw(35D).rotationPitch(-25D).model(NodesCatalogUI.CUBE).attach(card)).attach(this);

		super.card(7).body(card -> ModelViewerNode.create(100, 80, 140, 140).rotationYaw(-35D).rotationPitch(-25D).model(NodesCatalogUI.CUBE).attach(card)).attach(this);

		super.card(8).body(card -> {
			ContainerNode
			.create(40, 40, 260, 220)
			.body(container -> {
				RectNode.create(0, 0, 260, 220).color(CatalogUI.ELEMENT).attach(container);
				RectNode.create(30, 30, 90, 90).color(CatalogUI.INK).attach(container);
				RectNode.create(140, 100, 90, 90).color(CatalogUI.INK).attach(container);
			})
			.attach(card);
		})
		.attach(this);

		super.card(9).body(card -> {
			FlexNode
			.horizontal(25, 110, 80)
			.margin(10D)
			.body(flex -> {
				for (int i = 0; i < 4; i++) {
					RectNode.create(0, 0, 65, 80).color(CatalogUI.INK).attach(flex);
				}
			})
			.attach(card);
		})
		.attach(this);

		super.card(10).body(card -> {
			GridNode
			.create(40, 40, 260, 220)
			.margin(10D)
			.body(grid -> {
				for (int i = 0; i < 12; i++) {
					RectNode.create(0, 0, 55, 60).color(i % 2 == 0 ? CatalogUI.INK : CatalogUI.ELEMENT).attach(grid);
				}
			})
			.attach(card);
		})
		.attach(this);

		super.card(11).body(card -> {
			ReorderableFlexNode
			.vertical(70, 40, 200)
			.margin(10D)
			.body(flex -> {
				for (int i = 0; i < 4; i++) {
					RectNode.create(0, 0, 200, 47).color(i == 1 ? CatalogUI.INK : CatalogUI.ELEMENT).attach(flex);
				}
			})
			.attach(card);
		})
		.attach(this);

		super.card(12).body(card -> {
			ChartsLineChartNode
			.create(10, 30, 320, 240)
			.xAxis(ChartAxis.x("day", "Mon", "Tue", "Wed", "Thu", "Fri"))
			.yAxis(ChartAxis.y("visits"))
			.data("Visits", ChartData.create().add("Mon", 2).add("Tue", 6).add("Wed", 4).add("Thu", 8).add("Fri", 7))
			.attach(card);
		})
		.attach(this);

		super.card(13).body(card -> {
			ChartsRadarChartNode
			.create(70, 50, 200)
			.data(RadarChartData.create("A", 5))
			.data(RadarChartData.create("B", 3))
			.data(RadarChartData.create("C", 4))
			.data(RadarChartData.create("D", 2))
			.attach(card);
		})
		.attach(this);
	}

	private float getProgress() {
		final double time = (BridgeHandler.CLOCK.get().currentTimeMillis() - this.start) / 3000D;
		return (float) (0.8D - 0.2D * Math.cos(time * 2D * Math.PI));
	}

}