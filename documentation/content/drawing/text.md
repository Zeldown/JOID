# Drawing Text

`DrawText` (`dev.joid.lib.draw.text`) draws a string or a `Text` immediately, through `DrawUtils.TEXT`: at a point with an alignment, or inside a box where it can be cut or wrapped. It is what `TextNode` draws with; use it in your own draw hooks (see [Drawing Overview](draw-utils.md)).

## Quick example

```java
private final TextInfo label = TextInfo.create(Fonts.INTER, 18, Color.WHITE);

@Override
public void postDraw(final double mouseX, final double mouseY) {
    DrawUtils.TEXT.drawText(960D, 40D, "Paused", this.label, Align.CENTER, Align.START);
    DrawUtils.TEXT.drawText(40D, 100D, 400D, 200D, this.description, this.label, Align.START, Align.START, TextOverflow.NONE, TextMode.SPLIT);
}
```

The first call centers "Paused" horizontally on x = 960 with its top at y = 40. The second wraps the description into lines of at most 400 units, from the top-left corner of the box. The building blocks (`Text`, `TextInfo`, `TextMode`, `TextOverflow`) are described on [Text Model](../text/text-and-textinfo.md).

## DrawText reference

| Method | Description |
|---|---|
| `drawText(double x, double y, String text, TextInfo info, Align horizontalAlign, Align verticalAlign)` | Draws one line at a point. |
| `drawText(double x, double y, Text text)` | Draws a `Text` at a point, with its own alignment. |
| `drawText(double x, double y, double width, double height, String text, TextInfo info, Align horizontalAlign, Align verticalAlign, TextOverflow overflow, TextMode mode)` | Draws a string inside a box. |
| `drawText(double x, double y, double width, double height, Text text, TextMode mode)` | Draws a `Text` inside a box, with its own alignment and overflow mark. |
| `getLines(double width, String text, TextInfo info)` | Splits a string into the lines `SPLIT` would draw. |
| `getLines(double width, Text text)` | Splits a `Text` into one `Text` per line. |
| `DrawText.getInstance()` | The instance behind `DrawUtils.TEXT`. |

Every `drawText` returns the `FontBounds` of what it drew, and draws nothing (returning empty bounds) for a `Text` without element. The string overloads wrap the string in a one-element `Text` with the given alignment (and overflow mark).

## Drawing at a point

`drawText(x, y, text)` uses the alignment of the text to place it around the point:

| Alignment | Horizontal | Vertical |
|---|---|---|
| `START` | Left edge on `x` | Top on `y` |
| `CENTER` | Center on `x` | Middle on `y` |
| `END` | Right edge on `x` | Bottom on `y` |

The runs of the text are drawn one after the other on the same line. A run shorter than the tallest one is placed inside the line height by the vertical alignment: on top for `START`, centered for `CENTER`, at the bottom for `END`. The returned bounds are `text.getBounds()`: the sum of the run widths and the tallest line height.

```java
final Text hp = Text.create(
    TextElement.create("HP ", info),
    TextElement.create(() -> String.valueOf(this.health), info.copy().weight(FontWeight.BOLD))
).align(Align.END, Align.CENTER);

DrawUtils.TEXT.drawText(super.getX() + super.getWidth() - 12D, super.getY() + super.dh(2), hp);
```

A gradient text color spans the whole line, every run included.

## Text modes with TextMode

The box overloads lay the text out in the box `(x, y, width, height)` according to the mode. Nothing is clipped: a mode that does not cut can draw outside the box.

### NORMAL

The text is drawn on one line, aligned in the box: at `x`, `x + width / 2` or `x + width` horizontally and `y`, `y + height / 2` or `y + height` vertically, following its alignment. It is never cut. The bounds are those of the text.

### OVERFLOW

- A text that fits the width is drawn whole, without overflow mark.
- A wider text keeps the longest beginning that fits the width together with its overflow mark, and the mark is appended to the last run kept. The runs after the cut are dropped.
- With `TextOverflow.NONE`, the text is cut without mark.
- The text modifier is applied before the cut.
- When not even one character fits, nothing is drawn and the bounds are empty.

The cut text is aligned in the box like `NORMAL`. The bounds are `width` by the height of the cut text.

```java
DrawUtils.TEXT.drawText(super.getX(), super.getY(), super.getWidth(), super.getHeight(), this.title, TextMode.OVERFLOW);
```

### SPLIT

The text is split into lines with `getLines(width, text)`, and every line is drawn below the previous one:

- Horizontally, each line is aligned on its own: from `x`, centered on `x + width / 2` or ending on `x + width`.
- Vertically, the block of lines starts at `y` (`START`), is centered on `y + height / 2` (`CENTER`) or ends on `y + height` (`END`).

The bounds are `width` by the sum of the line heights.

### BOX

The lines are placed like `SPLIT`, and a line that does not fit entirely between `y` and `y + height` is skipped, above as well as below the box. The bounds are `width` by the sum of the heights of the lines drawn.

## Splitting text with getLines

```java
double y = 40D;
for (final String line : DrawUtils.TEXT.getLines(300D, this.message, info)) {
    DrawUtils.TEXT.drawText(20D, y, line, info, Align.START, Align.START);
    y += info.getHeight();
}
```

The rules `SPLIT` and `BOX` follow:

- `\n`, `\r`, `\r\n` and `<br>` end a line and are not part of it. A line break at the end leaves an empty last line.
- A line is filled while it stays at most `width` wide: a line exactly as wide as the box stays whole.
- When the next character does not fit, the line breaks at its last space, which is dropped. Without a space, the word is cut at that character. A single character wider than the box gets a line of its own.
- Each line of `getLines(double, Text)` is a `Text` with the alignment and overflow mark of the source text. Its runs keep their `TextInfo` and their dev mode origin, and hold the modified text with no modifier, so the modifier is applied exactly once.
- `getLines(double, String, TextInfo)` returns the text of each line.

> NOTE: Lines are cut from the raw string. A style opened by markup does not continue on the next line, and a cut can fall inside a markup code (see [Markup and Text Effects](../text/markup-and-effects.md#limits-of-markup)).

## Measuring before drawing

Measure with the same objects you draw:

| Need | Call |
|---|---|
| Width of a string | `info.getWidth(text)` |
| Line height | `info.getHeight()` |
| Size of a `Text` | `text.getWidth()`, `text.getHeight()`, `text.getBounds()` |
| Height of wrapped text | Sum of `getHeight()` over `DrawUtils.TEXT.getLines(width, text)` |

```java
final double height = DrawUtils.TEXT.getLines(400D, this.text).stream().mapToDouble(Text::getHeight).sum();
```

## See also

- [Text Model](../text/text-and-textinfo.md)
- [Fonts](../fonts/adding-fonts.md)
- [TextNode](../nodes/visual/text.md)
- [Drawing Overview](draw-utils.md)