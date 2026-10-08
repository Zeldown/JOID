# Introduction

JOID is a design-neutral UI engine for Java 8. You build each screen as a tree of nodes on a 1920×1080 virtual canvas, JOID lays it out, animates it and draws it on the GPU every frame, and your code runs unchanged on LWJGL 2, LWJGL 3, Vulkan or any engine that already owns a render loop.

## A first look

Here is a complete screen: a button that counts its clicks.

```java
public final class CounterUI extends UI {

	private static final Color INK   = Color.decode("#999999");
	private static final Color HOVER = Color.decode("#808080");

	private final IntegerSignal clicks = IntegerSignal.of(0);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(Theme.getFont(), 40F, Color.WHITE);

		RectNode
		.create(760, 440, 400, 120)
		.color(CounterUI.INK)
		.hoveredColor(CounterUI.HOVER)
		.effect(RoundedNodeEffect.create(16F))
		.onClick((node, mouseX, mouseY, clickType) -> this.clicks.increment())
		.body(button -> {
			TextNode.create(button.dw(2), button.dh(2)).text(Text.create("Clicks: " + this.clicks.get(), info)).anchor(Align.CENTER).attach(button);
		})
		.attach(this);
	}

}
```

![The mouse moves onto a rounded gray button, which darkens, then clicks it three times while its label counts from Clicks: 0 to Clicks: 3](../images/intro-counter.gif "The label follows the signal; the button blends to its hover color")

The whole screen is Java: factories, chained setters and lambdas, checked by the compiler and completed by your IDE. The rectangle darkens under the mouse, a shader effect rounds its corners, and the label reads the `clicks` signal: each click changes the signal, and JOID recomputes the text. The [Quick Start](quick-start.md) turns this class into a running program.

## What JOID is

| Idea | In one sentence |
| --- | --- |
| Retained node tree | A screen is a `UI` that owns a tree of `Node` objects: you build it once in `init()`, and JOID redraws it every frame. |
| Virtual canvas | Every UI is laid out on a 1920×1080 canvas that JOID fits into the window, whatever its size. |
| Reactive signals | A `Signal<T>` holds a value; the nodes that read it follow its changes, and controls write into it. |
| Effects | Rounded corners, borders, shadows, blur, masks and transforms are effects you add to any node, run as shaders. |
| Bridges and backends | Bridges connect JOID to a window, a renderer and an audio device; a backend implements them for one engine. |
| Design-neutral | JOID imposes no look: its components bring the behavior, and you draw them, or use a kit that draws them. |

The [Core Concepts](../concepts/canvas.md) section gives each idea its own page, right after the [Quick Start](quick-start.md).

### Write once, render anywhere

Your UIs talk only to the API of JOID. JOID reaches the window, the GPU and the audio device through small interfaces, the [bridges](../integration/bridges.md), and a backend implements them for one rendering engine.

![Your UIs talk to JOID, JOID talks to the bridges, and one backend per engine implements the bridges: LWJGL 2, LWJGL 3, Vulkan or your engine](../images/diagram-intro-layers.png "One code base for every renderer")

- **One code base for every renderer**: the same screens run on every OpenGL from 2.0 to 4.6 and on Vulkan, in a standalone window or embedded in a game. The renderer is the backend you register at startup.
- **The same pixels everywhere**: the snapshot tests render the same scenes on every official backend and compare them pixel by pixel (see [Testkit](../integration/testkit.md)).
- **Your engine next**: a new renderer only needs a backend; the [template](../integration/writing-a-backend.md) and the render contract tests guide you to a complete one.

### One canvas, every window

You design on a 1920×1080 frame, and every JOID UI is laid out on the same 1920×1080 canvas, which JOID fits into the window: positions and sizes are canvas units, whatever the resolution. A window of another shape keeps the proportions of the canvas and shows extra canvas on the sides or above and below, which your UI can use.

![The 1920×1080 canvas fitted into a 16:9, a 21:9 and a 4:3 window; the extra visible area is hatched](../images/diagram-canvas.png "One canvas, fitted into every window")

Positions, sizes, colors, corner radii, fonts, weights and font sizes therefore carry over from your design tool as they are: read them in the inspector, write them in your nodes, and the render lands on the design pixel for pixel.

![A design is rebuilt by copying each layer's values into JOID nodes, then the JOID render is overlaid on the design export with a sliding divider](../images/pixel-perfect.mp4 "Every value copied from the design; 99.7 % of the pixels identical, the rest is edge anti-aliasing")

### Your design, your UI kit

JOID draws nothing you did not ask for. Its components bring the behavior, the state and the input: a slider knows its values and follows the mouse, a selector opens and picks an option, a checkbox flips and calls you back. You draw each component once, in a small subclass, and these subclasses form the UI kit of your project. Your screens use the components through the same API whatever the kit:

```java
Slider.create(200, 110, 280, 44).values(0, 100, 65).signal(this.volume).attach(panel);
```

With a flat kit this line draws a thin track and a square cursor; with another kit, the very same line draws a rounded track and a glowing cursor. A new design for a whole application is a new UI kit, with the same screens. See [Building a UI Kit](../components/ui-kit.md).

### State that the screen follows

A `Signal<T>` holds a value. A setter that receives an expression reading signals, such as `text(Text.create("Clicks: " + this.clicks.get(), info))`, follows them and recomputes its value when they change; a control bound with `signal(...)` writes into the signal. Stores share state between UIs and save it to disk. You change the data; the interface follows. See [Signals](../state/signals.md) and [Reactive Properties](../state/reactive-properties.md).

### A developer experience designed end to end

- **A fluent, typed API**: every node is created with a factory and configured with chained setters; your IDE completes it and the compiler checks it, down to the type of the signals you connect.
- **Sensible defaults**: a node works with its factory alone, and every option has a default you can override in the chain.
- **Dev mode**: an on-screen inspector, a grid, a profiler, hot reload, and warnings that point at the line of your code that caused them (see [Developer Tools](../concepts/dev-tools.md)).
- **Direct GPU rendering**: each node turns into draw calls and shader passes of the engine, with no intermediate rendering layer; text is drawn from MSDF atlases, effects run as shaders, and resources are uploaded once and cached.

## What you can build

From a simple rectangle to a video, JOID draws it:

- **Shapes and layout**: rectangles, circles, flex and grid layouts, scrolling, drag and drop, reorderable lists.
- **Text**: MSDF fonts that stay sharp at any size and rotation, weights and italics, markup, per-glyph effects, single-line and multiline text fields.
- **Media**: PNG, JPEG, SVG, GIF, APNG and WebP images, animated or not, video with audio and positional sound, 3D models.
- **Effects and motion**: gradients, rounded corners, borders, shadows, blur, masks, transforms, custom shaders, tweens, easings and UI transitions.
- **Controls and data**: sliders, checkboxes, toggles, switches, selectors, line charts and radar charts.

Menus and settings screens of a game, the overlay of a tool, a launcher, a dashboard: a JOID UI runs in its own window or inside the render loop of a host.

## How to read these docs

The navigation is a learning path: each page relies only on the pages before it. Read Getting Started, Core Concepts, Essentials and the Tutorial in order; the later sections go deeper on each topic, and you can read them in order or jump to what you need.

| Section | What you find there | Start with |
| --- | --- | --- |
| **Getting Started** | This introduction, the installation and a minimal program you run. | [Installation](installation.md), [Quick Start](quick-start.md) |
| **Core Concepts** | One short page per foundation: the virtual canvas, UIs, nodes, input, signals, styling, the frame loop, bridges, the developer tools. | [The Virtual Canvas](../concepts/canvas.md) |
| **Essentials** | Short pages that apply the concepts to everyday screens: layout, text, input controls, saving state, images and media, animation. | [Layout](../essentials/layout.md) |
| **Tutorial** | Four parts that build a real settings screen with everything above. | [Tutorial 1: Project Setup](../tutorial/setup.md) |
| **Components** | A catalog with one page per node (layout, display, inputs, data), and how to build your UI kit. | [Component Catalog](../components/overview.md) |
| **Guides** | Complete pages on every topic: nodes, styling, interactions, animation, state, UIs, resources. | [Node Fundamentals](../nodes/node-fundamentals.md), [Signals](../state/signals.md) |
| **Text** and **Fonts** | The text model, styling, markup and text effects; how fonts work, adding fonts, the MSDF generator. | [Text and TextInfo](../text/text-and-textinfo.md) |
| **Advanced** | Custom nodes, drawing, 3D models, framebuffers and shaders. | [Custom Nodes](../nodes/custom-nodes.md) |
| **Integration** | Bridges, the official backends, writing a backend, snapshot testing. | [Bridges](../integration/bridges.md) |
| **Reference** | Utilities and the changelog. | [Changelog](../changelog/8.0.0.md) |

Every page starts with a minimal example and its result, then the common uses, the options, the full API of its classes and the pitfalls.

## Requirements at a glance

- Java 8 or later.
- One backend: LWJGL 2 or LWJGL 3 (OpenGL 2.0 to 4.6, compatibility or core) or Vulkan 1.3, or your own.
- A TrueType or OpenType font file for your text.

[Installation](installation.md) lists the jars, the libraries to declare and the natives of each backend.

## See also

- [Installation](installation.md)
- [Quick Start](quick-start.md)
- [Tutorial 1: Project Setup](../tutorial/setup.md)
- [The Virtual Canvas](../concepts/canvas.md)
- [Building a UI Kit](../components/ui-kit.md)
- [License](license.md)