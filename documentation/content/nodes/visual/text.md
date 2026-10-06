# TextNode

`TextNode` (`dev.joid.lib.ui.node.impl.design.text`) displays a [`Text`](../../text/text-and-textinfo.md) and sizes itself to it. A `TextMode` chooses between a single line, a truncated line, wrapped lines growing the node, or wrapped lines kept inside a fixed box.

In the examples, `font` is an `IFont` loaded at startup (see [Fonts](../../fonts/adding-fonts.md)).

## Creating a TextNode

```java
TextNode.create(100, 100).text(Text.create("Hello JOID", TextInfo.create(font, 24, Color.WHITE))).attach(this);
```

![The words Hello JOID in white](../../images/text-hello.png "A TextNode sized by its text (Montserrat, 24).")

- `TextNode.create(x, y)` creates a node of size 0×0: its width and height follow the text.
- `TextNode.create(x, y, width, height)` creates a node with a given box; a dimension given as `0` still follows the text.

A label centered in a button:

```java
RectNode.create(100, 100, 300, 60).color(Color.DARKGRAY).body(rect -> {
    TextNode
        .create(0, 0, rect.getWidth(), rect.getHeight())
        .text(Text.create("Play", TextInfo.create(font, 24, Color.WHITE), Align.CENTER, Align.CENTER))
        .attach(rect);
}).attach(this);
```

![A gray button with the centered label Play](../../images/text-button.png "The TextNode covers the button and centers its text on both axes.")

`Text` (`dev.joid.lib.draw.text.builder`), `TextInfo` (`dev.joid.lib.font.dto`), `Align` (`dev.joid.lib.utils.align`), overflow suffixes, modifiers and multi-style texts are described in [Text Model](../../text/text-and-textinfo.md).

## Dynamic text

The node reads its `Text` on every frame. Build the text from a supplier and the node shows (and resizes to) the current value without any rebuild:

```java
final IntegerSignal score = new IntegerSignal(0);

TextNode.create(20, 20).text(Text.create(() -> "Score: " + score.getOrDefault(), TextInfo.create(font, 20, Color.WHITE))).attach(this);
```

You can also change the content in place with `getText().text("...")`, or give the node another `Text` with `text(...)`. See [Signals](../../state/signals.md) and [Watching Signals](../../state/watch.md) for the other ways to react to state.

## Text modes with mode

`mode(TextMode)` (`dev.joid.lib.draw.text.utils.TextMode`) decides how the text is laid out in the node and which dimensions follow the text.

| Mode | Layout | Size of the node |
| --- | --- | --- |
| `NORMAL` (default) | One run, placed in the node's box according to the text's horizontal and vertical alignment. Never wrapped nor clipped. | Width and height follow the text when they were `0`. |
| `OVERFLOW` | One line cut to the node's width. When the text is cut, the text's `TextOverflow` suffix is appended (`ELLIPSIS` `...`, `DOT` `.`, `HYPHEN` `-`, `NONE` nothing). | Height follows the text when it was `0`. Give the width. |
| `SPLIT` | Wrapped to the node's width. Lines break at spaces, `\n`, `\r\n` and `<br>`; a word wider than the node is broken where it overflows. | Height is always the total height of the lines, even when a height was given. Give the width. |
| `BOX` | Wrapped like `SPLIT`, then placed in the node's box according to the text's alignment. Lines that do not fit entirely inside the box are not drawn. | Never changed. Give both dimensions. |

![The same sentence in a 220 × 60 box with the modes NORMAL, OVERFLOW, SPLIT and BOX](../../images/text-modes.png "NORMAL runs past the box, OVERFLOW cuts it with an ellipsis, SPLIT wraps and grows, BOX wraps and drops the line that does not fit.")

```java
TextNode
    .create(0, 0, 300, 0)
    .text(Text.create("A very long subtitle that does not fit", TextInfo.create(font, 20, Color.WHITE)).overflow(TextOverflow.ELLIPSIS))
    .mode(TextMode.OVERFLOW)
    .attach(this);

TextNode
    .create(0, 40, 300, 0)
    .text(Text.create("A paragraph wrapped on as many lines as it needs.", TextInfo.create(font, 20, Color.WHITE)))
    .mode(TextMode.SPLIT)
    .attach(this);
```

![A subtitle cut with an ellipsis above a paragraph wrapped on two lines](../../images/text-overflow-split.png "OVERFLOW cuts the line at 300 units and appends ..., SPLIT wraps the paragraph; the darker area marks the 300-unit width.")

## Automatic sizing

A dimension is automatic when it is `0` the first time the node is initialized, that is when it joins a UI (attached to the UI, or to a parent that is in a UI). Sizes you set before that moment count as given sizes. The node then:

- measures the text when it is initialized and on every [reload](../node-fundamentals.md), and again after each draw, so a supplier-driven text resizes the node frame by frame;
- sizes itself on its first draw when the text is given after initialization;
- keeps its size and draws nothing while the text is `null`, has no element, or is an empty string.

`getInitialWidth()` and `getInitialHeight()` return the size recorded at initialization (`0` for an automatic dimension), and `isInitialized()` tells whether it was recorded.

### Freezing a size with reset

`reset()` records the current size as the initial size: every non-zero dimension becomes a given size and stops following the text. Use it after resizing an initialized node yourself, or to freeze an automatic size:

```java
label.width(400D);
label.reset();
```

## Loading skeleton

While the node waits for a condition set with `wait(...)`, it takes the size of its text (when it has one) for the dimensions that are still `0`, and draws the default pulsing grey placeholder over it (see [Node Fundamentals](../node-fundamentals.md)).

## Effects on text

Node effects apply to the drawn glyphs. For example, an outline:

```java
TextNode.create(0, 0).text(Text.create("Outlined", TextInfo.create(font, 50, Color.WHITE))).effect(BorderNodeEffect.create(Color.BLACK, 2F)).attach(this);
```

![The word Outlined in white with a thin black outline, on a light background](../../images/text-outline.png "The BorderNodeEffect outlines the glyphs (shown on a light background so the black outline stands out).")

See [Effects](../../styling/effects.md).

## Reference

### Factories

| Method | Description |
| --- | --- |
| `TextNode.create(double x, double y)` | Creates a node sized by its text. |
| `TextNode.create(double x, double y, double width, double height)` | Creates a node with a given box; a `0` dimension follows the text. |

### Properties

| Method | Default | Description |
| --- | --- | --- |
| `text(Text text)` | `null` | Text to display. `null` clears it. |
| `mode(TextMode mode)` | `TextMode.NORMAL` | Layout mode, see the table above. |
| `reset()` | | Records the current size as the initial size. |

Every setter returns the node itself, typed by the generic return of the fluent API.

### Getters

| Method | Description |
| --- | --- |
| `getText()` | The displayed `Text`, or `null`. |
| `getMode()` | The current `TextMode`. |
| `isInitialized()` | `true` once the initial size is recorded. |
| `getInitialWidth()` / `getInitialHeight()` | The recorded initial size; `0` means automatic. |

## See also

- [Text Model](../../text/text-and-textinfo.md)
- [Fonts](../../fonts/adding-fonts.md)
- [Markup and Text Effects](../../text/markup-and-effects.md)
- [Drawing Text](../../drawing/text.md) for drawing text without a node
- [TextFieldNode](../input/text-field.md) for editable text