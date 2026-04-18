# ChartNode

Base class for chart-style data visualisation. `ChartNode` is **abstract** — subclass it to draw your chart (line, bar, pie, radar, whatever shape you need). The base class owns the X/Y axes and a map of named `ChartData` series; it offers no renderer of its own.

## Subclass skeleton

```java
public class LineChart extends ChartNode {

    public LineChart(double x, double y, double w, double h) { super(x, y, w, h); }

    @Override
    public void draw(double mouseX, double mouseY) {
        if (!isLoaded()) return;
        // Iterate getLabels() + getDataMap() to render your lines.
    }
}
```

Then wire it up:

```java
new LineChart(40, 40, 400, 160)
    .axis(ChartAxis.x("weekday", "Mon", "Tue", "Wed", "Thu", "Fri"))
    .axis(ChartAxis.y("requests").suffix(" req"))
    .data("api", ChartData.create()
        .add("Mon", 120).add("Tue", 180).add("Wed", 160)
        .add("Thu", 210).add("Fri", 240))
    .attach(parent);
```

## API — `ChartNode`

```java
T axis(XChartAxis x, YChartAxis y)
T axis(XChartAxis x)
T axis(YChartAxis y)

T data(String dataName, ChartData data)
T remove(String dataName)

boolean isLoaded()                          // axes set + at least one non-empty series
Set<String> getLabels()                      // X axis label set
Map<String, ChartData> getDataMap()
ChartData getData(String name)

Number getAverage()
Number getAverage(String dataName)
Number getMin()
Number getMin(String dataName)
Number getMax()
Number getMax(String dataName)

XChartAxis getXAxis()
YChartAxis getYAxis()
```

No `series(...)`, no `addPoint(...)`, no `yMin/yMax`, no `maxPoints`, no `gridColor`, no `labelInfo`, no `filled` setter — those features do not exist on this class. Styling and scale are the subclass's responsibility.

## Axes

Static factories on `ChartAxis`:

```java
XChartAxis ChartAxis.x(String name, String... labels)
YChartAxis ChartAxis.y(String name)
```

`XChartAxis` holds the ordered label set and the per-series `ChartData`:

```java
XChartAxis labelSet(String... labels)
XChartAxis labelSet(Set<String> labelSet)   // LinkedHashSet only
XChartAxis data(String dataName, ChartData data)
XChartAxis remove(String dataName)
ChartData get(String dataName)
```

`YChartAxis` holds optional prefix / suffix strings for value formatting:

```java
YChartAxis prefix(String prefix)
YChartAxis suffix(String suffix)
```

## `ChartData`

```java
ChartData ChartData.create()
ChartData ChartData.create(Map<String, Number> dataMap)

T add(String label, Number value)
T remove(String label)
T dataMap(Map<String, Number> dataMap)

Number get(String label)
boolean has(String label)
boolean isEmpty()

Number getAverage()
Number getMin()
Number getMax()
```

A `ChartData` holds a label-to-number mapping. Labels should match those declared on the X axis; missing keys are treated as "no value" by your renderer.

## See also

- `Color` — picking series colours in your subclass.