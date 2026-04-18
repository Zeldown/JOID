# TweenAnimator

High-level wrapper around the Universal Tween Engine. Animates a single `float` value over time with any easing curve — single tweens, sequences, parallel groups.

## Create

```java
TweenAnimator animator = TweenAnimator.create();        // starts at 0F
TweenAnimator animator = TweenAnimator.create(0F);      // explicit initial value
```

## Start a tween

```java
animator.sequence(400F, 100F).start();
```

Tween from the current value to `100F` over `400ms` with default `LINEAR` easing. **Durations are `float` milliseconds**, not `long`.

## Easing

```java
animator.sequence(400F, 100F, TweenEquations.CUBIC_OUT).start();
```

See `Easing` for the full catalog.

## Chaining

`sequence(...)` and `parallel(...)` **build a fresh timeline** — each call replaces any existing timeline on the animator. To extend the current timeline, use `push(...)`:

```java
animator
    .sequence(400F, 100F, TweenEquations.CUBIC_OUT)
    .push(200F, 50F, TweenEquations.CUBIC_IN)
    .push(300F, 100F, TweenEquations.BACK_OUT)
    .start();
```

`sequence(...)` creates a sequential timeline (each tween waits for the previous). `parallel(...)` creates a parallel timeline (all tweens run at once). `push(...)` appends to whichever timeline was just created.

## Completion callback

```java
animator
    .sequence(400F, 100F)
    .setCallback(tween -> System.out.println("done"))
    .start();
```

`setCallback(Consumer<BaseTween<?>>)` registers a `TweenCallback.END` callback — fires once when the timeline finishes. There is no per-frame callback; read `animator.getValue()` from your draw loop instead.

## Read the value

```java
final float current = animator.getValue();
```

Use it in a `draw()` override, a watch callback, or bind it via a supplier lambda where the API accepts one.

## Update cadence

The animator needs `update()` calls every frame to advance:

```java
animator.update();            // uses wall-clock delta since last call
animator.update(deltaMs);     // explicit delta (× speed)
```

`TweenAnimator.setSpeed(float)` scales the effective delta — `1F` is normal, `2F` is twice as fast, `0F` pauses.

## Example — fade in

```java
final TweenAnimator fade = TweenAnimator.create(0F);
fade.sequence(500F, 1F, TweenEquations.CUBIC_OUT).start();

RectNode.create(0, 0, 200, 100)
    .color(() -> Color.WHITE.copyAlpha(fade.getValue()))
    .attach(parent);
```

## Example — wobble chain

```java
final TweenAnimator wobble = TweenAnimator.create(0F);
wobble
    .sequence(150F, 10F, TweenEquations.CUBIC_OUT)
    .push(150F, -10F, TweenEquations.CUBIC_IN_OUT)
    .push(150F, 5F, TweenEquations.CUBIC_IN_OUT)
    .push(150F, 0F, TweenEquations.CUBIC_IN)
    .start();

node.x(() -> baseX + wobble.getValue());
```

## Parallel tracks

Use multiple `TweenAnimator` instances for independent axes — each owns its own `TweenManager`:

```java
final TweenAnimator x = TweenAnimator.create(0F);
final TweenAnimator y = TweenAnimator.create(0F);

x.sequence(500F, 200F, TweenEquations.CUBIC_OUT).start();
y.sequence(300F, 100F, TweenEquations.BOUNCE_OUT).start();
```

## Integration with Node hover

Every `Node` has a built-in hover animator:

```java
node.hoverDuration(200L);
node.hoverEquation(TweenEquations.CUBIC_OUT);

float t = node.hoverValue(1F);   // 0.0 → 1.0 on hover fade
```

## See also

- `Easing` — easing equations catalog.
- `Transitions` — UI-level in/out animations.