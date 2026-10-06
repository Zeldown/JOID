# ContainerNode

`ContainerNode` (`dev.joid.lib.ui.node.impl.structure.container`) is an invisible node that groups children. Use it to give a set of nodes a common origin, to clip or scroll them, or to scope a reactive or loading section of a UI without drawing anything.

## Grouping nodes in a ContainerNode

```java
final ContainerNode container = ContainerNode.create(0, 0, 1920, 1080);

container.body(() -> {
    RectNode.create(480, 270, 960, 540).color(Color.DARKGRAY).attach(container);
    RectNode.create(500, 290, 200, 60).color(Color.RED).attach(container);
});

container.attach(this);
```

![A dark gray panel with a red bar in its top-left corner](../../images/container-group.png "The container draws nothing: only its two children are visible.")

The children are placed relative to the container: moving it with `container.x(...)` moves all of them.

## Creating a ContainerNode

| Factory | Description |
| --- | --- |
| `create(double x, double y, double width, double height)` | A container with the given bounds. It is not attached: call `attach(...)`. |
| `create(Node parent)` | A container at (0, 0) with the current size of `parent`, already attached to `parent`. |

`create(Node parent)` reads the parent's size once, at creation; the container does not follow later resizes of the parent. Since the container is already attached, do not call `attach` on it again.

```java
RectNode
.create(10, 10, 400, 280)
.color(new Color(50, 50, 50))
.body(card -> {
    ContainerNode.create(card).body(content -> {
        RectNode.create(10, 10, 50, 50).color(Color.RED).attach(content);
    });
})
.attach(this);
```

## What a ContainerNode draws

Nothing: `draw` and `drawSkeleton` are empty, even while the container waits for data (see [Waiting and skeletons](../node-fundamentals.md#waiting-and-skeletons)). Its children draw normally, or draw their own placeholder while not mounted. Like any node, a container still has bounds: they are used for hover, clipping and scrolling.

## Common uses

| Use | How |
| --- | --- |
| Move or hide a group | `x(...)`, `y(...)`, `visible(...)` on the container apply to the whole group. |
| Clip a group | `overflow(OverflowProperty.HIDDEN)` clips the children to the container's bounds. |
| Scroll a group | `overflow(OverflowProperty.SCROLL)` and an optional `scrollbar(...)` (see [Overflow and Scrolling](overflow-and-scroll.md)). |
| Rebuild a section from a signal | `watch(signal, ...)` on the container, with the children built in `body` (see [Watching Signals](../../state/watch.md)). |
| Show a placeholder while loading | `wait(...)` and `skeleton(...)` on the container, then fill the children in `onMount`. |

A container that rebuilds its children when a signal changes:

```java
final ListSignal<String> names = new ListSignal<>(Arrays.asList("Sword", "Shield", "Bow"));

ContainerNode
.create(100, 100, 400, 300)
.watch(names, WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY)
.body(container -> {
    for (int i = 0; i < names.getOrDefault().size(); i++) {
        RectNode.create(0, i * 40, 400, 30).color(Color.GRAY).attach(container);
    }
})
.attach(this);
```

## Extending ContainerNode

`ContainerNode` is not final and its constructor `ContainerNode(double x, double y, double width, double height)` is `protected`, so you can extend it to build a composite node. `draw` and `drawSkeleton` are `final`: a subclass draws through [layers](../node-fundamentals.md#layers-with-layer) or children. See [Custom Nodes](../custom-nodes.md).

## Reference

| Method | Description |
| --- | --- |
| `create(double x, double y, double width, double height)` | New unattached container. |
| `create(Node parent)` | New container covering `parent`, attached to it. |

Everything else is inherited from [Node](../node-fundamentals.md).

## See also

- [Node Fundamentals](../node-fundamentals.md)
- [FlexNode](flex.md)
- [GridNode](grid.md)
- [Overflow and Scrolling](overflow-and-scroll.md)