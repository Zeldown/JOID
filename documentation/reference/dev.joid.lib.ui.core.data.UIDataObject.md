---
summary: The options of @UIData for a running UI, read from the annotation and changed by its setters.
---
[[UI#getData]] returns it.
Each option has a getter and a setter of the same name, such as `closeable()` and `closeable(boolean)`, and the setter returns the object for chaining; [[UIData]] gives the meaning of each option.
A change applies from the next frame; `reload()` reads the annotation again and keeps the options that the annotation did not change.

Learn: [UIs](/use/learn/describing-the-ui/uis/#configure-a-ui).

## create()

Creates the options with the default values of [[UIData]].

## active

If `false`, the UI is drawn but receives no input.

## visible

If `false`, the UI is neither drawn nor sent any input.

## closeable

If `false`, Escape leaves the UI open.

## zoomable

If `false`, the zoom keys of dev mode leave the zoom of the UI unchanged.

## projection

If `true`, the UI sets its own projection over the window.

## background

If `true`, the UI draws the UI backdrop behind its nodes.

## backgroundColor

Color of the UI backdrop, as a hexadecimal string `#RRGGBB` or `#RRGGBBAA`.

## getBackgroundColor()

Returns the color of the UI backdrop as a [[Color]], decoded once when `backgroundColor` changes.

## zindex

Order of the UI among the open UIs of its bridge.

## zlevel

Depth added along the Z axis before the UI is drawn.

## anchorX

Side of the window that the canvas is pinned to horizontally.

## anchorY

Side of the window that the canvas is pinned to vertically.

## getAnchorPositionX()

The horizontal anchor as a position on the canvas: `0`, `960` or `1920` design pixels.

## getAnchorPositionY()

The vertical anchor as a position on the canvas: `0`, `540` or `1080` design pixels.

## toString()

Returns the class and the value of every option, for logs.
