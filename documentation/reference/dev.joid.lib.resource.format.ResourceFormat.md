---
summary: The registry of the resource formats, which picks the decoder of a file from its first bytes.
---

The built-in formats read the files of [Supported formats](/use/learn/describing-the-ui/media/#supported-formats); a file that none of them recognizes goes to the bundled image readers.
The last registered format is asked first.

Learn: [Custom formats](/use/learn/going-further/custom-formats/), [Images and media](/use/learn/describing-the-ui/media/).

## register(IResourceFormat)

Adds a format in front of the others; registering it again moves it to the front.

## unregister(IResourceFormat)

Removes a format, a built-in one included.

## decoder(Asset)

Reads the first 4096 bytes of `asset` and returns the decoder of the first format that supports them, otherwise a [[RasterResourceDecoder]].
Throws `UncheckedIOException` when the asset cannot be read.
