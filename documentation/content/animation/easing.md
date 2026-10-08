# Easing

An easing equation shapes the progress of an animation: it maps the elapsed fraction of the duration to the fraction of the distance covered, so a value can start slowly, stop softly, overshoot or bounce. Every equation is a `TweenEquation`, and the built-in ones are constants of `TweenEquations` (`dev.joid.lib.animation.tweenengine`). [Animation](../essentials/animation.md) listed the usual ones; this page shows every curve, how to tune and write equations, and how they shape a [TweenAnimator](tween-animator.md).

## Using an equation

```java
final TweenAnimator open = TweenAnimator.create(0F).sequence(400F, 1F, TweenEquations.CUBIC_OUT).start();
```

![Nine dots crossing a track, each eased with a different OUT equation](../images/easing-race.gif "The same 1.2-second move with LINEAR and the OUT variants: BACK_OUT and ELASTIC_OUT overshoot, BOUNCE_OUT bounces on the target.")

Pass the equation wherever an animation accepts one:

```java
RectNode.create(0, 0, 200, 60).color(Color.WHITE).hoveredColor(Color.GRAY).hoverEquation(TweenEquations.SINE_INOUT).attach(this);
```

| Where | Method | Default |
| --- | --- | --- |
| [`TweenAnimator`](tween-animator.md) steps | `sequence(...)`, `parallel(...)`, `push(...)` with a `TweenEquation` argument | `TweenEquations.LINEAR` |
| Node hover animation | `Node.hoverEquation(TweenEquation)` | `TweenEquations.LINEAR` |
| A tween of the [Tween Engine](tween-engine.md) (next page) | `ease(TweenEquation)` | `TweenEquations.QUAD_INOUT` for `Tween.to`, `Tween.from` and `Tween.set` |

## IN, OUT and INOUT

Each family has three variants. With `f` the `IN` variant and `t` the elapsed fraction from `0` to `1`:

| Variant | Shape | Definition |
| --- | --- | --- |
| `IN` | Starts slowly, accelerates, arrives at full speed. | `f(t)` |
| `OUT` | Starts at full speed, decelerates, arrives softly. Use it for elements that appear or react to the user. | `1 - f(1 - t)` |
| `INOUT` | Accelerates during the first half, decelerates during the second. | `f(2t) / 2` for `t < 0.5`, then `1 - f(2 - 2t) / 2` |

`Back.INOUT` and `Elastic.INOUT` follow this shape with adjusted parameters, see [Tuning Back and Elastic with s, a and p](#tuning-back-and-elastic-with-s-a-and-p).

## Equation families

![Plots of the IN, OUT and INOUT variants of the eleven equation families](../images/easing-curves.png "Each plot shows the eased progress (vertical) against the elapsed fraction (horizontal); Back and Elastic leave the box.")

| Family | `IN` curve `f(t)` | Character |
| --- | --- | --- |
| `Linear` | `t` | Constant speed. Only `Linear.INOUT` exists (`TweenEquations.LINEAR`). |
| `Quad` | `t²` | Gentle acceleration. |
| `Cubic` | `t³` | Marked acceleration. |
| `Quart` | `t⁴` | Strong acceleration. |
| `Quint` | `t⁵` | Very strong acceleration. |
| `Sine` | `1 - cos(t · π / 2)` | Soft acceleration, close to `Quad`. |
| `Expo` | `2^(10 · (t - 1))`, exactly `0` at `t = 0` | Almost still, then very fast. |
| `Circ` | `1 - √(1 - t²)` | Quarter circle: slow, then abrupt at the end. |
| `Back` | `t² · ((s + 1) · t - s)`, `s = 1.70158` | Moves backward first (down to about `-0.1`), then forward. `OUT` overshoots the target by about 10 % before settling. |
| `Elastic` | `-a · 2^(10 · (t - 1)) · sin((t - 1 - σ) · 2π / p)`, `a = 1`, `p = 0.3`, `σ = p / 4` | Spring oscillation. `OUT` overshoots and oscillates around the target before settling. |
| `Bounce` | `1 - OUT(1 - t)`, where `OUT` is made of four parabolic arcs | `OUT` reaches the target, then bounces back three times with decreasing height, like a dropped ball. |

All equations return `0` at `t = 0` and `1` at `t = 1`. `Back` and `Elastic` leave the `0`–`1` range in between, so the animated value goes past its start or its target.

## Tuning Back and Elastic with s, a and p

`Back` and `Elastic` have parameters. Their methods return a **new** equation with the parameter changed; the constants themselves never change:

```java
final TweenAnimator pop = TweenAnimator.create(0F).sequence(500F, 1F, TweenEquations.BACK_OUT.s(3F)).start();

final Elastic loose = Elastic.OUT.a(2F).p(0.5F);
```

| Method | Parameter | Default |
| --- | --- | --- |
| `Back.s(float s)` | Overshoot amount. `0F` turns `Back` into the matching `Cubic` curve; larger values overshoot further. `Back.INOUT` multiplies it by `1.525` so that each half overshoots about as much as `IN` / `OUT`. | `1.70158F` |
| `Elastic.a(float a)` | Amplitude of the oscillation. Values below `1F` count as `1F`. | `0F` (counts as `1F`) |
| `Elastic.p(float p)` | Period of the oscillation: smaller values oscillate faster. | `0.3F` for `IN` and `OUT`, `0.45F` for `INOUT` |

A tuned equation keeps the name of its variant (`toString()` returns `"Back.OUT"`), so [`TweenUtils.parseEasing`](#parsing-an-equation-by-name-with-tweenutils) returns the default constant for that name.

## Writing a custom equation

Extend `TweenEquation` and implement `compute(float t)`. `t` goes from `0` (start) to `1` (end of the duration); return `0` at the start, `1` at the end and any value in between (values outside `0`–`1` overshoot).

```java
public class StepsEquation extends TweenEquation {

	private final int steps;

	public StepsEquation(final int steps) {
		this.steps = steps;
	}

	@Override
	public float compute(final float t) {
		return t >= 1F ? 1F : (float) Math.floor(t * this.steps) / this.steps;
	}

	@Override
	public String toString() {
		return "Steps." + this.steps;
	}

}
```

```java
final TweenAnimator ticks = TweenAnimator.create(0F).sequence(2000F, 1F, new StepsEquation(4)).start();
```

Override `toString()` to name the equation: `isValueOf(String)` compares a string with it.

## How the eased progress is used

At each update, an animation step (a tween, see [Tween Engine](tween-engine.md)) computes `p = equation.compute(elapsed / duration)` and sets each value to `start + p · (target - start)`. With [waypoints](tween-engine.md#paths-and-waypoints), `p` is the position along the path instead.

During the backward plays of a yoyo ([`repeatYoyo`](tween-animator.md#repeating-with-repeat-and-repeatyoyo)), the elapsed time runs backward: the value retraces the same curve in reverse. A `QUAD_OUT` forward play that arrives softly leaves softly on the way back.

## Parsing an equation by name with TweenUtils

`TweenUtils.parseEasing(String name)` returns the built-in equation whose `toString()` equals `name`, or `null` when none matches. Names are case-sensitive and use the `Family.VARIANT` form: `"Quad.OUT"`, `"Linear.INOUT"`, `"Elastic.INOUT"`. Use it to read equations from configuration files; custom equations are not known to it.

```java
final TweenEquation equation = TweenUtils.parseEasing("Cubic.OUT");
```

## Reference

### TweenEquations constants

| Constant | Instance and name (`toString()`) |
| --- | --- |
| `LINEAR` | `Linear.INOUT` |
| `QUAD_IN`, `QUAD_OUT`, `QUAD_INOUT` | `Quad.IN`, `Quad.OUT`, `Quad.INOUT` |
| `CUBIC_IN`, `CUBIC_OUT`, `CUBIC_INOUT` | `Cubic.IN`, `Cubic.OUT`, `Cubic.INOUT` |
| `QUART_IN`, `QUART_OUT`, `QUART_INOUT` | `Quart.IN`, `Quart.OUT`, `Quart.INOUT` |
| `QUINT_IN`, `QUINT_OUT`, `QUINT_INOUT` | `Quint.IN`, `Quint.OUT`, `Quint.INOUT` |
| `SINE_IN`, `SINE_OUT`, `SINE_INOUT` | `Sine.IN`, `Sine.OUT`, `Sine.INOUT` |
| `EXPO_IN`, `EXPO_OUT`, `EXPO_INOUT` | `Expo.IN`, `Expo.OUT`, `Expo.INOUT` |
| `CIRC_IN`, `CIRC_OUT`, `CIRC_INOUT` | `Circ.IN`, `Circ.OUT`, `Circ.INOUT` |
| `BACK_IN`, `BACK_OUT`, `BACK_INOUT` | `Back.IN`, `Back.OUT`, `Back.INOUT` |
| `ELASTIC_IN`, `ELASTIC_OUT`, `ELASTIC_INOUT` | `Elastic.IN`, `Elastic.OUT`, `Elastic.INOUT` |
| `BOUNCE_IN`, `BOUNCE_OUT`, `BOUNCE_INOUT` | `Bounce.IN`, `Bounce.OUT`, `Bounce.INOUT` |

The constants are the static fields of the family classes in `dev.joid.lib.animation.tweenengine.equation` (`Back`, `Bounce`, `Circ`, `Cubic`, `Elastic`, `Expo`, `Linear`, `Quad`, `Quart`, `Quint`, `Sine`): `TweenEquations.BACK_OUT == Back.OUT`.

### Methods

| Method | Description |
| --- | --- |
| `abstract float compute(float t)` | Eased progress for the elapsed fraction `t` (`0` to `1`). |
| `boolean isValueOf(String str)` | `true` when `str` equals `toString()`. |
| `Back s(float s)` | A new `Back` of the same variant with the overshoot `s`. |
| `Elastic a(float a)`, `Elastic p(float p)` | A new `Elastic` of the same variant with the amplitude `a` or the period `p`. |
| `static TweenEquation TweenUtils.parseEasing(String easingName)` | Built-in equation named `easingName` (`"Quad.IN"`...), or `null`. |

## Pitfalls

- `TweenEquations.BACK_OUT.s(3F);` alone does nothing: use the returned equation.
- Names are case-sensitive: `parseEasing("quad.out")` returns `null`.
- `Back` and `Elastic` overshoot: a value used as an alpha or a ratio can leave `0`–`1`; clamp it where that matters.

## See also

- Next: [Tween Engine](tween-engine.md)
- [TweenAnimator](tween-animator.md)
- [Animation](../essentials/animation.md)
- [Hover and Tooltips](../interactions/hover.md)
- [Transitions](../ui/transitions.md)