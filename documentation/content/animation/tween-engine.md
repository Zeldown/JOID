# Tween Engine

The tween engine (`dev.joid.lib.animation.tweenengine`) animates any `float` attributes of any object: tweens interpolate up to three attributes at once, timelines chain and group them, and a `TweenManager` updates everything. [`TweenAnimator`](tween-animator.md) runs on it; use the engine directly to animate your own objects, several attributes together, or along a path. Its API follows the Universal Tween Engine (`Tween.getVersion()` returns `"6.3.3"`).

## A first tween

A tween needs a target object, a way to read and write its attributes (a [`TweenAccessor`](#tween-accessors)) and a manager that you update every frame. `MutableFloat` (`dev.joid.lib.animation.tweenengine.primitive`) is a `float` holder that is its own accessor:

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
	public void preDraw(final double mouseX, final double mouseY) {
		this.manager.update((float) super.getFrameTime());
	}

}
```

![A white rectangle fading in over a gray frame](../images/tween-fade.gif "The tween moves opacity from 0 to 1 in 600 ms with CUBIC_OUT.")

- `Tween.to(target, tweenType, duration)` creates a tween that moves the target's attributes from their current values to `target(...)`.
- `start(manager)` adds it to the manager, which starts it.
- `manager.update(delta)` advances every tween of the manager. `UI.getFrameTime()` is the duration of the last frame in milliseconds.

The engine has no time unit of its own: durations, delays and update deltas only need to use the same one. JOID uses milliseconds everywhere (`TweenAnimator`, `UI.getFrameTime()`, hover durations).

## Tween accessors

### Writing a TweenAccessor

A `TweenAccessor<T>` reads and writes the attributes of a class. The `tweenType` (any `int` you choose) selects which attributes:

```java
public class NodeAccessor implements TweenAccessor<Node> {

	public static final int SIZE     = 1;
	public static final int POSITION = 0;

	@Override
	public int getValues(final Node target, final int tweenType, final float[] returnValues) {
		switch (tweenType) {
		case NodeAccessor.POSITION:
			returnValues[0] = (float) target.getX();
			returnValues[1] = (float) target.getY();
			return 2;
		case NodeAccessor.SIZE:
			returnValues[0] = (float) target.getWidth();
			returnValues[1] = (float) target.getHeight();
			return 2;
		default:
			return 0;
		}
	}

	@Override
	public void setValues(final Node target, final int tweenType, final float[] newValues) {
		switch (tweenType) {
		case NodeAccessor.POSITION:
			target.x(newValues[0]).y(newValues[1]);
			break;
		case NodeAccessor.SIZE:
			target.width(newValues[0]).height(newValues[1]);
			break;
		default:
			break;
		}
	}

}
```

| Method | Contract |
| --- | --- |
| `int getValues(T target, int tweenType, float[] returnValues)` | Writes the current values of the attributes into `returnValues` and returns how many there are (at most the [combined attributes limit](#limits), `3` by default). |
| `void setValues(T target, int tweenType, float[] newValues)` | Applies the interpolated values. |

`setValues` calls the value setters of the node: each call sets a fixed value, which replaces a signal or a lambda given before to the same setter. Register the accessor once, at startup (before opening your UIs):

```java
Tween.registerAccessor(Node.class, new NodeAccessor());
```

Then any node can be tweened, here in the `init()` of the UI of [A first tween](#a-first-tween):

```java
final RectNode card = RectNode.create(100, 300, 200, 120).color(Color.decode("#DDDDDD")).attach(this);
Tween.to(card, NodeAccessor.POSITION, 500F).target(1200F, 300F).ease(TweenEquations.QUART_OUT).start(this.manager);
```

![A light card sliding to the right and slowing down](../images/tween-card.gif "The card moves from x 100 to x 1200 in 500 ms with QUART_OUT (0.5× scale).")

The next examples reuse this `card` and the UI's `manager`.

### How the accessor is found

- `Tween.registerAccessor(Class<?>, TweenAccessor<?>)` registers one accessor per class; registering again replaces it. `Tween.getRegisteredAccessor(Class<?>)` returns it, or `null`.
- A tween looks for an accessor registered for the target's class, then for its superclasses (a `RectNode` uses the `Node` accessor above). Interfaces are not searched.
- A target that implements `TweenAccessor` and has no accessor registered for its own class is its own accessor (`MutableFloat`, `MutableInteger`).
- `cast(Class<?>)` forces the class used for the lookup. After the tween started, it throws `RuntimeException("You can't cast the target of a tween once it is started")`.
- When nothing is found, starting the tween throws `RuntimeException("No TweenAccessor was found for the target")`.
- The accessor of [`TweenAnimator`](tween-animator.md#tweening-an-animator-with-tweenanimatoraccessor) is registered automatically.

The registry is a plain static map: register the accessors at startup, before tweens run.

### MutableFloat and MutableInteger

Both extend `Number` and implement `TweenAccessor` for themselves, with a single attribute; the tween type is ignored.

| Class | Constructor | Write | Read |
| --- | --- | --- | --- |
| `MutableFloat` | `new MutableFloat(float value)` | `setValue(float)` | `floatValue()`, `doubleValue()`, `intValue()`, `longValue()` |
| `MutableInteger` | `new MutableInteger(int value)` | `setValue(int)` | `intValue()`, `longValue()`, `floatValue()`, `doubleValue()` |

`MutableInteger` truncates the interpolated value to an `int` at each update.

## Creating tweens

| Factory | Tween |
| --- | --- |
| `Tween.to(Object target, int tweenType, float duration)` | From the current values to the values given to `target(...)`. |
| `Tween.from(Object target, int tweenType, float duration)` | From the values given to `target(...)` to the current values. |
| `Tween.set(Object target, int tweenType)` | Duration `0`: applies the `target(...)` values at once. |
| `Tween.call(TweenCallback callback)` | Duration `0`, no target: runs `callback` with `TweenCallback.START` when reached. Use it to run code at a point of a timeline. |
| `Tween.mark()` | Duration `0`, no target, does nothing. With `delay(...)` it makes a pause; `Timeline.pushPause(...)` uses it. |

- `Tween.to` and `Tween.from` use `TweenEquations.QUAD_INOUT` and the `TweenPaths.catmullRom` path by default; `Tween.set` uses `QUAD_INOUT` and no path.
- A negative duration throws `RuntimeException("Duration can't be negative")`.
- The current values are read when the tween begins (after its delay), not when it is created. In a sequence, a tween starts from wherever the previous tweens left the target.

| Method | Description |
| --- | --- |
| `target(float)`, `target(float, float)`, `target(float, float, float)`, `target(float...)` | Absolute target values, one per attribute, in the order of the accessor. |
| `targetRelative(float)`, `targetRelative(float, float)`, `targetRelative(float, float, float)`, `targetRelative(float...)` | Offsets added to the start values when the tween begins. |

```java
Tween.to(card, NodeAccessor.POSITION, 300F).targetRelative(0F, -20F).ease(TweenEquations.BACK_OUT).start(this.manager);
```

## Timelines

A `Timeline` plays tweens and other timelines one after the other (sequence) or together (parallel). It is itself a tween object: it can be delayed, repeated, given callbacks and nested.

![Two time lines: three steps one after the other for a sequence, three steps starting together for a parallel timeline](../images/diagram-tween-timeline.png "A sequence lasts the sum of its children; a parallel timeline lasts as long as its longest child.")

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

![A light card moving right, then down while growing, then playing the whole path backward](../images/tween-timeline.gif "The sequence, then its yoyo repetition played backward after 500 ms (0.4× scale).")

| Method | Description |
| --- | --- |
| `static createSequence()` | New sequence: each child starts when the previous one ends (its delay, duration and repetitions included). |
| `static createParallel()` | New parallel timeline: all children start together. |
| `push(Tween)`, `push(Timeline)` | Adds a child. Every `begin...()` of a pushed timeline must be closed, otherwise `RuntimeException("You forgot to call a few 'end()' statements in your pushed timeline")`. |
| `pushPause(float time)` | Adds an empty step of `time`: a gap in a sequence, a minimum duration in a parallel timeline. |
| `beginSequence()`, `beginParallel()` | Opens a nested sequence / parallel timeline; the following `push...` calls go into it. |
| `end()` | Closes the innermost opened nested timeline. On the root timeline it throws `RuntimeException("Nothing to end...")`. |
| `getChildren()` | Children of the timeline being built (of the innermost opened one while building). |

- A timeline is built when it starts: from then on, `push...`, `begin...` and `end()` throw `RuntimeException("You can't push anything to a timeline once it is started")`.
- A child with infinite repetitions cannot go into a timeline: building it throws a `RuntimeException`.
- When a timeline repeats, its children play again from their start values (read during the first play).

## Running tweens with TweenManager

A `TweenManager` holds tweens and timelines and updates them together.

- A finished (or killed) object is removed at the next `update(...)` and returned to its [pool](#pooling): another `Tween.to(...)` or `Timeline.create...()` can then reuse it. Do not keep using a tween or timeline once it finished, or disable its auto remove and call `free()` yourself.
- `add`, `update`, the `kill...`, `contains...` and counting methods synchronize on a lock of the manager. The pools and the accessor registry are not synchronized: create and configure tweens on the thread that updates them.
- Without a manager, call `start()` on the object and `update(delta)` on it yourself; nothing frees it.

## Delays, repeat and yoyo

| Method | Description |
| --- | --- |
| `delay(float delay)` | Waits before the first play. Adds to the current delay when called several times. |
| `repeat(int count, float delay)` | Plays `count` more times (`count + 1` plays in total), waiting `delay` between plays. `Tween.INFINITY` (`-1`) repeats forever. A negative `delay` counts as `0`. |
| `repeatYoyo(int count, float delay)` | Same, but every second play runs backward, back to the start values. |

```java
Tween.to(card, NodeAccessor.SIZE, 150F).targetRelative(10F, 10F).repeatYoyo(5, 0F).start(this.manager);
```

`repeat(...)` and `repeatYoyo(...)` throw `RuntimeException("You can't change the repetitions of a tween or timeline once it is started")` once the object started; the last of the two calls wins. `getFullDuration()` returns `delay + duration + (repeatDelay + duration) × count`, or `-1` for infinite repetitions.

## Callbacks

### Events

| Constant | Value | Fired when |
| --- | --- | --- |
| `TweenCallback.BEGIN` | `0x01` | Playback enters the first play forward (after the delay). |
| `TweenCallback.START` | `0x02` | A play starts, forward. Once per play. |
| `TweenCallback.END` | `0x04` | A play ends, forward. Once per play. |
| `TweenCallback.COMPLETE` | `0x08` | The last play ends, forward. |
| `TweenCallback.BACK_BEGIN` | `0x10` | Playing backward (negative delta) enters a completed object from its end. |
| `TweenCallback.BACK_START` | `0x20` | A play is entered from its end, backward. |
| `TweenCallback.BACK_END` | `0x40` | A play is left through its start, backward. |
| `TweenCallback.BACK_COMPLETE` | `0x80` | Playing backward reaches the very beginning. |
| `TweenCallback.ANY_FORWARD`, `ANY_BACKWARD`, `ANY` | `0x0F`, `0xF0`, `0xFF` | Any forward, backward or event. |

A single forward play fires `BEGIN`, `START`, `END`, `COMPLETE` in this order.

### setCallback, setCallbackTriggers and addCallback

```java
Tween
.to(card, NodeAccessor.POSITION, 400F)
.target(800F, 0F)
.setCallback((type, source) -> System.out.println("Event " + type))
.setCallbackTriggers(TweenCallback.START | TweenCallback.COMPLETE)
.start(this.manager);

Timeline
.createSequence()
.push(Tween.to(card, NodeAccessor.SIZE, 200F).target(300F, 300F))
.addCallback(TweenCallback.END, timeline -> System.out.println("Grown"))
.start(this.manager);
```

| Method | Description |
| --- | --- |
| `setCallback(TweenCallback callback)` | Adds a callback `(int type, BaseTween<?> source) -> ...`, called for every event enabled by the triggers. Several calls chain the callbacks in order. |
| `setCallbackTriggers(int flags)` | Replaces the enabled events (default `TweenCallback.COMPLETE`). |
| `addCallback(int flags, Consumer<BaseTween<?>> callback)` | Enables `flags` on top of the current triggers and adds a callback that runs only for those events. |

`TweenCallback` is a functional interface with `onEvent(int type, BaseTween<?> source)` and a default `andThen(TweenCallback)`.

## Paths and waypoints

Waypoints make a tween pass through intermediate values. The number of waypoints per tween is limited, and the limit is `0` by default: raise it once at startup.

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

![A light card rising, crossing the canvas near the top past two gray dots, then descending to the bottom right](../images/tween-waypoints.gif "The Catmull-Rom path passes through both waypoints (gray dots) before reaching the target (0.35× scale).")

| Method | Description |
| --- | --- |
| `waypoint(float)`, `waypoint(float, float)`, `waypoint(float, float, float)`, `waypoint(float...)` | Adds a waypoint, one value per attribute. Throws a `RuntimeException` beyond the waypoints limit. |
| `path(TweenPath path)` | How the tween goes through its points. `null` ignores the waypoints and goes straight to the target. |

| Path (`TweenPaths`) | Route |
| --- | --- |
| `TweenPaths.catmullRom` (default of `to` / `from`) | Smooth Catmull-Rom spline through the start, every waypoint and the target. |
| `TweenPaths.linear` | Straight segments between the points. |

The eased progress (see [Easing](easing.md#how-the-eased-progress-is-used)) is spread evenly over the segments, whatever their length. With `targetRelative(...)`, the waypoints are relative to the start values too. A custom path implements `TweenPath`: `float compute(float t, float[] points, int pointsCnt)` returns the value at progress `t`, where `points[0]` is the start value and `points[pointsCnt - 1]` the target; it is called once per attribute.

## Limits

| Limit | Default | Setter | When exceeded |
| --- | --- | --- | --- |
| Combined attributes (values per tween) | `3` | `Tween.setCombinedAttributesLimit(int)` | `RuntimeException("You cannot combine more than 3 attributes in a tween. You can raise this limit with Tween.setCombinedAttributesLimit(), which should be called once in application initialization code.")` |
| Waypoints per tween | `0` | `Tween.setWaypointsLimit(int)` | `RuntimeException("You cannot add more than 0 waypoints to a tween. You can raise this limit with Tween.setWaypointsLimit(), which should be called once in application initialization code.")` |

Set both limits once, before creating tweens.

## Pooling

`Tween` and `Timeline` objects come from pools: the factories reuse freed objects, and a `TweenManager` frees finished objects automatically. `free()` resets an object and returns it to its pool (a timeline frees its children too); `Tween.getPoolSize()` / `Timeline.getPoolSize()` count the free objects, and `ensurePoolCapacity(int)` reserves room.

## Reference

### TweenManager

| Method | Description |
| --- | --- |
| `add(BaseTween<?> object)` | Adds the object (once) and starts it, unless its auto start is disabled. Returns the manager. `object.start(manager)` does the same and returns the object. |
| `update(float delta)` | Removes the finished objects, then advances every object by `delta`. A negative `delta` plays them backward. While the manager is paused, only the removal happens. |
| `pause()`, `resume()` | Freezes and resumes every object of the manager. |
| `killAll()` | Kills every object. |
| `killTarget(Object target)`, `killTarget(Object target, int tweenType)` | Kills the tweens of `target` (of this tween type), inside timelines too; a timeline that contains one is killed whole. |
| `containsTarget(Object target)`, `containsTarget(Object target, int tweenType)` | Whether an object of the manager (or a timeline's child) animates `target`. |
| `size()`, `getRunningTweensCount()`, `getRunningTimelinesCount()` | Number of root objects; number of tweens / timelines, nested ones included. |
| `getObjects()` | Read-only copy of the objects. |
| `ensureCapacity(int minCapacity)` | Reserves room in the internal list. |
| `static setAutoStart(BaseTween<?> object, boolean value)`, `static setAutoRemove(BaseTween<?> object, boolean value)` | Whether `add(...)` starts the object, and whether the manager removes and frees it once finished (both `true` by default). |

### BaseTween

Common base of `Tween` and `Timeline`. The configuring methods return the object for chaining.

| Method | Description |
| --- | --- |
| `start()`, `start(TweenManager manager)` | Builds and starts the object; with a manager, adds it to the manager. |
| `build()` | Prepares the object (resolves the accessor of a tween, computes the durations of a timeline). Called by `start()`. |
| `update(float delta)` | Advances the object by `delta` (negative: backward). |
| `delay(float)`, `repeat(int, float)`, `repeatYoyo(int, float)` | See [Delays, repeat and yoyo](#delays-repeat-and-yoyo). |
| `setCallback(TweenCallback)`, `setCallbackTriggers(int)`, `addCallback(int, Consumer<BaseTween<?>>)` | See [Callbacks](#callbacks). |
| `setUserData(Object)`, `getUserData()` | Any object you attach. Default `null`. |
| `pause()`, `resume()`, `isPaused()` | Freezes this object only. |
| `kill()` | Marks the object finished; its manager removes it at the next update. |
| `free()` | Returns the object to its pool. |
| `forceToEnd(float time)` | Puts the object in its final state. Used by timelines. |
| `isStarted()`, `isInitialized()`, `isFinished()` | Started; begun after its delay; finished or killed. |
| `isYoyo()`, `getDelay()`, `getDuration()`, `getRepeatCount()`, `getRepeatDelay()`, `getFullDuration()`, `getCurrentTime()`, `getStep()` | Timing. `getStep()` counts the plays: even values are plays (`0` is the first), odd values the delays between them. |

### Tween

| Method | Description |
| --- | --- |
| `static to(...)`, `from(...)`, `set(...)`, `call(...)`, `mark()` | See [Creating tweens](#creating-tweens). |
| `static INFINITY` | `-1`, infinite repetitions. |
| `static registerAccessor(Class<?>, TweenAccessor<?>)`, `getRegisteredAccessor(Class<?>)` | Accessor registry. |
| `static setCombinedAttributesLimit(int)`, `setWaypointsLimit(int)` | [Limits](#limits). |
| `static getPoolSize()`, `ensurePoolCapacity(int)` | [Pooling](#pooling). |
| `static getVersion()` | `"6.3.3"`. |
| `ease(TweenEquation)` | Easing equation. See [Easing](easing.md). |
| `target(...)`, `targetRelative(...)`, `waypoint(...)`, `path(TweenPath)`, `cast(Class<?>)` | Targets, path and accessor lookup. |
| `getTarget()`, `getType()`, `getTargetClass()`, `getAccessor()`, `getEasing()`, `getTargetValues()`, `getCombinedAttributesCount()` | Target, tween type (`-1` for `call` / `mark`), lookup class, resolved accessor (`null` before `build()`), equation, target values, attribute count (`0` before `build()`). |

## Pitfalls

- A finished tween or timeline goes back to its pool and is reused: never keep a reference to it after it finished in a manager, and never `free()` an object that is still in a manager or a timeline.
- `Tween.setWaypointsLimit(...)` and `setCombinedAttributesLimit(...)` resize the buffers of new tweens: call them once at startup, before any tween exists.
- A tween that writes a node property through its value setter replaces the signal or lambda given to that setter before.
- Nothing moves without `manager.update(...)` every frame.

## See also

- [TweenAnimator](tween-animator.md)
- [Easing](easing.md)
- [The UI Class](../ui/ui-class.md)
- [Node Fundamentals](../nodes/node-fundamentals.md)