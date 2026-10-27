---
summary: Signal bindings, followed suppliers, watches and queued events of one node or one UI.
---

Each [[Node]] and each [[UI]] owns one.
It subscribes to its signals when the node is attached and unsubscribes when it is detached, reads its followed suppliers before each frame, and delivers the events of its [[InteractiveValue]]s in the order of the changes, once the signals have settled.
A custom node reaches it through `getBindings()` to create its own interactive values.

Learn: [Controls](/use/learn/interactivity/controls/), [Give your controls a look](/use/learn/going-further/ui-kit/), [How signals work](/use/in-depth/how-signals-work/).

## NodeBindings(Object,EventBus,Supplier,Consumer)

Creates the bindings of `owner`, which fires its events on `eventBus`, is named by `name` in the warnings and runs each event listener through `runner`.

## open()

Opens a batch: signal notifications and queued events wait until the matching `close()`.

## close()

Closes a batch; at the outermost level, it delivers the queued events in their order.

## release(String)

Drops the binding or the followed supplier of `property`.

## releaseFollows()

Drops every followed supplier and keeps the bindings to signals, as on the copy of a node.

## subscribe(boolean)

Listens to the bound and watched signals, at the attachment of the node.
A bound property takes the value its signal reached while the node was detached; when `loaded` is `true`, a watch whose signal changed meanwhile runs once.

## unsubscribe()

Stops listening to the signals, at the detachment of the node.

## follow(String,Supplier,Consumer)

Applies the value of `supplier` to `property` now and again before each frame when it changes, then returns the owner.
A constant [[ComputedSignal]] applies once and is not kept.

## bind(String,Signal,Consumer,Function)

Binds `property` to `signal` in both directions, then returns the owner: `applier` receives each value of the signal except `null`, and `writer` turns the value of the node into the value to write back.
Throws an `IllegalArgumentException` when the signal is read-only.

## bindNullable(String,Signal,Consumer,Function)

Binds `property` like `bind(...)`, and passes `null` values to `applier` too.

## sync(String,Object)

Writes `value` back to the signal bound to `property`.
When bound controls keep correcting each other's value, it stops after 16 corrections, restores the value of the signal and prints a dev warning.

## batch(Runnable)

Runs `runnable` inside a batch.

## watch(Signal,BooleanSupplier,Runnable)

Runs `action` after each change of `signal` while `condition` is `true` and the node is attached, then returns the owner.

## pull()

Reads every followed supplier and applies the values that changed.
When an applier throws, JOID prints a warning and the property keeps its previous value.

## isBound(String)

If `true`, `property` is bound to a writable signal.

## isFollowed(String)

If `true`, `property` follows a supplier.

## isSubscribed()

If `true`, the node is attached and listens to its signals.

## start(String,V,Consumer)

Drops the binding of `property`, applies `value` through `applier`, then returns the owner.

## external(Runnable)

Runs `runnable` as a change that comes from outside the bound signal, so its write back reaches the signal even when the node has not seen its last value yet.

## see(String)

Records the current value of the signal bound to `property` as seen by the node.

## track(InteractiveValue)

Registers an interactive value, so input dispatches mark and commit it.

## mark()

Marks every interactive value at the start of an input dispatch.

## commit()

Commits every interactive value at the end of an input dispatch.

## listen(EventType,EventListener)

Adds `listener` for the events of `type`, then returns the owner.

## fire(EventType,Supplier)

Creates the event with `event` and fires it, and returns it; it returns `null` and creates nothing when no listener waits for this type.
A cancellable or consumable event fires at once; another one waits for the end of the batch in progress.

## fireSubscribed(EventType,Supplier)

Fires like `fire(...)` while the node is attached, after the signal notifications in progress; it returns `null` when the node is detached.
