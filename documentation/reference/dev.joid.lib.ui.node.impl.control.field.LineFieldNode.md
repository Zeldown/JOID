---
summary: A one-line field: Enter submits it, and its text scrolls sideways to follow the cursor.
---

Extend it for a field of your own value type: write `parse` and `format`, and `correct`, `accepts` or `increment` when the type needs them.
A field of height `0` takes the height of its font plus its top and bottom margins.

Learn: [Text fields](/use/learn/interactivity/text-fields/#write-a-field-for-your-own-value).

## LineFieldNode(double,double,double,double)

Creates the field at `x`, `y` with a `width` and a `height` in design pixels, from the constructor of a subclass.

## align

Sets `alignX` and `alignY` at once.

## alignX

Where the text sits across the field when it fits: at the start, the center or the end.
A text at the start, or one wider than the field, scrolls to follow the cursor.

## alignY

Where the text sits in the height of the field: at the top with `marginTop`, the center, or the bottom with `marginBottom`.

## onSubmit(EventListener)

Adds a listener that runs when the user presses Enter or the Enter of the keypad, after the field lost the focus.

## update()

Gives a field of height `0` the height of its font plus its top and bottom margins.

## drawField()

Draws the text or the placeholder between the margins, with the cursor while the field has the focus and the selection.

## isMultiline()

Returns `false`: the text holds one line.

## handleKey(Key)

Handles Enter and the Enter of the keypad: takes the focus, then fires `onSubmit`.

## getPositionAt(double,double)

Returns the index in the text nearest to the pointer, along the line.

## followCursorForward()

Scrolls the text to the left when the cursor passes the right margin.

## followCursorBackward()

Scrolls the text to the right when the cursor passes the left margin.
