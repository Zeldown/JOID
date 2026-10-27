---
summary: Changes the characters a text shows, such as its case, without changing its value.
---

Set one on a [[Text]] or a [[TextElement]] with `modifier(...)`, or use one of the constants.
It applies to the whole text at once and leaves the markup tags untouched.

Learn: [Text markup and effects](/use/learn/describing-the-ui/text-markup-and-effects/).

## modify(String)

Returns the text to show for `text`, without markup tags.

## modify(String,TextInfo)

Returns the text to show for `text`, with the tags of the markups of `info` kept as they are and the text between them modified as one string.

## modifyAfter(UnaryOperator,StringBuilder,String)

Returns the modified form of `text` when it follows `before`, so that a modifier that depends on what comes first, such as a capital at the start, stays right across elements; `text` is appended to `before`.

## camelCase(String,boolean)

Returns `text` with its words joined in camel case, the first word capitalized when `upperFirst` is `true`; any character other than a letter or a digit separates words.

## capitalizeWords(String)

Returns `text` with the first letter after each whitespace in title case.

## LOWER_CASE

Shows the text in lower case.

## UPPER_CASE

Shows the text in upper case.

## CAMEL_CASE

Shows `hello big world` as `helloBigWorld`.

## UPPER_CAMEL_CASE

Shows `hello big world` as `HelloBigWorld`.

## SNAKE_CASE

Shows `Hello big world` as `hello_big_world`.

## CAPITALIZE

Capitalizes the first letter of the text.

## WORD_CAPITALIZE

Capitalizes the first letter of every word.
