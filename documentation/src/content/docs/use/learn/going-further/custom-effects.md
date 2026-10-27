---
title: "Custom effects"
description: "Write a NodeEffect of your own: draw around a node, change how it draws, process its pixels, and place it among the other effects."
genre: learn
sidebar:
  order: 6
---

A custom effect is a treatment that goes on any node, like the built-in effects: a hard shadow, a lift on hover, a color filter.
It extends [[NodeEffect]] and changes how the node is drawn, never its layout or the area that receives the mouse.

**You will learn**
- How to draw around a node
- How to change how a node and its children draw
- How to process the pixels of a node with a shader pass
- How to place your effect among the other effects of a node

## Draw around a node

An effect wraps the drawing of its node: `draw(node, body)` runs `body`, which draws the node, its children and its layers, and can draw before and after it.
This one draws a flat shadow under the node:

```java file="com/example/further/HardShadowNodeEffect.java" region="effect"
```

```java file="com/example/further/EffectsUI.java" region="shadow"
```

<figure>

![A white rectangle with a dark grey rectangle of the same size offset to its bottom right, on a light grey background](/captures/further-effects-shadow.webp)

<figcaption>
The effect draws the dark rectangle, then runs `body`, which draws the node over it.
</figcaption>
</figure>

`draw` runs in the coordinates of the parent, like the `draw` of a [custom node](/use/learn/going-further/custom-nodes/): the node sits at `getX()`, `getY()`.
Always run `body` once, or the node disappears.
The constructor passes the priority to `super(...)`; the last section explains it.

[[Node#effect]] adds the effect; a node holds one effect per class, so a second effect of the same class replaces the first.
`removeEffect(class)` and `clearEffects()` remove them, and `getEffect(class)` returns the one a node holds.

## Change how a node draws

Code around `body` changes everything that `body` draws.
This effect lifts the node while the pointer is over it, by moving the matrix:

```java file="com/example/further/LiftNodeEffect.java" region="effect"
```

```java file="com/example/further/EffectsUI.java" region="lift"
```

<figure>

![The pointer moves onto a grey rectangle, which rises by a few pixels, then leaves it, and the rectangle comes back down](/captures/further-effects-lift.webp)

<figcaption>
`hoverValue` follows the hover progress of the node, so the lift eases in and out.
</figcaption>
</figure>

[[Node#hoverValue]] scales a value by the hover progress of the node, from `0F` to `1F` ([Hover and tooltips](/use/learn/interactivity/hover-and-tooltips/)).
The node is drawn higher, but the area that receives the mouse stays where the layout put it.
Restore what you change, here with `with(...)` of the [[MatrixStack]] ([Drawing resources and transforms](/use/learn/going-further/drawing-resources-and-transforms/#use-the-matrix-stack)).

`shouldApply(node)` decides on every frame whether the effect applies: here, a disabled node does not lift.

## Process the pixels of a node

An effect that changes the pixels returns a [shader pass](/use/learn/going-further/shaders/#write-a-shader-pass) from `toShaderPass(node)`; JOID draws the node into a buffer and runs the pass over it:

```java file="com/example/further/InvertNodeEffect.java" region="effect"
```

```java file="com/example/further/EffectsUI.java" region="invert"
```

<figure>

![On a light grey background, a light grey rectangle, then a light grey rectangle with a dark grey disc inside it](/captures/further-effects-invert.webp)

<figcaption>
Both rectangles are dark grey and the disc is white: the first effect inverts the node alone, the second inverts the node and its children.
</figcaption>
</figure>

`toShaderPasses(node)` returns several passes, for an effect that needs more than one, such as a blur in two directions.
The [[NodeEffect.NodeEffectScope]] decides what the passes process:

- `SELF`, the default, processes what the node draws itself.
- `CHILDREN` processes the node with its children and its layers.

`scope(...)` sets it, as in the second rectangle.
The passes of all the effects of a node run in one pipeline per scope, in the order of the priorities of their effects.

## Place your effect among the others

The priority orders the effects of a node: the lowest priority wraps all the others, and its passes run first.
The built-in effects use priorities from `-100`, the transform, to `200`, the border ([Effects](/use/learn/style-and-motion/effects/) lists them).
`priority(...)` changes the priority of one effect, and both `priority` and `scope` also take a `Supplier`.

| Hook | JOID calls it |
|---|---|
| `init(node, ui)` | When the node joins a UI, or when you add the effect to a node that is already in one. |
| `draw(node, body)` | On every frame while the effect applies, around the drawing of the node. |
| `shouldApply(node)` | On every frame, before the node draws. |
| `toShaderPass(node)`, `toShaderPasses(node)` | On every frame while the effect applies. |
| `detach(node)` | When the node leaves its UI, or when the effect is removed or replaced. |

The type parameter limits the nodes that take the effect: an effect for [[RectNode]], `NodeEffect<RectNode>`, goes only on a rectangle node and its subclasses, and `draw` receives it typed.
`copy()` of a node copies its effects field by field: an effect that holds something of its own instance, such as a framebuffer, creates it again in `init`.

## Recap

- A custom effect extends **[[NodeEffect]]** and changes how a node draws, never its layout or its mouse area.
- **`draw(node, body)`** draws around the node; it runs **`body`** once.
- Change the matrix or the render state around `body`, and **restore** it.
- **`shouldApply`** turns the effect on or off on every frame.
- **`toShaderPass`** processes the pixels; the **scope** chooses the node alone or with its children.
- The **priority** orders the effects: the lowest one wraps the others and runs its passes first.

## Try it

**A colored shadow.**
Give the hard shadow the violet [[Color]] `new Color(124, 92, 255)` and an offset of 6 design pixels.

<details>
<summary>Solution</summary>

```java fragment
.effect(HardShadowNodeEffect.create(new Color(124, 92, 255), 6D))
```

</details>

**Lift only when enabled and hovered.**
The lift already follows the hover; make a disabled node keep its place even while it is hovered.

<details>
<summary>Solution</summary>

It already does: `shouldApply` returns `false` for a disabled node, so JOID skips `draw` for it.
Call `enabled(false)` on the node to see it.

</details>

**Shadow under the lift.**
Make the hard shadow stay on the ground while the node lifts.

<details>
<summary>Solution</summary>

The shadow must wrap the lift, so give it the lower priority: `-60` is above `-70`, so lower it, for example with `.priority(-80)` on the shadow.
The shadow then draws first and stays in place, and the lift moves only the node.

</details>
