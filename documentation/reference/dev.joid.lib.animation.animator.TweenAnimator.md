---
summary: Read-only `float` signal that a timeline of tweens moves over time, the usual way to animate a node.
capture: reference-animation-slide
alt: A grey square slides to the right end of a white bar, then back to its left end, after a click on the bar.
---

A `TweenAnimator` is a [[Signal]] of a `float` that you never set: a [[Timeline]] of steps moves it, and everything that reads it follows.
Use one for any motion or fade that plays over time: describe the steps with `sequence(...)` and `push(...)`, then call `start()`.
Read its value like any signal, in a node property, a `map(...)` or an `onChange` listener.

```java file="com/example/reference/animation/SlideUI.java" region="slide"
```

A click plays the animator from its current value to `1F` in 600 ms, then back to `0F`, and the square follows it on every frame.
The durations are in milliseconds and each step starts from the value that the previous one reached.
After `start()`, JOID advances the animator once per frame until its timeline ends, until `clear()`, `value(...)` or a call to `sequence(...)`, or until you advance it by hand with `update(...)`.
Its listeners and the steps run on the render thread.

`sequence(...)` comes first: `delay(...)`, `repeat(...)`, `repeatYoyo(...)`, `push(...)` and `start()` act on the timeline it creates, and throw an `IllegalStateException` without one.
The steps and the repetitions are fixed by `start()`: call them before.
A later `sequence(...)` replaces the timeline and its steps.
The event listeners belong to the animator: give them once, before or after the first timeline, and they run for every timeline that follows.
`set(...)` and `reset()` throw an `IllegalStateException`; `value(...)` stops the animation and sets the value.

Learn: [Animation](/use/learn/style-and-motion/animation/).

## create()

Creates an animator whose value is `0F`, without timeline.

## create(float)

Creates an animator whose value is `value`, without timeline.

## timeline

The timeline that moves the value, or `null` when none is playing.
Setting another timeline kills the current one, and the next `update()` counts the time from that moment; give a timeline built from `tween(...)` to drive the animator with nested sequences and parallels.

## speed

The factor applied to the elapsed time: `2F` plays twice as fast, `0.5F` half as fast, `0F` freezes the animation.
`clear()` sets it back to `1F`.

## start()

Starts the timeline from its beginning and lets JOID advance it once per frame, then returns this animator.
Throws an `IllegalStateException` without timeline, or when the timeline is invalid (a block left open, a repetition forever without duration).

## onChange(SignalListener)

Adds a listener that runs with the value after each change, on the render thread, then returns this animator.

## removeOnChange(SignalListener)

Removes a listener given to `onChange(...)`, then returns this animator.

## isWritable()

Returns `false`: only the timeline, `value(...)` and `clear()` change the value.

## isAutomatic()

If `true`, JOID advances the animator once per frame: from `start()` until the timeline ends, `clear()`, `value(...)`, `sequence(...)` or `update(...)`.

## sequence(float,float)

Kills the current timeline and creates a sequence whose first step goes to `value` in `duration` ms, with a linear easing, then returns this animator.

## sequence(float,float,TweenEquation)

Kills the current timeline and creates a sequence whose first step goes to `value` in `duration` ms with `easing`, then returns this animator.

## delay(float)

Adds `delay` ms of waiting before the timeline starts, then returns this animator.

## repeat(int,float)

Plays the timeline `count` more times after the first, with `delay` ms between two plays, then returns this animator; [[BaseTween#INFINITY]] repeats forever.
Throws an `IllegalStateException` once the timeline is started.

## repeatYoyo(int,float)

Repeats like `repeat(...)`, every other play backward, then returns this animator.

## push(float,float)

Adds a step that goes from the value reached to `value` in `duration` ms, with a linear easing, then returns this animator.

## push(float,float,TweenEquation)

Adds a step that goes from the value reached to `value` in `duration` ms with `easing`, then returns this animator.

## tween(float)

Returns a [[Tween]] of `duration` ms that moves the value of this animator, to place in a [[Timeline]] of your own.

## value(float)

Stops the animation, drops the timeline and sets the value, then returns this animator.

## clear()

Stops the animation, sets the value to `0F` and the speed to `1F`, then returns this animator.

## update()

Advances the timeline by the time elapsed since the start or the previous update, and stops the advance once per frame, then returns this animator.

## update(float)

Advances the timeline by `delta` ms, multiplied by the speed, and stops the advance once per frame, then returns this animator.

## onStart(EventListener)

Adds a listener that runs when the timeline starts playing, after its delay.

## onIterationStart(EventListener)

Adds a listener that runs at the start of each play of the timeline.

## onIterationEnd(EventListener)

Adds a listener that runs at the end of each play of the timeline.

## onComplete(EventListener)

Adds a listener that runs when the last play of the timeline ends; it never runs for a repetition forever.

## onReverseStart(EventListener)

Adds a listener that runs when the timeline starts playing backward from its end, after an update with a negative time.

## onReverseIterationStart(EventListener)

Adds a listener that runs at the start of each play backward.

## onReverseIterationEnd(EventListener)

Adds a listener that runs at the end of each play backward.

## onReverseComplete(EventListener)

Adds a listener that runs when the timeline played backward reaches its beginning.

## copy()

Returns a fresh animator with the value, the speed and the listeners of this one, `onChange` included, without its timeline; the two animators are independent afterwards.
