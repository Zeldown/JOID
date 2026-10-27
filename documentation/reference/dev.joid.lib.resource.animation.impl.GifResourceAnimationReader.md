---
summary: Reads the frames of a GIF.
---

A frame delay of 10 ms or less shows for 100 ms, as browsers do; the play count comes from the loop extension of the file, otherwise one play.

Learn: [Custom formats](/use/learn/going-further/custom-formats/).

## GifResourceAnimationReader()

Creates the reader.

## read(InputStream)

Reads every frame of the GIF, composed with its disposal, and its play count.
