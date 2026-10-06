# Callbacks

Every interactive behaviour on a `Node` is a callback. Attach via fluent setters; handle via a lambda matching the callback's `apply(...)` signature.

## State lifecycle

```java
node.onInit((n) -> { });                                                          // NodeInitCallback
node.onRender((n, mouseX, mouseY) -> { });                                        // NodeRenderCallback
node.onDraw((n, mouseX, mouseY) -> { });                                          // NodeDrawCallback
node.onUpdate((n) -> { });                                                        // NodeUpdateCallback
node.onReload((n) -> { });                                                        // NodeReloadCallback
node.onAppend((n, child) -> { });                                                 // NodeAppendCallback
node.onDetach((n) -> { });                                                        // NodeDetachCallback
node.onMount((n) -> { });                                                         // NodeMountCallback
```

`onInit` fires each time the node is loaded into a UI: when it is attached, and again on `reload()` and `WatchProperty.RELOAD`. `onMount` fires on the first frame the node renders. `onDetach` fires when the node is removed from its parent (via `clearChildren()` or UI close).

## Mouse

```java
node.onMousePressed((n, mouseX, mouseY, clickType) -> { });                       // NodeMousePressedCallback
node.onMouseReleased((n, mouseX, mouseY, clickType) -> { });                      // NodeMouseReleasedCallback
node.onMouseDragged((n, mouseX, mouseY, clickType, deltaTime) -> { });            // NodeMouseDraggedCallback
node.onMouseScroll((n, mouseX, mouseY, value) -> { });                            // NodeMouseScrollCallback
node.onClick((n, mouseX, mouseY, clickType) -> { });                              // NodeMousePressedCallback, only for a press on the node
```

## Keyboard

```java
node.onKeyPressed((n, character, key) -> { });                                    // NodeKeyPressedCallback
```

## Scroll (overflow = SCROLL)

```java
node.onScrollUpdate((n, value) -> { });                                           // NodeScrollUpdateCallback
node.onScrollEnd((n, scrollX, scrollY) -> { });                                   // NodeScrollEndCallback
node.onScrollEnding((n, scrollX, scrollY) -> { });                                // NodeScrollEndingCallback
```

`onScrollUpdate` fires on each scroll tick with the scroll offset it aims at; `onScrollEnd` fires once the scroll comes to rest at the end of the content, with the final `(scrollX, scrollY)` offsets. Leaving the end before the scroll rests cancels it. `onScrollEnding` fires as soon as the scroll aims at the end, before it rests, with the offsets it heads to: append the next items of an infinite list there, so they are in place before the scroll stops.

## Drag

```java
node.onDrag((n) -> { });                                                          // NodeDragCallback
node.onSnap((n, snapNode) -> { });                                                // NodeSnapCallback
```

`onDrag` fires on each mouse drag event while the node is being dragged. `onSnap` fires when the dragged node is released, onto the nearest registered snap target (`NEAREST`, whatever the distance) or the one it overlaps (`OVERLAP`).

## Hover

```java
node.onHoverStart((n, mouseX, mouseY) -> { });                                    // NodeHoverStartCallback
node.onHoverEnd((n, mouseX, mouseY) -> { });                                      // NodeHoverEndCallback
node.onHover((n, mouseX, mouseY) -> { });                                         // NodeHoverCallback
```

`onHoverStart` fires the frame the mouse enters the node; `onHoverEnd` fires the frame it leaves; `onHover` fires every frame while the mouse is over the node. All three are wired into the same hover state machine that drives `hoverValue`, so they stay in sync with the hover fade.

## Animation

```java
node.onAnimate((n, animator, value) -> { });                                      // NodeAnimationCallback
```

Fires when a tween animator attached to the node publishes a new value. You get the current `TweenAnimator` and its `float` value.

## Signals

```java
node.onWatch((n, signal, properties) -> { });                                     // NodeWatchCallback
```

Fires whenever a watched signal triggers. `properties` is the `WatchProperty[]` vararg that was passed to `.watch(...)`.

## The `context` parameter (PRE/POST phases)

The lambdas shown above hit the **POST** phase of each callback. If you need to run logic before the default behaviour, implement the callback interface directly — the `pre` / `post` methods both receive an `InternalContext`:

```java
final NodeMousePressedCallback<RectNode> myHandler = new NodeMousePressedCallback<RectNode>() {

    @Override
    public void pre(RectNode node, InternalContext context, double mouseX, double mouseY, ClickType clickType) {
        if (!shouldReact()) context.cancel();
    }

    @Override
    public void apply(RectNode node, double mouseX, double mouseY, ClickType clickType) {
        // POST phase — skipped when context was cancelled in pre()
    }
};
```

`InternalContext` has `cancel()`, `cancel(Runnable)`, and `isCancelled()`. Default behaviour (node-level effects like flipping `CheckboxNode.checked`) runs between `pre` and `post`.

Every callback added to a node runs, in the order it was added. A callback that handles the event — the default `post`, or `context.cancel()` — stops it for the other nodes, never for the other callbacks of the same node: `onClick` and `onMousePressed` on one node both fire.

## `ClickType`

```java
ClickType.LEFT
ClickType.RIGHT
ClickType.MIDDLE
ClickType.BACK
ClickType.FORWARD
ClickType.OTHER
```

`ClickType.from(int button)` maps a mouse button index to one of the above (`0 → LEFT`, `1 → RIGHT`, `2 → MIDDLE`, `3 → BACK`, `4 → FORWARD`, anything else → `OTHER`).

## Examples

### Double-click

```java
final long[] lastClick = { 0L };

node.onClick((n, mx, my, ct) -> {
    final long now = System.currentTimeMillis();
    if (now - lastClick[0] < 300L) { /* double click */ }
    lastClick[0] = now;
});
```

### Right-click menu

```java
node.onClick((n, mx, my, ct) -> {
    if (ct == ClickType.RIGHT) openContextMenu(mx, my);
});
```

### Release resources on detach

```java
class MyAssetNode extends Node {
    private final Thread worker;

    public MyAssetNode() {
        this.worker = new Thread(this::doWork);
        this.worker.start();
        onDetach(n -> this.worker.interrupt());
    }
}
```

### Scroll position logging

```java
FlexNode.vertical(0, 0, 400)
    .overflow(OverflowProperty.SCROLL)
    .body(list -> { /* content */ })
    .onScrollEnd((n, sx, sy) -> System.out.println("Scrolled to " + sx + ", " + sy))
    .attach(parent);
```

## Best practices

- **Keep callbacks fast.** They run on the render thread; heavy work blocks the frame.
- **Use `onDetach` for cleanup.** Threads, sockets, native resources — release them here or leak.
- **Don't mutate the tree from `onUpdate`.** Use signals instead — adding/removing nodes during iteration is unsafe.
- **Prefer `onClick` over `onMousePressed`** unless you need press + release semantics.

## See also

- [Node Fundamentals](../nodes/node-fundamentals.md) — full callback list.
- [Hover](hover.md) — hover state queries.
- [Drag & Drop](drag-drop.md) — drag callbacks and snap targets.