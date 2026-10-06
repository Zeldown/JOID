# JOID backend template

A starting point to run [JOID](https://github.com/Zeldown/JOID) @JOID_VERSION@ on an engine of your choice, in your own repository, with only the JOID jars.

A backend implements three bridges — `IRenderBridge`, `IWindowBridge` and `IAudioBridge` — and never modifies the JOID core. The template compiles as is: every method throws `UnsupportedOperationException` until you implement it.

## Setup

1. Put the JOID jars listed in [`libs/README.md`](libs/README.md) in `libs/`.
2. Rename the `com.example.joid.engine` package, `group` and `archivesBaseName` in `build.gradle`, and `rootProject.name` in `settings.gradle`.
3. Add the libraries of your engine to the `compile` dependencies. When your engine runs on GLFW or OpenAL, add `joid-glfw` and `joid-openal` to the `embed` configuration instead of writing those bridges:

```groovy
dependencies {
    embed files("libs/joid-glfw-${joidVersion}.jar", "libs/joid-openal-${joidVersion}.jar")
}
```

The libraries of the JOID core are declared in the `libraries` configuration, and JavaCV and FFmpeg, needed by video playback, in the `video` configuration. No jar embeds them: the application that uses your backend declares them too.

## Implementing the backend

| Class | Role |
|---|---|
| `Backend` | Registers the bridges. It calls `JOID.checkVersion`, which warns when the loaded JOID has another major version than the one the backend targets. |
| `render/RenderBridge` | Extends the core `RenderBridge`, which tracks matrices and state in Java. Implement `clear`, `clearStencil`, `draw`, `createTexture`, `createFrameBuffer` and `createShader`, applying the current state when they run. |
| `window/WindowBridge` | Window size, mouse, keyboard and clipboard. |
| `audio/AudioBridge` | Streaming audio sources used by the video player. |
| `demo/DemoWindow` | Opens the JOID demo UIs on your engine. |
| `SnapshotBackend` (tests) | Creates an offscreen surface, runs a frame, captures its pixels and names the renderer. |

The render bridge contract — vertex layout, projection conventions, textures, framebuffers, state stack, shaders — is described in [Writing a Backend](https://joid.dev-zeldown.workers.dev/#/integration/writing-a-backend).

## Tasks

| Command | Result |
|---|---|
| `./gradlew test` | Runs `RenderBridgeContractTest`, which checks the render bridge contract in a few seconds, and `SnapshotTest`, which renders every JOID scenario and compares each shot pixel for pixel to the references of the machine, recorded in `.snapshots/references` on the first run. |
| `./gradlew updateSnapshots` | Replaces the references after an intended visual change. |
| `./gradlew renderBaseline` | Renders the same scenarios with the official LWJGL 3 backend into `build/snapshots/lwjgl3`. |
| `./gradlew crossBackendTest` | Runs the tests and the baseline, then compares your shots to the LWJGL 3 ones within one level per channel. |
| `./gradlew build` | Builds `joid-engine-1.0.0-dev.jar`, with the demo assets, and `joid-engine-1.0.0-prod.jar`, without them. Both embed the JOID core and the `embed` jars, and no third-party library. |
| `./gradlew testDevJar testProdJar` | Runs the tests against the packaged jars instead of the classes: every test for the dev jar, the contract and unit tests for the prod jar. |
| `./gradlew runDemo` | Launches `DemoWindow`. |

Every run writes an interactive `report.html` next to the renders, and its link is printed at the end of the build when a test fails.

## License

JOID is licensed under the [Apache License 2.0](https://github.com/Zeldown/JOID/blob/main/LICENSE), and so is this template. Your backend stays yours: license it as you want, as long as you keep the JOID notices in what you redistribute.
