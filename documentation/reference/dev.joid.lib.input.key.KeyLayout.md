---
summary: Translates the place of a letter or punctuation key into the key that the keyboard layout of the user types there.
---

A window library reports a key by its place on a US QWERTY keyboard: on a French AZERTY keyboard, the key that types `a` arrives as [[Key#Q]].
The input forwarder of a backend gives each key press two keys, its place and the key that the layout names ([Input forwarding](/build/input/#required)); a `KeyLayout` computes the second from the first.
Only the letters `A` to `Z` and the punctuation keys from [[Key#APOSTROPHE]] to [[Key#GRAVE_ACCENT]] depend on the layout; every other key passes through unchanged.

Learn: [Input forwarding](/build/input/#guarantees).

## from(Function)

Creates a layout from a function that returns the text that a place of the keyboard types with the current layout, such as the key name of the window library.
The function may return `null` when the place types nothing.

## translate(Key)

Returns the key that the layout types at a place.
The place comes back unchanged when it does not depend on the layout, or when the name function does not give one character that JOID knows.

## fromCharacter(char)

Returns the letter or punctuation key that types a character, whatever its case, or `null` for any other character.

## isLayoutKey(Key)

If `true`, the key depends on the layout: a letter or a punctuation key.
