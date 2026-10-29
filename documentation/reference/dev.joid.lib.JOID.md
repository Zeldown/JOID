---
summary: Entry point that opens and closes UIs, finds the open ones and runs code once JOID has loaded.
---
`JOID` holds static methods only, apart from `inst().load()` that the backend calls.
`open`, `close`, `forceOpen` and `forceClose` can be called from any thread: off the render thread, JOID runs them at the next frame.

Learn: [UIs](/use/learn/describing-the-ui/uis/), [Backends](/use/learn/running/backends/#run-code-once-joid-is-ready).

## VERSION

Version of the JOID core on the classpath, read from its jar; `development` when the classes do not come from a released jar.

## inst()

The single instance of `JOID`.

## load()

Loads JOID once: prints the version, the mode and the data and cache folders, cleans the cache in the background, loads the fonts of the dev tools in dev mode, installs the hook that closes the open UIs and saves the stores when the JVM stops, then runs the `onReady` listeners.
[[Backend]] calls it at the end of `register()`; a second call does nothing.

## isReady()

If `true`, `load()` has finished; another thread sees it at once.

## onReady(Runnable)

Runs the listener on the render thread once JOID has loaded; when it has already loaded, at once on the render thread, or at the start of the next frame from another thread.
A listener given before JOID has loaded that throws prints a warning and does not stop the next ones; once JOID has loaded, the exception reaches a caller on the render thread, and a call from another thread prints the warning of a failed render-thread task.

## checkVersion(String)

Throws an `IllegalStateException` when this version has another major number than the loaded core.
A backend calls it with the version it was built for; a `development` version on either side always passes.

## readVersion(Class,String)

Reads the `version` property of a properties file found next to this class, or `development` when the file or the property is missing.

## isOpen(UI)

If `true`, this UI is open in a UI bridge.

## isOpen(Class)

If `true`, a UI of this class (or of a subclass) is open in any UI bridge.

## getUi(Class)

The first open UI of this class or of a subclass, searched in every UI bridge from the highest priority down, or `null` when none is open.

## open(UI)

Opens the UI in the UI bridge that already holds it, otherwise in the bridge of highest priority whose `canHandle` accepts it, and returns that bridge.
A screen replaces the open screens, a popup or an overlay opens above them, and a UI already open moves to the top.
Throws an `IllegalStateException` when no registered bridge accepts the UI.

## close(UI)

Closes the UI: the `beforeClose` listeners can refuse, then the closing transition plays.
A UI that is not open is only removed from its bridge.

## forceOpen(UI)

Closes every UI of the bridge that accepts this one, without asking them and without transition, then opens it; returns that bridge.
Throws an `IllegalStateException` when no registered bridge accepts the UI.

## forceClose(UI)

Closes the UI at once, without asking the `beforeClose` listeners and without its closing transition.
