---
summary: Turns one kind of input into an Asset, so that every loader of JOID accepts it.
---

Register it with [[AssetLocator#register]].
Return an asset with a stable id, such as a [[ClasspathAsset]], so that the caches and a reload find the same content.

Learn: [Custom formats](/use/learn/going-further/custom-formats/).

## supports(Object)

If `true`, this locator turns `handle` into an asset.

## locate(Object)

Returns the asset of `handle`; JOID calls it only when `supports` returned `true`.
Throw `IllegalArgumentException` when the input names nothing: the resource then fails instead of loading.
