# Transitions

A transition animates a UI when it opens and when it closes. `Transition` (`dev.joid.lib.ui.core.transition`) holds an In state, played after each `init()`, and an Out state, played before the UI is removed. JOID ships `PopTransition`, the default of popups, and you write your own by extending `Transition`. This page closes the UIs guide; a transition is driven by a [TweenAnimator](../animation/tween-animator.md), seen in the Animation guide.

## Applying a transition with setTransition

```java
final SettingsUI settings = new SettingsUI();
settings.setTransition(new PopTransition());
JOID.open(settings);
```

![A card pops in from a smaller size, then shrinks away when closed with Escape](../images/transition-pop.gif "PopTransition: 130 ms in with QUART_OUT, 130 ms out with QUART_IN; the background fades with it.")

`ui.setTransition(Transition transition)` sets it and returns the UI, `null` removes it, and `getTransition()` returns it. Set it before opening the UI or in `init()`: the In state starts right after `init()`.

## How a UI plays its transition

![Boxes from init() to the In state, the shown UI, close(), the Out state and the removed UI](../images/diagram-transition-states.png "The In state runs after every init(); the Out state runs once close() agreed, and the UI is removed when its timeline ends.")

| Moment | What the UI does |
| --- | --- |
| After each `init()` (first load, reload, renew) | If the In state is enabled, calls `in.init(ui)` then `in.start()`. If the Out state is enabled, calls `out.init(ui)`. |
| Every draw while a state runs | Calls `update()` on the state, then `pre(ui, mouseX, mouseY)` before drawing the view, its nodes and its tooltips, and `post(ui, mouseX, mouseY)` after them, even when the drawing throws. |
| While a state runs | Multiplies the alpha of the `@UIData` background by the animator value of the state, so the background fades with the transition. |
| Closing (`ui.fireClose()`) | Once `close()` agreed, if the Out state is enabled, calls `out.start()` and removes the UI (`dispose()`, then the bridge's `close`) when the Out timeline ends. Close requests made meanwhile are refused. Without an enabled Out state, the UI is removed at once. |

A state stops running when the timeline of its animator ends (`getAnimator().getTimeline()` returns `null`). The `force` variants of `JOID.open` and `JOID.close` remove UIs without playing their Out state.

`pre` and `post` wrap the view transform: their transformations apply in the host's coordinate space (window pixels with the projection of the [Quick Start](../getting-started/quick-start.md)), around the whole UI except its background and `drawBackground`.

Inside the view, positions are units of the 1920×1080 virtual canvas, fitted to the window without stretching; wider or taller windows show extra canvas around it. A transition works outside that fit, so its offsets are window pixels.

![The 1920×1080 canvas fitted into a 16:9, a 21:9 and a 4:3 window; the extra visible area is hatched](../images/diagram-canvas.png "One canvas, fitted into every window")

See [The Virtual Canvas](../concepts/canvas.md) for the fit and the conversions between window pixels and canvas units.

## PopTransition

`PopTransition` (`dev.joid.lib.ui.core.transition.impl`) scales the UI around `getData().getAnchorPositionX()` / `getAnchorPositionY()`:

| State | Class | Animation |
| --- | --- | --- |
| In | `PopTransition.PopInTransition` | Scale from 0.75 to 1 in 130 ms, `TweenEquations.QUART_OUT`. |
| Out | `PopTransition.PopOutTransition` | Scale from 1 to 0.75 in 130 ms, `TweenEquations.QUART_IN`. |

The scale is `0.75 + 0.25 × value`, where `value` is the animator value of the state (0 to 1 for In, 1 to 0 for Out).

### Popup default transitions

A UI annotated `@UIDataPopup(active = true)` gets a `PopTransition`. The `transition` attribute chooses the states:

| `PopupTransition` | In | Out |
| --- | --- | --- |
| `IN_OUT` (default) | Enabled | Enabled |
| `IN` | Enabled | Disabled |
| `OUT` | Disabled | Enabled |
| `NONE` | No transition | No transition |

```java
@UIDataPopup(active = true, transition = PopupTransition.IN)
public class ToastPopup extends UI {}
```

`setTransition` replaces the default transition of a popup. `getPopup().setActive(...)` and `getPopup().setTransition(...)` create or remove the pop transition from the next frame, and a reload does the same when you edit `@UIDataPopup`; a transition set with `setTransition` is kept as long as these popup settings do not change.

```java
popup.getPopup().setTransition(PopupTransition.NONE);
```

## Writing a transition

Extend `Transition` and pass an `In` and an `Out` to its constructor. Each state implements four methods:

| Method | Role |
| --- | --- |
| `void init(UI ui)` | Prepares the state for `ui`; called after each `init()` of the UI. |
| `void start()` | Builds a timeline on the state's animator and starts it with `start(Timeline)`. The Out state must start a timeline that ends: the UI is removed when it ends. |
| `void pre(UI ui, double mouseX, double mouseY)` | Applies the effect before the UI draws, typically by pushing a matrix. |
| `void post(UI ui, double mouseX, double mouseY)` | Undoes what `pre` did. |

`pre` and `post` move the whole UI through the render bridge of the backend, `BridgeHandler.RENDER.get()` (an `IRenderBridge`, see [Bridges and Backends](../concepts/bridges.md)): `pushMatrix()` saves the current transform, `translate(x, y, z)` moves everything drawn after it, and `popMatrix()` restores the saved transform. This transition slides the UI up from 200 pixels below when it opens, and back down when it closes:

```java
public class SlideTransition extends Transition {

	public SlideTransition() {
		super(new SlideIn(), new SlideOut());
	}

	private static void push(final double offset) {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.pushMatrix();
		render.translate(0D, offset, 0D);
	}

	public static class SlideIn extends Transition.In {

		@Override
		public void init(final @NonNull UI ui) {}

		@Override
		public void start() {
			this.start(super.getAnimator().sequence(400F, 1F, TweenEquations.QUART_OUT).getTimeline());
		}

		@Override
		public void pre(final @NonNull UI ui, final double mouseX, final double mouseY) {
			SlideTransition.push(200D * (1D - super.getAnimator().getValue()));
		}

		@Override
		public void post(final @NonNull UI ui, final double mouseX, final double mouseY) {
			BridgeHandler.RENDER.get().popMatrix();
		}

	}

	public static class SlideOut extends Transition.Out {

		@Override
		public void init(final @NonNull UI ui) {}

		@Override
		public void start() {
			this.start(super.getAnimator().sequence(300F, 0F, TweenEquations.QUART_IN).getTimeline());
		}

		@Override
		public void pre(final @NonNull UI ui, final double mouseX, final double mouseY) {
			SlideTransition.push(200D * (1D - super.getAnimator().getValue()));
		}

		@Override
		public void post(final @NonNull UI ui, final double mouseX, final double mouseY) {
			BridgeHandler.RENDER.get().popMatrix();
		}

	}

}
```

![A card slides up into place, then slides down and disappears when closed with Escape](../images/transition-slide.gif "SlideTransition: 400 ms in from 200 pixels below, 300 ms out.")

The animator of an In state starts at `0F` and the one of an Out state at `1F`; animate them towards `1F` and `0F`. `sequence(duration, value, equation)` builds a timeline on the animator (duration in milliseconds); see [TweenAnimator](../animation/tween-animator.md) and [Easing](../animation/easing.md). Instead of raw matrix calls, `pre` and `post` can apply and reset a `Transformation`; see [Transformations and Framebuffers](../drawing/transformations.md).

A transition with a single state passes `null` for the other one:

```java
public static class InOnly extends Transition {

	public InOnly() {
		super(new SlideTransition.SlideIn(), null);
	}

}
```

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
| `start(Timeline timeline)` | Sets `timeline` on the animator and starts it; does nothing when the state is disabled. |
| `abstract void start()` | Starts the state; implement it with `start(Timeline)`. |
| `abstract void init(UI ui)`, `abstract void pre(UI ui, double mouseX, double mouseY)`, `abstract void post(UI ui, double mouseX, double mouseY)` | See [Writing a transition](#writing-a-transition). |
| `final void update()` | Advances the animator; the state stops running when its timeline ends. Called by the UI. |
| `boolean isRunning()` | Whether the state plays. |
| `boolean isEnabled()`, `enable()`, `disable()` | A disabled state never starts. States are enabled when created. |

## Pitfalls

- An Out state whose timeline repeats forever never ends: the UI is never removed.
- Every `pushMatrix` in `pre` needs its `popMatrix` in `post`; `post` runs even when the drawing throws.
- `pre` and `post` run outside the canvas transform: offsets are in the host's units (window pixels), not canvas units.
- `JOID.close(ui, true)` skips the Out state: use it only when the UI must disappear at once.

## See also

- Next: [Resources](../resources/resources.md)
- [The UI Class](ui-class.md)
- [Opening and Closing UIs](managing-uis.md)
- [TweenAnimator](../animation/tween-animator.md)
- [Easing](../animation/easing.md)
- [Transformations and Framebuffers](../drawing/transformations.md)