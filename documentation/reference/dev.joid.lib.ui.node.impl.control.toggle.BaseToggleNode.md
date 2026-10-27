---
summary: The behavior of a toggle without its look: a `boolean` that a left press switches, and the two values it stands for.
---

Extend [[ToggleNode]] for a look made of a track and a knob; extend `BaseToggleNode` only to build the look entirely yourself.
`F` is the type of the value when the toggle is on, `S` when it is off.

Learn: [Controls](/use/learn/interactivity/controls/#choose-between-two-values), [UI kit](/use/learn/going-further/ui-kit/#draw-a-control-entirely-your-own-way).

## BaseToggleNode(double,double,double,double)

Creates the toggle at `x`, `y` with a `width` and a `height` in design pixels, from the constructor of a skin.

## getValue()

Returns the value that the toggle stands for: the first value of `state(...)` while it is on, the second one while it is off, or a `Boolean` without a state.

## state(F,S)

Gives the two values that `getValue()` returns, such as `"Night"` and `"Day"`; it does not change whether the toggle is on.

## toggle(boolean)

Gives the toggle its start value: `true` is on; a toggle attached already fires `onChange` when it differs.
A setter is never refused.

## toggle(Signal)

Binds the on state to a signal both ways: the toggle shows the signal, and a press writes the new state into it.
Throws an `IllegalArgumentException` for a signal that cannot be written.

## change(boolean)

Sets the state as if the user pressed the toggle: `beforeChange` can refuse it, then `onChange` fires.

## beforeChange(EventListener)

Adds a listener that runs before a change that a press or `change(...)` asks for; `cancel()` refuses it.

## onChange(EventListener)

Adds a listener that runs after each real change of the state, whatever its source.

## isToggle()

Returns `true` while the toggle is on.

## getState()

Returns the pair given to `state(...)`, or `null` before it.

## mousePressed(NodeMouseEvent)

Switches the state on a left press over the toggle that no one consumed, and consumes the press.
