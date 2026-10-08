# ProgressNode

`ProgressNode` (`dev.joid.lib.ui.node.impl.design.progress`) draws a progress bar: a background filled up to a fraction by a foreground, made of colors or of two resources, in one of four directions. Use it for loading bars, health bars, volume gauges and media timelines.

## Creating a ProgressNode

```java
ProgressNode.create(100, 100, 400, 12).progress(0.25F).attach(this);
```

A rounded health bar that follows a signal:

```java
private final IntegerSignal health = IntegerSignal.of(72);
```

```java
ProgressNode
.create(20, 20, 300, 16)
.background(Color.DARKGRAY)
.foreground(Color.LIGHTGRAY)
.progress(this.health.get() / 100F)
.effect(RoundedNodeEffect.create(8F))
.attach(this);
```

![A white bar filled to a quarter on black, and a rounded light gray bar filled to 72 %](../../images/progress-basic.png "The default colors at 0.25F, and the health bar at 72 of 100.")

By default the bar is empty (`0F`), black with a white fill, and fills from left to right. The health bar reads `this.health.get()` in its expression: it follows the signal and redraws when the health changes (see [Signals and Reactivity](../../concepts/signals.md)).

## Setting the value with progress

`progress(float)` sets the filled fraction: `0F` is empty, `0.5F` half, `1F` full. A value from another range is an expression: `progress((value - min) / (max - min))`. A volume from 0 to 5 that a button raises:

```java
private final IntegerSignal volume = IntegerSignal.of(1);
```

```java
ProgressNode.create(100, 100, 400, 20).background(Color.DARKGRAY).foreground(Color.WHITE).progress(Math.min(1F, this.volume.get() / 5F)).attach(this);

RectNode
.create(100, 150, 120, 50)
.color(Color.GRAY)
.onClick((node, mouseX, mouseY, clickType) -> this.volume.increment())
.body(button -> {
	TextNode.create(60, 25).text(Text.create("+1", this.info, Align.CENTER, Align.CENTER)).anchor(Align.CENTER).attach(button);
})
.attach(this);
```

`info` is a `TextInfo` built from a loaded font (see [Text](../../essentials/text.md)).

![A volume signal changed by clicks: the bar grows by a fifth at each click](../../images/progress-signal.gif "progress follows the expression that reads the signal")

- The value is not clamped: a fraction below `0F` or above `1F` draws the fill outside the node. Clamp it in the expression (`Math.min(1F, ...)`).
- The value is not animated: a lambda (`progress(() -> this.animator.getValue())`) reads it every frame, for an animation.

## Direction with ProgressDirection

`direction(ProgressDirection)` sets where the fill starts. `ProgressDirection` is the nested enum `ProgressNode.ProgressDirection`.

| Value | Fill |
| --- | --- |
| `LEFT_TO_RIGHT` (default) | Grows from the left edge. |
| `RIGHT_TO_LEFT` | Grows from the right edge. |
| `TOP_TO_BOTTOM` | Grows from the top edge. |
| `BOTTOM_TO_TOP` | Grows from the bottom edge. |

```java
ProgressNode.create(0, 0, 20, 200).direction(ProgressDirection.BOTTOM_TO_TOP).progress(0.6F).attach(this);
```

![Four bars filled to 60 percent from the left, the right, the top and the bottom](../../images/progress-directions.png "The four ProgressDirection values at progress(0.6F).")

## Colors with background and foreground

`background(Color)` colors the whole node, `foreground(Color)` the filled part, drawn on top. Both accept gradients and suppliers.

## Images with backgroundResource and foregroundResource

```java
ProgressNode
.create(0, 0, 400, 40)
.backgroundResource(Resource.of(MyUI.class.getResourceAsStream("/textures/bar-empty.png")))
.foregroundResource(Resource.of(MyUI.class.getResourceAsStream("/textures/bar-full.png")))
.progress(0.4F)
.attach(this);
```

When both resources are set, the node draws them in place of the colors: the background resource stretched over the whole node, then the foreground resource also stretched over the whole node but masked to the filled part. The foreground image is revealed as the value grows, not squeezed. With only one resource set, the node keeps drawing the colors.

## Reference

| Method | Default | Description |
| --- | --- | --- |
| `ProgressNode.create(double x, double y, double width, double height)` | | An empty bar. |
| `progress(float)`, `progress(Supplier<Float>)` | `0F` | Filled fraction. |
| `direction(ProgressDirection)`, `direction(Supplier<ProgressDirection>)` | `LEFT_TO_RIGHT` | Where the fill starts. |
| `background(Color)`, `background(Supplier<Color>)` | `Color.BLACK` | Background color. |
| `foreground(Color)`, `foreground(Supplier<Color>)` | `Color.WHITE` | Fill color. |
| `backgroundResource(Resource)`, `backgroundResource(Supplier<Resource>)` | none | Background image. |
| `foregroundResource(Resource)`, `foregroundResource(Supplier<Resource>)` | none | Fill image. |
| `getProgress()`, `getDirection()`, `getBackground()`, `getForeground()`, `getBackgroundResource()`, `getForegroundResource()` | | Current values (resources nullable). |

## Pitfalls

- An unclamped expression (`this.done.get() / 3F` with more than three steps) draws past the end of the bar.
- Both resources are needed to draw images: with one, the colors are drawn.
- A literal `null` resource is ambiguous between the overloads: write `backgroundResource((Resource) null)`.

## See also

- Next: [TextFieldNode](../input/text-field.md)
- [SliderNode](../input/slider.md) for a value the user can drag
- [RectNode](rect.md)
- [Effects](../../styling/effects.md) for rounded bars
- [ResourcePlayerNode](resource-player.md) to drive a timeline with `onProgress`
- [Signals and Reactivity](../../concepts/signals.md)