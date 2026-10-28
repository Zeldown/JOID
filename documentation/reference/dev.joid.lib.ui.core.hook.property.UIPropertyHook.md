---
summary: Reads and writes the `@UIProperty` fields of a UI in its property file.
---
JOID calls it for you when a UI opens, reloads and closes, and when the JVM stops.
A field that cannot be read keeps its default value, and a field that cannot be saved is left out of the file; both print a warning.

Learn: [Stores and properties](/use/learn/state/stores-and-properties/#remember-a-field-of-a-ui).

## load(UI)

Sets every `@UIProperty` field of the UI from its property file; a key missing from the file leaves its field unchanged.

## save(UI)

Writes every `@UIProperty` field of the UI to its property file; a `null` field removes its key.
