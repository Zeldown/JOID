---
summary: The built-in format that recognizes HEIF and AVIF images, which JOID cannot decode, to fail with a clear message.
---

The resource fails, and the dev-mode warning asks for PNG, JPEG or WebP instead.

Learn: [Media](/use/learn/describing-the-ui/media/), [Custom formats](/use/learn/going-further/custom-formats/).

## HeifResourceFormat()

Creates the format.

## supports(byte[])

If `true`, `header` is an ISO media file of a HEIF or AVIF brand (`heic`, `avif`, `mif1`...).

## decoder(Asset,byte[])

Throws `IllegalArgumentException` that names the file and asks to convert it.
