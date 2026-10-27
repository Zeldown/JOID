---
summary: Turns a value of a type of yours into the string that a text shows.
---

Register one with [[TextConverter#register]]: every text, tooltip and text setter that takes an `Object` goes through it.

Learn: [Text markup and effects](/use/learn/describing-the-ui/text-markup-and-effects/).

## supports(Object)

If `true`, this converter turns `text` into a string; JOID asks the converters from the last registered to the first and uses the first that answers `true`.

## convert(Object)

Returns the string that a text shows for `text`; JOID calls it only when `supports` returned `true`.
