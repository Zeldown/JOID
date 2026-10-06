# Transitions

A transition animates a UI when it opens and when it closes. `Transition` (`dev.joid.lib.ui.core.transition`) holds an In state, played after each `init()`, and an Out state, played before the UI is removed. JOID ships `PopTransition`, used by default by popups, and you can write your own.

## Applying a transition

```java
final SettingsUI settings = new SettingsUI();
settings.setTransition(new PopTransition());
JOID.open(settings);
```

![A settings card pops in from a smaller size, then shrinks away when closed with Escape](../images/transition-pop.gif "PopTransition: 130 ms in with QUART_OUT, 130 ms out with QUART_IN; the background dims with it.")

`ui.setTransition(Transition transition)` sets it, `null` removes it, and `getTransition()` returns it. Set it before opening the UI or inside `init()`: the In state starts right after `init()`.

## How a UI plays its transition

| Moment | What the UI does |
| --- | --- |
| After each `init()` (first load and reloads) | If the In state is enabled, calls `in.init(ui)` then `in.start()`. If the Out state is enabled, calls `out.init(ui)`. |
| Every draw while a state runs | Calls `update()` on the state, then `pre(ui, mouseX, mouseY)` before drawing the view, its nodes and its tooltips, and `post(ui, mouseX, mouseY)` after them, even when the drawing throws. |
| While a state runs | Multiplies the alpha of the `@UIData` background by the state's animator value, so the background fades with the transition. |
| Closing (`ui.onClose()`) | Once `close()` agreed, if the Out state is enabled, calls `out.start()` and removes the UI when the Out timeline ends. Close requests made while it runs are ignored. Without an enabled Out state, the UI is removed at once. |

`JOID.close(ui, true)` and `JOID.open(ui, true)` remove UIs without playing their Out state.

`pre` and `post` wrap the view transform, so their transformations apply in the host's coordinate space (window pixels with the projection of the [Quick Start](../getting-started/quick-start.md)), around the whole UI except its background and `drawBackground`.

## PopTransition

`PopTransition` (`dev.joid.lib.ui.core.transition.impl`) scales the UI around its anchor position (`getData().getAnchorPositionX()`, `getAnchorPositionY()`):

| State | Class | Animation |
| --- | --- | --- |
| In | `PopTransition.PopInTransition` | Scale from 0.75 to 1 in 130 ms, `TweenEquations.QUART_OUT`. |
| Out | `PopTransition.PopOutTransition` | Scale from 1 to 0.75 in 130 ms, `TweenEquations.QUART_IN`. |

The scale is `0.75 + 0.25 × value`, where `value` is the animator value of the state (0 to 1 for In, 1 to 0 for Out).

### Popup default transitions

A UI annotated `@UIDataPopup(active = true)` gets a `PopTransition` at construction. The `transition` attribute chooses the states:

| `PopupTransition` | In | Out |
| --- | --- | --- |
| `IN_OUT` (default) | Enabled | Enabled |
| `IN` | Enabled | Disabled |
| `OUT` | Disabled | Enabled |
| `NONE` | No transition at all | |

```java
@UIDataPopup(active = true, transition = PopupTransition.IN)
public final class ToastPopup extends UI {}
```

`setTransition` replaces the default transition of a popup.

## Writing a transition

Extend `Transition` and pass an `In` and an `Out` to its constructor. Each state implements four methods:

| Method | Role |
| --- | --- |
| `void init(UI ui)` | Prepares the state for `ui`; called after each `init()` of the UI. |
| `void start()` | Builds a timeline on the state's animator and starts it with `start(Timeline)`. The Out state must start a timeline: the UI is removed when it ends. |
| `void pre(UI ui, double mouseX, double mouseY)` | Applies the effect before the UI draws, typically by pushing a matrix. |
| `void post(UI ui, double mouseX, double mouseY)` | Undoes what `pre` did. |

This transition slides the UI up from 200 pixels below when it opens, and back down when it closes:

```java
public final class SlideTransition extends Transition {

    public SlideTransition() {
        super(new SlideIn(), new SlideOut());
    }

    private static void push(final double offset) {
        final IRenderBridge render = BridgeHandler.RENDER.get();
        render.pushMatrix();
        render.translate(0D, offset, 0D);
    }

    public static final class SlideIn extends Transition.In {

        @Override
        public void init(final UI ui) {}

        @Override
        public void start() {
            this.start(this.getAnimator().sequence(400F, 1F, TweenEquations.QUART_OUT).getTimeline());
        }

        @Override
        public void pre(final UI ui, final double mouseX, final double mouseY) {
            SlideTransition.push(200D * (1D - this.getAnimator().getValue()));
        }

        @Override
        public void post(final UI ui, final double mouseX, final double mouseY) {
            BridgeHandler.RENDER.get().popMatrix();
        }

    }

    public static final class SlideOut extends Transition.Out {

        @Override
        public void init(final UI ui) {}

        @Override
        public void start() {
            this.start(this.getAnimator().sequence(300F, 0F, TweenEquations.QUART_IN).getTimeline());
        }

        @Override
        public void pre(final UI ui, final double mouseX, final double mouseY) {
            SlideTransition.push(200D * (1D - this.getAnimator().getValue()));
        }

        @Override
        public void post(final UI ui, final double mouseX, final double mouseY) {
            BridgeHandler.RENDER.get().popMatrix();
        }

    }

}
```

![A settings card slides up into place, then slides down and disappears when closed with Escape](../images/transition-slide.gif "SlideTransition: 400 ms in from 200 pixels below, 300 ms out.")

The animator of an In state starts at 0 and the one of an Out state at 1; animate them towards 1 and 0. `sequence(duration, value, equation)` builds a timeline on the animator (duration in milliseconds); see [TweenAnimator](../animation/tween-animator.md) and [Easing](../animation/easing.md). Instead of raw matrix calls, `pre` and `post` can apply and reset a `Transformation`; see [Transformations and Framebuffers](../drawing/transformations.md).

A transition with a single state passes `null` for the other one: `super(new SlideIn(), null)` plays only on opening.

## Reference

### Transition

| Member | Description |
| --- | --- |
| `Transition(In in, Out out)` | Constructor for subclasses; either state can be `null`. |
| `In getIn()`, `Out getOut()` | The states. |

### Transition.TransitionState

Base class of `Transition.In` (animator starts at `0F`) and `Transition.Out` (animator starts at `1F`).

| Member | Description |
| --- | --- |
| `TweenAnimator getAnimator()` | The animator whose value drives the state. |
| `void start(Timeline timeline)` | Sets `timeline` on the animator and starts it; does nothing when the state is disabled. |
| `abstract void start()` | Starts the state; implement it with `start(Timeline)`. |
| `abstract void init(UI ui)`, `abstract void pre(UI ui, double mouseX, double mouseY)`, `abstract void post(UI ui, double mouseX, double mouseY)` | See [Writing a transition](#writing-a-transition). |
| `final void update()` | Advances the animator; the state stops running when its timeline is finished. Called by the UI. |
| `boolean isRunning()` | Whether the state is playing. |
| `boolean isEnabled()`, `void enable()`, `void disable()` | A disabled state never starts. States are enabled when created. |

## See also

- [The UI Class](ui-class.md)
- [Opening and Closing UIs](managing-uis.md)
- [TweenAnimator](../animation/tween-animator.md)
- [Easing](../animation/easing.md)