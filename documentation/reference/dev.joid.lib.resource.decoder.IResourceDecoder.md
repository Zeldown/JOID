---
summary: Reads the data of one resource, decodes it on a background thread and sends it to the GPU on the render thread.
---

An [[IResourceFormat]] creates one per resource.
JOID calls `prepare` on the thread that first draws the resource, `decode` on a loading thread (or on the drawing thread for a blocking resource), then `upload` on the render thread; `clear` frees what the decoder holds.
Fill the [[ResourceData]] you are given: its size, its pixels or its textures.

Learn: [Custom formats](/use/learn/going-further/custom-formats/).

## prepare(ResourceData)

JOID calls it before each decode, on the thread that draws: create the textures there, for example with [[ResourceData#placeholder]].
An exception makes the resource fail.

## decode(ResourceData)

JOID calls it on a loading thread: read the asset and set the size and the pixels of `resource`.
An exception makes the resource fail, unless a reload replaced this decode.

## upload(ResourceData)

JOID calls it on the render thread once the pixels are decoded: send them to the textures, for example with [[ResourceData#uploadTextures]].

## clear(ResourceData)

JOID calls it when the resource is cleared or reloaded: free what the decoder holds, such as a file or a native handle.

## init(ResourceData)

JOID calls it when the decoder is attached to `resource`; empty by default.

## update(ResourceData)

JOID calls it each time the resource is bound for drawing, on the render thread: advance an animation there; empty by default.

## request(ResourceData,int,int,boolean,boolean)

JOID calls it on each draw with the size the resource covers in screen pixels; `async` tells whether to work in the background and `resizing` that the size changed less than 200 ms ago.
A vector decoder renders again at that size; empty by default.

## fork()

Returns a decoder for a separate playback of the same file, or `null` when the resource has no playback to separate; `null` by default.

## isSettled()

If `true`, the decoder has nothing left to update, so that a frame that draws it can be compared with a reference; `true` by default.

## isMipmappable()

If `true`, the textures accept mipmaps; `true` by default.
