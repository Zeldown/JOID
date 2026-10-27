---
summary: One folder of the cache, whose entries are written once by key, read many times, and deleted after a period without use.
---

Get one with `of(name)`, and keep it in a constant.
Each entry is written into a temporary file and moved into place in one step, so a reader never sees a half-written entry, and two threads that compute the same key write it once.
Reading an entry marks it as used; the retention counts from the last use.
The methods are thread-safe; call them off the render thread, since they touch the disk.

Learn: [Where JOID stores files](/use/learn/running/files/).

## of(String)

Returns the area `name`, the same instance for the same name, in the folder of that name under the cache folder.
Throws `IllegalArgumentException` when `name` is not a plain file name: a letter or a digit, then letters, digits, `.`, `-` and `_`.

## retention

How long an entry stays in the cache without being read before JOID deletes it.
`retention(Duration)` declares it and writes it into the folder, so the cleaning at the next start reads it.
Throws `IllegalArgumentException` for a duration of zero or less, and `IllegalStateException` when the area was already declared with another retention.

## getDirectory()

Returns the folder of the area under the cache folder; it is created by the first write.

## entry(String)

Returns the file of `key` and marks it as used, or `null` when the area has no such entry.
Throws `IllegalArgumentException` when `key` is not a plain file name.

## compute(String,FileCacheWriter)

Returns the file of `key`, writing it with `writer` first when the area does not have it.
Throws `IOException` when the folder cannot be created, when `writer` fails, or when it writes nothing.

## store(FileCacheWriter)

Writes a content with `writer` and returns its file, named after the SHA-1 of the content: the same content is stored once.
Throws `IOException` when the folder cannot be created, when `writer` fails, or when it writes nothing.

## remove(String)

Deletes the entry `key` and returns `true` when there was one.
An entry still open elsewhere is left to the next cleaning, with a dev-mode warning.

## clean(long,AtomicInteger)

Deletes the entries unused since `now` minus the retention, and the temporary files older than one day, adds the number of deleted files to `files` and returns the bytes freed.
[[FileCache#clean]] calls it for every area.

## getName()

Returns the name of the area, the name of its folder.

## MARKER

The name of the file, `.area`, that marks a folder of the cache folder as an area and holds its retention.
