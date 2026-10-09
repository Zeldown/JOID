# Animation

JOID animates at three levels: every node has a built-in hover animation, a `TweenAnimator` animates any value you choose, and transitions animate a whole UI when it opens and closes. They all run on the frame clock, so they look the same at any frame rate.

## The hover animation

Every node animates a hover value from `0` to `1` when the mouse enters it, and back when it leaves. Hover colors follow it, so this button already fades between its two colors:

```java
RectNode
.create(100, 100, 300, 80)
.color(Color.DARKGRAY)
.hoveredColor(Color.LIGHTGRAY)
.hoverDuration(300L)
.hoverEquation(TweenEquations.QUAD_OUT)
.attach(this);
```

![The cursor enters a dark gray button, which fades to light gray, then leaves](../images/ess-anim-hover.gif "A 300 ms QUAD_OUT fade between the two colors.")

- `hoverDuration(...)` sets the length of the animation in milliseconds (default `200L`).
- `hoverEquation(...)` sets its easing (default `TweenEquations.LINEAR`). `TweenEquations` is in `dev.joid.lib.animation.tween`.

## Driving anything with hoverValue

`hoverValue(max)` returns the hover progress scaled to `max`: from `0` (mouse away) to `max` (mouse over). Read it in a lambda and drive anything with it. This card lifts by 12 units and gets rounder under the mouse, without moving its layout:

```java
RectNode
.create(100, 200, 300, 200)
.color(Color.WHITE)
.hoverDuration(250L)
.hoverEquation(TweenEquations.CUBIC_OUT)
.self(node -> node.effect(RoundedNodeEffect.create(() -> 8F + node.hoverValue(8F))))
.self(node -> node.effect(TransformNodeEffect.create(new TranslateOperation(Vector.Y(() -> (double) -node.hoverValue(12F))))))
.attach(this);
```

![The cursor hovers a white card that lifts slightly and rounds its corners](../images/ess-anim-lift.gif "The card lifts by 12 units and its radius grows from 8 to 16 while hovered.")

`TranslateOperation` is in `dev.joid.lib.render.transform.operation` and `Vector` in `dev.joid.lib.render.transform`.

## Animating a value with TweenAnimator

`TweenAnimator` (`dev.joid.lib.animation.animator`) animates one `float`: you describe the steps, start it, and read the value while it moves.

```java
final TweenAnimator fade = TweenAnimator.create(0F).sequence(500F, 1F, TweenEquations.CUBIC_OUT).start();
RectNode
.create(760, 440, 400, 200)
.color(() -> Color.WHITE.copyAlpha(fade.getValue()))
.animate(fade)
.attach(this);
```

![A white rectangle fading in](../images/ess-anim-fade.gif "The lambda reads the animator value, from 0 to 1 in 500 ms.")

![An animator is created, given its steps with sequence, started, advanced every frame by the node it is passed to, and read with getValue](../images/ess-diagram-animator.png "How a TweenAnimator drives a node.")

1. `create(0F)` starts the value at `0F`.
2. `sequence(500F, 1F, ...)` describes one step: reach `1F` in 500 ms with the given easing.
3. `start()` starts the animation.
4. `animate(fade)` lets the node advance the animator every frame, even while it is hidden; the color lambda reads its value. A lambda is right here: the value changes on every frame.

`push(...)` adds more steps after the first one:

```java
final TweenAnimator wobble = TweenAnimator
.create(0F)
.sequence(150F, 10F, TweenEquations.CUBIC_OUT)
.push(150F, -10F, TweenEquations.CUBIC_INOUT)
.push(150F, 0F, TweenEquations.CUBIC_IN)
.start();
RectNode.create(100, 100, 100, 100).color(Color.GRAY).x(() -> 100D + wobble.getValue()).animate(wobble).attach(this);
```

## Moving nodes with onAnimate

`onAnimate((node, animator, value) -> ...)` runs on each frame where the value of an animator of the node changed. Animate a progress from `0F` to `1F` and map it to what you need:

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

![A light gray square sliding diagonally across the whole canvas and back](../images/ess-anim-move.gif "One second from corner to corner, back and forth forever (whole canvas at 0.25× scale).")

`repeatYoyo(Tween.INFINITY, 0F)` plays the animation back and forth forever; `repeat(count, delay)` replays it from the start. Set the repeats on `getTimeline()` right after `sequence(...)`, before `start()`. `Tween` is in `dev.joid.lib.animation.tween`.

## Animations started by the user

To play an animation in response to a click, describe a new step and start it: `sequence(...)` replaces the running animation, which continues from the current value. A side panel that opens and closes:

```java
private final TweenAnimator slide = TweenAnimator.create(0F);
```

```java
RectNode
.create(-300, 0, 300, 1080)
.color(Color.LIGHTGRAY)
.onAnimate((node, animator, value) -> node.x(-300 + 300 * value))
.animate(this.slide)
.attach(this);

RectNode
.create(1700, 20, 200, 60)
.color(Color.GRAY)
.onClick((node, mouseX, mouseY, clickType) -> this.slide.sequence(300F, this.slide.getValue() < 0.5F ? 1F : 0F, TweenEquations.QUART_OUT).start())
.attach(this);
```

![Clicking a gray button slides a light gray panel in from the left edge, a second click slides it out](../images/ess-anim-slide.gif "Each click starts the animator toward the other side (whole canvas at 0.3× scale).")

`setCallback(tween -> ...)`, called after `sequence(...)`, runs code when the animation ends.

## Easing

An easing equation shapes the motion. Each family has an `IN` variant (starts slowly), an `OUT` variant (ends softly) and an `INOUT` variant (both):

![Eight dots crossing a track, each eased with a different equation](../images/ess-anim-easing.gif "The same 1.2-second move with eight equations.")

| Equation | Feel |
| --- | --- |
| `LINEAR` | Constant speed. |
| `QUAD_OUT`, `CUBIC_OUT`, `QUART_OUT` | Decelerating, from gentle to strong: the usual choice for things that appear or react to the user. |
| `SINE_INOUT` | Soft at both ends, for loops. |
| `BACK_OUT` | Overshoots the target slightly, then settles. |
| `ELASTIC_OUT`, `BOUNCE_OUT` | Springs or bounces around the target. |

## Transitions between UIs

A transition animates a whole UI when it opens and when it closes. `PopTransition` (`dev.joid.lib.ui.core.transition.impl`) scales the UI from 75 % to 100 % in 130 ms, and back when it closes:

```java
final SettingsUI settings = new SettingsUI();
settings.setTransition(new PopTransition());
JOID.open(settings);
```

![A settings card pops in from a smaller size, then shrinks away when closed](../images/ess-anim-pop.gif "PopTransition when the UI opens, then closes with Escape; the background dims with it.")

Popups (`@UIDataPopup(active = true)`) get a pop transition by default. The UI is removed only once its closing animation has ended. To write your own (a slide, a fade), extend `Transition`, as shown in [Transitions](../ui/transitions.md).

## Pitfalls

- An animator does nothing until a node advances it: pass it to `animate(...)` on a node of an open UI.
- `start()` and `setCallback(...)` need steps first: call `sequence(...)` or `parallel(...)` before them, or they throw an `IllegalStateException`.
- `getTimeline()` returns `null` once the animation has ended: test the end with `getTimeline() == null`.

## See also

- Next: [Tutorial 1: Project Setup](../tutorial/setup.md): the Essentials put together in a real settings screen.
- [Component Catalog](../components/overview.md): every node you can use.
- [TweenAnimator](../animation/tween-animator.md): every method, timelines, callbacks, testing with a manual clock.
- [Easing](../animation/easing.md): every equation and its curve.
- [Hover and Tooltips](../interactions/hover.md): the hover animation in detail.
- [Transitions](../ui/transitions.md): how transitions run and how to write one.