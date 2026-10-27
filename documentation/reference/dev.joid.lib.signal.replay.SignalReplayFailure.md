---
summary: Kinds of expression that the replay cannot follow, each with its reason and its advice.
---

Each value gives the end of the dev warning that names the line.
The advice is always the same idea: compute the value with `map(...)`, with `Signal.from(() -> ...)` or with a lambda read at every frame.

Learn: [Signals](/use/learn/interactivity/signals/#what-joid-follows), [How signals work](/use/in-depth/how-signals-work/).

## getReason()

Returns the reason, a format with `%s` for the details.

## getAdvice()

Returns the advice, a format with `%s` for the details.

## CLASS_NOT_FOUND

The bytecode of the calling class cannot be read.

## MEMBER_NOT_FOUND

A field, method or class named in the bytecode does not exist at runtime, for example after a renaming that the [[ISignalReplayBridge]] does not map.

## CALL_NOT_FOUND

The call of the setter is not on the line, often because the `.class` file was compiled again since the launch.

## AMBIGUOUS_CALL

Several calls of the same setter on one line give the same value from the same signals; write one call per line.

## UNSUPPORTED_INSTRUCTION

The expression uses an instruction that the replay does not run.

## LAMBDA

The expression contains a lambda.

## LOCAL_COMBINED

A local variable is combined with a signal in the expression.

## LOCAL_CONDITION

A local variable decides a condition of the expression.

## VALUE_DIFFERS

Running the expression again gives another value than the one received: it has side effects or reads the time or a random value.

## SIGNALS_DIFFER

Running the expression again reads other signals than the first time.

## ACCESS_REFUSED

The module of a class refuses the reflective access; the advice gives the `opens` line to add to its `module-info`.

## REPLAY_FAILED

The replay threw while it ran the expression.
