---
title: "Drawing resources and transforms"
description: "Draw images, move, rotate and scale what you draw, and change the render state and the blending."
genre: learn
sidebar:
  order: 3
---

Shapes and text are not all you draw by hand: images go on the canvas too, and any drawing can turn, grow or blend with what is under it.
This page continues [Drawing shapes and text](/use/learn/going-further/drawing/) with resources, transformations and the render state.

**You will learn**
- How to draw an image stretched, cropped or tinted
- How to rotate and scale a drawing around a point
- How to use the matrix stack directly
- How to change the render state without breaking the next draws
- How to blend a drawing with what is under it

## Draw an image

The `RESOURCE` drawer of [[DrawUtils]], a [[DrawResource]], draws a [[Resource]]: an image, a vector image, a frame of an animation or a video.

```java file="com/example/further/TransformsUI.java" region="image"
```

```java file="com/example/further/TransformsUI.java" region="resource"
```

<figure>

![The same grey placeholder image four times on a light grey background: in a 320 by 180 box, stretched into a wide band, a small square cut from its middle, and tinted darker](/captures/further-transforms-resource.webp)

<figcaption>
A box, a stretched band, a region of the source, and the image tinted by a bound color.
</figcaption>
</figure>

- `drawResource(x, y, resource)` draws the image at the size of the resource, in design pixels.
- `drawResource(x, y, width, height, resource)` stretches it over the box.
- `drawResource(x, y, width, height, u, v, regionWidth, regionHeight, resource)` draws only a region of the source, in pixels of the image from its top-left corner.

A resource with its own `textureCoords(...)` draws only that region; the region of the third form is then counted from the corner of that region ([Images and media](/use/learn/describing-the-ui/media/)).
An image that is not loaded yet draws nothing and appears on the frame its content is ready.
Keep a resource in a field: created in drawing code, it would be created again on every frame.
An animated image or a video drawn this way plays on its own, and [Images and media](/use/learn/describing-the-ui/media/) shows how to control it.

To tint a drawing, run it inside [[Color#bind]]: with `color.bind(drawing, bounds, true)`, the image is multiplied by the color, and a gradient spreads over `bounds`, a [[BoundingBox]].

## Rotate and scale a drawing

A [[Transformation]] is a list of operations that apply to everything you draw inside `apply(...)`:

```java file="com/example/further/TransformsUI.java" region="pivot"
```

<figure>

![On a light grey background, two white outlines mark where two grey rectangles start; each rectangle is turned by 20 degrees around a dark dot, the first at its center, the second at its top-left corner](/captures/further-transforms-pivot.webp)

<figcaption>
The same rotation around two pivots, marked by the dots: the pivot is the point that stays in place.
The outlines show the starting place.
</figcaption>
</figure>

| Operation | Effect |
|---|---|
| `translate(vector)` | Moves what you draw by a [[Vector]], `Vector.create(x, y)`, in design pixels. |
| `rotate(angle, rotation, pivot)` | Turns it by `angle` degrees around `pivot`: [[Rotation#ROLL]] in the plane of the screen (clockwise for a positive angle), `YAW` around the vertical axis, `PITCH` around the horizontal one. |
| `scale(scale, pivot)` | Scales it around `pivot` by a [[Scale]], `Scale.create(width, height, depth)`: `1D` keeps the size, `-1D` mirrors. |

Operations apply in the order you add them, each one in the space that the previous ones produced.
`apply(drawing)` saves the matrix, applies the operations, runs the drawing and restores the matrix, even when the drawing throws.

The angle of `rotate`, and each value of [[Vector]], [[Scale]] and [[Rotation]], can be a `Supplier`, which `apply` calls each time.
Build the transformation once in a field, and it follows your values:

```java file="com/example/further/TransformsUI.java" region="spin"
```

Then apply it each time you draw, here in a [layer](/use/learn/going-further/drawing/#choose-where-to-draw):

```java file="com/example/further/TransformsUI.java" region="spin-draw"
```

The square turns a full circle every 4 seconds of the clock of the backend.
To move, turn or scale a whole node and its children, give it a [[TransformNodeEffect]] instead ([Effects](/use/learn/style-and-motion/effects/)).

## Use the matrix stack

A transformation is a shortcut for the model-view [[MatrixStack]] of the render bridge, which you can drive yourself:

```java file="com/example/further/TransformsUI.java" region="matrix"
```

<figure>

![On a light grey background, a white rectangle turned by a few degrees counterclockwise and 1.5 times larger than the grey outline of its node](/captures/further-transforms-matrix.webp)

<figcaption>
Translate to the center, rotate, scale, then draw around the origin.
The outline of the node shows its own bounds.
</figcaption>
</figure>

`with(body)` saves the matrix, runs your code and restores the matrix, even when the code throws.
Inside, `translate`, `rotate` and `scale` change it.
The calls compose like in OpenGL: the last call applies first to what you draw.
Here the rectangle is drawn around the origin, scaled, rotated, then moved to the center of the node.
X goes right and Y goes down, in design pixels; `rotate(angle, x, y, z)` turns by `angle` degrees around the axis `(x, y, z)`.

:::caution
`push()` and `pop()` save and restore the matrix by hand: call `pop()` in a `finally` block.
An exception between them leaves the matrix shifted, and every following frame draws in the wrong place.
:::

The render bridge, `RENDER` of [[BridgeHandler]], holds a second stack, the projection, in `getProjection()`; drawing code rarely touches it.

## Change the render state

The render state holds the line settings, the blending, the current color, the texture and the shader.
Change it inside `withState(...)`, so that what follows draws as before:

```java file="com/example/further/TransformsUI.java" region="state"
```

<figure>

![A grey triangle outlined by a thick line with smooth edges, on a light grey background](/captures/further-transforms-state.webp)

<figcaption>
`lineSmooth` and `lineWidth` apply to the next lines, until `withState` ends.
</figcaption>
</figure>

[[IRenderBridge#withState]] saves the state, runs your code and restores the state, even when the code throws.
[[RenderState]] setters return the state, so they chain: `lineWidth`, `lineSmooth`, `color`, `blend`, `texture`, `textureFilter`, `textureWrap` and `shader` are the ones that drawing code uses.
`withState` does not save the matrices: combine it with `with` when you change both.

## Blend with what is under

The blending decides how a drawing combines with the pixels already drawn.
A multiply blend darkens what is under it, like ink on paper:

```java file="com/example/further/TransformsUI.java" region="multiply"
```

```java file="com/example/further/TransformsUI.java" region="blend"
```

<figure>

![Two grey squares overlap on a light grey background; where they overlap, the grey is darker](/captures/further-transforms-blend.webp)

<figcaption>
With a multiply blend, the second square darkens the first where they overlap.
</figcaption>
</figure>

[[BlendState]] has three presets: `NORMAL`, the usual transparency; `PREMULTIPLIED`, for colors already multiplied by their alpha; and `DISABLED`, which replaces the pixels.
`BlendState.of(equation, source, destination)` builds any other one from a [[BlendState.Equation]] and two [[BlendState.Factor]] values, the operands of the blend function of the graphics card.

The calls of `DrawUtils` set the blending they need.
A blend of yours applies to the vertices you send yourself with the [[Tessellator]], as above; [Framebuffers and models](/use/learn/going-further/framebuffers-and-models/) explains the tessellator.

## Recap

- `DrawUtils.RESOURCE` draws a **resource** at its size, stretched, or as a region of the source.
- `color.bind(...)` **tints** what you draw inside it.
- A **`Transformation`** translates, rotates and scales a drawing around a **pivot**, in the order of its operations.
- The **`MatrixStack`** does the same by hand inside **`with(...)`**: the last call applies first.
- Change the **render state** inside **`withState(...)`**.
- A **`BlendState`** decides how a drawing combines with what is under it; it applies to your own geometry.

## Try it

**Mirror an image.**
Draw the placeholder image flipped horizontally, in the same 320 × 180 box.

<details>
<summary>Solution</summary>

Scale by `-1D` on X around the center of the box:

```java fragment
Transformation
.create()
.scale(Scale.create(-1D, 1D, 1D), Vector.create(160D, 90D))
.apply(() -> DrawUtils.RESOURCE.drawResource(0D, 0D, 320D, 180D, this.image));
```

</details>

**Swing instead of spin.**
Make the square of `spin` swing between -15 and 15 degrees instead of turning.

<details>
<summary>Solution</summary>

Give the rotation a supplier that follows a sine of the time:

```java fragment
() -> 15D * Math.sin(BridgeHandler.CLOCK.get().currentTimeMillis() / 500D)
```

</details>
