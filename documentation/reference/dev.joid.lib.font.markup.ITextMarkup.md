---
summary: A markup tag that a text reads inline, such as a bold or a color tag, which changes the style of the characters after it.
---

Register it with [[TextMarkup#register]] so that every text reads it, or give it to one style with [[TextInfo#markups]].
A tag takes no width, is never cut when a line wraps, and stays in the text that modifiers see untouched.

Learn: [Text markup and effects](/use/learn/describing-the-ui/text-markup-and-effects/).

## parse(String,int,TextStyle)

JOID calls it at each character of a text while it lays it out or measures it, on the render thread.
When a tag of yours starts at `index`, change `style` (weight, color, font, effects; [[TextStyle#reset]] for a closing tag) and return the number of characters of the tag; otherwise return `0`.
