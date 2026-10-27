---
summary: A run of text made of one or more elements, each with its own style, plus the alignment, overflow marker and modifier of the whole run.
---

A text node holds a `Text`, and [[DrawText]] draws one.
Each [[TextElement]] carries its value and its [[TextInfo]], so one run can mix sizes, weights and colors on a shared baseline.
The width and the height are measured on demand and kept until an element or its style changes.

Learn: [Text](/use/learn/describing-the-ui/text/).

## create(TextElement[])

Creates a text from elements, drawn one after the other.

## create(Object,TextInfo)

Creates a text of one element showing `text`, converted to a string by [[TextConverter]], styled by `info`.

## create(Supplier,TextInfo)

Creates a text of one element that shows the value of `text`, read each time the text is measured or drawn.

## text

The text of the run: `getText()` returns the text of every element after the modifiers, joined.
`text(Object)` and `text(Supplier)` set the value of the first element; they throw `IndexOutOfBoundsException` on a text without elements.

## alignX

Where the text sits horizontally around the point it is drawn at, or inside its box.

## alignY

Where the text sits vertically around the point it is drawn at, or inside its box.

## overflow

The marker that ends a text cut by [[TextMode#OVERFLOW]], such as `...`.

## modifier

The [[TextModifier]] applied to the whole run after the modifier of each element, or `null` for none.
It sees the text of the elements before each one, so a modifier such as a typewriter counts across the run.

## getWidth()

Returns the width of the run on one line, in design pixels, letter spacing between elements included.

## getHeight()

Returns the height of the tallest element, in design pixels.

## dw(double)

Returns the width divided by `value`: `dw(2D)` is half the width.

## dh(double)

Returns the height divided by `value`: `dh(2D)` is half the height.

## aw(double)

Returns the width plus `value`.

## ah(double)

Returns the height plus `value`.

## getElementList()

Returns the elements, as a list you cannot change; use `add`, `remove` and `clear`.

## getText(TextElement)

Returns the text of `element` after its own modifier and the modifier of the run.

## getRawText()

Returns the text of every element before any modifier, markup tags included.

## getBounds()

Returns the width and the height as a [[FontBounds]].

## get(int)

Returns the element at `index`.
Throws `IndexOutOfBoundsException` outside the elements.

## isEmpty()

If `true`, the text has no element and draws nothing.

## toString()

Returns the text, the alignments, the overflow and the element count, for logs.

## text(int,Object)

Sets the value of the element at `index`.
Throws `IndexOutOfBoundsException` outside the elements.

## text(int,Supplier)

Makes the element at `index` show the value of `text`.
Throws `IndexOutOfBoundsException` outside the elements.

## info(TextInfo)

Sets the style of the first element.

## info(int,TextInfo)

Sets the style of the element at `index`.
Throws `IndexOutOfBoundsException` outside the elements.

## add(Text)

Appends copies of the elements of `text`, so that changing one text leaves the other unchanged.

## add(TextElement[])

Appends the elements themselves.

## remove(TextElement)

Removes an element.

## clear()

Removes every element.

## copy()

Returns a copy with the same properties and copies of the elements.

## copyProperties()

Returns a text without elements that has the alignments, the overflow and the modifier of this one.

## align(Align)

Sets both `alignX` and `alignY`.
