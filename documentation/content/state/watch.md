# Watch

`watch(signal, properties)` binds a node to a `Signal<T>` so the node responds to changes automatically.

## Basic usage

```java
final StringSignal name = new StringSignal("world");

TextNode.create(0, 0)
    .text(() -> Text.create("Hello, " + name.getOrDefault(), info))
    .watch(name)
    .attach(parent);
```

On `name.set(...)`, the node reloads.

## `WatchProperty`

The second argument controls what happens on a signal change:

| Property | Behavior |
|---|---|
| `RELOAD` (default) | Run the node's `reload()` — re-executes its body/builder |
| `BODY` | Re-run only the `body(consumer)` — cheaper than full reload |
| `CLEAR_CHILDREN` | Removes children; useful when the body rebuilds them |
| `NONE` | Marker only — no automatic action |

```java
node.watch(signal);                                    // RELOAD
node.watch(signal, WatchProperty.BODY);
node.watch(signal, WatchProperty.CLEAR_CHILDREN, WatchProperty.RELOAD);  // multiple props
```

When multiple properties are passed, they execute in order.

## Conditional watches

Gate the watch with a predicate:

```java
node.watch(signal, () -> ZUI.isOpen(this.ui), WatchProperty.RELOAD);
```

The signal fires only when the condition returns `true`. Useful for UIs that shouldn't reload when closed or in the background.

## Multiple signals

Bind one node to multiple signals:

```java
node.watch(signalA);
node.watch(signalB, WatchProperty.BODY);
node.watch(signalC, () -> isReady(), WatchProperty.RELOAD);
```

Each call is independent.

## Using `onWatch` callback

Attach a callback that fires whenever any watch triggers:

```java
node.onWatch((n, signal, properties) -> {
    System.out.println("Signal fired: " + signal);
});
```

## Example — list rebuild on filter change

```java
final StringSignal filter = new StringSignal("");
final ListSignal<Item> items = new ListSignal<>();

FlexNode.vertical(0, 0, 400).margin(8)
    .watch(filter, WatchProperty.BODY)
    .watch(items, WatchProperty.BODY)
    .body(list -> {
        final String f = filter.getOrDefault().toLowerCase();
        for (Item item : items) {
            if (!item.name.toLowerCase().contains(f)) continue;
            renderRow(item, list);
        }
    })
    .attach(parent);

TextFieldNode.create(0, 0, 400, 30)
    .onChange((tf, value) -> filter.set(value))
    .attach(parent);
```

Both signals trigger a body re-run, so the list updates on filter input and on item changes.

## Lifecycle

Watches are automatically unregistered when the node is detached (via `clearChildren` or UI close). You don't need to manually unsubscribe.

## Best practices

- **Prefer `BODY` over `RELOAD` for list-rebuild scenarios** — no need to re-run the full `init()`.
- **Use conditional watches for UIs in multi-UI setups** — prevents wasted reloads when the UI isn't on top.
- **Watch the smallest node possible.** Watching a parent reloads everything; watching a leaf node only reloads that node.

## See also

- [Signals](signals.md).
- [Stores](stores.md) — persistent signals with auto-watch.
