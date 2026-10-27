---
title: "How JOID thinks"
description: "The ideas behind JOID before any code: UIs, the node tree and the life of a node, the design canvas, signals, and where JOID runs in your application."
genre: learn
sidebar:
  order: 0
---

With JOID, you describe an interface and JOID draws it.
This page gives the few ideas behind that model, without code, so that every later page fits into a picture you already hold.

**You will learn**
- What a UI is, and how open UIs stack
- Why everything on screen is a node in a tree, and how a node lives
- Why you write every position for one fixed canvas
- How a value on screen follows your data
- Where JOID runs in your application, and what one frame does

<figure>

![A map of JOID on a light grey background. A top row of three white boxes joined by arrows from left to right: Your loop (events, frame(), present), Backend (LWJGL 3, Vulkan or LWJGL 2) and UI bridge (routes input, draws the UIs). An arrow goes down from the UI bridge to the open UIs, labelled Open UIs, stacked: a white Screen card with a violet Popup card over its corner. An arrow goes left from the screen to a node tree inside an outlined frame labelled Design canvas, 1920 x 1080: a UI box with arrows to a RectNode box and a TextNode box, and an arrow from the RectNode box down to a box reading Clicks: 2. At the bottom left, a violet box labelled Signal reads clicks = 2, with an arrow to the Clicks: 2 box.](/captures/thinking-map.webp)

<figcaption>
Your loop runs the backend, the UI bridge holds the open UIs, each UI is a tree of nodes on the design canvas, and the nodes follow signals.
Each section of this page explains one part of the map.
</figcaption>
</figure>

## Each interface is a UI

A UI is one interface of your application: a main menu, a settings panel, a dialog, a panel that stays over your scene.
It is a Java class that extends [[UI]] and builds its content in `init()`, each time it opens.

You open and close UIs with [[JOID#open]] and `JOID.close`.
JOID keeps the open ones in order, sends the mouse and the keyboard to the right one, and draws them.

Several UIs can be open at once.
A **screen** takes the window, and opening another screen replaces it; a **popup** opens above another UI; an **overlay** stays over the screens and can let the input through.
JOID draws them from the bottom up and sends the input from the top down ([See how UIs stack](/use/learn/describing-the-ui/overlays-and-popups/#see-how-uis-stack)).

Read [UIs](/use/learn/describing-the-ui/uis/) to write one and follow it from its opening to its closing.

## A UI is a tree of nodes

Everything a UI shows is a node: a rectangle, a text, an image, a video, a control, a container.
Each node does one thing, and you build an interface by nesting them: a button is a rectangle with a text inside, a card is a rectangle that holds a title, an image and a button.

A node attached to another node becomes its child.
A child is placed relative to its parent, and moves, hides and leaves with it.
You never draw pixels in the common case: you describe the tree, and JOID draws it on every frame.

A node also has a life inside that tree.
It joins its UI when you attach it to the UI or to a node already in it, then updates and draws on every frame while it stays there.
It leaves when you remove it, when its parent leaves, or when its UI closes; the next opening of the UI builds a fresh tree.
Callbacks run your code at each of these moments ([Node lifecycle](/use/learn/describing-the-ui/node-lifecycle/#follow-a-node-from-attach-to-remove)).

Read [Nodes and the tree](/use/learn/describing-the-ui/nodes/) to build a tree.

## You design on one fixed canvas

Every position and size you write is in **design pixels**, on a canvas 1920 wide and 1080 high.
JOID scales the whole canvas to the window by one factor, the largest that keeps it whole, so a square stays a square and a centered button stays centered.
A window of another shape shows more space on two sides of the canvas, and your nodes can reach into it.

You never compute a layout for each window size.
You copy the coordinates of a 1920 × 1080 frame of your mockup, and the interface looks the same in a small window and on a 4K screen.

Read [The design canvas](/use/learn/describing-the-ui/design-canvas/) to see how the canvas fits windows of other shapes.

## Values on screen follow signals

A **signal** holds a value that changes: a score, a name, a list of rounds.
A node property set from a signal follows it: when the value changes, the node shows the new one from the next frame.

So you change the data, never the nodes.
A click adds one to a counter, and every text, width and color built from that counter follows.

Behind this, every node reads the values it follows again on every frame, before it draws.
What you gave to the setter decides what that read does: a plain value never changes, an expression that reads signals is computed again only after one of them changed, and a lambda runs on every frame.

When a value on screen does not update, ask one question: when does JOID compute this value again?
[Signals](/use/learn/interactivity/signals/#ask-when-does-joid-compute-this-value-again) answers it for each form.

## JOID runs inside your application

JOID never creates a window and never runs a loop: your application owns both.
It creates its window, hands it to a **backend**, and calls [[Backend#frame]] once per turn of its loop.
The backend runs JOID on LWJGL 3, Vulkan or LWJGL 2, and the three draw the same pixels: your UIs never name it, and switching backends changes your startup code only ([Choosing a backend](/use/learn/running/backends/)).

The backend reaches the window through **bridges**, small interfaces that it fills in for JOID.
The one you meet is the **UI bridge**: it holds the open UIs, routes the input to them and draws them on every frame.
The backend registers one, the window bridge; an application that draws a scene of its own under JOID, or a library with its own panels, registers others ([Embedding JOID](/use/learn/running/embedding/#fit-joid-in-your-loop)).

### What one frame does

Input arrives between two frames: when your loop polls the window events, each event goes at once to the open UIs, from the top one down.
Then `frame()` updates every open UI and its nodes, and draws them: bridge after bridge, UI after UI, node after node.

All of it, your callbacks included, runs on one thread, the render thread, so nothing in a frame runs at the same time as your code ([Threads](/use/learn/running/threads/)).
[The frame lifecycle](/use/in-depth/frame-lifecycle/) lists every step, for the day you need to know exactly when your code runs.

## Recap

- A **UI** is one interface, built in `init()` at each opening; open UIs stack as **screens**, **popups** and **overlays**.
- Everything on screen is a **node**; nodes nest into a **node tree**, and a child follows its parent.
- A node joins its UI when attached, updates and draws on every frame, and leaves when removed or when its UI closes.
- Every position and size is in **design pixels** on a 1920 × 1080 **design canvas** that JOID scales to the window.
- A node property set from a **signal** follows its value: change the data, not the nodes.
- Your application owns the window and the loop; the **backend** runs JOID in them, and the **UI bridge** holds, routes and draws the UIs.
- One **frame** updates everything, then draws everything, on the **render thread**.

## Try it

**Name the UIs.**
A settings panel opens above the main menu, and the menu stays visible around it.
Which two UIs are open, and of which kinds?

<details>
<summary>Solution</summary>

The main menu is a screen, and the settings panel is a popup opened above it.

</details>

**Shrink the window.**
A button sits at x = 1720 on the canvas, near its right edge.
The user shrinks the window to half its width and half its height.
Where is the button now?

<details>
<summary>Solution</summary>

Still near the right edge, at the same place relative to the rest of the interface: JOID scales the whole canvas, so every position keeps its proportion.

</details>

**A frozen score.**
A text shows a score held in an `int` field of the UI.
A click adds one to the field, and the text does not change.
Why?

<details>
<summary>Solution</summary>

The text reads no signal, so it is a plain value: JOID never computes it again.
Hold the score in a signal and build the text from it, and the text follows each click.

</details>

**Switch the backend.**
Your application moves from LWJGL 3 to Vulkan.
Which of your UI classes change?

<details>
<summary>Solution</summary>

None of them.
Only the startup code that creates the window and the backend changes: the UIs never name the backend.

</details>
