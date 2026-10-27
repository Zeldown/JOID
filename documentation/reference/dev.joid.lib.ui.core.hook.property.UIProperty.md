---
summary: Annotation that makes JOID remember a field of a UI on disk, from one run of the application to the next.
---
Put it on a field of a [[UI]] that is neither `static` nor `final`.
JOID writes the field as JSON when the UI closes, reloads and when the JVM stops, and reads it back before `init()` at the next opening; the file is `property/<class name>.property` in the data folder.

Learn: [Stores and properties](/use/learn/state/stores-and-properties/#remember-a-field-of-a-ui).

## value()

Key of the field in the file; empty uses the name of the field.
Give a key to keep the saved value when you rename the field.
