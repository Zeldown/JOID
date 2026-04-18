# MultilineTextFieldNode

Multi-line editable text input. Wraps long text to the node's width, supports scroll via the mouse wheel, and has full clipboard / selection / navigation like `TextFieldNode`.

## Create

```java
MultilineTextFieldNode.create(x, y, width, height)
```

Only one factory — `height` is always explicit because line wrapping depends on it.

## API

```java
T text(String text)
T placeholder(String placeholder)
T info(TextInfo textInfo)

T focused(boolean focused)
T filter(BiFunction<String, String, String> filter)
T maxTextLength(int maxTextLength)                         // -1 = unlimited

T margin(double margin)
T margin(double margin, double cursorMargin)
T marginTop(double margin)
T marginBottom(double margin)
T marginLeft(double margin)
T marginRight(double margin)
T cursorMargin(double cursorMargin)
T cursorPosition(int cursorPos)
```

Defaults: `margin = 2` on all sides, `cursorMargin = -1` (interpreted as `lineHeight * 2` on the first draw).

There is no `lineHeight`, `maxLines`, alignment, `cursorColor`, or `selectionColor` setter. Line height equals `info.getHeight()`, text is always left-aligned, and both cursor and selection use fixed colours.

## Callbacks

```java
T onChange(NodeTextFieldChangeCallback<T> callback)       // (node, oldText, newText)
T onFocus(NodeTextFieldFocusCallback<T> callback)         // (node)
```

There is **no** `onEnter` on `MultilineTextFieldNode` — pressing Enter inserts a newline character (both `Enter` and `Numpad Enter`).

## Keyboard & mouse

- `←` / `→` — move cursor on the current line.
- `↑` / `↓` — move cursor to the previous / next visual line, keeping the horizontal offset.
- `Home` / `End` — cursor to 0 / end of text.
- `Backspace` / `Delete` — delete character or selection.
- `Shift + ← / → / ↑ / ↓` — extend selection.
- `Ctrl + A` — select all.
- `Ctrl + C` / `Ctrl + V` / `Ctrl + X` — clipboard.
- `Enter` / `Numpad Enter` — insert newline.
- `Esc` — unfocus.
- Mouse wheel — scrolls the text vertically by one line per tick.

## Example — note editor

```java
final StringSignal note = new StringSignal("");

RectNode.create(40, 40, 600, 400)
    .color(Color.decode("#1f2937"))
    .effect(RoundedNodeEffect.create(8F))
    .body(wrapper -> {
        MultilineTextFieldNode.create(12, 12, 576, 376)
            .info(TextInfo.create(myFont, 14, Color.WHITE))
            .placeholder("Start typing…")
            .maxTextLength(10_000)
            .onChange((field, oldText, newText) -> note.set(newText))
            .attach(wrapper);
    })
    .attach(parent);
```

## See also

- [TextFieldNode](text-field.md) — single-line variant.