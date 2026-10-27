---
summary: Base class of a radar chart, which shows one value per axis around a center.
---
A radar chart draws nothing by itself: a subclass draws its data.
It waits until it has at least three entries and every entry has a value, drawing its skeleton meanwhile.

Learn: [Charts](/use/learn/going-further/charts/#draw-a-radar-chart).

## RadarChartNode(double,double,double,double)

Called by a subclass with the position and the size of the chart.

## data(DATA)

Adds an entry, after the others.

## removeData(DATA)

Removes an entry.

## clear()

Removes every entry.

## getDataList()

The entries, in order; a read-only view.

## getMin()

Bottom of the scale over the entries that have a value.

## getMax()

Top of the scale over the entries that have a value.

## getAverage()

Average of the entries that have a value.
