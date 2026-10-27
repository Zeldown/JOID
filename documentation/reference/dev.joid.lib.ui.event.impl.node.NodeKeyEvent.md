---
summary: The event of `handleKeyPressed` and `handleKeyReleased` on a node, with the key that the layout names and its place on the keyboard.
---

Every visible node of the UI receives the keys, whether the pointer is over it or not; the release reaches every node, even hidden or disabled.
A held key sends one press and one release, never a repeat.

Learn: [Keyboard](/use/learn/interactivity/keyboard/#tell-a-letter-from-its-place).

## create(N,Key,Key,IConsumableEvent)

Creates the event for a node, the place of the key and the key that the layout names.
`source` is the input that the event belongs to, or `null` for an input of its own.

## getKey()

Returns the key that the layout of the user names: on a French AZERTY keyboard, the key at the place of `Q` is [[Key#A]].
Use it for a shortcut.

## getPhysicalKey()

Returns the place of the key on a US QWERTY keyboard, whatever the layout.
Use it for a movement, so that the keys stay under the same fingers on every layout.
