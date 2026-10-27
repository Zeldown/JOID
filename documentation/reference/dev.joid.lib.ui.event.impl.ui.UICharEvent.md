---
summary: The event of `handleCharTyped` on a UI, with one typed character that no node consumed, as a Unicode code point.
---

The character comes with the layout and the Shift key applied; control characters never arrive.

Learn: [Keyboard](/use/learn/interactivity/keyboard/#react-to-a-key).

## create(U,int)

Creates the event for a UI and a code point.

## getCodepoint()

Returns the Unicode code point of the typed character; `Character.toChars` turns it into text.
