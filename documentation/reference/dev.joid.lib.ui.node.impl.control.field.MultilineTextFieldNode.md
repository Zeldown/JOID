---
summary: A field of several lines whose value is its text, wrapped in its width.
---

Enter starts a new line, Up and Down move the cursor between lines, and the wheel scrolls a text taller than the field by one line per notch.

Learn: [Text fields](/use/learn/interactivity/text-fields/#let-the-user-type-several-lines).

## create(double,double,double,double)

Creates the field at `x`, `y` with a `width` and a `height` in design pixels.

## MultilineTextFieldNode(double,double,double,double)

Creates the field, from the constructor of a subclass.

## mouseScroll(NodeMouseScrollEvent)

Over the field, scrolls the text by one line per notch, and consumes the wheel while it can still scroll in that direction.

## drawField()

Draws the wrapped lines or the placeholder inside the margins, with the cursor while the field has the focus and the selection over each line.

## isMultiline()

Returns `true`: the text keeps its line breaks.

## handleKey(Key)

Handles Up and Down, which move the cursor to the nearest place on the line above or below, Home and End, which move it to the start or the end of its line, wrapped lines included, and Enter, which inserts a line break.

## getPositionAt(double,double)

Returns the index in the text nearest to the pointer, on the line under it.

## followCursorForward()

Scrolls the text up when the cursor goes below the visible lines.

## followCursorBackward()

Scrolls the text down when the cursor goes above the visible lines.

## parse(String)

Returns the text itself: every text is a value.

## format(String)

Returns the value itself.
