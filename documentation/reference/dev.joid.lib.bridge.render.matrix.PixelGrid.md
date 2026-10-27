---
summary: Maps design pixels onto the screen pixels of the current projection, model-view and viewport, and snaps edges, sizes and motions onto whole pixels.
---

A `PixelGrid` is a snapshot of how the current matrices place a design pixel on the target.
`getPixelGrid()` of the render bridge computes it on each call; the drawing code, the resources and the shader passes read it to put every edge on a pixel boundary, so a line of one pixel stays one sharp pixel at any interface scale.

Screen coordinates are pixels of the viewport with the origin at the bottom-left corner, as in OpenGL: under the projection of a UI, the screen Y grows upward while the design Y grows downward.
Snapping happens only on an aligned grid: under a rotation other than a half turn, a shear or a perspective, every `snap`, `span` and `quantize` method returns its input unchanged; a mirror or a half turn stays aligned.

Learn: [Pixel alignment](/use/in-depth/pixel-alignment/), [Render pipeline](/build/pipeline/#matrices-and-coordinates).

## of(float[],float[],int,int)

Computes the grid of a column-major projection and model-view and of a viewport size in pixels.

## snapX(double)

Returns the design X of the screen pixel boundary closest to `x`.

## snapY(double)

Returns the design Y of the screen pixel boundary closest to `y`.

## snapRight(double,double)

Returns the snapped far edge of a span from `left` to `right`.
When both edges would snap to the same boundary, the far edge moves one pixel away, so a shape that is not empty never vanishes.

## snapBottom(double,double)

Returns the snapped far edge of a span from `top` to `bottom`, one pixel away from the near edge when both would snap together.

## snapWidth(double,double)

Returns the far edge of a stroke that starts at `left` and extends by `width`, which may be negative.
The stroke covers a whole number of pixels, at least one, so a border keeps the same thickness on every side and at every position.

## snapHeight(double,double)

Returns the far edge of a stroke that starts at `top` and extends by `height`, which may be negative, rounded to a whole number of pixels and at least one.

## spanX(double,double)

Returns the horizontal [[PixelGrid.Span]] that draws a shape from `x` over `width`.
A span thinner than three pixels keeps its rounded pixel thickness and stays centered on the shape; a span thinner than one pixel draws one pixel with a coverage below `1F`, so a hairline fades instead of disappearing.

## spanY(double,double)

Returns the vertical [[PixelGrid.Span]] that draws a shape from `y` over `height`, by the same rules as `spanX`.

## quantizeX(double)

Rounds a horizontal motion in design pixels to a whole number of screen pixels, so that a moving node lands on the grid.

## quantizeY(double)

Rounds a vertical motion in design pixels to a whole number of screen pixels.

## getVisibleArea(double)

Returns the area of the design space that the viewport shows, widened by `margin` screen pixels on each side.
Returns `null` under a perspective, or when the transform flattens the plane.

## toScreenX(double)

Converts a design X to a screen X, in pixels of the viewport.

## toScreenY(double)

Converts a design Y to a screen Y, in pixels of the viewport counted from its bottom.

## fromScreenX(double)

Converts a screen X back to a design X.

## fromScreenY(double)

Converts a screen Y, counted from the bottom of the viewport, back to a design Y.

## toPixelWidth(double)

Returns how many screen pixels a design width covers, rounded up and at least one: the size of an off-screen buffer that holds it without loss.

## toPixelHeight(double)

Returns how many screen pixels a design height covers, rounded up and at least one.

## getPixelSize()

Returns the size of one screen pixel in design pixels, on the axis where the scale is largest: the width that the shaders give to an antialiased edge.

## getUnitX()

Returns the signed number of screen pixels that one design pixel moves along X.

## getUnitY()

Returns the signed number of screen pixels that one design pixel moves along Y; it is negative under the projection of a UI, whose Y goes down.

## getSkewX()

Returns the number of screen pixels along X that one design pixel along Y moves; `0` without rotation nor shear.

## getSkewY()

Returns the number of screen pixels along Y that one design pixel along X moves; `0` without rotation nor shear.

## getScaleX()

Returns the length, in screen pixels, of one design pixel along X, whatever the rotation.

## getScaleY()

Returns the length, in screen pixels, of one design pixel along Y, whatever the rotation.

## getOriginX()

Returns the screen X of the design origin.

## getOriginY()

Returns the screen Y of the design origin, counted from the bottom of the viewport.

## getViewportWidth()

Returns the width of the viewport, in pixels.

## getViewportHeight()

Returns the height of the viewport, in pixels.

## isFlat()

If `true`, the transform has no perspective: the plane of the drawing stays parallel to the screen.

## isAligned()

If `true`, the design axes are parallel to the screen axes and the snapping methods move edges onto whole pixels.
If `false`, they return their input, and the core draws with smooth edges instead.
