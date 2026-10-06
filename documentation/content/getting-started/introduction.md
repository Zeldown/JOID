# Introduction

Welcome to JOID, a Java 8 library for building fast, beautiful user interfaces. You write an interface once, in Java, and it runs on any rendering engine, with the design of your choice. Each screen is a tree of nodes that JOID lays out, animates and draws on the GPU every frame, in its own window on LWJGL 2, LWJGL 3 or Vulkan, or inside any host that already owns a render loop.

## A first look

Here is a complete screen: a button that counts its clicks.

```java
public final class CounterUI extends UI {

    private final IntegerSignal clicks = new IntegerSignal();
    private final IFont font;

    public CounterUI(final IFont font) {
        this.font = font;
    }

    @Override
    public void init() {
        final TextInfo info = TextInfo.create(this.font, 40, Color.WHITE);

        RectNode
        .create(760, 440, 400, 120)
        .color(Color.decode("#1F2937"), Color.decode("#374151"))
        .effect(RoundedNodeEffect.create(16F))
        .onClick((node, mouseX, mouseY, clickType) -> this.clicks.increment())
        .body(button -> {
            TextNode
            .create(button.dw(2), button.dh(2))
            .text(Text.create("", info))
            .<TextNode>onInit(node -> node.getText().text("Clicks: " + this.clicks.getOrDefault()))
            .watch(this.clicks)
            .anchor(Align.CENTER)
            .attach(button);
        })
        .attach(this);
    }

}
```

![The mouse moves onto a dark rounded button, which lightens, then clicks it three times while its label counts from Clicks: 0 to Clicks: 3](../images/intro-counter.gif "The label follows the signal; the button lightens under the mouse")

The whole screen is Java: factories, chained setters and lambdas, checked by the compiler and completed by your IDE. The rectangle lightens under the mouse, its corners are rounded by a shader effect, and the label watches the signal: each click publishes a new count and the label writes it. The [Quick Start](quick-start.md) turns this class into a running program.

## Why JOID

### Write once, render anywhere

Your UIs talk only to JOID's API. JOID reaches the window, the GPU and the audio device through small interfaces, the [bridges](../integration/bridges.md), and a backend implements them for one rendering engine.

```
Your UIs  ──>  JOID API  ──>  bridges  ──>  LWJGL 2 · LWJGL 3 · Vulkan · your engine
```

- **One code base for every renderer**: the same screens run on OpenGL 2, OpenGL 3.3 and Vulkan, in a standalone window or embedded in a game. The renderer is the backend you register at startup.
- **Always up to date**: when a rendering engine or its version changes, only its backend changes. Your UIs keep compiling and keep looking the same, so you follow every new engine and version with the code you already have.
- **The same pixels everywhere**: the snapshot tests render the same scenes on every official backend and compare them pixel by pixel (see [Testkit](../integration/testkit.md)).
- **Your engine next**: a new renderer only needs a backend; the [template](../integration/writing-a-backend.md) and the render contract tests guide you to a complete one.

### Pixel-perfect from your design

You design on a 1920×1080 frame in your design tool, and every JOID UI is laid out on the same 1920×1080 canvas, which JOID fits to any window size. Positions, sizes, colors, corner radii, fonts, weights and font sizes therefore carry over as they are: read them in the inspector, write them in your nodes, and the render lands on the design pixel for pixel.

![A design is rebuilt by copying each layer's values into JOID nodes, then the JOID render is overlaid on the design export with a sliding divider](../images/pixel-perfect.mp4 "Every value copied from the design; 99.7 % of the pixels identical, the rest is edge anti-aliasing")

### Your design, your UI kit

JOID is design-neutral: you decide every pixel. Its components bring the behavior, the state and the input: a slider knows its values and follows the mouse, a selector opens and picks an option, a checkbox flips and calls you back. You draw each component once, in a small subclass, and these subclasses form your project's UI kit.

Your screens use the components through the same API whatever the kit:

```java
Slider.create(0, 0, 300, 24).values(0, 100, 65).signal(volume).attach(panel);
```

With a flat kit this line draws a thin track and a square thumb; with another kit, the very same line draws a glowing rounded pill. A new design for a whole application is a new UI kit, with the same screens. See [Building a UI Kit](../components/ui-kit.md).

### A developer experience designed end to end

- **A fluent, typed API**: every node is created with a factory and configured with chained setters; your IDE completes it and the compiler checks it, down to the type of the signals you connect.
- **Sensible defaults**: a node works with its factory alone, and every option has a default you can override in the chain.
- **Dev mode**: an on-screen inspector, a profiler, hot reload, and warnings that point at the line of your code that caused them (see [Developer Tools](dev-tools.md)).
- **One documentation path**: a tutorial, short essentials, then a complete page for every topic.

### Everything an interface needs, in one library

From a simple rectangle to a video, JOID draws it:

- **Shapes and layout**: rectangles, circles, flex and grid layouts, scrolling, drag and drop, reorderable lists.
- **Text**: MSDF fonts that stay sharp at any size and rotation, weights and italics, markup, per-glyph effects, text fields.
- **Media**: PNG, JPEG, SVG, animated GIF, APNG and WebP, video with audio and positional sound, 3D models.
- **Effects and motion**: gradients, rounded corners, borders, blur, masks, transforms, custom shaders, tweens, easings and UI transitions.
- **Controls and data**: sliders, checkboxes, toggles, switches, selectors, line, bar and radar charts.

### Direct GPU rendering

JOID draws straight through your engine's graphics API: each node turns into GPU draw calls and shader passes, with no intermediate rendering layer in between. Text is drawn as one quad per glyph from an MSDF atlas, effects run as shaders on the GPU, and resources are uploaded once and cached. Your interface gets the full speed of the renderer it runs on.

### Reactive state

A `Signal<T>` holds a value and notifies whoever depends on it: `watch` updates or rebuilds just the part of the tree that depends on it, and a slider or a text field writes to it. Stores share state between UIs and persist it to disk. You change the data; the interface follows. See [Signals](../state/signals.md).

## Key ideas

| Idea | In one sentence |
| --- | --- |
| Retained node tree | A screen is a `UI` that owns a tree of `Node` objects: you build it once in `init()`, change it afterwards, and JOID redraws it every frame. |
| Virtual canvas | You design every UI on a 1920×1080 canvas that JOID fits into the window, whatever its size. |
| Signals | A `Signal<T>` holds a value and notifies whoever depends on it. |
| Effects | Rounded corners, borders, blur, masks and transforms are effects you add to any node, composed in a shader pipeline. |
| Bridges and backends | The bridges connect JOID to an engine; a backend implements them, so your UIs run on every engine. |

The [Core Concepts](core-concepts.md) page explains each idea once you have seen them at work in the tutorial.

## How to read these docs

The navigation is a learning path. Read the first two sections in order, then jump to what you need.

| Section | What you find there | Start with |
| --- | --- | --- |
| **Get Started** | Installation, a minimal program, a four-part tutorial that builds a real settings screen, and the core concepts. | [Installation](installation.md), [Tutorial](../tutorial/setup.md) |
| **Essentials** | Short pages, read in order, covering the everyday use of each topic: UIs, nodes, layout, styling, input, state, text, media, animation. | [UIs](../essentials/uis.md) |
| **Text** | The text model, styling text, markup and per-glyph text effects. | [Text and TextInfo](../text/text-and-textinfo.md) |
| **Fonts** | How fonts work in JOID, adding your own fonts, the MSDF generator, custom font implementations. | [How Fonts Work](../fonts/how-fonts-work.md) |
| **Components** | A catalog with one page per node: layout, display, inputs, data. | [Component Catalog](../components/overview.md) |
| **Guides** | Complete, in-depth pages on every topic: UIs, nodes, interactions, state, styling, resources, animation, drawing, shaders. | [The UI Class](../ui/ui-class.md), [Effects](../styling/effects.md), [Resources](../resources/resources.md) |
| **Advanced** | Writing your own nodes, effects, shaders and resource formats. | [Custom Nodes](../nodes/custom-nodes.md) |
| **Integration** | Bridges, the official backends, writing a backend, snapshot testing. | [Bridges](../integration/bridges.md) |
| **Reference** | Utilities and the changelog. | [Changelog](../changelog/8.0.0.md) |

Every guide page starts with a minimal example, then explains the behavior, then lists the full API of its classes.

## Requirements at a glance

- Java 8 or later.
- One backend: LWJGL 2 (OpenGL with GLSL 1.20 shaders), LWJGL 3 (OpenGL 3.3 core profile) or Vulkan 1.3, or your own.
- A TrueType or OpenType font file for your text.

[Installation](installation.md) lists the jars, the libraries to declare and the natives of each backend.

## See also

- [Installation](installation.md)
- [Quick Start](quick-start.md)
- [Tutorial 1: Project Setup](../tutorial/setup.md)
- [Core Concepts](core-concepts.md)
- [License](license.md)