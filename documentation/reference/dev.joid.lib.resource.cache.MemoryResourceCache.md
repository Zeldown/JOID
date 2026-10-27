---
summary: The default resource cache, which keeps the data in memory while it is used and for a while after.
---

Data stays as long as a resource uses it, then for `expiry` after its last access, within `size` entries.

Learn: [Media](/use/learn/describing-the-ui/media/).

## create()

Creates a cache without a size limit and with an expiry of 5 minutes.

## size

The number of entries kept after their last use; `size(int)` sets it and keeps the current entries.

## expiry

How long an entry stays after its last access, in milliseconds; `expiry(long)` sets it and keeps the current entries.

## put(String,ResourceData)

Keeps `data` under `uniqueId`.

## remove(String)

Forgets the data of `uniqueId`.

## clear()

Forgets every data.

## get(String)

Returns the data of `uniqueId` while it is cached or still used by a resource, or `null`.

## getDataList()

Returns the data kept by the size and expiry rules, as a new list.
