package be.zeldown.joid.demo.ui.chart;

import be.zeldown.joid.demo.ui.UIDemo;
import be.zeldown.joid.demo.ui.chart.node.DemoChartNode;
import be.zeldown.joid.demo.ui.chart.node.DemoRadarChartNode;
import be.zeldown.joid.lib.ui.node.impl.structure.chart.ChartNode.ChartAxis;
import be.zeldown.joid.lib.ui.node.impl.structure.chart.ChartNode.ChartData;
import be.zeldown.joid.lib.ui.node.impl.structure.chart.RadarChartNode.RadarChartData;

public class UIDemoChart extends UIDemo {

	@Override
	public void init() {
		DemoChartNode
		.create(50, 50, 923, 345)
		.axis(ChartAxis.x("date", "13/03", "14/03", "15/03", "16/03", "17/03", "18/03", "19/03", "20/03"), ChartAxis.y("value").suffix("$"))
		.data(
				"Zeldown",
				ChartData
				.create()
				.add("13/03", 0)
				.add("14/03", 4)
				.add("15/03", 5)
				.add("16/03", 4)
				.add("17/03", 5)
				.add("18/03", 5.5)
				.add("19/03", 8)
				.add("20/03", 10)
				)
		.attach(this);

		DemoChartNode
		.create(50, 450, 923, 345)
		.smooth(false)
		.axis(ChartAxis.x("date", "13/03", "14/03", "15/03", "16/03", "17/03", "18/03", "19/03", "20/03"), ChartAxis.y("value").suffix("$"))
		.data(
				"Zeldown",
				ChartData
				.create()
				.add("13/03", 0)
				.add("14/03", 4)
				.add("15/03", 5)
				.add("16/03", 4)
				.add("17/03", 5)
				.add("18/03", 5.5)
				.add("19/03", 8)
				.add("20/03", 10)
				)
		.attach(this);

		DemoRadarChartNode
		.create(1200, 50, 345, 345)
		.data(RadarChartData.create("A", 5))
		.data(RadarChartData.create("B", 4))
		.data(RadarChartData.create("C", 4))
		.data(RadarChartData.create("D", 5))
		.attach(this);
	}

}