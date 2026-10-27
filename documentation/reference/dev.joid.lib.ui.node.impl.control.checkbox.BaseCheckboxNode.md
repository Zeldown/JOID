---
summary: The behavior of a checkbox without its look: a `boolean` that a left press on the node toggles.
---

Extend [[CheckboxNode]] for a look made of a box and a mark; extend `BaseCheckboxNode` only to build the look entirely yourself.
The value follows the pattern of every control: a start value, a signal bound both ways, `change(...)`, `beforeChange` and `onChange`.

Learn: [Controls](/use/learn/interactivity/controls/#turn-an-option-on-or-off), [UI kit](/use/learn/going-further/ui-kit/#draw-a-control-entirely-your-own-way).

## BaseCheckboxNode(double,double,double,double)

Creates the checkbox at `x`, `y` with a `width` and a `height` in design pixels, from the constructor of a skin.

## checked(boolean)

Gives the checkbox its start value; the box shows it, and a box attached already fires `onChange` when it differs.
A setter is never refused.

## checked(Signal)

Binds the value to a signal both ways: the box shows the signal, and a press writes the new value into it.
Throws an `IllegalArgumentException` for a signal that cannot be written, such as a derived value.

## change(boolean)

Sets the value as if the user pressed the box: `beforeChange` can refuse it, then `onChange` fires.

## beforeChange(EventListener)

Adds a listener that runs before a change that a press or `change(...)` asks for, with the value now and the value asked for; `cancel()` refuses it.

## onChange(EventListener)

Adds a listener that runs after each real change of the value, whatever its source.
It does not run for a value equal to the current one.

## isChecked()

If `true`, the box is checked.

## mousePressed(NodeMouseEvent)

Toggles the value on a left press over the box that no one consumed, and consumes the press.
