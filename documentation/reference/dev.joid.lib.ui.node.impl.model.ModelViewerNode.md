---
summary: Draws a 3D model that the user turns by dragging and zooms with the wheel, within ranges.
---
A drag with the left button turns the model, one degree per 5 design pixels; one wheel notch zooms by `0.04D`.
The model moves smoothly toward the requested zoom and rotation.
A requested value outside its range is kept and applied again when the range changes.

Learn: [3D models](/use/learn/going-further/3d-models/#let-the-user-rotate-and-zoom).

## create(double,double,double,double)

Creates a model viewer at `x`, `y` with this width and height.

## ModelViewerNode(double,double,double,double)

Called by a subclass with the same arguments as `create`.

## zoom(double)

Sets the zoom of the model, kept within the zoom range.

## zoom(Signal)

Binds the zoom to a signal: the viewer follows it, and a wheel zoom writes back to it.

## rotationYaw

Rotation around the vertical axis, in degrees, kept within its range; a signal is bound both ways.

## rotationPitch

Rotation around the horizontal axis, in degrees, kept within its range; a signal is bound both ways.

## zoomRange(double,double)

Smallest and largest zoom.
Throws an `IllegalArgumentException` when the minimum is above the maximum.

## zoomRange(Supplier,Supplier)

Smallest and largest zoom, following the suppliers.

## rotationYawRange(double,double)

Smallest and largest rotation around the vertical axis, in degrees.
Throws an `IllegalArgumentException` when the minimum is above the maximum.

## rotationYawRange(Supplier,Supplier)

Range of the rotation around the vertical axis, following the suppliers.

## rotationPitchRange(double,double)

Smallest and largest rotation around the horizontal axis, in degrees.
Throws an `IllegalArgumentException` when the minimum is above the maximum.

## rotationPitchRange(Supplier,Supplier)

Range of the rotation around the horizontal axis, following the suppliers.

## zoomTo(double)

Zooms to this value as a change: the `beforeZoom` listeners can refuse it and `onZoom` fires.

## rotate(double,double)

Turns the model to these angles as a change: the `beforeRotate` listeners can refuse it and `onRotate` fires.

## beforeZoom(EventListener)

Adds a listener that runs before the zoom changes; cancelling the event refuses the change.

## onZoom(EventListener)

Adds a listener that runs after the zoom has changed.

## beforeRotate(EventListener)

Adds a listener that runs before the rotation changes; the value is a point of yaw and pitch.

## onRotate(EventListener)

Adds a listener that runs after the rotation has changed.

## getZoom()

The zoom the model moves to.

## getMinZoom()

The smallest zoom.

## getMaxZoom()

The largest zoom.

## getMinRotationYaw()

The smallest rotation around the vertical axis.

## getMaxRotationYaw()

The largest rotation around the vertical axis.

## getMinRotationPitch()

The smallest rotation around the horizontal axis.

## getMaxRotationPitch()

The largest rotation around the horizontal axis.

## getDrawnZoom()

The zoom drawn on this frame, moving toward the zoom.

## getDrawnRotationYaw()

The vertical-axis rotation drawn on this frame.

## getDrawnRotationPitch()

The horizontal-axis rotation drawn on this frame.

## init(UI)

Draws the model at its requested zoom and rotation from the first frame, without moving toward them.

## draw(double,double)

Moves the drawn zoom and rotation toward the requested ones, then draws the model.

## mousePressed(NodeMouseEvent)

Starts turning the model on a left press over the node and consumes the press.

## mouseReleased(NodeMouseEvent)

Stops turning the model when the left button is released.

## mouseDragged(NodeMouseEvent)

Turns the model with the mouse move.

## mouseScroll(NodeMouseScrollEvent)

Zooms on a wheel turn over the node and consumes it.

## detach()

Stops a turn in progress.
