# Text and TextInfo

The text model describes what a piece of text looks like before anything is drawn: a `Text` is a line made of one or more `TextElement` runs, and each run carries a `TextInfo` (font, size, weight, color, spacing, shadow, markup and effects). The same objects feed `TextNode`, `DrawUtils.TEXT` and every measurement helper, so you build a style once and reuse it everywhere.

## Quick example

```java
final TextInfo body = TextInfo.create(font, 20, Color.WHITE);
final TextInfo strong = body.copy().weight(FontWeight.BOLD);

final Text greeting = Text.create(
    TextElement.create("Welcome back, ", body),
    TextElement.create(() -> this.playerName, strong)
);

TextNode.create(0, 0).text(greeting).attach(flex);
```

![The words Welcome back, followed by the name Alex in bold](../images/text-quick.png "Two runs on one line: the second shares the font and size but is bold (playerName is Alex).")

- `font` is an `IFont`, usually an `MsdfFont` loaded with `MsdfFontLoader` (see [Adding Your Own Fonts](../fonts/adding-fonts.md)).
- `TextInfo` (`dev.joid.lib.font.dto`), `FontWeight` (`dev.joid.lib.font`), `Text` and `TextElement` (`dev.joid.lib.draw.text.builder`).
- The second element reads its supplier at every measure and draw, so the name updates without rebuilding the `Text`.

How these objects fit together:

| Object | Role |
|---|---|
| `TextInfo` | The style of a run: font, size, weight, italic, color, spacing, line height, shadow, markup, effects. |
| `TextElement` | One run: a text source (a value or a `Supplier`) and its `TextInfo`. |
| `Text` | A line of runs, with its alignment, overflow mark and modifier. |
| `FontBounds` | The width and height returned by every measure and draw call. |

Weights, alignment, text modes, overflow marks and modifiers are on [Styling Text](styling-text.md).

## TextInfo

`TextInfo` is the style of a run. It is mutable: every setter changes the instance and returns it.

### Creating a TextInfo with create

| Factory | Description |
|---|---|
| `TextInfo.create(IFont font, float fontSize)` | `REGULAR` weight, black. |
| `TextInfo.create(IFont font, float fontSize, Color color)` | `REGULAR` weight, given color. |
| `TextInfo.create(IFont font, FontWeight weight, float fontSize)` | Given weight, black. |
| `TextInfo.create(IFont font, FontWeight weight, float fontSize, Color color)` | Given weight and color. |

A new `TextInfo` starts upright, colored, without letter spacing, with the line height of the font, without shadow color, without effects and following the registered markups.

```java
final TextInfo caption = TextInfo.create(font, FontWeight.MEDIUM, 14, Color.decode("#9CA3AF"))
    .letterSpacing(0.04F)
    .lineHeight(1.4F)
    .shadow(Color.BLACK.copyAlpha(0.5F))
    .shadow(1F, 1F);
```

### TextInfo properties

| Method | Description | Default |
|---|---|---|
| `font(IFont font)` | Font of the run. | Given at creation |
| `fontSize(float fontSize)` | Size of the em, in UI units. | Given at creation |
| `weight(FontWeight weight)` | Weight the font family resolves (see [Choosing a face](../fonts/how-fonts-work.md#choosing-a-face-with-fontfamily-resolve)). | `FontWeight.REGULAR` |
| `italic(boolean italic)` | Draws the italic face of the family, or slants the upright face when the family has no italic face. | `false` |
| `letterSpacing(float letterSpacing)` | Space added between two glyphs, as a fraction of the font size: `0.1F` adds 10 % of the size, a negative value tightens. | `0F` |
| `lineHeight(float lineHeight)` | Line height as a fraction of the font size: `1.5F` = 150 %. `0F` (or any value not above 0) keeps the line height of the font. | `0F` |
| `color(Color color)` | Color of the text. A gradient color spans the whole drawn line, every run included. | Given at creation, `Color.BLACK` otherwise |
| `colored(boolean colored)` | When `false`, colors set by markup are ignored and every glyph takes `color`. | `true` |
| `shadow()` | Sets the shadow color to the current `color` made 30 % darker (`color.darker(0.3F)`). | No shadow |
| `shadow(Color color)` | Sets the shadow color; `null` removes the shadow. | `null` |
| `shadow(float x, float y)` | Shadow offset, in UI units. | `fontSize / 13.5F` on both axes, computed at creation |
| `markups(ITextMarkup... markups)` | Uses only these markups for this run; `markups()` with no argument disables markup. | Follows the `TextMarkup` registry |
| `effects(ITextEffect... effects)` | Effects applied to every glyph of the run. | None |
| `copy()` | New `TextInfo` with every property copied. | |

![The same sentence drawn with the default style, letter spacing 0.2 and -0.05, italic, two shadows and a gradient color](../images/text-properties.png "TextInfo.create(font, 28, Color.WHITE) with one property changed per line (Montserrat).")

Getters: `getFont()`, `getFontSize()`, `getWeight()`, `getLetterSpacing()`, `getLineHeight()`, `getColor()`, `isColored()`, `isItalic()`, `getShadowColor()`, `getShadowX()`, `getShadowY()`, `getMarkups()` (the registered markups while `markups(...)` was never called, read-only), `getEffects()` (read-only) and `getStyle()` (a new [`TextStyle`](styling-text.md#textstyle) built from the weight, italic flag, color and effects).

> NOTE: The shadow offset is computed from the font size given to `create`. Changing `fontSize(...)` later keeps the old offset: call `shadow(x, y)` again if it must follow.

### Colors and alpha

- With `colored(true)`, a color set by markup replaces the `TextInfo` color for the following glyphs, and takes the alpha of the `TextInfo` color. Fading `color` therefore fades every glyph, markup colors included.
- With `colored(false)`, every glyph is drawn with `color`. Text effects can still recolor glyphs (see [Markup and Text Effects](markup-and-effects.md)).
- The shadow is drawn with `getShadowColor()` and does not take the markup colors.

### Sharing a TextInfo

A `TextInfo`, like a `Text` or a `TextElement`, is shared by reference. Two runs built on the same instance change together. Call `copy()` before changing a style that other runs use:

```java
final TextInfo title = body.copy().fontSize(32).weight(FontWeight.BOLD);
```

### Measuring with TextInfo

| Method | Description |
|---|---|
| `getWidth(String text)` | Width of the text drawn on one line: advances, kerning and letter spacing, without trailing spacing. Markup characters take no room. |
| `getHeight()` | Line height: `lineHeight * fontSize` when `lineHeight` is above 0, the line height of the font face otherwise. |
| `getHeight(String text)` | Height of the text, the line height for the MSDF fonts. |
| `getBounds(String text)` | `FontBounds` of `getWidth(text)` and `getHeight(text)`. |
| `dw(String text, double value)` | `getWidth(text) / value`. |
| `dh(double value)`, `dh(String text, double value)` | `getHeight() / value`, `getHeight(text) / value`. |
| `aw(String text, double value)` | `getWidth(text) + value`. |
| `ah(double value)`, `ah(String text, double value)` | `getHeight() + value`, `getHeight(text) + value`. |

```java
final double width = caption.aw("Settings", 24D);
RectNode.create(0, 0, width, caption.ah(12D)).color(Color.BLACK).attach(flex);
```

![A black box tightly sized around the word Settings](../images/text-caption.png "The box is the text width plus 24 and the line height plus 12; the word is drawn inside for reference (2× scale).")

## Text

`Text` (`dev.joid.lib.draw.text.builder`) is an ordered list of `TextElement` runs drawn one after the other on the same line, with an alignment, an overflow mark and an optional modifier. A `Text` starts aligned `START`/`START`, with `TextOverflow.NONE` and no modifier.

### Creating a Text with create

| Factory | Description |
|---|---|
| `Text.create()` | Empty text. |
| `Text.create(TextElement... elements)`, `Text.create(List<TextElement> elements)` | Text made of these runs. |
| `Text.create(Object text, TextInfo info)` | One run; the object is read through `toString()` at every measure and draw. |
| `Text.create(Supplier<?> text, TextInfo info)` | One run read from the supplier at every measure and draw. |
| `Text.create(text, info, Align horizontalAlign)` | One run, horizontal alignment. `text` is an `Object` or a `Supplier<?>`, as for every factory below. |
| `Text.create(text, info, TextOverflow overflow)` | One run, overflow mark. |
| `Text.create(text, info, Align align, TextOverflow overflow)` | One run, horizontal alignment and overflow mark. |
| `Text.create(text, info, Align horizontalAlign, Align verticalAlign)` | One run, both alignments. |
| `Text.create(text, info, Align horizontalAlign, Align verticalAlign, TextOverflow overflow)` | One run, both alignments and overflow mark. |

### Dynamic text with Supplier

A `Supplier<?>` or a mutable object (a `StringBuilder`, an object with its own `toString()`) is read again at every measure and draw, so the text follows your state without rebuilding anything:

```java
final Text score = Text.create(() -> "Score: " + this.score, info);
```

### Editing a Text

| Method | Description |
|---|---|
| `text(String text)`, `text(Supplier<?> text)` | Replaces the text of the first element. |
| `text(int index, String text)`, `text(int index, Supplier<?> text)` | Replaces the text of the indexed element; an index outside the list is ignored. |
| `info(TextInfo info)`, `info(int index, TextInfo info)` | Replaces the `TextInfo` of the first or indexed element; an index outside the list is ignored. |
| `add(TextElement element)` | Appends a run. |
| `add(Text text)` | Appends the runs of another text (the same element instances). |
| `addAll(List<TextElement> elements)` | Appends runs. |
| `remove(TextElement element)` | Removes a run. |
| `clear()` | Removes every run. |
| `get(int index)` | Element at this index. |
| `getElementList()` | The list of runs. |

The alignment (`align`, `horizontalAlign`, `verticalAlign`), the overflow mark (`overflow`) and the modifier (`modifier`) of a `Text` are described on [Styling Text](styling-text.md).

### Copying a Text

| Method | Description |
|---|---|
| `copy()` | New text with its own list of the same element instances and the same settings. |
| `copyProperties()` | New empty text with the same alignment, overflow mark and modifier. |
| `copyWithModifier(ITextModifier)`, `copyWithOverflow(TextOverflow)`, `copyWithHorizontalAlign(Align)`, `copyWithVerticalAlign(Align)` | `copy()` with one setting replaced. |

### Reading and measuring a Text

| Method | Description |
|---|---|
| `getText()` | Joined text of every run, with the element modifiers and the text modifier applied. |
| `getText(TextElement element)` | Part of `getText()` that belongs to this run. |
| `getRawText()` | Joined text with the element modifiers but without the text modifier. |
| `getWidth()` | Sum of the widths of the runs. |
| `getHeight()` | Height of the tallest run. |
| `getBounds()` | `FontBounds` of the width and height. |
| `dw(double)`, `dh(double)` | Width or height divided by the value. |
| `aw(double)`, `ah(double)` | Width or height plus the value. |
| `isEmpty()` | `true` when the text has no run. |

> NOTE: A `Text` caches its size until its raw text changes or one of its own editing methods (`text`, `info`, `add`, `addAll`, `remove`, `clear`, `modifier`) is called. Changing a `TextInfo` in place, or calling `info(...)` directly on one of its `TextElement`s, keeps the old size: go through `Text.info(...)` or give the run a new `TextInfo`.

## TextElement

`TextElement` is one run: a text source, a `TextInfo` and an optional modifier of its own.

| Method | Description |
|---|---|
| `TextElement.create(Object text, TextInfo info)` | Run read through `toString()` at every call. |
| `TextElement.create(Supplier<?> text, TextInfo info)` | Run read from the supplier at every call. |
| `TextElement.create(int / long / char / float / double / boolean text, TextInfo info)` | Run with the `String.valueOf` of the value. |
| `text(...)` | Same overloads as `create`: replaces the text source. |
| `info(TextInfo info)` | Replaces the `TextInfo`. |
| `modifier(ITextModifier modifier)` | Modifier of this run only; `null` removes it (see [Text modifiers](styling-text.md#text-modifiers-with-textmodifier)). |
| `copy()` | New run with the same source, `TextInfo` and modifier. |
| `copyWithText(Object)`, `copyWithText(Supplier<?>)`, `copyWithInfo(TextInfo)`, `copyWithModifier(ITextModifier)` | Copy with one part replaced. |
| `getText()` | Text with the run modifier applied. |
| `getRawText()` | Text without the run modifier. |
| `getInfo()`, `getModifier()` | Current `TextInfo` and modifier. |
| `getOrigin()` | In dev mode, the stack trace of the code that created the run (used to point font warnings at your code); `null` otherwise. |

Every fluent method returns the element with a generic type, so it chains in subclasses of `TextElement`.

## FontBounds

`FontBounds` (`dev.joid.lib.font.dto`) is the size returned by every measure and draw call.

| Method | Description |
|---|---|
| `getWidth()` | Width, in UI units. |
| `getHeight()` | Height, in UI units. |
| `FontBounds.empty()` | Bounds of 0×0. |

## See also

- [Styling Text](styling-text.md)
- [Markup and Text Effects](markup-and-effects.md)
- [How Fonts Work](../fonts/how-fonts-work.md)
- [Drawing Text](../drawing/text.md)
- [TextNode](../nodes/visual/text.md)
- [Colors and Gradients](../styling/colors.md)