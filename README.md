<div align="center">

# JOID
## Java Open Interface Development

<div align="center">
  <img align="center" src="https://img.shields.io/badge/version-7.0.1 (e3dd96b)-blue">
  <img align="center" src="https://img.shields.io/badge/maintainer-Zeldown-orange">
  <img align="center" src="https://img.shields.io/maintenance/yes/9999">
  <img align="center" src="https://img.shields.io/badge/license-Apache--2.0-blue">
  <img align="center" src="https://github.com/Zeldown/JOID/actions/workflows/release.yml/badge.svg">
</div>

<br>

**Build GPU-composed UIs in pure Java — no CSS, no XML, no runtime parser.**
<br><br>
Welcome to JOID, a flexible component-based UI toolkit for any Java application that owns its rendering path — OpenGL, Vulkan, or any engine you bridge it to — made for developers who want to ship interfaces that stand out. No default theme, no stylesheet dialect to fight — the code you write is the layout the GPU draws.
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

Every release also ships two tools: **joid-msdf-generator-X.Y.Z.zip**, the font atlas generator with its launch scripts for Windows, macOS and Linux — see [MSDF Atlas](documentation/content/fonts/msdf-atlas.md) — and **joid-backend-template-X.Y.Z.zip**, a starting point to write a backend in your own repository.

Download the desired artifact from the [Releases page](https://github.com/Zeldown/JOID/releases) and add it to your project's classpath. To build them from source, `./gradlew build` (with `-Pdev` for the dev flavour) copies every release artifact into `build/libs`. The jars only contain JOID code: add Guava 15.0, Gson 2.2.4, commons-lang3 3.1, commons-compress 1.8.1, commons-io 2.4 and vecmath 1.3.1 to your project, along with the libraries of your backend listed in [Installation](documentation/content/getting-started/installation.md).

### Gradle

```groovy
dependencies {
    compile files('libs/joid-lwjgl2-7.0.1-prod.jar')
}
```

### Maven

```xml
<dependency>
    <groupId>dev.joid</groupId>
    <artifactId>joid</artifactId>
    <version>7.0.1</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/joid-lwjgl2-7.0.1-prod.jar</systemPath>
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
| `testkit` | Snapshot test framework shared by the backends | — |
| `msdf` | Font atlas generator, `.ttf` or `.otf` to `font.msdf` | `MsdfGenerator` |

Entry points live in the `dev.joid.impl.<module>` package of each backend. Backends only implement the bridges — they never modify the `core` module. See [Backends](documentation/content/ui/backends.md) to write a new one, in this repository or in your own from the `joid-backend-template` of each release.

## Tests

`./gradlew test` runs the shader unit tests, then renders the demo UIs offscreen on each backend with a controlled clock and compares them pixel by pixel to references recorded per machine in `.snapshots`. `./gradlew crossBackendTest` also compares the backends to each other within one level per channel, and `./gradlew updateSnapshots` accepts an intended visual change. Each run writes an interactive `report.html` to `build/snapshots` to inspect every difference down to the pixel. These tests need a GPU and run on the staged changes before each commit and on the pushed commits before each push, through the hooks installed by `./gradlew installLocalGitHook`. See [Backends](documentation/content/ui/backends.md#tests).

## Features

- 🧱 **Node-based UI** — Hierarchical component system with layout nodes (flex, grid, scrollbar, container) and design nodes (shapes, text, images, text fields, sliders, charts, video…)
- 🎨 **MSDF font rendering** — Crisp text at any scale using Multi-channel Signed Distance Fields, with kerning, from atlases JOID generates itself
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

JOID is released under the **Apache License 2.0** — see [LICENSE](LICENSE).

Use it in anything: commercial or not, open or closed, forked or embedded. Nothing to request, no revenue share, no non-commercial boundary.

When you redistribute JOID, modified or not, the license asks you to:

- ship a copy of the license and keep the copyright, patent, trademark and attribution notices;
- state the files you changed;
- pass the [NOTICE](NOTICE) content on with your distribution.

It also grants you the patents of every contributor, and reserves the **JOID** name as a trademark: give your fork another name. Section 4 of the [LICENSE](LICENSE) has the exact wording.

## Credits

- [Universal Tween Engine](https://github.com/AurelienRibon/universal-tween-engine) by **Aurélien Ribon** — Tween animation engine (Apache-2.0, bundled in `lib/animation/tweenengine`)
- [msdfgen](https://github.com/Chlumsky/msdfgen) by **Viktor Chlumský** — the multi-channel signed distance field algorithm, which JOID reimplements in Java in the `msdf/` module
- [LWJGL](https://www.lwjgl.org/) — OpenGL / Vulkan / OpenAL Java bindings used by the backends (BSD-3-Clause)
- [JavaCV / FFmpeg](https://github.com/bytedeco/javacv) by **Bytedeco** — Video decoding (Apache-2.0; the FFmpeg builds carry their own terms)
