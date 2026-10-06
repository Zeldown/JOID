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

<p align="center">
  <img src="documentation/content/images/pixel-perfect.webp" alt="A design rebuilt in JOID by copying each layer's values, then overlaid on the design export" width="100%">
</p>

## Why JOID

- 🔌 **Renderer-agnostic** — your UIs talk only to JOID, and a backend adapts it to the engine underneath. The same code runs on LWJGL 2, LWJGL 3, Vulkan or your own engine, and on every version of them, every backend being checked pixel by pixel against the others.
- 📐 **Pixel-perfect from your design** — every UI is laid out on a 1920×1080 canvas, the frame you design on: copy the positions, sizes, colors and font settings from your design tool and the render lands on the mockup pixel for pixel, scaled to any window.
- 🎨 **Design-neutral** — every component an interface needs comes with its behavior, state and input handled, and the look you give it. Draw them once in your own UI kit: the same usage code gets an entirely different design with another kit.
- 🧰 **Developer experience, end to end** — a fluent, typed API completed by your IDE and checked by the compiler, sensible defaults, and a dev mode with an inspector, a profiler, hot reload and warnings that point at your own line of code.
- 🧱 **Everything in one library** — from a simple rectangle to a video: MSDF text sharp at any size, images, SVG, animated GIF/APNG/WebP, video with positional audio, 3D models, gradients, rounded corners, borders, blur, masks, custom shaders, tweens and transitions.
- ⚡ **Direct GPU rendering** — nodes turn straight into GPU draw calls and shader passes of your engine, with no intermediate rendering layer.
- 🔄 **Reactive state** — signals update exactly the parts of the interface that depend on them, and stores share and persist your state across UIs.

## Showcase

<table>
  <tr>
    <td width="50%"><img src="documentation/content/images/tutorial-overview.png" alt="A settings screen built with JOID"></td>
    <td width="50%"><img src="documentation/content/images/tutorial-polish-hover.gif" alt="Hover animations and a tooltip"></td>
  </tr>
  <tr>
    <td align="center"><sub>A settings screen, built step by step in the tutorial</sub></td>
    <td align="center"><sub>Hover animations, tooltips and an animated switch</sub></td>
  </tr>
  <tr>
    <td colspan="2"><img src="documentation/content/images/uikit-side-by-side.png" alt="The same code drawn by two UI kits"></td>
  </tr>
  <tr>
    <td colspan="2" align="center"><sub>The same screen code, drawn by two UI kits: only the import changes</sub></td>
  </tr>
  <tr>
    <td width="50%"><img src="documentation/content/images/text-effects-animated.gif" alt="Animated text effects"></td>
    <td width="50%"><img src="documentation/content/images/msdf-sizes.png" alt="MSDF text at 16, 64 and 256 px"></td>
  </tr>
  <tr>
    <td align="center"><sub>Markup and per-glyph text effects</sub></td>
    <td align="center"><sub>MSDF text, sharp from 16 to 256 px</sub></td>
  </tr>
</table>

## Documentation

<div align="center">

### 📖 [**joid.dev-zeldown.workers.dev**](https://joid.dev-zeldown.workers.dev/)

Getting started · Tutorial · Essentials · Components · Guides · Search (`Ctrl+K`)

</div>

Start with the [Quick Start](https://joid.dev-zeldown.workers.dev/#/getting-started/quick-start) for a first window in a few minutes, or the [Tutorial](https://joid.dev-zeldown.workers.dev/#/tutorial/setup) to build a complete screen. The site also lives in the [`documentation/`](documentation) folder: serve it with any static HTTP server (`cd documentation && npx serve .`).

## Credits

- [Universal Tween Engine](https://github.com/AurelienRibon/universal-tween-engine) by **Aurélien Ribon** — Tween animation engine (Apache-2.0, bundled in `lib/animation/tweenengine`)
- [msdfgen](https://github.com/Chlumsky/msdfgen) by **Viktor Chlumský** — the multi-channel signed distance field algorithm, which JOID reimplements in Java in the `msdf/` module
- [LWJGL](https://www.lwjgl.org/) — OpenGL / Vulkan / OpenAL Java bindings used by the backends (BSD-3-Clause)
- [JavaCV / FFmpeg](https://github.com/bytedeco/javacv) by **Bytedeco** — Video decoding (Apache-2.0; the FFmpeg builds carry their own terms)

JOID is released under the [Apache License 2.0](LICENSE).
