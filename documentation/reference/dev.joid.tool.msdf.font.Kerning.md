---
summary: The kerning pairs of the requested code points, read from the `GPOS` table or the `kern` table of a font file.
---

Learn: [MSDF text](/use/in-depth/msdf-text/).

## read(FontFile,Font,int[])

Reads the kerning of every pair of those code points that the font draws.

## getUnitsPerEm()

Returns the units per em of the values.

## getKerning()

Returns the non-zero kerning of each pair, in font units, by the two code points packed in a `long`, read-only.
