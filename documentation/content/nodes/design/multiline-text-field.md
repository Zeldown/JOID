# MultilineTextFieldNode

Multi-line editable text input. Same API as `TextFieldNode` plus line-wrapping and vertical navigation.

## Create

```java
MultilineTextFieldNode.create(x, y, width, height)
    .defaultInfo(TextInfo.create(myFont, 16, Color.WHITE))
    .placeholder("Type your description…")
    .attach(parent);
```

## API

Identical to [TextFieldNode](text-field.md), plus:

```java
node.lineHeight(double);      // px between lines (default = font size * 1.3)
node.maxLines(int);           // auto-scroll after this many lines
```

All setters return the node for chaining.

## Callbacks

Same as `TextFieldNode`. `onEnter` fires on `Enter` unless shift is held (Shift+Enter inserts a newline).

## Keyboard shortcuts

All `TextFieldNode` shortcuts plus:

- `↑` / `↓` — move between lines.
- `Shift + ↑/↓` — extend selection across lines.
- `Shift + Enter` — new line.
- `Enter` — fires `onEnter`.

## Scrolling

When content exceeds `height`, set the parent's `overflow` to `SCROLL` and attach a `ScrollbarNode`:

```java
RectNode.create(40, 40, 400, 300)
    .color(Color.decode("#1f2937"))
    .effect(RoundedNodeEffect.create(8F))
    .overflow(OverflowProperty.SCROLL)
    .body(wrapper -> {
        MultilineTextFieldNode.create(12, 12, 376, 276)
            .defaultInfo(info)
            .placeholder("Long description…")
            .attach(wrapper);

        ScrollbarNode.create(388, 12, 8, 276).attach(wrapper);
    })
    .attach(parent);
```

## Example — note-taking widget

```java
final StringSignal note = new StringSignal("");

MultilineTextFieldNode.create(0, 0, 600, 400)
    .defaultInfo(info)
    .placeholder("Start typing…")
    .maxLength(10_000)
    .onChange((field, value) -> note.set(value))
    .attach(parent);
```

## See also

- [TextFieldNode](text-field.md).
- [ScrollbarNode](../structure/scrollbar.md).
