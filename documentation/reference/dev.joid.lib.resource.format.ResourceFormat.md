---
summary: The registry of the resource formats, which picks the decoder of a file from its first bytes.
---

The built-in formats are SVG, WebP, HEIF (refused with a message), video (MP4, Matroska, AVI), APNG and GIF; a file that none of them recognizes is read as a still image (PNG, JPEG, BMP and the other formats of the bundled image readers).
The last registered format is asked first.

Learn: [Custom formats](/use/learn/going-further/custom-formats/), [Images and media](/use/learn/describing-the-ui/media/).

## register(IResourceFormat)

Adds a format in front of the others; registering it again moves it to the front.

## unregister(IResourceFormat)

Removes a format, a built-in one included.

## decoder(Asset)

Reads the first 4096 bytes of `asset` and returns the decoder of the first format that supports them, otherwise a [[RasterResourceDecoder]].
Throws `UncheckedIOException` when the asset cannot be read.
