---
summary: Where a ResourceBuilder keeps the data of its resources by id, so that two loads of the same input share it.
---

[[MemoryResourceCache]] is the default; implement this interface to keep data under another policy, and give it to [[ResourceBuilder#cache]].
It is called from the render thread and from loading threads: make it thread-safe.

Learn: [Custom formats](/use/learn/going-further/custom-formats/).

## put(String,ResourceData)

Keeps `data` under `uniqueId`.

## remove(String)

Forgets the data of `uniqueId`.

## clear()

Forgets every data; the next load of each input reads it again.

## get(String)

Returns the data of `uniqueId`, or `null` when the cache does not have it.

## getDataList()

Returns the data the cache holds, as a new collection.
