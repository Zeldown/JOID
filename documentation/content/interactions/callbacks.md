# Callbacks

Every interactive behavior on a `Node` is a callback. Attach via fluent setters, handle via lambda or method reference.

## Callback categories

### State

Fired during the node's lifecycle:

```java
node.onInit((n, context) -> { });         // first time the node is loaded into a UI
node.onRender((n, context) -> { });       // before each draw
node.onDraw((n, context) -> { });         // during draw (after children below)
node.onUpdate((n, context) -> { });       // each tick
node.onReload((n, context) -> { });       // after reload
node.onAppend((n, context, child) -> { }); // a child was appended
node.onDetach((n, context) -> { });       // node removed — cleanup here
node.onMount((n, context) -> { });        // first frame rendered
```

### Mouse

```java
node.onMousePressed((n, mouseX, mouseY, clickType, context) -> { });
node.onMouseReleased((n, mouseX, mouseY, clickType, context) -> { });
node.onMouseDragged((n, mouseX, mouseY, clickType, deltaTime, context) -> { });
node.onMouseScroll((n, mouseX, mouseY, value, context) -> { });
node.onClick((n, mouseX, mouseY, clickType) -> { });   // shortcut for press
```

### Keyboard

```java
node.onKeyPressed((n, character, keyCode, context) -> { });
```

### Scroll (for nodes with overflow)

```java
node.onScrollUpdate((n, context) -> { });     // scroll position changed
node.onScrollEnd((n, context) -> { });         // scroll animation finished
```

### Drag

```java
node.onDrag((n, context) -> { });          // while dragging
node.onSnap((n, context, snapTarget) -> { }); // snapped onto another node
```

### Animation

```java
node.onAnimation((n, context, property, from, to, progress) -> { });
```

### Signal

```java
node.onWatch((n, signal, properties) -> { });   // fired when any watched signal triggers
node.onMount((n, context) -> { });               // first frame — good place to init external state
```

## The `context` parameter

Most callbacks get an `InternalContext`:

```java
context.cancel(Runnable cleanup);   // defer until after the callback chain
context.cancelled();                 // check if cancelled
```

Used internally to defer effects until the callback phase completes.

## Pre / post phases

Every callback has `PRE` and `POST` phases. The builder API exposes a single consumer that maps to POST by default — this is fine for 99% of use cases.

Advanced: if you need to intercept before the default behavior, implement the callback interface and annotate methods with `@NodeCallbackMethod(Type.PRE)`:

```java
final NodeClickCallback<RectNode> myHandler = new NodeClickCallback<RectNode>() {

    @NodeCallbackMethod(Type.PRE)
    public void before(RectNode n, InternalContext ctx, double mx, double my, ClickType ct) {
        ctx.cancel();   // cancel the default click
    }

    @Override
    public void apply(RectNode n, double mx, double my, ClickType ct) {
        // default - won't run if cancelled in PRE
    }
};
```

## ClickType

```java
ClickType.LEFT
ClickType.RIGHT
ClickType.MIDDLE
ClickType.XBUTTON1
ClickType.XBUTTON2
```

## Examples

### Double-click

```java
final long[] lastClick = { 0L };

node.onClick((n, mx, my, ct) -> {
    final long now = System.currentTimeMillis();
    if (now - lastClick[0] < 300L) {
        // double click
    }
    lastClick[0] = now;
});
```

### Right-click menu

```java
node.onClick((n, mx, my, ct) -> {
    if (ct == ClickType.RIGHT) {
        openContextMenu(mx, my);
    }
});
```

### Release resources on detach

```java
class MyAssetNode extends Node {
    private Thread worker;

    public MyAssetNode() {
        this.worker = new Thread(this::doWork);
        this.worker.start();
        onDetach(n -> this.worker.interrupt());
    }
}
```

### Scroll-based animations

```java
FlexNode.vertical(0, 0, 400).overflow(OverflowProperty.SCROLL).body(list -> {
    // content
}).onScrollEnd((n, ctx) -> {
    System.out.println("Scrolled to " + n.getScrollY());
}).attach(parent);
```

## Best practices

- **Keep callbacks fast.** They run on the render thread; heavy work blocks the frame.
- **Use `onDetach` for cleanup.** Threads, sockets, native resources — release them here or leak.
- **Don't manipulate the tree from `onUpdate`.** Use signals instead — adding/removing nodes during iteration is unsafe.
- **Prefer `onClick` over `onMousePressed`** unless you need press+release semantics.

## See also

- [Node Fundamentals](../nodes/node-fundamentals.md) — full callback list.
- [Hover](hover.md) — hover-specific callbacks.
- [Drag & Drop](drag-drop.md) — drag callbacks.
