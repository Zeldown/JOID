---
summary: Entry point of the replay that turns a plain value passed to a setter into a signal that follows the signals its expression read.
---

When a setter receives a plain value, `Signal.from(value)` asks `SignalReplay` whether signals were read right before.
If they were, it finds the calling line in the stack, reads its bytecode, isolates the instructions that computed the argument and runs them again after each change of those signals.
When it cannot follow the expression, the value stays fixed and dev mode prints one warning per line, with the reason and the fix.

Learn: [How signals work](/use/in-depth/how-signals-work/), [Signals](/use/learn/interactivity/signals/#what-joid-follows).

## replay(T)

Returns a supplier that computes again the expression that produced `value`, or `null` when no signal was read or when JOID cannot follow it.

## run(Object,Object,Runnable)

Runs `runnable` with read tracking on, for the code of `owner` (a UI or a node) and of `parent` (the UI of a node), so the replay can find the object that the bytecode calls `this`.
JOID calls it around `init()`, bodies and watches.

## reset()

Forgets the reads and the call order recorded on the calling thread.

## clear()

Forgets every bytecode analysis kept in memory, so the next replay reads the classes again.

## warn(SignalReplayCall,List,int,SignalReplayException,Object)

Prints the dev warning of a line that JOID cannot follow, once per line: the file and line, the setter, the signals read, the reason, the value that stays and the advice.
