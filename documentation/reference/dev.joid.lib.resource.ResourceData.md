---
summary: The shared state of one resource, its decoder, pixels, textures, size and errors, behind every Resource handle on the same input.
---

You meet it when you write a decoder or a resolver: the [[IResourceDecoder]] fills it, the [[Resource]] handles draw it.
Decoding runs on a pool of 16 background threads; the upload and the listeners run on the render thread.
When no handle uses it any more, its textures are freed at the next frame.

Learn: [Custom formats](/use/learn/going-further/custom-formats/).

## create(String,IResourceDecoder)

Creates the data cached under `uniqueId`, read by `decoder`, or by the decoder that `detectDecoder` finds when it is `null`.

## uniqueId

The id under which the data is cached and named in the warnings.

## decoder

The [[IResourceDecoder]] that reads the data; `decoder(IResourceDecoder)` sets it and calls its `init`.

## assetSource

Where the data reads its asset when it detects its format: `assetSource(Supplier)` sets it, read again on each detection, so that a reload finds a file that appeared since.

## textures

The GPU textures of the data, one per plane or per frame buffer of the decoder: `getTextures()` returns a copy, `textures(ITexture[])` sets them.

## data

The decoded pixels, one `0xAARRGGBB` array per texture, waiting for the upload: `getData()` returns a copy of the outer array, `data(int[][])` sets it; `null` once uploaded.

## generated

If `true`, the decode has started; `generated(boolean)` sets it.

## width

The width of the image in pixels, or of the region when one is set; `width(int)` sets the width of the image.

## height

The height of the image in pixels, or of the region when one is set; `height(int)` sets the height of the image.

## isFailed()

If `true`, the data failed; [[ResourceData#getError]] tells why.

## isDispatching()

If `true`, a background task started by `dispatch`, such as the detection of a remote format, is still running.

## getForks()

Returns the data of the separate playbacks forked from this one, as a new list.

## getDecoder(Class)

Returns the decoder when it is an instance of `clazz`, otherwise `null`.

## getRegion()

Returns a copy of the region of the textures that holds the image, as `x`, `y`, `width`, `height` in texture pixels, or `null` for the whole texture.

## getData(int)

Returns the pixels waiting for the texture at `index`, or `null`.

## getTexture(int)

Returns the texture at `index`, or `null`.

## getTextureCount()

Returns the number of textures.

## getMissingTexture()

Returns the 8 × 8 magenta and black checkerboard drawn for a failed resource in dev mode, created on the render thread at its first use.

## getError()

Returns the error that made the data fail, or `null`.

## isUploaded()

If `true`, the pixels are on the GPU.

## isLoaded()

If `true`, the decode ended without error.

## texture(ITexture)

Sets one texture, such as a texture borrowed from the application.

## region(int,int,int,int)

Sets the part of the textures at `x`, `y` of `width` × `height` texture pixels that holds the image; the size of the data becomes that of the region.

## fork(IResourceDecoder)

Returns a data with the same id, read by `decoder`, for a separate playback of the same file.

## dispatch(Runnable)

Runs `task` on a thread of its own named `ResourceTask/` and the id, such as reading a remote asset.

## await()

Waits for the detection and the decode in progress to end.

## awaitDetection()

Waits for the detection of the format in progress to end.

## detectDecoder(boolean)

Reads the first bytes of the asset and picks the decoder of its format, on a background thread when `async` is `true` and the asset is remote.
A failure to read or an unknown format makes the data fail.

## generate(boolean)

Starts the decode, on the background pool when `async` is `true`; a decode already running is not started again.
Data built from textures only, without a decoder, loads at once.

## upload()

Sends the decoded pixels to the GPU, through the decoder when it has one, then drops them from memory; JOID calls it on the render thread.

## uploadTextures()

Allocates each texture at the size of the data and uploads its pixels; a decoder calls it from its own `upload`.

## fail(Throwable)

Makes the data fail with `error` and notifies the `onError` listeners of every handle; only the first error counts.
In dev mode, it warns with the cause.

## listen(Resource)

Registers a handle to notify when the data loads or fails; the data keeps it alive until then.

## clear()

Frees the textures and the pixels, through the decoder, and resets the state so that the next draw decodes again; textures still read by a running decode are freed when it ends.

## placeholder()

Returns a new 1 × 1 transparent texture, for a decoder that has nothing to show yet.

## reload(IResourceDecoder)

Waits for a running decode, clears the data and its region, and sets `decoder` as its decoder.

## releaseCollected()

Clears the data that no handle uses any more; each draw of a UI bridge calls it.
