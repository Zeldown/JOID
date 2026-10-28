---
summary: The interface-scale options of `@UIDataScale` for a running UI.
---
[[UI#getScale]] returns it; a change applies from the next frame.

Learn: [The design canvas](/use/learn/describing-the-ui/design-canvas/#follow-the-interface-scale).

## create()

Creates the options with the default values of [[UIDataScale]].

## apply(double)

Returns the interface scale that the UI uses for this scale of the bridge: `1` when the option is off, at most `limit` when it is limited.

## active

If `false`, the UI ignores the interface scale.

## limited

If `true`, the interface scale never goes above `limit`.

## limit

Largest interface scale applied when the scale is limited.

## toString()

Returns the class and the value of every option, for logs.
