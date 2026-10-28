---
title: "Framebuffers and models"
description: "Draw into a framebuffer once, borrow a temporary target, build your own geometry and draw 3D models."
genre: learn
sidebar:
  order: 4
---

Some drawings cost too much to repeat on every frame, some need a target of their own, some need geometry that no helper draws, and some are in 3D.
This page closes the drawing chapter with the tools for these cases: framebuffers, the framebuffer pool, the [[Tessellator]] and 3D models.

**You will learn**
- How to draw once into a framebuffer and reuse the result
- How to borrow a temporary target on every frame
- How to build your own geometry, vertex by vertex
- How to use the texture of a resource in your own drawing
- How to draw a 3D model in your own drawing code

## Draw once, reuse every frame

A [[FrameBuffer]] is an offscreen image: you fill it once, then draw it as often as you need, at any size.
This node fills one on its first frame, draws it, and deletes it when it leaves its UI:

```java file="com/example/further/StampNode.java" region="node"
```

```java file="com/example/further/FramebuffersUI.java" region="stamp"
```

<figure>

![The same dark grey disc with a blue center twice: a large one and a small one to its right](/captures/further-framebuffers-stamp.webp)

<figcaption>
One framebuffer per node, filled once, drawn at 128 and 64 design pixels.
</figcaption>
</figure>

- `FrameBuffer.create(width, height, filter)` allocates the buffer, in pixels; the [[TextureFilter]] decides how it is sampled when drawn at another size.
- `fill(drawing)` draws into the buffer: inside, coordinates are pixels of the buffer from its top-left corner, and every [[DrawUtils]] call works.
- `draw(x, y, width, height)` draws the content in a box, in design pixels.
- `delete()` releases the memory of the graphics card.

Nothing clears the buffer: clear it at the start of `fill` through the render bridge, `RENDER` of [[BridgeHandler]]: `BridgeHandler.RENDER.get().clearColor(red, green, blue, alpha)`, as above.
Fill it again only when its content changes; `draw` before the first `fill` throws `IllegalStateException`.
`fill(x, y, width, height, drawing)` maps a region of your coordinates onto the whole buffer, to capture what you draw at its usual place.

:::danger
A framebuffer holds memory on the graphics card until `delete()`.
Create it lazily in `draw`, keep it in a `transient` field, and delete it in `detach()`: a framebuffer created on every frame exhausts the memory of the card.
:::

## Borrow a temporary target

A drawing that needs a fresh target on every frame, such as a group of shapes drawn with one shared transparency, borrows it from the [[FrameBufferPool]] instead of keeping a framebuffer:

```java file="com/example/further/FramebuffersUI.java" region="pool"
```

<figure>

![Two overlapping light discs at half transparency, grey on the dark stage, with no lighter area where they overlap](/captures/further-framebuffers-pool.webp)

<figcaption>
Drawn into a borrowed target, then drawn at 50 %, the two discs share one transparency.
</figcaption>
</figure>

`FrameBufferPool.allocate(owner, depth, count, width, height)` returns `count` targets of at least `width` × `height` pixels.
The pool keeps one set per owner, depth and count, and grows it by powers of two, so the next frame gets the same targets back.
Pass a depth of your own when a drawing nests inside another drawing of the same owner, so that both do not share a target.
JOID deletes a set that no frame used for 5 seconds, so you never delete a borrowed target.

Inside, the drawing sets the target and the viewport in the render state, and a projection that maps your coordinates onto the pixels of the target.
The target is larger than what you drew: `u` and `v` keep the used part when you draw its texture with `drawTexture`.
The texture holds premultiplied colors, so it draws with `PREMULTIPLIED` of [[BlendState]], and the color of the render state scales the whole group.

To post-process what you draw, such as a blur or rounded corners, use a shader pass instead: [Shaders](/use/learn/going-further/shaders/) shows how.

## Build your own geometry

The [[Tessellator]] collects vertices and sends them in one draw call; every `DrawUtils` call uses it.
Here, a triangle with one color per vertex:

```java file="com/example/further/FramebuffersUI.java" region="triangle"
```

<figure>

![A triangle whose corners are light grey, blue and dark grey, the colors blending across it](/captures/further-framebuffers-triangle.webp)

<figcaption>
The colors of the three vertices blend across the triangle.
</figcaption>
</figure>

1. `Tessellator.inst()` returns the shared tessellator, and `start(mode)` begins a shape in a [[DrawMode]]: `TRIANGLES`, `QUADS`, `POLYGON` (a convex fan), `LINES`, `LINE_STRIP` or `LINE_LOOP`.
2. `setColor(...)`, `setTextureUV(u, v)` and `setNormal(x, y, z)` apply to the vertices added after them.
3. `addVertex(x, y, z)` and `addVertexWithUV(x, y, z, u, v)` add the vertices, in your drawing coordinates.
4. `draw()` sends them.

A shape without `setColor` takes the color of the render state.
The tessellator does not touch the render state: set the blending, the texture and the shader yourself, inside `withState(...)` ([Drawing resources and transforms](/use/learn/going-further/drawing-resources-and-transforms/#change-the-render-state)).
`start` while a shape is open, or `draw` without `start`, throws `IllegalStateException`.

To fill a texture with pixels of your own, create it with `createTexture()` of the render bridge, then call `allocate(width, height)` and `upload(pixels)` of the [[ITexture]], one ARGB `int` per pixel, row by row from the top.
Delete it with `delete()`, like a framebuffer.

## Use the texture of a resource

To texture your geometry with an image, draw it inside `resource.bind(wrap, drawing)` of a [[Resource]].
It starts the decoding when needed, uploads the decoded pixels once they are ready, binds the texture with the filter of the resource and the given [[TextureWrap]], and unbinds it after the drawing, even when the drawing throws.

`getTexture()` gives the [[ITexture]] itself, for a sampler of a shader or a bitmap font, and `null` while there is none.
To get it without `bind` or a node that draws the resource, call [[Resource#prepareBind]] on the render thread at each use: it creates the texture, then uploads the image once it is loaded.
`isUploaded()` tells that the texture holds the image; a texture that is not `null` may still be empty while the resource loads in the background.

## Draw a 3D model

`DrawUtils.MODEL`, a [[DrawModel]], draws an [[IDrawableModel]], such as an [[ObjModel]] loaded as [3D models](/use/learn/going-further/3d-models/) shows, or a model of your own:

```java file="com/example/further/PyramidModel.java" region="model"
```

```java file="com/example/further/FramebuffersUI.java" region="model"
```

```java file="com/example/further/FramebuffersUI.java" region="draw-model"
```

<figure>

![A light grey pyramid turned to show two of its faces, one lit in white and the other in a slightly darker grey, the light coming from the viewer](/captures/further-framebuffers-model.webp)

<figcaption>
The normals give each face its own light, so four triangles read as a solid pyramid.
</figcaption>
</figure>

`drawModel(x, y, size, model)` puts the origin of the model on `(x, y)` and draws one model unit as `size` design pixels; `drawModel(x, y, sizeX, sizeY, sizeZ, model)` scales each axis on its own.
Models follow the convention of modeling tools: +X goes right, +Y goes up and +Z comes toward you.
The light comes from the viewer, and normals decide how much light each face gets.
Each call clears the depth buffer before and after the model, so what you draw next covers it.

`render()` sends the geometry around the origin, and `getWidth()`, `getHeight()`, `getDepth()` give its size in model units.
To show a model as a node that the user turns, use a [[ModelNode]] ([3D models](/use/learn/going-further/3d-models/)).

## Recap

- A **`FrameBuffer`** holds a drawing that you **`fill`** once and **`draw`** as often as you need.
- Clear the buffer yourself in `fill`, and **`delete()`** it when its node detaches.
- The **`FrameBufferPool`** lends temporary targets for one frame and deletes them when no frame uses them.
- The **`Tessellator`** sends your own vertices: `start`, set color, texture coordinates or normals, add vertices, `draw`.
- The tessellator uses the **render state** as it is: set it inside `withState(...)`.
- **`resource.bind(...)`** textures your geometry; **`prepareBind()`** readies the texture of a resource by hand.
- **`DrawUtils.MODEL`** draws an **`IDrawableModel`**, such as an `ObjModel`, with its origin at a point.

## Try it

**A grey square.**
Draw a grey square of 100 design pixels with `DrawMode.QUADS`.

<details>
<summary>Solution</summary>

After `tessellator.start(DrawMode.QUADS)`, one quad takes four vertices, in order around the square:

```java fragment
tessellator.addVertex(0D, 100D, 0D);
tessellator.addVertex(100D, 100D, 0D);
tessellator.addVertex(100D, 0D, 0D);
tessellator.addVertex(0D, 0D, 0D);
```

Then `setColor` before the vertices, and `draw()`.

</details>

**Refresh the stamp.**
Make `StampNode` draw a new picture when its size changes.

<details>
<summary>Solution</summary>

Delete the buffer and set the field to `null` when the size differs from the one you filled it for; `draw` creates and fills a new one on the next frame.
Filling the same buffer again also works when its size stays right: clear it first.

</details>

**A taller pyramid.**
Draw the pyramid twice as tall as it is wide.

<details>
<summary>Solution</summary>

Scale each axis on its own:

```java fragment
DrawUtils.MODEL.drawModel(100D, 120D, 80D, 160D, 80D, this.pyramid);
```

</details>
