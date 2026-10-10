# Animation

JOID animates on the frame clock, so motion looks the same at any frame rate. Every node has a hover animation, and a `TweenAnimator` animates any value you choose. To animate a whole UI when it opens and closes, see the transitions of [UIs](uis.md).

```java
final TweenAnimator fade = TweenAnimator.create(0F).sequence(500F, 1F, TweenEquations.CUBIC_OUT).start();

RectNode
.create(760, 440, 400, 200)
.color(() -> Color.WHITE.copyAlpha(fade.getValue()))
.animate(fade)
.attach(this);
```

![A white rectangle fading in](../images/ess-anim-fade.gif "The lambda reads the animator value, from 0 to 1 in 500 ms.")

## The hover animation

Every node animates a hover value from `0` to `1` when the mouse enters it, and back when it leaves. Hovered colors follow it; `hoverValue(max)` scales it to drive anything else:

```java
RectNode
.create(100, 200, 300, 200)
.color(Color.WHITE)
.hoverDuration(250L)
.hoverEquation(TweenEquations.CUBIC_OUT)
.self(node -> node.effect(RoundedNodeEffect.create(() -> 8F + node.hoverValue(8F))))
.self(node -> node.effect(TransformNodeEffect.create(new TranslateTransformOperation(Vector.Y(() -> (double) -node.hoverValue(12F))))))
.attach(this);
```

![The cursor hovers a white card that lifts slightly and rounds its corners](../images/ess-anim-lift.gif "The card lifts by 12 units and its radius grows from 8 to 16 while hovered.")

`hoverDuration(...)` sets the length in milliseconds (`200L` by default) and `hoverEquation(...)` the easing (`LINEAR` by default).

## Animating a value with TweenAnimator

`TweenAnimator` (`dev.joid.lib.animation.animator`) animates one `float`:

![An animator is created, given its steps with sequence, started, advanced every frame by the node it is passed to, and read with getValue](../images/ess-diagram-animator.png "How a TweenAnimator drives a node.")

1. `create(0F)` starts the value at `0F`.
2. `sequence(500F, 1F, equation)` describes a step: reach `1F` in 500 ms. `push(...)` adds steps after it.
3. `start()` starts the animation.
4. `animate(animator)` makes the node advance it every frame; a lambda reads `getValue()`.

```java
final TweenAnimator wobble = TweenAnimator
.create(0F)
.sequence(150F, 10F, TweenEquations.CUBIC_OUT)
.push(150F, -10F, TweenEquations.CUBIC_INOUT)
.push(150F, 0F, TweenEquations.CUBIC_IN)
.start();

RectNode.create(100, 100, 100, 100).color(Color.GRAY).x(() -> 100D + wobble.getValue()).animate(wobble).attach(this);
```

`parallel(...)` instead of `sequence(...)` starts every pushed step at the same time.

## Moving nodes with onAnimate

`onAnimate((node, animator, value) -> ...)` runs on each frame where an animator of the node changed. Animate a progress from `0F` to `1F` and map it:

```java
final TweenAnimator move = TweenAnimator.create(0F).sequence(1000F, 1F);
move.getTimeline().repeatYoyo(Tween.INFINITY, 0F);
move.start();

RectNode
.create(0, 0, 100, 100)
.color(Color.LIGHTGRAY)
.onAnimate((node, animator, value) -> node.x((1920 - 100) * value).y((1080 - 100) * value))
.animate(move)
.attach(this);
```

![A light gray square sliding diagonally across the whole canvas and back](../images/ess-anim-move.gif "One second from corner to corner, back and forth forever.")

On `getTimeline()`, before `start()`: `repeatYoyo(count, delay)` plays back and forth, `repeat(count, delay)` replays from the start; `Tween.INFINITY` repeats forever.

## Animations started by the user

`sequence(...)` replaces the running animation, which continues from the current value. A side panel that opens and closes on click:

```java
private final TweenAnimator slide = TweenAnimator.create(0F);

RectNode
.create(-300, 0, 300, 1080)
.color(Color.LIGHTGRAY)
.onAnimate((node, animator, value) -> node.x(-300 + 300 * value))
.animate(this.slide)
.attach(this);

RectNode
.create(1700, 20, 200, 60)
.color(Color.GRAY)
.onClick((node, mouseX, mouseY, button) -> this.slide.sequence(300F, this.slide.getValue() < 0.5F ? 1F : 0F, TweenEquations.QUART_OUT).start())
.attach(this);
```

![Clicking a gray button slides a light gray panel in from the left edge, a second click slides it out](../images/ess-anim-slide.gif "Each click starts the animator toward the other side.")

`setCallback(tween -> ...)`, after `sequence(...)`, runs code when the animation ends. `setSpeed(2F)` plays it twice as fast.

## Easing with TweenEquations

An easing equation shapes the motion. Each family has `IN` (starts slowly), `OUT` (ends softly) and `INOUT` (both) variants: `QUAD`, `CUBIC`, `QUART`, `QUINT`, `SINE`, `EXPO`, `CIRC`, `BACK`, `ELASTIC`, `BOUNCE`, plus `LINEAR`.

![Eight dots crossing a track, each eased with a different equation](../images/ess-anim-easing.gif "The same 1.2-second move with eight equations.")

| Equation | Feel |
|---|---|
| `LINEAR` | Constant speed. |
| `QUAD_OUT`, `CUBIC_OUT`, `QUART_OUT` | Decelerating, from gentle to strong: the usual choice for things that appear or react. |
| `SINE_INOUT` | Soft at both ends, for loops. |
| `BACK_OUT` | Overshoots the target, then settles. |
| `ELASTIC_OUT`, `BOUNCE_OUT` | Springs or bounces around the target. |

## Reference

| Method | Description |
|---|---|
| `TweenAnimator.create()`, `create(float)` | New animator at `0F` or the given value. |
| `sequence(duration, value[, equation])`, `parallel(...)` | Replaces the animation with a first step; `LINEAR` by default. |
| `push(duration, value[, equation])` | Adds a step. |
| `start()` | Starts the animation. |
| `getValue()`, `setValue(float)` | Current value. |
| `setCallback(tween -> ...)` | Runs at the end of each play. |
| `setSpeed(float)` | Time multiplier, `1F` by default. |
| `getTimeline()` | The running `Timeline` (`repeat`, `repeatYoyo`, `delay`); `null` once ended. |
| `animate(animator)`, `removeAnimator(animator)` | The node advances the animator every frame, or stops. |
| `onAnimate((node, animator, value) -> ...)` | Runs when an animator of the node changed. |
| `hoverDuration(long)`, `hoverEquation(TweenEquation)`, `hoverValue(float)` | Hover animation. |

## Good to know

- An animator does nothing until a node advances it: pass it to `animate(...)` on a node of an open UI.
- `start()`, `setCallback(...)` and `getTimeline()` need steps first: call `sequence(...)` or `parallel(...)` before them.
- Read an animator in a lambda (`() -> fade.getValue()`), not in an expression: the value changes every frame without a signal.

## See also

- Next: [Frame Loop and Dev Tools](frame-loop.md)
- [Tween Engine](../animation/tween-engine.md): tween several attributes of any object, timelines.
- [UIs](uis.md): transitions when a UI opens and closes.
- [Effects](../styling/effects.md): transforms and other effects to animate.
- [Input](input.md): the hover state.