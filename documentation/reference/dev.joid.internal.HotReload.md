---
summary: Watches the class files of open UIs in dev mode and reloads a UI when its classes change.
---
An open UI is watched in dev mode unless its `@UIDataDebug` turns `hotreload` off.
JOID watches the folder or the jar file that the classes of the UI come from, every 500 ms, and reloads the UI on the render thread one second after the last change.

Learn: [Dev tools](/use/get-started/dev-tools/).

## watch(UI,Runnable)

Starts watching the classes of the UI and runs `reload` when they change.
When the classes come neither from a folder nor from a jar file, it prints a warning and watches nothing.
A UI calls it when it opens in dev mode.

## unwatch(UI)

Stops watching the classes of the UI; the watcher of a folder stops with its last UI.
