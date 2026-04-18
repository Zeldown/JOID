# TextFieldNode

Single-line editable text input. The node owns its own text, cursor, and selection state, and handles keyboard navigation, clipboard, and mouse clicks. Background and border are drawn by the container, not by the field itself.

## Create

```java
TextFieldNode.create(x, y, width)                 // height auto-computed from info
TextFieldNode.create(x, y, width, height)         // explicit height
```

Both factories return a `TextFieldNode`. When `height` is `0`, the node resizes to `info.getHeight() + marginVertical * 2` on the first draw.

## API

```java
T text(String text)
T placeholder(String placeholder)
T info(TextInfo textInfo)

T align(Align horizontal, Align vertical)
T horizontalAlign(Align align)
T verticalAlign(Align align)

T focused(boolean focused)
T filter(BiFunction<String, String, String> filter)      // (oldText, newText) -> newText
T maxTextLength(int maxTextLength)                         // -1 = unlimited

T margin(double margin)
T margin(double margin, double cursorMargin)
T marginHorizontal(double margin)
T marginVertical(double margin)
T marginLeft(double margin)
T marginRight(double margin)
T marginTop(double margin)
T marginBottom(double margin)
T cursorMargin(double cursorMargin)
T cursorPosition(int cursorPos)
```

Defaults: `marginHorizontal = 2`, `marginVertical = 10`, `cursorMargin = 15`, `horizontalAlignment = START`, `verticalAlignment = CENTER`.

There is no `cursorColor`, `cursorWidth`, `selectionColor`, or `password` setter — the cursor is drawn using the current `info` colour with a sine-pulsed alpha, and the selection colour is hard-coded to `(50, 152, 253, 100)`.

## Callbacks

```java
T onChange(NodeTextFieldChangeCallback<T> callback)       // (node, oldText, newText)
T onFocus(NodeTextFieldFocusCallback<T> callback)         // (node)
T onEnter(NodeTextFieldEnterCallback<T> callback)         // (node, text)
```

`onEnter` fires on `ENTER`, `NUMPAD_ENTER`, and `ESC` — each of those keys also unfocuses the field.

## Keyboard shortcuts

Handled inside `keyPressed`:

- `←` / `→` — move cursor (hold to auto-repeat).
- `Home` / `End` — line start / end.
- `Backspace` / `Delete` — delete character or selection.
- `Shift + ←/→` — extend selection.
- `Ctrl + A` — select all.
- `Ctrl + C` / `Ctrl + V` / `Ctrl + X` — clipboard.
- `Enter` / `Numpad Enter` / `Esc` — unfocus and fire `onEnter`.

## Example — email input

```java
final StringSignal email = new StringSignal("");

TextFieldNode.create(40, 40, 400, 40)
    .info(TextInfo.create(myFont, 16, Color.WHITE))
    .placeholder("email@example.com")
    .onChange((field, oldText, newText) -> email.set(newText))
    .attach(parent);
```

Wrap in a `RectNode` for background and border:

```java
RectNode.create(40, 40, 400, 40)
    .color(Color.decode("#1f2937"))
    .effect(RoundedNodeEffect.create(6F))
    .body(wrapper -> {
        TextFieldNode.create(8, 0, 384)
            .info(TextInfo.create(myFont, 16, Color.WHITE))
            .placeholder("email@example.com")
            .attach(wrapper);
    })
    .attach(parent);
```

## `IntegerFieldNode`

Sub-class of `TextFieldNode` that keeps its content as an integer within a `[min, max]` range:

```java
IntegerFieldNode.create(x, y, width)
IntegerFieldNode.create(x, y, width, height)

T min(int minValue)
T max(int maxValue)
T range(int minValue, int maxValue)
T value(int value)
int getValue()
```

The subclass installs a `filter` that strips non-digit characters and clamps the parsed value to `[min, max]`. `getValue()` returns the integer parse of the current text.

## See also

- [MultilineTextFieldNode](multiline-text-field.md) — multi-line variant.
- [Signals](../../state/signals.md) — bind field value to state.