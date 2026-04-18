# TweenAnimator

High-level wrapper around the Universal Tween Engine. Animate a `float` value over time with any easing curve — single tweens, sequences, parallel groups.

## Create

```java
final TweenAnimator animator = TweenAnimator.create(0F);   // initial value
```

Or create from a supplier if the initial value is dynamic:

```java
final TweenAnimator animator = TweenAnimator.create();   // defaults to 0F
```

## Start a tween

```java
animator.sequence(400L, 100F).start();
```

Animate from current value to `100F` over `400ms` with default `LINEAR` easing.

## Easing

```java
animator.sequence(400L, 100F, TweenEquations.CUBIC_OUT).start();
```

See [Easing](easing.md) for the full catalog.

## Chained sequences

Use multiple `sequence()` calls to build a timeline:

```java
animator
    .sequence(400L, 100F, TweenEquations.CUBIC_OUT)
    .sequence(200L, 50F, TweenEquations.CUBIC_IN)
    .sequence(300L, 100F, TweenEquations.BACK_OUT)
    .start();
```

Each sequence starts when the previous ends.

## Callbacks

```java
animator
    .sequence(400L, 100F)
    .callback(value -> System.out.println("Current: " + value))
    .onComplete(() -> System.out.println("Done"))
    .start();
```

`callback(Consumer<Float>)` — fires every frame with the current value. `onComplete(Runnable)` — fires once the full chain finishes.

## Read the value

```java
final float current = animator.getValue();
```

Use in `draw` or reactive bindings:

```java
node.x(() -> animator.getValue());
```

## Example — fade in a node

```java
final TweenAnimator fade = TweenAnimator.create(0F);
fade.sequence(500L, 1F, TweenEquations.CUBIC_OUT).start();

RectNode.create(0, 0, 200, 100)
    .color(() -> Color.WHITE.copyAlpha(fade.getValue()))
    .attach(parent);
```

## Example — wobble

```java
final TweenAnimator wobble = TweenAnimator.create(0F);
wobble
    .sequence(150L, 10F, TweenEquations.CUBIC_OUT)
    .sequence(150L, -10F, TweenEquations.CUBIC_IN_OUT)
    .sequence(150L, 5F, TweenEquations.CUBIC_IN_OUT)
    .sequence(150L, 0F, TweenEquations.CUBIC_IN)
    .start();

node.x(() -> baseX + wobble.getValue());
```

## Parallel tweens

Use multiple `TweenAnimator` instances for independent animation tracks:

```java
final TweenAnimator x = TweenAnimator.create(0F);
final TweenAnimator y = TweenAnimator.create(0F);

x.sequence(500L, 200F, TweenEquations.CUBIC_OUT).start();
y.sequence(300L, 100F, TweenEquations.BOUNCE_OUT).start();

node.position(() -> x.getValue(), () -> y.getValue());
```

## Integration with Node hover

Every `Node` has a built-in hover animator:

```java
// Automatic fade from 0 to 100 on hover
node.hoverDuration(200L);
node.hoverEquation(TweenEquations.CUBIC_OUT);

// Read the current hover progress
float t = node.hoverValue(1F);  // 0.0 → 1.0
```

For custom hover animations, combine `hoverValue()` with your own tween or just use it directly.

## Thread safety

The underlying `TweenManager` is thread-safe — animations can be started, stopped, and read from multiple threads. Still, prefer the render thread for UI animations.

## Best practices

- **Prefer `TweenAnimator` over manual `t += deltaTime`.** Fewer bugs, smoother easing.
- **Cache animators.** Creating one per frame defeats the purpose — store them as fields.
- **Don't animate everything.** Animations are cheap but not free; each active animator costs a per-frame interpolation.

## See also

- [Easing](easing.md) — easing equations catalog.
- [Transitions](../ui/transitions.md) — UI-level in/out animations.
