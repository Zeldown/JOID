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

Every frame below is a real JOID render on the 1920×1080 canvas. Only the mouse pointer is drawn on top. Watch them all in the [1080p video](documentation/content/images/showcase.mp4).

<p align="center">
  <img src="documentation/content/images/showcase-hero.webp" alt="A dashboard drawn by JOID: a large gradient headline, a video card with rounded corners, a live chart, a spinning 3D teapot and a notification sliding in, over soft blurred colors" width="100%">
</p>

<table>
  <tr>
    <td width="50%"><img src="documentation/content/images/showcase-reactive.webp" alt="Three sliders drive a glowing orb: its size, colors and glow, the numbers, the swatches and an equalizer follow every move"></td>
    <td width="50%"><img src="documentation/content/images/showcase-type.webp" alt="Text from 14 to 108 px in several fonts, with markup and animated effects, then a 6x zoom that stays perfectly sharp"></td>
  </tr>
  <tr>
    <td align="center"><sub><b>Reactive.</b> Drag a slider and every node that reads its value follows.</sub></td>
    <td align="center"><sub><b>Typography.</b> Markup, animated effects, and a 6× zoom that stays sharp.</sub></td>
  </tr>
  <tr>
    <td width="50%"><img src="documentation/content/images/showcase-effects.webp" alt="Seven switches add rounded corners, a gradient, an inner border, a drop shadow, a glow, a blur and a tilt to a card, one by one"></td>
    <td width="50%"><img src="documentation/content/images/showcase-motion.webp" alt="Six balls slide along their tracks with six easing curves, each curve plotted next to its track"></td>
  </tr>
  <tr>
    <td align="center"><sub><b>Effects.</b> Stack corners, gradients, shadows, glow and blur, and animate every value.</sub></td>
    <td align="center"><sub><b>Motion.</b> Ease any property with the curve you pick.</sub></td>
  </tr>
  <tr>
    <td width="50%"><img src="documentation/content/images/showcase-form.webp" alt="A sign-up form: a name and an email are typed, a word is selected with a double click and replaced, and a live profile card follows"></td>
    <td width="50%"><img src="documentation/content/images/showcase-player.webp" alt="A video player with designed controls: the pointer pauses the video, drags the progress bar to seek, then plays it again"></td>
  </tr>
  <tr>
    <td align="center"><sub><b>Text input.</b> Typing, selection, markup and validation, drawn in your kit.</sub></td>
    <td align="center"><sub><b>Video.</b> Play, pause and seek a video with controls you design.</sub></td>
  </tr>
  <tr>
    <td width="50%"><img src="documentation/content/images/showcase-model.webp" alt="A textured teapot turned with the mouse and zoomed with the wheel, its yaw, pitch and zoom shown live"></td>
    <td width="50%"><img src="documentation/content/images/showcase-lists.webp" alt="A playlist: a track is dragged to a new place, the others make room, then the list scrolls with a custom scrollbar"></td>
  </tr>
  <tr>
    <td align="center"><sub><b>3D models.</b> Show a lit, textured OBJ model and let the mouse turn it.</sub></td>
    <td align="center"><sub><b>Lists.</b> Drag rows to reorder them, scroll with a scrollbar you draw.</sub></td>
  </tr>
  <tr>
    <td width="50%"><img src="documentation/content/images/showcase-drag.webp" alt="A record is dragged from a library into a player, snaps into place, and the player shows its title and color"></td>
    <td width="50%"><img src="documentation/content/images/showcase-theme.webp" alt="A dashboard switches from light to dark and back with one toggle, every color fading smoothly"></td>
  </tr>
  <tr>
    <td align="center"><sub><b>Drag and drop.</b> Drop a record, it snaps into place and the player updates.</sub></td>
    <td align="center"><sub><b>Themes.</b> Flip one signal and the whole screen changes theme.</sub></td>
  </tr>
  <tr>
    <td width="50%"><img src="documentation/content/images/showcase-inspect.webp" alt="The dev mode over the playlist: the grid is turned on and off, a reload rebuilds the list, the panel is dragged to the top, hovered nodes are outlined with their render time, and a click locks a row whose details scroll in the panel"></td>
    <td width="50%"><img src="documentation/content/images/showcase-design.webp" alt="A settings screen drawn by a dark neon kit, then wiped into a flat paper kit by the same code: switches, a slider and a save button react in both, and the values follow from one kit to the other"></td>
  </tr>
  <tr>
    <td align="center"><sub><b>Dev mode.</b> Grid, reload, a movable panel, and a click to inspect any node.</sub></td>
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
