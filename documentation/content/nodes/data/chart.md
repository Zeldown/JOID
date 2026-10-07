# ChartNode

`ChartNode` (`dev.joid.lib.ui.node.impl.structure.chart`) is the base of charts built on an X axis of labels and named series of values: line charts, bar charts, area charts. It stores the axes and the series and computes the scale; it is abstract and draws nothing, so you subclass it and draw the chart your way.

```java
public class LineChartNode extends ChartNode {

	private static final Color[] SERIES = {Color.decode("#999999"), Color.decode("#555555")};

	private final TextInfo info;

	protected LineChartNode(final double x, final double y, final double width, final double height, final TextInfo info) {
		super(x, y, width, height);
		this.info = info;
	}

	public static @NonNull LineChartNode create(final double x, final double y, final double width, final double height, final @NonNull TextInfo info) {
		return new LineChartNode(x, y, width, height, info);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		DrawUtils.SHAPE.drawRect(super.getX(), super.getY(), super.getWidth(), super.getHeight(), Color.WHITE);
		if (!super.isLoaded()) {
			return;
		}

		final double min = super.getMin().doubleValue();
		final double max = super.getMax().doubleValue();
		final double left = super.getX() + 100D;
		final double top = super.getY() + 20D;
		final double width = super.getWidth() - 130D;
		final double height = super.getHeight() - 60D;
		final double step = width / Math.max(1, super.getLabels().size() - 1);
		DrawUtils.TEXT.drawText(super.getX() + 10D, top, super.getYAxis().format(max), this.info, Align.START, Align.CENTER);
		DrawUtils.TEXT.drawText(super.getX() + 10D, top + height, super.getYAxis().format(min), this.info, Align.START, Align.CENTER);

		int index = 0;
		for (final String label : super.getLabels()) {
			DrawUtils.TEXT.drawText(left + step * index, top + height + 12D, label, this.info, Align.CENTER, Align.START);
			index++;
		}

		int series = 0;
		for (final ChartData data : super.getDataMap().values()) {
			final Color color = LineChartNode.SERIES[series % LineChartNode.SERIES.length];
			Vector2d last = null;
			double x = left;
			for (final String label : super.getLabels()) {
				final Number value = data.get(label);
				if (value != null) {
					final Vector2d point = new Vector2d(x, top + height * (1D - (value.doubleValue() - min) / (max - min)));
					if (last != null) {
						DrawUtils.SHAPE.drawLine(color, 2F, last, point);
					}
					DrawUtils.SHAPE.drawCircle(point.x, point.y, color, 4D);
					last = point;
				}
				x += step;
			}
			series++;
		}
	}

}
```

Then create it with its axes and a series (`this.info` is a `TextInfo` built from a loaded font, see [Text and TextInfo](../../text/text-and-textinfo.md)):

```java
LineChartNode
.create(100, 100, 480, 280, this.info)
.xAxis(ChartAxis.x("day", "Mon", "Tue", "Wed", "Thu", "Fri"))
.yAxis(ChartAxis.y("visits"))
.data("Visits", ChartData.create().add("Mon", 2).add("Tue", 6).add("Wed", 4).add("Thu", 8).add("Fri", 7))
.attach(this);
```

![A gray line with five points on a white card, Mon to Fri, between 2.0 and 8.0](../../images/chart-line.png "The Visits series drawn by LineChartNode, scaled from getMin() to getMax().")

`ChartAxis` and `ChartData` are nested classes: `ChartNode.ChartAxis` (with `ChartAxis.XChartAxis` and `ChartAxis.YChartAxis`) and `ChartNode.ChartData`. `Vector2d` comes from `javax.vecmath`; the drawing calls are described in [Shapes](../../drawing/shapes.md) and [Drawing Text](../../drawing/text.md). The protected constructor and the `create` factory follow the [custom node](../custom-nodes.md) contract.

## Axes with xAxis and yAxis

`ChartAxis.x(name, labels...)` creates the X axis with its labels, in order (a repeated label is kept once, at its first position); `ChartAxis.y(name)` creates the Y axis. Set them with `xAxis(...)` and `yAxis(...)`.

The series live in the X axis: set the X axis before adding a series, and know that a new X axis comes with its own series. Like every node setter, `xAxis(...)` and `yAxis(...)` also take a `Supplier`: a `map(...)` that builds the axis and its series follows a [signal](../../state/signals.md).

```java
LineChartNode
.create(100, 100, 480, 280, this.info)
.yAxis(ChartAxis.y("visits"))
.xAxis(this.visits.map(visits -> ChartAxis.x("day", "Mon", "Tue", "Wed").data("Visits", ChartData.create().add("Mon", 2).add("Tue", 6).add("Wed", visits))))
.attach(this);
```

## Several series with data

`data(name, series)` adds a series, or replaces the series of that name. The series keep the order in which their names were first added, so your `draw` can give each one its color:

```java
LineChartNode
.create(100, 100, 480, 280, this.info)
.xAxis(ChartAxis.x("day", "Mon", "Tue", "Wed", "Thu", "Fri"))
.yAxis(ChartAxis.y("count"))
.data("Visits", ChartData.create().add("Mon", 2).add("Tue", 6).add("Wed", 4).add("Thu", 8).add("Fri", 7))
.data("Orders", ChartData.create().add("Mon", 1).add("Tue", 2).add("Wed", 3).add("Thu", 3).add("Fri", 5))
.attach(this);
```

![Two lines on one chart: a light gray Visits line and a dark gray Orders line below it, between 1.0 and 8.0](../../images/chart-series.png "getMin() and getMax() cover every series: the scale goes from 1 (Orders) to 8 (Visits).")

A series does not need a value for every label: `get(label)` returns `null` for a missing one, so check it in `draw`, as above.

## Value labels with prefix and suffix

The Y axis formats the values for your labels: `format(value)` puts the value between the prefix and the suffix.

```java
LineChartNode
.create(100, 100, 480, 280, this.info)
.xAxis(ChartAxis.x("month", "Jan", "Feb", "Mar", "Apr"))
.yAxis(ChartAxis.y("price").prefix("$").suffix(" k"))
.data("Price", ChartData.create().add("Jan", 10).add("Feb", 12.5D).add("Mar", 11).add("Apr", 15))
.attach(this);
```

![A line chart whose value labels read $15.0 k at the top and $10.0 k at the bottom](../../images/chart-format.png "LineChartNode draws getYAxis().format(max) and format(min).")

`format` writes the value with `toString()`: the scale methods return `double` values, so `15` shows as `15.0`. Format the number yourself (`String.format`) for another notation. The node draws nothing from the Y axis: the name, the prefix and the suffix are there for your `draw`.

## Updating the data

The chart reads its series on every frame. Change a `ChartData` with `add(...)` or `remove(...)`, or call `data(...)` again on the node, and the next frame shows it:

```java
private int week;

@Override
public void init() {
	final LineChartNode chart = LineChartNode
			.create(100, 100, 480, 280, this.info)
			.xAxis(ChartAxis.x("day", "Mon", "Tue", "Wed", "Thu"))
			.yAxis(ChartAxis.y("value"))
			.data("Week", ChartData.create().add("Mon", 1).add("Tue", 3).add("Wed", 2).add("Thu", 4))
			.attach(this);

	RectNode
	.create(600, 100, 120, 60)
	.color(Color.decode("#999999"))
	.onClick((node, mouseX, mouseY, clickType) -> {
		this.week++;
		chart.data("Week", ChartData.create().add("Mon", 1 + this.week % 3).add("Tue", 3 - this.week % 3).add("Wed", 2 + this.week % 2).add("Thu", 4 - this.week % 4));
	})
	.body(rect -> {
		TextNode.create(60, 30).text(Text.create("Next", this.white)).anchor(Align.CENTER).attach(rect);
	})
	.attach(this);
}
```

![The cursor clicks a Next button three times: the line of the chart takes a new shape and new min and max labels at each click](../../images/chart-update.gif "Each click calls data(...) with a new Week series; the scale follows the new values.")

## Scale with getMin and getMax

| Method | Returns |
| --- | --- |
| `getMax()` | Largest value of all series. |
| `getMin()` | Smallest value of all series. |
| `getAverage()` | Average of the averages of the series. |
| `getMax(String)`, `getMin(String)`, `getAverage(String)` | The same for one series. A missing series gives the scale `0` to `1` and an average of `0`. |

- The scale starts at the smallest value, not at `0`: a series from 2 to 8 is drawn from 2 to 8.
- When all the values are equal, `getMin()` returns `0` and `getMax()` twice the value (`1` when the value is `0`). Without series, the scale is `0` to `1`.
- `getMax() - getMin()` is therefore never `0`: you can divide by it.

## Loading state with isLoaded

`isLoaded()` returns `true` once the node is mounted (no pending `wait(...)` condition), both axes are set, at least one series exists and no series is empty. Test it at the start of `draw` to draw a placeholder or nothing.

While the node waits for a `wait(...)` condition, it draws the default pulsing skeleton over its bounds instead of `draw` (see [Node Fundamentals](../node-fundamentals.md)); override `drawSkeleton` to draw your own:

```java
LineChartNode
.create(100, 100, 480, 280, this.info)
.xAxis(ChartAxis.x("day", "Mon", "Tue", "Wed"))
.yAxis(ChartAxis.y("value"))
.data("Visits", ChartData.create().add("Mon", 2).add("Tue", 6).add("Wed", 4))
.wait(2, TimeUnit.SECONDS)
.attach(this);
```

## Reference

### ChartNode

| Method | Description |
| --- | --- |
| `ChartNode(double x, double y, double width, double height)` | Protected constructor for your subclass. |
| `xAxis(XChartAxis)`, `xAxis(Supplier<XChartAxis>)` | Sets the X axis (labels and series), or follows it. |
| `yAxis(YChartAxis)`, `yAxis(Supplier<YChartAxis>)` | Sets the Y axis, or follows it. |
| `data(String dataName, ChartData data)` | Adds or replaces a series. Throws `IllegalStateException("You must set the X axis before adding data")` without X axis. |
| `remove(String data)` | Removes a series. Throws `IllegalStateException` without X axis. |
| `getXAxis()`, `getYAxis()` | The axes, or `null`. |
| `getLabels()` | The X labels, in order; empty without X axis. |
| `getData(String)` | The series of that name, or `null`. |
| `getDataMap()` | The series by name, in the order they were first added; empty without X axis. |
| `getMin()`, `getMax()`, `getAverage()` and their `(String)` overloads | Scale of all series, or of one. |
| `isLoaded()` | `true` when the chart has everything to draw. |

### ChartAxis

| Method | Description |
| --- | --- |
| `ChartAxis.x(String name, String... labels)` | Creates the X axis with its labels. |
| `ChartAxis.y(String name)` | Creates the Y axis. |
| `getName()` | Name of the axis. |

| `XChartAxis` method | Description |
| --- | --- |
| `labelSet(String... labels)`, `labelSet(Set<String> labelSet)` | Replaces the labels (a copy of the set, in its iteration order). |
| `data(String dataName, ChartData data)`, `remove(String data)` | Adds, replaces or removes a series. |
| `get(String data)` | The series of that name, or `null`. |
| `getLabelSet()`, `getDataMap()` | The ordered labels, and the series by name. |

| `YChartAxis` method | Description |
| --- | --- |
| `prefix(String)`, `suffix(String)` | Text before and after the formatted values. Default `null` (nothing). |
| `format(Number value)` | Prefix + `value.toString()` + suffix: `ChartAxis.y("price").prefix("$").suffix(" k").format(12.5D)` gives `$12.5 k`. |
| `getPrefix()`, `getSuffix()` | The texts, or `null`. |

### ChartData

| Method | Description |
| --- | --- |
| `ChartData.create()` | Creates an empty series. |
| `ChartData.create(Map<String, Number> dataMap)` | Creates a series backed by your map (not copied). |
| `add(String label, Number data)` | Sets the value of a label. |
| `remove(String label)` | Removes the value of a label. |
| `dataMap(Map<String, Number> dataMap)` | Replaces the backing map. |
| `get(String label)` | The value of a label, or `null`. |
| `has(String label)` | `true` when the label has a value. |
| `isEmpty()` | `true` without values (or without map). |
| `getMax()`, `getMin()`, `getAverage()` | Statistics of the values; `0` for an empty series. |
| `getDataMap()` | The backing map. |

The node setters return the node itself, typed by the generic return of the fluent API. The rest of the node API is inherited from `Node` (see [Node Fundamentals](../node-fundamentals.md)).

## Pitfalls

- Set the X axis before `data(...)` or `remove(...)`: both throw `IllegalStateException` without it.
- Setting a new X axis drops the series of the previous one: they belong to the axis.
- `ChartData.create(map)` keeps your map: changing the map changes the chart at the next frame.
- An empty series keeps `isLoaded()` at `false`: the chart draws its placeholder until the series has a value.

## See also

- [RadarChartNode](radar-chart.md)
- [Custom Nodes](../custom-nodes.md)
- [Shapes](../../drawing/shapes.md)
- [Drawing Text](../../drawing/text.md)
- [Signals](../../state/signals.md)