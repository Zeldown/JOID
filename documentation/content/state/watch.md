# Watching Signals

`Node.watch(...)` subscribes a node to a [signal](signals.md): each time the signal publishes, the node reloads, rebuilds its children or runs your code. Use it to keep a part of the tree in sync with your state without rebuilding the whole UI.

## Watching a signal

```java
public class ProfileUI extends UI {

    private final StringSignal name = new StringSignal("Guest");

    @Override
    public void init() {
        TextNode
        .create(100, 100)
        .text(Text.create("", info))
        .<TextNode>onInit(node -> node.getText().text("Hello " + this.name.getOrDefault()))
        .watch(this.name)
        .attach(this);

        RectNode
        .create(100, 160, 200, 60)
        .color(Color.WHITE)
        .onClick((node, mouseX, mouseY, clickType) -> this.name.set("Alex"))
        .attach(this);
    }

}
```

`watch(signal)` reloads the node on every publish: `reload()` loads the node and its children again, so their `onInit` callbacks and `init(ui)` hooks run with the new value. `info` is a `TextInfo` (see [Text Model](../text/text-and-textinfo.md)).

## Choosing the reaction with WatchProperty

`WatchProperty` (`dev.joid.lib.ui.node.property.watch`) says what the node does when the signal publishes. `watch(signal, properties...)` applies the given values in order; an exception thrown by one is printed and the next ones still run.

| Value | Effect |
|---|---|
| `RELOAD` | Calls `reload()`: the children and the node are loaded again, and their `onInit` callbacks fire inside `onReload`. The children stay. Default of `watch(signal)`. |
| `BODY` | Runs again the consumer given to `body(...)`, with the node. Does nothing for a node without a body. |
| `CLEAR_CHILDREN` | Calls `clearChildren()`: every child is detached (`onDetach`), removed and loses its parent. |
| `NONE` | Does nothing; react in `onWatch`. |

`apply(Node node)` applies one value to a node directly.

### Rebuilding children with CLEAR_CHILDREN and BODY

`BODY` alone appends a new set of children next to the old ones; clear them first:

```java
final ListSignal<String> items = new ListSignal<>(new ArrayList<>());

FlexNode
.vertical(100, 100, 400)
.watch(items, WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY)
.body(flex -> {
    for (final String item : items.getOrDefault()) {
        TextNode.create(0, 0).text(Text.create(item, info)).attach(flex);
    }
})
.attach(this);

items.add("Sword");
```

`body(...)` runs its consumer immediately and keeps it, so the order of `watch` and `body` in the chain does not matter. Children appended to a node already in a UI are loaded at once.

### Updating in place with onWatch

`onWatch((node, signal, properties) -> ...)` fires on every publish, after the properties are applied. With `NONE`, it updates the existing nodes without rebuilding them:

```java
final StringSignal title = new StringSignal("Loading");

TextNode
.create(100, 100)
.text(Text.create(title.getOrDefault(), info))
.watch(title, WatchProperty.NONE)
.<TextNode>onWatch((node, signal, properties) -> node.getText().text(title.getOrDefault()))
.attach(this);
```

`signal` is the signal that published and `properties` the values given to `watch`. Overriding the `pre` method of `NodeWatchCallback` and cancelling its context skips the properties and the POST phase for that publish (see [Callbacks](../interactions/callbacks.md#pre-and-post-phases)).

## watch overloads

| Method | Description |
|---|---|
| `watch(Signal<?> signal)` | Same as `watch(signal, WatchProperty.RELOAD)`. |
| `watch(Signal<?> signal, WatchProperty... properties)` | Same as `watch(signal, condition, properties)` with a condition that is `true` while the node's UI is open (`JOID.isOpen(ui)`). |
| `watch(Signal<?> signal, Supplier<Boolean> condition, WatchProperty... properties)` | Watches with your own condition. |

Each call adds one subscription: a node can watch several signals, and watching the same signal twice applies its properties twice.

## Conditions and lifetime of a watch

A watch follows the attachment of its node. `onDetach()` (`remove(...)`, `clearChildren()`, an `append` that moves the node, `WatchProperty.CLEAR_CHILDREN`, `UI.reload()`, the UI closing) unsubscribes the node and its whole subtree at once, so a detached node is never reloaded or rebuilt by its signals. Loading it again (`append`, `attach`, reopening its UI) subscribes it again, exactly once; when the signal changed while the node was detached, the watch is applied once right away.

On every publish of the signal, the subscription of the node:

1. Does nothing when the node is not in a UI yet. It stays subscribed only when a UI is running its `init()` at that moment (`UI.getCurrent()` is not `null`); otherwise it unsubscribes.
2. Fires `onWatch` with the properties as its default action.
3. Evaluates the condition. When it returns `false`, the node stops watching the signal.

The condition is evaluated after the properties are applied: the publish that ends a watch still updates the node. Closing the UI detaches its nodes, which stops their watches before the default condition is even evaluated.

> NOTE: A subscription keeps its node in memory as long as the signal is reachable and the subscription is active. Detaching the node removes it; a node built but never attached stays subscribed until the next publish outside a UI's `init()`. A condition that never returns `false` (such as `() -> true`) keeps an attached node subscribed until it is detached.

### Custom conditions

A custom condition decides when the watch ends; once it returns `false`, the node does not subscribe again by itself. A condition that is always `false` reacts to the next publish only:

```java
final StringSignal motd = new StringSignal("Loading");

TextNode
.create(100, 100)
.text(Text.create("", info))
.<TextNode>onInit(node -> node.getText().text(motd.getOrDefault()))
.watch(motd, () -> false, WatchProperty.RELOAD)
.attach(this);
```

### JOID.isOpen

The default condition relies on `JOID.isOpen` (`dev.joid.internal.JOID`), which you can also use in your own conditions.

| Method | Description |
|---|---|
| `JOID.isOpen(UI ui)` | `true` while `ui` is in the list of its UI bridge. |
| `JOID.isOpen(Class<? extends UI> uiClass)` | `true` while an instance of `uiClass` (or of a subclass) is open in the UI bridge that handles that class. |

See [Opening and Closing UIs](../ui/managing-uis.md) for the other `JOID` methods.

## Waiting for a signal with wait and onMount

`wait(ISignal<?> signal)` keeps a node unmounted until the signal has a value (`isPresent()`). An unmounted node draws its skeleton instead of itself (see [Node Fundamentals](../nodes/node-fundamentals.md) for `wait` and `skeleton`). `onMount` fires on the first frame the node is drawn mounted:

```java
final ListSignal<String> lines = new ListSignal<>();

ContainerNode
.create(100, 100, 400, 200)
.body(container -> {
    TextNode.create(10, 10).text(Text.create("", info)).attach(container);
    TextNode.create(10, 60).text(Text.create("", info)).attach(container);
})
.wait(lines)
.onMount(container -> {
    for (int i = 0; i < lines.size(); i++) {
        container.getChild(i, TextNode.class).getText().text(lines.get(i));
    }
})
.attach(this);

this.schedule(() -> lines.set(Arrays.asList("First line", "Second line")), 2000L);
```

- A node is mounted when all its `wait(...)` conditions (`wait(ISignal<?>)`, `wait(long, TimeUnit)`, `wait(Predicate<T>)`) are met and its parent is mounted.
- Mounting is checked on every frame the node is visible, so `onMount` fires only for a visible node.
- `onMount` fires again each time the node becomes mounted after being unmounted, for example when the signal is reset to an empty value and set again.

## See also

- [Signals](signals.md)
- [Callbacks](../interactions/callbacks.md)
- [Node Fundamentals](../nodes/node-fundamentals.md)
- [Opening and Closing UIs](../ui/managing-uis.md)