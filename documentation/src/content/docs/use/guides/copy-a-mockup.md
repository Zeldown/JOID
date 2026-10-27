---
title: "Copy a mockup"
description: "Rebuild a design mockup pixel for pixel, then lay the mockup over the UI to check it."
genre: guide
sidebar:
  order: 7
---

A mockup gives every position, size, color and text style of a screen.
This guide turns those values into nodes without conversion, then lays the mockup over the result to check every edge.

## What you will build

<figure>

![A white rounded card with a soft shadow: a light grey circle for the avatar, the name Ada Lovelace in bold, the role Analyst, and a rounded Follow button](/captures/guides-mockup-card.webp)

<figcaption>
A profile card copied from a 1920 × 1080 mockup.
</figcaption>
</figure>

## Before you start

- The design canvas and design pixels: [The design canvas](/use/learn/describing-the-ui/design-canvas/).
- Positions and sizes of nodes: [Position and size](/use/learn/describing-the-ui/position-and-size/).
- Text styles: [Text](/use/learn/describing-the-ui/text/).
- Rounded corners and shadows: [Effects](/use/learn/style-and-motion/effects/).
- The mockup, drawn on a frame of 1920 × 1080, and an export of that frame as a PNG of the same size.

## 1. Draw the mockup on a 1920 × 1080 frame

A UI places its nodes on a design canvas of 1920 × 1080 design pixels, which JOID scales to the window.
When the frame of the mockup has the same size, every value of the mockup is a value of JOID.

**Check:** the frame of the mockup measures 1920 × 1080.

## 2. Turn each part of the mockup into a node

Give the position and the size of each layer to `create(x, y, width, height)`.
The position of a child is relative to its parent: subtract the position of the parent layer.
A hexadecimal color goes through [[Color#decode]], a corner radius becomes a [[RoundedNodeEffect]], and a shadow becomes a [[ShadowNodeEffect]] with the same color, blur and offset:

```java file="com/example/guides/MockupGuideUI.java" region="card"
```

Here the card sits at 660, 290 on the frame, and the avatar at 900, 350: inside the card, the avatar is at 240, 60.

**Check:** the card has the size, the corners and the shadow of the mockup.

## 3. Copy the text styles

The font size of the mockup is the font size of the [[TextInfo]].
Letter spacing and line height are written on 1: a letter spacing of -2 % is `-0.02F`, a line height of 140 % is `1.4F`.

```java file="com/example/guides/MockupGuideUI.java" region="text"
```

Use the font of the mockup, in the same weights ([Replace a font](/use/guides/replace-a-font/)).

**Check:** the name and the role have the width and the height of the texts of the mockup.

## 4. Lay the mockup over the UI

Hold whether the mockup shows in a [[BooleanSignal]], `false` at first:

```java file="com/example/guides/MockupGuideUI.java" region="flag"
```

Draw the exported mockup over the whole canvas at half opacity, let the mouse through it, and toggle it with a [[Shortcut]] on `F2`:

```java file="com/example/guides/MockupGuideUI.java" region="overlay"
```

`color(Color.WHITE.withAlpha(0.5F))` tints the image at 50 % opacity, and `interactive(false)` lets clicks reach the nodes under it.

**Check:** press `F2`.
Where the copy matches, the edges of the UI and of the mockup overlap exactly; a node that is off shows a double edge.
Resize the window: the mockup and the UI scale together, because both live on the design canvas.

<figure>

![The same card; the exported mockup appears over it at half opacity, then disappears: the card, the circle and the button of the mockup land exactly on those of the UI, and grey bars mark the place of the name and of the role](/captures/guides-mockup-overlay.webp)

<figcaption>
`F2` shows and hides the mockup over the UI: a shape that keeps a single edge is copied pixel for pixel.
The mockup marks each text with a bar, so the text only has to sit on its bar.
</figcaption>
</figure>

With the dev jar, the inspector shows the position and the size of the node under the mouse ([Dev tools](/use/get-started/dev-tools/)).
[Pixel alignment](/use/in-depth/pixel-alignment/) explains where each edge lands on the screen.

## If it does not work

1. Everything is shifted or scaled: the frame of the mockup is not 1920 × 1080; resize the frame, or scale every value by the same factor.
2. A child is offset by the position of its parent: its position was copied from the frame; subtract the position of the parent layer.
3. A text is wider or narrower than in the mockup: the font or the weight differs, or the letter spacing was written on 100 (`-2F` instead of `-0.02F`).
4. A magenta and black checkerboard covers the canvas, or nothing shows with the prod jar: the path of the PNG is wrong, and the dev jar prints the cause.
5. Clicks stop working while the mockup shows: the image misses `interactive(false)`.

## Reference

- [[RectNode]]
- [[TextInfo]]
- [[RoundedNodeEffect]]
- [[ShadowNodeEffect]]
- [[ResourceNode]]
- [[Shortcut]]
