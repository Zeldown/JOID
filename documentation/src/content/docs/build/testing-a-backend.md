---
title: "Testing a backend"
description: "Prove a backend correct: the contract suites, the snapshot suite and its references, and the comparison with LWJGL 3."
genre: build
sidebar:
  order: 1
---

A backend is correct when it draws the same pixels as the others.
The testkit (`joid-tool-testkit`) proves it at three levels: contract suites check one rule each in a few seconds, the snapshot suite renders every scenario of the demos against references, and the cross-backend comparison holds every backend to the LWJGL 3 rendering.
The user side, snapshots of your own screens, is on [Testing your UIs](/use/learn/running/testing/).

## Give the testkit a surface

Every suite drives your backend through an [[ISnapshotBackend]]: a hidden surface on which your backend is registered.

```java file="com/example/backend/EngineSnapshotBackend.java" region="snapshot"
```

| Member | What it does |
|---|---|
| [[ISnapshotBackend#create]] | Creates a hidden surface of that size and registers your backend on it, with `audio(false)`: 1920×1080 for the snapshot suite, a smaller size for each contract suite. |
| [[ISnapshotBackend#capture]] | Reads the pixels of the last frame, before `present()`: the area of that size at the bottom-left corner of the surface. |
| [[ISnapshotBackend#present]] | Ends the frame on the surface, a buffer swap or a submission. |
| [[ISnapshotBackend#getRenderer]] | Names the GPU and the driver; the references are stored per renderer. |
| [[ISnapshotBackend#destroy]] | Releases the surface. |

[[SnapshotImage#from]] builds an image from RGBA or BGRA bytes ([[PixelLayout]]), top-down or bottom-up.
A [[SnapshotBridgeRestorer]], created before the backend registers, unregisters, when your `destroy()` calls its `restore()`, every bridge registered since then, so that the next suite starts clean.
The official snapshot backends ([[Lwjgl2SnapshotBackend]], [[Lwjgl3SnapshotBackend]], [[VulkanSnapshotBackend]]) live in the `snapshot` package of each backend.

## The suites

Each suite is an abstract JUnit 4 class with one hook, `createBackend()`.
A test class of your backend extends it in a few lines:

```java file="com/example/backend/EngineRenderBridgeContractTest.java" region="contract"
```

| Suite | Checks | Needs |
|---|---|---|
| [[RenderBridgeContractSuite]] | The [render contract](/build/bridges/render/#guarantees): frames, vertex colors, every uniform type, samplers, lighting, culling, stencil, alpha test, lines, ARGB uploads, mipmaps, transparent borders, state stack, framebuffers, raster, depth, current color, OpenGL projection, exact translations and pixel quantization. | an [[ISnapshotBackend]] |
| [[BitmapFontContractSuite]] | Bitmap text: whole texels at a whole number of pixels, the same ink in every texel column at a fractional scale, the same glyph from every atlas. | an [[ISnapshotBackend]] |
| [[BorrowedTextureContractSuite]] | Textures lent by a host ([Resource resolvers](/build/resource-resolvers/#guarantees)). | an [[IBorrowedTextureBackend]]: borrow, create and inspect a texture of the host |
| [[StateGuardContractSuite]] | A JOID frame leaves the state of the host untouched, one [[StateTrap]] at a time ([Embedding in a host](/build/embedding-in-a-host/#check-the-guard)). | an [[IStateGuardBackend]]: inject a trap, draw like the host, read the state |
| [[SnapshotSuite]] | Every scenario (`dev`, `popup`, `static`, `window`, `resource`, `transition`, `interaction`), pixel for pixel against the references of the machine. | an [[ISnapshotBackend]], and the demo classes on the classpath |

The official backends run all five.
A trap that a context cannot set skips its test instead of failing: a backend without global state, such as Vulkan, answers `false` from `supports` for every trap and still runs the raster tests of the state guard suite.
[[ContractDraw]] holds the helpers of the suites (a frame around a drawing, quads, a shader from a fragment stage, a pixel assertion) for the contract tests of your own.

## References and the report

The snapshot suite renders each shot and compares it with the reference of the same name, for the same renderer, with no tolerance: one different pixel fails the shot.

| System property | Default | Holds |
|---|---|---|
| `joid.snapshot.references` | `.snapshots/references` | The references, one folder per renderer. |
| `joid.snapshot.output` | `build/snapshots/renders` | The renders of the last run, `renderer.txt` and `report.html`. |
| `joid.snapshot.update` | `false` | When `true`, every render replaces its reference. |
| `joid.snapshot.record` | `false` | When `true`, a shot without a reference becomes its reference, and the others are compared. |

A shot without a reference for the current GPU fails its scenario with a message that names the GPU and the GPUs that have references: record them with `updateSnapshots`.
References are not shared between machines: each machine and each renderer records its own, and the comparison with LWJGL 3 is what holds backends together.
A reference whose shot no scenario takes any more is deleted at the end of the run.

`report.html` shows every shot next to its reference, with filters on the status and views that compare them (swipe, onion skin, blink, difference, highlight).
A failed run prints its link.

## Compare with LWJGL 3

[[SnapshotComparison]] compares the renders of several backends with those of a reference backend, one level per channel at most, and writes its own report.
Small differences of rasterization between drivers stay within that level; a wrong blend, an inverted axis or a missing mip level does not.
A render missing from a candidate fails too, so a scenario that crashes on your backend does not pass silently.
When the two `renderer.txt` name different GPUs, the comparison warns that the differences may come from the GPUs, then compares the pixels as usual.

[[SnapshotBaseline]] renders the scenarios with a given snapshot backend into a folder: the template runs it with [[Lwjgl3SnapshotBackend]] to produce the reference renders.

## Run the tests of your backend

| Command in the template | Does |
|---|---|
| `./gradlew test` | Runs the test classes of the template (the render and bitmap font contract suites and the snapshot suite) and the ones you add, such as the borrowed texture and state guard suites; references in `.snapshots/references`, renders in `build/snapshots/<archivesBaseName>`. |
| `./gradlew updateSnapshots` | Runs the tests and replaces the references, the first time and after an intended change of your rendering. |
| `./gradlew test -PrecordSnapshots` | Runs the tests and records only the missing references. |
| `./gradlew renderBaseline` | Renders the scenarios with the LWJGL 3 backend into `build/snapshots/lwjgl3`. |
| `./gradlew crossBackendTest` | Runs the tests and the baseline, then compares your renders with those of LWJGL 3. |
| `./gradlew testDemoJar testDevJar testProdJar` | Runs the tests against your packaged jars: every test for the demo jar, the contract and unit tests for the dev and prod jars, which hold no demo. |

Each test class runs in its own JVM, so a backend registered by one class does not leak into the next, and each task writes its data and its cache under `build/`.

## Force the dev mode in a test

The dev and demo modes follow the classes on the classpath ([Demos](/build/demos/#modes)).
A test that needs the dev mode on or off sets it with [[JOIDEnv#devMode]] ([Turn the dev tools on](/use/get-started/dev-tools/#turn-the-dev-tools-on)) and restores it afterwards.
The snapshot runner turns the dev mode off for every scenario, except where a scenario turns it on.

Every command of a scenario is listed on the [[SnapshotRunner]] reference page.
