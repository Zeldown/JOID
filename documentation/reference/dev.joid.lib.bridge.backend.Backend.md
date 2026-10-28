---
summary: The base class of every backend, which connects JOID to a window, a graphics API and an audio device in one `register()` call.
---

An application never extends `Backend`: it creates the backend of its library, sets its options, calls `register()` once the window and its graphics context exist, then `frame()` once per iteration of its loop ([Start a backend](/use/learn/running/backends/#start-a-backend)).

A backend author extends it and implements the `create...` hooks, which `register()` calls in a fixed order.

Learn: [Choosing a backend](/use/learn/running/backends/), [Writing a backend](/build/writing-a-backend/#extend-backend).

## Backend()

Creates the backend and reads its version from the `backend.properties` file next to its class, which `register()` compares with the loaded JOID.

## audio(boolean)

If `false`, `register()` registers no audio bridge and opens no audio device: videos play their picture without sound.

## dataDir(File)

Sets the folder where JOID writes the data of the application; it wins over the `joid.data` system property.

## cacheDir(File)

Sets the folder where JOID writes the caches it can rebuild; it wins over the `joid.cache` system property.

## register()

Connects the backend to JOID, loads JOID and runs the [[JOID#onReady]] listeners: [What register() does](/use/learn/running/backends/#what-register-does) lists every step.
Call it once, on the thread of your loop, with the graphics context current; a second call throws an `IllegalStateException`.

## frame()

Runs one frame of every UI bridge through [[UIBridgeRegistry#frame]]: resize when the window changed, update, clear and draw.
Call it once per iteration of your loop, between polling the events and showing the image.

## createWindowBridge()

JOID calls it during `register()`, after the audio bridge, and registers the result in `BridgeHandler.WINDOW`.
Return the window bridge of your backend.

## createRenderBridge()

JOID calls it during `register()`, after the window bridge, with the graphics context current, and registers the result in `BridgeHandler.RENDER`.
Return the render bridge of your backend.

## createAudioBridge()

JOID calls it first during `register()`, unless `audio(false)` was set.
Return the audio bridge, or `null` to register none; it returns `null` by default.

## createThreadBridge()

JOID calls it during `register()`, after the render bridge.
Return the bridge of your render thread, or `null` to keep the [[DirectThreadBridge]]; it returns a [[FrameThreadBridge]] tied to the calling thread by default.

## registerDefaults()

JOID calls it during `register()`, after the bridges.
It registers a [[WindowUIBridge]] with `registerDefault` by default; override it to add what your backend needs in other registries, and call `super.registerDefaults()` to keep the default UI bridge.

## attachInput()

JOID calls it during `register()`, after `registerDefaults()`, to connect the input of the window to `BridgeHandler.UI`; it does nothing by default.
A backend whose input belongs to the loop of the application leaves it empty and gives the application a forwarder to poll.

## getVersion()

Returns the version of JOID that the backend was built for, read from `backend.properties`.

## getDataDir()

Returns the folder set with `dataDir(File)`, or `null` when none was set.

## isAudio()

If `true`, `register()` registers the audio bridge of the backend.

## getCacheDir()

Returns the folder set with `cacheDir(File)`, or `null` when none was set.

## isRegistered()

If `true`, `register()` has run.
