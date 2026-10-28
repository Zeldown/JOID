---
title: "Charts"
description: "Feed a chart with axes and series of values, update its data, and draw it with a skin of your own."
genre: learn
sidebar:
  order: 10
---

A chart node holds the data of a chart: its axes, its series and their scale.
How the chart looks is yours: like a control, a chart is an abstract class that a skin of yours draws.

**You will learn**
- How to draw a line chart from its axes and series
- How to update the data of a chart
- How to draw a radar chart
- What a chart shows while it has no data

## Draw a line chart

A [[ChartNode]] holds an X axis with its labels, a Y axis and named series of values.
A skin overrides `draw` and reads them:

```java file="com/example/further/ChartsLineChartNode.java" region="node"
```

The skin is a [custom node](/use/learn/going-further/custom-nodes/) that draws with [[DrawUtils]] ([Drawing shapes and text](/use/learn/going-further/drawing/) teaches the calls).
Give the chart its axes, then its series:

```java file="com/example/further/ChartsUI.java" region="line"
```

<figure>

![A dark grey line chart with a blue and a grey series over Mon to Fri, labeled 8 k and 1 k on the left, next to a radar chart with five axes](/captures/further-charts-start.webp)

<figcaption>The line chart draws one line per series, in the order they were added; the radar chart draws one axis per data.</figcaption>
</figure>

[[ChartNode.ChartAxis]] creates the axes.
`ChartAxis.x(name, labels...)` creates the X axis: its labels are the columns of the chart, in this order.
`ChartAxis.y(name)` creates the Y axis; `prefix(...)` and `suffix(...)` decorate the values that `format(value)` returns, here `8 k`.

`data(name, data)` adds a series, or replaces the series of that name in place; call it after `xAxis(...)`, which holds the series.
A [[ChartNode.ChartData]] maps each label to a number, and a label it does not hold has no value there: the skin decides what to draw.

A skin reads the chart through its getters, which [[ChartNode]] lists: `getLabels()` in order, `getDataMap()` by series, `getMin()`, `getMax()` and `getAverage()` over every series or for one, and the axes for `format(value)`.
`getAverage()` over every series is the average of the averages of the series.
When every value is the same, the scale stretches to `0`, so that the chart keeps a height.

A chart throws when it is attached without its two axes.

## Update the data

JOID calls `draw` on every frame, so the chart shows a change of its data on the next frame:

```java file="com/example/further/ChartsUI.java" region="update"
```

<figure>

![The charts of the previous figure; the pointer clicks Next week, and the line chart keeps one series with new values, scaled up to 9 k, while the Power axis of the radar chart grows](/captures/further-charts-next.webp)

<figcaption>The click replaces one series and removes the other; the scale follows the new values.</figcaption>
</figure>

- `data(name, data)` replaces a whole series, and `removeData(name)` removes one.
- `add(label, value)` and `remove(label)` change one value of a [[ChartNode.ChartData]] you kept.
- `ChartData.create(map)` keeps your map itself, so a change you make to the map shows too.
- `labelSet(labels...)` of the X axis changes the columns.

The axes take a `Supplier` too, so a chart can follow a signal that picks its axes ([Signals](/use/learn/interactivity/signals/) explains reactive setters).

## Draw a radar chart

A [[RadarChartNode]] holds a list of values, each with its label, drawn as the axes of a radar:

```java file="com/example/further/ChartsRadarChartNode.java" region="node"
```

```java file="com/example/further/ChartsUI.java" region="radar"
```

`data(...)` adds an axis, `removeData(...)` removes one and `clear()` removes them all; `getMin()`, `getMax()` and `getAverage()` cover every value.
A [[RadarChartNode.RadarChartData]] takes its label and its value as a value or as a `Supplier`, read each time the skin calls `getValue()` and `getLabel()`.
Here the Power axis reads a signal: the click of the previous section lengthens it.

The type parameter of `RadarChartNode` is the class of its data: extend `RadarChartData` to give each axis more fields, such as a color, and read them in the skin.

## Know what a chart shows without data

A chart waits for its data before it draws.
While it waits, it draws its skeleton, a pulsing grey block, instead of calling `draw` ([Rebuilding the tree](/use/learn/state/rebuilding/) explains skeletons):

- A [[ChartNode]] waits while it has no series, or while one of its series is empty.
- A [[RadarChartNode]] waits while it has fewer than 3 axes, or while one of them has no value; `clear()` of a `RadarChartData` empties its value.

So a skin never draws an empty chart, and needs no test for one.

## Recap

- A chart is an **abstract node**: a skin overrides `draw` and reads the axes, the series and the scale.
- A **`ChartNode`** takes an X axis with its **labels**, a Y axis that **formats** the values, then **series** by name.
- The chart reads its data **on every frame**: replace a series, remove one, or change the values you kept.
- A **`RadarChartNode`** draws one axis per `RadarChartData`, whose value can follow a `Supplier`.
- A chart draws its **skeleton** until it has data to draw.

## Try it

**An average line.**
Draw a dashed horizontal line at the average of every series.

<details>
<summary>Solution</summary>

At the end of `draw`:

```java fragment
final double average = top + height - height * (super.getAverage().doubleValue() - min) / (max - min);
final Point start = Point.of(left, average);
DrawUtils.SHAPE.drawDashedLine(ChartsLineChartNode.MUTED, 8, 1F, start, Point.of(left + width, average));
```

</details>

**A missing day.**
Leave Wednesday out of the Orders series, and see what the chart draws.

<details>
<summary>Solution</summary>

Remove `.add("Wed", 3)` from the series.
The skin skips a label that the series does not hold, so the line goes straight from Tuesday to Thursday.

</details>
