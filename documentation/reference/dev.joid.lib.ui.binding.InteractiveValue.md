---
summary: Value of a node that both the code and the user change, with its bounds, its events and its two-way binding to a signal.
---

The controls, the position of a node, its scroll offsets, the zoom of a UI and the camera of `ModelViewerNode` keep their value in an `InteractiveValue`.
It keeps the last value requested by a setter, the bound signal or a gesture, and applies it within the bounds ([Keep a value that the bounds refuse](/use/learn/interactivity/controls/#keep-a-value-that-the-bounds-refuse)); a change runs its steps in a fixed order ([Know the order of a change](/use/learn/interactivity/controls/#know-the-order-of-a-change)).
A custom control creates one in its constructor and calls it from its own setters and input handlers.

Learn: [Controls](/use/learn/interactivity/controls/), [Give your controls a look](/use/learn/going-further/ui-kit/).

## create(NodeBindings,String,Supplier,Consumer)

Creates the value `property` of the node that owns `bindings`; `getter` reads the applied value from the node and `setter` stores it there.

## source

The getter of the value asked for, when the field keeps it apart from the value shown, as the view keeps the zoom asked for a UI; the getter of the value by default.
With a source, the setter receives the value asked for and the getter returns the value shown.

## clamp

The function that bounds every requested value before it applies; the default keeps the value unchanged.
Call `reapply()` after the bounds change.

## then

The action that runs with the previous and the applied value after each change; the default does nothing.

## events(EventType,BiFunction,EventType,BiFunction)

Sets the event types fired around a change and the functions that create the events from the previous and the next value; the `before` event can cancel the change.

## events(V,EventType,BiFunction,EventType,BiFunction)

Sets the events fired when the value changes to `target`, in place of the general ones, as the open and close events of a choice.

## start(V)

Sets the start value from the code and drops the binding to a signal, then returns the node.
The value applies without `before` event.

## bind(Signal)

Binds the value to `signal` in both directions, then returns the node: the node takes the value of the signal and writes each change made by the user to it.
Throws an `IllegalArgumentException` when the signal is read-only.

## change(V)

Applies a change made by the user or by an action of the code, and returns `false` when a `before` listener cancelled it.
During the dispatch of an input event, the change waits until the end of the dispatch, so the listeners of the event read the value before the change.

## update(V)

Applies a value that comes from the code or from the bound signal: no `before` event, then the `on` event when the applied value changed.

## reapply()

Applies the requested value again, after its bounds changed.

## get()

Returns the applied value, or the value waiting for the end of the input dispatch.

## set(V)

Requests `value`: during the dispatch of an input event it waits for `commit()`, otherwise it applies through `change(...)`.

## getRequested()

Returns the last requested value before the bounds, or the applied value when none was requested.

## mark()

Starts the dispatch of an input event: changes wait until `commit()`.

## commit()

Ends the dispatch of an input event and applies the last waiting change.

## before(EventListener)

Adds a listener of the `before` event, then returns the node.

## before(V,EventListener)

Adds a listener of the `before` event of the change to `target`, then returns the node.

## on(EventListener)

Adds a listener of the `on` event, then returns the node.

## on(V,EventListener)

Adds a listener of the `on` event of the change to `target`, then returns the node.
