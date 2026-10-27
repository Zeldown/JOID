---
title: "Transitions"
description: "Animate a UI when it opens and closes: the popup transition, a transition on any UI, and transitions of your own."
genre: learn
sidebar:
  order: 4
---

A transition animates a whole UI when it opens and when it closes.
Popups play one by default; any other UI can play one too, and you can write your own.

**You will learn**
- What a popup plays when it opens and closes, and how to change it
- How to give any UI a transition
- How to write a transition of your own
- What happens to the UI while a transition plays

## Choose what a popup plays

A popup, a UI whose [[UIDataPopup]] sets `active = true`, plays a [[PopTransition]]: it grows from 75 % to its full size around its anchor in 130 ms when it opens, and shrinks back in 130 ms when it closes.

<figure>

![Over a light grey screen, a white popup grows into view while a dark backdrop fades in, stays, then shrinks away while the backdrop fades out](/captures/style-pop.webp)

<figcaption>The pop transition, in and out; the backdrop of the popup fades with it.</figcaption>
</figure>

The `transition` element of `@UIDataPopup` chooses which half plays:

```java fragment
@UIDataPopup(active = true, transition = PopupTransition.IN)
```

Of the four values of [[UIDataPopup.PopupTransition]], `IN_OUT` is the default, `IN` animates the opening only, `OUT` the closing only, and `NONE` shows and hides the popup at once.
[Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/) explains popups themselves.

## Give any UI a transition

[[UI#transition]] gives a transition to any UI: a screen, an overlay or a popup.
When the UI always enters the same way, set it in its constructor:

```java file="com/example/style/SlidePopupUI.java" region="set"
```

When the code that opens the UI decides how it enters, set the transition before [[JOID#open]]:

```java file="com/example/style/MenuUI.java" region="open"
```

A transition set this way replaces the one of `@UIDataPopup`, and `transition(null)` removes every transition, the popup one included.
The opening transition starts once `init()` has run, so a transition set in `init()` plays as well.

A transition is a configuration that several UIs can share, such as a constant.
`transition(...)` copies its two steps into the UI, and each UI plays its own copies: a UI that closes never stops another one.
The settings and the listeners are copied at that moment, so a change made later to the shared transition reaches no UI that already has it.
The state of a playing transition is on the UI: [[UI#getTransitionIn]] and [[UI#getTransitionOut]] return the steps it plays, with `isRunning()` and `getAnimator()`.

## Write a transition of your own

A transition is a class that extends [[Transition]] and passes two [[TransitionStep]] values to its constructor: one for the opening, one for the closing.
This one slides the UI up from below its place with a small overshoot, and drops it down when it closes.
`getModelView()` of the render bridge is the matrix stack that every drawing goes through: `with(...)` saves it, runs the lambda, which translates it and draws `body`, then restores it.


```java file="com/example/style/SlideTransition.java"
```

<figure>

![Over a screen darkened by the popup backdrop, a white card slides up from below with a small bounce, stays, then drops down and disappears while the backdrop fades out](/captures/style-transition.webp)

<figcaption>`SlideTransition`: 500 ms in with `BACK_OUT`, 300 ms out with `QUART_IN`.</figcaption>
</figure>

`TransitionStep.create(duration, easing)` gives the duration in milliseconds and the [easing](/use/learn/style-and-motion/animation/#shape-the-motion-with-easing) of the step.
`draw((ui, progress, body) -> ...)` receives the progress of the step, from `0F` to `1F` with its easing applied, and `body`, which draws the UI: wrap `body.run()` in the transformation of this progress.
Both steps count their progress from `0F` at their start, so the opening places the UI at `1F - progress` away, and the closing at `progress` away.

The translations of the matrix stack ([Drawing resources and transforms](/use/learn/going-further/drawing-resources-and-transforms/)) are in design pixels, like the positions of the nodes: `300D` moves the UI by 300 design pixels whatever the size of the window.
`enabled(false)` on a step turns it off, and an override of [[TransitionStep#init]] prepares what depends on the UI, such as its size, each time the UI loads.

## Know what happens while a transition plays

- The [backdrop](/use/learn/describing-the-ui/uis/) of a UI whose [[UIData]] sets `background = true` fades in and out with the transition.
- On `JOID.close(ui)` or Escape, the UI stays open while its closing transition plays, and JOID removes it when the transition ends; a second close in that time does nothing.
- A close during the opening transition plays the closing one from the point that the opening reached, so the UI turns back without a jump.
- `JOID.open(ui)` during its closing transition stops the transition and keeps the UI open.
- Opening a screen over another screen plays the closing transition of the open one first; the screen you open appears when it ends.
  When several screens are opened during that time, the last one wins.
- [[JOID#forceClose]] closes at once, without the closing transition, and so does a close of a UI that is hidden or not drawn.

[Open and close a UI](/use/learn/describing-the-ui/uis/#open-and-close-a-ui) says when the opening transition plays.

## Recap

- A **popup** plays a `PopTransition` by default; `@UIDataPopup(transition = ...)` keeps the opening, the closing, both or none.
- **`transition(...)`** gives a transition to any UI: in its constructor when the UI decides, before `JOID.open` when the caller decides.
- A UI plays **copies** of the steps of its transition, so several UIs can share one transition.
- A transition of your own extends **`Transition`** with two **`TransitionStep`** values: the opening, then the closing.
- A step **draws** the UI through `body`, wrapped in the transformation of its progress, from `0F` to `1F` with its easing.
- The UI closes when its closing transition ends; **`JOID.forceClose`** skips it.

## Try it

**Slide from the side.**
Change `SlideTransition` so that the UI enters from the right edge of the design canvas.

<details>
<summary>Solution</summary>

Translate on the x axis by the width of the canvas instead of 300 design pixels on y:

```java fragment
matrix.translate(1920D * away, 0D, 0D);
```

</details>

**A popup that only opens softly.**
Keep the pop when the popup opens, and close it at once.

<details>
<summary>Solution</summary>

```java fragment
@UIDataPopup(active = true, transition = PopupTransition.IN)
```

</details>
