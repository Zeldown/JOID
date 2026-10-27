---
summary: The change of the text of a field, with the text before and after and the value read from it, in a cancellable `Pre` and a `Post`.
---

[[FieldNode#beforeChange]] receives a [[NodeFieldChangeEvent.Pre]] and can refuse the edit; [[FieldNode#onChange]] receives a [[NodeFieldChangeEvent.Post]] once the text changed.

Learn: [Text fields](/use/learn/interactivity/text-fields/), [Controls](/use/learn/interactivity/controls/#react-to-a-change).

## getValue()

Returns the value that the field reads from the new text, as [[FieldNode#getValue]] would.

## getText()

Returns the text after the change.

## isValid()

If `true`, the new text is a valid value, as [[FieldNode#isValid]] would.

## getPrevious()

Returns the text before the change.
