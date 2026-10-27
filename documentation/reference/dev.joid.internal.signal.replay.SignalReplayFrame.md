---
summary: Interpreter that runs the instructions of one expression of the bytecode with the current values of fields and signals.
---

Learn: [How signals work](/use/in-depth/how-signals-work/).

## SignalReplayFrame(SignalReplayRun,SignalReplaySlice,int)

Creates the interpreter of `slice` for `run`; `depth` counts the nested definitions of local variables.

## execute()

Runs the instructions of the slice and returns the value they leave.
Throws a [[SignalReplayException]] when an instruction is not supported or fails.

## convert(Object,Class)

Converts an `int` of the bytecode to the `boolean`, `char`, `byte` or `short` that `type` expects; any other value passes unchanged.

## isLastLive()

If `true`, the last value produced depends on a signal.
