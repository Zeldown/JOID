# Signals

A `Signal<T>` holds a value and notifies its subscribers when the value changes. Nodes rebuild themselves from signals with [watch](watch.md), wait for them before showing with `wait`, and some input nodes write their value into one. Typed signals add operations for numbers, strings and collections.

## A first signal

```java
public class CounterUI extends UI {

    private final IntegerSignal count = new IntegerSignal();

    @Override
    public void init() {
        TextNode
        .create(100, 100)
        .text(Text.create("", info))
        .<TextNode>onInit(node -> node.getText().text("Clicked " + this.count.getOrDefault() + " times"))
        .watch(this.count)
        .attach(this);

        RectNode
        .create(100, 160, 200, 60)
        .color(Color.WHITE)
        .onClick((node, mouseX, mouseY, clickType) -> this.count.increment())
        .attach(this);
    }

}
```

Each click increments the signal; the text node watches it and reloads, which runs its `onInit` callback again with the new value. `info` is a `TextInfo` (see [Text Model](../text/text-and-textinfo.md)). `Signal`, `ISignal` and `SignalSubscriber` are in `dev.joid.lib.utils.signal`, the typed signals in `dev.joid.lib.utils.signal.impl.primitive` and `dev.joid.lib.utils.signal.impl.iterable`.

## Value, default and presence

A signal keeps two values: the current value, set by `set`, and a default value, given at construction.

| Creation | Value | Default | `isPresent()` | `getOrDefault()` |
|---|---|---|---|---|
| `new Signal<>()` | `null` | `null` | `false` | `null` |
| `new Signal<>(T defaultValue)` | `null` | `defaultValue` | `false` | `defaultValue` |
| `Signal.of(T value)` | `value` | `null` | `true` when `value` is not `null` | `value` |
| `Signal.of(CompletionStage<T> future)` | `null`, then the result of `future` | `null` | `false` until `future` completes | the result once completed |

| Method | Description |
|---|---|
| `getOrDefault()` | The value when it is not `null`, otherwise the default. |
| `isPresent()` | `true` when the value is not `null`. The default does not count. |
| `set(T value)` | Stores `value` and publishes it when it differs from the previous value. `set(null)` clears the value. |
| `reset()` | `set(default)`: with a non-`null` default the signal holds the default as its value; with a `null` default it becomes empty. |

## Change detection

- `set` compares the new value with the previous value (not with the default) using `equals`; two `null`s are equal. An equal value publishes nothing.
- Because the default is not compared, setting the default on a signal that holds no value publishes it.
- Mutating an object held by a signal publishes nothing, and setting the same instance again publishes nothing either: call `publish()` after the mutation. The typed collection signals do this for you.

| Method | Description |
|---|---|
| `publish()` | Notifies every subscriber with the current value (the value, not the default), changed or not. |
| `silent()` | Skips the next publish, whether it comes from `set` or `publish`. The flag stays armed until a publish consumes it, so a `set` that publishes nothing leaves it for the next one. |

```java
final Signal<String> title = new Signal<>("Untitled");
title.silent().set("Draft");
title.set("Final");
```

The first `set` stores `"Draft"` without notifying anyone; the second notifies `"Final"`.

## Subscribing with SignalSubscriber

`SignalSubscriber<T>` is a functional interface: `boolean update(T value)`. Return `true` to stay subscribed, `false` to be removed after this call.

```java
final StringSignal name = new StringSignal("Guest");

name.subscribe(value -> {
    System.out.println("Hello " + value);
    return true;
});

name.set("Alex");
```

| Method | Description |
|---|---|
| `subscribe(SignalSubscriber<T> subscriber)` | Adds a subscriber. Adding the same instance twice keeps one subscription. |
| `unsubscribe(SignalSubscriber<T> subscriber)` | Removes the subscriber; pass the same instance you subscribed. |
| `getEventSet()` | The live set of subscribers. |

- Subscribers are stored in a set: their notification order is unspecified.
- A publish notifies the subscribers present when it starts; a subscriber added during a publish is notified from the next one.
- Subscribers run synchronously, on the thread that calls `set` or `publish`.

> WARNING: A signal set from another thread (a network callback, the completion of a `CompletableFuture`) runs its subscribers on that thread, including the watches that rebuild nodes. Set it on the thread that draws the UI instead, for example with `ui.schedule(() -> signal.set(value))` (see [The UI Class](../ui/ui-class.md)).

## Values from a CompletableFuture

`Signal.of(CompletionStage<T> future)` creates an empty signal that takes the result of the stage when it completes, and publishes it. A stage that fails leaves the signal empty. Combined with `wait`, a node shows its skeleton until the data arrives:

```java
final Signal<String> motd = Signal.of(CompletableFuture.supplyAsync(() -> "Welcome back"));

TextNode
.create(100, 100, 400, 40)
.text(Text.create("", info))
.wait(motd)
.<TextNode>onMount(node -> node.getText().text(motd.getOrDefault()))
.attach(this);
```

The value is set on the thread that completes the stage (see the warning above). See [Watching Signals](watch.md#waiting-for-a-signal-with-wait-and-onmount) for `wait` and `onMount`.

## Equality

Two signals are equal when they have the same class and their `getOrDefault()` values are equal; `hashCode` follows `getOrDefault()`. A `StringSignal` is never equal to a `Signal<String>`, even with the same value. The typed signals print as `ClassName{value}`, for example `IntegerSignal{3}`.

## Number signals

| Class | Value type | `new X()` default | `new X(v)` | `X.of(v)` |
|---|---|---|---|---|
| `BooleanSignal` | `Boolean` | `false` | default `v` | value `v`, default `false` |
| `IntegerSignal` | `Integer` | `0` | default `v` | value `v`, default `0` |
| `LongSignal` | `Long` | `0L` | default `v` | value `v`, default `0L` |
| `FloatSignal` | `Float` | `0F` | default `v` | value `v`, default `0F` |
| `DoubleSignal` | `Double` | `0D` | default `v` | value `v`, default `0D` |

Every operation reads `getOrDefault()`, computes the result and calls `set`, so it publishes only when the result differs from the stored value (on a signal that holds only its default, the first operation always publishes). The operations return `void`.

| Operation | `BooleanSignal` | `IntegerSignal` | `LongSignal` | `FloatSignal` | `DoubleSignal` |
|---|---|---|---|---|---|
| `toggle()` | Yes | | | | |
| `increment()`, `decrement()` | | Yes | Yes | Yes | Yes |
| `add(v)`, `subtract(v)`, `multiply(v)` | | Yes | Yes | Yes | Yes |
| `divide(v)` | | Integer division | Integer division | Yes | Yes |
| `power(int exponent)` | | Yes | Yes | | |

- `divide(0)` throws an `ArithmeticException` for every number signal, including `FloatSignal` and `DoubleSignal`.
- `IntegerSignal.power` computes with `Math.pow` and casts the result to `int`.
- `LongSignal.power` is exact for exponents of 0 and more (it overflows like `long` multiplication) and truncates toward zero for negative exponents (`2` to the power `-1` gives `0`).

## StringSignal

`new StringSignal()` has a `null` default; `new StringSignal(String v)` uses `v` as the default; `StringSignal.of(String v)` holds `v` as its value.

| Operation | Description |
|---|---|
| `append(String value)`, `concat(String str)` | Adds text at the end. On an empty signal, the value becomes the argument. |
| `replace(char oldChar, char newChar)`, `replace(CharSequence target, CharSequence replacement)` | Same as `String.replace`. |
| `toLowerCase()`, `toUpperCase()`, `trim()` | Same as the `String` methods. |
| `substring(int beginIndex)`, `substring(int beginIndex, int endIndex)` | Keeps a part of the text. |
| `intern()` | Replaces the value by its interned instance, an equal string, so nothing is published when the signal already holds a value. |

Every operation except `append` and `concat` throws a `NullPointerException` when the signal has neither value nor default. Like the number signals, they call `set` and publish only when the text differs from the stored value.

## Collection signals

`ListSignal<E>`, `SetSignal<E>` and `MapSignal<K, V>` wrap a collection, publish after every mutation they perform, and expose read methods.

| Creation | Behavior |
|---|---|
| `new ListSignal<>()`, `new SetSignal<>()`, `new MapSignal<>()` | No value, no default. Reads throw a `NullPointerException` until a mutation creates the collection. |
| `new ListSignal<>(List<E> list)`, `new SetSignal<>(Set<E> set)`, `new MapSignal<>(Map<K, V> map)` | The collection is the default. The first mutation copies it into the value, so the collection you passed is never modified. |
| `new ListSignal<>(Collection<E> values)`, `new SetSignal<>(Collection<E> values)` | The default is a copy of `values` (an `ArrayList` or a `HashSet`). |
| `ListSignal.of(List<E> list)`, `SetSignal.of(Set<E> set)`, `MapSignal.of(Map<K, V> map)` | The collection is the value itself: mutations write into it, so it must be mutable. |

The copy made by the first mutation is an `ArrayList` for lists, a `LinkedHashSet` for sets and a `LinkedHashMap` for maps.

| Class | Mutations (publish every time) | Reads |
|---|---|---|
| `ListSignal<E>` | `add(E e)`, `remove(E e)`, `remove(int index)`, `set(int index, E element)`, `clear()` | `get(int index)`, `indexOf(E e)`, `contains(E e)`, `isEmpty()`, `size()` |
| `SetSignal<E>` | `add(E e)`, `remove(E e)`, `clear()` | `contains(E e)`, `isEmpty()`, `size()` |
| `MapSignal<K, V>` | `put(K key, V value)`, `remove(K key)`, `clear()` | `get(K key)`, `containsKey(K key)`, `keySet()`, `values()`, `entrySet()`, `isEmpty()`, `size()` |

- Mutations return what the `java.util` method returns (`boolean`, the previous element or value); `clear()` returns the signal.
- A mutation publishes even when it changes nothing, such as removing a missing element.
- Changes made through `getOrDefault()`, `keySet()`, `values()` or `entrySet()` go straight to the collection and publish nothing: call `publish()` afterwards.
- As with `List`, `remove(1)` on a `ListSignal<Integer>` removes by index.

```java
final ListSignal<String> cart = new ListSignal<>(new ArrayList<>());

cart.subscribe(items -> {
    System.out.println(items.size() + " items");
    return true;
});

cart.add("Sword");
cart.add("Shield");
cart.remove("Sword");
```

## Deriving a signal from others

Compute a signal from other signals with a subscriber that sets it:

```java
final IntegerSignal price = new IntegerSignal(10);
final IntegerSignal quantity = new IntegerSignal(1);
final IntegerSignal total = new IntegerSignal(10);

final SignalSubscriber<Integer> recompute = value -> {
    total.set(price.getOrDefault() * quantity.getOrDefault());
    return true;
};

price.subscribe(recompute);
quantity.subscribe(recompute);
```

## Signals in the rest of JOID

| API | Behavior | Page |
|---|---|---|
| `Node.watch(Signal<?> signal, ...)` | Reloads or rebuilds the node when the signal publishes. | [Watching Signals](watch.md) |
| `Node.wait(ISignal<?> signal)` | Keeps the node unmounted (skeleton) until the signal has a value. | [Watching Signals](watch.md#waiting-for-a-signal-with-wait-and-onmount) |
| `Node.visible(Signal<?>... signals)` | Shows the node only while every signal's `getOrDefault()` is neither `null` nor `false`: a `BooleanSignal` toggles it. | [Node Fundamentals](../nodes/node-fundamentals.md) |
| `CheckboxNode.signal(Signal<Boolean> signal)`, `ToggleNode.signal(Signal<Boolean> signal)` | Binds the checked state or the side to the signal, both ways. | [CheckboxNode](../nodes/input/checkbox.md), [ToggleNode](../nodes/input/toggle.md) |
| `SliderNode.signal(Signal<O> signal)` | Binds the selected value to the signal, both ways. | [SliderNode](../nodes/input/slider.md) |
| `SwitchNode.signal(Signal<String> signal)` | Binds the name of the current state to the signal, both ways. | [SwitchNode](../nodes/input/switch.md) |
| `SelectorNode.signal(Signal<V> signal)` | Binds the selected value to the signal, both ways. | [SelectorNode](../nodes/input/selector.md) |
| `TextFieldNode.signal(Signal<String> signal)`, `MultilineTextFieldNode.signal(Signal<String> signal)` | Binds the text to the signal, both ways. | [TextFieldNode](../nodes/input/text-field.md#binding-a-signal-with-signal), [MultilineTextFieldNode](../nodes/input/multiline-text-field.md) |
| `IntegerFieldNode.signal(IntegerSignal signal)` | Binds the value to the signal, both ways. | [TextFieldNode](../nodes/input/text-field.md#integerfieldnode) |
| `UI.getZoomLevel()`, `UI.getScaledWidth()`, `UI.getScaledHeight()` | `DoubleSignal`s updated when the view changes. | [View and Scaling](../ui/view-and-scaling.md) |

## ISignal

`ISignal<T>` is the interface of `Signal`: `set`, `reset`, `subscribe`, `unsubscribe`, `silent`, `publish`, `getOrDefault` and `isPresent`. `Node.wait(ISignal<?>)` accepts any implementation; `watch` and `visible` take a `Signal`.

## See also

- [Watching Signals](watch.md)
- [Stores](stores.md)
- [Node Fundamentals](../nodes/node-fundamentals.md)
- [SliderNode](../nodes/input/slider.md)