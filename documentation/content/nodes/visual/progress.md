# ProgressNode

`ProgressNode` (`dev.joid.lib.ui.node.impl.design.progress`) draws a progress bar: a background filled up to a fraction by a foreground, made of colors or of two resources, in one of four directions. Use it for loading bars, health bars, volume gauges and media timelines.

## Creating a ProgressNode

```java
ProgressNode.create(100, 100, 400, 12).progress(0.25F).attach(this);
```

A rounded health bar:

```java
final ProgressNode health = ProgressNode
    .create(20, 20, 300, 16)
    .color(Color.DARKGRAY, Color.RED)
    .progress(0F, 100F, 72F)
    .effect(RoundedNodeEffect.create(8F))
    .attach(this);
```

![A white bar filled to a quarter on black, and a rounded red health bar filled to 72 %](../../images/progress-basic.png "The default colors at 0.25F, and the health bar at 72 of 100.")

By default the bar is empty (`0F`), black with a white fill, and fills from left to right.

## Setting the value with progress

| Method | Description |
| --- | --- |
| `progress(float progress)` | Sets the filled fraction: `0F` = empty, `0.5F` = half, `1F` = full. |
| `progress(float min, float max, float value)` | Sets the fraction of `value` in the range: `(value - min) / (max - min)`. |

- The value is not clamped: a fraction below `0F` or above `1F` draws the fill outside the node. With `min == max`, the fraction is not finite; avoid an empty range.
- The value is not animated and has no supplier overload: call `progress(...)` again when your value changes, for example from another node's callback, an [`onUpdate` callback](../../interactions/callbacks.md) or a [watched signal](../../state/watch.md).

```java
health.progress(0F, 100F, 35F);
```

## Direction with ProgressDirection

`direction(ProgressDirection)` sets where the fill starts. `ProgressDirection` is the nested enum `ProgressNode.ProgressDirection`.

| Value | Fill |
| --- | --- |
| `LEFT_TO_RIGHT` (default) | Grows from the left edge. |
| `RIGHT_TO_LEFT` | Grows from the right edge. |
| `TOP_TO_BOTTOM` | Grows from the top edge. |
| `BOTTOM_TO_TOP` | Grows from the bottom edge. |

![Four bars filled to 60 percent from the left, the right, the top and the bottom](../../images/progress-directions.png "The four ProgressDirection values at progress(0.6F).")

```java
ProgressNode.create(0, 0, 20, 200).direction(ProgressDirection.BOTTOM_TO_TOP).progress(0.6F).attach(this);
```

## Colors

| Method | Description |
| --- | --- |
| `color(Color background, Color foreground)` | Sets both colors. |
| `background(Color color)` | Sets the background color. |
| `foreground(Color color)` | Sets the fill color. |

The background covers the whole node; the foreground is a rectangle covering the filled part, drawn on top.

## Resources

| Method | Description |
| --- | --- |
| `resource(Resource background, Resource foreground)` | Sets both resources. |
| `background(Resource resource)` | Sets the background resource. |
| `foreground(Resource resource)` | Sets the fill resource. |

When both resources are set, the node draws them instead of the colors: the background resource stretched over the whole node, then the foreground resource also stretched over the whole node but masked to the filled part. The foreground image is revealed as the value grows, not squeezed. With only one resource set, the node keeps drawing the colors.

```java
ProgressNode
    .create(0, 0, 400, 40)
    .resource(Resource.of(MyUI.class.getResourceAsStream("/textures/bar-empty.png")), Resource.of(MyUI.class.getResourceAsStream("/textures/bar-full.png")))
    .progress(0.4F)
    .attach(this);
```

## Reference

### Factory

| Method | Description |
| --- | --- |
| `ProgressNode.create(double x, double y, double width, double height)` | Creates an empty bar. |

### Properties

| Method | Default | Description |
| --- | --- | --- |
| `progress(float)` / `progress(float, float, float)` | `0F` | Filled fraction. |
| `direction(ProgressDirection)` | `LEFT_TO_RIGHT` | Where the fill starts. |
| `color(Color, Color)` | `Color.BLACK`, `Color.WHITE` | Background and fill colors. |
| `background(Color)` / `foreground(Color)` | | One of the colors. |
| `resource(Resource, Resource)` | none | Background and fill resources. |
| `background(Resource)` / `foreground(Resource)` | | One of the resources. |

Every setter returns the node itself, typed by the generic return of the fluent API.

### Getters

| Method | Description |
| --- | --- |
| `getProgress()` | The filled fraction. |
| `getDirection()` | The current `ProgressDirection`. |
| `getColors()` | `Color[]` of two entries: background, then foreground. |
| `getResources()` | `Resource[]` of two entries: background, then foreground; `null` entries when not set. |

## See also

- [SliderNode](../input/slider.md) for a value the user can drag
- [RectNode](rect.md)
- [Effects](../../styling/effects.md) for rounded bars
- [ResourcePlayerNode](resource-player.md) to drive a timeline with `onProgress`