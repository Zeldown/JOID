---
summary: Remembers which key each held place of the keyboard typed, so that a release and a held test use the key of its press.
---

The UI bridge registry keeps one `KeyState`: a press records its place and its key, and the release of that place reports the same key, even when the layout changed in between.
[[Key#isDown]] reads it.

Learn: [Input forwarding](/build/input/#send-the-position-and-the-printed-key).

## KeyState()

Creates a state in which no key is held.

## press(Key,Key)

Records that the place `physicalKey` went down and typed `key`.

## release(Key)

Forgets a place that went up and returns the key that its press typed, or the place itself when no press was recorded.

## isDown(Key)

Returns `true` while a key is held.
For a key that depends on the layout, it tests every held place that typed it; for any other key, it asks the window bridge.

## getKey(Key)

Returns the key that a held place typed, or the place itself when it is not held.
