---
title: "3D models"
description: "Load a Wavefront .obj model, show it in a node turned by a yaw and a pitch, and let the user rotate and zoom it."
genre: learn
sidebar:
  order: 11
---

A model node shows a 3D model inside its bounds, lit and textured, among the other nodes of a UI.
JOID reads models from Wavefront `.obj` files: a format that every 3D tool exports.

**You will learn**
- How to load a `.obj` model and its texture
- How to show a model and turn it
- How to let the user rotate and zoom a model
- What JOID reads in a `.obj` file

## Load a model

[[ObjModel#of]] reads a model file and takes the texture that paints it:

```java file="com/example/further/ModelsUI.java" region="load"
```

The first argument names the model in error messages.
The file is any input that [[Resource#of]] takes: a path in the classpath, a file, a stream or a URL ([Custom formats](/use/learn/going-further/custom-formats/) explains how JOID finds it).
`of` reads the whole file at once, on the calling thread, and throws when a line is wrong: load a model once, here in a constant, and share it between nodes.
The texture is a [[Resource]], loaded like any image ([Images and media](/use/learn/describing-the-ui/media/)), and repeats when the texture coordinates go past its edges.

## Show and turn a model

A [[ModelNode]] draws its model centered in its bounds:

```java file="com/example/further/ModelsUI.java" region="node"
```

```java file="com/example/further/ModelsUI.java" region="turn"
```

<figure>

![Two grey and white textured cubes seen at an angle, with a blue Turn button under the first one and a Yaw text under the second; the pointer drags the second cube right and back, which turns under the mouse and back while the Yaw text follows, then clicks Turn eight times, and the first cube turns at each click until it has made a full turn](/captures/further-3d-models-cubes.webp)

<figcaption>
Both nodes share one model.
The first one turns by 45 degrees on each click of the button; the second one turns under the mouse.
</figcaption>
</figure>

| Property | Effect |
|---|---|
| `zoom` | The size of the model: at `1D`, the width of the model fills the width of the node. |
| `rotationYaw` | The turn around the vertical axis, in degrees. |
| `rotationPitch` | The tilt toward or away from you, in degrees. |
| `model` | The model to draw, required: a node without one throws when it is attached. |

Each property takes a value or a `Supplier`, so a model can turn with a signal, as the yaw does here.
JOID draws the model with lighting and its own depth test, then clears the depth, so the model never cuts into the nodes drawn after it.

Both nodes extend [[BaseModelNode]], which holds the model: a [custom node](/use/learn/going-further/custom-nodes/) that extends it turns the model by a rule of its own and calls `drawModel(zoom, rotationYaw, rotationPitch)` from its `draw`.
[Draw a 3D model](/use/learn/going-further/framebuffers-and-models/#draw-a-3d-model) draws a model without a node.

## Let the user rotate and zoom

A [[ModelViewerNode]] turns its model while the user drags it with the left button, and zooms it with the mouse wheel:

```java file="com/example/further/ModelsUI.java" region="viewer"
```

- A drag of 5 design pixels turns the model by 1 degree; a notch of the wheel zooms by `0.04D`.
- The drawn model eases toward the requested rotation and zoom, so a drag stays smooth.
- `zoomRange(min, max)` bounds the zoom, `0.1D` to `2D` by default; `rotationYawRange` and `rotationPitchRange` bound the rotation, which is free by default.
  A range of `0D, 0D` locks an axis.

The rotation and the zoom are values that both the user and your code change, like the value of a control ([Controls](/use/learn/interactivity/controls/) teaches the pattern):

- `zoom(...)`, `rotationYaw(...)` and `rotationPitch(...)` set a start value, or bind a signal that follows each change of the user.
- `zoomTo(zoom)` and `rotate(yaw, pitch)` change them as the user does.
- `beforeZoom` and `beforeRotate` can refuse a change, and `onZoom` and `onRotate` hear it.
- `getZoom()`, `getRotationYaw()` and `getRotationPitch()` read them:

```java file="com/example/further/ModelsUI.java" region="read"
```

## Know what JOID reads in a `.obj` file

| Line | JOID reads |
|---|---|
| `v x y z` | A vertex position. The Y axis points up. |
| `vt u v` | A texture coordinate, with `v` from the bottom of the texture, as every 3D tool writes it. |
| `vn x y z` | A vertex normal, for smooth lighting; without normals, each face is lit flat. |
| `f v1 v2 v3 ...` | A face of 3 vertices or more, as `v`, `v/vt`, `v//vn` or `v/vt/vn`, split into triangles. Indices start at 1, and negative indices count from the last line read. |
| `o name`, `g name` | Starts a group. All faces of a group share one format. |

JOID skips every other line, such as materials (`mtllib`, `usemtl`) and smoothing groups (`s`): one texture paints the whole model.
A malformed line throws with the name of the model and the number of the line.

To draw a model of your own making, implement [[IDrawableModel]] and draw it with the model drawer of [[DrawUtils]], `DrawUtils.MODEL` ([Framebuffers and models](/use/learn/going-further/framebuffers-and-models/) builds geometry by hand).

## Recap

- **`ObjModel.of(name, file, texture)`** reads a `.obj` file at once: load it once and share it.
- A **`ModelNode`** draws its model centered in its bounds, sized by **`zoom`** and turned by **`rotationYaw`** and **`rotationPitch`**.
- A **`ModelViewerNode`** lets the user **drag** to rotate and **scroll** to zoom, within ranges you set.
- Its rotation and zoom are **interactive values**: set them, bind them to signals, refuse or hear their changes.
- JOID reads **positions, texture coordinates, normals, faces and groups**; one texture paints the whole model.

## Try it

**Look from the side only.**
Let the user turn the viewer from left to right, but never tilt it.

<details>
<summary>Solution</summary>

```java fragment
.rotationPitchRange(20D, 20D)
```

The pitch stays at its start value of 20 degrees, and the yaw stays free.

</details>

**Back to the front.**
Add a button that turns the viewer back to a yaw of 0, as if the user did it.

<details>
<summary>Solution</summary>

```java fragment
.onClick(event -> viewer.rotate(0D, viewer.getRotationPitch()))
```

`rotate` goes through `beforeRotate` and `onRotate`, and the model eases back to the front.

</details>
