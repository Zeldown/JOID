---
summary: A source that reads one MsdfFontFace, from a font file or from anything else.
---

Pass one to [[MsdfFontLoader#load]] in place of a file.
Extend [[MsdfSource]] rather than implementing this interface, so that `weight(...)` and `italic(...)` work on your source too.

Learn: [Custom fonts and MSDF](/use/learn/going-further/fonts/).

## read()

JOID calls it on a loader thread, never on the render thread: read the face and return it.
Throw `IOException` when it cannot be read; the future of the loader then completes exceptionally.

## describe()

Returns how the face was read, for the line that dev mode logs after the load; by default the name of the source class.
