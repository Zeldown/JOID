# RadarChartNode

`RadarChartNode<DATA extends RadarChartData>` is the base of radar (spider) charts: an ordered list of labeled values, one per spoke. It stores the values and computes the scale; it draws nothing, so you subclass it and draw the chart your way.

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

Create it with its values (`this.info` is a `TextInfo` built from a loaded font, see [Text and Fonts](../../concepts/text.md)):

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

The first spoke points up and the next ones turn clockwise in this example. `drawPolygon` fills a fan from its first point, so the value polygon starts at the center and closes on its first spoke. `RadarChartData` is a nested class of `RadarChartNode`.

## Values with RadarChartData

`RadarChartData.create(label, value)` creates a value; `create(label)` creates an empty one. `data(...)` appends a value: the spokes follow the order of the calls. `value(...)` and `label(...)` take a plain value, an expression that reads [signals](../../concepts/state.md), or a `Supplier`, and the chart reads them on every frame.

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
	.onClick((node, mouseX, mouseY, button) -> this.speed.set(this.speed.peek() % 5 + 1))
	.body(rect -> {
		TextNode.create(60, 30).text(Text.create("Speed", this.info)).anchor(Align.CENTER).attach(rect);
	})
	.attach(this);
}
```

![The cursor clicks a Speed button four times: the Speed spoke of the radar grows, then falls back, and the whole shape rescales](../../images/radar-signal.gif "value(this.speed.get()) follows speed; getMax() follows the largest value.")

`remove(data)` and `clear()` on the node remove one value or all of them; the next frame draws the new spokes.

## Custom values

Extend `RadarChartData` (its constructors are protected) to carry more per spoke, and type your node with it:

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

| Method | Description |
|---|---|
| `data(DATA)` | Appends a value (a new spoke). |
| `remove(DATA)`, `clear()` | Removes a value, or every value. |
| `getDataList()` | The values, in order. |
| `getMax()`, `getMin()`, `getAverage()` | Scale of the non-empty values. `getMax()` is never `0`. |
| `isLoaded()` | `true` when mounted, with at least three values, none empty. |
| `RadarChartData.create(label)`, `create(label, value)` | Creates an empty value, or a value. |
| `value(Number)`, `value(Supplier)` | Sets the value, or follows it. |
| `label(String)`, `label(Supplier)` | Sets the label, or follows it. |
| `clear()` (on the data) | Empties the value. |
| `getValue()`, `getLabel()`, `isEmpty()` | Current value (`null` when empty), label, emptiness. |

## Good to know

- Test `isLoaded()` at the start of `draw`: `getValue()` of an empty value is `null`, and with fewer than three values the chart is not loaded.
- Divide by `getMax()` for a radar scaled on its largest value, or by your own fixed maximum (`5D`) to keep the scale steady.

## See also

- Next: [Effects](../../styling/effects.md)
- [ChartNode](chart.md)
- [Custom Nodes](../custom-nodes.md)
- [Drawing](../../drawing/drawing.md)
- [Signals and State](../../concepts/state.md)