---
summary: The built-in format of SVG files, rendered as vector images at the size they are drawn.
---

The root element must be `<svg>`; an XML declaration, comments and a doctype before it are skipped.

Learn: [Images and media](/use/learn/describing-the-ui/media/), [Custom formats](/use/learn/going-further/custom-formats/).

## SvgResourceFormat()

Creates the format.

## supports(byte[])

If `true`, the first element of `header` is `<svg`.

## decoder(Asset,byte[])

Returns a [[VectorResourceDecoder]] that renders the document.
