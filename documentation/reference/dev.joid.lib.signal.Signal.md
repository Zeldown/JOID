---
summary: Holds a value that nodes, derived signals and listeners follow, so the interface shows each change without rebuilding.
capture: reference-signals-counter
alt: A text that reads "Clicks: 2" above a white button, after two clicks on the button.
---

A `Signal<T>` holds one value that changes while a UI is open: a count, a name, a list, a setting.
A node property set from an expression that reads the signal with `get()` follows it: JOID computes the expression again after each change and the node shows the result.
Use a signal for every value that the interface must show while it changes; keep plain fields for values that never change while the UI is open.

```java file="com/example/reference/signals/CounterUI.java" region="counter"
```

Each click calls `increment()`, and the text node shows the value that `"Clicks: " + this.clicks.get()` gives with the current count.
The typed signals add operations for their type: [[IntegerSignal]], [[LongSignal]], [[FloatSignal]], [[DoubleSignal]], [[BooleanSignal]], [[StringSignal]], [[ListSignal]], [[SetSignal]] and [[MapSignal]].
`map(...)` and `Signal.from(...)` give a read-only [[ComputedSignal]] computed from other signals.

A write made on the render thread applies at once; a write made on another thread applies on the render thread, at the next frame.
Listeners always run on the render thread.

Learn: [Signals](/use/learn/interactivity/signals/), [Derived values](/use/learn/state/derived-values/), [Threads](/use/learn/running/threads/), [How signals work](/use/in-depth/how-signals-work/).

## Signal()

Creates a signal whose value and default value are `null`.

## Signal(T)

Creates a signal whose value and default value are `value`; `reset()` comes back to this value.

## from(T)

Wraps the value of an expression in a read-only [[ComputedSignal]] that follows the signals the expression read.
When the expression read signals with `get()` right before this call, JOID reads the bytecode of the calling line and computes the expression again after each change of those signals.
When it read none, or when JOID cannot follow it, the result is a constant signal, and dev mode prints a warning that names the line and the fix.
[Signals](/use/learn/interactivity/signals/#what-joid-follows) lists the expressions that JOID follows.

## from(Supplier)

Creates a read-only [[ComputedSignal]] whose value is the result of `supplier`.
JOID records the signals that the supplier reads with `get()` and calls it again only after one of them changed.
In dev mode, a supplier that reads no signal prints a warning, since its value never changes.

## batch(Runnable)

Runs `runnable` and holds back the notifications of every signal it changes until it ends, so each listener runs once, with the final value.
Batches nest: the notifications run when the outermost batch ends.
Writes made from another thread inside a batch reach the render thread together.

## silent(Runnable)

Runs `runnable` without calling the `onChange` listeners of the signals it changes.
The values still change, and the derived signals compute the new result when they are read.

## silent(Consumer)

Runs `action` with this signal, without calling the `onChange` listeners of the signals it changes, then returns this signal.
It is the fluent form of `Signal.silent(Runnable)`.

## get()

Returns the value and records the read, so the node property, the [[ComputedSignal]] or the expression that is being computed follows this signal.

## peek()

Returns the value without recording the read: nothing follows the signal through this call.
Use it in listeners and in code that must not depend on the signal.

## isPresent()

If `true`, the value is not `null`.
The read is recorded like `get()`.

## isWritable()

If `true`, `set(...)` and `reset()` change the value; a [[ComputedSignal]] and a [[TweenAnimator]] return `false`.

## toString()

Returns the simple name of the class and the value, as in `IntegerSignal{3}`.

## set(T)

Sets the value, then returns this signal.
A value equal to the current one (by `equals`) changes nothing and notifies no one.
`null` is a value like any other: the listeners receive `null` and `isPresent()` returns `false`.
Called from another thread, the write applies on the render thread at the next frame.
Throws an `IllegalStateException` when the signal is read-only (`isWritable()` is `false`).

## reset()

Sets the value back to the default value given to the constructor, then returns this signal.
Throws an `IllegalStateException` when the signal is read-only.

## onChange(SignalListener)

Calls `listener` with the value after each change, on the render thread, then returns this signal.
Inside a batch, the listener runs once when the batch ends; inside `silent(...)`, it does not run.
Adding the same listener twice keeps one; when a listener throws, the others still run and the first exception is thrown afterwards.

## removeOnChange(SignalListener)

Removes a listener given to `onChange(...)`, then returns this signal.

## map(Function)

Returns a read-only [[ComputedSignal]] whose value is `function` applied to the value of this signal.
The function runs again only after this signal changed, and only when the result is read.

## fireChange()

Notifies the listeners and the derived signals with the current value, without changing it, then returns this signal.
Use it after changing the inside of a mutable value that the signal holds.

## fireChange(T)

Calls every listener with `value`; when a listener throws, the others still run and the first exception is thrown at the end.
For subclasses: it does not touch the value nor the derived signals.

## refuse(String)

Returns the message of the exception that `set(...)` and `reset()` throw when the signal is read-only; `action` describes the refused write.
Override it to explain how to change the value of a read-only subclass.

## seal(T)

Returns the value to store for each write; the signal stores `value` itself.
The collection signals override it to store a read-only copy.

## refresh()

Brings the value up to date before a read; a plain signal does nothing.
[[ComputedSignal]] overrides it to compute its value again when a signal it read changed.

## dispatch()

Runs once per change when the notifications are delivered, and calls the listeners with the value.
Override it to filter or delay what the listeners receive.

## getVersion()

Returns a counter that grows on each change; derived signals compare it to know whether they are outdated.

## isObserved()

If `true`, a derived signal or a listener follows this signal.

## addObserver(ComputedSignal)

Registers a derived signal to mark as outdated when this signal changes.

## removeObserver(ComputedSignal)

Removes a derived signal registered by `addObserver(...)`.

## write(Runnable)

Runs `task` now on the render thread, or queues it for the render thread when it is called from another thread, then returns this signal.
Every write of a subclass goes through it.

## transform(UnaryOperator)

Writes the result of `operator` applied to the value, then returns this signal.
The operator reads the value when the write runs, so increments queued from another thread add up instead of overwriting each other.

## hasListeners()

If `true`, at least one `onChange` listener is registered.

## invalidate(SignalContext)

Marks every derived signal that follows this signal as outdated.

## nextVersion()

Increases the version counter without notifying anyone.

## fire()

Increases the version, marks the derived signals as outdated and schedules the listeners, as a change does.
