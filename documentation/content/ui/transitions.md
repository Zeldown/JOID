# Transitions

Entry / exit animations that run around the opening and closing of a `UI`, backed by the Tween Engine.

## Structure

`Transition` is abstract and final only in its static shape:

```java
public abstract class Transition {
    private final In in;
    private final Out out;
    // ...
}
```

A transition is a pair of states — one `In`, one `Out` — each with its own `TweenAnimator`. Both `In` and `Out` are themselves abstract subclasses of `TransitionState`:

```java
public static abstract class TransitionState {
    private final TweenAnimator animator;
    public TransitionState(float defaultValue) { ... }

    public abstract void init(UI ui);
    public abstract void start();
    public abstract void pre(UI ui, double mouseX, double mouseY);
    public abstract void post(UI ui, double mouseX, double mouseY);
}

public static abstract class In  extends TransitionState { public In()  { super(0F); } }
public static abstract class Out extends TransitionState { public Out() { super(1F); } }
```

`In`'s animator starts at `0F`, `Out`'s at `1F`. `init` runs once with the UI on attach, `start` kicks off the tween, `pre`/`post` wrap the UI's draw.

## Attaching a transition

Call `setTransition(...)` on your UI:

```java
public class MyUI extends UI {
    public MyUI() {
        setTransition(new PopTransition());
    }
}
```

## Built-in: `PopTransition`

`PopTransition` scales the UI from `0.75` to `1.0` around its anchor position on entry, and back on exit, using `QUART_OUT` / `QUART_IN` over `130ms`.

```java
public class PopTransition extends Transition {
    public PopTransition() {
        super(new PopInTransition(), new PopOutTransition());
    }

    public static class PopInTransition extends Transition.In {
        @Override public void init(UI ui) {}
        @Override public void start() {
            final Timeline timeline = getAnimator().sequence(130F, 1F, TweenEquations.QUART_OUT).getTimeline();
            start(timeline);
        }
        @Override public void pre(UI ui, double mx, double my) {
            final double scale = 0.75D + getAnimator().getValue() * 0.25D;
            GL11.glPushMatrix();
            GL11.glTranslated(ui.getData().getAnchorPositionX(), ui.getData().getAnchorPositionY(), 0);
            GL11.glScaled(scale, scale, 1D);
            GL11.glTranslated(-ui.getData().getAnchorPositionX(), -ui.getData().getAnchorPositionY(), 0);
        }
        @Override public void post(UI ui, double mx, double my) { GL11.glPopMatrix(); }
    }

    public static class PopOutTransition extends Transition.Out {
        // symmetrical with QUART_IN, value 0F
    }
}
```

## Writing a custom transition

A transition is always a constructor call to the parent (`super(new MyIn(), new MyOut())`) plus two inner classes extending `In` and `Out`. Inside each, wire the tween in `start()` and apply it in `pre()` / `post()`:

```java
public class SlideTransition extends Transition {

    public SlideTransition() {
        super(new SlideIn(), new SlideOut());
    }

    public static class SlideIn extends Transition.In {
        @Override public void init(UI ui) {}
        @Override public void start() {
            final Timeline timeline = getAnimator().sequence(300F, 1F, TweenEquations.CUBIC_OUT).getTimeline();
            start(timeline);
        }
        @Override public void pre(UI ui, double mx, double my) {
            final float offset = (1F - getAnimator().getValue()) * (float) ui.getWidth();
            GL11.glPushMatrix();
            GL11.glTranslatef(offset, 0F, 0F);
        }
        @Override public void post(UI ui, double mx, double my) { GL11.glPopMatrix(); }
    }

    public static class SlideOut extends Transition.Out {
        @Override public void init(UI ui) {}
        @Override public void start() {
            final Timeline timeline = getAnimator().sequence(250F, 0F, TweenEquations.CUBIC_IN).getTimeline();
            start(timeline);
        }
        @Override public void pre(UI ui, double mx, double my) {
            final float offset = (1F - getAnimator().getValue()) * -(float) ui.getWidth();
            GL11.glPushMatrix();
            GL11.glTranslatef(offset, 0F, 0F);
        }
        @Override public void post(UI ui, double mx, double my) { GL11.glPopMatrix(); }
    }
}
```

There is **no** `setIn(...)` / `setOut(...)` setter and **no** `TransitionState(equation, duration, from, to)` constructor — the class is built around the abstract methods above.

## Disabling a state

`TransitionState` exposes `enable()` / `disable()` and `isEnabled()`. A disabled state's `start()` is a no-op, so the tween never fires:

```java
ui.getTransition().getIn().disable();
```

## No-transition UIs

Don't call `setTransition(...)` at all — the UI enters and exits instantly. There is no "no-op" `Transition` constructor since the class is abstract.

## See also

- [TweenAnimator](../animations/tween-animator.md) — the animator driving each state.
- [Easing](../animations/easing.md) — easing equations.
- [Bridge](bridge.md) — how `open` / `close` drive transitions.