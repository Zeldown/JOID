---
summary: The dev-mode options of @UIDataDebug for a running UI.
---
[[UI#getDebug]] returns it.
Each option has a getter and a setter of the same name, such as `hotreload()` and `hotreload(boolean)`.

Learn: [UIs](/use/learn/describing-the-ui/uis/#configure-a-ui).

## create()

Creates the options with the default values of [[UIDataDebug]].

## isProfiler()

If `true`, dev mode prints the load time of the UI and warns about slow frames.

## setProfiler(boolean)

Turns the profiler of the UI on or off.

## isHotreload()

If `true`, dev mode reloads the UI when its classes change.

## setHotreload(boolean)

Turns hot reload on or off for the UI, from the next frame.
