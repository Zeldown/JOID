---
summary: Signal of a `String` with the text operations of `String`, which write their result.
---

It works like [[Signal]]; each operation applies the method of the same name of `String` to the value and writes the result.
An index outside the text throws a `StringIndexOutOfBoundsException` when the write applies.

Learn: [Signals](/use/learn/interactivity/signals/).

## StringSignal()

Creates a signal whose value and default value are the empty text `""`.

## StringSignal(String)

Creates a signal whose value and default value are `value`.

## append(String)

Adds `value` at the end of the text, then returns this signal; `null` adds the text `"null"`.

## replace(char,char)

Replaces every `oldChar` with `newChar`, then returns this signal.

## replace(CharSequence,CharSequence)

Replaces every occurrence of `target` with `replacement`, then returns this signal.

## toLowerCase()

Writes the text in lower case, with the rules of the default locale, then returns this signal.

## toUpperCase()

Writes the text in upper case, with the rules of the default locale, then returns this signal.

## trim()

Removes the spaces and control characters at both ends of the text, then returns this signal.

## substring(int)

Keeps the text from `beginIndex` to its end, then returns this signal.

## substring(int,int)

Keeps the text from `beginIndex` included to `endIndex` excluded, then returns this signal.
