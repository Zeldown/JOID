# ChartNode

Classe de base pour la visualisation de données sous forme de graphiques. `ChartNode` est **abstract** — sous-classez-la pour dessiner votre graphique (line, bar, pie, radar, n'importe quelle forme). La classe de base possède les axes X/Y et une map de séries `ChartData` nommées ; elle n'offre aucun renderer elle-même.

## Squelette de sous-classe

```java
public class LineChart extends ChartNode {

    public LineChart(double x, double y, double w, double h) { super(x, y, w, h); }

    @Override
    public void draw(double mouseX, double mouseY) {
        if (!isLoaded()) return;
        // Itérez getLabels() + getDataMap() pour rendre vos lignes.
    }
}
```

Puis câblez-la :

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

boolean isLoaded()                          // axes définis + au moins une série non vide
Set<String> getLabels()                      // labels de l'axe X
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

Pas de `series(...)`, pas de `addPoint(...)`, pas de `yMin/yMax`, pas de `maxPoints`, pas de `gridColor`, pas de `labelInfo`, pas de setter `filled` — ces fonctionnalités n'existent pas sur cette classe. Le styling et l'échelle sont du ressort de la sous-classe.

## Axes

Factories statiques sur `ChartAxis` :

```java
XChartAxis ChartAxis.x(String name, String... labels)
YChartAxis ChartAxis.y(String name)
```

`XChartAxis` porte le label set ordonné et les `ChartData` par série :

```java
XChartAxis labelSet(String... labels)
XChartAxis labelSet(Set<String> labelSet)   // LinkedHashSet uniquement
XChartAxis data(String dataName, ChartData data)
XChartAxis remove(String dataName)
ChartData get(String dataName)
```

`YChartAxis` porte un préfixe / suffixe optionnel pour le formatage de valeurs :

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

Un `ChartData` porte un mapping label → nombre. Les labels doivent correspondre à ceux déclarés sur l'axe X ; les clés manquantes sont traitées comme « pas de valeur » par votre renderer.

## Voir aussi

- `Color` — choisir les couleurs de séries dans votre sous-classe.