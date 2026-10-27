---
summary: The cache folder of JOID, split into named areas whose entries expire when they go unused.
---

Ask for an area by name and keep it in a constant; JOID itself uses the areas `msdf` (font atlases), `video` (copies of the videos it plays) and `ffmpeg` (the video natives).

```java fragment
private static final FileCacheArea THUMBNAILS = FileCache.area("thumbnails").retention(Duration.ofDays(7L));
```

Every entry that has not been read for longer than the retention of its area is deleted when JOID starts.
Use the cache for files that you can build again; data that must survive goes in the data folder.

Learn: [Files](/use/learn/running/files/).

## area(String)

Returns the area `name`, the same instance for the same name, in the folder of that name under the cache folder.
Throws `IllegalArgumentException` when `name` is not a plain file name: a letter or a digit, then letters, digits, `.`, `-` and `_`.

## clean()

Deletes the expired entries of every area of the cache folder, and the temporary files older than one day, then returns the number of bytes freed.
JOID runs it on a background thread when it starts and logs what it freed.
