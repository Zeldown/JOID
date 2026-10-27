---
summary: An asset read from a URL, over the network or from a file:, jar: or other scheme.
---

A `String` that starts with a scheme (`https:`, `file:`, `jar:`) gives one; its id is the URL.
A connection waits 10 seconds to connect and 30 seconds for data; an `https:` URL that fails on a TLS error is tried once more over `http:`, and any other error goes up as it is.

Learn: [Images and media](/use/learn/describing-the-ui/media/).

## of(String)

Creates the asset of `url`.

## open()

Connects to the URL and returns its content.
Throws `IOException` when the connection or the transfer fails; for the `http:` retry, its failure is added as suppressed to the TLS error.

## getFile()

Returns the file of a `file:` URL, or `null` for any other URL or a `file:` URL that is not a valid file URI.

## isRemote()

If `true`, the URL is neither `file:` nor `jar:` and is read on a background thread.

## getUrl()

Returns the URL.
