# SliderNode

Draggable value slider. `SliderNode<O>` is **abstract** and generic — the type parameter `O` is the value type. Three abstract subclasses cover the common cases:

- `DoubleSliderNode extends SliderNode<Double>`
- `IntegerSliderNode extends SliderNode<Integer>`
- `StringSliderNode extends SliderNode<String>`

You subclass one of them to provide `drawSlider(...)`, and pair it with an abstract `SliderCursorNode` subclass for the thumb.

## Minimal setup

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

Wire them up:

```java
new MySlider(40, 40, 300, 24)
    .values(0, 100, 50)           // min, max, initial
    .cursor(new MyCursor(16, 16))
    .onChange((node, value) -> System.out.println("value = " + value))
    .attach(parent);
```

## Base API — `SliderNode<O>`

```java
T valueSet(Set<O> values, O initial)   // set the full value set
T value(O value)                        // change current value (must be in set)
T signal(Signal<O> signal)              // bind an external signal, updated on change
T cursor(SliderCursorNode cursor)       // attach the cursor subclass

T onChange(NodeSliderChangeCallback<T, O> callback)

O getValue()
Set<O> getValueSet()
SliderCursorNode getCursor()
Signal<O> getSignal()
```

`valueSet` is the canonical set of discrete positions. The slider snaps to the nearest entry as the cursor moves. Calling `value(...)` with a value outside the set throws `IllegalArgumentException`.

### `DoubleSliderNode`

```java
T values(double min, double max, double step, double value)
T values(double value, Double... values)
```

The first overload builds a stepped range (`min`, `min+step`, …, `max`). The second accepts explicit values.

### `IntegerSliderNode`

```java
T values(int min, int max, int value)
T values(int value, Integer... values)
```

The first overload builds the inclusive integer range `[min, max]`.

### `StringSliderNode`

```java
T values(String value, String... values)
T values(Enum<?> value, Enum<?>... values)   // maps enum.name() into the set
```

## `SliderCursorNode`

```java
protected SliderCursorNode(double width, double height)

T dragging(boolean value)
T slider(SliderNode<?> parent)       // set by the slider via cursor(...)

abstract void drawCursor(double mouseX, double mouseY)
```

The base class handles drag tracking, bounds clamping, and mouse pressed/released — you only render.

## Example — volume slider with signal binding

```java
final Signal<Double> volume = new Signal<>(0.75D);

new MySlider(40, 40, 300, 24)
    .valueSet(tenths(0D, 1D), volume.getOrDefault())
    .signal(volume)
    .cursor(new MyCursor(16, 16))
    .attach(parent);
```

The `.signal(...)` binding updates `volume` automatically on every change.

## See also

- `Signals` — external state binding.