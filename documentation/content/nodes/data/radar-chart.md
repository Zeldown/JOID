# RadarChartNode

`RadarChartNode<DATA extends RadarChartData>` (`dev.joid.lib.ui.node.impl.structure.chart`) is the abstract base of radar (spider) charts: an ordered list of labeled values, one per spoke. It stores the values and computes the scale; you subclass it and draw the chart in `draw`.

## Writing a radar chart

Subclass `RadarChartNode` following the [custom node](../custom-nodes.md) contract (protected constructor, static `create` factory) and draw from the values in `draw`:

```java
public class StatsRadarNode extends RadarChartNode<RadarChartData> {

    protected StatsRadarNode(final double x, final double y, final double width, final double height) {
        super(x, y, width, height);
    }

    public static StatsRadarNode create(final double x, final double y, final double width, final double height) {
        return new StatsRadarNode(x, y, width, height);
    }

    @Override
    public void draw(final double mouseX, final double mouseY) {
        if (!super.isLoaded()) {
            return;
        }

        final int size = super.getDataList().size();
        final double max = super.getMax().doubleValue();
        final Vector2d[] points = new Vector2d[size];
        for (int i = 0; i < size; i++) {
            final double radius = super.getDataList().get(i).getValue().doubleValue() / max * super.dh(2D);
            final double angle = 2D * Math.PI * i / size - Math.PI / 2D;
            points[i] = new Vector2d(super.getX() + super.dw(2D) + radius * Math.cos(angle), super.getY() + super.dh(2D) + radius * Math.sin(angle));
        }

        DrawUtils.SHAPE.drawPolygon(new Color(239, 57, 38), points);
    }

}
```

Then create it with its values:

```java
StatsRadarNode
    .create(1200, 50, 345, 345)
    .data(RadarChartData.create("Speed", 5))
    .data(RadarChartData.create("Power", 4))
    .data(RadarChartData.create("Range", 4))
    .data(RadarChartData.create("Armor", 5))
    .attach(this);
```

![A red four-sided polygon with longer top and left spokes](../../images/chart-radar.png "Speed (top) and Armor (left) reach the edge at 5; Power and Range stop at 4/5 of the radius.")

`RadarChartData` is the nested class `RadarChartNode.RadarChartData`. `Vector2d` comes from `javax.vecmath`; the drawing calls are described in [Shapes](../../drawing/shapes.md).

## Values with RadarChartData

| Method | Description |
| --- | --- |
| `RadarChartData.create(String label)` | Creates a value without number yet (empty). |
| `RadarChartData.create(String label, Number value)` | Creates a value. |
| `value(Number value)` | Sets the number. |
| `label(String label)` | Sets the label. |
| `clear()` | Removes the number; the value becomes empty. |
| `getLabel()`, `getValue()` | The label and the number (`null` when empty). |
| `isEmpty()` | `true` while the number is `null`. |

The node reads the values on every frame: change a `RadarChartData` and the next frame shows it. An empty value lets you declare a spoke before its number arrives.

### Custom value types

The `DATA` type parameter lets a chart carry more than a label and a number. Extend `RadarChartData` (its constructors are protected) and declare the chart with your type:

```java
public class ColoredRadarData extends RadarChartData {

    private final Color color;

    protected ColoredRadarData(final String label, final Number value, final Color color) {
        super(label, value);
        this.color = color;
    }

    public static ColoredRadarData create(final String label, final Number value, final Color color) {
        return new ColoredRadarData(label, value, color);
    }

    public Color getColor() {
        return this.color;
    }

}
```

A `RadarChartNode<ColoredRadarData>` then accepts only `ColoredRadarData` in `data(...)`, and `getDataList()` returns them typed.

## Values on the node

| Method | Description |
| --- | --- |
| `data(DATA data)` | Appends a value; spokes keep the order of the calls. |
| `remove(DATA data)` | Removes a value. |
| `clear()` | Removes every value. |
| `getDataList()` | The node's own list of values, in order. Reorder values through it. |

## Scale with getMin and getMax

| Method | Description |
| --- | --- |
| `getMax()` | Largest number among the non-empty values. |
| `getMin()` | Smallest number among the non-empty values. |
| `getAverage()` | Average of the non-empty values, `0` without any. |

When all the numbers are equal, `getMin()` returns `0` and `getMax()` returns twice the number. When the largest number is `0`, and without values, `getMax()` returns `1`. `getMax()` and `getMax() - getMin()` are therefore never `0`, so you can divide by them safely.

## Loading state with isLoaded

`isLoaded()` returns `true` when the node is mounted (no pending `wait(...)` condition), it has at least 3 values, and no value is empty. Test it at the start of `draw`: `getValue()` is `null` on an empty value.

While the node waits for a `wait(...)` condition, it draws the default pulsing grey placeholder over its bounds (see [Node Fundamentals](../node-fundamentals.md)). Override `drawSkeleton` to draw your own.

## Reference

| Method | Description |
| --- | --- |
| `data(DATA data)`, `remove(DATA data)`, `clear()` | Appends, removes or clears the values. They return the node, typed by the generic return of the fluent API. |
| `getDataList()` | The values, in order. |
| `getMin()`, `getMax()`, `getAverage()` | Scale of the non-empty values. |
| `isLoaded()` | `true` when the chart has everything to draw. |

The rest of the node API is inherited from `Node` (see [Node Fundamentals](../node-fundamentals.md)).

## See also

- [ChartNode](chart.md)
- [Custom Nodes](../custom-nodes.md)
- [Shapes](../../drawing/shapes.md)