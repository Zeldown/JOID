# RadarChartNode

`RadarChartNode<DATA extends RadarChartData>` (`dev.joid.lib.ui.node.impl.structure.chart`) is the base of radar (spider) charts: an ordered list of labeled values, one per spoke. It stores the values and computes the scale; it is abstract and draws nothing, so you subclass it and draw the chart your way.

```java
public class StatsRadarNode extends RadarChartNode<RadarChartData> {

	private static final Color INK = Color.decode("#999999");

	private final TextInfo info;

	protected StatsRadarNode(final double x, final double y, final double width, final double height, final TextInfo info) {
		super(x, y, width, height);
		this.info = info;
	}

	public static @NonNull StatsRadarNode create(final double x, final double y, final double width, final double height, final @NonNull TextInfo info) {
		return new StatsRadarNode(x, y, width, height, info);
	}

	@Override
	public void draw(final double mouseX, final double mouseY) {
		if (!super.isLoaded()) {
			return;
		}

		final int size = super.getDataList().size();
		final double max = super.getMax().doubleValue();
		final Vector2d[] frame = new Vector2d[size];
		final Vector2d[] values = new Vector2d[size + 2];
		values[0] = this.getPoint(0, 0D);
		for (int i = 0; i < size; i++) {
			final RadarChartData data = super.getDataList().get(i);
			frame[i] = this.getPoint(i, super.dh(2D));
			values[i + 1] = this.getPoint(i, super.dh(2D) * data.getValue().doubleValue() / max);
			final Vector2d label = this.getPoint(i, super.dh(2D) + 40D);
			DrawUtils.TEXT.drawText(label.x, label.y, data.getLabel(), this.info, Align.CENTER, Align.CENTER);
		}

		values[size + 1] = values[1];
		DrawUtils.SHAPE.drawPolygon(Color.WHITE, frame);
		DrawUtils.SHAPE.drawPolygon(StatsRadarNode.INK, values);
	}

	private Vector2d getPoint(final int index, final double radius) {
		final double angle = 2D * Math.PI * index / super.getDataList().size() - Math.PI / 2D;
		return new Vector2d(super.getX() + super.dw(2D) + radius * Math.cos(angle), super.getY() + super.dh(2D) + radius * Math.sin(angle));
	}

}
```

Then create it with its values (`this.info` is a `TextInfo` built from a loaded font, see [Text and TextInfo](../../text/text-and-textinfo.md)):

```java
StatsRadarNode
.create(100, 100, 300, 300, this.info)
.data(RadarChartData.create("Speed", 5))
.data(RadarChartData.create("Power", 4))
.data(RadarChartData.create("Range", 3))
.data(RadarChartData.create("Armor", 4))
.data(RadarChartData.create("Grip", 5))
.attach(this);
```

![A white pentagon with a gray polygon inside: Speed and Grip reach the edge, Power and Armor stop at 4/5, Range at 3/5](../../images/chart-radar.png "Each value is drawn as a fraction of getMax(): the largest values touch the frame.")

The first spoke points up and the next ones turn clockwise. `drawPolygon` fills a fan from its first point, so the value polygon starts at the center (`values[0]`) and closes on its first spoke: any shape of values fills correctly. `RadarChartData` is the nested class `RadarChartNode.RadarChartData`; `Vector2d` comes from `javax.vecmath`. The drawing calls are described in [Shapes](../../drawing/shapes.md), the constructor and factory contract in [Custom Nodes](../custom-nodes.md).

## Values with RadarChartData

`RadarChartData.create(label, value)` creates a value with its label; `RadarChartData.create(label)` creates a value still empty. `data(...)` appends a value to the node: the spokes follow the order of the calls.

`value(...)` and `label(...)` set the value and the label later. Like node setters, they take a plain value, an expression that reads [signals](../../state/signals.md), a signal or a `Supplier`, and the chart reads them on every frame:

```java
private final IntegerSignal speed = IntegerSignal.of(2);

@Override
public void init() {
	StatsRadarNode
	.create(100, 100, 300, 300, this.info)
	.data(RadarChartData.create("Speed").value(this.speed.get()))
	.data(RadarChartData.create("Power", 4))
	.data(RadarChartData.create("Range", 3))
	.data(RadarChartData.create("Armor", 4))
	.attach(this);

	RectNode
	.create(460, 100, 120, 60)
	.color(Color.decode("#999999"))
	.onClick((node, mouseX, mouseY, clickType) -> this.speed.set(this.speed.peek() % 5 + 1))
	.body(rect -> {
		TextNode.create(60, 30).text(Text.create("Speed", this.white)).anchor(Align.CENTER).attach(rect);
	})
	.attach(this);
}
```

![The cursor clicks a Speed button four times: the Speed spoke of the radar grows, then falls back, and the whole shape rescales](../../images/radar-signal.gif "value(this.speed.get()) follows speed: each click moves the Speed spoke, and getMax() follows the largest value.")

`clear()` on a `RadarChartData` forgets its value: the data becomes empty, and the chart is not loaded until it gets a value again.

## Adding and removing values

| Method | Effect |
| --- | --- |
| `data(DATA data)` | Appends a value (a new spoke). |
| `remove(DATA data)` | Removes that value. |
| `clear()` | Removes every value. |

The list is read on every frame: the next frame draws the new spokes. `getDataList()` returns the values in their order.

## Scale with getMax

| Method | Returns |
| --- | --- |
| `getMax()` | Largest value. |
| `getMin()` | Smallest value. |
| `getAverage()` | Average of the values. |

- Empty values are ignored.
- `getMax()` is never `0`: it returns `1` when the largest value is `0` or when there is no value, and twice the value when all the values are equal (then `getMin()` returns `0`). You can divide by it.
- Draw each value as a fraction of `getMax()` for a radar scaled on its largest value, as above, or divide by a fixed maximum of your own (`5D` for a score out of 5) to keep the scale steady.

## Loading state with isLoaded

`isLoaded()` returns `true` once the node is mounted (no pending `wait(...)` condition), it has at least three values and none of them is empty. Test it at the start of `draw`: `getValue()` of an empty value returns `null`. While the node waits for a `wait(...)` condition, it draws the default pulsing skeleton over its bounds; override `drawSkeleton` to draw your own (see [Node Fundamentals](../node-fundamentals.md)).

## Custom values

`DATA` lets a radar carry more than a label and a number per spoke. Extend `RadarChartData` (its constructors are protected) and type your node with it:

```java
@Getter
public class ColoredRadarData extends RadarChartData {

	private final Color color;

	protected ColoredRadarData(final String label, final Number value, final Color color) {
		super(label, value);
		this.color = color;
	}

	public static @NonNull ColoredRadarData create(final @NonNull String label, final Number value, final @NonNull Color color) {
		return new ColoredRadarData(label, value, color);
	}

}
```

A `TeamRadarNode extends RadarChartNode<ColoredRadarData>` then accepts only `ColoredRadarData` in `data(...)`, and its `draw` reads `super.getDataList().get(i).getColor()`.

## Reference

### RadarChartNode

| Method | Description |
| --- | --- |
| `RadarChartNode(double x, double y, double width, double height)` | Protected constructor for your subclass. |
| `data(DATA data)` | Appends a value. |
| `remove(DATA data)` | Removes a value. |
| `clear()` | Removes every value. |
| `getDataList()` | The values, in order. |
| `getMax()`, `getMin()`, `getAverage()` | Scale of the values that are not empty. |
| `isLoaded()` | `true` when mounted, with at least three values, none empty. |

### RadarChartData

| Method | Description |
| --- | --- |
| `RadarChartData.create(String label)` | Creates an empty value. |
| `RadarChartData.create(String label, Number value)` | Creates a value. |
| `value(Number)`, `value(Supplier<? extends Number>)` | Sets the value, or follows it. |
| `label(String)`, `label(Supplier<String>)` | Sets the label, or follows it. |
| `clear()` | Forgets the value: the data becomes empty. |
| `getValue()` | Current value, or `null` when empty. |
| `getLabel()` | Current label. |
| `isEmpty()` | `true` without value. |

The setters return the object itself, typed by the generic return of the fluent API. The rest of the node API is inherited from `Node` (see [Node Fundamentals](../node-fundamentals.md)).

## Pitfalls

- `getValue()` of an empty value returns `null`: draw only when `isLoaded()` is `true`, or check each value.
- With fewer than three values, `isLoaded()` stays `false`: a radar of two spokes is never drawn by code that tests it.
- `clear()` exists on both classes: on the node it removes every value, on a `RadarChartData` it empties that value only.

## See also

- [ChartNode](chart.md)
- [Custom Nodes](../custom-nodes.md)
- [Shapes](../../drawing/shapes.md)
- [Drawing Text](../../drawing/text.md)
- [Reactive Properties](../../state/reactive-properties.md)