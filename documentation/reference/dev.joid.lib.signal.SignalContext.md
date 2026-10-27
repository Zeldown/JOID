---
summary: Per-thread state of the signal engine: batches, silence, read tracking and the queue of notifications.
---

Each thread has its own `SignalContext`, which [[Signal]] and [[ComputedSignal]] use to record reads, group notifications and defer writes made outside the render thread.
Application code uses `Signal.batch(...)` and `Signal.silent(...)` instead.

Learn: [How signals work](/use/in-depth/how-signals-work/).

## current()

Returns the context of the calling thread.

## getEpoch()

Returns the global change counter, which grows on every change of any signal.

## nextEpoch()

Increases the global change counter and returns its value.

## open()

Opens a batch: notifications wait until the matching `close()`.

## close()

Closes a batch; at the outermost level, it sends the deferred writes to the render thread and delivers the notifications.
When listeners throw, the others still run and the first exception is thrown at the end.

## defer(Runnable)

Queues a write for the render thread: at the end of the batch inside a batch, otherwise at once.
The write keeps the silence of the code that queued it.

## settle(Runnable)

Runs `task` after the notifications being delivered, or at once when none are.

## keepSilence(Runnable)

Returns a task that runs `task` with the silence that holds now.

## read(Signal)

Records a read of `signal` for the computation or the read tracking in progress.

## isSilent()

If `true`, the changes on this thread notify no listener.

## silent(boolean)

Sets whether the changes on this thread notify listeners, and returns the previous setting.

## tracing(boolean)

Sets whether reads outside a computation are recorded for the replay of expressions, and returns the previous setting.

## untracked(Supplier)

Returns the result of `supplier` without recording any of its reads.

## observe(ComputedSignal)

Sets the derived signal that receives the reads, and returns the previous one; `null` stops the recording.

## schedule(Signal)

Queues the notification of `signal`, once per delivery.

## countWriteBack(Signal)

Counts the writes back to `signal` during the delivery in progress and returns the count; it returns `0` outside a delivery.
Bound controls use it to stop two controls that keep correcting each other.

## getWriteBackOrigin(Signal)

Returns the value `signal` had before its first write back in the delivery in progress.

## getReadTotal()

Returns the number of reads recorded on this thread since it started.

## hasReads()

If `true`, reads are waiting for the replay of an expression.

## takeReads()

Returns the reads recorded for the replay, oldest first, and forgets them.

## putBackReads(List)

Puts back reads that a replay did not use, before the reads recorded since.

## clearReads()

Forgets the reads recorded for the replay.
