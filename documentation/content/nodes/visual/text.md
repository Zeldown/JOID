# TextNode

`TextNode` displays a `Text` and sizes itself to it. Its `TextMode` chooses between a single line, a truncated line, wrapped lines that grow the node, or wrapped lines kept inside a fixed box.

In the examples, `font` is a font loaded once at startup with `MsdfFontLoader` (see [Text and Fonts](../../concepts/text.md)).

```java
TextNode.create(100, 100).text(Text.create("Hello JOID", TextInfo.create(font, 24F, Color.WHITE))).attach(this);
```

![The words Hello JOID in white](../../images/text-hello.png "A TextNode sized by its text (Montserrat, 24).")

`TextNode.create(x, y)` creates a node whose width and height follow the text. `TextNode.create(x, y, width, height)` gives it a box; a dimension given as `0` still follows the text.

## Labels in a box with Align

Give the node the box of its parent and align the text on both axes:

```java
RectNode
.create(100, 100, 300, 60)
.color(Color.DARKGRAY)
.body(rect -> {
	TextNode.create(0, 0, rect.getWidth(), rect.getHeight()).text(Text.create("Play", TextInfo.create(font, 24F, Color.WHITE), Align.CENTER, Align.CENTER)).attach(rect);
})
.attach(this);
```

![A gray button with the centered label Play](../../images/text-button.png "The TextNode covers the button and centers its text on both axes.")

## Dynamic text

Write the text as an expression that reads a [signal](../../concepts/state.md): the text follows it and the node resizes to the new text.

```java
private final IntegerSignal score = IntegerSignal.of(0);

@Override
public void init() {
	TextNode.create(20, 20).text(Text.create("Score: " + this.score.get(), TextInfo.create(font, 20F, Color.WHITE))).attach(this);
}
```

![A score text counting up as the score signal changes](../../images/text-signal.gif "The text follows the signal read in its expression.")

For a value that changes on every frame, such as a clock, pass a lambda: `Text.create(() -> "Open for " + this.seconds() + " s", info)`.

## Text modes with mode

`mode(TextMode)` decides how the text is laid out and which dimensions follow it.

| Mode | Layout | Size of the node |
|---|---|---|
| `NORMAL` (default) | One run, placed in the box by the text's alignment. | Width and height follow the text when `0`. |
| `OVERFLOW` | One line cut to the width, with the text's `TextOverflow` suffix (`ELLIPSIS`, `DOT`, `HYPHEN`, `NONE`). | Height follows the text. Give the width. |
| `SPLIT` | Wrapped to the width at spaces, `\n` and `<br>`. | Height is the height of the lines. Give the width. |
| `BOX` | Wrapped like `SPLIT`, then aligned in the box; lines that do not fit are not drawn. | Never changed. Give both dimensions. |

![The same sentence in a 220 × 60 box with the modes NORMAL, OVERFLOW, SPLIT and BOX](../../images/text-modes.png "NORMAL runs past the box, OVERFLOW cuts it, SPLIT wraps and grows, BOX wraps and drops the line that does not fit.")

```java
TextNode
.create(0, 0, 300, 0)
.text(Text.create("A very long subtitle that does not fit", TextInfo.create(font, 20F, Color.WHITE)).overflow(TextOverflow.ELLIPSIS))
.mode(TextMode.OVERFLOW)
.attach(this);

TextNode
.create(0, 40, 300, 0)
.text(Text.create("A paragraph wrapped on as many lines as it needs.", TextInfo.create(font, 20F, Color.WHITE)))
.mode(TextMode.SPLIT)
.attach(this);
```

![A subtitle cut with an ellipsis above a paragraph wrapped on two lines](../../images/text-overflow-split.png "OVERFLOW cuts the line at 300 units, SPLIT wraps the paragraph.")

## Effects on text

Node effects apply to the drawn glyphs, for example an outline:

```java
TextNode.create(0, 0).text(Text.create("Outlined", TextInfo.create(font, 50F, Color.WHITE))).effect(BorderNodeEffect.create(Color.BLACK, 2F)).attach(this);
```

![The word Outlined in white with a thin black outline, on a light background](../../images/text-outline.png "BorderNodeEffect outlines the glyphs.")

## Reference

| Method | Default | Description |
|---|---|---|
| `create(x, y)`, `create(x, y, width, height)` | | Node sized by its text, or with a box (`0` follows the text). |
| `text(Text)`, `text(Supplier<Text>)` | `null` | Text to display; `text((Text) null)` clears it. |
| `mode(TextMode)`, `mode(Supplier<TextMode>)` | `NORMAL` | Layout mode. |
| `reset()` | | Freezes the current size: non-zero dimensions stop following the text. |
| `getText()`, `getMode()` | | Current text and mode. |

## Good to know

- A `Text` whose content and `TextInfo` both read signals is rebuilt as a whole: keep signals out of the info, or use a lambda.
- `text(null)` does not compile: write `text((Text) null)`.
- A node sized by its text changes width with it: anchor it (`anchorX(Align.CENTER)`) to keep its center in place.

## See also

- Next: [ResourceNode](resource.md)
- [Text and Fonts](../../concepts/text.md)
- [TextFieldNode](../input/text-field.md) for editable text
- [Effects](../../styling/effects.md)