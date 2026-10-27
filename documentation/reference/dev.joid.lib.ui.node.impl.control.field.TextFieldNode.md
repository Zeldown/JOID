---
summary: A one-line field whose value is its text.
---

It needs only a [[TextInfo]] and optional styling; a `background(...)` gives it a look.

Learn: [Text fields](/use/learn/interactivity/text-fields/#add-a-text-field).

## create(double,double,double)

Creates the field at `x`, `y` with a `width` in design pixels; its height is the height of its font plus its top and bottom margins.

## create(double,double,double,double)

Creates the field at `x`, `y` with a `width` and a `height` in design pixels.

## TextFieldNode(double,double,double,double)

Creates the field, from the constructor of a subclass.

## parse(String)

Returns the text itself: every text is a value.

## format(String)

Returns the value itself.
