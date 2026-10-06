# TweenAnimator

`TweenAnimator` (`dev.joid.lib.animation.animator`) animates a single `float` value over time: you describe the steps (duration, target value, easing), start it, and read the value while it moves. It is the simplest way to animate a node, and it is what JOID itself uses for hover fades and UI transitions. It is built on the [Tween Engine](tween-engine.md), which you only need for objects with several attributes or for advanced timelines.

## Animating a node

A `TweenAnimator` attached to a node with `animate(...)` is advanced by the node every frame, and `onAnimate(...)` receives each new value:

```java
@Override
public void init() {
    final TweenAnimator move = TweenAnimator.create(0F).sequence(1000F, 1F);
    move.getTimeline().repeatYoyo(Tween.INFINITY, 0F);
    move.start();

    RectNode
    .create(0, 0, 100, 100)
    .color(Color.RED)
    .onAnimate((node, animator, value) -> node.position((1920 - 100) * value, (1080 - 100) * value))
    .animate(move)
    .attach(this);
}
```

![A red square sliding diagonally across the whole canvas and back](../images/animator-move.gif "The square goes from the top-left to the bottom-right corner in one second and back, forever (whole canvas at 0.25× scale).")

1. `TweenAnimator.create(0F)` creates an animator whose value is `0F`.
2. `sequence(1000F, 1F)` describes one step: go to `1F` in 1000 ms, with the default `LINEAR` easing.
3. `getTimeline().repeatYoyo(Tween.INFINITY, 0F)` makes the animation go back and forth forever (see [Repeating with repeat and repeatYoyo](#repeating-with-repeat-and-repeatyoyo)).
4. `start()` starts the animation.
5. `animate(move)` lets the node update the animator each frame; `onAnimate(...)` moves the square from the top-left corner to the bottom-right corner as the value goes from `0F` to `1F`.

The value does not need to be a ratio: `sequence(500F, 360F)` animates from the current value to `360F`. Animating a `0F` → `1F` progress and mapping it in the callback keeps the animation reusable.

## Durations, values and the clock

- Durations are `float` milliseconds.
- `update()` advances the animation by the time elapsed since the previous `update()` (or since `start()`), read from the clock bridge `BridgeHandler.CLOCK` (`dev.joid.lib.bridge`), multiplied by `getSpeed()`.
- `getValue()` returns the current value at any time. `setValue(float)` jumps to a value; a running animation overwrites it at its next update.

The default clock is the system clock. In tests, register a `ManualClockBridge` to control time (see [Testing with a manual clock](#testing-with-a-manual-clock)).

## Building the animation with sequence, parallel and push

`sequence(...)` and `parallel(...)` replace the animator's timeline with a new one containing a first step; `push(...)` appends a step to that timeline.

```java
final TweenAnimator wobble = TweenAnimator
.create(0F)
.sequence(150F, 10F, TweenEquations.CUBIC_OUT)
.push(150F, -10F, TweenEquations.CUBIC_INOUT)
.push(150F, 5F, TweenEquations.CUBIC_INOUT)
.push(150F, 0F, TweenEquations.CUBIC_IN)
.start();
```

| Method | Timeline it builds |
| --- | --- |
| `sequence(float duration, float value)`, `sequence(float duration, float value, TweenEquation equation)` | A sequence: each step starts when the previous one ends, from the value the previous step reached. |
| `parallel(float duration, float value)`, `parallel(float duration, float value, TweenEquation equation)` | A parallel timeline: every step starts at the same time from the same value. They all write the same value, so while several steps run, the step pushed last wins. With a single step it behaves like `sequence(...)`. |
| `push(float duration, float value)`, `push(float duration, float value, TweenEquation equation)` | Adds a step to the timeline built by the last `sequence(...)` or `parallel(...)`. |

- Without an equation, a step uses `TweenEquations.LINEAR`. See [Easing](easing.md) for every equation.
- A step starts from the value the animator has when the step begins, not when it was declared.
- Call `sequence(...)` or `parallel(...)` before `push(...)` and before `setCallback(...)`: the animator has no timeline until then.

## Starting and replacing an animation

`start()` records the current clock time and starts the timeline in the animator's own `TweenManager`. Configure the timeline (repeats, delays, callbacks) before calling it.

To play a new animation, call `sequence(...)` or `parallel(...)` again, then `start()`. Here a button opens and closes a side panel:

```java
final TweenAnimator slide = TweenAnimator.create(0F);

RectNode
.create(-300, 0, 300, 1080)
.color(Color.BLACK)
.onAnimate((node, animator, value) -> node.x(-300 + 300 * value))
.animate(slide)
.attach(this);

RectNode
.create(1700, 20, 200, 60)
.color(Color.BLUE)
.onClick((node, mouseX, mouseY, clickType) -> {
    slide.getManager().killAll();
    slide.sequence(300F, slide.getValue() < 0.5F ? 1F : 0F, TweenEquations.QUART_OUT).start();
})
.attach(this);
```

![Clicking a blue button slides a black panel in from the left edge, a second click slides it out](../images/animator-slide.gif "Each click restarts the animator toward the other side, eased with QUART_OUT (whole canvas at 0.3× scale).")

- Starting a new timeline does not stop the previous one: both stay in the manager and the one started last writes the value last each frame. If the previous timeline runs longer, it takes over the value once the new one ends. `getManager().killAll()` stops every running timeline first; the value stays where it is.
- Never call `start()` twice on the same timeline once it has finished. A finished timeline is removed from the manager at the next update and returned to a pool, where another animation can reuse it. Read `getTimeline()` only while the animation runs.
- `clear()` resets the animator completely: value `0F`, speed `1F`, no timeline and a new, empty manager.

## Repeating with repeat and repeatYoyo

`getTimeline()` returns the timeline built by `sequence(...)` / `parallel(...)`. Set its repetitions before `start()`:

```java
final TweenAnimator pulse = TweenAnimator.create(0F).sequence(600F, 1F, TweenEquations.SINE_INOUT);
pulse.getTimeline().repeatYoyo(Tween.INFINITY, 200F);
pulse.start();
```

| Call on the timeline | Effect |
| --- | --- |
| `repeat(int count, float delay)` | Plays the whole timeline `count` more times, waiting `delay` ms between plays. |
| `repeatYoyo(int count, float delay)` | Same, but every second play runs backward. |
| `delay(float delay)` | Waits `delay` ms before the first play (adds up when called several times). |

`Tween.INFINITY` (`-1`) repeats forever. Calling `repeat(...)` or `repeatYoyo(...)` after `start()` throws a `RuntimeException`. See [Tween Engine](tween-engine.md#delays-repeat-and-yoyo) for the details.

## Reacting to the end with setCallback

`setCallback(Consumer<BaseTween<?>>)` runs when the timeline reaches its end. It receives the timeline.

```java
final TweenAnimator fade = TweenAnimator
.create(1F)
.sequence(400F, 0F, TweenEquations.QUAD_OUT)
.setCallback(timeline -> System.out.println("Faded out"))
.start();
```

- The callback is registered on the current timeline (as a `TweenCallback.END` callback): call it after `sequence(...)` / `parallel(...)`. A later `sequence(...)` builds a new timeline without it.
- With `repeat(...)` or `repeatYoyo(...)`, it runs at the end of every play.
- Several calls add several callbacks, run in the order they were added.
- For other events (start, completion of the last repetition, backward play), add a callback to the timeline itself with `getTimeline().addCallback(...)`, see [Callbacks](tween-engine.md#callbacks).

## Driving a node with animate and onAnimate

`Node.animate(TweenAnimator)` registers an animator on a node. While the node is visible, it calls `update()` on every registered animator each frame, before drawing itself.

`Node.onAnimate(NodeAnimationCallback<T>)` registers a callback `(node, animator, value) -> ...`:

- It runs once per frame and per registered animator whose value changed since the previous frame. The first comparison is made with the value the animator had when `animate(...)` was called, so a value that does not move never triggers it.
- `animator` tells which animator changed when several are registered on the node.
- It is a regular node callback, with `PRE`/`POST` phases, see [Callbacks](../interactions/callbacks.md).

You can also read the value directly where a node accepts a supplier, and only register the animator so that it is updated:

```java
final TweenAnimator fade = TweenAnimator.create(0F).sequence(500F, 1F, TweenEquations.CUBIC_OUT).start();

RectNode
.create(760, 440, 400, 200)
.color(() -> Color.WHITE.copyAlpha(fade.getValue()))
.animate(fade)
.attach(this);
```

![A white rectangle fading in](../images/animator-fade.gif "The supplier reads the animator value, from 0 to 1 in 500 ms.")

Behaviors to know:

- Registering the same animator on several nodes is safe: `update()` advances by the time elapsed since its previous update, so extra updates in the same frame add nothing.
- The node stops updating its animators while it is not visible, but the clock keeps running: when the node shows again, the animation jumps to where it would have been.
- There is no method to unregister an animator. To stop the animation, kill its timeline with `getManager().killAll()`; the node keeps calling `update()`, which then does nothing. `getAnimatorMap()` returns the live map of registered animators (animator to last reported value); do not modify it from an `onAnimate` callback, which runs while the node iterates over it.

## Updating an animator yourself

An animator that is not registered on a node only moves when you call `update()`. Call it once per frame, for example from `UI.preDraw(...)` or from the `draw(...)` of a custom node:

```java
public class UIBanner extends UI {

    private final TweenAnimator slide = TweenAnimator.create(0F);

    @Override
    public void init() {
        this.slide.sequence(800F, 1F, TweenEquations.EXPO_OUT).start();
        RectNode.create(0, 0, 1920, 120).color(() -> Color.BLACK.copyAlpha(this.slide.getValue())).attach(this);
    }

    @Override
    public void preDraw(final double mouseX, final double mouseY) {
        this.slide.update();
    }

}
```

| Method | Effect |
| --- | --- |
| `update()` | Advances by the clock time elapsed since the last `update()` or `start()`, times the speed. |
| `update(float delta)` | Advances by `delta` ms times the speed. It does not change `getLastUpdate()`, so a later `update()` still counts the whole time since the previous `update()`. Use one form or the other. |
| `setSpeed(float speed)` | Time multiplier, default `1F`. `2F` plays twice as fast, `0.5F` half as fast, `0F` freezes the animation. |

## Hover and transitions

JOID uses `TweenAnimator` in two places you can configure:

- Every node owns a hover animator (`getHoverAnimator()`), which goes to `1F` when the mouse enters the node and back to `0F` when it leaves. `hoverDuration(long)` sets its duration in ms (default `200`), `hoverEquation(TweenEquation)` its easing (default `TweenEquations.LINEAR`), and `hoverValue(float value)` returns `value` multiplied by the current hover progress. See [Hover and Tooltips](../interactions/hover.md).
- Every `Transition.TransitionState` owns an animator (`getAnimator()`), starting at `0F` for `Transition.In` and `1F` for `Transition.Out`. A transition builds its timeline with the animator, then passes it to `start(Timeline)`:

```java
@Override
public void start() {
    final Timeline timeline = super.getAnimator().sequence(1000F, 1F, TweenEquations.QUART_OUT).getTimeline();
    this.start(timeline);
}
```

See [Transitions](../ui/transitions.md).

## Testing with a manual clock

`update()` reads `BridgeHandler.CLOCK`. Register a `ManualClockBridge` (`dev.joid.lib.bridge.clock`) to step animations deterministically, and register a `SystemClockBridge` again afterwards:

```java
final ManualClockBridge clock = ManualClockBridge.create(1000L);
BridgeHandler.CLOCK.register(clock);

final TweenAnimator animator = TweenAnimator.create().sequence(100F, 10F).start();
clock.advance(25L);
animator.update();
System.out.println(animator.getValue());

BridgeHandler.CLOCK.register(new SystemClockBridge());
```

This prints `2.5`: a quarter of the way to `10F` with `LINEAR` easing.

## TweenAnimator reference

| Method | Description |
| --- | --- |
| `static create()` | New animator with the value `0F`. |
| `static create(float value)` | New animator with the given value. |
| `sequence(float duration, float value)`, `sequence(float duration, float value, TweenEquation equation)` | Replaces the timeline with a sequence whose first step goes to `value` in `duration` ms. Default equation `LINEAR`. Returns the animator. |
| `parallel(float duration, float value)`, `parallel(float duration, float value, TweenEquation equation)` | Replaces the timeline with a parallel timeline whose first step goes to `value` in `duration` ms. Default equation `LINEAR`. Returns the animator. |
| `push(float duration, float value)`, `push(float duration, float value, TweenEquation equation)` | Appends a step to the current timeline. Default equation `LINEAR`. Returns the animator. |
| `start()` | Starts the current timeline in the animator's manager and records the clock time. Returns the animator. |
| `setCallback(Consumer<BaseTween<?>> callback)` | Adds a callback run at the end of each play of the current timeline. Returns the animator. |
| `update()` | Advances by the clock time elapsed since the last `update()` / `start()`, times the speed. Returns the animator. |
| `update(float delta)` | Advances by `delta` ms, times the speed. Returns the animator. |
| `clear()` | Resets the value to `0F`, the speed to `1F`, removes the timeline and replaces the manager. |
| `getValue()` / `setValue(float)` | Current value. |
| `getSpeed()` / `setSpeed(float)` | Time multiplier, default `1F`. |
| `getTimeline()` / `setTimeline(Timeline)` | Timeline started by `start()`. `null` until `sequence(...)` / `parallel(...)`. |
| `getManager()` / `setManager(TweenManager)` | The animator's own `TweenManager`. |
| `getLastUpdate()` / `setLastUpdate(long)` | Clock time, in ms, of the last `update()` or `start()`. `0` before. |

The setters generated for the properties (`setValue`, `setSpeed`, ...) return `void`; the other methods return the animator for chaining.

Related node methods (on `Node`):

| Method | Description |
| --- | --- |
| `animate(TweenAnimator animator)` | Updates the animator every frame while the node is visible. |
| `onAnimate(NodeAnimationCallback<T> callback)` | Called with `(node, animator, value)` when a registered animator's value changed since the previous frame. |
| `getAnimatorMap()` | Registered animators, mapped to the last value reported to `onAnimate`. |
| `hoverDuration(long)`, `hoverEquation(TweenEquation)`, `hoverValue(float)`, `getHoverAnimator()` | Hover animation, see [Hover and Tooltips](../interactions/hover.md). |

## Tweening an animator with TweenAnimatorAccessor

`TweenAnimatorAccessor` is the [`TweenAccessor`](tween-engine.md#tween-accessors) of `TweenAnimator`. Its only tween type, `TweenAnimatorAccessor.ANIMATION_VALUE` (`0`), reads and writes `getValue()` / `setValue(...)`. It is registered automatically, so an animator can be the target of a raw tween or timeline:

```java
final TweenManager manager = new TweenManager();
final TweenAnimator progress = TweenAnimator.create(0F);

Timeline
.createSequence()
.push(Tween.to(progress, TweenAnimatorAccessor.ANIMATION_VALUE, 300F).target(0.8F))
.pushPause(500F)
.push(Tween.to(progress, TweenAnimatorAccessor.ANIMATION_VALUE, 200F).target(1F))
.start(manager);
```

Such tweens run in `manager`, not in the animator's own manager: update `manager` yourself each frame (see [Running tweens with TweenManager](tween-engine.md#running-tweens-with-tweenmanager)).

## See also

- [Easing](easing.md)
- [Tween Engine](tween-engine.md)
- [Hover and Tooltips](../interactions/hover.md)
- [Transitions](../ui/transitions.md)
- [Callbacks](../interactions/callbacks.md)