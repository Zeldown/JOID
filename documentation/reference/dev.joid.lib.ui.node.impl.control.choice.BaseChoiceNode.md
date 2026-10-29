---
summary: The behavior shared by the controls that select one value among options: the switch, the selector and the sliders.
---

A choice holds a list of options and the selected one.
New options keep the selected value when it is still one of them; otherwise the choice shows the value of `fallback(...)`.
A value given by a bound signal that is not an option yet waits until the options hold it; a plain value must be one of the options.
The order of `values(...)` and `value(...)` does not matter.
Attaching a choice without options throws an `IllegalStateException`.

Learn: [Controls](/use/learn/interactivity/controls/#choose-one-of-a-few-options).

## BaseChoiceNode(double,double,double,double)

Creates the choice at `x`, `y` with a `width` and a `height` in design pixels, from the constructor of a subclass.

## values

The options, at least one, in their order on screen.
The reactive setter takes a `Supplier` of a list, and the choice builds its options again each time the list changes.
An empty list throws an `IllegalArgumentException`.

Default: none, `values(...)` is required before the attach

## value

The selected option.
A plain value is the start value, and throws an `IllegalArgumentException` when it is not one of the options; a signal binds the selection both ways.

Default: the first option

## valueIndex

The index of the selected option in the options.
A plain index is the start value, and throws an `IllegalArgumentException` when it is out of the options; an [[IntegerSignal]] binds the index both ways.

Default: `0`, the first option

## getValueList()

Returns the options, as a read-only view that follows the options set later.

## change(V)

Selects an option as if the user picked it: `beforeChange` can refuse it, then `onChange` fires.
Throws an `IllegalArgumentException` for a value that is not an option.

## beforeChange(EventListener)

Adds a listener that runs before a change that a gesture or `change(...)` asks for; `cancel()` refuses it.

## onChange(EventListener)

Adds a listener that runs after each real change of the selected value, whatever its source.

## createOptions()

Hook that JOID calls each time the options change, after the choice stored them.
The switch and the selector build one node per option there; the default does nothing.

## fallback(V)

Hook that picks the value to show when the value asked for is not one of the options.
The default keeps the current value when it is still an option, else the first option; the sliders of numbers return the nearest option.
