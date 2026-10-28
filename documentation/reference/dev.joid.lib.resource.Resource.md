---
summary: An image, an animated image, a vector image or a video, loaded in the background and drawn by a resource node or `DrawResource`.
---

Create one from a path, a URL, a `File`, a stream or an [[Asset]], and give it to a resource node:

```java fragment
final Resource logo = Resource.of("images/logo.png").nearest();
final Resource trailer = Resource.of("https://example.com/trailer.mp4").onError((resource, error) -> this.retry());
```

A `Resource` is a light handle: every handle of the same input shares one [[ResourceData]] through the cache of its [[ResourceBuilder]], so loading the same file twice decodes it once.
Its own settings (interpolation, mipmaps, region, blocking) belong to the handle.
The file is decoded on a background thread the first time the resource is drawn, then uploaded on the render thread; a resource that cannot be read fails and draws a checkerboard in dev mode, nothing in production.

Learn: [Images and media](/use/learn/describing-the-ui/media/).

## create(ResourceBuilder,ResourceData)

Creates a handle on `data` with the default settings of `builder`; [[ResourceBuilder#of]] does that for you.

## of(Object)

Returns a handle on the resource of `input`, through the default builder and its shared memory cache.
The format comes from the first bytes of the file, not from its name.
A path found nowhere gives a failed resource, which `reload()` reads again; an input of a type that no locator knows throws `IllegalArgumentException`.

## properties

The settings of this handle: interpolation, mipmaps and region.
`getProperties()` returns them; `properties(ResourceProperties)` replaces them with a copy.

## uniqueId

The id under which the data of this resource is cached and named in the warnings.
`uniqueId(String)` renames the shared data: every handle on it sees the new id.

## decoder

The [[IResourceDecoder]] that reads the data, or `null` until the format is detected.
`decoder(IResourceDecoder)` waits for a running decode, clears the data and decodes it again with `decoder`, for every handle on the data.

## getWidth()

Returns the width to draw at: the width of the region when `textureCoords` is set, otherwise the width of the image in pixels, `0` before it is loaded.

## getHeight()

Returns the height to draw at: the height of the region when `textureCoords` is set, otherwise the height of the image in pixels, `0` before it is loaded.

## bind(TextureWrap,Runnable)

Binds the texture of the resource with `wrap` and the settings of this handle, runs `runnable`, then unbinds it, even when `runnable` throws.
It starts the decode and the upload when they have not happened yet; a resource still loading binds no texture.

## unbind()

Unbinds the texture from the render state.

## bindTextureOnly(TextureWrap)

Binds the texture like `bind` and leaves it bound; call `unbind()` afterwards.
A failed resource binds the checkerboard in dev mode and no texture in production.

## getSourceWidth()

Returns the width of the whole image in pixels, whatever the region.

## getSourceHeight()

Returns the height of the whole image in pixels, whatever the region.

## getData()

Returns the decoded pixels of the first texture as `0xAARRGGBB` values, or `null` once they are uploaded.

## getData(int)

Returns the decoded pixels of the texture at `index`, or `null`.

## getTexture()

Returns the first texture of the resource, or `null` before it is loaded.

## getTexture(int)

Returns the texture at `index`, such as one plane of a video frame, or `null`.

## isMipmappable()

If `true`, the decoder accepts mipmaps; a video does not.

## getResourceData()

Returns the shared data behind this handle.

## getPlayback()

Returns the playback controls of an animated image or a video, or `null` for a still image.

## isLoaded()

If `true`, the data is decoded.

## isUploaded()

If `true`, the decoded pixels are on the GPU.

## isGenerated()

If `true`, the decode has started.

## isFailed()

If `true`, the resource could not be read; the error went to the `onError` listeners.

## getBuilder()

Returns the builder that made this handle.

## isBlocking()

If `true`, this handle decodes on the thread that draws it.

## blocking()

Makes this handle decode on the thread that draws it and wait for the end, so that it shows on the first frame; a remote asset is then also detected on that thread.
Use it for small images that must never pop in.

## interpolation(TextureFilter)

Sets how the texture is filtered when it is scaled.

## linear()

Filters the texture smoothly when it is scaled, the default.

## nearest()

Keeps the pixels sharp when the texture is scaled, for pixel art.

## mipmap(boolean)

If `true`, the texture gets mipmaps, for an image drawn much smaller than its size; if `false`, never.
Without a call, JOID turns them on by itself when the image is drawn smaller than its texture with linear filtering.

## textureCoords(double,double,double,double)

Draws only the region at `u`, `v` of `width` × `height` pixels of the image, such as one sprite of a sheet; the size of the resource becomes that of the region.

## reset()

Sets the settings of this handle back to those of its builder.

## copy()

Returns another handle on the same data, with the same settings.

## fork()

Returns a handle on a separate playback of the same file, for an animated image or a video shown twice with its own position; a still image returns this handle.
It waits for the format to be detected.

## generate()

Starts the decode, on a background thread unless the handle is blocking; drawing the resource does it for you, and calling it earlier has a large image ready for its first draw ([Know when an image is loaded](/use/learn/describing-the-ui/media/#know-when-an-image-is-loaded)).

## upload()

Sends the decoded pixels to the GPU, on the render thread; drawing the resource does it for you.

## request(int,int)

Tells the decoder the size the resource is drawn at, in screen pixels, so that a vector image renders at that size; while the size keeps changing, the decoder waits 200 ms before it renders again.
[[DrawResource]] calls it on every draw.

## prepareBind()

Starts the decode, applies the mipmap setting and uploads the pixels when they are ready; `bind` calls it.

## clear()

Frees the textures and the pixels of the data and removes it from the cache, for every handle on it; the next draw decodes the file again.

## reload()

Frees the textures and the pixels of the data, for every handle on it, so that the next draw reads the file again; a failed resource gets a new try.

## fireLoad()

Calls the `onLoad` listeners of this handle on the render thread; the data calls it once the decode ends.

## fireError(Throwable)

Calls the `onError` listeners of this handle on the render thread with `error`; the data calls it when the resource fails.

## onLoad(Consumer)

Adds a listener that runs with this resource when its data is decoded, through `runOnRenderThread` of the thread bridge, or at once when it is already decoded.
The data keeps the handle alive until the load or the failure is notified, so a handle thrown away right after `onLoad` still receives it.
[[Resource#copy]] does not copy the listeners.

## onError(BiConsumer)

Adds a listener that runs with this resource and the error when it fails, through `runOnRenderThread` of the thread bridge, or at once when it already failed.
