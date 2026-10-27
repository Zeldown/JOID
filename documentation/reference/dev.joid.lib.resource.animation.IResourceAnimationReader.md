---
summary: Reads every frame of an animated image file into a ResourceAnimation.
---

Give it to [[AnimatedResourceDecoder#create]] from the [[IResourceFormat]] of your format.
Compose the frames on a [[ResourceAnimationCanvas]] when the format stores them as partial updates.

Learn: [Custom formats](/use/learn/going-further/custom-formats/).

## read(InputStream)

JOID calls it on a loading thread, once per file: read the whole stream and return its frames, each a full image of the animation size.
Throw `IOException` or `IllegalArgumentException` when the file is damaged; the resource then fails.
