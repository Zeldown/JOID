---
summary: Recognizes one file format from its first bytes and creates the decoder that reads it.
---

Register it with [[ResourceFormat#register]] so that [[Resource#of]] reads your format.
JOID calls both methods on the thread that detects the format, often a background thread.

Learn: [Custom formats](/use/learn/going-further/custom-formats/).

## supports(byte[])

If `true`, this format reads a file that starts with `header`, the first 4096 bytes of the file or fewer.

## decoder(Asset,byte[])

Returns the decoder that reads `asset`; JOID calls it only when `supports` returned `true`.
Throw an exception to make the resource fail with its message.
