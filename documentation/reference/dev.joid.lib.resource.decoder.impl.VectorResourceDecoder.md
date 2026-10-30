---
summary: Decodes an SVG document and renders it again at the size it is drawn at, so that it stays sharp.
---

[[SvgResourceFormat]] picks it.
The first render has the size of the document; each draw then asks for its size in screen pixels, at most 4096 on the longer side, rendered on a background thread and kept with the last 8 sizes.
While the size keeps changing, the closest texture already rendered is drawn, so an animated size does not render on every frame.
Every render gives the same pixels on every JDK, from Java 8 to the latest: the shapes are filled without antialiasing at 8 samples per pixel on each axis, fewer above 512 pixels but at least 2, then averaged, and JOID outlines the strokes itself instead of the JDK.

Learn: [Custom formats](/use/learn/going-further/custom-formats/), [Images and media](/use/learn/describing-the-ui/media/).

## create(Asset)

Creates a decoder of the SVG `asset`.

## VectorResourceDecoder(Asset)

Creates the decoder, for a subclass.

## prepare(ResourceData)

Gives the data a transparent placeholder texture until the document is rendered.

## decode(ResourceData)

Parses the document and renders it at its own size.
Throws `UncheckedIOException` when the asset cannot be read, and `IllegalArgumentException` when it is not an SVG document.

## upload(ResourceData)

Uploads the first render.

## clear(ResourceData)

Frees every rendered texture and the parsed document.

## isSettled()

If `true`, no render is in progress or waiting for its upload.

## isMipmappable()

Always `false`: a vector image renders at the size it is drawn at instead.

## getAsset()

Returns the SVG file.

## update(ResourceData)

Uploads a render finished in the background, unless the document was replaced since.

## request(ResourceData,int,int,boolean,boolean)

Shows the texture of that size when it is already rendered, otherwise renders it: on the background thread when `async` is `true`, at once otherwise.
While `resizing`, it shows the closest larger texture or renders 25 % larger, so that a growing size does not render on every frame.
