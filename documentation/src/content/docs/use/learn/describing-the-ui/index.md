---
title: "Describing the UI"
description: "How a JOID interface is described: UIs, nodes, the canvas, positions, layout, text and media."
genre: learn
sidebar:
  order: 0
---

Before an interface reacts or animates, it has to exist: screens, the shapes and texts on them, their places and their sizes.
This chapter shows how you describe all of it in Java, as a tree of nodes inside a UI, on a canvas that JOID fits to any window.
The [Catalog](/use/catalog/) shows every node with a capture, when you look for one.

## UIs

A UI is a class that builds its nodes in `init()`; you open it, and JOID draws it until you close it.

```java file="com/example/describing/uis/Navigation.java" region="open"
```

Read [UIs](/use/learn/describing-the-ui/uis/) to learn the life of a UI, how to keep it open and how to configure it.

## Nodes and the tree

Everything a UI shows is a node, created by a factory, configured by setters and attached to the UI or to another node.

```java file="com/example/describing/nodes/ToolbarUI.java" region="opener"
```

Read [Nodes and the tree](/use/learn/describing-the-ui/nodes/) to learn how to nest nodes and how positions follow the tree.

## Node lifecycle

A node joins a UI, lives there for many frames and leaves; callbacks run your code at each of these moments.

```java fragment
.onInit(event -> this.frames = 0L)
.onUpdate(event -> this.frames++)
```

Read [Node lifecycle](/use/learn/describing-the-ui/node-lifecycle/) to learn when each callback runs and what to release when a node leaves.

## The design canvas

You place every node on a 1920 × 1080 canvas of design pixels, and JOID fits it to the window.

```java file="com/example/describing/canvas/EdgesUI.java" region="view"
```

Read [The design canvas](/use/learn/describing-the-ui/design-canvas/) to learn how the canvas fits windows of any shape, how to reach their edges and how to zoom.

## Position and size

A node is placed and sized from its parent; anchors, `zindex`, visibility and copies refine it.

```java file="com/example/describing/position/OrderUI.java" region="order"
```

Read [Position and size](/use/learn/describing-the-ui/position-and-size/) to learn how to size from a parent, keep a point fixed and choose what is on top.

## Overlays and popups

A popup opens above the current screen and keeps the input until it closes; an overlay stays open while screens open and close.

```java fragment
@UIDataPopup(active = true)
public class ConfirmPopup extends UI {
```

Read [Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/) to learn how UIs stack, how a popup dims the screen and how an overlay takes input.

## Layout

A node never moves its children; a layout node does, and any node can clip or scroll what goes beyond it.

```java file="com/example/describing/layout/OverflowUI.java" region="hidden"
```

Read [Layout](/use/learn/describing-the-ui/layout/) to learn the layout rule, groups, clipping and scrolling.

## Flex layout

A flex node lines its children up in a column or a row, and grows to fit them.

```java file="com/example/describing/flex/FlexCasesUI.java" region="column"
```

Read [Flex layout](/use/learn/describing-the-ui/flex-layout/) to learn its rule, case by case.

## Grid layout

A grid node fills rows of cells and wraps at its width.

```java file="com/example/describing/grid/GridCasesUI.java" region="wrap"
```

Read [Grid layout](/use/learn/describing-the-ui/grid-layout/) to learn its rule, case by case.

## Text

A text node draws a text in a style that names its font, size and color.

```java file="com/example/describing/text/GreetingUI.java" region="opener"
```

Read [Text](/use/learn/describing-the-ui/text/) to learn styles, alignment, long texts and measures.

## Fonts

Your application loads its own fonts, once.

```java file="com/example/describing/fonts/AppFonts.java" region="class"
```

Read [Fonts](/use/learn/describing-the-ui/fonts/) to learn how weights are picked and which kind of font to choose.

## Text markup and effects

Markup styles parts of a string, and effects change its glyphs as they are drawn.

```java file="com/example/describing/markup/MarkupUI.java" region="markup"
```

Read [Text markup and effects](/use/learn/describing-the-ui/text-markup-and-effects/) to learn how to write a markup, an effect, a modifier and a converter.

## Images and media

A resource is an image, an animation or a video that a node draws or plays.

```java file="com/example/describing/media/GalleryUI.java" region="image"
```

Read [Images and media](/use/learn/describing-the-ui/media/) to learn how to fit, tint and crop images, play videos, and which formats JOID reads.
