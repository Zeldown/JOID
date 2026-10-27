---
title: "Going further"
description: "Extend JOID with your own nodes, drawing, shaders, effects, fonts and formats, and use charts and 3D models."
genre: learn
sidebar:
  order: 0
---

The built-in nodes, effects, fonts and formats cover most interfaces, but sooner or later a design asks for something they do not draw.
This chapter opens every layer below the nodes: you write your own nodes and effects, draw by hand, run your own shaders, teach JOID other fonts and file types, and give the controls your own look.
It also covers the two families of nodes that need more than a line to use: charts and 3D models.

## Custom nodes

A custom node extends [[Node]] and draws itself; it attaches, nests and reacts like any built-in node.

```java file="com/example/further/CustomNodesUI.java" region="dots"
```

Read [Custom nodes](/use/learn/going-further/custom-nodes/) to learn how to write a node, give it properties that follow signals, hold a value that the user changes and fire events of your own.

## Drawing shapes and text

[[DrawUtils]] draws shapes, lines and text on every frame, from a layer, a custom node or an effect.

```java file="com/example/further/DrawingUI.java" region="layer"
```

Read [Drawing shapes and text](/use/learn/going-further/drawing/) to learn where to draw and how to draw shapes, lines, curves and text.

## Drawing resources and transforms

`DrawUtils.RESOURCE` draws images, and a [[Transformation]] rotates or scales everything drawn inside it.

```java file="com/example/further/TransformsUI.java" region="pivot"
```

Read [Drawing resources and transforms](/use/learn/going-further/drawing-resources-and-transforms/) to learn how to draw images, rotate and scale a drawing, and change the render state and the blending.

## Framebuffers and models

A [[FrameBuffer]] keeps a drawing made once, to draw it again on every frame, at any size.

```java file="com/example/further/StampNode.java" region="fill"
```

Read [Framebuffers and models](/use/learn/going-further/framebuffers-and-models/) to learn how to draw once and reuse the result, borrow a temporary target, build your own geometry and draw 3D models by hand.

## Shaders

A shader colors each pixel with a formula, written once in JOID GLSL for every backend, and shader passes post-process a drawing.

```java file="com/example/further/ShadersUI.java" region="pipeline"
```

Read [Shaders](/use/learn/going-further/shaders/) to learn how to write a shader, bind it around a drawing, set its uniforms, and write a pass of your own.

## Custom effects

A custom effect is a treatment that goes on any node, like the built-in effects.

```java file="com/example/further/EffectsUI.java" region="shadow"
```

Read [Custom effects](/use/learn/going-further/custom-effects/) to learn how to draw around a node, change how it draws, process its pixels, and order it among the other effects.

## Custom fonts and MSDF

A font of your own can draw pixel-art letters from an atlas, at the exact size you ask for.

```java file="com/example/further/FontsUI.java" region="use"
```

Read [Custom fonts and MSDF](/use/learn/going-further/fonts/) to learn how to write a bitmap or glyph font, and how to generate MSDF atlases ahead of time.

## Custom formats

A locator, a format and a decoder of yours make any source and any file type load like a PNG.

```java file="com/example/further/FormatsUI.java" region="register"
```

Read [Custom formats](/use/learn/going-further/custom-formats/) to learn how to read resources from your own sources and file types, and build animations from your own frames.

## Give your controls a look

The controls of JOID hold their behavior, and a small subclass, a skin, gives them their parts.

```java file="com/example/further/KitUI.java" region="use"
```

Read [Give your controls a look](/use/learn/going-further/ui-kit/) to learn how to skin each control and gather the skins in a UI kit.

## Charts

A chart node holds axes and series of values; a skin of yours draws them.

```java file="com/example/further/ChartsUI.java" region="line"
```

Read [Charts](/use/learn/going-further/charts/) to learn how to feed a chart, update its data and draw it.

## 3D models

A model node shows a `.obj` model in its bounds, turned by a yaw and a pitch.

```java file="com/example/further/ModelsUI.java" region="node"
```

Read [3D models](/use/learn/going-further/3d-models/) to learn how to load a model, turn it, and let the user rotate and zoom it.
