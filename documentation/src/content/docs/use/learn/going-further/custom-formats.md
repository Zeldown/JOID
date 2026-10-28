---
title: "Custom formats"
description: "Read resources from your own sources and your own file types, and build animations from your own frames."
genre: learn
sidebar:
  order: 8
---

[[Resource#of]] reads images, animations, vector images and videos from files, streams, URLs and the classpath ([Images and media](/use/learn/describing-the-ui/media/) lists the supported formats).
When your images live somewhere else, or in a file type that JOID does not read, teach JOID where to find them and how to decode them: they then load, cache and draw like a PNG.

**You will learn**
- How JOID turns an input into a resource
- How to read resources from a source of your own
- How to decode a file type of your own
- How to build an animation from frames of your own

## See how JOID loads a resource

`Resource.of(input)` goes through three registries, and in each one the last registered entry is asked first:

1. A resource resolver turns an input that is already a texture or a `BufferedImage` into a resource ([Images and media](/use/learn/describing-the-ui/media/) shows the textures of your application).
2. Otherwise, an asset locator of [[AssetLocator]] turns the input into an [[Asset]]: the bytes and a unique id.
   The built-in locators read a `File`, an `InputStream`, and a `String` that is a URL with its scheme, a path in the classpath or a path on the disk.
3. A resource format of [[ResourceFormat]] reads the first 4 096 bytes of the asset and returns the decoder that reads it.
   When no format recognizes them, the image goes to `ImageIO`, which reads PNG, JPEG and BMP.

The decoder then reads the asset in the background, the first time a node draws the resource, and JOID uploads the pixels on the render thread.
Resources with the same unique id share one decoded copy.

Here a locator of your own reads the prefix `pack:`, and a format of your own reads PPM images:

```java file="com/example/further/FormatsUI.java" region="register"
```

```java file="com/example/further/FormatsUI.java" region="ppm"
```

<figure>

![A grey ring drawn in large square pixels on a light square, next to a dot that turns around a dark grey circle](/captures/further-custom-formats.webp)

<figcaption>The ring is a PPM text file read by its own format; the turning dot is a sprite strip read as an animation.
Both come through the `pack:` locator.</figcaption>
</figure>

Register your locators and formats once, before the first `Resource.of(...)` that needs them: a static block of the UI that uses them, or the start of your application.
`AssetLocator.unregister(...)` and `ResourceFormat.unregister(...)` remove them.

## Read resources from your own source

An [[IAssetLocator]] says which inputs it supports, then turns each one into an [[Asset]]:

```java file="com/example/further/FormatsPackAssetLocator.java" region="locator"
```

`supports(handle)` decides from the input alone, without reading anything.
`locate(handle)` returns one of the built-in assets ([[ClasspathAsset]], [[FileAsset]], [[UrlAsset]], [[StreamAsset]]) or an [[Asset]] of your own, which implements `open()` and passes its unique id to the constructor.

When a locator throws an `IllegalArgumentException` for a `String`, the resource fails like a missing file: its `onError` listeners receive the exception, and in dev mode it draws a magenta and black checkerboard.
Any other exception, and any exception for another input, reaches the caller of `Resource.of(...)`.

## Decode a file type of your own

An [[IResourceFormat]] recognizes the file from its first bytes, then returns its decoder:

```java file="com/example/further/FormatsPpmResourceFormat.java" region="format"
```

`supports(header)` receives the first 4 096 bytes, or fewer for a smaller file: test only what your format starts with.
`decoder(asset, header)` returns a new [[IResourceDecoder]] for this asset.
When the file holds a plain image, a [[RasterResourceDecoder]] does the rest: return `RasterResourceDecoder.create(image)` with a `BufferedImage` that you build.

A decoder of your own implements four steps, each one on the [[ResourceData]] of the resource:

```java file="com/example/further/FormatsPpmResourceDecoder.java" region="decoder"
```

| Step | JOID calls it | It must |
|---|---|---|
| `prepare(resource)` | On the render thread, the first time the resource is drawn | Give the resource its textures, here one empty `ResourceData.placeholder()`. |
| `decode(resource)` | In the background, unless the resource is `blocking()` | Read the asset and set `width`, `height` and `data`: one array of ARGB pixels per texture. |
| `upload(resource)` | On the render thread, after `decode` | Send the pixels to the textures; `uploadTextures()` does it for one image per texture. |
| `clear(resource)` | When the resource is released, after JOID deleted its textures | Release what the decoder holds on its own. |

An exception thrown by `decode` makes the resource fail, as a missing file does.
The other hooks of [[IResourceDecoder]] do nothing by default: `update` runs each time the resource is bound, for an animation that changes its texture; `request` receives the size the resource is drawn at, for a format that renders at any size; `fork` returns a copy that plays on its own, one per node; `isMipmappable` returns `false` when the texture changes too often for mipmaps.

## Build an animation from your own frames

An animation needs no decoder of your own: an [[IResourceAnimationReader]] reads the frames, and an [[AnimatedResourceDecoder]] plays them like a GIF.
This reader cuts a sprite strip into frames of the same width:

```java file="com/example/further/FormatsStripAnimationReader.java" region="reader"
```

[[ResourceAnimationFrame#of]] takes the ARGB pixels of the whole frame and its duration in milliseconds.
[[ResourceAnimation#of]] takes the size, the number of plays (`0` loops forever) and the frames.

A sprite strip has no header that a format could recognize, so the resource names its decoder itself.
[[ResourceBuilder#compute]] creates the resource from a [[ResourceData]], once per unique id:

```java file="com/example/further/FormatsUI.java" region="strip"
```

```java file="com/example/further/FormatsUI.java" region="coin"
```

The animation plays, pauses and loops like any animated image ([Images and media](/use/learn/describing-the-ui/media/) shows the playback).
When the frames of your format cover only part of the image, as in a GIF, compose them on a [[ResourceAnimationCanvas]]: `compose(frame, x, y, width, height, blend, disposal)` draws a partial frame over the previous ones and returns the whole image, with the [[ResourceAnimationCanvas.Blend]] and the [[ResourceAnimationCanvas.Disposal]] of the frame.

## Recap

- `Resource.of(...)` goes through the **resolvers**, the **asset locators**, then the **resource formats**; the last registered entry is asked first.
- An **`IAssetLocator`** turns an input of your own into an `Asset`; when it throws, the resource fails.
- An **`IResourceFormat`** recognizes a file from its first 4 096 bytes and returns a **decoder**.
- A **decoder** prepares its textures, **decodes in the background**, then uploads on the render thread.
- An **`IResourceAnimationReader`** turns a file into frames, played by an **`AnimatedResourceDecoder`**.
- **`ResourceBuilder.compute`** creates a resource with a decoder that no format can pick.

## Try it

**A faster coin.**
Make the coin turn twice as fast.

<details>
<summary>Solution</summary>

Give each frame 40 milliseconds instead of 80:

```java fragment
new FormatsStripAnimationReader(8, 40L)
```

</details>

**A coin that stops.**
Make the coin turn once, then stop on its last frame.

<details>
<summary>Solution</summary>

In the reader, play the frames once:

```java fragment
return ResourceAnimation.of(width, height, 1, frames);
```

</details>
