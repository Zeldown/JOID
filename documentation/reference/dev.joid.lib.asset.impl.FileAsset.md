---
summary: An asset read from a file on disk.
---

A `File`, or a `String` path that is not in the classpath but exists on disk, gives one; its id is the absolute path.

Learn: [Media](/use/learn/describing-the-ui/media/).

## of(File)

Creates the asset of `file`.

## open()

Opens the file; throws `IOException` when it does not exist or cannot be read.

## getFile()

Returns the file.
