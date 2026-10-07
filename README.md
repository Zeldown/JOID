<div align="center">

# JOID
## Java Open Interface Development

<div align="center">
  <img align="center" src="https://img.shields.io/badge/version-8.0.0 (00d946d)-blue">
  <img align="center" src="https://img.shields.io/badge/maintainer-Zeldown-orange">
  <img align="center" src="https://img.shields.io/maintenance/yes/9999">
  <img align="center" src="https://img.shields.io/badge/license-Apache--2.0-blue">
  <img align="center" src="https://github.com/Zeldown/JOID/actions/workflows/release.yml/badge.svg">
</div>

<br>

**Write your interface once. Render it anywhere, with your design.**

[**Documentation**](https://joid.dev-zeldown.workers.dev/) · [**Quick Start**](https://joid.dev-zeldown.workers.dev/#/getting-started/quick-start) · [**Tutorial**](https://joid.dev-zeldown.workers.dev/#/tutorial/setup) · [**Releases**](https://github.com/Zeldown/JOID/releases)

</div>

<br>

JOID is a UI engine for Java 8. You build each screen from nodes, JOID draws it on the GPU of the engine you already use, and you decide how everything looks.

<p align="center">
  <img src="documentation/content/images/pixel-perfect.webp" alt="A design rebuilt in JOID by copying each layer's values, then overlaid on the design export" width="100%">
</p>

## Why JOID

- **One UI, any engine.** Write your interface once and run it anywhere: LWJGL 2, LWJGL 3, Vulkan, or any other engine, even one you built yourself.
- **Your mockup, at any size.** Lay out your UI on a fixed design canvas, copy positions, sizes and colors straight from your mockup, and JOID scales it to any window.
- **Reactive to your data.** Show your data and forget about it: when a value changes, everything displaying it on screen updates by itself.
- **Your look, not ours.** Components handle behavior and input. You draw them in your own UI kit, and another kit gives the same code a whole new design.
- **Looks great at any size.** Text and shapes stay razor-sharp at any size and any resolution, thanks to built-in MSDF text and anti-aliasing.
- **Everything included.** Images, SVG, animated GIF and WebP, video, 3D models, shadows, blur, masks, shaders, animations, and whatever else you can imagine.
- **Made to be pleasant to use.** A typed fluent API, sensible defaults, and a dev mode with an inspector, hot reload and warnings that point at your line of code.

## Showcase

Every frame below is a real JOID render on the 1920×1080 canvas. Only the mouse pointer is drawn on top.

<p align="center">
  <img src="documentation/content/images/showcase-hero.webp" alt="A dashboard drawn by JOID: a large gradient headline, a video card with rounded corners, a live chart, a spinning 3D model and a notification sliding in, over soft blurred colors" width="100%">
</p>

<p align="center"><sub>Video, a lit 3D model, a live chart, gradients, glows and blur in one screen. <a href="documentation/content/images/showcase.mp4">Watch the full showcase in 1080p (MP4)</a>.</sub></p>

<table>
  <tr>
    <td width="50%"><img src="documentation/content/images/showcase-reactive.webp" alt="Three sliders drive a glowing orb: its size, colors and glow, the numbers and an equalizer follow every move"></td>
    <td width="50%"><img src="documentation/content/images/showcase-type.webp" alt="Text from 14 to 108 px in several fonts, with markup and animated effects, then a 6x zoom that stays perfectly sharp"></td>
  </tr>
  <tr>
    <td align="center"><sub><b>Reactive.</b> Drag a slider and every node that reads its value follows.</sub></td>
    <td align="center"><sub><b>Typography.</b> Markup, animated effects, and a 6× zoom that stays sharp.</sub></td>
  </tr>
  <tr>
    <td width="50%"><img src="documentation/content/images/showcase-drag.webp" alt="A record is dragged from a library into a player, snaps into place, and the player shows its title and color"></td>
    <td width="50%"><img src="documentation/content/images/uikit-side-by-side.png" alt="The same screen code drawn by two different UI kits"></td>
  </tr>
  <tr>
    <td align="center"><sub><b>Drag and drop.</b> Drop a record, it snaps into place and the player updates.</sub></td>
    <td align="center"><sub><b>Your design.</b> The same screen code, drawn by two UI kits.</sub></td>
  </tr>
</table>

The code stays short. This button counts its clicks, and its label updates by itself:

```java
public final class CounterUI extends UI {

	private static final Color VIOLET = Color.decode("#8B5CF6");
	private static final Color CYAN   = Color.decode("#22D3EE");

	private final IntegerSignal clicks = IntegerSignal.of(0);

	@Override
	public void init() {
		final TextInfo info = TextInfo.create(Theme.getFont(), 40F, Color.WHITE);

		RectNode
		.create(760, 440, 400, 120)
		.color(CounterUI.VIOLET.toGradient(CounterUI.CYAN))
		.effect(RoundedNodeEffect.create(24F))
		.effect(ShadowNodeEffect.create(CounterUI.VIOLET, 32F))
		.onClick((node, mouseX, mouseY, clickType) -> this.clicks.increment())
		.body(button -> {
			TextNode.create(button.dw(2), button.dh(2)).text(Text.create("Clicks: " + this.clicks.get(), info)).anchor(Align.CENTER).attach(button);
		})
		.attach(this);
	}

}
```

`Theme` loads your font once at startup: the [Quick Start](https://joid.dev-zeldown.workers.dev/#/getting-started/quick-start) shows the whole program, window included.

## Documentation

<div align="center">

### [**joid.dev-zeldown.workers.dev**](https://joid.dev-zeldown.workers.dev/)

Getting started · Tutorial · Essentials · Components · Guides · Search (`Ctrl+K`)

</div>

Start with the [Quick Start](https://joid.dev-zeldown.workers.dev/#/getting-started/quick-start) for a first window in a few minutes, or follow the [Tutorial](https://joid.dev-zeldown.workers.dev/#/tutorial/setup) to build a complete screen. The site also lives in the [`documentation/`](documentation) folder: serve it with any static HTTP server (`cd documentation && npx serve .`).

## Credits

- [Universal Tween Engine](https://github.com/AurelienRibon/universal-tween-engine) by **Aurélien Ribon**: tween animation engine (Apache-2.0, bundled in `lib/animation/tweenengine`)
- [JavaCV / FFmpeg](https://github.com/bytedeco/javacv) by **Bytedeco**: video decoding (Apache-2.0; the FFmpeg builds carry their own terms)

JOID is released under the [Apache License 2.0](LICENSE).
