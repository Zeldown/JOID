# ChartNode

`ChartNode` (`dev.joid.lib.ui.node.impl.structure.chart`) is the abstract base of charts built on an X axis of labels and named series of values: line charts, bar charts, area charts. It stores the axes and the series and computes the scale; you subclass it and draw the chart in `draw`.

## Writing a chart

Subclass `ChartNode` following the [custom node](../custom-nodes.md) contract (protected constructor, static `create` factory) and draw from the data in `draw`:

```java
public class LineChartNode extends ChartNode {

    protected LineChartNode(final double x, final double y, final double width, final double height) {
        super(x, y, width, height);
    }

    public static LineChartNode create(final double x, final double y, final double width, final double height) {
        return new LineChartNode(x, y, width, height);
    }

    @Override
    public void draw(final double mouseX, final double mouseY) {
        if (!super.isLoaded()) {
            return;
        }

        final double min = super.getMin().doubleValue();
        final double max = super.getMax().doubleValue();
        final double step = super.getWidth() / Math.max(1, super.getLabels().size() - 1);
        for (final ChartData data : super.getDataMap().values()) {
            Vector2d last = null;
            double x = super.getX();
            for (final String label : super.getLabels()) {
                final Optional<Number> value = data.get(label);
                if (value.isPresent()) {
                    final Vector2d point = new Vector2d(x, super.getY() + super.getHeight() * (1D - (value.get().doubleValue() - min) / (max - min)));
                    if (last != null) {
                        DrawUtils.SHAPE.drawLine(Color.RED, 2F, last, point);
                    }
                    last = point;
                }
                x += step;
            }
        }
    }

}
```

Then create it with its axes and series:

```java
LineChartNode
    .create(50, 50, 900, 340)
    .axis(ChartAxis.x("date", "13/03", "14/03", "15/03", "16/03"), ChartAxis.y("value").suffix("$"))
    .data("Revenue", ChartData.create().add("13/03", 0).add("14/03", 4).add("15/03", 5.5D).add("16/03", 10))
    .attach(this);
```

![A red line rising across four points](../../images/chart-line.png "The Revenue series drawn by LineChartNode: 0, 4, 5.5 and 10 across the four dates, scaled from getMin() to getMax().")

`ChartAxis` and `ChartData` are nested classes: `ChartNode.ChartAxis` (with `ChartAxis.XChartAxis` and `ChartAxis.YChartAxis`) and `ChartNode.ChartData`. `Vector2d` comes from `javax.vecmath`; the drawing calls are described in [Shapes](../../drawing/shapes.md).

## Axes with ChartAxis

| Factory | Description |
| --- | --- |
| `ChartAxis.x(String name, String... labels)` | Creates the X axis with its labels, in order. A repeated label is kept once, at its first position. |
| `ChartAxis.y(String name)` | Creates the Y axis. |

Set them on the node with `axis(XChartAxis)`, `axis(YChartAxis)` or `axis(XChartAxis, YChartAxis)`.

### XChartAxis

The X axis holds the labels and the series.

| Method | Description |
| --- | --- |
| `labelSet(String... labels)` | Replaces the labels. |
| `labelSet(Set<String> labelSet)` | Replaces the labels with a copy of any set, in its iteration order. |
| `data(String dataName, ChartData data)` | Adds or replaces a series. |
| `remove(String data)` | Removes a series. |
| `get(String data)` | The series of that name, as an `Optional`. |
| `getName()`, `getLabelSet()`, `getDataMap()` | The name, the ordered labels, and the series by name. |

Because the series live in the X axis, replacing the X axis also replaces the series.

### YChartAxis

| Method | Description |
| --- | --- |
| `prefix(String prefix)` / `suffix(String suffix)` | Text to put before or after the values, for example a currency. |
| `format(Number value)` | The value between the prefix and the suffix: `ChartAxis.y("price").prefix("$").format(12.5D)` gives `$12.5`. A missing prefix or suffix counts as empty. |
| `getName()`, `getPrefix()`, `getSuffix()` | The name and the texts; prefix and suffix are `null` until set. |

The node does not draw anything from the Y axis: your `draw` code writes the name as an axis title and `format(...)` as the value labels.

## Series with ChartData

A series maps each X label to a `Number`.

| Method | Description |
| --- | --- |
| `ChartData.create()` | Creates an empty series. |
| `ChartData.create(Map<String, Number> dataMap)` | Creates a series backed by your map (not copied). A `null` map makes an empty series. |
| `add(String label, Number data)` | Sets the value of a label. |
| `remove(String label)` | Removes the value of a label. |
| `dataMap(Map<String, Number> dataMap)` | Replaces the backing map. |
| `get(String label)` | The value of a label, as an `Optional`. |
| `has(String label)` | `true` when the label has a value. |
| `isEmpty()` | `true` when the series has no value. |
| `getMax()`, `getMin()`, `getAverage()` | Statistics of the values, `0` for an empty series. |
| `getDataMap()` | The backing map. |

A series does not need a value for every label: `get(label)` returns an empty `Optional` for a missing one, so check it in `draw`.

### Adding and removing series on the node

| Method | Description |
| --- | --- |
| `data(String dataName, ChartData data)` | Adds or replaces a series. Throws `IllegalStateException` when no X axis is set. |
| `remove(String data)` | Removes a series. Throws `IllegalStateException` when no X axis is set. |
| `getData(String data)` | The series of that name, as an `Optional`. |
| `getDataMap()` | The series by name, in the order they were first added. |
| `getLabels()` | The X labels, in order. |

The chart reads its data on every frame: change a `ChartData` with `add(...)` or `remove(...)`, or call `data(...)` again, and the next frame shows it.

## Scale with getMin and getMax

| Method | Description |
| --- | --- |
| `getMax()` | Largest value of all series. |
| `getMin()` | Smallest value of all series. |
| `getAverage()` | Average of the averages of the series. |
| `getMax(String data)`, `getMin(String data)`, `getAverage(String data)` | The same for one series. A missing series has the scale `0` to `1` and an average of `0`. |

When all the values are equal, `getMin()` returns `0` and `getMax()` returns twice the value (`1` when the value is `0`). Without series, the scale is `0` to `1`. `getMax() - getMin()` is therefore never `0`, so you can divide by it safely.

## Loading state with isLoaded

`isLoaded()` returns `true` when the node is mounted (no pending `wait(...)` condition), both axes are set, there is at least one series, and no series is empty. Test it at the start of `draw` to show a placeholder or nothing.

- While the node waits for a `wait(...)` condition, it draws the default pulsing grey placeholder over its bounds (see [Node Fundamentals](../node-fundamentals.md)). Override `drawSkeleton` to draw your own.
- Without an X axis, `getLabels()` and `getDataMap()` are empty, `getData(...)` returns an empty `Optional` and the scale is `0` to `1`.

## Reference

| Method | Description |
| --- | --- |
| `axis(XChartAxis x)`, `axis(YChartAxis y)`, `axis(XChartAxis x, YChartAxis y)` | Sets the axes. |
| `data(String, ChartData)`, `remove(String)` | Adds, replaces or removes a series. |
| `getXAxis()`, `getYAxis()` | The axes, or `null`. |
| `getLabels()`, `getData(String)`, `getDataMap()` | Labels and series. |
| `getMin()`, `getMax()`, `getAverage()` and their per-series overloads | Scale. |
| `isLoaded()` | `true` when the chart has everything to draw. |

The setters return the node itself, typed by the generic return of the fluent API. The rest of the node API is inherited from `Node` (see [Node Fundamentals](../node-fundamentals.md)).

## See also

- [RadarChartNode](radar-chart.md)
- [Custom Nodes](../custom-nodes.md)
- [Shapes](../../drawing/shapes.md)
- [Drawing Text](../../drawing/text.md) for labels and values