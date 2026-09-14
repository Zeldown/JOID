<div align="center">

# JOID
## Java OpenGL Interface Development

<div align="center">
  <img align="center" src="https://img.shields.io/badge/version-6.5.4 (f9432b5)-blue">
  <img align="center" src="https://img.shields.io/badge/maintainer-Zeldown-orange">
  <img align="center" src="https://img.shields.io/maintenance/yes/9999">
  <img align="center" src="https://github.com/Zeldown/JOID/actions/workflows/release.yml/badge.svg">
</div>

<br>

**Build GPU-composed UIs in pure Java — no CSS, no XML, no runtime parser.**
<br><br>
Welcome to JOID, a flexible component-based UI toolkit for OpenGL and Vulkan applications, made for developers who want to own their rendering path and ship interfaces that stand out. No default theme, no stylesheet dialect to fight — the code you write is the layout the GPU draws.
<br><br>
Under the hood: a retained-mode node tree, reactive signals that only notify the nodes watching them, a composable shader pipeline for custom GPU effects, and a set of bridges that drop the library into any host and any rendering engine — games, tools, editors, overlays.
<br><br>
No DSL to learn, no scene-graph format to serialize, no runtime engine to boot. Just chainable Java classes and a compiler that catches UI bugs the way it catches everything else — rename a node and every reference follows, wire the wrong signal type and you get a build error instead of a silent runtime failure.
<br><br>

[Installation](#installation)
[Backends](#backends)
[Features](#features)
[Documentation](#documentation)
[License](#license)
[Credits](#credits)

</div>

## Installation

JOID is distributed via GitHub Releases for every backend (`lwjgl2`, `lwjgl3`, `vulkan`), each as two artifacts:

- **joid-<backend>-X.Y.Z-prod.jar** — production build (excludes dev/demo assets)
- **joid-<backend>-X.Y.Z-dev.jar** — dev build (includes demo assets, fonts, demo textures)

Download the desired artifact from the [Releases page](https://github.com/Zeldown/JOID/releases) and add it to your project's classpath.

### Gradle

```groovy
dependencies {
    compile files('libs/joid-lwjgl2-6.5.4-prod.jar')
}
```

### Maven

```xml
<dependency>
    <groupId>be.zeldown.joid</groupId>
    <artifactId>joid</artifactId>
    <version>6.5.4</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/joid-lwjgl2-6.5.4-prod.jar</systemPath>
</dependency>
```

### Native libraries

The `impl/lwjgl2` module contains a `native/` folder with the required OpenGL and OpenAL native libraries. Make sure they are exposed to the JVM via `-Djava.library.path=./native` at launch. The LWJGL 3 and Vulkan backends resolve their natives from Maven.

## Backends

JOID is a multi-module Gradle build. The `core` module contains the engine-agnostic library, and each rendering engine is a module under `impl/`, with its own `build.gradle`, shaders and ready-to-run demo (`./gradlew :vulkan:runDemo`). LWJGL 3 and Vulkan share the `glfw` window module and the `openal` audio module:

| Module | Engine | Entry point |
|---|---|---|
| `core` | Engine-agnostic core | — |
| `lwjgl2` | LWJGL 2.9.1 — OpenGL fixed pipeline, OpenAL | `Backend.register()` |
| `lwjgl3` | LWJGL 3.3.4 — GLFW, OpenGL 3.3 core, OpenAL | `Backend.register(window)` |
| `vulkan` | LWJGL 3.3.4 — GLFW, Vulkan 1.3, shaderc, OpenAL | `Backend.register(window)` |

Entry points live in the `be.zeldown.joid.impl.<module>` package of each backend. Backends only implement the bridges — they never modify the `core` module. See [Backends](documentation/content/ui/backends.md) to write a new one.

## Tests

`./gradlew test` runs the shader unit tests, then renders the demo UIs offscreen on each backend with a controlled clock and compares them pixel by pixel to the references in `impl/<module>/src/test/snapshots`. `./gradlew crossBackendTest` also compares the backends to each other, and `./gradlew updateSnapshots` accepts an intended visual change. These tests need a GPU and run before each commit through the hook installed by `./gradlew installLocalGitHook`. See [Backends](documentation/content/ui/backends.md#tests).

## Features

- 🧱 **Node-based UI** — Hierarchical component system with layout nodes (flex, grid, scrollbar, container) and design nodes (shapes, text, images, text fields, sliders, charts, video…)
- 🎨 **MSDF font rendering** — Crisp text at any scale using Multi-channel Signed Distance Fields
- 🌈 **Shader pipeline** — Composable multi-pass GPU effects: blur, border, gradient, circle, rounded corners
- ✨ **Tween animations** — Full Universal Tween Engine integration (easing, paths, timelines, callbacks)
- 🎯 **Reactive signals** — Observable values with conditional watches that auto-reload nodes
- 💾 **Persistent stores** — `@UIStoreData`-annotated fields auto-serialized to JSON
- 🎬 **Video playback** — `VideoPlayerNode` with FFmpeg-backed decoding (MP4/MOV/WEBM/MKV/AVI/GIF/APNG)
- 🔌 **Bridge pattern** — Host- and engine-agnostic integration via `IUIBridge`, `IWindowBridge`, `IRenderBridge`, `IAudioBridge` and `IClockBridge`

## Documentation

<div align="center">

### 📖 [**joid.dev-zeldown.workers.dev**](https://joid.dev-zeldown.workers.dev/)

Full reference · Searchable (`Ctrl+K`) · English & French · Per-page PDF export

</div>

<sub>Offline: the site is self-contained in the `documentation/` folder. Serve it with any static HTTP server (`cd documentation && npx serve .` or `python -m http.server 3000`) — opening `index.html` via `file://` won't work because pages load through `fetch`.</sub>

## License

JOID is released under the **JOID Community Source License v1.0** — a source-available, copyleft, non-commercial-by-default license.

Full text: [LICENSE.md](LICENSE.md) (English, authoritative) · [LICENSE.fr.md](LICENSE.fr.md) (French, informational).

### What you get for free

- Use JOID in **any non-commercial project** — general gameplay UIs (HUD, settings, scoreboard, inventory, chat, minimap, launcher, login screen), internal tools, open-source projects, and Minecraft servers that accept donations without building a shop on top of JOID.
- Fork, modify and redistribute the source, as long as modifications stay under the same license.

### What you owe in return

- **Attribution** — include a [NOTICE](NOTICE) entry in your project (repo root, about screen, partners page, Discord, or equivalent) that links back to the JOID repository.
- **Share-alike** — if you distribute or publicly host a modified JOID, publish your modifications under the same license within 90 days.
- **No sublicensing, no reselling JOID itself, no white-label consulting that is primarily JOID repackaging.**

### Commercial use

Any UI whose **primary purpose** generates direct revenue (in-game shops, paid cosmetic selectors, donations tied to rewards, paid launcher features…) requires a written commercial agreement.

Request one via Discord DM to **`zeldown`** — see [COMMERCIAL.md](COMMERCIAL.md) for the procedure (typical response time: 24–48h). The public record of granted licenses is kept in [COMMERCIAL_GRANTS.md](COMMERCIAL_GRANTS.md).

> This summary is informational. The license text in [LICENSE.md](LICENSE.md) prevails in case of conflict.

## Credits

- [Universal Tween Engine](https://github.com/AurelienRibon/universal-tween-engine) by **Aurélien Ribon** — Tween animation engine
- [msdfgen](https://github.com/Chlumsky/msdfgen) by **Viktor Chlumský** — MSDF font atlases
- [LWJGL](https://www.lwjgl.org/) — OpenGL / Vulkan / OpenAL Java bindings used by the backends
- [JavaCV / FFmpeg](https://github.com/bytedeco/javacv) by **Bytedeco** — Video decoding
