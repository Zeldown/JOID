---
summary: The dev-mode options of `@UIDataDebug` for a running UI.
---
[[UI#getDebug]] returns it.
Each option has a getter and a setter of the same name, such as `hotreload()` and `hotreload(boolean)`.

Learn: [UIs](/use/learn/describing-the-ui/uis/#configure-a-ui).

## create()

Creates the options with the default values of [[UIDataDebug]].

## profiler

If `true`, dev mode prints the load time of the UI and warns about slow frames.

## hotreload

If `true`, dev mode reloads the UI when its classes change.

## toString()

Returns the class and the value of every option, for logs.
