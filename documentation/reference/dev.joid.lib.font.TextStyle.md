---
summary: The running style of the characters while a text is laid out, which markup tags change as they are read.
---

A text renderer starts from [[TextInfo#getStyle]], derives a working copy, and hands it to each [[ITextMarkup]] it meets: a tag of yours can set the weight, the color, the font or the effects of the characters that follow, and a closing tag resets them.
Each glyph keeps a copy of the style it was laid out with.

Learn: [Text markup and effects](/use/learn/describing-the-ui/text-markup-and-effects/).

## create(FontWeight,boolean,Color,ITextEffect[])

Creates a style without a base, with the given weight, italic flag, color and effects, and no font of its own.

## font

The font of the characters, or `null` to keep the font of the [[TextInfo]]; a font drawn by another text renderer is ignored with a dev-mode warning.

## italic

If `true`, the characters draw with the italic face, or slanted when the font has none.

## color

The color of the characters; it applies when [[TextInfo#colored]] is `true`.

## weight

The [[FontWeight]] of the characters.

## copy()

Returns a style with the same values and the same base.

## reset()

Sets every value back to the base style; a style without a base is unchanged.

## derive()

Returns a working copy whose base is this style, so that `reset()` comes back to it.

## effect(ITextEffect)

Adds an effect to the characters that follow, unless the style already has it.

## removeEffect(ITextEffect)

Removes an effect from the characters that follow.

## getBase()

Returns the base style, or this style when it has none.

## getEffects()

Returns the effects of the characters, as a list you cannot change.
