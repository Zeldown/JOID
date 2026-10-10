# Tween Engine

The tween engine (`dev.joid.lib.animation.tween`) animates `float` attributes of any object: one or several at once, chained in timelines, through waypoints. [`TweenAnimator`](../concepts/animation.md) is built on it; use the engine when you animate your own objects or several attributes together.

```java
public class WelcomeUI extends UI {

	private final TweenManager manager = new TweenManager();
	private final MutableFloat opacity = new MutableFloat(0F);

	@Override
	public void init() {
		Tween.to(this.opacity, 0, 600F).target(1F).ease(TweenEquations.CUBIC_OUT).start(this.manager);

		RectNode.create(760, 440, 400, 200).color(() -> Color.WHITE.copyAlpha(this.opacity.floatValue())).attach(this);
	}

	@Override
	public void update() {
		this.manager.update((float) super.getFrameTime());
	}

}
```

![A white rectangle fading in over a gray frame](../images/tween-fade.gif "The tween moves opacity from 0 to 1 in 600 ms with CUBIC_OUT.")

A tween needs a target, a `TweenAccessor` that reads and writes its attributes, and a `TweenManager` that you update every frame. `MutableFloat` and `MutableInteger` (`dev.joid.lib.animation.tween.primitive`) are their own accessors. Durations are in milliseconds, like `UI.getFrameTime()`.

## Accessors with TweenAccessor

A `TweenAccessor<T>` reads the attributes of a tween type into an array and returns how many it read, then writes the interpolated values back:

```java
public class NodeAccessor implements TweenAccessor<Node> {

	public static final int POSITION = 0;
	public static final int SIZE     = 1;

	@Override
	public int getValues(final Node target, final int tweenType, final float[] returnValues) {
		if (tweenType == NodeAccessor.POSITION) {
			returnValues[0] = (float) target.getX();
			returnValues[1] = (float) target.getY();
			return 2;
		}

		returnValues[0] = (float) target.getWidth();
		returnValues[1] = (float) target.getHeight();
		return 2;
	}

	@Override
	public void setValues(final Node target, final int tweenType, final float[] newValues) {
		if (tweenType == NodeAccessor.POSITION) {
			target.x(newValues[0]).y(newValues[1]);
		} else {
			target.width(newValues[0]).height(newValues[1]);
		}
	}

}
```

Register it once at startup; any node can then be tweened:

```java
private final TweenManager manager = new TweenManager();

Tween.registerAccessor(Node.class, new NodeAccessor());

final RectNode card = RectNode.create(100, 300, 200, 120).color(Color.decode("#DDDDDD")).attach(this);
Tween.to(card, NodeAccessor.POSITION, 500F).target(1200F, 300F).ease(TweenEquations.QUART_OUT).start(this.manager);
```

![A light card sliding to the right and slowing down](../images/tween-card.gif "The card moves from x 100 to x 1200 in 500 ms with QUART_OUT.")

The next examples use this `card`, this `manager` and this `NodeAccessor`. A tween that writes a node property replaces the signal or lambda given to that setter before.

## Creating tweens

| Factory | Description |
|---|---|
| `Tween.to(target, type, duration)` | From the current values to `target(...)`. |
| `Tween.from(target, type, duration)` | From `target(...)` to the current values. |
| `Tween.set(target, type)` | Applies `target(...)` at once, as a timeline step. |
| `Tween.call((type, source) -> ...)` | Runs code when a timeline reaches it. |

`target(...)` takes one value per attribute; `targetRelative(...)` adds offsets to the start values. `ease(...)` takes a `TweenEquations` constant (see [Animation](../concepts/animation.md#easing-with-tweenequations)), `delay(ms)` waits before the first play.

```java
Tween.to(card, NodeAccessor.SIZE, 150F).targetRelative(10F, 10F).repeatYoyo(5, 0F).start(this.manager);
```

`repeat(count, delay)` plays `count` more times; `repeatYoyo(count, delay)` plays every second time backward. `Tween.INFINITY` repeats forever.

## Timelines

A `Timeline` plays tweens one after the other (`createSequence()`) or together (`createParallel()`), and nests:

![Two time lines: three tweens then a call one after the other for createSequence, three tweens starting together for createParallel](../images/diagram-tween-timeline.png "A sequence lasts the sum of its children; a parallel timeline lasts as long as its longest child.")

```java
Timeline
.createSequence()
.push(Tween.set(card, NodeAccessor.POSITION).target(100F, 300F))
.push(Tween.to(card, NodeAccessor.POSITION, 400F).target(800F, 300F))
.pushPause(200F)
.beginParallel()
.push(Tween.to(card, NodeAccessor.POSITION, 400F).target(800F, 600F))
.push(Tween.to(card, NodeAccessor.SIZE, 400F).target(300F, 200F))
.end()
.push(Tween.call((type, source) -> System.out.println("Arrived")))
.repeatYoyo(1, 500F)
.start(this.manager);
```

![A light card moving right, then down while growing, then playing the whole path backward](../images/tween-timeline.gif "The sequence, then its yoyo repetition played backward after 500 ms.")

A timeline is a tween object too: it takes `delay`, `repeat`, `repeatYoyo` and callbacks. Close every `beginSequence()` or `beginParallel()` with `end()`, and push everything before `start`.

## Callbacks

```java
Tween
.to(card, NodeAccessor.POSITION, 400F)
.target(800F, 0F)
.addCallback(TweenCallback.COMPLETE, tween -> System.out.println("Done"))
.start(this.manager);
```

`addCallback(flags, tween -> ...)` runs for the given events: `BEGIN`, `START`, `END` and `COMPLETE` forward (one play fires them in this order), the `BACK_` events backward, `ANY` for all. `setCallback((type, source) -> ...)` with `setCallbackTriggers(flags)` receives the event type (`COMPLETE` by default).

## Paths with waypoint

Waypoints make a tween pass through intermediate values on a smooth Catmull-Rom curve (`path(TweenPaths.linear)` for straight segments). Raise their limit, `0` by default, once at startup:

```java
Tween.setWaypointsLimit(4);

Tween
.to(card, NodeAccessor.POSITION, 1200F)
.target(1600F, 800F)
.waypoint(400F, 100F)
.waypoint(1200F, 100F)
.ease(TweenEquations.SINE_INOUT)
.start(this.manager);
```

![A light card rising, crossing the canvas near the top past two gray dots, then descending to the bottom right](../images/tween-waypoints.gif "The path passes through both waypoints (gray dots) before reaching the target.")

## Reference

| Method | Description |
|---|---|
| `Tween.registerAccessor(Class, TweenAccessor)` | Accessor of a class. |
| `target(...)`, `targetRelative(...)` | End values, absolute or relative. |
| `ease(TweenEquation)` | Easing; `LINEAR` by default. |
| `delay(ms)`, `repeat(count, delay)`, `repeatYoyo(count, delay)` | Timing of a tween or timeline. |
| `waypoint(...)`, `path(TweenPath)` | Intermediate values and the curve through them. |
| `addCallback(flags, callback)`, `setCallback(callback)` | Event callbacks. |
| `start(manager)` | Adds it to a manager and starts it. |
| `pause()`, `resume()`, `kill()` | Controls one tween or timeline. |
| `Timeline.createSequence()`, `createParallel()` | New timeline. |
| `push(...)`, `pushPause(ms)`, `beginSequence()`, `beginParallel()`, `end()` | Builds a timeline. |
| `TweenManager.update(delta)` | Advances every tween by `delta` milliseconds. |
| `pause()`, `resume()`, `killAll()`, `killTarget(target)`, `containsTarget(target)` | Controls the tweens of a manager. |

## Good to know

- Nothing moves without `manager.update(...)` every frame, for example in the `update()` of the UI.
- A finished tween returns to a pool and is reused: do not keep a reference to it after it finished.
- `Tween.setWaypointsLimit(...)` must run before any tween with waypoints is created.

## See also

- [Animation](../concepts/animation.md): `TweenAnimator`, easing and the hover animation.
- [Frame Loop and Dev Tools](../concepts/frame-loop.md): `update()` and the frame time.
- [Nodes](../concepts/nodes.md): the setters an accessor writes.
- [Custom Nodes](../nodes/custom-nodes.md): animating your own nodes.