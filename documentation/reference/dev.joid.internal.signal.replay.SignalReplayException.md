---
summary: Reason why the replay cannot follow an expression, with the advice that the dev warning prints.
---

The replay throws and catches it internally; it reaches application code only through the warning text.

Learn: [How signals work](/use/in-depth/how-signals-work/).

## SignalReplayException(SignalReplayFailure,Object[])

Creates the exception of `failure`, whose message is its reason filled with `arguments`.

## getAdvice()

Returns the fix that the warning suggests, filled with the arguments.

## getArguments()

Returns the values that fill the reason and the advice.

## getFailure()

Returns the kind of failure.
