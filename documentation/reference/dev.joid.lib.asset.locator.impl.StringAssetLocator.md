---
summary: The built-in locator that turns a `String` into a URL, classpath or file asset.
---

Learn: [Custom formats](/use/learn/going-further/custom-formats/).

## StringAssetLocator()

Creates the locator.

## supports(Object)

If `true`, `handle` is a `String`.

## locate(Object)

Returns a [[UrlAsset]] for a text that starts with a scheme of two letters or more (`https:`, `file:`, `jar:`), otherwise a [[ClasspathAsset]] when the classpath has the path, with or without a leading `/`, otherwise a [[FileAsset]] when the path is a file on disk.
Throws `IllegalArgumentException` when the path is found nowhere.
