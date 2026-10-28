---
summary: Instructions of the bytecode that compute one argument of a setter call.
---

Learn: [How signals work](/use/in-depth/how-signals-work/).

## of(SignalReplayMethod,int,int,Type,List)

Returns the slice of `method` from `start` to `end` (excluded), whose value has the type `type`; `followingList` holds the slices of the arguments that the same call evaluates after this one.

## hasLambda()

If `true`, the slice creates a lambda.

## isEnclosed()

If `true`, the slice is inside a larger expression that reads a signal before it.

## isParameter()

If `true`, the slice only loads a parameter of its method, boxed or not.

## isEquivalent(SignalReplaySlice)

If `true`, `slice` has the same type and the same instructions.

## encloses(SignalReplaySlice)

If `true`, `slice` lies strictly inside this slice, in the same method.

## convert(Object)

Converts an `int` result to the type of the slice.

## getStart()

Returns the position of the first instruction.

## getEnd()

Returns the position after the last instruction.

## getType()

Returns the type of the value.

## getSignature()

Returns the text that identifies the instructions; two slices with the same text are equivalent.

## getMethod()

Returns the analysed method.

## getSignalNameList()

Returns the names of the signals read, for the warnings.

## getFollowingList()

Returns the slices of the arguments that the call evaluates after this one.

## getSignalCount()

Returns the number of signal reads in the slice.
