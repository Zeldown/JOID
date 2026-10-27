package com.example.further;

import com.example.ExampleFont;

import dev.joid.lib.color.Color;
import dev.joid.lib.draw.text.Text;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.signal.impl.primitive.IntegerSignal;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIData;
import dev.joid.lib.ui.node.impl.chart.ChartNode.ChartAxis;
import dev.joid.lib.ui.node.impl.chart.ChartNode.ChartData;
import dev.joid.lib.ui.node.impl.chart.RadarChartNode.RadarChartData;
import dev.joid.lib.ui.node.impl.shape.RectNode;

@UIData(background = true, backgroundColor = "#DDDDDD")
public class ChartsUI extends UI {

	private static final Color INK = new Color(153, 153, 153).lock();

	@Override
	public void init() {
		final TextInfo label = TextInfo.create(ExampleFont.REGULAR, 22F).color(Color.WHITE);
		final IntegerSignal week = new IntegerSignal(0);

		// #region line
		final ChartsLineChartNode chart = ChartsLineChartNode
		.create(100, 100, 400, 240)
		.xAxis(ChartAxis.x("day", "Mon", "Tue", "Wed", "Thu", "Fri"))
		.yAxis(ChartAxis.y("visits").suffix(" k"))
		.data("Visits", ChartData.create().add("Mon", 2).add("Tue", 6).add("Wed", 4).add("Thu", 8).add("Fri", 7))
		.data("Orders", ChartData.create().add("Mon", 1).add("Tue", 2).add("Wed", 3).add("Thu", 3).add("Fri", 5))
		.attach(this);
		// #endregion

		// #region radar
		ChartsRadarChartNode
		.create(640, 120, 200)
		.data(RadarChartData.create("Speed", 4))
		.data(RadarChartData.create("Power").value(() -> 2 + week.get() % 4))
		.data(RadarChartData.create("Range", 3))
		.data(RadarChartData.create("Grip", 5))
		.data(RadarChartData.create("Armor", 2))
		.attach(this);
		// #endregion

		// #region update
		RectNode
		.create(100, 380, 200, 50)
		.color(ChartsUI.INK)
		.label(Text.create("Next week", label))
		.onClick(event -> {
			week.increment();
			chart
			.data("Visits", ChartData.create().add("Mon", 3).add("Tue", 5).add("Wed", 7).add("Thu", 6).add("Fri", 9))
			.removeData("Orders");
		})
		.attach(this);
		// #endregion
	}

}