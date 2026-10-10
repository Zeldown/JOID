# RectNode

`RectNode` draws a filled rectangle with an optional border and hover colors. Use it for backgrounds, cards, buttons, separators and invisible click areas; add effects for rounded corners, shadows or blur.

```java
RectNode.create(100, 100, 400, 200).color(Color.LIGHTGRAY).attach(this);
```

![A light gray rectangle on a dark stage](../../images/rect-basic.png "A 400 × 200 RectNode filled with Color.LIGHTGRAY.")

`create(x, y, width, height)` takes canvas units. Without `color(...)`, the rectangle is transparent but still receives the mouse.

## Hover with hoveredColor

`hoveredColor(...)` and `hoveredBorderColor(...)` set the colors reached under the mouse. The drawn color blends to them with the hover animation of the node (200 ms by default), so the change fades in and out.

```java
RectNode
.create(100, 100, 400, 120)
.color(Color.DARKGRAY)
.hoveredColor(Color.GRAY)
.borderColor(Color.GRAY)
.hoveredBorderColor(Color.WHITE)
.borderStroke(2D)
.effect(RoundedNodeEffect.create(12F))
.attach(this);
```

![The cursor enters a dark gray rounded card: its fill lightens and its border turns white](../../images/rect-card-hover.gif "The fill and the border blend to their hovered colors.")

## Colors that follow signals

Every setter takes a plain value, an expression that reads [signals](../../concepts/state.md) (recomputed when they change), a signal, or a `Supplier` read on every frame.

```java
private final IntegerSignal clicks = IntegerSignal.of(0);

@Override
public void init() {
	RectNode
	.create(100, 100, 200, 120)
	.color(this.clicks.get() >= 3 ? Color.WHITE : Color.GRAY)
	.onClick((node, mouseX, mouseY, button) -> this.clicks.increment())
	.attach(this);
}
```

![A gray rectangle is clicked three times and turns white](../../images/rect-signal.gif "The color expression reads clicks: the third click turns the rectangle white.")

## Gradients

Any color can be a gradient from `Color.toGradient(...)`. It spans the bounds of the node, left to right by default; a `Vector4f` gives the start and end points as fractions of the bounds.

```java
RectNode.create(100, 100, 400, 200).color(Color.RED.toGradient(Color.BLUE)).attach(this);

RectNode.create(560, 100, 400, 200).color(Color.CYAN.toGradient(Color.MAGENTA, new Vector4f(0F, 0F, 0F, 1F))).attach(this);
```

![A red-to-blue horizontal gradient next to a cyan-to-magenta vertical gradient](../../images/rect-gradients.png "Left to right by default; Vector4f(0, 0, 0, 1) runs from top to bottom.")

## Borders with borderColor and borderStroke

The border is drawn outside the rectangle while its stroke is above `0`, and follows the shape of a `RoundedNodeEffect` or `CircleNodeEffect`.

```java
RectNode
.create(100, 100, 200, 120)
.color(Color.GRAY)
.borderColor(Color.WHITE)
.borderStroke(3D)
.effect(RoundedNodeEffect.create(20F))
.attach(this);
```

![A rounded gray rectangle with a white border](../../images/rect-border.png "The border follows the rounded corners.")

`borderFill(false)` leaves the four outer corner squares of the border empty:

```java
RectNode.create(100, 100, 200, 120).color(Color.GRAY).borderColor(Color.WHITE).borderStroke(12D).attach(this);

RectNode.create(400, 100, 200, 120).color(Color.GRAY).borderColor(Color.WHITE).borderStroke(12D).borderFill(false).attach(this);
```

![A gray rectangle with a thick white border whose outer corners are rounded, next to the same border with empty corners](../../images/rect-border-fill.png "borderFill(true) on the left, borderFill(false) on the right.")

## Reference

Every setter has a value overload and a `Supplier` overload.

| Method | Default | Description |
|---|---|---|
| `create(x, y, width, height)` | | Creates a transparent rectangle. |
| `color(Color)` | `Color.TRANSPARENT` | Fill color. |
| `hoveredColor(Color)` | none | Fill color under the mouse. |
| `borderColor(Color)` | `Color.TRANSPARENT` | Border color. |
| `hoveredBorderColor(Color)` | none | Border color under the mouse. |
| `borderStroke(double)` | `0D` | Border width; `0` draws no border. |
| `borderFill(boolean)` | `true` | Whether the border covers its outer corners. |

## Good to know

- Call the `RectNode` setters before the `Node` setters (`effect`, `onClick`) in a chain: a `Node` setter returns a `Node`, which has no `RectNode` setter.
- `hoveredColor((Color) null)` removes the hover color; a bare `null` is ambiguous.
- The border setters install a `BorderNodeEffect`: do not add your own `BorderNodeEffect` on the same node.

## See also

- Next: [CircleNode](circle.md)
- [Styling](../../concepts/styling.md)
- [Effects](../../styling/effects.md)
- [Signals and State](../../concepts/state.md)