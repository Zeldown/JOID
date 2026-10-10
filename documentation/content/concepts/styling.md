# Styling

JOID imposes no look: you style nodes with colors and effects. A `Color` fills shapes, text and borders; an effect rounds, outlines, blurs or moves the rendering of any node. Both take values, signals or lambdas.

```java
RectNode
.create(100, 100, 300, 80)
.color(Color.DARKGRAY)
.hoveredColor(Color.GRAY)
.borderColor(Color.GRAY)
.hoveredBorderColor(Color.WHITE)
.borderStroke(2D)
.attach(this);
```

![The cursor hovers a dark gray rectangle: it lightens and its border turns white](../images/ess-styling-hover.gif "The fill and the border blend to their hovered colors in 200 ms.")

`RectNode`, `CircleNode` and `ResourceNode` have a color and a hovered color, and blend from one to the other with the hover progress (200 ms by default, see [Input](input.md)).

## Colors with Color

`Color` (`dev.joid.lib.color`) is an immutable RGBA value. Use a preset, float or integer components, or a decoded string:

```java
RectNode.create(100, 100, 200, 120).color(Color.RED).attach(this);
RectNode.create(320, 100, 200, 120).color(new Color(0.2F, 0.4F, 0.6F, 0.8F)).attach(this);
RectNode.create(540, 100, 200, 120).color(Color.decode("#3366CC")).attach(this);
RectNode.create(760, 100, 200, 120).color(Color.BLUE.toGradient(Color.GREEN)).attach(this);
```

![A red, a translucent steel blue, a medium blue and a blue-to-green gradient rectangle](../images/colors-basic.png "A preset, float components with alpha 0.8, a decoded hex string and a gradient.")

| Create | Example |
|---|---|
| Presets | `WHITE`, `LIGHTGRAY`, `GRAY`, `DARKGRAY`, `BLACK`, `RED`, `GREEN`, `BLUE`, `YELLOW`, `ORANGE`, `PINK`, `CYAN`, `MAGENTA`, `TRANSPARENT` |
| Float components `0F` to `1F` | `new Color(1F, 0.5F, 0F)`, `new Color(1F, 0.5F, 0F, 0.8F)` |
| Integer components `0` to `255` | `new Color(255, 128, 0)`, `new Color(255, 128, 0, 128)` |
| Strings | `Color.decode("#FF8000")`, `"#FF8000AA"`, `"rgb(255, 128, 0)"`, `"rgba(255, 128, 0, 0.5)"` |
| Animated presets | `Color.RAINBOW` (hue cycle), `Color.LOADING` (pulse for placeholders) |

![Swatches of the fourteen fixed preset colors with their names](../images/colors-presets.png "The fixed presets; TRANSPARENT draws nothing.")

## Deriving colors

Every method returns a new color:

```java
final Color base = Color.decode("#3366CC");

RectNode.create(100, 100, 140, 80).color(base).attach(this);
RectNode.create(270, 100, 140, 80).color(base.darker()).attach(this);
RectNode.create(440, 100, 140, 80).color(base.darker(0.25F)).attach(this);
RectNode.create(610, 100, 140, 80).color(base.brighter()).attach(this);
RectNode.create(780, 100, 140, 80).color(base.brighter(0.6F)).attach(this);
RectNode.create(950, 100, 140, 80).color(base.copyAlpha(0.5F)).attach(this);
```

![A blue base color next to its darker, brighter and half-transparent variants](../images/colors-derive.png "The base #3366CC, darker(), darker(0.25F), brighter(), brighter(0.6F) and copyAlpha(0.5F).")

| Method | Description |
|---|---|
| `darker()`, `darker(scale)` | Multiplies r, g, b by `1 - scale` (`0.5F` by default). |
| `brighter()`, `brighter(scale)` | Multiplies r, g, b by `1 + scale` (`0.2F` by default). |
| `copyAlpha(alpha)` | Same color with another alpha. |
| `to(target, progress)` | Blends toward `target`; `0F` gives this color, `1F` the target. |
| `encode()` | `#RRGGBBAA` string, read back by `decode`. |

## Gradients with toGradient

`toGradient(end)` makes a left-to-right gradient. A `Vector4f` of `(startX, startY, endX, endY)`, in fractions of the drawn box, gives another direction:

```java
RectNode.create(100, 100, 200, 120).color(Color.BLUE.toGradient(Color.GREEN)).attach(this);
RectNode.create(320, 100, 200, 120).color(Color.CYAN.toGradient(Color.MAGENTA, new Vector4f(0F, 0F, 0F, 1F))).attach(this);
RectNode.create(540, 100, 200, 120).color(Color.RED.toGradient(Color.YELLOW, new Vector4f(0F, 0F, 1F, 1F))).attach(this);
RectNode.create(760, 100, 200, 120).color(Color.ORANGE.toGradient(Color.PINK, new Vector4f(1F, 0F, 0F, 0F))).attach(this);
```

![Four gradient rectangles: blue to green, cyan to magenta from the top, red to yellow diagonally, orange to pink from the right](../images/colors-gradients.png "Left to right by default, then (0, 0, 0, 1) top to bottom, (0, 0, 1, 1) diagonal and (1, 0, 0, 0) right to left.")

Gradients work everywhere a color does: fills, borders, text. A fade to `Color.TRANSPARENT` works too.

## Colors that follow your state

A color expression that reads a signal is computed again when the signal changes:

```java
private final BooleanSignal selected = BooleanSignal.of(false);

RectNode
.create(100, 100, 200, 60)
.color(this.selected.get() ? Color.WHITE : Color.GRAY)
.onClick((node, mouseX, mouseY, button) -> this.selected.toggle())
.attach(this);
```

![Each click on a gray button turns it white, the next one gray again](../images/ess-styling-state.gif "The color expression reads the signal, so it is computed again on each change.")

A lambda is read every frame, for colors that move with an animation or the hover progress: `rect.color(() -> Color.RED.to(Color.BLUE, rect.hoverValue(1F)))`. See [Signals and State](state.md).

## Effects

Effects change how a node renders: `RoundedNodeEffect`, `CircleNodeEffect`, `BorderNodeEffect`, `BlurNodeEffect`, `ShadowNodeEffect`, `MaskNodeEffect` and `TransformNodeEffect`. Add them with `effect(...)`; they stack, and a border always follows rounded corners:

```java
RectNode
.create(100, 100, 300, 200)
.color(Color.WHITE)
.effect(RoundedNodeEffect.create(16F))
.effect(BorderNodeEffect.create(Color.GRAY, 2F))
.attach(this);
```

![A white rounded rectangle with a gray border that follows the corners](../images/ess-styling-effects.png "Rounded corners and a border that follows them.")

Effect settings take suppliers too. Add the effect in `self(...)`, which hands you the node, to follow its hover progress:

```java
RectNode
.create(100, 100, 300, 200)
.color(Color.WHITE)
.self(node -> node.effect(RoundedNodeEffect.create(() -> 8F + node.hoverValue(16F))))
.self(node -> node.effect(BorderNodeEffect.create(Color.GRAY, 1F).width(() -> 1F + node.hoverValue(4F))))
.attach(this);
```

![The cursor hovers a white rectangle: its corners round and its border thickens](../images/ess-styling-effect-hover.gif "The radius goes from 8 to 24 and the border from 1 to 5 with the hover progress.")

[Effects](../styling/effects.md) shows every effect with its options, and the `CHILDREN` scope that rounds a card together with its content.

## Reference

| Method | Description |
|---|---|
| `color(...)`, `hoveredColor(...)` | Fill and hovered fill of `RectNode`, `CircleNode`, `ResourceNode` (tint). |
| `borderColor(...)`, `hoveredBorderColor(...)`, `borderStroke(double)`, `borderFill(boolean)` | Border of a `RectNode`. |
| `hoverDuration(long)`, `hoverEquation(...)` | Speed and easing of the hover blend. |
| `effect(NodeEffect)` | Adds an effect; one per class. |
| `Color.decode(String)`, `toGradient(...)`, `to(...)`, `copyAlpha(...)` | Build and derive colors. |

## Good to know

- Integer arguments select the `0` to `255` constructor: `new Color(1, 0, 0)` is almost black. Write `new Color(1F, 0F, 0F)` for red.
- Effects change pixels only: clicks and hover still use the node's rectangle.
- The border of a `RectNode` and a `BorderNodeEffect` are the same effect: the last one set replaces the other.

## See also

- Next: [Text and Fonts](text.md)
- [Effects](../styling/effects.md)
- [Custom Effects](../styling/custom-effects.md)
- [Signals and State](state.md)
- [Animation](animation.md)