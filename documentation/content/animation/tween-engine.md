# Tween Engine

The tween engine (`dev.joid.lib.animation.tweenengine`) animates any `float` attributes of any object: tweens interpolate up to three attributes at once, timelines chain and group them, and a `TweenManager` updates everything. [`TweenAnimator`](tween-animator.md) is built on it; use the engine directly to animate your own objects, several attributes together, or along a path. Its API follows the Universal Tween Engine (`Tween.getVersion()` returns `"6.3.3"`).

## A first tween

A tween needs a target object, a way to read and write its attributes (a [`TweenAccessor`](#tween-accessors)) and a manager that you update every frame. `MutableFloat` (`dev.joid.lib.animation.tweenengine.primitive`) is a `float` holder that is its own accessor:

```java
public class UIWelcome extends UI {

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

![A white rectangle fading in](../images/tween-fade.gif "The tween moves opacity from 0 to 1 in 600 ms with CUBIC_OUT.")

- `Tween.to(target, tweenType, duration)` creates a tween that moves the target's attributes from their current values to `target(...)`.
- `start(manager)` adds it to the manager, which starts it.
- `manager.update(delta)` advances every tween of the manager. `UI.getFrameTime()` is the time of the last frame in milliseconds.

The engine has no time unit of its own: durations, delays and update deltas only need to use the same one. JOID uses milliseconds everywhere (`TweenAnimator`, `UI.getFrameTime()`, node hover durations).

## Tween accessors

### Writing a TweenAccessor

A `TweenAccessor<T>` reads and writes the attributes of a class. The `tweenType` (any `int` you choose) selects which attributes:

```java
public final class NodeAccessor implements TweenAccessor<Node> {

    public static final int POSITION = 0;
    public static final int SIZE     = 1;

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
            target.position(newValues[0], newValues[1]);
            break;
        case NodeAccessor.SIZE:
            target.size(newValues[0], newValues[1]);
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

Register the accessor once, at startup (before opening your UIs):

```java
Tween.registerAccessor(Node.class, new NodeAccessor());
```

Then any node can be tweened, here in the `init()` of the UI of [A first tween](#a-first-tween):

```java
final RectNode card = RectNode.create(100, 300, 200, 120).color(Color.WHITE).attach(this);
Tween.to(card, NodeAccessor.POSITION, 500F).target(1200F, 300F).ease(TweenEquations.QUART_OUT).start(this.manager);
```

![A white card sliding to the right and slowing down](../images/tween-card.gif "The card moves from x = 100 to x = 1200 in 500 ms with QUART_OUT (0.5× scale).")

The next examples on this page reuse this `card` and the UI's `manager`.

### How the accessor is found

- `Tween.registerAccessor(Class<?>, TweenAccessor<?>)` registers one accessor per class; registering again replaces it. `Tween.getRegisteredAccessor(Class<?>)` returns it, or `null`.
- A tween looks for an accessor registered for the target's class, then for its superclasses (a `RectNode` uses the `Node` accessor above). Interfaces are not searched.
- A target that implements `TweenAccessor` and has no accessor registered for its own class is its own accessor (`MutableFloat`, `MutableInteger`).
- `cast(Class<?>)` forces the class used for the lookup. It must be called before the tween starts, otherwise it throws a `RuntimeException`.
- When nothing is found, starting (building) the tween throws `RuntimeException("No TweenAccessor was found for the target")`.
- The accessor of [`TweenAnimator`](tween-animator.md#tweening-an-animator-with-tweenanimatoraccessor) is registered automatically.

The registry is a plain static map: register accessors at startup, before tweens run.

### MutableFloat and MutableInteger

Both extend `Number` and implement `TweenAccessor` for themselves, with a single attribute; the tween type is ignored.

| Class | Constructor | Write | Read |
| --- | --- | --- | --- |
| `MutableFloat` | `new MutableFloat(float value)` | `setValue(float)` | `floatValue()`, `doubleValue()`, `intValue()`, `longValue()` |
| `MutableInteger` | `new MutableInteger(int value)` | `setValue(int)` | `intValue()`, `longValue()`, `floatValue()`, `doubleValue()` |

`MutableInteger` truncates the interpolated value to an `int` (`(int) value`) at each update.

## Creating tweens

| Factory | Tween |
| --- | --- |
| `Tween.to(Object target, int tweenType, float duration)` | From the current values to the values given to `target(...)`. |
| `Tween.from(Object target, int tweenType, float duration)` | From the values given to `target(...)` to the current values. |
| `Tween.set(Object target, int tweenType)` | Duration `0`: applies the `target(...)` values at once (at the first update that moves time forward). |
| `Tween.call(TweenCallback callback)` | Duration `0`, no target: runs `callback` with `TweenCallback.START` when reached. Use it to run code at a point of a timeline. |
| `Tween.mark()` | Duration `0`, no target, does nothing. With `delay(...)` it makes a pause; `Timeline.pushPause(...)` uses it. |

- `Tween.to` and `Tween.from` use `TweenEquations.QUAD_INOUT` and the `TweenPaths.catmullRom` path by default; `Tween.set` uses `QUAD_INOUT` and no path.
- A negative duration throws `RuntimeException("Duration can't be negative")`.
- The current values are read when the tween begins (after its delay, see [Delays](#delays-repeat-and-yoyo)), not when it is created. In a sequence, a tween therefore starts from wherever the previous tweens left the target.

### Target values

| Method | Description |
| --- | --- |
| `target(float)`, `target(float, float)`, `target(float, float, float)`, `target(float...)` | Absolute target values, one per attribute, in the order of the accessor. |
| `targetRelative(float)`, `targetRelative(float, float)`, `targetRelative(float, float, float)`, `targetRelative(float...)` | Offsets added to the start values when the tween begins. Called on a tween that has already begun, the offsets are added to its start values at once. |

Passing more values than the combined attributes limit to the varargs forms throws a `RuntimeException`.

```java
Tween.to(card, NodeAccessor.POSITION, 300F).targetRelative(0F, -20F).ease(TweenEquations.BACK_OUT).start(this.manager);
```

## Timelines

A `Timeline` plays tweens and other timelines one after the other (sequence) or together (parallel). It is itself a tween object: it can be delayed, repeated, given callbacks and nested.

```java
Timeline
.createSequence()
.push(Tween.set(card, NodeAccessor.POSITION).target(0F, 0F))
.push(Tween.to(card, NodeAccessor.POSITION, 400F).target(800F, 0F))
.pushPause(200F)
.beginParallel()
.push(Tween.to(card, NodeAccessor.POSITION, 400F).target(800F, 600F))
.push(Tween.to(card, NodeAccessor.SIZE, 400F).target(300F, 300F))
.end()
.push(Tween.call((type, source) -> System.out.println("Arrived")))
.repeatYoyo(1, 500F)
.start(this.manager);
```

![A white card moving right, then down while growing, then playing the whole path backward](../images/tween-timeline.gif "The sequence, then its yoyo repetition played backward after 500 ms (0.4× scale).")

| Method | Description |
| --- | --- |
| `static createSequence()` | New sequence: each child starts when the previous one ends (its delay, duration and repetitions included). Its duration is the sum of its children's. |
| `static createParallel()` | New parallel timeline: all children start together. Its duration is the longest child's. |
| `push(Tween)` | Adds a tween to the timeline being built. |
| `push(Timeline)` | Adds a timeline. Every `begin...()` of the pushed timeline must be closed with `end()`, otherwise it throws a `RuntimeException`. |
| `pushPause(float time)` | Adds an empty step of `time`: a gap in a sequence, a minimum duration in a parallel timeline. |
| `beginSequence()`, `beginParallel()` | Opens a nested sequence / parallel timeline; the following `push...` calls go into it. |
| `end()` | Closes the innermost opened nested timeline. On the root timeline it throws `RuntimeException("Nothing to end...")`. |
| `getChildren()` | Children of the timeline being built (of the innermost opened one while building). Read-only once the timeline is built. |

Rules:

- A timeline is built when it starts: from then on, every `push...`, `begin...` and `end()` throws a `RuntimeException`.
- A child with infinite repetitions cannot go into a timeline: building it throws a `RuntimeException`.
- When a timeline repeats, its children play again from their start values (read during the first play).

## Running tweens with TweenManager

A `TweenManager` holds tweens and timelines and updates them together.

| Method | Description |
| --- | --- |
| `add(BaseTween<?> object)` | Adds the object (once) and starts it, unless its auto start is disabled. Returns the manager. `object.start(manager)` does the same and returns the object. |
| `update(float delta)` | Removes the finished objects (see below), then advances every object by `delta`. A negative `delta` plays them backward. While the manager is paused, only the removal happens. |
| `pause()`, `resume()` | Freezes and resumes every object of the manager. |
| `killAll()` | Kills every object. |
| `killTarget(Object target)`, `killTarget(Object target, int tweenType)` | Kills the tweens of `target` (of this tween type), including inside timelines; a timeline that contains one is killed whole. |
| `containsTarget(Object target)`, `containsTarget(Object target, int tweenType)` | Whether an object of the manager (or a timeline's child) animates `target` (with this tween type). |
| `size()` | Number of objects (tweens and root timelines) in the manager. |
| `getRunningTweensCount()`, `getRunningTimelinesCount()` | Number of tweens / timelines, nested ones included. |
| `getObjects()` | Read-only copy of the objects. |
| `ensureCapacity(int minCapacity)` | Reserves room in the internal list. |
| `static setAutoStart(BaseTween<?> object, boolean value)` | Whether `add(...)` starts the object (default `true`). |
| `static setAutoRemove(BaseTween<?> object, boolean value)` | Whether the manager removes and frees the object once finished (default `true`). |

- A finished (or killed) object is removed at the next `update(...)` and returned to its [pool](#pooling): another `Tween.to(...)` or `Timeline.create...()` can then reuse it. Do not keep using a tween or timeline after it finished, or disable its auto remove and call `free()` yourself.
- `add`, `update`, the `kill...`, `contains...` and counting methods synchronize on an internal lock of the manager. The pools and the accessor registry are not synchronized: create and configure tweens on the thread that updates them.
- Without a manager, call `start()` on the object and `update(delta)` on it yourself; nothing frees it.

## Delays, repeat and yoyo

| Method | Description |
| --- | --- |
| `delay(float delay)` | Waits before the first play. Adds to the current delay when called several times. |
| `repeat(int count, float delay)` | Plays `count` more times (`count + 1` plays in total), waiting `delay` between plays. `Tween.INFINITY` (`-1`) repeats forever. A negative `delay` counts as `0`. |
| `repeatYoyo(int count, float delay)` | Same, but every second play runs backward, back to the start values. |

- `repeat(...)` and `repeatYoyo(...)` throw a `RuntimeException` once the object has started; the last of the two calls wins.
- `getFullDuration()` returns `delay + duration + (repeatDelay + duration) × count`, or `-1` when the repetitions are infinite.

```java
Tween.to(card, NodeAccessor.SIZE, 150F).targetRelative(10F, 10F).repeatYoyo(5, 0F).start(this.manager);
```

## Callbacks

### Events

| Constant | Value | Fired when |
| --- | --- | --- |
| `TweenCallback.BEGIN` | `0x01` | Playback enters the first play forward (after the delay). |
| `TweenCallback.START` | `0x02` | A play starts, forward. Once per play. |
| `TweenCallback.END` | `0x04` | A play ends, forward. Once per play. |
| `TweenCallback.COMPLETE` | `0x08` | The last play ends, forward. |
| `TweenCallback.BACK_BEGIN` | `0x10` | Playing backward (negative delta) re-enters a completed object from its end. |
| `TweenCallback.BACK_START` | `0x20` | A play is entered from its end, backward. |
| `TweenCallback.BACK_END` | `0x40` | A play is left through its start, backward. |
| `TweenCallback.BACK_COMPLETE` | `0x80` | Playing backward reaches the very beginning. |
| `TweenCallback.ANY_FORWARD` | `0x0F` | Any forward event. |
| `TweenCallback.ANY_BACKWARD` | `0xF0` | Any backward event. |
| `TweenCallback.ANY` | `0xFF` | Any event. |

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
| `addCallback(int flags, Consumer<BaseTween<?>> callback)` | Enables `flags` in addition to the current triggers and adds a callback that runs only for those events, with the source object. |

`TweenCallback` is a functional interface with `onEvent(int type, BaseTween<?> source)` and a default `andThen(TweenCallback)` that runs both callbacks in order.

> NOTE: The triggers are shared by every callback of the object. After `addCallback(TweenCallback.START, ...)`, a callback added with `setCallback(...)` also receives `START` events.

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

![A white card rising, crossing the canvas near the top, then descending to the bottom right](../images/tween-waypoints.gif "The Catmull-Rom path passes through both waypoints before reaching the target (0.35× scale).")

| Method | Description |
| --- | --- |
| `waypoint(float)`, `waypoint(float, float)`, `waypoint(float, float, float)`, `waypoint(float...)` | Adds a waypoint, one value per attribute (use the overload matching the accessor's attribute count). Throws a `RuntimeException` beyond the waypoints limit. |
| `path(TweenPath path)` | How the tween goes through its points. `null` ignores the waypoints and goes straight to the target. |

| Path (`TweenPaths`) | Class | Route |
| --- | --- | --- |
| `TweenPaths.catmullRom` (default of `to` / `from`) | `path.CatmullRom` | Smooth Catmull-Rom spline through the start, every waypoint and the target. |
| `TweenPaths.linear` | `path.Linear` | Straight segments between the points. |

- The eased progress (see [Easing](easing.md#how-the-eased-progress-is-used)) is spread evenly over the segments: with two waypoints, each of the three segments takes a third of the progress, whatever its length.
- With `targetRelative(...)`, the waypoints are relative to the start values too.
- A custom path implements `TweenPath`: `float compute(float t, float[] points, int pointsCnt)` returns the value at progress `t`, where `points[0]` is the start value, `points[pointsCnt - 1]` the target and the waypoints are in between. It is called once per attribute.

## Limits

| Limit | Default | Setter | When exceeded |
| --- | --- | --- | --- |
| Combined attributes (values per tween) | `3` | `Tween.setCombinedAttributesLimit(int)` | `RuntimeException`: "You cannot combine more than 3 attributes in a tween. You can raise this limit with Tween.setCombinedAttributesLimit(), which should be called once in application initialization code." |
| Waypoints per tween | `0` | `Tween.setWaypointsLimit(int)` | `RuntimeException`: "You cannot add more than 0 waypoints to a tween. You can raise this limit with Tween.setWaypointsLimit(), which should be called once in application initialization code." |

The combined attributes limit is checked by the varargs `target(...)` / `targetRelative(...)` and when the tween is built, against the count returned by the accessor. Set both limits once, before creating tweens.

## Pooling

`Tween` and `Timeline` objects come from pools: the factories (`Tween.to`, `Timeline.createSequence`...) reuse freed objects, and a `TweenManager` frees finished objects automatically.

| Method | Description |
| --- | --- |
| `free()` | Resets the object and returns it to its pool. A timeline frees its children too. |
| `Tween.getPoolSize()`, `Timeline.getPoolSize()` | Number of free objects waiting in the pool. |
| `Tween.ensurePoolCapacity(int)`, `Timeline.ensurePoolCapacity(int)` | Reserves room in the pool, without creating objects. |

> WARNING: A freed object is reset and reused by the next factory call. Do not keep references to tweens or timelines after they finished in a manager, and do not `free()` an object that is still in a manager or a timeline.

## Reference

### BaseTween

Common base of `Tween` and `Timeline`. The methods that configure the object return it (`Tween` or `Timeline`) for chaining.

| Method | Description |
| --- | --- |
| `start()` | Builds and starts the object; it then moves with `update(...)`. |
| `start(TweenManager manager)` | Adds the object to `manager`, which starts it (unless auto start is disabled). |
| `build()` | Prepares the object (resolves the accessor of a tween, computes the durations of a timeline). Called by `start()`. |
| `update(float delta)` | Advances the object by `delta` (negative: backward). Called by the manager. |
| `delay(float)`, `repeat(int, float)`, `repeatYoyo(int, float)` | See [Delays, repeat and yoyo](#delays-repeat-and-yoyo). |
| `setCallback(TweenCallback)`, `setCallbackTriggers(int)`, `addCallback(int, Consumer<BaseTween<?>>)` | See [Callbacks](#callbacks). |
| `setUserData(Object)`, `getUserData()` | Any object you want to attach. Default `null`. |
| `pause()`, `resume()`, `isPaused()` | Freezes this object only. |
| `kill()` | Marks the object finished; its manager removes it at the next update. |
| `free()` | Returns the object to its pool. |
| `forceToEnd(float time)` | Puts the object in its final state (target values, or start values when the last play is a backward yoyo play). Used by timelines; `time` is the position in the parent. |
| `isStarted()`, `isInitialized()`, `isFinished()` | Started; begun after its delay (start values read); finished or killed. |
| `isYoyo()` | Whether the repetitions are yoyo. |
| `getDelay()`, `getDuration()`, `getRepeatCount()`, `getRepeatDelay()`, `getFullDuration()` | Timing. `getFullDuration()` is `-1` for infinite repetitions. |
| `getCurrentTime()` | Time elapsed in the current play or repeat delay. |
| `getStep()` | Play counter: even values are plays (`0` is the first), odd values the delays between them; `repeatCount × 2 + 1` once finished. |

### Tween

| Method | Description |
| --- | --- |
| `static to(...)`, `from(...)`, `set(...)`, `call(...)`, `mark()` | See [Creating tweens](#creating-tweens). |
| `static INFINITY` | `-1`, infinite repetitions. |
| `static registerAccessor(Class<?>, TweenAccessor<?>)`, `getRegisteredAccessor(Class<?>)` | Accessor registry. |
| `static setCombinedAttributesLimit(int)`, `setWaypointsLimit(int)` | [Limits](#limits). |
| `static getPoolSize()`, `ensurePoolCapacity(int)` | [Pooling](#pooling). |
| `static getVersion()` | `"6.3.3"`. |
| `ease(TweenEquation)` | Easing equation. |
| `target(...)`, `targetRelative(...)` | Target values. |
| `waypoint(...)`, `path(TweenPath)` | [Paths and waypoints](#paths-and-waypoints). |
| `cast(Class<?>)` | Class used to find the accessor. |
| `getTarget()`, `getType()`, `getTargetClass()`, `getAccessor()` | Target, tween type (`-1` for `call` / `mark`), class used for the accessor lookup, resolved accessor (`null` before `build()`). |
| `getEasing()` | The equation. |
| `getTargetValues()` | Target values array (sized to the combined attributes limit). |
| `getCombinedAttributesCount()` | Number of attributes returned by the accessor (`0` before `build()`). |

## See also

- [TweenAnimator](tween-animator.md)
- [Easing](easing.md)
- [The UI Class](../ui/ui-class.md)
- [Node Fundamentals](../nodes/node-fundamentals.md)