# Transitions

Smooth in/out animations when a UI opens or closes, backed by the Tween Engine.

## Setting a transition

Call `setTransition()` in the UI constructor or `init()`:

```java
public MyUI() {
    setTransition(new PopTransition());
}
```

`PopTransition` is the built-in scale-pop-in / scale-pop-out. It animates the `scaleLevel` signal of the UI from 0 to 1 on entry, and 1 to 0 on exit.

## The `Transition` class

A transition is two independent animators — one for `in`, one for `out` — each a `TweenAnimator` with custom easing and duration:

```java
public class PopTransition extends Transition {

    public PopTransition() {
        setIn(new TransitionState(TweenEquations.BACK_OUT, 400L, 0F, 1F));
        setOut(new TransitionState(TweenEquations.BACK_IN, 300L, 1F, 0F));
    }
}
```

| Param | Meaning |
|---|---|
| Easing | Any `TweenEquation` (see [Easing](../animations/easing.md)) |
| Duration | Milliseconds |
| From | Starting value (typically 0 or 1) |
| To | End value |

During the transition, the UI's `transition` signal drives node rendering — you can read it via `ui.getTransition().getState()` and apply custom effects.

## Writing a custom transition

Extend `Transition` and define both states:

```java
public class SlideTransition extends Transition {

    public SlideTransition() {
        setIn(new TransitionState(TweenEquations.CUBIC_OUT, 500L, -1920F, 0F));
        setOut(new TransitionState(TweenEquations.CUBIC_IN, 400L, 0F, 1920F));
    }
}
```

Read the current offset in `preDraw` to apply:

```java
@Override
public void preDraw(double mouseX, double mouseY) {
    final float offset = getTransition().getState().getAnimator().getValue();
    GL11.glTranslatef(offset, 0F, 0F);
}
```

## How the bridge drives it

When `JOID.open(ui)` is called on a UI that already has a `Transition`:

1. The previous UI's **out** transition starts.
2. When it finishes, the new UI is `add`ed.
3. The new UI's **in** transition starts.

Chaining is automatic. If you open without closing (overlay mode), only the `in` of the new UI runs.

## Disabling transitions

Some UIs shouldn't animate — transient popups, HUDs. Just don't set a transition:

```java
public HUDOverlay() {
    // no setTransition call
}
```

Or set a no-op:

```java
setTransition(new Transition());  // no in/out → instant
```

## Best practices

- **Keep durations short.** 200-400ms feels snappy; anything over 600ms feels laggy.
- **Match in/out easings.** `CUBIC_OUT` on entry pairs with `CUBIC_IN` on exit.
- **Don't mutate the tree during a transition.** Build your nodes in `init()`, let the transition handle the visual progress.

## See also

- [TweenAnimator](../animations/tween-animator.md) — full animation system.
- [Easing](../animations/easing.md) — catalog of easing equations.
- [Bridge](bridge.md) — how `open`/`close` trigger transitions.
