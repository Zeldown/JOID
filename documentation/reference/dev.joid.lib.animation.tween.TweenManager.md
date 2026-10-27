---
summary: Holds tweens and timelines and advances them together when you update it.
---

Use one when code of yours already runs at each frame, for example `UI.update()`: call `update(...)` there with the time elapsed since the previous frame, in ms.
A [[TweenAnimator]] needs no manager.
Its methods can be called from several threads.
The UI that owns the manager advances it in its own `update()`:

```java file="com/example/style/TweenUI.java" region="update"
```

Learn: [Animation](/use/learn/style-and-motion/animation/).

## TweenManager()

Creates an empty manager that is not paused.

## add(BaseTween)

Adds `object` once and starts it, then returns this manager; adding it again starts it again.

## killAll()

Kills every tween and timeline held, then returns this manager.

## pause()

Stops the updates from advancing anything, then returns this manager.

## resume()

Lets the updates advance again, then returns this manager.

## update(float)

Removes the finished objects, then advances the others by `delta` ms, backward and in reverse order when `delta` is negative.

## size()

Returns the number of objects held.

## getRunningTweensCount()

Returns the number of tweens held, those inside timelines included.

## getRunningTimelinesCount()

Returns the number of timelines held, the nested ones included.

## getObjects()

Returns a read-only copy of the objects held.
