---
title: "Drawing shapes and text"
description: "Draw shapes, lines and text with DrawUtils, in layers, custom nodes and effects, and show what another renderer draws."
genre: learn
sidebar:
  order: 2
---

Nodes cover most interfaces, but some pictures are not a tree of rectangles: a gauge, a graph, a decoration made of lines.
[[DrawUtils]] draws shapes, lines and text directly, on every frame, from the places that JOID gives you.
Use it for free geometry; when a part needs layout, hover, effects or input, make it a node instead.

**You will learn**
- Where to put drawing code
- How to draw rectangles, circles, polygons and shadows
- How to draw lines and curves
- How to draw text at a point or in a box
- How to show what another renderer draws

## Choose where to draw

A layer is the shortest way to draw: a lambda that a node runs on every frame, after itself and its children.

```java file="com/example/further/DrawingUI.java" region="layer"
```

<figure>

![A white card with rounded corners on a light grey background, a grey disc on its left and a light grey bar on its right](/captures/further-drawing-layer.webp)

<figcaption>
Three `DrawUtils` calls in the layer of an empty [[ContainerNode]].
</figcaption>
</figure>

[[Node#layer]] adds a [[NodeLayer]], a lambda that receives the node and the position of the mouse.
A layer draws in the coordinates of its node: `(0, 0)` is the top-left corner of the node, and the mouse position is relative to that corner too, in design pixels.
A node runs its layers in the order you add them; `layer(index, layer)` inserts one at a position and `clearLayers()` removes them all.

You can draw from four places, all inside the draw of a node:

| Place | Coordinates | Use it for |
|---|---|---|
| `layer(...)` | The node: `(0, 0)` is its corner. | Drawing over a node and its children, without writing a class. |
| `draw(...)` of a [custom node](/use/learn/going-further/custom-nodes/) | The parent: the node sits at `getX()`, `getY()`. | A reusable node that draws itself, below its children. |
| `onDraw` and `onRender` | The parent. | Drawing over one node from the outside ([Node lifecycle](/use/learn/describing-the-ui/node-lifecycle/)). |
| `draw(node, body)` of a [custom effect](/use/learn/going-further/custom-effects/) | The parent. | A treatment that applies to any node. |

[The frame lifecycle](/use/in-depth/frame-lifecycle/) gives the exact order of these steps.
A drawing call outside a frame throws `IllegalStateException`: draw only from these places.
To draw only inside an area, clip your drawing with a mask ([Effects](/use/learn/style-and-motion/effects/#clip-your-own-drawing)).

## Draw shapes

`DrawUtils.SHAPE`, a [[DrawShape]], draws filled and outlined shapes.
Rectangles take a position and a size, circles a center and a radius, polygons their points:

```java file="com/example/further/DrawingUI.java" region="shapes"
```

<figure>

![From left to right on a light grey background: a white rounded square with a soft dark shadow, a grey rounded outline, a square grey outline, a grey disc and a white triangle](/captures/further-drawing-shapes.webp)

<figcaption>
`drawShadow` then `drawRoundedRect`, `drawRoundedBorder`, `drawBorder`, `drawCircle` and `drawPolygon`.
</figcaption>
</figure>

| You want | Call |
|---|---|
| A filled rectangle | `drawRect(x, y, width, height, color)` |
| Rounded corners, on all corners or some of them | `drawRoundedRect(x, y, width, height, color, radius)`, with four `boolean`, one per side (left, top, right, bottom): a corner is rounded when both of its sides are |
| A rounded outline | `drawRoundedBorder(x, y, width, height, color, radius, stroke)` |
| A square outline | `drawBorder(x, y, width, height, color, stroke)` |
| A soft shadow of a rounded box | `drawShadow(x, y, width, height, color, radius, blur)`, before the shape it shadows |
| A disc | `drawCircle(centerX, centerY, color, radius)` |
| A filled polygon | `drawPolygon(color, points...)` |

Every value is in design pixels, and every color can be a gradient ([Colors and styling](/use/learn/style-and-motion/styling/)); `drawShadow` takes the first color of a gradient.
Outlines draw their stroke inside the box, and a radius larger than half the box is reduced to it.
Rectangles, outlines and images snap their edges to the pixels of the screen, and a rectangle thinner than a pixel fades instead of disappearing; circles, polygons and lines keep their exact geometry ([Pixel alignment](/use/in-depth/pixel-alignment/)).
Each call sets the render state it needs, so the order of your calls is the only thing that matters: each call draws over the previous ones.

## Draw lines and curves

Lines and curves go through points, given as [[Point]] in design pixels:

```java file="com/example/further/DrawingUI.java" region="lines"
```

<figure>

![On a light grey background: a grey zigzag line, a grey dashed diagonal and a white S-shaped curve](/captures/further-drawing-lines.webp)

<figcaption>
`drawLine` through three points, `drawDashedLine` and a cubic `drawCurvedLine`.
</figcaption>
</figure>

- `drawLine(color, stroke, points...)` joins the points with smooth edges.
- `drawDashedLine(color, pattern, stroke, points...)` alternates dashes and gaps of `pattern` design pixels; a pattern of 0 or less throws `IllegalArgumentException`.
- `drawCurvedLine(color, stroke, start, control, end)` draws a quadratic Bézier curve; with two control points, `(start, startControl, endControl, end)`, a cubic one.
- `stroke` is in design pixels, like the thickness of a border, so a line grows with the UI.
- `drawShape(mode, color, points...)` sends the points as they are, as `LINES`, `LINE_STRIP`, `LINE_LOOP`, `TRIANGLES`, `QUADS` or a `POLYGON` ([[DrawMode]]).

The stroke of lines and curves is in screen pixels, so a line keeps its weight whatever the size of the window; without a stroke, a line is 1 pixel wide.
The stroke of `drawBorder` and `drawRoundedBorder` is in design pixels, like the shapes it outlines.

## Draw text

`DrawUtils.TEXT`, a [[DrawText]], draws a string at a point, or a [[Text]] in a box:

```java file="com/example/further/DrawingUI.java" region="hint"
```

```java file="com/example/further/DrawingUI.java" region="text"
```

<figure>

![The word Paused centered at the top, and below it a sentence wrapped on three centered lines](/captures/further-drawing-text.webp)

<figcaption>
A word centered on a point, then a sentence wrapped in a box 400 design pixels wide.
</figcaption>
</figure>

At a point, the alignments decide which side of the text sits on the point: `START` starts there, `CENTER` centers on it, `END` ends there.
In a box, the [[TextMode]] decides whether the text stays on one line or wraps, and the alignments of the `Text` place it in the box.
The [[TextInfo]] gives the font, the size and the color, as for a [[TextNode]]; [Text](/use/learn/describing-the-ui/text/) explains the modes and the overflow.
Build a `Text` once, outside the layer: the layer runs on every frame.

Each call returns the [[FontBounds]] of what it drew.
`getLines(width, text, info)` splits a string into the lines that a box of that width holds, to measure a paragraph before you draw it.

## Show what another renderer draws

`DrawUtils.RASTER`, a [[DrawRaster]], hands a rectangle of the UI to drawing code that JOID does not know, such as the renderer of a game or a plotting library:

```java fragment
DrawUtils.RASTER.drawRaster(0D, 0D, 128D, 128D, (width, height) -> this.renderer.render(width, height));
```

JOID gives your [[DrawableRaster]] a transparent target of `width` × `height` screen pixels, at most 4096 on each side, then draws the target into the rectangle, sharp at every scale.
The drawable speaks the graphics API of the backend: OpenGL calls on LWJGL 2 and LWJGL 3, where JOID saves its own OpenGL state around them, and Vulkan commands in the render pass of the target on Vulkan.

## Recap

- [[DrawUtils]] draws **shapes**, **lines** and **text** on every frame, in design pixels.
- Draw in a **layer**, in the `draw` of a custom node, in `onDraw` or in an effect, never outside a frame.
- A **layer** draws in the coordinates of its node; the other places draw in the **coordinates of the parent**.
- Each call draws over the previous ones and sets the render state it needs.
- Line strokes are **screen pixels**; outline strokes and every position are design pixels.
- `drawText` aligns text on a point, or places a `Text` in a box with a **`TextMode`**.
- **`DrawUtils.RASTER`** shows what another renderer draws, in the graphics API of the backend.

## Try it

**A progress ring.**
Draw a grey disc with a smaller light disc on top, so that only a ring of 10 design pixels shows.

<details>
<summary>Solution</summary>

Draw the larger disc first; the second one covers its center:

```java fragment
DrawUtils.SHAPE.drawCircle(60D, 60D, DrawingUI.INK, 60D);
DrawUtils.SHAPE.drawCircle(60D, 60D, DrawingUI.LIGHT, 50D);
```

</details>

**A caption under a point.**
Draw the word `Peak` centered horizontally under the point `(400, 200)` of a layer.

<details>
<summary>Solution</summary>

`CENTER` on the horizontal axis, `START` on the vertical one: the top of the text sits on the point.

```java fragment
DrawUtils.TEXT.drawText(400D, 200D, "Peak", info, Align.CENTER, Align.START);
```

</details>

**Follow the pointer.**
Draw a small disc under the mouse while the pointer is over a node.

<details>
<summary>Solution</summary>

The layer receives the mouse in the coordinates of the node, and `isHovered()` tells whether the pointer is on it:

```java fragment
.layer((node, mouseX, mouseY) -> {
	if (node.isHovered()) {
		DrawUtils.SHAPE.drawCircle(mouseX, mouseY, DrawingUI.INK, 8D);
	}
})
```

</details>
