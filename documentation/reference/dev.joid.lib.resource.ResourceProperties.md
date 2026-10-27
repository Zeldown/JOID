---
summary: The drawing settings of a resource handle, its filtering, its mipmaps and its region.
---

Learn: [Media](/use/learn/describing-the-ui/media/).

## create()

Creates settings with linear filtering, automatic mipmaps and no region.

## interpolation

The [[TextureFilter]] used when the texture is scaled: `LINEAR` smooths, `NEAREST` keeps the pixels sharp.

## mipmap

`true` to always use mipmaps, `false` to never use them; `null` lets JOID turn them on when the image is drawn smaller than its texture with linear filtering.

Default: not forced

## linear()

Sets the filtering to `LINEAR`.

## nearest()

Sets the filtering to `NEAREST`.

## textureCoords(double,double,double,double)

Sets the region at `u`, `v` of `width` × `height` pixels of the image to draw.

## getTextureCoords()

Returns a copy of the region as `u`, `v`, `width`, `height`, or `null` for the whole image.

## copy()

Returns a copy of these settings.

## set(ResourceProperties)

Copies every setting of `properties` into these.

## equals(Object)

If `true`, the other object is of the same class with the same mipmap, texture coordinates and interpolation.

## hashCode()

Returns a hash of the values that `equals` compares.
