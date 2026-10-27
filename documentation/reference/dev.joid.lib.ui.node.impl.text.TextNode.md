---
summary: Draws a Text in its bounds, and takes the size of the text when no size is declared.
---
The style comes from the [[Text]] and its [[TextInfo]]; the mode decides how a text wider than the node is shown.
The node measures its text in `update()`, so a layout node sees a change of text on the same frame; an empty or `null` text gives a size of `0` on each axis that is not declared.

Learn: [Text](/use/learn/describing-the-ui/text/).

## create(double,double)

Creates a text node at `x`, `y` that takes the width and the height of its text.

## create(double,double,double,double)

Creates a text node with a declared size; a size of `0` on an axis takes the size of the text on that axis.

## TextNode(double,double,double,double)

Called by a subclass with the same arguments as `create`.

## text

The text to draw; `null` draws nothing.

## mode

How the text fits the node, see [[TextMode]]; with `SPLIT`, the height follows the lines the text wraps into.

## init(UI)

Measures the text when the node loads.

## update()

Measures the text on every frame.

## draw(double,double)

Draws the text in the bounds and fits the size of the node to what was drawn.

## drawSkeleton(double,double)

Measures the text, then draws the default skeleton at that size.
