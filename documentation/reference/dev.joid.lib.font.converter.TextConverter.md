---
summary: The registry of the text converters, which turns any value into the string a text shows.
---

A `String` shows as it is; any other value goes to the last registered [[ITextConverter]] that supports it, otherwise to its `toString()`.

Learn: [Text markup and effects](/use/learn/describing-the-ui/text-markup-and-effects/).

## register(ITextConverter)

Adds a converter in front of the others; registering it again moves it to the front.

## unregister(ITextConverter)

Removes a converter.

## convert(Object)

Returns the string for `text`.

## convertLines(Object)

Returns one string per element of `content` when it is an `Iterable`, otherwise a list of one string; tooltips use it for their lines.
