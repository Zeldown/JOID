---
summary: Reads a face from a TrueType or OpenType file, generating its MSDF atlas into the cache folder on the first load.
---

The atlas covers the codepoints 32 to 563 at 2048 × 2048 pixels.
It is stored in the `msdf` area of the cache folder under a hash of the font and of the generator settings, kept 30 days after its last use, and generated again when the cached file cannot be read.

Learn: [Fonts](/use/learn/going-further/fonts/), [MSDF text](/use/in-depth/msdf-text/).

## of(Object)

Creates a source from anything [[Asset#of]] accepts: a path, a `File`, a URL, a stream or an [[Asset]].

## supports(byte[])

If `true`, `header` starts like a TrueType, OpenType or font collection file.

## describe()

Returns whether the atlas was generated or read from the cache, and its file.

## getAsset()

Returns the font file.

## getFile()

Returns the cached atlas file, `null` before the face is read.

## isGenerated()

If `true`, the last read generated the atlas instead of finding it in the cache.

## HEADER

The number of bytes `supports` needs: 4.
