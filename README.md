<div align="center">

# JOID
## Java OpenGL Interface Developement

<div align="center">
  <img align="center" src="https://img.shields.io/badge/version-6.0.0 (9d897a3)-blue">
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
[Documentation](#documentation)
[License](#license)
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

## Documentation

The full reference lives in the `documentation/` folder — a static single-page app that loads its Markdown pages through `fetch`. You need to serve it through a local HTTP server; opening `index.html` directly via `file://` will break every page load.

### Prerequisites

Pick either Node.js (for `npx serve`) or Python 3 (for `http.server`). You only need one.

**Node.js (for `npx serve`)** — `npx` ships with Node.js. To check if you have it:

```bash
node -v
npx -v
```

If either command isn't found, install Node.js:

- **Windows** — download the LTS installer from <https://nodejs.org/> and run it. Or via winget: `winget install OpenJS.NodeJS.LTS`
- **macOS** — via [Homebrew](https://brew.sh/): `brew install node` — or download the `.pkg` installer from <https://nodejs.org/>.
- **Linux** — `sudo apt install nodejs npm` (Debian/Ubuntu), `sudo dnf install nodejs` (Fedora), `sudo pacman -S nodejs npm` (Arch), or use [nvm](https://github.com/nvm-sh/nvm) to manage versions.

`npx serve .` then works out of the box — `npx` auto-downloads the `serve` package on first use. If you prefer a permanent install, run `npm install -g serve` once and call `serve .` directly afterwards.

**Python 3 (alternative)** — already present on most macOS/Linux systems; on Windows install it from <https://python.org/> or via winget: `winget install Python.Python.3.12`. Check with `python3 --version` (or `python --version` on Windows).

### Windows

Double-click `documentation\run.bat` — it runs `npx serve .` inside the folder. Equivalent manual commands:

```powershell
cd documentation
npx serve .
# or, if Python is installed:
python -m http.server 3000
```

### macOS

```bash
cd documentation
npx serve .
# or, if Python is installed:
python3 -m http.server 3000
```

### Linux

```bash
cd documentation
npx serve .
# or, if Python is installed:
python3 -m http.server 3000
```

Each command prints the local URL it's serving (usually <http://localhost:3000> for `serve`, <http://localhost:8000> for Python). Open it in your browser — the language toggle, Ctrl+K search, and Download-as-PDF button are all available from the UI.

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
- [LWJGL 2.9](https://www.lwjgl.org/) — OpenGL / OpenAL Java bindings
- [JavaCV / FFmpeg](https://github.com/bytedeco/javacv) by **Bytedeco** — Video decoding
