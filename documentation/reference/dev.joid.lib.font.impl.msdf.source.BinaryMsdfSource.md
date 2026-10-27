---
summary: Reads a face from a .msdf file made by the msdf tool of JOID, with no generation at load time.
---

Ship `.msdf` files to skip the generation of the first load.
The read fails with `IOException` when the file does not start with `JOIDMSDF` or was written in another format version of the tool.

Learn: [Custom fonts and MSDF](/use/learn/going-further/fonts/), [MSDF text](/use/in-depth/msdf-text/).

## of(Object)

Creates a source from anything [[Asset#of]] accepts: a path, a `File`, a URL, a stream or an [[Asset]].

## describe()

Returns `read from a .msdf file`.

## getAsset()

Returns the `.msdf` file.
