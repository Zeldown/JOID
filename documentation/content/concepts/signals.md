# Signals and Reactivity

Your UI shows data that changes: a counter, a list, a setting. JOID keeps that data in signals, values that tell whoever depends on them when they change. You write your setters with plain Java expressions that read signals, and the nodes follow them. The Quick Start counter already did it with `"Clicks: " + this.clicks.get()`; this page explains the model: signals, the setters that follow them, derived values and subscriptions. Controls bound to signals, lists rebuilt from them and saved state come in the Essentials.

## A counter in two lines

```java
private final IntegerSignal clicks = IntegerSignal.of(0);
```

```java
RectNode
.create(100, 100, 200, 60)
.color(Color.GRAY)
.onClick((node, mouseX, mouseY, clickType) -> this.clicks.increment())
.attach(this);

TextNode.create(100, 200).text(Text.create("Clicks: " + this.clicks.get(), this.info)).attach(this);

RectNode.create(100, 260, 0, 20).color(Color.WHITE).width(40D * this.clicks.get()).attach(this);
```

![Five clicks on a gray button count the text up to Clicks: 5 and grow a white bar](../images/ess-state-counter.gif "The text and the width read the signal, so each click shows at once.")

The text and the width are ordinary Java expressions. Because they read `clicks` with `get()`, JOID follows them: each time `clicks` changes, the expressions are computed again and the nodes take the new values. `info` is a `TextInfo`, the style of a text built from a loaded font, as in the `CounterUI` of the Quick Start ([Text](../essentials/text.md) covers it).

## Signals

A signal holds a value. `set(...)` changes it and notifies what depends on it, only when the new value differs from the current one:

```java
final IntegerSignal count = IntegerSignal.of(0);
count.set(5);
count.increment();
System.out.println(count.get());
count.reset();
```

This prints `6`; `reset()` goes back to the default, `0`.

| Signal | Holds | Operations |
| --- | --- | --- |
| `Signal<T>` | Any value: `Signal.of("Guest")` | `get`, `peek`, `set`, `reset` |
| `BooleanSignal` | A `Boolean` | `toggle()` |
| `IntegerSignal`, `LongSignal`, `FloatSignal`, `DoubleSignal` | A number | `increment()`, `decrement()`, `add(v)`, `subtract(v)`... |
| `StringSignal` | A `String` | `append(s)`, `toUpperCase()`... |
| `ListSignal<E>`, `SetSignal<E>`, `MapSignal<K, V>` | A collection | `add`, `remove`, `put`, `clear`... |

`Signal` is in `dev.joid.lib.signal`, the typed signals in `dev.joid.lib.signal.impl.primitive` and `dev.joid.lib.signal.impl.iterable`. `get()` reads the value and is followed; `peek()` reads it without being followed. The collection signals notify after each change they make, so `items.add("Sword")` updates everything that reads `items`.

## Setters that follow signals

Every setter of every node exists twice: one takes a value, the other a `Supplier`. What you pass decides how the node behaves:

| You pass | Example | The node |
| --- | --- | --- |
| A plain value | `color(Color.GRAY)` | Keeps it. |
| An expression that reads signals | `color(this.clicks.get() >= 3 ? Color.WHITE : Color.GRAY)` | Follows it: computed again when one of its signals changes. |
| A signal, `map(...)` or `Signal.from(...)` | `visible(this.music)` | Follows the signal. |
| A lambda | `width(() -> this.animator.getValue() * 300D)` | Reads it every frame. |

![A click sets the signal, the value changes, the expression given to the setter is computed again, and the node applies the new text at its next frame](../images/ess-diagram-signal.png "How a setter follows a signal.")

Nothing is computed while the signals stay the same: a followed property costs a comparison per frame. Keep lambdas for values that change on every frame without a signal, such as an animation or a clock:

```java
final long opened = BridgeHandler.CLOCK.get().currentTimeMillis();
TextNode.create(100, 380).text(Text.create(() -> "Open for " + (BridgeHandler.CLOCK.get().currentTimeMillis() - opened) / 1000L + " s", this.info)).attach(this);
```

## Derived values with map and Signal.from

When you want the derived value as an object, `map(...)` derives a signal from one signal, and `Signal.from(() -> ...)` from any computation that reads signals. Both give a read-only `ComputedSignal` that you can pass to any setter, read with `get()` or `subscribe` to:

```java
private final IntegerSignal price = IntegerSignal.of(12);
private final IntegerSignal quantity = IntegerSignal.of(3);
```

```java
TextNode.create(100, 300).text(Text.create(this.clicks.map(clicks -> "Doubled: " + clicks * 2), this.info)).attach(this);

TextNode.create(100, 340).text(Text.create(Signal.from(() -> "Total: " + this.price.get() * this.quantity.get()), this.info)).attach(this);
```

## Reacting in code with subscribe

Outside nodes, subscribe to a signal. The subscriber returns `true` to stay subscribed:

```java
private final Signal<String> name = Signal.of("Guest");
```

```java
this.name.subscribe(value -> {
	System.out.println("Hello " + value);
	return true;
});

this.name.set("Alex");
```

`Signal.batch(() -> { ... })` groups several `set` calls: subscribers and derived values are notified once, at the end, with the final values.

## Pitfalls

- A local variable combined with a signal in a followed expression, such as a loop index in `this.clicks.get() + index`, cannot be followed: the value stays fixed and a dev warning names the line. Use a field, `map(...)` or `Signal.from(() -> ...)`.
- Changing an object held by a plain `Signal` (a list read with `get()`) notifies nobody. Use the collection signals, or call `publish()` after the change.
- A `ComputedSignal` from `map` or `Signal.from` is read-only: it has no `set`, and it follows its sources.
## See also

- Next: [Styling and Effects](styling.md)
- [Signals](../state/signals.md): every signal type and operation, `ComputedSignal`, `batch`, `silent`, threads.
- [Reactive Properties](../state/reactive-properties.md): exactly what a setter follows, the dev warnings and their fixes.
- [Watching Signals](../state/watch.md): `watch`, `WatchProperty.custom`, `onWatch`, `wait` and `onMount`.
- [Stores](../state/stores.md) and [Persistent UI Properties](../state/properties.md): lookup, lifecycle, persistence.