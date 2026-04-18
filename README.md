<div align="center">

# JOID
## Java OpenGL Interface Developement

<div align="center">
  <img align="center" src="https://img.shields.io/badge/version-5.1.0 (3806526)-blue">
  <img align="center" src="https://img.shields.io/badge/maintainer-Zeldown-orange">
  <img align="center" src="https://img.shields.io/maintenance/yes/9999">
  <img align="center" src="https://github.com/Zeldown/JOID/actions/workflows/push.yml/badge.svg">
</div>

<br>

Welcome to JOID, a powerful and flexible user interface toolkit designed for developer community.
<br><br>
Design your project uniquely by making the theme that best fits your preferences, as there is no default theme to limit your creativity.
<br>
Stand out in the global landscape with a personalized design that reflects your vision.
<br><br>


Create the ideal interface with a wide range of customizable components and impressive animations, allowing you to showcase your talent effortlessly.
<br><br>

[Installation](#installation)
[Features](#features)
[Credits](#credits)

</div>

## Installation

JOID is distributed via GitHub Releases as two artifacts:

- **joid-X.Y.Z-prod.jar** — production build (excludes dev/test assets)
- **joid-X.Y.Z-dev.jar** — dev build (includes demo assets, fonts, test textures)

Download the desired artifact from the [Releases page](https://github.com/Zeldown/JOID/releases) and add it to your project's classpath.

### Gradle

```groovy
dependencies {
    compile files('libs/joid-6.0.0-prod.jar')
}
```

### Maven

```xml
<dependency>
    <groupId>be.zeldown.joid</groupId>
    <artifactId>joid</artifactId>
    <version>6.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/joid-6.0.0-prod.jar</systemPath>
</dependency>
```

### Native libraries

The repository contains a `native/` folder with the required OpenGL and OpenAL native libraries. Make sure they are exposed to the JVM via `-Djava.library.path=./native` at launch.

## Features

- 🧱 **Node-based UI** — Hierarchical component system with layout nodes (flex, grid, scrollbar, container) and design nodes (shapes, text, images, text fields, sliders, charts, video…)
- 🎨 **MSDF font rendering** — Crisp text at any scale using Multi-channel Signed Distance Fields
- 🌈 **Shader pipeline** — Composable multi-pass GL effects: blur, border, gradient, circle, rounded corners
- ✨ **Tween animations** — Full Universal Tween Engine integration (easing, paths, timelines, callbacks)
- 🎯 **Reactive signals** — Observable values with conditional watches that auto-reload nodes
- 💾 **Persistent stores** — `@UIStoreData`-annotated fields auto-serialized to JSON
- 🎬 **Video playback** — `VideoPlayerNode` with FFmpeg-backed decoding (MP4/MOV/WEBM/MKV/AVI/GIF/APNG)
- 🔌 **Bridge pattern** — Host-agnostic integration via `IUIBridge`

## Credits

- [Universal Tween Engine](https://github.com/AurelienRibon/universal-tween-engine) by **Aurélien Ribon** — Tween animation engine
- [msdfgen](https://github.com/Chlumsky/msdfgen) by **Viktor Chlumský** — MSDF font atlases
- [LWJGL 2.9](https://www.lwjgl.org/) — OpenGL / OpenAL Java bindings
- [JavaCV / FFmpeg](https://github.com/bytedeco/javacv) by **Bytedeco** — Video decoding
