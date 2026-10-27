---
title: "Troubleshooting"
description: "Find the cause of a problem from its symptom, with the checks to run in order."
genre: guide
sidebar:
  order: 1
---

Each section starts from what you see, then lists the checks in the order that finds the cause fastest.
Run your application with the dev jar while you look for a problem: JOID then prints a `[JOID] …` warning for most of the mistakes below, and [Warnings and errors](/reference/errors/) explains each message.

## The window stays black

1. **No UI is open.** Check that your program calls [[JOID#open]] after the `register()` of the backend, and that `Escape` did not close the screen.
2. **The loop never draws.** Each turn of your loop must call `frame()` on the backend, then present the image ([Choosing a backend](/use/learn/running/backends/)).
3. **Your UI has no backdrop, and the window background is black.** A UI draws nothing behind its nodes unless its [[UIData]] sets `background = true` ([UIs](/use/learn/describing-the-ui/uis/)), and the window background is black until you change it ([Style the window bridge](/use/learn/running/backends/#style-the-window-bridge)): dark nodes on it look like nothing.

## Opening a UI throws "No IUIBridge can open"

`JOID.open(...)` ran before any UI bridge was registered, so nothing could receive the UI.

1. Open your first UI after the `register()` of the backend.
2. In code that does not control the startup order, such as a library, give the call to `JOID.onReady(...)` ([Run code once JOID is ready](/use/learn/running/backends/#run-code-once-joid-is-ready)).

## My node does not appear

1. **It is not attached.** A node shows only once attached, to the UI with `attach(this)` or to a parent node ([Nodes and the tree](/use/learn/describing-the-ui/nodes/)).
2. **It is created in the constructor of the UI.** Create every node in `init()` ([UIs](/use/learn/describing-the-ui/uis/)).
3. **It is outside the window.** Positions are in design pixels on the 1920 × 1080 canvas, and a child is placed relative to its parent: a child at 900 of a parent at 1500 sits at 2400, off the canvas.
4. **It is hidden or covered.** Look for a `visible(false)` on it or on a parent, for a parent that clips its children, and for a node drawn above it ([Position and size](/use/learn/describing-the-ui/position-and-size/)).
5. **Its color matches what is behind it**, or its alpha is 0.

With the dev jar, the inspector shows which nodes sit under the mouse ([Dev tools](/use/get-started/dev-tools/)).

## My text does not appear

1. **The text has the color of its background.** The color of a text is in its [[TextInfo]], black by default.
2. **The characters are not in the font.** A generated atlas holds the characters from U+0020 to U+0233 ([MSDF text](/use/in-depth/msdf-text/#what-the-atlas-holds)).
3. **The text node has no text.** A [[TextNode]] draws the [[Text]] given to `text(...)`, and nothing without it.

JOID ships no font for your application: [Fonts](/use/learn/describing-the-ui/fonts/) shows how to load yours.

## My image shows a checkerboard, or nothing

1. **The resource cannot be read.** In dev mode, JOID draws a magenta and black checkerboard in its place and prints `[JOID] The resource … cannot be read: …` with the cause; with the prod jar, it draws nothing.
2. **The path is wrong.** A path without a scheme is read from the classpath, then as a file; a URL needs its scheme, such as `https:`.
3. **The format is not supported.** [Images and media](/use/learn/describing-the-ui/media/#supported-formats) lists the formats JOID reads.

## A value on screen does not update

A node follows a value only when JOID can compute it again ([Signals](/use/learn/interactivity/signals/)).

1. **The value is not a signal.** A plain field or a local variable, read once, never updates the node: hold it in a signal.
2. **JOID could not follow the expression.** A lambda inside the expression, or a local variable in a calculation, leaves it fixed, and the dev jar prints `[JOID] … but cannot follow it: …` with the line, the reason and the fix ([What JOID follows](/use/learn/interactivity/signals/#what-joid-follows)).
3. **The signal object was replaced.** A node follows the signal it read: give the existing signal a value with `set(...)` instead of creating another signal.
4. **The collection changed behind the signal.** Change a [[ListSignal]], [[MapSignal]] or [[SetSignal]] through its own methods, never through the collection that `get()` returns.
5. **The number of nodes depends on the value.** Rows that follow a list need a rebuild ([Rebuilding the tree](/use/learn/state/rebuilding/)).

## My click does nothing

1. **Another node is on top.** The press goes to the topmost node under the pointer, and a node drawn above yours takes it ([Events](/use/learn/interactivity/events/)).
2. **The node is disabled**, or a parent is: `enabled(false)` turns off its reactions.
3. **A popup is open.** While a popup is open, the UIs under it receive no input ([Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/)).
4. **The listener is on another node.** `onClick` on a child does not fire when the press lands on the parent outside the child.

## Tooltips do not show

The window bridge has no text style for them, because JOID ships no font.
Give it one with `hoverInfo(...)` ([Style the window bridge](/use/learn/running/backends/#style-the-window-bridge)); in dev mode, JOID warns about it once.

## The first launch is slow

The first time a font loads on a machine, JOID generates its atlas and keeps it in the cache folder; later launches read it back ([What happens when a font loads](/use/in-depth/msdf-text/#what-happens-when-a-font-loads)).
The first video also downloads the FFmpeg natives into the cache folder ([Show a video](/use/guides/show-a-video/)).

## My settings are lost after a restart

1. **The program was killed.** JOID saves stores and properties when a UI closes and when the JVM shuts down normally; a killed process saves nothing ([Close the UIs before the program ends](/use/learn/running/backends/#close-the-uis-before-the-program-ends)).
2. **The store is not permanent.** Only a store of scope `PERMANENT` writes a file ([Stores and remembered properties](/use/learn/state/stores-and-properties/)).
3. **The working directory changed.** The data folder is relative to the working directory by default ([Where JOID stores files](/use/learn/running/files/)).

## Escape closes my screen

A screen and a popup close on `Escape` unless a node consumes the key first.
Set `closeable = false` in the `@UIData` of the UI ([Know who receives the keyboard](/use/learn/interactivity/keyboard/#know-who-receives-the-keyboard)).

## The dev tools do nothing

The inspector and the reload exist only in the dev jar.
Check the line JOID prints when it loads: without `in dev mode`, a prod jar is on the classpath ([Installation](/use/get-started/installation/)).

## The program stops at startup on macOS

Start the JVM with `-XstartOnFirstThread` for the LWJGL 3 and Vulkan backends ([Choosing a backend](/use/learn/running/backends/)).
