---
summary: Future that completes on the render thread once the work it wraps is done, so its callbacks can touch the interface.
---

Methods of JOID that load in the background, such as `MsdfFontLoader.load(...)`, return one.
Its callbacks run at the frame after the work ends; `join()` and `get()` called on the render thread wait for the work and complete it at once, without waiting for a frame.

Learn: [Threads](/use/learn/running/threads/).

## from(CompletableFuture)

Returns a future that completes on the render thread with the result or the failure of `source`.

## join()

Returns the result; on the render thread, it waits for the source and completes the future at once.

## get()

Returns the result like `join()`, and throws an `ExecutionException` when the work failed.

## get(long,TimeUnit)

Returns the result like `get()`, waiting `timeout` at most; throws a `TimeoutException` past it.
