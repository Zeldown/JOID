---
summary: Read-only signal whose value comes from a function of other signals, computed again only after one of them changed.
---

A `ComputedSignal<T>` comes from `Signal.from(...)` or `map(...)`.
It computes its value on the first read, records the signals that the function reads with `get()`, and computes again only when one of them changed.
While nothing follows it, it computes on demand; once a listener or another derived signal follows it, it follows its own signals and notifies its listeners when its result changes (by `equals`).
`set(...)` and `reset()` throw an `IllegalStateException`: set the signals it reads instead.

Learn: [Derived values](/use/learn/state/derived-values/), [How signals work](/use/in-depth/how-signals-work/).

## ComputedSignal(Supplier)

Creates a signal computed by `supplier`, for a subclass.

## constant(T)

Returns a signal whose value is `value` forever: it reads no signal and never notifies.

## get()

Returns the value, computed first when a signal it read changed, and records the read like [[Signal#get]].

## peek()

Returns the value, computed first when needed, without recording the read.

## isWritable()

Returns `false`: the value comes from the function.

## isConstant()

If `true`, the value never changes: the signal comes from `Signal.from(value)` with an expression that read no signal.
A node keeps a constant value as a plain value and does not follow it.

## onChange(SignalListener)

Calls `listener` with the result after each change of the result, on the render thread, then returns this signal.
From the first listener on, the signal follows the signals it reads instead of computing on demand.

## removeOnChange(SignalListener)

Removes a listener, then returns this signal; without listeners nor followers, it stops following its signals.

## map(Function)

Returns a signal computed by `function` from this one; on a constant signal, the result is a constant too.

## refuse(String)

Returns the message of the exception that `set(...)` and `reset()` throw.

## refresh()

Computes the value again when a signal it read changed since the last computation.
Throws an `IllegalStateException` when the function reads the signal that it computes.
When the function throws, the exception goes to the caller and the next read computes again.

## dispatch()

Notifies the listeners when the result changed since the last notification.

## addObserver(ComputedSignal)

Registers a derived signal and starts following the signals this one reads.

## removeObserver(ComputedSignal)

Removes a derived signal and stops following its signals when nothing else follows this one.

## markStale(SignalContext)

Marks the value as outdated after a change of a signal it read, schedules its listeners and passes the mark to its own followers.

## depend(Signal)

Records `signal` as read by the computation in progress.
