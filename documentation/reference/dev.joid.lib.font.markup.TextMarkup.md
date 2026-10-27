---
summary: The registry of the markup tags that every text reads, and the helpers that find the tags in a string.
---

JOID registers no markup: the tags a text understands are the ones you register, or the ones you give a style with [[TextInfo#markups]].

Learn: [Text markup and effects](/use/learn/describing-the-ui/text-markup-and-effects/).

## register(ITextMarkup)

Adds a markup in front of the others, so that it is tried first; registering it again moves it to the front.

## unregister(ITextMarkup)

Removes a markup.

## getRegistered()

Returns the registered markups, the first tried first, as a list you cannot change.

## parse(List,String,int,TextStyle)

Tries each markup of `markups` at `index` and returns the length of the first tag found, or `0` when no tag starts there.

## tags(String,TextInfo)

Returns, for each character of `text`, the index where the tag that holds it starts, or `-1` for a character outside any tag, with the markups of `info`.

## opened(String,int[],int)

Returns every tag found in `text` before `end`, joined, so that a line that starts at `end` reopens the styles of the previous one.
