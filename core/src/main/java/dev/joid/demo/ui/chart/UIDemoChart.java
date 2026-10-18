package dev.joid.demo.ui.chart;

import dev.joid.demo.DemoFont;
import dev.joid.demo.ui.UIDemo;
import dev.joid.demo.ui.node.impl.chart.DemoChartNode;
import dev.joid.demo.ui.node.impl.chart.DemoRadarChartNode;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.chart.ChartNode.ChartAxis;
import dev.joid.lib.ui.node.impl.chart.ChartNode.ChartData;
import dev.joid.lib.ui.node.impl.chart.RadarChartNode.RadarChartData;
import dev.joid.lib.ui.node.impl.shape.RectNode;
import dev.joid.lib.ui.node.impl.text.TextNode;
import dev.joid.lib.utils.align.Align;

@UIData(background = true)
public class UIDemoChart extends UIDemo {

	private static final Color INK         = new Color(153, 153, 153).lock();
	private static final Color PLACEHOLDER = new Color(221, 221, 221).lock();

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(DemoFont.MONTSERRAT, 24).color(UIDemoChart.INK);
		final TextInfo label = TextInfo.create(DemoFont.MONTSERRAT, 22).color(Color.WHITE);
		final IntegerSignal week = new IntegerSignal(0);

		RectNode
		.create(100, 210, 400, 260)
		.color(UIDemoChart.PLACEHOLDER)
		.body(rect -> {
			DemoChartNode
			.create(20, 20, 360, 220)
			.xAxis(ChartAxis.x("day", "Mon", "Tue", "Wed", "Thu", "Fri"))
			.yAxis(ChartAxis.y("value"))
			.data("Visits", ChartData.create().add("Mon", 2).add("Tue", 6).add("Wed", 4).add("Thu", 8).add("Fri", 7))
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Smooth", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 210, 400, 260)
		.color(UIDemoChart.PLACEHOLDER)
		.body(rect -> {
			DemoChartNode
			.create(20, 20, 360, 220)
			.smooth(false)
			.xAxis(ChartAxis.x("day", "Mon", "Tue", "Wed", "Thu", "Fri"))
			.yAxis(ChartAxis.y("value"))
			.data("Visits", ChartData.create().add("Mon", 2).add("Tue", 6).add("Wed", 4).add("Thu", 8).add("Fri", 7))
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Straight", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 210, 400, 260)
		.color(UIDemoChart.PLACEHOLDER)
		.body(rect -> {
			DemoChartNode
			.create(20, 20, 360, 220)
			.xAxis(ChartAxis.x("day", "Mon", "Tue", "Wed", "Thu", "Fri"))
			.yAxis(ChartAxis.y("value"))
			.data("Visits", ChartData.create().add("Mon", 2).add("Tue", 6).add("Wed", 4).add("Thu", 8).add("Fri", 7))
			.data("Orders", ChartData.create().add("Mon", 1).add("Tue", 2).add("Wed", 3).add("Thu", 3).add("Fri", 5))
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Two series", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 210, 400, 260)
		.color(UIDemoChart.PLACEHOLDER)
		.body(rect -> {
			DemoChartNode
			.create(20, 20, 360, 220)
			.xAxis(ChartAxis.x("month", "Jan", "Feb", "Mar", "Apr"))
			.yAxis(ChartAxis.y("price").prefix("$").suffix(" k"))
			.data("Price", ChartData.create().add("Jan", 10).add("Feb", 12.5D).add("Mar", 11).add("Apr", 15))
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Prefix and suffix", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(100, 550, 400, 260)
		.color(UIDemoChart.PLACEHOLDER)
		.body(rect -> {
			DemoChartNode.create(20, 20, 360, 220).xAxis(ChartAxis.x("day", "Mon", "Tue", "Wed")).yAxis(ChartAxis.y("value")).attach(rect);
			TextNode.create(200, 275).text(Text.create("No data", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(540, 550, 400, 260)
		.color(UIDemoChart.PLACEHOLDER)
		.body(rect -> {
			DemoRadarChartNode
			.create(110, 40, 180, 180)
			.data(RadarChartData.create("A", 5))
			.data(RadarChartData.create("B", 3))
			.data(RadarChartData.create("C", 4))
			.data(RadarChartData.create("D", 2))
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Radar", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(980, 550, 400, 260)
		.color(UIDemoChart.PLACEHOLDER)
		.body(rect -> {
			DemoRadarChartNode
			.create(110, 40, 180, 180)
			.data(RadarChartData.create("A", 5))
			.data(RadarChartData.create("B", 4))
			.data(RadarChartData.create("C", 2))
			.data(RadarChartData.create("D", 5))
			.data(RadarChartData.create("E", 3))
			.data(RadarChartData.create("F", 4))
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("Six axes", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);

		RectNode
		.create(1420, 550, 400, 260)
		.color(UIDemoChart.PLACEHOLDER)
		.body(rect -> {
			final DemoChartNode chart = DemoChartNode
			.create(20, 20, 260, 220)
			.xAxis(ChartAxis.x("day", "Mon", "Tue", "Wed", "Thu"))
			.yAxis(ChartAxis.y("value"))
			.data("Week", ChartData.create().add("Mon", 1).add("Tue", 3).add("Wed", 2).add("Thu", 4))
			.attach(rect);
			RectNode
			.create(290, 20, 90, 50)
			.color(UIDemoChart.INK)
			.onClick(event -> {
				week.increment();
				chart.data("Week", ChartData.create().add("Mon", 1 + week.peek() % 3).add("Tue", 3 - week.peek() % 3).add("Wed", 2 + week.peek() % 2).add("Thu", 4 - week.peek() % 4));
			})
			.label(Text.create("Next", label))
			.attach(rect);
			TextNode.create(200, 275).text(Text.create("New data", info)).anchorX(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}