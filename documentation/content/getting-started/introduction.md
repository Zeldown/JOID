# Introduction

JOID is a design-neutral UI engine for Java 8. You build each screen as a tree of nodes on a 1920×1080 virtual canvas; JOID lays it out, animates it and draws it on the GPU every frame, and your code runs unchanged on LWJGL 2, LWJGL 3, Vulkan or an engine that already owns a render loop.

## A first look

Here is a complete screen: a button that counts its clicks. `Theme.getFont()` returns the font your application loads at startup (see [Quick Start](quick-start.md)).

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
		.onClick((node, mouseX, mouseY, button) -> this.clicks.increment())
		.body(rect -> {
			TextNode.create(rect.dw(2), rect.dh(2)).text(Text.create("Clicks: " + this.clicks.get(), info)).anchor(Align.CENTER).attach(rect);
		})
		.attach(this);
	}

}
```

![The mouse moves onto a rounded gray button, which darkens, then clicks it three times while its label counts from Clicks: 0 to Clicks: 3](../images/intro-counter.gif "The label follows the signal; the button blends to its hover color")

The whole screen is Java: factories, chained setters and lambdas that your IDE completes and the compiler checks. The rectangle blends to its hover color, an effect rounds its corners, and the label reads the `clicks` signal: each click changes the signal and JOID recomputes the text.

## What JOID is

| Idea | In one sentence |
| --- | --- |
| Retained node tree | A screen is a `UI` that owns a tree of `Node` objects; you build it once in `init()` and JOID draws it every frame. |
| Virtual canvas | Every UI is laid out on a 1920×1080 canvas that JOID fits into the window, whatever its size. |
| Reactive signals | A `Signal<T>` holds a value; the nodes that read it follow its changes, and controls write into it. |
| Effects | Rounded corners, borders, shadows, blur, masks and transforms run as shaders on any node. |
| Bridges and backends | Bridges connect JOID to a window, a renderer and an audio device; a backend implements them for one engine. |
| Design-neutral | Components bring the behavior and you draw them once, in your own UI kit. |

![Your UIs talk to JOID, JOID talks to the bridges, and one backend per engine implements the bridges: LWJGL 2, LWJGL 3, Vulkan or your engine](../images/diagram-intro-layers.png "One code base for every renderer")

## What you can build

- **Shapes and layout**: rectangles, circles, flex and grid layouts, scrolling, drag and drop, reorderable lists.
- **Text**: MSDF fonts that stay sharp at any size, weights, markup, per-glyph effects, text fields.
- **Media**: PNG, JPEG, SVG, GIF, APNG, WebP, video with audio, 3D models.
- **Effects and motion**: gradients, shadows, blur, masks, custom shaders, tweens and UI transitions.
- **Controls and data**: sliders, checkboxes, toggles, switches, selectors, line and radar charts.

## How to read these docs

Read Getting Started, then Core Concepts in order: each page relies only on the pages before it. The Tutorial then builds a complete settings screen. After that, jump to what you need.

## Requirements

- Java 8 or later.
- One backend: LWJGL 2 or LWJGL 3 (OpenGL 2.0 to 4.6) or Vulkan 1.3, or your own.
- A TrueType or OpenType font file.

## License

JOID is released under the Apache License, Version 2.0. The full text is in `LICENSE` and the attribution notices in `NOTICE`, both at the root of the repository and inside every JOID jar (`META-INF/`). Keep them in what you redistribute.

## See also

- [Installation](installation.md)
- [Quick Start](quick-start.md)
- [Canvas and Scaling](../concepts/canvas.md)
- [Tutorial 1: Project Setup](../tutorial/setup.md)
- [Building a UI Kit](../components/ui-kit.md)