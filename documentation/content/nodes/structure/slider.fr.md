# SliderNode

Slider de valeur draggable. `SliderNode<O>` est **abstract** et générique — le type paramétrique `O` est le type de valeur. Trois sous-classes abstraites couvrent les cas courants :

- `DoubleSliderNode extends SliderNode<Double>`
- `IntegerSliderNode extends SliderNode<Integer>`
- `StringSliderNode extends SliderNode<String>`

Vous sous-classez l'une d'elles pour fournir `drawSlider(...)`, et la jumelez avec une sous-classe abstraite `SliderCursorNode` pour le curseur.

## Setup minimal

```java
public class MySlider extends IntegerSliderNode {
    public MySlider(double x, double y, double w, double h) { super(x, y, w, h); }

    @Override
    public void drawSlider(final double mouseX, final double mouseY) {
        DrawUtils.SHAPE.drawRoundedRect(getX(), getY() + getHeight() / 2 - 2,
            getWidth(), 4, Color.decode("#374151"), 2F);
    }
}

public class MyCursor extends SliderCursorNode {
    public MyCursor(double w, double h) { super(w, h); }

    @Override
    public void drawCursor(final double mouseX, final double mouseY) {
        DrawUtils.SHAPE.drawCircle(getX() + getWidth() / 2,
            getY() + getHeight() / 2, Color.WHITE, getWidth() / 2);
    }
}
```

Câblez :

```java
new MySlider(40, 40, 300, 24)
    .values(0, 100, 50)           // min, max, initial
    .cursor(new MyCursor(16, 16))
    .onChange((node, value) -> System.out.println("value = " + value))
    .attach(parent);
```

## API de base — `SliderNode<O>`

```java
T valueSet(Set<O> values, O initial)   // définir l'ensemble de valeurs complet
T value(O value)                        // changer la valeur courante (doit être dans le set)
T signal(Signal<O> signal)              // lier un signal externe, mis à jour au changement
T cursor(SliderCursorNode cursor)       // attacher la sous-classe de curseur

T onChange(NodeSliderChangeCallback<T, O> callback)

O getValue()
Set<O> getValueSet()
SliderCursorNode getCursor()
Signal<O> getSignal()
```

`valueSet` est l'ensemble canonique de positions discrètes. Le slider snap à l'entrée la plus proche quand le curseur bouge. Appeler `value(...)` avec une valeur hors du set throw `IllegalArgumentException`.

### `DoubleSliderNode`

```java
T values(double min, double max, double step, double value)
T values(double value, Double... values)
```

La première construit une plage par pas (`min`, `min+step`, …, `max`). La seconde accepte des valeurs explicites.

### `IntegerSliderNode`

```java
T values(int min, int max, int value)
T values(int value, Integer... values)
```

La première construit la plage entière inclusive `[min, max]`.

### `StringSliderNode`

```java
T values(String value, String... values)
T values(Enum<?> value, Enum<?>... values)   // mappe enum.name() dans le set
```

## `SliderCursorNode`

```java
protected SliderCursorNode(double width, double height)

T dragging(boolean value)
T slider(SliderNode<?> parent)       // défini par le slider via cursor(...)

abstract void drawCursor(double mouseX, double mouseY)
```

La classe de base gère le tracking du drag, le clamp aux bounds et les mouse pressed/released — vous ne faites que le rendu.

## Exemple — slider de volume avec binding signal

```java
final Signal<Double> volume = new Signal<>(0.75D);

new MySlider(40, 40, 300, 24)
    .valueSet(tenths(0D, 1D), volume.getOrDefault())
    .signal(volume)
    .cursor(new MyCursor(16, 16))
    .attach(parent);
```

Le binding `.signal(...)` met à jour `volume` automatiquement à chaque changement.

## Voir aussi

- `Signals` — binding d'état externe.