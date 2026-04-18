# Easing

The catalog of easing equations available through `TweenEquations`. Each equation takes a normalized time `t ∈ [0, 1]` and returns a value shaping the interpolation curve.

## Quick reference

| Family | `IN` | `OUT` | `IN_OUT` |
|---|---|---|---|
| Linear | `LINEAR` | — | — |
| Quadratic | `QUAD_IN` | `QUAD_OUT` | `QUAD_IN_OUT` |
| Cubic | `CUBIC_IN` | `CUBIC_OUT` | `CUBIC_IN_OUT` |
| Quartic | `QUART_IN` | `QUART_OUT` | `QUART_IN_OUT` |
| Quintic | `QUINT_IN` | `QUINT_OUT` | `QUINT_IN_OUT` |
| Sine | `SINE_IN` | `SINE_OUT` | `SINE_IN_OUT` |
| Expo | `EXPO_IN` | `EXPO_OUT` | `EXPO_IN_OUT` |
| Circ | `CIRC_IN` | `CIRC_OUT` | `CIRC_IN_OUT` |
| Back | `BACK_IN` | `BACK_OUT` | `BACK_IN_OUT` |
| Bounce | `BOUNCE_IN` | `BOUNCE_OUT` | `BOUNCE_IN_OUT` |
| Elastic | `ELASTIC_IN` | `ELASTIC_OUT` | `ELASTIC_IN_OUT` |

## Naming convention

- **`_IN`** — slow start, fast end. Use for "entering" motions.
- **`_OUT`** — fast start, slow end. Use for "exiting" or "settling" motions.
- **`_IN_OUT`** — slow both ends, fast middle. Use for "traveling" motions.

## Picking the right curve

- **UI fades & slides** — `CUBIC_OUT` or `QUART_OUT`. Feels responsive without overshoot.
- **Attention grabs** — `BACK_OUT` for a small overshoot, `ELASTIC_OUT` for a strong bounce.
- **Physical motion** — `BOUNCE_OUT` for dropping objects, `ELASTIC_IN_OUT` for rubber-band.
- **Seeking / scrubbing** — `QUAD_IN_OUT` or `SINE_IN_OUT` — smooth without character.
- **Never use `LINEAR` for UI.** Feels robotic.

## Example comparison

```java
// Opening a menu — fast into view, smooth settle
animator.sequence(300L, 1F, TweenEquations.CUBIC_OUT).start();

// Popup appearing — slight overshoot for polish
animator.sequence(400L, 1F, TweenEquations.BACK_OUT).start();

// Closing — accelerate away
animator.sequence(250L, 0F, TweenEquations.CUBIC_IN).start();

// Interactive feedback (button press) — sharp in, relax out
animator
    .sequence(80L, 0.95F, TweenEquations.QUAD_IN)
    .sequence(200L, 1F, TweenEquations.ELASTIC_OUT)
    .start();
```

## Using on Node hover

```java
node.hoverEquation(TweenEquations.CUBIC_OUT);
node.hoverDuration(250L);
```

Default is `LINEAR` — override for most nodes.

## Custom easing

Implement `TweenEquation`:

```java
public class SquareEasing extends TweenEquation {
    @Override
    public float compute(float t) {
        return t * t;
    }

    @Override
    public String toString() {
        return "Square";
    }
}

animator.sequence(400L, 100F, new SquareEasing()).start();
```

## See also

- [TweenAnimator](tween-animator.md) — full animation API.
- [Transitions](../ui/transitions.md) — UI-level easing.
