---
summary: Decodes a still image, PNG, JPEG, BMP, still WebP and the other formats of the bundled image readers, into one texture.
---

[[ResourceFormat]] picks it for any file that no other format recognizes.
The transparent pixels take the color of their opaque neighbours, so that the edges of a scaled image do not darken.

Learn: [Custom formats](/use/learn/going-further/custom-formats/).

## create(Asset)

Creates a decoder that reads `asset` with the image readers of the JVM.

## create(BufferedImage)

Creates a decoder of an image already in memory, such as one drawn with Java2D.

## create(Asset,ImageReaderSpi)

Creates a decoder that reads `asset` with `reader`.

## RasterResourceDecoder(Asset,BufferedImage,ImageReaderSpi)

Creates the decoder, for a subclass; give either an asset or an image.

## prepare(ResourceData)

Gives the data a transparent placeholder texture until the image is decoded.

## decode(ResourceData)

Reads the image and sets the size and the pixels of the data.
Throws `UncheckedIOException` when the asset cannot be read, and `IllegalArgumentException` when no reader can decode it.

## upload(ResourceData)

Uploads the pixels into the texture.

## clear(ResourceData)

Holds nothing to free; a subclass that holds more frees it there.
