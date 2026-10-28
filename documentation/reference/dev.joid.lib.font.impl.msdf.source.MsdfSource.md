---
summary: Base class of the MSDF sources, which can override the weight and the style that the file declares.
---

Wrap a file in a source when the weight or the style that it declares is wrong ([Set the weight of a font file](/use/learn/going-further/fonts/#set-the-weight-of-a-font-file)).

Learn: [Custom fonts and MSDF](/use/learn/going-further/fonts/).

## MsdfSource()

Creates a source, for a subclass.

## italic

If set, the face read by this source declares this style instead of the one of its file.

## weight

If set, the face read by this source declares this [[FontWeight]] instead of the one of its file.

## read()

Reads the face with `parse()`, then applies the weight and the style set on this source.

## parse()

JOID calls it from `read()` on a loader thread: read the face as its file describes it.
Throw `IOException` when it cannot be read.
