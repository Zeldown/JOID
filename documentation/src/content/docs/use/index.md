---
title: "Introduction"
description: "What JOID is, who it is for, and where to start in this documentation."
genre: learn
sidebar:
  order: 0
---

JOID is a Java library that draws interfaces: menus, panels, forms, players, dashboards.
You describe each screen in Java, on a fixed canvas of 1920 × 1080 design pixels, and JOID draws it sharp in a window of any size, with OpenGL or Vulkan.

Here is a complete screen: a card with a button that turns the notifications on and off.

```java file="com/example/start/IntroUI.java" title="IntroUI.java"
```

<figure>

![A white card with rounded corners and a soft shadow on a light grey background, reading Notifications next to a rounded button; the pointer clicks the button four times, and the button turns dark grey and reads On, then light grey and reads Off](/captures/start-intro.webp)

<figcaption>
The screen above, clicked four times.
The button reads the signal `notifications`, so its color and its label follow each click by themselves.
</figcaption>
</figure>

`ExampleFont` loads the font of the examples ([Quick Start](/use/get-started/quick-start/#run-any-example-of-this-documentation)).

The [Quick Start](/use/get-started/quick-start/) writes a smaller screen line by line and runs it in a window.

## What JOID gives you

- **A tree of nodes.** Rectangles, text, images, videos, 3D models, controls and layouts, created by factories and nested in each other ([Nodes and the tree](/use/learn/describing-the-ui/nodes/)).
- **A design canvas.** You copy positions and sizes from your mockup, and JOID scales the canvas to the window ([The design canvas](/use/learn/describing-the-ui/design-canvas/)).
- **Signals.** A node that reads a signal follows its value: change the value, and the screen follows ([Signals](/use/learn/interactivity/signals/)).
- **Your own look.** Controls handle the behavior and the input; you decide how they look ([Give your controls a look](/use/learn/going-further/ui-kit/)).
- **Sharp rendering.** Text, curves and images stay sharp at every size, on every backend ([Pixel alignment](/use/in-depth/pixel-alignment/)).
- **Dev tools.** With the dev jar, an inspector, a hot reload, and warnings that name the cause and the fix ([Dev tools](/use/get-started/dev-tools/)).

## Who this documentation is for

This documentation has two parts that never mix.

**Use JOID**, the part you are reading, is for Java developers who build interfaces with JOID and one of its backends: LWJGL 3, Vulkan or LWJGL 2.
It assumes that you know Java 8 and a build tool such as Gradle, and nothing about JOID.

[Build on JOID](/build/) is for the developers who write a backend, to run JOID on another graphics library or inside their own engine.

The [reference](/reference/) belongs to both: it lists every public type of JOID, member by member, with its default values.

## Where to start

Start with [Installation](/use/get-started/installation/), then the [Quick Start](/use/get-started/quick-start/), which opens a first window and tours the rest.
The sidebar follows the reading order from there: the [Tutorial](/use/tutorial/) builds a complete menu lesson by lesson, then Learn explains each notion in full.

## How to read Learn

The Learn chapters form one path, from describing a screen to running it in your application.
Each page relies only on the pages before it, so read them in the sidebar order the first time, starting with [How JOID works](/use/learn/how-joid-thinks/).
A page explains one notion in full; any other page that needs it gives one sentence and a link.

The examples of Learn and of the guides are UIs: the [launcher of the Quick Start](/use/get-started/quick-start/#run-any-example-of-this-documentation) runs any of them.
The [glossary](/reference/glossary/) defines the terms that many pages use, such as design pixel, signal or popup.

## License

JOID is released under the [Apache License 2.0](https://github.com/Zeldown/JOID/blob/main/LICENSE).
Its jars embed software under their own licenses, among them JSVG and TwelveMonkeys ImageIO for images, JavaCV for videos, and ASM, which reads your bytecode to follow signals.
The `META-INF/NOTICE` file inside every JOID jar credits each of them, and `META-INF/licenses/` holds the texts of the licenses other than Apache 2.0.
