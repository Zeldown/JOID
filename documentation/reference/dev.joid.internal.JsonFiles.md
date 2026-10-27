---
summary: Reads and writes the JSON files of JOID safely: atomic writes, and a damaged file set aside instead of lost.
---

The stores and the remembered properties of the UIs keep their files with it.
Every failure prints a `[JOID]` warning instead of throwing.

Learn: [Stores and remembered properties](/use/learn/state/stores-and-properties/), [Where JOID stores files](/use/learn/running/files/).

## read(File,String)

Returns the JSON object of `file`, or `null` when the file does not exist or cannot be used.
A file that holds no JSON object is renamed to `<name>.corrupt`; the warning ends with `consequence`, which says what happens instead.

## write(File,String)

Writes `json` to `file` through a temporary file in the same folder, created when missing, then moves it in place in one step, so a crash never leaves half a file.
It does nothing when `json` is the text that it last read or wrote for that file and the file exists.

## keep(File,String,String)

Renames `file` to `<name>.corrupt`, replacing an older one, and prints a warning with `failure` and `consequence`.
