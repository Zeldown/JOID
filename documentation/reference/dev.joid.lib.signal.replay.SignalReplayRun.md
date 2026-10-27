---
summary: One execution of a slice of bytecode, during the match or during a replay, with the signals it reads.
---

Learn: [How signals work](/use/in-depth/how-signals-work/).

## match(Object,boolean,List)

Returns a run that matches a slice against the recorded reads; `positionalList` gives the signals to use by position, or `null`.

## replay(Object,boolean,Map,Map)

Returns a run that computes a matched slice again, with the deduced texts and the signals matched by position.

## run(SignalReplaySlice)

Runs `slice` and returns its value.

## define(SignalReplaySlice,int)

Runs the definition of a local variable once and returns its value and whether it depends on a signal, or `null` when it cannot run.

## position(Object,Class)

Returns the signal matched by position for `key`; throws a [[SignalReplayException]] when the reads do not fit.

## getDefinitionList()

Returns the reads made by the definitions of local variables, in the order of the code.

## getSelf()

Returns the object that the bytecode calls `this`.

## isDefinitions()

If `true`, the run computes the definitions of local variables again.

## getHoleMap()

Returns the texts deduced for the local variables.

## getPositionalMap()

Returns the signals matched by position.

## getDirectList()

Returns the reads made by the slice itself.

## getDefinitionCache()

Returns the values of the definitions already run.

## isPositionalNeeded()

If `true`, the match needs the signals by position, since the slice reads a signal it cannot find by itself.
