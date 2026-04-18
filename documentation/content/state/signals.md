# Signals

`Signal<T>` is JOID's observable container. Set a value, subscribers fire. Bind nodes to signals for reactive UI.

## Basic signal

```java
final Signal<String> name = new Signal<>("world");

name.subscribe(v -> System.out.println("Name: " + v));

name.set("JOID");  // prints: Name: JOID
```

## Primitive variants

Specialized signals avoid boxing:

```java
new IntegerSignal(0);
new LongSignal(0L);
new FloatSignal(0F);
new DoubleSignal(0D);
new BooleanSignal(false);
new StringSignal("");
```

Each has an idiomatic `.set(primitive)` / `.getOrDefault()` API.

## Iterable signals

```java
new ListSignal<Item>();     // List<T> backed
new MapSignal<K, V>();       // Map backed
new SetSignal<T>();          // Set backed
```

Iterable signals fire when the collection is modified — not just reassigned:

```java
final ListSignal<String> items = new ListSignal<>();
items.add("hello");   // fires subscribers
items.remove(0);      // fires subscribers
```

## Reading

```java
String current = name.getOrDefault();   // null-safe; returns initial value if unset
String raw = name.get();                 // may be null
```

## Subscribing

```java
SignalSubscriber<String> sub = name.subscribe(v -> reactTo(v));
sub.unsubscribe();  // later
```

Most of the time you don't manage subscriptions manually — use `node.watch(signal)` instead.

## Silent updates

Set without firing subscribers (e.g., to avoid cascading updates):

```java
name.setSilent("loaded from disk");
```

Common in `UIStore.load()` to hydrate state without triggering reloads.

## Binding to nodes

```java
final IntegerSignal count = new IntegerSignal(0);

TextNode.create(0, 0)
    .text(() -> Text.create("Count: " + count.getOrDefault(), info))
    .watch(count)
    .attach(parent);
```

See [Watch](watch.md) for full binding options.

## Computed signals

Combine signals into derived values:

```java
final IntegerSignal a = new IntegerSignal(3);
final IntegerSignal b = new IntegerSignal(4);
final IntegerSignal sum = new IntegerSignal(a.getOrDefault() + b.getOrDefault());

a.subscribe(v -> sum.set(v + b.getOrDefault()));
b.subscribe(v -> sum.set(a.getOrDefault() + v));
```

There's no built-in `computed(...)` helper — roll your own when you need one.

## Thread safety

`Signal<T>` is safe to `set` from any thread. Subscribers fire synchronously on the caller's thread — if you `set` from a worker, handlers run there.

For UI updates from a worker thread, schedule back on the render thread:

```java
ui.schedule(() -> node.reload(), 0L, 0L);
```

## Best practices

- **Use primitive signals.** Fewer allocations than `Signal<Integer>`.
- **Store signals on the UI or a shared state holder.** Lifecycle matches the owner.
- **Avoid deep signal graphs.** 2-3 hops is fine; 10 is a redesign signal.
- **Debounce high-frequency sets.** `onMouseDragged` fires every frame; don't set a signal per frame if the consumer only needs the final value.

## See also

- [Watch](watch.md) — node-level signal binding.
- [Stores](stores.md) — persistent signals.
