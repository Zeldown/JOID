---
summary: Draws and measures text, on one line or wrapped in a box.
---

Reach it through `DrawUtils.TEXT`.
Positions and sizes are in design pixels; every method returns the bounds of what it drew, and a text cut or wrapped to fit a box returns the width of the box.
Markup tags inside the text are never cut in half when a line wraps or is cut, and the elements of a [[Text]] share one baseline.

Learn: [Drawing](/use/learn/going-further/drawing/), [Text](/use/learn/describing-the-ui/text/).

## inst()

Returns the shared instance, the one in `DrawUtils.TEXT`.

## drawText(double,double,Text)

Draws the text on one line, placed around `x`, `y` by its alignment: `START` puts its left or top edge there, `CENTER` its middle, `END` its right or bottom edge.

## drawText(double,double,double,double,Text,TextMode)

Draws the text in the box at `x`, `y` of `width` × `height`, aligned inside it, with the given [[TextMode]].

## drawText(double,double,String,TextInfo,Align,Align)

Draws a string on one line with `info`, placed around `x`, `y` by the horizontal and vertical alignments.

## drawText(double,double,double,double,String,TextInfo,Align,Align,TextOverflow,TextMode)

Draws a string in a box, with its alignments, its overflow marker and its mode.

## getLines(double,Text)

Splits the text into the lines that `SPLIT` draws for `width`, each a [[Text]] with the same alignment and overflow.
A line breaks at a line break or a `<br>` tag, otherwise at the last space that fits; only a word wider than the whole line is cut.

## getLines(double,String,TextInfo)

Splits a string into the lines that fit `width` with `info`.
