---
title: "Derived values"
description: "Compute a value from signals once, reuse it everywhere, change several signals at once and run your own code on a change."
genre: learn
sidebar:
  order: 1
---

A total depends on a price and a quantity.
Computing it in every node that shows it repeats the code, and repeats the work.
A derived value computes it once, from its signals, and behaves like a signal of its own.

**You will learn**
- How to transform one signal with `map`
- How to combine several signals with [[Signal#from]]
- Why a derived value is read-only
- How to change several signals at once with `batch`
- How to run your own code when a value changes

## Transform one signal

[[Signal#map]] gives a value computed from one signal by your function:

```java file="com/example/state/DerivedUI.java" region="sources"
```

```java file="com/example/state/DerivedUI.java" region="map"
```

`price.map(value -> "Price: " + value)` is a [[ComputedSignal]].
Like any signal, it is a `Supplier`, so every reactive setter takes it, and so does [[Text#create]].
The function runs again only when `price` changes, never on a frame where nothing changed.

A function given to `map` can hold any Java code: it is the way out for an expression that JOID cannot follow inside a setter ([What JOID follows](/use/learn/interactivity/signals/)).

## Combine several signals

[[Signal#from]] with a lambda gives a value computed from every signal that the lambda reads with `get()`:

```java file="com/example/state/DerivedUI.java" region="from"
```

```java file="com/example/state/DerivedUI.java" region="total"
```

<figure>

![A white card with the grey buttons "Price +1", "Quantity +1" and "Both +1", then the lines "Price: 13", "Quantity: 4", "Total: 52" and "Changes: 3"](/captures/state-derived.webp)

<figcaption>
After "Quantity +1" twice and "Both +1" once: the total followed both signals.
"Changes: 3" counts the calls of its listener, one per click.
</figcaption>
</figure>

JOID records the signals that the lambda reads while it runs, and runs it again only after one of them changed.
A lambda passed to a setter is different: JOID calls it on every frame ([Signals](/use/learn/interactivity/signals/)).

A derived value follows four rules:
- **Lazy**: it computes nothing before its first read, then gives its last result until a signal it read changes.
- **Dynamic**: only the signals read by the last run count.
  In `flag.get() ? a.get() : b.get()`, a change of `b` recomputes nothing while `flag` is `true`.
- **Stopped by equal values**: when it computes a value equal to the previous one (`equals`), nothing that reads it is computed again and its listeners are not called.
- **Chainable**: `total.map(...)` derives from a derived value, as above.

A derived value that nothing reads and nothing listens to is held by no signal, so the garbage collector takes it: you never dispose of it.

:::caution
A derived value changes only when a signal that it reads changes.
A `Signal.from(...)` that reads no signal never changes, and the [dev mode](/use/get-started/dev-tools/) warns about it once: read the signal with `get()`, or pass a lambda to the setter when the value comes from elsewhere.
:::

## A derived value is read-only

A `ComputedSignal` takes its value from its function.
`set(...)` and `reset()` throw an `IllegalStateException` whose message tells you to set the signals it reads instead, and [[Signal#isWritable]] returns `false`.
For the same reason, a control refuses it with an `IllegalArgumentException`: a control writes the value that the user picks, so it needs a signal it can write ([Controls](/use/learn/interactivity/controls/)).

## Change several signals at once

The "Both +1" button changes the price and the quantity in one [[Signal#batch]]:

```java file="com/example/state/DerivedUI.java" region="batch"
```

Inside `batch`, every `set` applies at once, and the listeners wait.
When the batch ends, each listener runs once, with the final values.
A derived value read inside the batch already gives the updated result.
Batches nest: the listeners wait for the end of the outermost one.

Without `batch`, the two increments call the listener of the total twice, the first time with a total that mixes the new price and the old quantity.

## Run your own code on a change

A setter follows a value for a node.
To run code of your own, such as playing a sound or saving a file, listen to the signal with [[Signal#onChange]]:

```java file="com/example/state/DerivedUI.java" region="listen"
```

A listener receives the new value, and [[Signal#removeOnChange]] removes it.
It:
- does not run when you add it, only at the next change;
- runs once the change and the derived values that depend on it are settled, after the batch that contains the change;
- runs on the render thread, even when another thread set the signal ([Tasks and background updates](/use/learn/state/tasks/#set-a-signal-from-another-thread));
- of a derived value, runs only when the derived value itself changes, not each time one of its signals changes;
- receives `null` when the signal holds no value.

When a listener throws, the other listeners still run, and the first exception reaches the code that changed the signal.

To change a signal without calling its listeners, wrap the change in [[Signal#silent]]: `Signal.silent(() -> volume.set(0))` silences every change made inside, and `volume.silent(signal -> signal.set(0))` silences the changes of `volume` only.
The nodes that follow the signal still show the new value.

A listener lives as long as the signal that holds it.
A listener added in `init()` to a signal created in the same `init()` goes away with the UI, but one added to a signal that outlives the UI, such as a signal of a [store](/use/learn/state/stores-and-properties/), keeps running after the UI closes: remove it in [[UI#onClose]].

## Recap

- [[Signal#map]] derives a value from one signal; **`Signal.from(() -> ...)`** derives it from every signal that its lambda reads.
- A **derived value** is lazy, computed again only after a signal that it read changed, and stopped by equal values.
- A lambda given to `Signal.from` or `map` is **tracked**; a lambda given to a setter runs on every frame.
- A **`ComputedSignal`** is read-only: `set` throws, and controls refuse it.
- **`batch`** applies several changes and calls each listener once, with the final values.
- **`onChange`** runs your code after each change, on the render thread; `removeOnChange` stops it, and `silent` skips it.

## Try it

**Free shipping.**
Show "Free shipping" in `DerivedUI` once the total reaches 50.

<details>
<summary>Solution</summary>

Derive the text from the total:

```java fragment
TextNode.create(60, 450).text(Text.create(total.map(value -> value >= 50 ? "Free shipping" : ""), info)).attach(card);
```

</details>

**One change per click.**
A click runs `price.increment(); quantity.increment();` without `batch`, from a price of 12 and a quantity of 1.
Which totals does the listener of `total` receive?

<details>
<summary>Solution</summary>

`13`, then `26`: the first increment gives 13 × 1 and calls the listener, the second gives 13 × 2 and calls it again.
Inside a `batch`, the listener receives `26` once.

</details>
