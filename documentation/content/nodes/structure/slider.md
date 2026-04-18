# SliderNode

Draggable value slider. Three variants exist for common types: `DoubleSliderNode`, `IntegerSliderNode`, `StringSliderNode`.

## Create

```java
DoubleSliderNode.create(x, y, width, height)
    .min(0D).max(100D)
    .value(50D)
    .attach(parent);

IntegerSliderNode.create(x, y, width, height)
    .min(0).max(10).step(1)
    .value(5)
    .attach(parent);

StringSliderNode.create(x, y, width, height)
    .values("Easy", "Normal", "Hard", "Insane")
    .value("Normal")
    .attach(parent);
```

## API (shared)

```java
slider.min(T);
slider.max(T);
slider.value(T);
slider.value(Supplier<T>);
slider.step(T);                       // snap increments (int/double)
slider.trackColor(Color);
slider.trackActiveColor(Color);       // from min to current value
slider.cursorColor(Color);
```

## Callbacks

```java
slider.onChange((node, value) -> { /* final value committed */ });
```

## Example — volume slider

```java
final FloatSignal volume = new FloatSignal(0.75F);

DoubleSliderNode.create(40, 40, 300, 24)
    .min(0D).max(1D)
    .value(volume.getOrDefault().doubleValue())
    .trackColor(Color.decode("#374151"))
    .trackActiveColor(Color.decode("#3b82f6"))
    .effect(RoundedNodeEffect.create(12F))
    .onChange((node, value) -> volume.set(value.floatValue()))
    .attach(parent);
```

## Custom cursor

Style the cursor via `SliderCursorNode`:

```java
slider.cursor(SliderCursorNode.create(0, 0, 16, 16)
    .color(Color.WHITE)
    .effect(CircleNodeEffect.create()));
```

## Best practices

- **Reactive bindings via `.value(Supplier)`** keep the slider in sync with external state.
- **Use `IntegerSliderNode` with step** for discrete values — prevents float precision weirdness.
- **Round the track** for a modern look.

## See also

- [Signals](../../state/signals.md) — bind slider value.
