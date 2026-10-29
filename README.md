<div align="center">

# JOID
## Java Open Interface Development

<div align="center">
  <img align="center" src="https://img.shields.io/badge/version-8.0.0 (f74ac60)-blue">
  <img align="center" src="https://img.shields.io/maintenance/yes/9999">
  <img align="center" src="https://img.shields.io/badge/license-Apache--2.0-blue">
  <img align="center" src="https://github.com/Zeldown/JOID/actions/workflows/release.yml/badge.svg">
</div>

<br>

**Write your interface once. Render it anywhere, with your design.**

[**Documentation**](https://documentation.joid.dev/) · [**Quick Start**](https://documentation.joid.dev/#/getting-started/quick-start) · [**Tutorial**](https://documentation.joid.dev/#/tutorial/setup) · [**Releases**](https://github.com/Zeldown/JOID/releases)

</div>

<br>

JOID is a Java UI engine: build your interface once, run it on any engine, and get your mockup pixel for pixel at any window size, in a look that is entirely yours.

## Why JOID

- **One UI, any engine.** Write your interface once and run it anywhere: LWJGL 2, LWJGL 3, Vulkan, or any other engine, even one you built yourself.
- **Your mockup, at any size.** Lay out your UI on a fixed design canvas, copy positions, sizes and colors straight from your mockup, and JOID scales it to any window.
- **Reactive to your data.** Show your data and forget about it: when a value changes, everything displaying it on screen updates by itself.
- **Your look, not ours.** Components handle behavior and input. You draw them in your own UI kit, and another kit gives the same code a whole new design.
- **Looks great at any size.** Text and shapes stay razor-sharp at any size and any resolution.
- **Everything included.** Images, SVG, animated GIF and WebP, video, 3D models, shadows, blur, masks, shaders, animations, and whatever else you can imagine.
- **Made to be pleasant to use.** A typed fluent API, sensible defaults, and a dev mode with an inspector, hot reload and warnings that point at your line of code.

<p align="center">
  <img src="documentation/content/images/pixel-perfect.webp" alt="A design rebuilt in JOID by copying each layer's values, then overlaid on the design export" width="100%">
</p>

## Showcase

Real JOID renders, styled for this showcase: JOID ships with no design of its own. Watch them in the [1080p video](documentation/public/showcase/showcase.mp4).

<p align="center">
  <img src="documentation/public/showcase/hero.webp" alt="A dashboard drawn by JOID: a large gradient headline, a video card with rounded corners, a live chart, a spinning 3D teapot and a notification sliding in, over soft blurred colors" width="100%">
</p>

<table>
  <tr>
    <td width="50%"><img src="documentation/public/showcase/reactive.webp" alt="Three sliders drive a glowing orb: its size, colors and glow, the numbers, the swatches and an equalizer follow every move, then a switch lights a halo behind the orb"></td>
    <td width="50%"><img src="documentation/public/showcase/type.webp" alt="Text from 14 to 108 px in several fonts, with markup and animated effects, then a 6x zoom that stays perfectly sharp"></td>
  </tr>
  <tr>
    <td align="center"><sub><b>Reactive.</b> Values follow your data.</sub></td>
    <td align="center"><sub><b>Typography.</b> Markup, effects, sharp zoom.</sub></td>
  </tr>
  <tr>
    <td width="50%"><img src="documentation/public/showcase/effects.webp" alt="Seven switches add rounded corners, a gradient, an inner border, a drop shadow, a glow, a blur and a tilt to a card, one by one"></td>
    <td width="50%"><img src="documentation/public/showcase/motion.webp" alt="Six balls slide along their tracks with six easing curves, each curve plotted next to its track"></td>
  </tr>
  <tr>
    <td align="center"><sub><b>Effects.</b> Stack and animate any effect.</sub></td>
    <td align="center"><sub><b>Motion.</b> Any property, any curve.</sub></td>
  </tr>
  <tr>
    <td width="50%"><img src="documentation/public/showcase/form.webp" alt="A sign-up form: a name and an email are typed, a word is selected with a double click and replaced, and a live profile card follows"></td>
    <td width="50%"><img src="documentation/public/showcase/player.webp" alt="A video player with designed controls: the pointer pauses the video, hovers the progress bar where a preview frame follows it, drags the bar to seek, then plays the video again"></td>
  </tr>
  <tr>
    <td align="center"><sub><b>Text input.</b> Selection, markup, validation.</sub></td>
    <td align="center"><sub><b>Video.</b> Controls you design.</sub></td>
  </tr>
  <tr>
    <td width="50%"><img src="documentation/public/showcase/model.webp" alt="A textured teapot turned with the mouse and zoomed with the wheel, its yaw, pitch and zoom shown live"></td>
    <td width="50%"><img src="documentation/public/showcase/lists.webp" alt="A playlist: a track is dragged to a new place, the others make room, then the list scrolls with a custom scrollbar"></td>
  </tr>
  <tr>
    <td align="center"><sub><b>3D models.</b> Lit, textured, interactive.</sub></td>
    <td align="center"><sub><b>Lists.</b> Reorder, custom scrollbars.</sub></td>
  </tr>
  <tr>
    <td width="50%"><img src="documentation/public/showcase/drag.webp" alt="A record is dragged from a library into a player, snaps into place, and the player shows its title and color"></td>
    <td width="50%"><img src="documentation/public/showcase/theme.webp" alt="A dashboard switches from light to dark and back with one toggle, every color fading smoothly"></td>
  </tr>
  <tr>
    <td align="center"><sub><b>Drag and drop.</b> Drop, snap, update.</sub></td>
    <td align="center"><sub><b>Themes.</b> One signal, a new theme.</sub></td>
  </tr>
  <tr>
    <td width="50%"><img src="documentation/public/showcase/inspect.webp" alt="The dev mode over the playlist: the grid is turned on and off, a reload rebuilds the list, the panel is dragged to the top, hovered nodes are outlined with their render time, and a click locks a row whose details scroll in the panel"></td>
    <td width="50%"><img src="documentation/public/showcase/design.webp" alt="A settings screen drawn by a dark neon kit, wiped into a flat paper kit by the same code and back: switches and a slider react in both kits, a save button confirms, and the values follow from one kit to the other"></td>
  </tr>
  <tr>
    <td align="center"><sub><b>Dev mode.</b> Inspect any node live.</sub></td>
    <td align="center"><sub><b>Your design.</b> Same code, two UI kits.</sub></td>
  </tr>
</table>

## Quality

The same scene twice: a plain renderer on the left, JOID on the right. Both are real renders, and the framed areas are enlarged pixel for pixel.

<p align="center">
  <img src="documentation/content/images/quality-text.webp" alt="The phrase Sharp at every size from 8 to 140 px, side by side: an atlas baked at 24 px breaks small letters and blurs large ones, JOID is sharp at every size" width="100%">
</p>
<p align="center"><sub><b>Text.</b> One font, sharp at every size.</sub></p>

<table>
  <tr>
    <td width="50%"><img src="documentation/content/images/quality-shapes.webp" alt="Circles and rounded rectangles: polygons show stair steps, JOID's edges are smooth"></td>
    <td width="50%"><img src="documentation/content/images/quality-pixel-font.webp" alt="A pixel-art font at 10, 15, 20 and 24 px: the scaled atlas blurs and bleeds, JOID stays crisp"></td>
  </tr>
  <tr>
    <td align="center"><sub><b>Shapes.</b> Smooth curves.</sub></td>
    <td align="center"><sub><b>Pixel fonts.</b> Crisp at any size.</sub></td>
  </tr>
  <tr>
    <td width="50%"><img src="documentation/content/images/quality-downscale.webp" alt="A detailed image shrinking: the plain renderer flickers with moiré, JOID stays clean"></td>
    <td width="50%"><img src="documentation/content/images/quality-svg.webp" alt="A compass icon growing to 216 px: the plain renderer blurs, JOID stays sharp"></td>
  </tr>
  <tr>
    <td align="center"><sub><b>Images.</b> Clean when scaled down.</sub></td>
    <td align="center"><sub><b>Icons.</b> Sharp when scaled up.</sub></td>
  </tr>
</table>

## Documentation

<div align="center">

### [**documentation.joid.dev**](https://documentation.joid.dev/)

Getting started · Core concepts · Essentials · Tutorial · Components · Guides · Search (`Ctrl+K`)

</div>

Start with the [Quick Start](https://documentation.joid.dev/#/getting-started/quick-start) for a first window in a few minutes, then read the [Core Concepts](https://documentation.joid.dev/#/concepts/canvas) in order, starting with the virtual canvas: the documentation is a learning path where each page relies only on the ones before it. The site also lives in the [`documentation/`](documentation) folder: serve it with any static HTTP server (`cd documentation && npx serve .`).

JOID is released under the [Apache License 2.0](LICENSE).
