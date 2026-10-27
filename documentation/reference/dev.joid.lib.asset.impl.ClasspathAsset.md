---
summary: An asset found in the classpath, such as a file packed in the jar of your application.
---

A `String` path without a scheme gives one when the classpath has it, with or without a leading `/`; its id is `classpath:` followed by the path.

Learn: [Images and media](/use/learn/describing-the-ui/media/).

## of(String,URL)

Creates the asset of the classpath `path`, read from `url`.

## open()

Opens the URL of the resource.

## getFile()

Returns the file when the classpath entry is a folder on disk, or `null` inside a jar.

## getUrl()

Returns the URL the asset is read from.

## getPath()

Returns the classpath path, without a leading `/`.
