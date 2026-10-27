---
summary: Shared state and timing of a tween and a timeline: delay, repetitions, pause, events and the advance in time.
---

A [[Tween]] or a [[Timeline]] does nothing by itself: something advances it with `update(delta)`, in milliseconds.
A [[TweenAnimator]] advances its timeline once per frame; a [[TweenManager]] advances what it holds when you update it; code of your own can call `update(...)` itself.
A negative delta plays backward.

Learn: [Animation](/use/learn/style-and-motion/animation/).

## BaseTween()

Creates a tween that is not started, for a subclass.

## INFINITY

Repeat count that repeats forever (`-1`).

## delay

The time in ms before the first play; `delay(...)` adds to it.

## duration

The length of one play in ms; a timeline computes it from its children when it is built.

## getFullDuration()

Returns the delay, every play and every repeat delay added together, in ms, or `-1F` for a repetition forever.

## isFinished()

If `true`, the last play ended or the tween was killed.

## getDelay()

Returns the delay in ms.

## isYoyo()

If `true`, every other play runs backward.

## getRepeatCount()

Returns the number of plays after the first, or `INFINITY`.

## getDuration()

Returns the length of one play in ms.

## getRepeatDelay()

Returns the time between two plays in ms.

## getStep()

Returns the position in the plays: negative before the start, an even number during a play, an odd number between two plays.

## isPaused()

If `true`, updates do not advance the tween.

## getCurrentTime()

Returns the time in ms spent in the current play or wait.

## isStarted()

If `true`, `start()` was called.

## isInitialized()

If `true`, the delay elapsed and the tween read its start values.

## getUserData()

Returns the object given to `userData(...)`, or `null`.

## start()

Builds the tween and places it at its beginning, then returns it; the next updates play it.
Throws an `IllegalStateException` for a repetition forever whose duration and repeat delay are both `0`.

## start(TweenManager)

Adds the tween to `manager`, which starts it, then returns it.

## build()

Fixes the children and the duration of a timeline, then returns it; `start()` calls it.

## kill()

Stops the tween and its children for good, then returns it; `isFinished()` becomes `true`.
Killed from one of its own listeners, it stops within the same update.

## pause()

Freezes the tween, then returns it.

## resume()

Lets updates advance the tween again, then returns it.

## delay(float)

Adds `delay` ms before the first play, then returns the tween.

## repeat(int,float)

Plays the tween `count` more times after the first, with `delay` ms between two plays, then returns it; a negative delay counts as `0`.
Throws an `IllegalStateException` once the tween is started.

## repeatYoyo(int,float)

Repeats like `repeat(...)`, every other play backward, then returns the tween.

## userData(Object)

Attaches an object of your own to the tween, then returns it.

## forceToStart()

Puts the tween before its first play and applies its start values, then returns it.

## forceToEnd(float)

Puts the tween after its last play and applies its end values, then returns it; `time` is the time already spent past the end.

## update(float)

Advances the tween by `delta` ms, backward when `delta` is negative, firing the events it crosses.
It does nothing before `start()`, while paused or once killed.

## isValid(int)

If `true`, `step` is within the plays of the tween.

## isReverse(int)

If `true`, the play at `step` runs backward in a yoyo.

## killChildren()

Kills the children; a timeline overrides it.

## startChildren()

Starts the children; a timeline overrides it.

## buildChildren()

Builds the children; a timeline overrides it.

## initializeValues()

Reads the start values when the delay ends.

## updateValues(int,int,boolean,float)

Applies the values for the time reached, after each advance.

## forceStartValues()

Applies the values of the beginning of a play.

## forceEndValues()

Applies the values of the end of a play.

## onStart(EventListener)

Fires when the first play starts, after the delay.

## onIterationStart(EventListener)

Fires at the start of each play.

## onIterationEnd(EventListener)

Fires at the end of each play.

## onComplete(EventListener)

Fires when the last play ends; it never fires for a repetition forever.

## onReverseStart(EventListener)

Fires when the tween starts playing backward from its end.

## onReverseIterationStart(EventListener)

Fires at the start of each play backward.

## onReverseIterationEnd(EventListener)

Fires at the end of each play backward.

## onReverseComplete(EventListener)

Fires when the tween played backward reaches its beginning.
