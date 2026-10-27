---
summary: Reads the frames of an animated PNG (APNG).
---

Learn: [Custom formats](/use/learn/going-further/custom-formats/).

## read(InputStream)

Reads every frame of the APNG, composed with its blend and disposal, and the play count of the file.

## isAnimated(byte[])

Returns `true` when `bytes` show an animation control chunk before the image data, `false` when they show the image data first or are not a PNG, and `null` when they end before either.
