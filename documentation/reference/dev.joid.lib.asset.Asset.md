---
summary: Where the bytes of a resource, a font, a shader or a model come from, a file, the classpath, a URL or a stream, behind one stable id.
---

Every loader of JOID takes an `Object` and turns it into an asset with `Asset.of(...)`: a `String` path or URL, a `File`, an `InputStream`, or an asset you build yourself.
The unique id keys the caches, so two loads of the same asset share their data.

Learn: [Custom formats](/use/learn/going-further/custom-formats/), [Images and media](/use/learn/describing-the-ui/media/).

## of(Object)

Returns `handle` when it is already an asset, otherwise the asset that the registered [[AssetLocator]] list finds for it.
Throws `IllegalArgumentException` when no locator supports `handle`, or when a path is found neither in the classpath nor as a file.

## Asset(String)

Creates an asset with the id that keys its caches; two assets with the same id are the same content.

## getFile()

Returns the file on disk that holds the asset, or `null` when it is not a plain file, such as a resource inside a jar or a URL; a video reads such an asset through a copy in the cache folder.

## isRemote()

If `true`, opening the asset goes through the network, so JOID reads it on a background thread.
`false` by default.

## peek(int)

Returns the first `length` bytes of the asset, fewer when it is shorter; the formats use them to recognize a file.
Throws `UncheckedIOException` when the asset cannot be read.

## read()

Returns the whole content of the asset.
Throws `IOException` when it cannot be read.

## toString()

Returns the class and the id of the asset, for logs.

## getUniqueId()

Returns the id that keys the caches of this asset.

## open()

Returns a new stream on the content, which the caller closes; JOID calls it each time it reads the asset, possibly from a background thread.
Throw `IOException` when the content cannot be opened.
