---
summary: One styled part of a `Text`, with its value, its `TextInfo` and its own modifier.
---

Put several elements in one [[Text]] to mix styles in one run, such as a bold word in a sentence.
The value can be any object, converted to a string by [[TextConverter]], or a `Supplier` read each time the text is measured or drawn.

Learn: [Text](/use/learn/describing-the-ui/text/).

## create(Object,TextInfo)

Creates an element showing `text`, styled by `info`.

## create(Supplier,TextInfo)

Creates an element showing the value of `text`, styled by `info`.

## text

The value of the element: `getText()` returns it as a string after the modifier; `text(Object)` and `text(Supplier)` set it.

Default: set at creation, by `TextElement.create(text, info)`

## info

The [[TextInfo]] that styles the element: font, size, color, weight and spacing.

Default: set at creation, by `TextElement.create(text, info)`

## modifier

The [[TextModifier]] applied to the text of this element, or `null` for none.

## copy()

Returns an element with the same value, style and modifier.

## copyWithText(Object)

Returns an element with the style and the modifier of this one and the value `text`.

## copyWithText(Supplier)

Returns an element with the style and the modifier of this one, showing the value of `text`.

## getRawText()

Returns the value converted to a string, before the modifier, markup tags included.
