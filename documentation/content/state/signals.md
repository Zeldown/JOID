# Signals

`Signal<T>` is JOID's observable container. Set a value, subscribers fire. Bind nodes to signals for reactive UI.

## Basic signal

```java
final Signal<String> name = new Signal<>("world");

name.subscribe(v -> {
    System.out.println("Name: " + v);
    return true;        // return false to auto-unsubscribe
});

name.set("JOID");       // prints: Name: JOID
```

`SignalSubscriber<T>` is a `@FunctionalInterface` with a single method `boolean update(T value)`. Returning `false` from the lambda removes the subscriber automatically after the call.

## Primitive and string variants

Specialized signals avoid boxing:

```java
new IntegerSignal(0);
new LongSignal(0L);
new FloatSignal(0F);
new DoubleSignal(0D);
new BooleanSignal(false);
new StringSignal("");
```

They all extend `Signal<T>` with the matching boxed type. No specialised `.set(primitive)` — use `.set(Integer.valueOf(0))` or the `int` auto-boxing overload provided by `Integer`.

## Iterable signals

```java
new ListSignal<Item>();                                     // List<T> backed
new MapSignal<K, V>();                                       // Map<K, V> backed
new SetSignal<T>();                                          // Set<T> backed
```

Iterable signals wrap the collection and publish on each mutation. `ListSignal`'s surface:

```java
boolean add(E e)
boolean remove(E e)
E remove(int index)
E get(int index)
E set(int index, E element)
int indexOf(E e)
boolean contains(E e)
int size()
boolean isEmpty()
ListSignal<E> clear()
```

Each write calls `publish()` internally.

## Reading

```java
String current = name.getOrDefault();                        // returns value, or the default if null
boolean hasValue = name.isPresent();                          // value != null
```

There is no `signal.get()` — read through `getOrDefault()`.

## Subscribing / unsubscribing

```java
final SignalSubscriber<String> sub = v -> { react(v); return true; };
name.subscribe(sub);
// later
name.unsubscribe(sub);
```

Keep a reference to the lambda if you want to unsubscribe later. Subscribers that return `false` from `update(...)` are removed automatically.

Most of the time you won't manage subscriptions by hand — use `node.watch(signal)` and let the node clean up on detach.

## Silent updates

Skip the next `publish()` with `silent()`:

```java
name.silent().set("loaded from disk");
```

The flag applies only to the immediately-following `set(...)` — it's not sticky. Used by `UIStore` during JSON hydration so stored signals don't trigger reloads on startup.

## Static factories

```java
Signal<T> Signal.of(T defaultValue)
Signal<T> Signal.of(CompletionStage<T> future)
```

The `CompletionStage` variant subscribes to the future and sets the value when it completes.

## Other operations

```java
signal.reset();                                              // set(defaultValue), triggers publish if different
signal.publish();                                            // re-fire subscribers with the current value
signal.getEventSet();                                         // the internal subscriber set
```

## Binding to nodes

```java
final IntegerSignal count = new IntegerSignal(0);

TextNode.create(0, 0)
    .text(Text.create(() -> "Count: " + count.getOrDefault(), info))
    .watch(count)
    .attach(parent);
```

See [Watch](watch.md) for full binding options.

## Computed signals

Combine signals into derived values by subscribing one to the others:

```java
final IntegerSignal a = new IntegerSignal(3);
final IntegerSignal b = new IntegerSignal(4);
final IntegerSignal sum = new IntegerSignal(a.getOrDefault() + b.getOrDefault());

a.subscribe(v -> { sum.set(v + b.getOrDefault()); return true; });
b.subscribe(v -> { sum.set(a.getOrDefault() + v); return true; });
```

There is no built-in `computed(...)` helper — wire one yourself when needed.

## Thread safety

`Signal<T>` uses a plain `HashSet` for its subscriber set and is not internally synchronised — if you publish from multiple threads you're responsible for your own locking. Subscribers fire synchronously on the thread calling `set` / `publish`; UI mutations from worker threads should hop back to the render thread via `ui.schedule(...)`.

## Best practices

- **Store signals on the UI or a shared state holder** — lifecycle matches the owner.
- **Avoid deep signal graphs.** 2–3 hops is fine; beyond that, refactor.
- **Debounce high-frequency sets.** `onMouseDragged` fires every frame; don't `set` a signal per frame if the consumer only needs the final value.

## See also

- [Watch](watch.md) — node-level signal binding.
- [Stores](stores.md) — persistent signals.