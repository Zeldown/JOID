---
summary: Generates the MSDF atlas of a font file, as a `font.msdf` file that JOID loads without generating it again.
---

JOID calls it at the first load of a font file and caches the result; run it from the command line to ship the atlases ready.

```text fragment
java -jar joid-tool-msdf-<version>.jar --font Montserrat-Regular.ttf --output fonts/Montserrat-Regular
```

Learn: [Generate MSDF atlases before you ship](/use/learn/going-further/fonts/#generate-msdf-atlases-before-you-ship), [MSDF text](/use/in-depth/msdf-text/).

## main(String[])

Reads the options `--font` (required), `--output`, `--charset`, `--range`, `--width`, `--height` and `--size`, then writes `font.msdf` into the output folder and prints a summary that starts with `[JOID]`.
Throws an `IllegalArgumentException` with the usage for a missing font or an unknown option, and when an option has no value.

## generate(byte[],File,int[],int,int,double,double)

Writes the atlas of the font bytes into the file, with the glyphs of those code points that the font draws, at that atlas size in pixels and that distance range in pixels, and returns a one-line summary.
A requested glyph size of `0D` or less takes the largest size that fits; a larger one throws an `IllegalStateException` when the glyphs do not fit at it.

## codepoints(String)

Returns the code points of a charset: the path of a file that holds one, or ranges and values such as `[32, 126], 8364`.
Throws an `IllegalArgumentException` when the text is neither.

## REVISION

The revision of the generator, part of the cache key of the atlases that JOID generates: a new revision generates them again.

## WIDTH

The default width of the atlas, in pixels.

## HEIGHT

The default height of the atlas, in pixels.

## RANGE

The default distance range, in pixels of the atlas.

## CHARSET

The default charset, the code points from 32 to 563: Basic Latin, Latin-1 Supplement, Latin Extended-A and most of Latin Extended-B.
