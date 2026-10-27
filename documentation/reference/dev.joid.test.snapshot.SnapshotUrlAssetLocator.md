---
summary: Downloads an `http` or `https` resource once into the `snapshot` area of the cache folder, so that later snapshot runs work offline.
---

Learn: [Keep every run identical](/use/learn/running/testing/#keep-every-run-identical).

## SnapshotUrlAssetLocator()

Creates the locator.

## supports(Object)

If `true`, the handle is a string that starts with `http://` or `https://`.

## locate(Object)

Returns an asset that reads the cached download of the URL, downloading it at the first read.
