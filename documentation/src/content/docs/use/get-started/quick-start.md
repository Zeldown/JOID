---
title: "Quick Start"
description: "A first window with a UI, then a tour of the 80 % of JOID you use every day, with a link to each topic."
genre: learn
sidebar:
  order: 2
---

This page builds the smallest complete JOID program: a window with a button that counts its clicks.
Then it tours what you use every day, one short section per topic, each with a link to the page that explains it.

<figure>

![A blue button with rounded corners at the center of the window; the button lightens under the pointer, which clicks it three times, and its label goes from Clicks: 0 to Clicks: 3](/captures/quick-start-counter.webp)

<figcaption>
The program of this page: at each click, the label follows the counter by itself.
</figcaption>
</figure>

## Write your first program

Start from the project of [Installation](/use/get-started/installation/), with the LWJGL 3 jar, and set the main class to `com.example.start.Main`.

**1. Add a font.**
JOID ships no font for your application ([Fonts](/use/learn/describing-the-ui/fonts/)).
Put any TrueType or OpenType file at `src/main/resources/fonts/Montserrat/Montserrat-Regular.ttf`; the program loads it from the classpath.

**2. Write the UI.**
A UI is a class that extends [[UI]] and creates its nodes in `init()`:

```java file="com/example/start/CounterUI.java" title="CounterUI.java"
```

- [[MsdfFontLoader]] loads the font; the first run on a machine takes a few seconds, and later runs read a [cache](/use/in-depth/msdf-text/#what-happens-when-a-font-loads).
- `lock()` keeps a shared color from being changed by accident ([Keep a palette safe](/use/learn/style-and-motion/styling/#keep-a-palette-safe)).
- [[RectNode]]`.create(760, 480, 400, 120)` is a 400 × 120 rectangle at the center of the 1920 × 1080 canvas, and `attach(this)` adds it to the UI.
- `onClick(...)` increments the signal `clicks`, and the label that reads `this.clicks.get()` follows it: [Signals](/use/learn/interactivity/signals/) explains how.

**3. Write the program.**
JOID never creates a window: your program creates it with GLFW, hands it to the backend, opens the UI and calls `frame()` in its loop.

```java file="com/example/start/Main.java" title="Main.java"
```

- [[GlContextRequest]]`.CORE_33.apply()` asks GLFW for an OpenGL 3.3 core context with the depth and stencil buffers that JOID needs.
- `register()` needs the context current on this thread, because it creates GPU objects at once.
- `frame()` draws one frame of every open UI, and `glfwSwapBuffers` shows it.
- `closeAll()` closes the open UIs once the window closes, so that what they keep [reaches the disk](/use/learn/running/backends/#close-the-uis-before-the-program-ends).

**4. Run it** with `gradle run`.
The console prints `[JOID] 8.0.0 loaded in dev mode, …` ([Installation](/use/get-started/installation/#choose-the-dev-or-the-prod-jar)), and the window shows the button: each click increments the counter, and Escape closes the UI.

This program runs on LWJGL 3.
With Vulkan or LWJGL 2, take the complete program of your backend from [Start the other backends](/use/learn/running/backends/#start-the-other-backends) and open `CounterUI` in it; the rest of this page does not change, and the tutorial changes only `Main`: apply its highlighted lines to your program.

### If it does not work

Check, in this order:

1. **`ExceptionInInitializerError` in `CounterUI`**: the font file is not at `src/main/resources/fonts/Montserrat/Montserrat-Regular.ttf`, and the stack trace reads `The asset fonts/Montserrat/Montserrat-Regular.ttf is not found in the classpath nor as a file`.
2. **`UnsatisfiedLinkError`, `NoClassDefFoundError`, or a thread error on macOS**: an LWJGL module or its natives are missing; [If it does not work](/use/get-started/installation/#if-it-does-not-work) of Installation gives the cause of each.
3. **`Unable to create the window`**: the driver offers no OpenGL 3.3 core context; call `GlContextRequest.COMPATIBILITY.apply()` instead, to take the default context of the driver.
4. **The window stays black or empty**: [Troubleshooting](/use/troubleshooting/) lists the checks by symptom.

## Run any example of this documentation

Most examples of Learn and of the guides are UIs, and the "Full file" link under each one shows its class without a `main`; the few that start JOID themselves, in [Running your UI](/use/learn/running/), have their own `main`.
An example that uses other classes of its package, such as a markup, a node or a store, needs them too: copy the whole package of the example.
Most of them write their text with `ExampleFont.REGULAR`, a family of two faces of Montserrat loaded once for every example.
Add this class to the package `com.example`, and put `Montserrat-Bold.ttf` next to the `Montserrat-Regular.ttf` of step 1:

```java file="com/example/ExampleFont.java" title="ExampleFont.java"
```

Your application loads its own family the same way ([Fonts](/use/learn/describing-the-ui/fonts/)): use it wherever an example names `ExampleFont.REGULAR`.

Then add this launcher next to `Main`.
It opens the UI whose class you name, gives the tooltips a text style, and closes the open UIs when the window closes:

```java file="com/example/start/Launcher.java" title="Launcher.java"
```

Set the main class to `com.example.start.Launcher`, then name the UI on the command line:

```bash
gradle run --args="com.example.start.CounterUI"
```

An example that reads a file reads it from the resources, under the path that its code names.
The [resources of the examples](https://github.com/Zeldown/JOID/tree/main/documentation/examples/src/main/resources) hold every one of them: fonts, images, a video and 3D models.
The exception is an example that reads the disk with `new File(...)`: put a file of yours at that path, from the working directory.
Copy the folders you need into your `src/main/resources`, under the same paths.

JOID draws no control by itself: each example of a control gives it its parts, the nodes that draw it, where it creates it.

## Place nodes on the design canvas

Every position and size is in design pixels, on a canvas of 1920 × 1080.
JOID scales the whole canvas to the window, so you copy coordinates from your mockup as they are.
Resize the window of the program above: the button stays centered and keeps its proportions.

Read [The design canvas](/use/learn/describing-the-ui/design-canvas/) to fit the canvas to a window of any shape.

## Build a tree of nodes

A node is a shape, a text, an image, a control or a container.
`body(...)` creates the children of a node, and a child is positioned relative to its parent.

Read [Nodes and the tree](/use/learn/describing-the-ui/nodes/), then [Position and size](/use/learn/describing-the-ui/position-and-size/).
The [Catalog](/use/catalog/) shows every node that JOID offers.

The examples of the rest of this page come from one UI, `TourUI`.
Its nodes each sit in a card of their own, `body(card -> ...)`, and the fields and the lines of `init()` that they read are shown above them: paste each one at the same place in your UI, or open its full file.

## Lay out a row or a column

A [[FlexNode]] places its children one after the other, so you compute no position:

```java file="com/example/start/TourUI.java" region="flex"
```

<figure>

![Three blue squares in a row on a dark card, 20 design pixels apart](/captures/quick-start-flex.webp)

<figcaption>
The row places each square after the previous one.
</figcaption>
</figure>

Read [Layout](/use/learn/describing-the-ui/layout/), [Flex layout](/use/learn/describing-the-ui/flex-layout/) and [Grid layout](/use/learn/describing-the-ui/grid-layout/).

## Show text

A [[TextNode]] draws a [[Text]], built from a string and a [[TextInfo]]: the font, the size, the weight and the color.
`label(...)`, in the program above, centers a text on any node.

Read [Text](/use/learn/describing-the-ui/text/) and [Fonts](/use/learn/describing-the-ui/fonts/), then [Text markup and effects](/use/learn/describing-the-ui/text-markup-and-effects/) to style part of a text.

## Show an image

A [[ResourceNode]] draws an image, an animated GIF or WebP, or an SVG, loaded with [[Resource]]:

```java file="com/example/start/TourUI.java" region="image"
```

<figure>

![A grey placeholder image crossed by two diagonals, on a dark card](/captures/quick-start-image.webp)

<figcaption>
The image fills the size of the node.
</figcaption>
</figure>

Read [Images and media](/use/learn/describing-the-ui/media/), which also plays videos and sounds.

## React to the mouse and the keyboard

`hoveredColor(...)` and `onClick(...)` in the program above are the most common reactions.
Nodes also react to the pointer entering and leaving, to drags, to the wheel and to keys.

Read [Events](/use/learn/interactivity/events/), [Hover, tooltips and cursors](/use/learn/interactivity/hover-and-tooltips/), [Drag and drop](/use/learn/interactivity/drag-and-drop/) and [Keyboard and shortcuts](/use/learn/interactivity/keyboard/).
[Controls](/use/learn/interactivity/controls/) and [Text fields](/use/learn/interactivity/text-fields/) cover checkboxes, sliders and text input.

## Follow a value with a signal

A node set from an expression that reads a signal follows that signal.
`map(...)` derives a signal from another:

```java file="com/example/start/TourUI.java" region="signals"
```

```java file="com/example/start/TourUI.java" region="click"
```

<figure>

![The pointer clicks a dark grey rectangle on a dark card three times; at the third click, the rectangle turns blue](/captures/quick-start-click.webp)

<figcaption>
At the third click, the derived signal turns `true` and the color follows.
</figcaption>
</figure>

Read [Signals](/use/learn/interactivity/signals/), then [Derived values](/use/learn/state/derived-values/) and [Rebuilding the tree](/use/learn/state/rebuilding/).

## Remember a value

A field annotated with [[UIProperty]] keeps its value from one run to the next.
JOID saves it when the UI closes ([Close the UIs before the program ends](/use/learn/running/backends/#close-the-uis-before-the-program-ends)):

```java file="com/example/start/TourUI.java" region="property"
```

Increment it in `init()`, which runs at each opening, and show it in a text, `"Visit " + this.visits`:

```java file="com/example/start/TourUI.java" region="visits"
```

Read [Stores and remembered properties](/use/learn/state/stores-and-properties/) to share values between UIs, and [Where JOID stores files](/use/learn/running/files/) to find the files.

## Round the corners and add a shadow

An effect changes how a node is drawn, never where it sits:

```java file="com/example/start/TourUI.java" region="effects"
```

<figure>

![A blue rectangle with rounded corners and a soft shadow below it, on a dark card](/captures/quick-start-effects.webp)

<figcaption>
Two effects on one rectangle: the shadow follows the rounded shape.
</figcaption>
</figure>

Read [Colors and styling](/use/learn/style-and-motion/styling/), then [Effects](/use/learn/style-and-motion/effects/).

## Animate a value

A [[TweenAnimator]] moves a value over time; it is a signal, so a setter that reads it in an expression follows it:

```java file="com/example/start/TourUI.java" region="animator"
```

```java file="com/example/start/TourUI.java" region="animation"
```

<figure>

![A blue square slides from left to right and back on a dark card](/captures/quick-start-animation.webp)

<figcaption>
The animator goes from 0 to 1 in 800 ms, then plays back and forth forever.
</figcaption>
</figure>

An animator that repeats forever keeps running after its UI closes: the full file stops it with `slide.clear()` when its card detaches ([Chain, repeat and replay](/use/learn/style-and-motion/animation/#chain-repeat-and-replay)).

Read [Animation](/use/learn/style-and-motion/animation/), then [Transitions](/use/learn/style-and-motion/transitions/) to animate a UI when it opens and closes.

## Open a popup

A popup is a UI opened above the current one.
The annotation [[UIDataPopup]], `@UIDataPopup(active = true)`, turns a UI into a popup, `background = true` of [[UIData]] dims the screen behind it, and [[JOID#open]] opens it:

```java file="com/example/start/NoticeUI.java" region="popup"
```

```java file="com/example/start/TourUI.java" region="popup"
```

<figure>

![A dark card with a dark grey button above the text "Visit 4"; the pointer clicks the button: a dark backdrop dims the screen and an empty dark card opens above it; a click on that card closes it and the screen comes back](/captures/quick-start-popup.webp)

<figcaption>
The popup opens above the screen, which stays open behind its backdrop; a click on the popup closes it.
</figcaption>
</figure>

Read [UIs](/use/learn/describing-the-ui/uis/), then [Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/).

## Inspect and reload your UI

With the dev jar, `F3` shows the inspector, and `Ctrl+R` or `F5` runs `init()` again.
To reload the code you change, start the application in debug mode from your IDE: hot reload then reloads the UI by itself.
JOID also prints a warning that starts with `[JOID]` when a call cannot work, with its cause and its fix.

Read [Dev tools](/use/get-started/dev-tools/).

## Next steps

- Build a complete menu, lesson by lesson, in the [Tutorial](/use/tutorial/).
- Learn the ideas behind JOID in [How JOID works](/use/learn/how-joid-thinks/), then follow the Learn chapters in order.
