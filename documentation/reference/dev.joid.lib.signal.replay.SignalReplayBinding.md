---
summary: Supplier that computes again, from bytecode, the argument of one setter call after the signals it read change.
---

[[SignalReplay]] creates one for each expression it follows; the [[ComputedSignal]] that `Signal.from(value)` returns reads it.

Learn: [How signals work](/use/in-depth/how-signals-work/).

## SignalReplayBinding(SignalReplayCall,SignalReplayMatch,List,T)

Creates the supplier of the call `call`, matched by `match`, that read the signals of `readList` and gave `value`.

## get()

Returns the value of the expression: the value received by the setter on the first read in the same change, then the result of the replay.
When the replay fails, it keeps the last value and prints the dev warning of the line.
