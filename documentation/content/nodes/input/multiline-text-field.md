# MultilineTextFieldNode

`MultilineTextFieldNode` (`dev.joid.lib.ui.node.impl.design.textfield`) is an editable text area: the text wraps to the width of the field, Enter inserts line breaks, and the content scrolls vertically. It shares the editing model of [`TextFieldNode`](text-field.md) (focus, selection, clipboard, filter, maximum length) through their common base, `FieldNode`; this page details what differs.

In the examples, the code runs in `UI.init()` and `font` is an `IFont` you loaded (see [Fonts](../../fonts/adding-fonts.md)).

## Creating a text area

The field has a fixed size and draws no background:

```java
RectNode
.create(610, 340, 700, 400)
.color(Color.DARKGRAY)
.body(background -> {
    MultilineTextFieldNode
    .create(10, 10, 680, 380)
    .info(TextInfo.create(font, 24F, Color.WHITE))
    .placeholder("Write a message")
    .maxTextLength(500)
    .onChange((field, oldText, newText) -> System.out.println(newText.length() + " / 500"))
    .attach(background);
})
.attach(this);
```

![Typing two sentences in a gray text area: Enter starts a new line and the long sentence wraps](../../images/multiline-type.gif "Enter inserts a line break and long lines wrap to the width of the field (0.75× scale).")

- `MultilineTextFieldNode.create(double x, double y, double width, double height)` is the only factory: the height is required and the field never resizes to its text.
- `info(TextInfo)` is required: a field without `TextInfo` throws a `NullPointerException` on its first draw.
- To draw a background or a focus border inside the node, subclass it and draw before `super.draw(mouseX, mouseY)`, as shown for [`TextFieldNode`](text-field.md#styling-the-field).

## Differences from TextFieldNode

| | `TextFieldNode` | `MultilineTextFieldNode` |
| --- | --- | --- |
| Factories | With or without height | With height only |
| Long text | Scrolls horizontally | Wraps, then scrolls vertically |
| Enter / Numpad Enter | Unfocuses and calls `onEnter` | Inserts a line break |
| Escape | Unfocuses and calls `onEnter` | Unfocuses |
| Up / Down | Nothing | Move between lines |
| Mouse wheel | Nothing | Scrolls while the pointer is over it |
| Alignment | `align`, `horizontalAlign`, `verticalAlign` | Always top-left |
| Callbacks | `onChange`, `onFocus`, `onEnter` | `onChange`, `onFocus` |
| Default margins | 2 left / right, 10 top / bottom | 2 on every side |
| `cursorMargin` | 15, kept from both edges | Two line heights, kept from the top edge |
| Word separators (Ctrl) | Spaces | Spaces and line breaks |
| Line breaks in the text | Dropped | Kept, as `\n` |

## Wrapping and line breaks

- The text is laid out in the space between the four margins and clipped to it.
- A line wraps at the last space that fits; that space is not drawn at the start of the next line. A word longer than the line is cut between two characters.
- Only `\n` starts a new line. Every new text (typed, pasted, given to `text(String)`, set through the bound signal or returned by the filter) has its `\r\n` and `\r` stored as `\n`. `<br>` and markup tags are plain text in the field: the text is drawn [without markup](text-field.md#markup) unless `markup(true)`.
- Lines are drawn one line height apart (the `TextInfo` line height).

## Keyboard shortcuts

"Ctrl" is Control, or on macOS Command for Ctrl+A/C/X/V and Option for the word keys, as in [`TextFieldNode`](text-field.md#keyboard-shortcuts); "Shift" is either Shift key. Lines are the drawn lines, wrapping included.

| Keys | Action |
| --- | --- |
| A character key | Inserts the character at the cursor, replacing the selection if there is one. |
| Enter / Numpad Enter | Inserts a line break (`\n`). |
| Left / Right | Moves the cursor one character (a line break is one character) and drops the selection. |
| Up / Down | Moves the cursor to the previous / next line, on the character boundary nearest to its current horizontal position (the end of the line if the line is shorter). Does nothing on the first / last line. Drops the selection. |
| Ctrl+Left / Ctrl+Right | Moves the cursor to the start of the current or previous word / of the next word. |
| Shift + any of the arrows above | Extends the selection (the selection starts at the cursor if there is none). |
| Home / End | Moves the cursor to the start / end of the whole text, not of the line, and drops the selection. |
| Shift+Home / Shift+End | Extends the selection to the start / end of the whole text. |
| Backspace / Delete | Deletes the selection, or else the character before / after the cursor (a line break included). |
| Ctrl+Backspace / Ctrl+Delete | Deletes back to the start of the current or previous word / forward to the start of the next word. |
| Ctrl+A | Selects the whole text. |
| Ctrl+C / Ctrl+X | Copies / cuts the selection, line breaks included. Nothing without a selection. |
| Ctrl+V | Inserts the clipboard text, replacing the selection. Nothing when the clipboard is empty. |
| Escape | Unfocuses the field. In a closeable UI, Escape closes the UI before the field receives it (see the [Escape warning](text-field.md#what-a-focused-field-takes-from-the-ui)). |

- Left, Right, Backspace and Delete repeat while held (first repeat after 500 ms, then every 100 ms); Up and Down do not.
- Every new text keeps line breaks and the characters from U+0020 to U+0233 except U+007F and U+00A7 (`§`); tabs, other control characters and characters above U+0233 are dropped, from the keyboard, the clipboard, `text(String)`, the bound signal and the filter result alike.
- While focused, the field consumes every key, like `TextFieldNode`. Tab, Page Up and Page Down do nothing. There is no undo / redo.

## Mouse and scrolling

- A press on the field (any button) focuses it and puts the cursor on the line under the pointer, at the nearest character boundary. A press above the first line or below the last one picks that line. Shift+press extends the selection; a press without Shift drops it. A press elsewhere unfocuses the field; losing the focus, whatever the cause, drops the selection.
- Each mouse wheel notch over the field scrolls the text by one line height, within the content, focused or not. The field consumes the wheel only while the pointer is over it: elsewhere, a scrollable parent scrolls.
- When the cursor moves, the field scrolls to keep it visible: moving down keeps the cursor line above the bottom margin, moving up keeps at least `cursorMargin` between the top margin and the cursor line. `cursorMargin` defaults to `-1`, which the first draw replaces with two line heights. The cursor at position 0 scrolls back to the top.
- `getYOffset()` returns the vertical scroll offset in UI units.

## Look

The placeholder, cursor and selection follow the same rules as [`TextFieldNode`](text-field.md#styling-the-field): placeholder at `0.5F` alpha while empty and unfocused, a 2-unit-wide fading cursor in the `TextInfo` color, a translucent blue selection. A selection spanning several lines is highlighted line by line; an empty line inside a selection shows a 2-unit-wide mark.

## Callbacks

| Method | Lambda | Called |
| --- | --- | --- |
| `onChange(NodeTextFieldChangeCallback<T>)` | `(node, oldText, newText)` | After the text changed, by the keyboard, the clipboard, `text(String)` or the bound signal; only when the stored text differs. `newText` has passed the filter and the length cut. |
| `onFocus(NodeTextFieldFocusCallback<T>)` | `(node)` | After the focus changed, in both directions: read `node.isFocused()`. |

They use the same interfaces as `TextFieldNode` and can veto the change in their PRE phase the same way (see [Vetoing a change](text-field.md#vetoing-a-change-in-the-pre-phase)). There is no submit key: submit from a button, or read `getText()` in `onFocus` when the field loses the focus.

## Filter and maximum length

`filter(BiFunction<String, String, String>)` and `maxTextLength(int)` behave as in [`TextFieldNode`](text-field.md#filtering-with-filter): the filter receives `(oldText, newText)` for every change and returns the text to keep; the maximum (`-1`, no limit, by default) counts line breaks as characters and cuts every new text after the filter.

```java
MultilineTextFieldNode
.create(610, 340, 700, 400)
.info(TextInfo.create(font, 24F, Color.WHITE))
.filter((oldText, newText) -> newText.split("\n", -1).length <= 10 ? newText : oldText)
.attach(this);
```

This field refuses an eleventh line.

## Binding a signal with signal

`signal(Signal<String>)` keeps the text and a signal in sync, both ways, like [`TextFieldNode.signal(...)`](text-field.md#binding-a-signal-with-signal): the field starts on the signal's text, each change of the text writes into the signal before `onChange` runs, and each value the signal publishes replaces the text while the field's UI is open.

## Reference

### Factory

| Method | Description |
| --- | --- |
| `MultilineTextFieldNode.create(double x, double y, double width, double height)` | Creates a text area of the given size. |

### Properties

| Method | Default | Description |
| --- | --- | --- |
| `text(String)` | `""` | Replaces the text through the filter and the length cut; calls `onChange` if it changed. Does not move the cursor. |
| `placeholder(String)` | `""` | Text shown while empty and unfocused. |
| `info(TextInfo)` | none, required | Font, size and color of the text, placeholder and cursor. |
| `focused(boolean)` | `false` | Focuses or unfocuses the field; calls `onFocus` when the state changes. |
| `filter(BiFunction<String, String, String>)` | `(oldText, newText) -> newText` | Text filter. |
| `maxTextLength(int)` | `-1` | Maximum length, `-1` for none. |
| `markup(boolean)` | `false` | Draws the text with the markups of the `TextInfo`. |
| `signal(Signal<String>)` | none | Binds a signal to the text, both ways. |
| `margin(double)` | `2` | Sets the four margins. |
| `margin(double margin, double cursorMargin)` | | Sets the four margins and `cursorMargin`. |
| `marginHorizontal(double)`, `marginVertical(double)` | `2` | Left and right margins, or top and bottom margins. |
| `marginTop(double)`, `marginLeft(double)`, `marginRight(double)`, `marginBottom(double)` | `2` | One margin. |
| `cursorMargin(double)` | `-1` (two line heights) | Distance kept between the top margin and the cursor line when scrolling up. |
| `cursorPosition(int)` | `0` | Cursor index, clamped to `[0, text length]`. |
| `onChange(NodeTextFieldChangeCallback<T>)` | | Adds a change callback. |
| `onFocus(NodeTextFieldFocusCallback<T>)` | | Adds a focus callback. |

Every setter returns the node itself, typed by the generic return of the fluent API.

### Getters

| Method | Description |
| --- | --- |
| `getText()`, `getValue()` | Current text. |
| `getPlaceholder()`, `getInfo()` | Placeholder and `TextInfo`. |
| `isFocused()` | Whether the field has the keyboard. |
| `getCursorPos()` | Cursor index in the text. |
| `getSelectionStart()` | Selection anchor index, `-1` without selection. |
| `getMaxTextLength()`, `getFilter()` | Maximum length and filter. |
| `isMarkup()`, `getSignal()` | Whether the text is drawn with markup, and the bound signal or `null`. |
| `getMarginTop()`, `getMarginLeft()`, `getMarginRight()`, `getMarginBottom()`, `getCursorMargin()` | Margins. |
| `getYOffset()` | Vertical scroll offset. |
| `getInputType()`, `getLastInput()`, `isInputting()`, `isFirstInput()` | State of the held-key repeat. |

## See also

- [TextFieldNode](text-field.md)
- [Mouse and Keyboard](../../interactions/mouse-and-keyboard.md)
- [Callbacks](../../interactions/callbacks.md)
- [Text Model](../../text/text-and-textinfo.md)