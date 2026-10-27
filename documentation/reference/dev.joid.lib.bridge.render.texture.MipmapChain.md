---
summary: The levels of a mipmapped texture, each half the size of the previous one down to 1 pixel.
---

Learn: [Mipmaps](/build/pipeline/#mipmaps).

## of(int,int,boolean)

Returns the chain of a texture of that size: every level down to 1×1 when mipmapped, the first level alone otherwise.

## limit(int)

Returns the chain cut to that many levels, at least one.

## getWidth(int)

Returns the width of that level, at least `1`.

## getWidth()

Returns the width of the first level.

## getHeight(int)

Returns the height of that level, at least `1`.

## getHeight()

Returns the height of the first level.

## forEachStep(MipmapStep)

Calls the step for each level after the first, with the size of the level above it and its own size.

## getLevels()

Returns the number of levels.
