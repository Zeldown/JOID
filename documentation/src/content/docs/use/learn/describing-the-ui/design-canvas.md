---
title: "The design canvas"
description: "Place nodes on a fixed 1920 × 1080 canvas that JOID fits to any window, reach the edges of the window, pin a UI to a corner, follow the interface scale and zoom."
genre: learn
sidebar:
  order: 4
---

A window can be small or large, wide or tall, and you do not want to compute every position again for each of them.
JOID gives every UI a fixed canvas of 1920 × 1080 design pixels and fits it to the window.
This page shows how the canvas fits, and how to use the space that the window shows around it.

**You will learn**
- How the canvas fits a window of any shape
- How to reach the edges of the window
- How to pin a UI to a side or a corner
- How the interface scale changes the size of a UI
- How to zoom a UI

## Place nodes on the canvas

Every position and size in JOID is in design pixels, on a canvas 1920 wide and 1080 high.
JOID scales the whole canvas to the window, by the same factor on both axes, so that the canvas fits entirely:

<figure>

![Two windows drawn in dark grey. On the left, a wide window holds a white design canvas of 1920 by 1080 in its middle, with dark bands on its left and right. On the right, a tall window holds the white canvas in its middle, with dark bands above and below. The labels read: wider window, more on the sides; taller window, more above and below.](/captures/describing-canvas-diagram.webp)

<figcaption>
The canvas always fits: a window of another shape shows more space on two of its sides.
</figcaption>
</figure>

A node at (960, 540) is in the middle of the canvas, whatever the size of the window.
On a 16:9 window, the canvas fills it exactly.
On a wider window, the window shows more space on the left and on the right of the canvas; on a taller one, above and below.
That space is still part of the UI: nodes can be drawn there, at negative positions or beyond 1920 and 1080.

The size of the window is in screen pixels: [[UI#getWindowWidth]], [[UI#getWindowHeight]].
The position of the mouse is in design pixels, like the nodes: [[UI#getMouseX]] and [[UI#getMouseY]].
[[UI#getView]] converts between the two units: `toScreenX(x)` gives the screen pixel of a design position, `toUiX(x)` the design position of a screen pixel, and `toScreenY`, `toUiY`, `toScreenWidth` and `toScreenHeight` do the same for the other axis and the sizes.

## Reach the edges of the window

The view is the part of the canvas plane that the window shows: [[UI#getViewX]], [[UI#getViewY]], [[UI#getViewWidth]] and [[UI#getViewHeight]], in design pixels.
A node that follows the view covers the whole window:

```java file="com/example/describing/canvas/EdgesUI.java" region="view"
```

On a wide window, `getViewX()` is negative and `getViewWidth()` is more than 1920.
The nodes placed on the canvas stay where they are, and the nodes that follow the view reach the edges:

```java file="com/example/describing/canvas/EdgesUI.java" region="edges"
```

<figure>

![A wide window: a light grey area fills it, a white canvas sits in its middle with a grey card in the center, a dark grey bar runs along the whole top edge of the window, and a grey square sits in its top-right corner.](/captures/describing-canvas-wide.webp)

<figcaption>
On a wide window, the bar and the square follow the view, while the card stays in the middle of the canvas.
</figcaption>
</figure>

<figure>

![A tall window: the white canvas is in its middle with the grey card, light grey areas above and below it, the dark grey bar along the top edge of the window, above the canvas, and the grey square in the top-right corner.](/captures/describing-canvas-tall.webp)

<figcaption>
The same UI on a tall window: the bar and the square still reach the top edge.
</figcaption>
</figure>

The lambdas read the view again on every frame, so the nodes follow a resize.
[Signals](/use/learn/interactivity/signals/) explains this form of setter.

## Pin a UI to a side or a corner

By default, the canvas sits in the middle of the window.
`anchorX` and `anchorY` of [[UIData]] pin it to a side or a corner instead:

```java file="com/example/describing/canvas/MinimapUI.java" region="class"
```

<figure>

![A wide window filled with light grey, with a white square holding a grey disc in its top-right corner.](/captures/describing-canvas-pinned.webp)

<figcaption>
Pinned to the top-right corner, the canvas keeps the minimap against the right edge of a wide window.
</figcaption>
</figure>

With [[Align]], `START` pins the canvas to the left or the top, `CENTER` keeps it in the middle, and `END` pins it to the right or the bottom.
The anchor is also the point that stays in place when the UI is scaled or zoomed.

## Follow the interface scale

The interface scale is a factor that the UI bridge applies on top of the fit, to make every UI larger or smaller, such as a scale option of the application that hosts JOID.
The default window bridge gives 1; a UI bridge of your own gives another value ([Embedding JOID](/use/learn/running/embedding/)).

[[UIDataScale]] tells how a UI follows it:

```java fragment
@UIDataScale(limited = true, limit = 1.5D)
public class HudUI extends UI {
```

- By default, the UI follows the interface scale.
- `limited = true` follows it up to `limit`: here, a scale of 2 draws the UI at 1.5.
- `active = false` ignores it: the UI keeps the size of the fit.

## Zoom a UI

The zoom makes a UI smaller around its anchor, on top of the fit and the interface scale:

```java file="com/example/describing/canvas/ZoomUI.java" region="zoom"
```

<figure>

![A window whose light grey UI is drawn smaller than the window, framed by the dark window around it, with a white card in its middle that reads 80 % above two grey buttons marked minus and plus; the pointer clicks plus and the UI grows to 90 %, then clicks minus and the UI shrinks back to 80 %.](/captures/describing-canvas-zoom.webp)

<figcaption>
At a zoom of `0.8D`, the whole UI is drawn at 80 % of its size, around the center of the window.
</figcaption>
</figure>

[[UI#getZoom]] reads the zoom shown; `1D` is the size of the fit.
The zoom shown stays between `0.1D` and `1D`, and goes up to the size of the fit when the interface scale is below `1D`.

- [[UI#zoom(double)]] sets the zoom, like any setter; [[UI#zoom(Signal)]] ties it to a signal, both ways.
  A value beyond the bounds stays asked for: `zoom(1.5D)` shows `1D` until the interface scale allows more.
- [[UI#zoomTo]] and [[UI#zoomBy]] change it the way the user does, within the bounds.
- In [dev mode](/use/get-started/dev-tools/), keys and the mouse wheel zoom the UI unless `@UIData(zoomable = false)`; an application that lets the user zoom gives its own controls, which call `zoomTo` or `zoomBy`.

A change by the user, `zoomTo` or `zoomBy` asks the [[UI#beforeZoom]] listeners first, then tells the [[UI#onZoom]] listeners:

```java file="com/example/describing/canvas/ZoomUI.java" region="limit"
```

Here the zoom never goes below `0.5D`: the listener cancels any change below it.
The listeners run only for a zoom that changes what is shown: a request that the bounds clamp to the current zoom runs none of them.

## Recap

- Every position and size is in **design pixels** on a **1920 × 1080 canvas** that JOID fits to the window.
- A window of another shape shows **more space** on two sides; nodes can be drawn there.
- The **view** (`getViewX`, `getViewY`, `getViewWidth`, `getViewHeight`) is what the window shows; follow it to reach the edges.
- The mouse position is in design pixels too; the window size is in **screen pixels**.
- **`anchorX`** and **`anchorY`** pin the canvas to a side or a corner, and set the point that stays in place when the UI scales.
- **`@UIDataScale`** chooses how a UI follows the **interface scale** of its UI bridge.
- The **zoom** shrinks a UI around its anchor; **`beforeZoom`** can refuse a change.

## Try it

**Center on the view.**
Place a 200 × 200 square in the middle of the window, not of the canvas.

<details>
<summary>Solution</summary>

The middle of the window is the middle of the view:

```java fragment
RectNode
.create(0, 0, 200, 200)
.x(() -> super.getViewX() + super.getViewWidth() / 2D - 100D)
.y(() -> super.getViewY() + super.getViewHeight() / 2D - 100D)
.attach(this);
```

With the default anchor, the middle of the view is also the middle of the canvas, (960, 540); with `anchorX = Align.END` on a wide window, it is not.

</details>

**Reset the zoom.**
Add a button to `ZoomUI` that brings the zoom back to 1.

<details>
<summary>Solution</summary>

`zoomTo` changes the zoom the way the user does, so the `beforeZoom` listener receives it:

```java fragment
.onClick(event -> super.zoomTo(1D))
```

</details>
