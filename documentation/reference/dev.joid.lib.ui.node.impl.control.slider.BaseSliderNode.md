---
summary: The behavior of a slider without its look: a choice whose options sit along the width of the node, picked by a press or a drag.
---

A left press jumps to the step nearest to the pointer, and a drag follows the pointer until the release; each new step goes through `change(...)`, so `beforeChange` can refuse it.
A refused step does not stop the drag: the value keeps the last step that passed.
Use [[IntegerSliderNode]], [[DoubleSliderNode]] or [[StringSliderNode]] for the options, and [[SliderNode]] for a look made of a track and a thumb.

Learn: [Controls](/use/learn/interactivity/controls/#pick-a-value-on-a-track), [UI kit](/use/learn/going-further/ui-kit/#draw-a-control-entirely-your-own-way).

## BaseSliderNode(double,double,double,double)

Creates the slider at `x`, `y` with a `width` and a `height` in design pixels, from the constructor of a subclass.

## inset

The space, in design pixels, between each end of the node and the first or the last step.
A press inside that space picks the first or the last step.

## getProgress()

Returns the place of the value along the track, from `0D` at the first option to `1D` at the last.
During a drag, the place of the pointer, so that a thumb follows it between the steps.

## isSliding()

If `true`, the user drags the slider.

## mousePressed(NodeMouseEvent)

On a left press over the slider that no one consumed, starts sliding, picks the step under the pointer and consumes the press.

## mouseReleased(NodeMouseEvent)

Picks the step under the pointer and ends the slide.

## mouseDragged(NodeMouseEvent)

Picks the step under the pointer while the slide goes on.

## detach()

Ends a slide in progress when the slider leaves the tree.
