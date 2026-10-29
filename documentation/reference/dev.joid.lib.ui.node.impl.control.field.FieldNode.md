---
summary: The base of every text field: it edits a text, reads a value of type `V` from it, and handles the focus, the cursor, the selection and the clipboard.
---

A field turns its text into a value with `parse` and a value into its text with `format`; [[TextFieldNode]], [[IntegerFieldNode]] and [[MultilineTextFieldNode]] are ready to use, and a field of your own type extends [[LineFieldNode]].
The field draws its text itself; its only part is an optional `background`.
A held Backspace, Delete or arrow key repeats after 500 ms, then every 100 ms.
Attaching a field without a [[TextInfo]] throws an `IllegalStateException`.

Learn: [UI kit](/use/learn/going-further/ui-kit/#know-the-parts-of-each-control), [Text fields](/use/learn/interactivity/text-fields/).

## FieldNode(double,double,double,double)

Creates the field at `x`, `y` with a `width` and a `height` in design pixels, from the constructor of a subclass.

## background

The node drawn behind the text, over the whole field, with a `zindex` of `-1`; optional.

Default: `null`: no background

## text

The text in the field.
A plain text is the start value, and a signal binds the text both ways: the field shows the signal and writes each edit into it.
A text from code is cleaned of control characters and cut to `maxTextLength`; it is never refused.

## placeholder

The text shown while the field is empty and has no focus.

## info

The font, the size and the color of the text, as a [[TextInfo]]; required.

Default: none, `info(...)` is required before the attach

## cursorColor

The color of the blinking cursor; `null` uses the color of `info`.

Default: `null`: the color of `info`

## selectionColor

The color of the rectangle drawn over the selected text.

## placeholderColor

The color of the placeholder; `null` uses the color of `info` at half its opacity.

Default: `null`: the color of `info` at half its opacity

## accept

A test that every edit of the user must pass: an edit typed, pasted, cut or deleted whose text fails it is refused, and the text stays as it was.
A text that fails it is not a valid value.

## normalize

A function applied to the text before the field reads a value from it, such as `String::trim`.
The field shows the normalized text when it loses the focus, and at once for a text given by a setter or a signal.

## allowEmpty

If `true`, an empty field is valid and its value is `null`.
If `false`, the empty text goes through `parse`: a [[TextFieldNode]] then holds `""`, and a field whose `parse` returns `null` for it, such as [[IntegerFieldNode]], takes back its last valid value when it loses the focus.

## maxTextLength

The largest number of characters of the text; `-1` means no limit.
Typing and pasting stop at the limit, and a longer text from code is cut.

Default: no limit

## markup

If `true`, the text is read with the markups of `info`, so that the tags in the text style it; if `false`, the field shows the tags as typed.

## margin

Sets the four margins at once.

## marginTop

The space, in design pixels, between the top edge and the text; a one-line field uses it with `alignY(START)` and to size a field of height `0`.

## marginBottom

The space, in design pixels, between the bottom edge and the text; a one-line field uses it with `alignY(END)` and to size a field of height `0`.

## marginLeft

The space, in design pixels, between the left edge and the text; the text is clipped there.

## marginRight

The space, in design pixels, between the right edge and the text; the text is clipped there.

## marginHorizontal

Sets the left and the right margins at once.

## marginVertical

Sets the top and the bottom margins at once.

## cursorMargin

The distance, in design pixels, that the field keeps between the cursor and the edge of the visible text when it scrolls to follow the cursor.

## cursorPosition

The index of the cursor in the text, kept within the text; a plain index is the start value.
`getCursorPosition()` reads it.

## cursorPosition(Signal)

Binds the index of the cursor to a signal both ways.
Throws an `IllegalArgumentException` for a signal that cannot be written.

## moveCursor(int)

Moves the cursor as if the user did: `beforeCursorMove` can refuse it, then `onCursorMove` fires.

## beforeCursorMove(EventListener)

Adds a listener that runs before the cursor moves on a key, a press or `moveCursor(...)`; `cancel()` keeps it in place.

## onCursorMove(EventListener)

Adds a listener that runs after each move of the cursor, whatever its source.

## focused(boolean)

Gives the field its start focus: `true` gives it the keyboard.

## focused(Signal)

Binds the focus to a signal both ways: the field gains or loses the focus with the signal, and writes into it when the user focuses or blurs the field.
Throws an `IllegalArgumentException` for a signal that cannot be written.

## focus()

Gives the field the focus: `beforeFocus` can refuse it, then `onFocus` fires; unlike a press, it does not blur the other fields.

## blur()

Blurs the field as a press elsewhere would: `beforeBlur` can refuse it, then `onBlur` fires.

## beforeFocus(EventListener)

Adds a listener that runs before the field gains the focus on a press or `focus()`; `cancel()` refuses the focus.

## beforeBlur(EventListener)

Adds a listener that runs before the field loses the focus on a press elsewhere, Escape, Enter or `blur()`; `cancel()` keeps the focus.

## onFocus(EventListener)

Adds a listener that runs each time the field gains the focus, whatever its source.

## onBlur(EventListener)

Adds a listener that runs each time the field loses the focus, after it rewrote its text from its value.

## isFocused()

If `true`, the field has the focus.

## getValue()

Returns the value read from the text, normalized and brought into its bounds.
A text that is not a value gives the last valid value; an empty text with `allowEmpty(true)` gives `null`.

## isValid()

If `true`, the text passes `accept`, reads as a value and that value is already within its bounds; an empty text with `allowEmpty(true)` is valid.

## getSelection()

Returns the selected range of the text; an empty range when nothing is selected.

## selection(int,int)

Gives the field its start selection, from `start` to `end` in any order, kept within the text.
A loss of the focus clears it, even while the text is too short to hold it.

## selection(Signal)

Binds the selection to a signal of [[FieldSelection]] both ways.
Throws an `IllegalArgumentException` for a signal that cannot be written.

## select(int,int)

Selects a range as if the user did: `beforeSelect` can refuse it, then `onSelect` fires.

## beforeSelect(EventListener)

Adds a listener that runs before the selection changes on a key, a press or `select(...)`; `cancel()` keeps the selection.

## onSelect(EventListener)

Adds a listener that runs after each change of the selection, whatever its source.

## change(String)

Replaces the text as if the user typed it: `beforeChange` can refuse it, then `onChange` fires.

## beforeChange(EventListener)

Adds a listener that runs before an edit of the user or `change(...)`, with the text now, the text asked for and the value read from it; `cancel()` refuses the edit.

## onChange(EventListener)

Adds a listener that runs after each real change of the text, whatever its source.

## layout()

Places the background over the whole field.

## draw(double,double)

Draws the field with `drawField()`, then repeats a held editing key.
An override calls `super.draw(...)`.

## mousePressed(NodeMouseEvent)

On a press over the field that no one consumed, blurs the other fields, focuses this one, places the cursor and starts a selection, and consumes the press; a double press selects a word, a triple press a line.
Any other press blurs the field.

## mouseReleased(NodeMouseEvent)

Ends the selection that a press started.

## mouseDragged(NodeMouseEvent)

Extends the selection to the character under the pointer.

## mouseScroll(NodeMouseScrollEvent)

Over the focused field, steps the value up or down with `increment` when its type has steps, and consumes the wheel.

## charTyped(NodeCharEvent)

While the field has the focus, inserts the character at the cursor, in place of the selection, and consumes it.
A character typed with Ctrl (Cmd on macOS) and without Alt is a shortcut and is not inserted.

## keyPressed(NodeKeyEvent)

While the field has the focus, handles the editing, the cursor and the clipboard keys and consumes every key; Escape blurs the field.
It asks `handleKey` first.

## detach()

Blurs the field when it leaves the tree.

## drawField()

Hook that draws the text, the cursor and the selection, on every frame.

## isMultiline()

Hook that returns `true` when the text may hold line breaks; a one-line field drops them from a paste.

## handleKey(Key)

Hook that JOID calls first with each key pressed while the field has the focus.
Return `true` when the key is handled: the field does nothing else with it, and repeats it while it is held.

## getPositionAt(double,double)

Hook that returns the index in the text under a point of the UI, in design pixels.

## followCursorForward()

Hook that scrolls the visible text after the cursor moved forward, so that the cursor stays in view with `cursorMargin`.

## followCursorBackward()

Hook that scrolls the visible text after the cursor moved backward, so that the cursor stays in view with `cursorMargin`.

## parse(String)

Hook that turns a text into a value; return `null` when the text is not a value.

## format(V)

Hook that turns a value into the text that the field shows.

## correct(V)

Hook that brings a value into its bounds, such as a minimum and a maximum; the default returns the value.
A text whose value changes here is not valid while typed, and shows the corrected value when the field loses the focus.

## accepts(String)

Hook that refuses an edit whose text can never become a value, such as a letter in a number field; the default accepts every text.
An edit passes only when both `accepts` and `accept` let it through.

## increment(V,int)

Hook that returns the value `count` steps away, for Up, Down and the wheel; `count` is negative to go down.
The default returns `null`: the type has no steps.

## fallback(V)

Gives the value that the field holds while no text was valid yet, such as `0` for a number.

## startValue(V)

Gives the field a start value of its type, shown through `format`: the body of a public `value(...)` setter.

## bindValue(Signal)

Binds the value to a signal of its type both ways: the field shows the signal through `format`, and writes the value of each valid edit into it.
Throws an `IllegalArgumentException` for a signal that cannot be written.

## write(V,boolean)

Shows a value, brought into its bounds and formatted.
If `refusable` is `true`, the write acts like the user and goes through `beforeChange`: the body of a public `change(...)`.

## revalidate()

Shows again the value asked for last, after a bound of the field changed, such as its `min` or its `max`.

## blurNow()

Blurs the field at once.

## insert(String)

Inserts a text at the cursor, in place of the selection, as typing would: cleaned of control characters, cut to `maxTextLength`, and tested by `accept` and `accepts`.
Returns `true` when the text changed.

## placeCursor(int,boolean)

Moves the cursor to an index and scrolls to keep it in view; with Shift held, extends the selection.
`forward` is `true` for a move toward the end.

## increaseCursor(int)

Moves the cursor forward by a number of characters and scrolls to keep it in view.

## decreaseCursor(int)

Moves the cursor backward by a number of characters and scrolls to keep it in view.

## getShownInfo()

Returns the [[TextInfo]] that draws the text: `info` with its markups, or without them when `markup` is `false`.

## getPlaceholderInfo()

Returns the [[TextInfo]] that draws the placeholder, in the color of `placeholderColor`.

## getShownCursorColor()

Returns the color of the cursor on this frame, with the opacity of its blink.

## getLineX(String,int[],int,int,String,int)

Returns the horizontal offset, in design pixels, of a position within a line of the text from `start` to `end`, with the markup tags given by `tags` and a `prefix` of opened tags.

## getLinePosition(String,int[],int,int,String,double)

Returns the index of the line from `start` to `end` that is nearest to a horizontal offset, the inverse of `getLineX`.

## holdsInput()

If `true`, the field has the focus or a selection by drag goes on: it keeps its keys and its drags while it is scrolled out of the area of its container.
