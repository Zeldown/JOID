---
summary: An asset read once from an `InputStream` and kept in memory, so that it can be read again.
---

An `InputStream` gives one; its id is `stream_` followed by a counter, so two streams never share a cache entry.
The first `open()` reads the stream to its end and closes it; a stream that fails to close only warns.

Learn: [Images and media](/use/learn/describing-the-ui/media/).

## of(InputStream)

Creates the asset of `stream`, which is read at the first `open()`.

## open()

Returns a new stream on the bytes read from the original stream, so that a reload reads the same content.
