---
summary: The event of `handleCharTyped` on a node, with one typed character as a Unicode code point.
---

The character comes with the layout and the Shift key applied; control characters, such as the one of Enter, never arrive.
Every visible node of the UI receives it, whether the pointer is over it or not.

Learn: [Keyboard](/use/learn/interactivity/keyboard/#react-to-a-key).

## create(N,int,IConsumableEvent)

Creates the event for a node and a code point.
`source` is the input that the event belongs to, consumed with it, or `null` for an input of its own.

## getCodepoint()

Returns the Unicode code point of the typed character; `Character.toChars` turns it into text.
