---
summary: The style of a text, with its font, size, weight, color, spacing, shadow, markups and effects, and the measures it gives.
---

Create one per style with `create(font, fontSize)` and keep it in a field: every text node and [[DrawText]] call takes one.

```java fragment
final TextInfo title = TextInfo.create(font, 32F).weight(FontWeight.BOLD).color(Color.decode("#E7E9EE"));
final TextInfo faded = title.withColor(Color.decode("#8B93A3"));
```

The plain setters (`color`, `fontSize`…) change this style in place; the `withX` methods return a changed copy and leave this one unchanged, which a locked style requires: a plain setter on a locked style throws `IllegalStateException`.
Sizes are in design pixels; the letter spacing and the line height are on 1 of the font size.

Learn: [Text](/use/learn/describing-the-ui/text/).

## create(IFont,float)

Creates a style that draws with `font` at `fontSize` design pixels, in opaque black, regular weight, upright, with the registered markups.

## font

The [[IFont]] that draws the text; its text renderer measures and draws it.

Default: set at creation, by `TextInfo.create(font, fontSize)`

## fontSize

The size of the text, in design pixels: the em size of the font.

Default: set at creation, by `TextInfo.create(font, fontSize)`

## weight

The [[FontWeight]] of the text; a font without that weight draws its closest one.

## letterSpacing

The extra space after each character, on 1 of the font size: `0.1F` adds a tenth of the size, `-0.02F` tightens.

## lineHeight

The height of one line, on 1 of the font size; `0F` takes the line height of the font.

Default: the line height of the font

## color

The [[Color]] of the text, gradient and animated colors included; markup colors replace it when `colored` is `true`.

## colored

If `true`, the markups that set a color change the color of the text and keep the alpha of `color`; if `false`, every character takes `color`.

## italic

If `true`, the text draws with the italic face of the font, or slanted when the font has none.

## markups

The [[ITextMarkup]] tags that the text reads; JOID registers none by default.
`getMarkups()` returns the registered markups of [[TextMarkup]] until `markups(...)` sets a list of your own.

Default: the registered markups of [[TextMarkup]]

## effects

The [[ITextEffect]] list applied to every glyph, such as a wave or a typewriter; `getEffects()` returns it as a list you cannot change.

Default: none

## shadowTint

The tint of a shadow drawn in the color of each glyph multiplied by this factor, alpha kept; `null` for none.
When it is set, it replaces the color of `shadow(Color)`.

Default: none, the shadow takes the color of `shadow(Color)`

## getWidth(String)

Returns the width of `text` drawn on one line with this style, in design pixels, markup tags excluded.

## getHeight()

Returns the height of one line, in design pixels.

## getHeight(String)

Returns the height of `text` drawn with this style, in design pixels.

## dw(String,double)

Returns the width of `text` divided by `value`.

## dh(double)

Returns the line height divided by `value`.

## dh(String,double)

Returns the height of `text` divided by `value`.

## aw(String,double)

Returns the width of `text` plus `value`.

## ah(double)

Returns the line height plus `value`.

## ah(String,double)

Returns the height of `text` plus `value`.

## withColor(Color)

Returns a copy with `color` as its color.

## withColored(boolean)

Returns a copy with `colored` set.

## withShadowTint(Float)

Returns a copy with `shadowTint` set.

## getShadowColor()

Returns the color of the shadow as it is drawn, the color of the text darkened for `shadow()`, or `null` when the text has no shadow of its own color.

## getBaseline()

Returns the distance from the top of a line to its baseline, in design pixels; the elements of a [[Text]] align on it.

## getStyle()

Returns the [[TextStyle]] that the markups start from: weight, italic, color and effects of this style.

## getShadowX()

Returns the horizontal offset of the shadow, in design pixels; by default a 13.5th of the font size.

## getShadowY()

Returns the vertical offset of the shadow, in design pixels; by default a 13.5th of the font size.

## getBounds(String)

Returns the width and the height of `text` as a [[FontBounds]].

## toString()

Returns the font, the size and the color, for logs.

## shadow()

Adds a shadow in the color of the text darkened by 30 %; it follows a color set later.
Throws `IllegalStateException` on a locked style.

## shadow(Color)

Sets the color of the shadow; `null` removes it.
Throws `IllegalStateException` on a locked style.

## shadow(float,float)

Sets the offset of the shadow, in design pixels; on the pixel grid it rounds to at least one screen pixel.
Throws `IllegalStateException` on a locked style.

## withFont(IFont)

Returns a copy with `font` as its font.

## withFontSize(float)

Returns a copy with `fontSize` as its size.

## withWeight(FontWeight)

Returns a copy with `weight` as its weight.

## withLetterSpacing(float)

Returns a copy with `letterSpacing` set.

## withLineHeight(float)

Returns a copy with `lineHeight` set.

## withItalic(boolean)

Returns a copy with `italic` set.

## withMarkups(ITextMarkup[])

Returns a copy that reads only `markups`.

## withShadow()

Returns a copy with a shadow in the color of the text darkened by 30 %, which follows a color set later.

## withShadow(Color)

Returns a copy with a shadow of `color`.

## withShadow(float,float)

Returns a copy with the shadow offset set.

## copy()

Returns an unlocked copy with every property of this style.

## withEffects(ITextEffect[])

Returns a copy with `effects` as its effects.
