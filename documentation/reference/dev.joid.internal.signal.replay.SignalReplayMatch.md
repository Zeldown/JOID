---
summary: Result of matching one slice of bytecode against the value a setter received and the signals read before it.
---

Learn: [How signals work](/use/in-depth/how-signals-work/).

## of(SignalReplaySlice,SignalReplayRun,Map,boolean[],boolean)

Returns the match of `slice` found by `run`, with the text of the local variables in `holeMap`, the reads it used in `consumed`, and `followed` when the reads only matched together with those of the arguments that the call evaluates after this one.

## getReadList(List)

Returns the reads of `readList` that the expression used.

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

## isFollowed()

If `true`, the match needed the reads of the arguments evaluated after the slice; the replay keeps it only when no other match does without them.
