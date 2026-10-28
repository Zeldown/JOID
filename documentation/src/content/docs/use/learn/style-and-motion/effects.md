---
title: "Effects"
description: "Round, outline, shadow, blur, mask and transform any node with effects, control their order, and clip your own drawing."
genre: learn
sidebar:
  order: 2
---

An effect changes how a node is drawn: rounded corners, a border, a shadow, a blur, a mask or a transform.
It never changes the layout, and it works on every node, built-in or yours.
This page shows how to add effects, drive them with changing values, and predict the result when several of them stack.

**You will learn**
- How to add effects to a node and stack them
- How to drive an effect with a value that changes
- How to round a node together with its children
- In which order JOID applies effects, and how to change it
- How to clip your own drawing to a rectangle or an image

## Add effects to a node

[[Node#effect]] adds an effect to a node and returns the node, so effects chain like any other setter:

```java file="com/example/style/EffectsUI.java" region="stack"
```

<figure>

![A dark card with rounded corners, a thin blue border and a soft blue glow under it, the shadow of the card](/captures/style-stack.webp)

<figcaption>
Three effects on one rectangle: the border and the shadow follow the rounded corners.
</figcaption>
</figure>

A node holds one effect per class: a second [[RoundedNodeEffect]] replaces the first.
[[Node#getEffect]] returns the effect of a class, or `null`, and `hasEffect`, `removeEffect` and `clearEffects` manage the others.
JOID ships seven effects; the [catalog](/use/catalog/#effects) shows each one.

| You want | Effect |
|---|---|
| Rounded corners, on every corner or on some of them | [[RoundedNodeEffect]] |
| A round node cut from a square one | [[CircleNodeEffect]] |
| An outline that follows the drawn shape | [[BorderNodeEffect]] |
| A drop shadow or a glow | [[ShadowNodeEffect]] |
| A blurred node | [[BlurNodeEffect]] |
| Only part of a node visible, cut by a rectangle or an image | [[MaskNodeEffect]] |
| A node moved, rotated or scaled without touching the layout | [[TransformNodeEffect]] |

:::note
An effect changes pixels only.
The mouse still hits the rectangle of the node: a round node reacts in its corners, and a rotated node reacts at its layout position.
:::

Each effect takes its main values in `create(...)` and the others through setters:
- `RoundedNodeEffect.create(radius)` rounds every corner; `top(false)`, `bottom(false)`, `left(false)` and `right(false)` keep a side square, and a corner is rounded only when both of its sides are.
  The radius never goes past half the shorter side, so `create(1000F)` turns a rectangle into a pill.
- `BorderNodeEffect.create(color, width)` outlines the pixels that the node draws: rounded corners, circles, glyphs, the opaque part of an image.
  `mode(...)` takes a [[BorderShader.BorderMode]]: `BorderMode.OUT`, the default, draws it outside the shape, and `BorderMode.IN` inside; `corners(false)` leaves the corners of an `OUT` border open.
- `ShadowNodeEffect.create(color, blur)` draws a shadow of the shape of the node, moved by `offsetX(...)` and `offsetY(...)` in design pixels.
  It does not read the pixels of the node: it draws a rounded rectangle with the radius of the `RoundedNodeEffect` of the node, a disc for a [[CircleNode]] or a node with a `CircleNodeEffect`, and a plain rectangle otherwise.
- `BlurNodeEffect.create(radius)` blurs the node, by a radius in design pixels.
- `MaskNodeEffect.create()` keeps the whole node visible; `x(...)`, `y(...)`, `width(...)` and `height(...)` place the rectangle that stays visible, from the top-left corner of the node, and `resource(...)` cuts it to the opaque part of an image stretched over that rectangle.
- `TransformNodeEffect.create(transformation)` applies a [[Transformation]] around the center of the node ([Drawing resources and transforms](/use/learn/going-further/drawing-resources-and-transforms/)).

A border, a blur or a rounding of `0` does nothing, so a value that drops to `0` turns the effect off.

:::caution
The `borderColor(...)`, `borderStroke(...)` and `borderCorners(...)` setters of a [[RectNode]] drive the `BorderNodeEffect` of the node.
Give a rectangle one or the other: a `BorderNodeEffect` added after them replaces theirs, and the setters called after it change its color and its width.
:::

## Drive an effect with a changing value

Every setting of an effect takes a value or a `Supplier`, like a node setter ([Signals](/use/learn/interactivity/signals/)).
Build the suppliers that read the node inside [[Node#self]], which hands you the node:

```java file="com/example/style/EffectsUI.java" region="hover"
```

<figure>

![The pointer enters a dark rectangle: its corners round strongly and its blue border thickens, then go back as the pointer leaves](/captures/style-effect-hover.webp)

<figcaption>
The radius goes from 8 to 48 design pixels and the border from 1 to 6 with the hover progress.
</figcaption>
</figure>

`hoverValue(max)` is the hover progress of the node scaled to `max` ([Hover, tooltips and cursors](/use/learn/interactivity/hover-and-tooltips/)).
An [animator](/use/learn/style-and-motion/animation/) drives an effect through an expression: `RoundedNodeEffect.create(40F * appear.get())`.

## Round a node with its children

By default, an effect applies to the node alone, and its children draw on top of the result, untouched.
[[NodeEffect.NodeEffectScope#CHILDREN]], given to `scope(...)`, applies the effect to the node and its children together:

```java file="com/example/style/EffectsUI.java" region="scope"
```

<figure>

![Two dark cards with rounded corners and a blue header: on the left the header keeps its square top corners, on the right it is rounded with the card](/captures/style-scope.webp)

<figcaption>
With `SELF`, the default, the rounded corners cut only the card; with `CHILDREN`, they cut its header too.
</figcaption>
</figure>

Use `CHILDREN` for a card whose content reaches its edges, such as an image header or a list that scrolls.
The scope matters for the effects that work on pixels (rounded, circle, blur and border): a shadow, a mask or a transform always covers the children.

## Control the order of effects

Each effect has a priority, and JOID applies the effects of a node in a fixed frame, whatever the order you add them in:

<figure>

![Diagram of nested boxes. The outer box: 1, the wrapping effects start, lowest priority first: transform (-100), shadow (-50), mask (0), yours (0). Inside it, a second box holds a blue box: 2, the node draws itself, 3, its SELF passes by priority: rounded or circle (100), blur (150), border (200); and a dark grey box: 4, the children draw on top. Under them: 5, the CHILDREN passes run over the node and its children. At the bottom of the outer box: 6, the wrapping effects end, in reverse order.](/captures/style-effect-order.webp)

<figcaption>
The priorities decide what a border outlines, what a mask cuts and what a transform moves.
A wrapping effect contains every effect of a higher priority.
</figcaption>
</figure>

1. The effects that wrap the drawing (`TransformNodeEffect`, `ShadowNodeEffect`, `MaskNodeEffect` and most effects of your own) start, the lowest priority first.
2. The node draws itself.
3. Its `SELF` effects that work on pixels run on that drawing, by priority: rounded or circle, then blur, then border.
   A border outlines the rounded corners whatever the order you add them in.
4. The children draw on top.
5. The `CHILDREN` effects that work on pixels run on the node and its children together.
6. The wrapping effects end, in reverse order.

`priority(...)` changes the priority of an effect, and two effects of the same priority keep the order in which you added them.
In the example below, the `Transformation` turns each node by 20 degrees with [[Rotation#ROLL]], in the plane of the screen ([Rotate and scale a drawing](/use/learn/going-further/drawing-resources-and-transforms/#rotate-and-scale-a-drawing)).
A transform, at `-100`, wraps the mask, at `0`, so the mask turns with the node; a mask given a priority under `-100` wraps the transform, and stays in place while the node turns under it:

```java file="com/example/style/EffectsUI.java" region="priority"
```

<figure>

![Two faint blue rectangles turned by 20 degrees, each crossed by the solid light blue band that its mask keeps. On the left, the band turns with the rectangle. On the right, the band stays horizontal.](/captures/style-priority.webp)

<figcaption>
Left: the mask runs inside the transform and turns with the node.
Right: `priority(-200)` starts the mask before the transform, so it stays horizontal.
The faint rectangles, two other nodes, show the whole node.
</figcaption>
</figure>

The same rule places a shadow: at its own `-50`, it turns and moves with the node; with a priority under `-100`, it stays where the layout puts the node.
[Custom effects](/use/learn/going-further/custom-effects/) shows how to write an effect of your own, and where its priority places it.

## Clip your own drawing

`MaskNodeEffect` clips a node; the same clipping is open to your own drawing code.
[[UI#mask]] runs a drawing with everything outside a rectangle hidden:

```java file="com/example/style/EffectsUI.java" region="clip"
```

<figure>

![The top half of a blue disc, cut flat by the mask](/captures/style-clip.webp)

<figcaption>
The circle is drawn whole, and the mask keeps the top half of the node.
</figcaption>
</figure>

The overload that takes a [[Resource]] first keeps the pixels where the image is opaque (alpha above `0.5F`), to cut a drawing to a star, a logo or a rounded frame.
Masks nest: a mask started inside another one shows only what both of them keep.
Call `mask` while JOID draws, such as in [[Node#onDraw]] or in the `draw` of a [custom node](/use/learn/going-further/custom-nodes/): outside a frame, drawing throws.
[Drawing shapes and text](/use/learn/going-further/drawing/) explains the drawing calls.

## Recap

- **`effect(...)`** adds an effect to any node; effects stack, one per class.
- An effect never changes the **layout** nor the area that receives the mouse.
- Every setting takes a value or a supplier; **`self(...)`** gives the node to build suppliers on it.
- Effects apply to the node alone; **`CHILDREN`** applies them to the node and its children together.
- Wrapping effects run by **priority**, the lowest outermost; the pixel effects always run rounded or circle, then blur, then border.
- Give a `RectNode` its **border** through its setters or through a `BorderNodeEffect`, never both.
- **`UI.mask`** clips your own drawing to a rectangle or to the opaque part of an image.

## Try it

**A pill button.**
Turn a 200 × 60 rectangle into a pill with a 2 design pixel blue border inside the shape.

<details>
<summary>Solution</summary>

```java fragment
.effect(RoundedNodeEffect.create(30F))
.effect(BorderNodeEffect.create(EffectsUI.ACCENT, 2F).mode(BorderMode.IN))
```

A radius of half the height gives round ends.

</details>

**A shadow that stays on the ground.**
A card turns with a `TransformNodeEffect`, and its shadow must stay upright under it.
What do you change?

<details>
<summary>Solution</summary>

The priority of the shadow: `ShadowNodeEffect.create(color, 24F).priority(-200)` starts it before the transform, so the transform leaves it in place.

</details>
