---
title: "Best practices"
description: "What to do, what to avoid, and what to prefer when you write a JOID interface."
genre: guide
sidebar:
  order: 2
---

These rules come from the way JOID works.
Each one says how strongly it applies, why, and which page explains the notion behind it.

- **Do**: always, unless you have a reason you can state.
- **Don't**: never; the code compiles, but misbehaves.
- **Prefer**: the better of two ways that both work.
- **Avoid**: a way that works, at a cost you rarely want to pay.

## Describing the screen

**Do** create the nodes of a UI in `init()`, never in its constructor.
JOID runs `init()` when the UI opens and on every reload, and clears the nodes before it does.
[UIs](/use/learn/describing-the-ui/uis/)

**Do** copy positions and sizes from a 1920 × 1080 mockup.
Every value is in design pixels, and JOID scales the canvas to the window: a layout computed from the size of the window fights that scaling.
[The design canvas](/use/learn/describing-the-ui/design-canvas/)

**Prefer** a flex or grid layout to positions computed by hand for anything that repeats: buttons, rows, cards.
Inserting or hiding an item then moves the others by itself.
[Layout](/use/learn/describing-the-ui/layout/)

**Do** load each font once, in a constant shared by your UIs, and give every text a [[TextInfo]] of that font.
JOID ships no font for your application, and each load reads the files again.
[Fonts](/use/learn/describing-the-ui/fonts/)

**Prefer** locked text styles and colors for the values that your UIs share, and their `with...` copies for the variants.
A setter on a shared value changes every text that uses it.
[Text](/use/learn/describing-the-ui/text/)

## Holding the state

**Do** hold every value that changes on screen in a signal, and change the value, never the nodes.
[Signals](/use/learn/interactivity/signals/)

**Prefer** an expression that reads a signal, or the signal itself, to a lambda: JOID computes the expression only when the signal changes, and runs a lambda on every frame.
Keep lambdas for values that change without a signal, such as the progress of a video or the mouse.
[Signals](/use/learn/interactivity/signals/)

**Avoid** mixing a local variable and a signal in a calculation or a condition.
JOID cannot follow it, keeps the first value, and the dev jar prints a `[JOID] … but cannot follow it` warning; read a field, or derive a value with `map(...)`.
[What JOID follows](/use/learn/interactivity/signals/#what-joid-follows)

**Don't** change the collection that `get()` of a [[ListSignal]], [[MapSignal]] or [[SetSignal]] returns.
Nothing tells JOID about the change; call the methods of the signal (`add`, `remove`, `put`…).
[Signals](/use/learn/interactivity/signals/)

**Do** bind a control to a signal when its value matters outside the control, such as a setting in a store.
A control given a plain value keeps the value to itself.
[Controls](/use/learn/interactivity/controls/)

**Prefer** rebuilding the children only when their number or their kind depends on a value, such as the rows of a list.
For one text or one color, an expression that reads the signal is enough.
[Rebuilding](/use/learn/state/rebuilding/)

**Prefer** a store for a value that several UIs share, and a [[UIProperty]] field for a value that belongs to one UI.
[Stores and properties](/use/learn/state/stores-and-properties/)

**Do** close the open UIs before your program ends: closing a UI saves its stores and properties.
[Close the UIs before the program ends](/use/learn/running/backends/#close-the-uis-before-the-program-ends)

## Threads

**Don't** create, change or attach nodes from another thread than the render thread.
JOID is not thread-safe: send the work to the render thread, or write a signal, whose write JOID applies on the render thread.
[Threads](/use/learn/running/threads/)

## Style and motion

**Prefer** an effect to drawing a shape yourself for rounded corners, borders, shadows and blur.
Effects follow the node, its size and its children.
[Effects](/use/learn/style-and-motion/effects/)

**Avoid** a custom node or a shader for what a node and its effects already draw.
They are the right tools for a look that no node gives, and the most code to maintain.
[Custom nodes](/use/learn/going-further/custom-nodes/), [Shaders](/use/learn/going-further/shaders/)

**Do** give the window bridge a text style for the tooltips when your UIs use them.
[Style the window bridge](/use/learn/running/backends/#style-the-window-bridge)

## Building and shipping

**Do** develop with the dev jar: the inspector, the reload and the warnings find most mistakes as you make them.
[Dev tools](/use/get-started/dev-tools/)

**Do** ship the prod jar, and run the application once with it before you publish.
[Ship your application](/use/guides/ship-your-application/)

**Do** test your screens with snapshots, so that a change that breaks a screen fails a test.
[Testing your UIs](/use/learn/running/testing/)
