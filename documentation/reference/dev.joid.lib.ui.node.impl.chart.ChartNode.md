---
summary: Base class of a chart that shows named series of values over the labels of an X axis.
---
A chart draws nothing by itself: a subclass draws its data, and the chart computes the bounds of the scale.
It waits until it has data and every series has a value, drawing its skeleton meanwhile.

Learn: [Charts](/use/learn/going-further/charts/).

## ChartNode(double,double,double,double)

Called by a subclass with the position and the size of the chart.
The chart throws an `IllegalStateException` when it is attached without an X axis or a Y axis.

## xAxis

The X axis: its labels and the series of the chart.

## yAxis

The Y axis: its name and the prefix and suffix of its values.

## data(String,ChartData)

Adds or replaces the series of this name.
Throws an `IllegalStateException` when the chart has no X axis yet.

## removeData(String)

Removes the series of this name.
Throws an `IllegalStateException` when the chart has no X axis yet.

## getData(String)

The series of this name, or `null`.

## getDataMap()

Every series by name, in the order they were added; a read-only view.

## getLabels()

The labels of the X axis.

## getMin()

Bottom of the scale over every series: the smallest value, or `0` when every value is the same and positive.

## getMin(String)

Bottom of the scale of one series.

## getMax()

Top of the scale over every series: the largest value, or `1` when every value is `0`.

## getMax(String)

Top of the scale of one series.

## getAverage()

Average of the averages of the series.

## getAverage(String)

Average of one series.

## scaleMin(double,double)

Bottom of the scale for these smallest and largest values: the smallest, or `0` when both are equal and positive.

## scaleMax(double,double)

Top of the scale for these smallest and largest values: the largest, `0` when both are equal and negative, or `1` when both are `0`.
