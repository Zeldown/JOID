# TextFieldNode

Single-line editable text input with cursor, selection, and focus management.

## Create

```java
TextFieldNode.create(x, y, width, height)
    .defaultInfo(TextInfo.create(myFont, 18, Color.WHITE))
    .placeholder("Enter your name…")
    .attach(parent);
```

## Core setters

```java
node.text(String);
node.placeholder(String);
node.placeholder(String, Color color);
node.cursorColor(Color);
node.cursorWidth(double);
node.cursorMargin(double);
node.cursorPosition(int);        // programmatically place the caret
node.defaultInfo(TextInfo);
node.maxLength(int);
node.selectionColor(Color);
node.password(boolean);          // render masked (•)
node.focus(boolean);              // request focus
```

## Callbacks

```java
node.onChange((field, value) -> { /* value changed */ });
node.onFocus((field, focused) -> { /* gained or lost focus */ });
node.onEnter((field, value) -> { /* Enter pressed */ });
```

## Example — email input

```java
final StringSignal email = new StringSignal("");

TextFieldNode.create(40, 40, 400, 40)
    .defaultInfo(TextInfo.create(myFont, 16, Color.WHITE))
    .placeholder("email@example.com")
    .cursorColor(Color.decode("#3b82f6"))
    .selectionColor(Color.decode("#3b82f640"))
    .onChange((field, value) -> email.set(value))
    .attach(parent);
```

Combine with a `RectNode` wrapper for border/background:

```java
RectNode.create(40, 40, 400, 40)
    .color(Color.decode("#1f2937"))
    .effect(RoundedNodeEffect.create(6F))
    .body(wrapper -> {
        TextFieldNode.create(8, 8, 384, 24)
            .defaultInfo(TextInfo.create(myFont, 16, Color.WHITE))
            .placeholder("email@example.com")
            .attach(wrapper);
    })
    .attach(parent);
```

## Keyboard shortcuts

Built-in:

- `←` / `→` — move cursor.
- `Home` / `End` — line start/end.
- `Backspace` / `Delete`.
- `Shift + ←/→/Home/End` — extend selection.
- `Ctrl + A` — select all.
- `Ctrl + C` / `Ctrl + V` / `Ctrl + X` — clipboard.
- `Ctrl + ←/→` — word jump.
- `Enter` — fires `onEnter`.

## Integer-only input

Use the `IntegerFieldNode` subclass for numeric input with validation:

```java
IntegerFieldNode.create(40, 40, 100, 30)
    .min(0)
    .max(100)
    .defaultValue(50)
    .onChange((field, value) -> System.out.println(value))
    .attach(parent);
```

## Best practices

- **Cache `TextInfo`.** One per field is fine; one per frame isn't.
- **Wrap in a container for styling.** The field itself doesn't draw a border — use a `RectNode` parent.
- **Debounce `onChange` for expensive reactions.** It fires on every keystroke.

## See also

- [MultilineTextFieldNode](multiline-text-field.md) — multi-line variant.
- [Signals](../../state/signals.md) — bind field value to state.
