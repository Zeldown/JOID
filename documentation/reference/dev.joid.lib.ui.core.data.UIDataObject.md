---
summary: The options of @UIData for a running UI, read from the annotation and changed by its setters.
---
[[UI#getData]] returns it.
Each option has a getter and a setter of the same name, such as `closeable()` and `closeable(boolean)`, and the setter returns the object for chaining; [[UIData]] gives the meaning of each option.
A change applies from the next frame; `reload()` reads the annotation again and keeps the options that the annotation did not change.

Learn: [UIs](/use/learn/describing-the-ui/uis/#configure-a-ui).

## create()

Creates the options with the default values of [[UIData]].

## isActive()

If `false`, the UI is drawn but receives no input.

## setActive(boolean)

Lets the UI receive the input or not.

## isVisible()

If `false`, the UI is neither drawn nor sent any input.

## setVisible(boolean)

Shows or hides the UI.

## isCloseable()

If `false`, Escape leaves the UI open.

## setCloseable(boolean)

Lets Escape close the UI or not.

## isZoomable()

If `false`, the zoom keys leave the zoom of the UI unchanged.

## setZoomable(boolean)

Turns the zoom keys on or off for the UI.

## isProjection()

If `true`, the UI sets its own projection over the window.

## setProjection(boolean)

Lets the UI set its own projection, or draw inside the projection of the host.

## isBackground()

If `true`, the UI draws the UI backdrop behind its nodes.

## setBackground(boolean)

Turns the UI backdrop on or off.

## backgroundColor

Color of the UI backdrop: the getter returns it as a [[Color]], the setter takes a hexadecimal string.

## setBackgroundColor(String)

Sets the color of the UI backdrop from a hexadecimal string `#RRGGBB` or `#RRGGBBAA`.

## getZindex()

Order of the UI among the open UIs of its bridge.

## setZindex(int)

Moves the UI among the open UIs of its bridge.

## getZlevel()

Depth added along the Z axis before the UI is drawn.

## setZlevel(double)

Sets the depth added before the UI is drawn.

## getAnchorX()

Side of the window that the canvas is pinned to horizontally.

## setAnchorX(Align)

Pins the canvas to another side horizontally, from the next frame.

## getAnchorY()

Side of the window that the canvas is pinned to vertically.

## setAnchorY(Align)

Pins the canvas to another side vertically, from the next frame.

## getAnchorPositionX()

The horizontal anchor as a position on the canvas: `0`, `960` or `1920` design pixels.

## getAnchorPositionY()

The vertical anchor as a position on the canvas: `0`, `540` or `1080` design pixels.
