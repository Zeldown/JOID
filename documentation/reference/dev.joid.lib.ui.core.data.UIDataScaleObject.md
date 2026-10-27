---
summary: The interface-scale options of @UIDataScale for a running UI.
---
[[UI#getScale]] returns it; a change applies from the next frame.

Learn: [The design canvas](/use/learn/describing-the-ui/design-canvas/#follow-the-interface-scale).

## create()

Creates the options with the default values of [[UIDataScale]].

## apply(double)

Returns the interface scale that the UI uses for this scale of the bridge: `1` when the option is off, at most `limit` when it is limited.

## isActive()

If `false`, the UI ignores the interface scale.

## setActive(boolean)

Lets the UI follow the interface scale or not.

## isLimited()

If `true`, the interface scale never goes above `limit`.

## setLimited(boolean)

Caps the interface scale at `limit` or not.

## getLimit()

Largest interface scale applied when the scale is limited.

## setLimit(double)

Sets the largest interface scale applied when the scale is limited.
