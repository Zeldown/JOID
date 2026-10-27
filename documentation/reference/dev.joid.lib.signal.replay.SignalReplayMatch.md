---
summary: Result of matching one slice of bytecode against the value a setter received and the signals read before it.
---

Learn: [How signals work](/use/in-depth/how-signals-work/).

## of(SignalReplaySlice,SignalReplayRun,Map,boolean[])

Returns the match of `slice` found by `run`, with the text of the local variables in `holeMap` and the reads it used in `consumed`.

## getReadList(List)

Returns the reads of `readList` that the expression used.

## getRemainingList(List)

Returns the reads of `readList` that the expression did not use, left for the next setter.

## getSlice()

Returns the matched slice.

## getSelf()

Returns the object that the bytecode calls `this`, or `null` in a static method.

## isDefinitions()

If `true`, the match runs the definitions of the local variables again.

## getHoleMap()

Returns the text deduced for each local variable that a concatenation uses.

## getPositionalMap()

Returns the signals matched by their position in the reads.

## getConsumed()

Returns, for each read, whether the expression used it.
