---
title: "Animation"
description: "Choose an animation tool, then use it: animators, easing, repetition, following a moving target and the tween engine."
genre: learn
sidebar:
  order: 3
---

JOID animates on its own clock, so an animation takes the same time at 30 or at 240 frames per second.
It offers a few tools, each made for one kind of motion.
This page helps you pick the right one, then shows how to use it.

**You will learn**
- Which tool fits which motion
- How to animate a value with an animator, and shape it with easing
- How to chain, repeat and replay an animation
- How to follow a moving target
- How to animate values of your own with the tween engine

## Choose the right tool

Start from what you want to see:

| You want | Use |
|---|---|
| A node that changes while the mouse is over it | The hover progress of the node ([Hover, tooltips and cursors](/use/learn/interactivity/hover-and-tooltips/)) |
| A value that goes from A to B: once, in a loop, or when the user clicks | A [[TweenAnimator]] ([Animate a value](#animate-a-value)) |
| A value that chases a target that keeps moving, such as the mouse | [[UI#lerpByFramerate]] ([Follow a moving target](#follow-a-moving-target)) |
| Several values of your own, in timelines with pauses and listeners | The tween engine: [[Tween]], [[Timeline]], [[TweenManager]] ([Animate values of your own](#animate-values-of-your-own)) |
| A UI that animates when it opens or closes | A [transition](/use/learn/style-and-motion/transitions/) |

Every tool produces a number; a node turns it into motion by reading it in a setter.

## Animate a value

A `TweenAnimator` moves one `float` over time:

```java file="com/example/style/AnimationUI.java" region="appear"
```

<figure>

![A dark grey card fades in while it slides up into place](/captures/style-appear.webp)

<figcaption>The value goes from 0 to 1 in 600 ms; the opacity and the position read it.</figcaption>
</figure>

1. `TweenAnimator.create(0F)` starts the value at `0F`.
2. `sequence(600F, 1F, ...)` describes a step: reach `1F` in 600 ms with the [[TweenEquations#CUBIC_OUT]] easing.
3. `start()` starts it: JOID advances it on every frame until the step ends.

An animator is a signal of its own: `get()` reads its value, and the lambdas of the setters read it on every frame.
It is read-only: `set(...)` throws, [[TweenAnimator#value]] puts a value at once and drops the steps, and [[TweenAnimator#clear]] stops it and puts it back to `0F`.

## Shape the motion with easing

An easing equation decides how the value travels from its start to its target.
Each family has three variants: `IN` starts slowly, `OUT` ends softly and `IN_OUT` does both.
The [catalog](/use/catalog/#easing-curves) draws the curves of every family.

| Feel | Equation |
|---|---|
| Constant speed, for loops that never stop | [[TweenEquations#LINEAR]], the easing of a step without one |
| Fast start, soft landing, for what appears or reacts to the user | `QUAD_OUT`, `CUBIC_OUT`, `QUART_OUT`, from gentle to strong |
| Soft at both ends, for back and forth motion | `SINE_IN_OUT`, `QUAD_IN_OUT` |
| Slow start, fast end, for what leaves the screen | `QUART_IN`, `CUBIC_IN` |
| Overshoots the target, then settles | `BACK_OUT` |
| Springs or bounces around the target | `ELASTIC_OUT`, `BOUNCE_OUT` |

[[BackTweenEquation]] and [[ElasticTweenEquation]] take settings.
This square overshoots its target on each trip, further than the default `BACK_OUT`:

```java file="com/example/style/AnimationUI.java" region="easing"
```

<figure>

![A blue square on a dark track goes right, passes its target and settles back on it, then returns left, passes its start and settles there, and starts again](/captures/style-easing.webp)

<figcaption>
`BACK_OUT.overshoot(3F)` carries the square a quarter of the trip beyond each end before it settles.
</figcaption>
</figure>

`overshoot(...)` returns a changed copy and leaves `TweenEquations.BACK_OUT` as it is, and `TweenEquations.ELASTIC_OUT.amplitude(...).period(...)` changes the spring.
[[TweenEquations#of]] finds an easing by its name, such as `"Quad.OUT"` or `"Back.IN_OUT"`, to read it from a settings file; it returns `null` for an unknown name.
The name of a curve is its `toString()`, settings included, such as `"Back.OUT(2.5)"`, so a curve written to a file comes back the same.
For any other curve, extend [[TweenEquation]] and return the eased progress from `compute(t)`, where `t` goes from `0F` to `1F`.

## Chain, repeat and replay

`push(...)` adds a step after the previous one, and `repeat(...)` plays the whole chain again:

```java file="com/example/style/AnimationUI.java" region="loop"
```

<figure>

![A blue square glides from the left end of a dark track to the right end and back, pauses, and starts again](/captures/style-loop.webp)

<figcaption>Two steps, then a pause of 200 ms, forever.</figcaption>
</figure>

- `repeat(count, delay)` plays the chain `count` more times, waiting `delay` ms between plays; `repeatYoyo(count, delay)` plays every second time backward.
- [[BaseTween#INFINITY]] repeats forever.
- `delay(ms)` waits before the first play, and `speed(2F)` plays twice as fast; `speed(0F)` holds the value where it is.
- Set the repetitions before `start()`: a started animation refuses them with an `IllegalStateException`.

:::caution
A started animator runs until its steps end, whether a UI shows it or not.
An animator that repeats forever keeps running after its UI closes: stop it with `clear()` in the [[Node#onDetach]] of the node that shows it, as above.
:::

To play an animation on demand, call `sequence(...)` again: it replaces the running steps and starts from the current value, so a click in the middle of the motion turns it around smoothly.

```java file="com/example/style/AnimationUI.java" region="click"
```

<figure>

![A click on the blue Toggle button slides a blue square to the right end of its track, and the next click slides it back; the counter goes from "Plays: 0" to "Plays: 2"](/captures/style-click.webp)

<figcaption>Each click starts a 400 ms step toward the other end, and `onComplete` counts the plays that reached their end.</figcaption>
</figure>

`onStart`, `onIterationEnd`, `onComplete` and the other listeners of [[TweenAnimator]] belong to the animator and run for every sequence that follows.
Give them once, where you create the animator: given in the `onClick` that starts a sequence, a listener adds up, and the tenth play runs it ten times.
Once its steps end, the animator drops them: `start()` alone then throws an `IllegalStateException` until the next `sequence(...)`.

Two values that move apart need two animators, each read by its own setter.
To update several properties from one animator, `animate(animator)` on a node calls [[Node#onAnimate]] on each frame where the value changed:

```java fragment
node.animate(slide).onAnimate(event -> event.getNode().x(260D * event.getValue()).y(40D * event.getValue()))
```

## Follow a moving target

An animation with a fixed duration fits a target that stays put.
When the target keeps moving, move the value a share of the remaining distance on each frame with [[UI#lerpByFramerate]], in the `update()` of the UI:

```java file="com/example/style/AnimationUI.java" region="follow-field"
```

```java file="com/example/style/AnimationUI.java" region="follow-update"
```

```java file="com/example/style/AnimationUI.java" region="follow-node"
```

<figure>

![A blue disc follows the pointer from left to right along a dark grey band and back, with a smooth delay](/captures/style-follow.webp)

<figcaption>The disc covers a share of the distance to the pointer on every frame, so it slows down as it arrives.</figcaption>
</figure>

`lerpByFramerate(value, target, speed, snapDiff)` returns the next value.
`speed` is the share of the distance covered in one frame at 60 frames per second, from `0` to `1`; JOID scales it by the real frame time, so the motion keeps its pace at any frame rate.
Once the distance falls to `snapDiff` or under, the value jumps to the target.

## Animate values of your own

`TweenAnimator` is built on a tween engine that you can use directly, to animate any `float` you can read and write.
A [[Tween]] takes a getter, a setter and a duration, a [[Timeline]] arranges tweens, and a [[TweenManager]] advances them when you call its `update(...)`:

```java file="com/example/style/TweenUI.java" region="fields"
```

```java file="com/example/style/TweenUI.java" region="update"
```

```java file="com/example/style/TweenUI.java" region="timeline"
```

```java file="com/example/style/TweenUI.java" region="node"
```

<figure>

![A blue square moves right, pauses, drops down with a small overshoot, then plays the same path backward](/captures/style-tween.webp)

<figcaption>A sequence of two tweens with a pause, played back and forth.</figcaption>
</figure>

`killAll()` comes first because `init()` runs again at each opening and reload, and the previous timeline would keep playing next to the fresh one.

- `Tween.to(getter, setter, ms)` goes from the current value to `target(...)`, `Tween.from(...)` comes from `target(...)` to the current value, `Tween.set(...)` applies `target(...)` at once, and `Tween.call(listener)` runs code when a timeline reaches it.
- `targetRelative(...)` adds an offset to the start value, and `waypoint(...)` adds values to pass through, on a smooth curve, or on straight segments with [[TweenPaths#LINEAR]] given to `path(...)`; a path of your own implements [[ITweenPath]].
- `Timeline.sequence()` plays its children one after the other and `Timeline.parallel()` together; `beginSequence()`, `beginParallel()` and `end()` nest them, and `pushPause(ms)` waits.
- A tween or a timeline has the `repeat`, `delay` and listeners of an animator, and `pause()`, `resume()` and `kill()`.
- `start()` without a manager starts a tween that you advance with its own `update(delta)`.
- [[TweenAnimator#timeline]] gives an animator a timeline that you build, such as one with a pause, from tweens made by its `tween(ms)`.

## Recap

- Pick the tool from the motion: the **hover progress** for hover, a **`TweenAnimator`** for a value from A to B, **`lerpByFramerate`** for a moving target, the **tween engine** for values of your own.
- An **animator** is a read-only signal that JOID advances after `start()`; setters read it in a lambda.
- **Easing**: `OUT` for what appears or reacts, `IN` for what leaves, `IN_OUT` for back and forth; **`TweenEquations.of`** finds one by name.
- **`push`** chains steps, **`repeat`** and **`repeatYoyo`** replay them, set before `start()`; `sequence(...)` again replays from the current value.
- An animator that repeats forever runs until you **`clear()`** it.
- The tween engine needs **tweens** on a getter and a setter, a **timeline** to arrange them, and a **manager** advanced in `update()`.

## Try it

**Pulse forever.**
Make a circle grow from 1 to 1.2 times its size and back, forever, soft at both ends.

<details>
<summary>Solution</summary>

```java fragment
final TweenAnimator pulse = TweenAnimator.create(1F).sequence(600F, 1.2F, TweenEquations.SINE_IN_OUT);
pulse.repeatYoyo(BaseTween.INFINITY, 0F).start();
final Scale scale = Scale.create(() -> (double) pulse.get(), () -> (double) pulse.get(), () -> 1D);
CircleNode.create(0, 0, 100).effect(TransformNodeEffect.create(Transformation.create().scale(scale))).attach(this);
```

[[Scale]] scales around the center of the node ([Effects](/use/learn/style-and-motion/effects/)); add `onDetach(event -> pulse.clear())` to stop it with the UI.

</details>

**Read the easing from a file.**
A settings file holds the name of the easing of a menu, such as `"Cubic.OUT"`, and a missing or wrong name must fall back to `QUAD_OUT`.

<details>
<summary>Solution</summary>

```java fragment
final TweenEquation found = TweenEquations.of(name);
final TweenEquation easing = found != null ? found : TweenEquations.QUAD_OUT;
```

</details>
