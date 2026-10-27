---
summary: The registry of the asset locators, which turns any input into an Asset.
---

JOID registers three locators, for a `File`, a `String` and an `InputStream`; register your own to accept another kind of input, such as an id of your application.
The last registered locator is asked first.

Learn: [Custom formats](/use/learn/going-further/custom-formats/).

## register(IAssetLocator)

Adds a locator in front of the others; registering it again moves it to the front.

## unregister(IAssetLocator)

Removes a locator, a built-in one included.

## supports(Object)

If `true`, `handle` is an [[Asset]] or a registered locator supports it.

## locate(Object)

Returns `handle` when it is an asset, otherwise the asset of the first locator that supports it.
Throws `IllegalArgumentException` when no locator supports it.
