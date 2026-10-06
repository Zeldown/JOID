# Styling Text

Once a [`TextInfo`](text-and-textinfo.md) gives a run its font, size and color, you shape the text further with a weight (`FontWeight`), italic, an alignment, a layout mode for boxes (`TextMode`), an overflow mark (`TextOverflow`) and modifiers that rewrite the string (`TextModifier`). This page covers each of them, then `TextStyle`, the per-glyph style that markup and effects work on.

## Quick example

```java
final TextInfo title = TextInfo.create(font, FontWeight.SEMI_BOLD, 28, Color.WHITE).italic(true);

final Text heading = Text.create("player settings", title, Align.CENTER, Align.CENTER)
    .modifier(TextModifier.WORD_CAPITALIZE);

DrawUtils.TEXT.drawText(960, 80, heading);

TextNode.create(0, 0, 300, 0)
    .text(Text.create(this.description, TextInfo.create(font, 16, Color.WHITE), TextOverflow.ELLIPSIS))
    .mode(TextMode.OVERFLOW)
    .attach(flex);
```

![Player Settings in semi-bold italic centered on a red mark, above a description cut with an ellipsis](../images/text-styling-quick.png "The heading drawn with DrawUtils.TEXT.drawText at a point (red mark) and the description in a 300-unit TextNode.")

`DrawUtils.TEXT.drawText` (in a [custom node](../nodes/custom-nodes.md)'s `draw`) draws the heading `Player Settings`, semi-bold and italic, centered on the point (960, 80): the alignment places the text around the point it is drawn at.; the description stays on one line and ends with `...` when it is wider than 300 units.

## Weight with FontWeight

`FontWeight` (`dev.joid.lib.font`) names the nine CSS weights. Set it with `TextInfo.create(font, weight, size)` or `TextInfo.weight(...)`.

| Constant | Value |
|---|---|
| `THIN` | 100 |
| `EXTRA_LIGHT` | 200 |
| `LIGHT` | 300 |
| `REGULAR` | 400 |
| `MEDIUM` | 500 |
| `SEMI_BOLD` | 600 |
| `BOLD` | 700 |
| `EXTRA_BOLD` | 800 |
| `BLACK` | 900 |

![The nine weights of Montserrat, upright and italic](../images/text-weights.png "Every FontWeight of the demo Montserrat family, upright and with italic(true).")

- `getValue()` returns the numeric weight.
- `FontWeight.of(int value)` returns the nearest named weight, a half step rounding up and values clamped to 100–900: `of(600)` is `SEMI_BOLD`, `of(650)` is `BOLD`, `of(1000)` is `BLACK`.

A weight draws the face of that weight when the family has one. Otherwise the family draws its closest face, following the rules of [Choosing a face](../fonts/how-fonts-work.md#choosing-a-face-with-fontfamily-resolve), and dev mode prints a [missing weight warning](../fonts/adding-fonts.md#missing-weight-warnings). Load every weight you use.

## Italic

`TextInfo.italic(true)` draws the italic face of the family. When the family has no italic face, the upright glyphs are sheared by 0.2 (about 11°), so italic always shows. Load the italic file of the font when you want the real italic shapes (see [Italic without an italic face](../fonts/how-fonts-work.md#italic-without-an-italic-face)).

## Alignment with horizontalAlign and verticalAlign

The alignment belongs to the `Text`:

| Method | Description |
|---|---|
| `align(Align horizontal, Align vertical)` | Sets both alignments. |
| `horizontalAlign(Align align)` | `START`, `CENTER` or `END`. |
| `verticalAlign(Align align)` | `START`, `CENTER` or `END`. |
| `getHorizontalAlignment()`, `getVerticalAlignment()` | Current alignments. |

![Nine boxes showing a text aligned START, CENTER and END horizontally and vertically](../images/text-align.png "The nine combinations of horizontalAlign / verticalAlign in a 220 × 100 box.")

When the text is drawn at a point, the alignment says which part of the text sits on that point: with `CENTER`/`CENTER`, the point is the center of the text; with `END`, the point is its right (or bottom) edge. When it is drawn in a box, the text is aligned inside the box. Runs of different heights are aligned vertically inside the height of the tallest one. See [Drawing Text](../drawing/text.md).

## Layout in a box with TextMode

`TextMode` (`dev.joid.lib.draw.text.utils`) chooses how a text is laid out in a box. It is used by `TextNode.mode(...)` and by the box overloads of `DrawUtils.TEXT.drawText`.

| Mode | Behavior |
|---|---|
| `NORMAL` | One line, aligned in the box, never cut. |
| `OVERFLOW` | One line; a text wider than the box is cut and ends with the overflow mark. |
| `SPLIT` | Wraps into lines that fit the box width; every line is drawn. |
| `BOX` | Wraps like `SPLIT` and skips the lines that do not fit entirely inside the box height. |

![The same sentence in a 220 x 60 box with the modes NORMAL, OVERFLOW, SPLIT and BOX](../images/text-modes.png "NORMAL runs past the box, OVERFLOW cuts it, SPLIT wraps and grows, BOX wraps and drops the line that does not fit.")

Line breaks (`\n`, `\r`, `\r\n`, `<br>`) start a new line in `SPLIT` and `BOX` only. The exact rules are on [Drawing Text](../drawing/text.md#text-modes-with-textmode).

## Overflow marks with TextOverflow

`TextOverflow` (`dev.joid.lib.draw.text.builder.utils`) is the mark that ends a text cut by `TextMode.OVERFLOW`. Set it with `Text.overflow(...)` or a `Text.create(..., TextOverflow)` factory; `getOverflow()` returns the mark of a constant, `Text.getOverflow()` the mark of a text.

| Constant | Mark |
|---|---|
| `NONE` | Nothing: the text is cut without mark. |
| `ELLIPSIS` | `...` |
| `DOT` | `.` |
| `HYPHEN` | `-` |

![A long sentence cut at 240 units with no mark, ..., . and -](../images/text-overflow-marks.png "The four TextOverflow marks with TextMode.OVERFLOW.")

```java
TextNode.create(0, 0, 300, 0)
    .text(Text.create(this.description, info, TextOverflow.ELLIPSIS))
    .mode(TextMode.OVERFLOW)
    .attach(flex);
```

## Text modifiers with TextModifier

An `ITextModifier` (`dev.joid.lib.draw.text.builder.modifier`) rewrites the string right before it is measured and drawn. Set it with `Text.modifier(...)` or `TextElement.modifier(...)`; `null` removes it. `TextModifier` holds the built-in ones:

| Constant | Rule | `"hello big_World"` becomes |
|---|---|---|
| `TextModifier.UPPER_CASE` | `String.toUpperCase()` | `HELLO BIG_WORLD` |
| `TextModifier.LOWER_CASE` | `String.toLowerCase()` | `hello big_world` |
| `TextModifier.CAPITALIZE` | First character in upper case, the rest unchanged. | `Hello big_World` |
| `TextModifier.WORD_CAPITALIZE` | First character of every whitespace-separated word in upper case, the rest unchanged. | `Hello Big_World` |
| `TextModifier.CAMEL_CASE` | Splits on every character that is not a letter or a digit, first word in lower case, the next ones capitalized and lower-cased. | `helloBigWorld` |
| `TextModifier.UPPER_CAMEL_CASE` | Same split, every word capitalized and lower-cased. | `HelloBigWorld` |
| `TextModifier.SNAKE_CASE` | Lower case, spaces replaced by `_`. | `hello_big_world` |

![hello big_World rewritten by each TextModifier](../images/text-modifiers.png "The string of the table drawn with each built-in modifier.")

- On a `TextElement`, the modifier applies to that run only.
- On a `Text`, the modifier applies to the joined text of every run, so a rule that depends on the previous characters stays right across runs: `CAMEL_CASE` on the runs `"hello "` and `"big world"` draws `hello` and `BigWorld`.
- An element modifier applies first, then the text modifier.

A custom modifier is a lambda:

```java
final ITextModifier masked = text -> text.replaceAll(".", "*");
final Text password = Text.create(() -> this.password, info).modifier(masked);
```

| `Text` method | Description |
|---|---|
| `overflow(TextOverflow overflow)` | Mark appended when `TextMode.OVERFLOW` cuts the text. |
| `modifier(ITextModifier modifier)` | Modifier applied to the joined text of every run; `null` removes it. |
| `getOverflow()`, `getModifier()` | Current values. |

## TextStyle

`TextStyle` (`dev.joid.lib.font.dto`) is the style of a single glyph: weight, italic flag, color and effects. Drawing starts from `TextInfo.getStyle()` and markup edits a working copy as it reads the string. You meet it when you write an `ITextMarkup` or an `ITextEffect` (see [Markup and Text Effects](markup-and-effects.md)).

| Method | Description |
|---|---|
| `TextStyle.create(FontWeight weight, boolean italic, Color color, ITextEffect... effects)` | Root style, its own base. |
| `weight(FontWeight)`, `italic(boolean)`, `color(Color)` | Change the style. |
| `effect(ITextEffect effect)` | Adds an effect; an effect already present is not added twice. |
| `removeEffect(ITextEffect effect)` | Removes an effect. |
| `reset()` | Restores the weight, italic flag, color and effects of the base style. |
| `derive()` | New style whose base is this one. |
| `copy()` | Snapshot with the same base. |
| `getBase()` | Base style, this style at the root. While a string is laid out, it is the style of the `TextInfo`. |
| `getWeight()`, `isItalic()`, `getColor()`, `getEffects()` | Current values; the effect list is read-only. |

## See also

- [Text and TextInfo](text-and-textinfo.md)
- [Markup and Text Effects](markup-and-effects.md)
- [How Fonts Work](../fonts/how-fonts-work.md)
- [Drawing Text](../drawing/text.md)
- [TextNode](../nodes/visual/text.md)