# Utilities

The `dev.joid.lib.utils` packages hold small helper types used across the API. `info` below is a `TextInfo` (see [Text and Fonts](../concepts/text.md)).

## Align

`Align` is the alignment of node anchors, text and layouts: `START` (left or top), `CENTER` or `END` (right or bottom).

```java
TextNode.create(960, 540).text(Text.create("Title", this.info, Align.CENTER)).anchor(Align.CENTER).attach(this);
```

Test a value with `isStart()` (or `isLeft()`), `isCenter()`, `isEnd()` (or `isRight()`) and `is(Align)`.

## Bezier

`Bezier` evaluates a Bézier curve at `t` from 0 (start) to 1 (end).

```java
final Vector2d point = Bezier.cubic(0.5D, new Vector2d(0D, 0D), new Vector2d(0D, 100D), new Vector2d(200D, 100D), new Vector2d(200D, 0D));
```

![A cubic curve with its two control points and its point at t = 0.5](../images/utilities-bezier.png "t = 0.5 gives (100, 50)")

| Method | Description |
| --- | --- |
| `quadratic(double t, Vector2d start, Vector2d end, Vector2d control)` | Point of the quadratic curve. |
| `cubic(double t, Vector2d start, Vector2d startControl, Vector2d end, Vector2d endControl)` | Point of the cubic curve. |

## BoundingBox

`BoundingBox` is a mutable rectangle stored as minimum and maximum corners.

```java
final BoundingBox box = BoundingBox.create(10D, 20D, 30D, 40D).expand(5D);
final double width = box.getWidth();
```

| Method | Description |
| --- | --- |
| `create(double x, double y, double width, double height)` | A box from `(x, y)` to `(x + width, y + height)`. |
| `getMinX()`, `getMinY()`, `getMaxX()`, `getMaxY()`, setters | The corners. |
| `getWidth()`, `getHeight()`, `copy()` | Size, independent copy. |
| `expand(double)`, `contract(double)` | Grows or shrinks every side; returns the box. |

## Pair and Tuple

`Tuple<F, S>` holds two immutable values; `Pair<T>` is a `Tuple<T, T>`. Read them with `getFirst()` and `getSecond()`.

```java
final Tuple<String, Integer> entry = new Tuple<>("width", 42);
final Pair<Double> range = new Pair<>(0D, 1D);
final double max = range.getSecond();
```

## FormatUtils

`FormatUtils.formatNumber(long value)` shortens a number with a suffix (`k`, `M`, `B`, `T`, `P`, `E`), with one truncated decimal below 10 of a unit.

| Value | Result |
| --- | --- |
| `999` | `999` |
| `1500` | `1.5k` |
| `12345` | `12k` |
| `2500000000` | `2.5B` |

## ImageUtils and PixelLayout

`ImageUtils.read(InputStream, ImageReaderSpi)` reads the first image of a stream without closing it. `ImageUtils.bleedAlpha(int[] pixels, int width, int height)` colors fully transparent pixels like their visible neighbors, against dark fringes under linear filtering.

`PixelLayout` (`RGBA8`, `BGRA8`) converts ARGB `int` pixels to and from graphics API bytes. `read` takes `bottomUp` when the first row is the bottom one, as OpenGL reads it back.

```java
final ByteBuffer bytes = PixelLayout.RGBA8.write(pixels, ByteBuffer.allocateDirect(pixels.length * 4));
final int[] argb = PixelLayout.RGBA8.read(bytes, width, height, false);
```

## ThreadUtils

`ThreadUtils` creates daemon threads and runs work on the render thread.

```java
final ExecutorService executor = Executors.newFixedThreadPool(4, ThreadUtils.daemonFactory("MyLoader"));
ThreadUtils.runOnRenderThread(() -> this.label.text(Text.create("Loaded", this.info)));
```

| Method | Description |
| --- | --- |
| `daemonFactory(String name)` | A factory of daemon threads named `name/1`, `name/2`, and so on. |
| `daemonThread(Runnable task, String name)` | A daemon thread running `task`, not started. |
| `runOnRenderThread(Runnable task)` | Runs `task` at once from the render thread, otherwise queues it there. |

## Platform

`Platform.current()` returns `WINDOWS`, `MACOS` or `LINUX` (any other system); `Platform.is64Bit()` tells the architecture.

```java
final String copy = Platform.current() == Platform.MACOS ? "Cmd+C" : "Ctrl+C";
```

## IIndexedList

These lists keep their elements sorted by `getIndex()`: `UI.getNodeList()`, `Node.getChildren()` (by `zindex`), `IUIBridge.getUiList()`.

```java
for (final Node node : super.getNodeList().recursive()) {
	System.out.println(node.getHierarchy());
}
```

![Diagram of an IIndexedList sorted by index while elements are added](../images/diagram-indexed-list.png "add inserts after the elements of the same index")

| Method | Description |
| --- | --- |
| `add(E)`, `remove(E)`, `clear()` | Inserts after the elements of the same index; an element added again moves only when out of order. |
| `sort()` | Sorts again after index changes, keeping the order of equal indexes. |
| `size()`, `isEmpty()`, `contains(E)`, `get(int)` | Size, membership, element at a position. |
| `getFirst()`, `getLast()` | Lowest or highest; `null` when empty. |
| `ordered()`, `reversed()` | Live views in ascending or descending order. |
| `recursive()` | A flat list of every element and its descendants, depth first. |
| `copy()` | An independent list. |

`IndexedLinkedList` is not thread-safe; `IndexedConcurrentList` iterates on a snapshot.

## Good to know

- Changing the value behind `getIndex()` does not move an element: call `add` again or `sort()`. `Node.zindex(...)` does it for you.
- `Tuple` and `Pair` compare by identity: do not use them as map keys for their values.

## See also

- [Drawing](../drawing/drawing.md)
- [Nodes](../concepts/nodes.md)
- [Bridges and Backends](../integration/backends.md)
- [Changelog 8.0.0](../changelog/8.0.0.md)