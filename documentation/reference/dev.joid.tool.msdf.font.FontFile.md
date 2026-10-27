---
summary: The tables of a TrueType or OpenType file, read for the weight, the style and the kerning that the font of Java does not give.
---

Learn: [MSDF text](/use/in-depth/msdf-text/).

## read(byte[])

Reads the table directory of the file, or of the first font of a collection.

## getWeight()

Returns the weight of the `OS/2` table, `400` without it.

## getUnitsPerEm()

Returns the units per em of the `head` table, `1000` without it.

## getTables()

Returns the offset of each table by tag, read-only.

## isItalic()

If `true`, the `OS/2` or `head` table marks the face as italic or oblique.

## signed(int)

Returns the signed 16-bit value at that offset.

## unsigned(int)

Returns the unsigned 16-bit value at that offset.

## integer(int)

Returns the 32-bit value at that offset.
