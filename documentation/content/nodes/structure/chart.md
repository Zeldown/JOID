# ChartNode & RadarChartNode

Data visualization nodes for line/area charts and radar charts.

## ChartNode — line / area

```java
ChartNode.create(x, y, width, height)
    .series("fps", Color.decode("#3b82f6"))
    .addPoint("fps", 60)
    .addPoint("fps", 62)
    .addPoint("fps", 58)
    .attach(parent);
```

### API

```java
chart.series(String id, Color color);      // register a series
chart.addPoint(String id, double value);    // append a data point
chart.clearSeries(String id);
chart.yMin(double);                         // fixed Y range (otherwise auto)
chart.yMax(double);
chart.maxPoints(int);                       // oldest points discarded beyond this
chart.gridColor(Color);
chart.labelInfo(TextInfo);
chart.filled(boolean);                      // area fill under the line
```

### Example — live FPS graph

```java
final ChartNode chart = ChartNode.create(40, 40, 400, 120)
    .series("fps", Color.decode("#3b82f6"))
    .yMin(0D).yMax(120D)
    .maxPoints(200)
    .filled(true)
    .effect(RoundedNodeEffect.create(8F))
    .attach(this);

this.schedule(() -> chart.addPoint("fps", getFps()), 0L, 100L);
```

## RadarChartNode — radar / spider chart

```java
RadarChartNode.create(x, y, size)
    .axis("STR").axis("DEX").axis("INT").axis("CHA").axis("WIS").axis("CON")
    .series("player", Color.decode("#3b82f6"))
    .setValue("player", "STR", 0.7D)
    .setValue("player", "DEX", 0.5D)
    .setValue("player", "INT", 0.9D)
    .attach(parent);
```

### API

```java
radar.axis(String label);
radar.series(String id, Color color);
radar.setValue(String seriesId, String axisLabel, double value);    // 0.0 → 1.0
radar.gridColor(Color);
radar.labelInfo(TextInfo);
radar.filled(boolean);
```

## Best practices

- **Limit series count** to 3–4 per chart. Beyond that, viewers can't distinguish lines.
- **Pre-normalize radar values** to `0.0 → 1.0` for consistent visualization.
- **Use `filled(true)` for single-series area charts**, `false` for multi-series comparison.

## See also

- [Color](../../drawing/color.md) — picking series colors.
