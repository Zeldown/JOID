---
summary: Converts between the screen pixels of the window and the design pixels of a UI, from the window size, the anchor, the interface scale and the zoom.
---
Every [[UI]] has one view, returned by [[UI#getView]].
The view fits the 1920 × 1080 design canvas in the window without cropping it, then applies the interface scale and the zoom around the anchor.
Use it to place your own drawing in screen pixels or to read where the window edges fall on the canvas.

Learn: [The design canvas](/use/learn/describing-the-ui/design-canvas/).

## DESIGN_WIDTH

Width of the design canvas, in design pixels.

## DESIGN_HEIGHT

Height of the design canvas, in design pixels.

## create(double,double)

Creates a view whose anchor is this point of the design canvas, for a 1920 × 1080 window, with no zoom and no interface scale.
A UI creates its own view from its anchors.

## anchorX

Horizontal position, in design pixels, of the anchor that the canvas zooms around and stays pinned to when the window shows more than the canvas.

## anchorY

Vertical position, in design pixels, of the anchor that the canvas zooms around and stays pinned to.

## zoom

Zoom applied on top of the interface scale.
The view keeps the value asked for, even beyond the bounds, and `getZoom()` returns it kept within `clampZoom`; unlike [[UI#zoom(double)]], writing it here runs no listener.

## getRequestedZoom()

Returns the zoom asked for, before the bounds of `clampZoom`, by `UI.zoom(...)` or by `zoom(...)` of the view.
A value written to the view that equals the zoom shown keeps a request of `UI.zoom(...)` for the next change of interface scale, while `getRequestedZoom()` returns the value written.

## interfaceScale

Factor that the UI bridge applies to make every UI larger or smaller, after the `@UIDataScale` of the UI.

## getWidth()

Width of the window, in screen pixels.

## getHeight()

Height of the window, in screen pixels.

## getViewportWidth()

Width of the window measured in design pixels at a scale of 1: at least 1920, larger when the window is wider than 16:9.

## getViewportHeight()

Height of the window measured in design pixels at a scale of 1: at least 1080, larger when the window is taller than 16:9.

## getScale()

The interface scale multiplied by the zoom.

## getOffsetX()

Horizontal shift of the canvas inside the viewport, in design pixels, that keeps it pinned to its anchor when the window is wider than the canvas.

## getOffsetY()

Vertical shift of the canvas inside the viewport, in design pixels, that keeps it pinned to its anchor when the window is taller than the canvas.

## getVisibleWidth()

Width of the window measured in design pixels, after the scale.

## getVisibleHeight()

Height of the window measured in design pixels, after the scale.

## toUiX(double)

Converts a horizontal position of the window, in screen pixels, to the design canvas.

## toUiY(double)

Converts a vertical position of the window, in screen pixels, to the design canvas.

## toScreenX(double)

Converts a horizontal position of the design canvas to the window, in screen pixels.

## toScreenY(double)

Converts a vertical position of the design canvas to the window, in screen pixels.

## toScreenWidth(double)

Converts a width in design pixels to screen pixels.

## toScreenHeight(double)

Converts a height in design pixels to screen pixels.

## clampZoom(double)

Returns the zoom kept between `0.1` and the largest zoom that the interface scale allows: `1`, or `1 / interfaceScale` when the scale is below 1.

## resize(double,double)

Fits the view to a window of this size, in screen pixels.
A size of `0` or below, such as a minimized window, is ignored.

## render(IRenderBridge,boolean,Runnable)

Runs the drawing in the coordinates of the design canvas.
If `projection` is `true`, it also sets an orthographic projection on the viewport; if `false`, it keeps the projection of the caller and applies only the offset, the scale and the zoom.
