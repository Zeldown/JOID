---
summary: Every argument expression of one setter call on one line, analysed once and kept for the next replays.
---

Learn: [How signals work](/use/in-depth/how-signals-work/).

## analyze(SignalReplayClass,StackTraceElement,String)

Finds the calls of `setter` on the line of `caller` and slices their arguments.
Throws a [[SignalReplayException]] when the line holds no such call.

## fail(SignalReplayException)

Returns a site that remembers why its line cannot be followed.

## isPassThrough()

If `true`, the call only passes a parameter of its method on, so the replay looks for the expression one frame higher in the stack.

## encloses(SignalReplaySlice)

If `true`, one of the slices of the site encloses `slice`.

## getSliceList()

Returns the slices of the arguments.

## getFailure()

Returns why the line cannot be followed, or `null`.
