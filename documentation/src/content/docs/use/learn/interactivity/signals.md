---
title: "Signals"
description: "Hold a value that changes in a signal, and let every node that shows it follow it."
genre: learn
sidebar:
  order: 2
---

A node shows a value, a count, a name or a volume, and must change with it when it changes, without rebuilding the screen.
A signal holds such a value, and the setters of every node follow it.
The counter of [Events](/use/learn/interactivity/events/#react-to-a-click) already used one: this page explains why its text changes.

**You will learn**
- How to hold a changing value in a signal and change it
- How to tell, for any setter call, when JOID computes its value again
- What an expression follows and what freezes it
- How to read a signal without following it
- Which signal type to pick for numbers, text and collections

## Hold a value that changes

Declare the value as a signal, in a field of the UI:

```java file="com/example/interactivity/signals/CounterUI.java" region="signal"
```

An [[IntegerSignal]] holds an `int`, here `0`.
A click changes it with `increment()`:

```java file="com/example/interactivity/signals/CounterUI.java" region="update"
```

To show the value, read it with `get()` inside the value that you give to a setter:

```java file="com/example/interactivity/signals/CounterUI.java" region="follow"
```

<figure>

![A dark grey "+1" button, the text "Clicks: 0", a short dark grey bar and the text "At open: 0"; the pointer clicks the button three times: the count goes up to 3, the bar widens at each click and turns blue at the third, and "At open" stays at 0](/captures/interactivity-counter.webp)

<figcaption>
At each click, the text, the width and the color of the bar follow `clicks`, and nothing else runs again.
</figcaption>
</figure>

`init()` runs once, so the text and the bar are created once.
When `clicks` changes, JOID computes `"Clicks: " + this.clicks.get()` again and gives the result to the text node.

Every change of a signal, through [[Signal#set]] or an operation such as `increment()`, stores the new value: a value equal to the current one (by `equals`) changes nothing and computes nothing again.
[[Signal#reset]] goes back to the value that the signal was created with.

A control, such as a checkbox or a slider, also writes its value into a signal: [Controls](/use/learn/interactivity/controls/#bind-a-control-to-a-signal) shows how.

## Ask: when does JOID compute this value again?

Most property setters of a node exist twice: one overload takes a value, the other takes a `Supplier`, the reactive setter.
What you write in the call decides when JOID computes the value again.
The three bars below set the same property in the three possible ways:

```java file="com/example/interactivity/signals/RereadUI.java" region="forms"
```

<figure>

![Three blue bars: the "Value" bar keeps its width, the "Expression" bar grows at each click on "+1", the "Lambda" bar grows on every frame and starts again every two seconds](/captures/interactivity-reread.webp)

<figcaption>
A plain value never changes, an expression changes after a click, a lambda on every frame.
</figcaption>
</figure>

The lambda reads the [time of JOID](/use/learn/running/testing/#keep-every-run-identical) from [[BridgeHandler]]`.CLOCK`.

| What you pass | Example | JOID computes it again |
|---|---|---|
| A plain value | `width(200D)` | Never: the value is fixed |
| An expression that reads signals with `get()` | `width(40D + clicks.get() * 80D)` | When one of those signals changes |
| A signal, or a value derived from signals | `visible(shown)`, `width(clicks.map(count -> count * 40D))` | When one of its signals changes |
| A lambda or a method reference | `width(() -> ...)`, `width(this::computeWidth)` | On every frame, whatever changed |

A signal is itself a `Supplier`, so every reactive setter accepts it.
A [derived value](/use/learn/state/derived-values/) is one too.
`visible(shown::get)` is a method reference, read on every frame: pass `shown` itself.

The rule follows from the question.
A value that depends on signals is an expression that reads them, or the signal itself.
A value that changes without any signal (the time, the position of the pointer) is a lambda; an [animator](/use/learn/style-and-motion/animation/#animate-a-value) is a signal.
A value that never changes is a plain value.

Most factories (`create(...)`) take plain values: their arguments follow nothing.
The factories of the rounded, border, shadow and blur effects follow the signals that their arguments read, as a setter does: a [[BorderNodeEffect]] created with `BorderNodeEffect.create(on.get() ? accent : element, 1.5F)` changes color with `on`.
So does the factory of [[Text]] ([What JOID follows](#what-joid-follows)).
Calling a setter again replaces what it followed: `width(200D)` after `width(40D + clicks.get() * 80D)` stops following `clicks`.

Some setters take a `Signal` in place of a `Supplier`: the value of a control, the position of a node (`x(...)`, `y(...)`), its scroll.
The user changes these values too, so the node writes them back into the signal: a signal given there is bound both ways ([Controls](/use/learn/interactivity/controls/#bind-a-control-to-a-signal)).

## What JOID follows

An expression passed to a setter is ordinary Java.
JOID follows it by replaying it when one of the signals that it read changes; [How signals work](/use/in-depth/how-signals-work/) explains the mechanism.

The safe form: read each signal with `get()` on a field or on a local variable that holds the signal itself.
For anything else, derive a value with `map(...)` or pass a lambda.
The table below says what happens outside that form; some code cannot be replayed, and then the value stays fixed:

| In the expression | Result |
|---|---|
| `get()` on a signal held by a field or a local variable | Followed |
| `get()` on a signal returned by a getter of a field, such as `this.settings.getMusic().get()` | Followed |
| `get()` on a signal returned by a getter of a local variable, such as `settings.getMusic().get()` | Followed, except after a condition decided by a signal in the same expression: frozen |
| A field of the class that is not a signal, such as `this.bonus` | Read again each time a signal of the expression changes, never on its own |
| A getter of a node, a control or the UI, such as `isChecked()`, `getWidth()` or `super.getViewWidth()` | It returns a field: read again each time a signal of the expression changes, never on its own; on a node held in a local variable, frozen |
| A local variable set before the call, from fields or signals | Followed |
| The variable of a loop inside a text, such as `name + ": " + score.get()` | Followed |
| The variable of a loop combined with a signal in a calculation, or deciding a condition | Frozen |
| A lambda written inside the expression, such as `names.stream().filter(name -> ...)` | Frozen |
| The time, a random number, a call with side effects | Frozen |

To follow a getter, pass a lambda, as [The design canvas](/use/learn/describing-the-ui/design-canvas/#reach-the-edges-of-the-window) does with the view; to follow a control, bind it to a signal ([Controls](/use/learn/interactivity/controls/#bind-a-control-to-a-signal)).

The text of a [[Text]] is followed by itself: `Text.create(text, info)` replays its arguments, so `Text.create("Clicks: " + clicks.get(), info)` follows `clicks` wherever you pass it.
Its [[TextInfo]] is followed too: `Text.create(on.get() ? "On" : "Off", on.get() ? this.bold : this.regular)` switches the text and its style.
Keep such styles in fields: a style held in a local variable, such as one of `init()` used inside a `body(...)`, and chosen by a signal stays fixed, with a warning in dev mode.

This table is about the expression given to the value overload.
A lambda given to the setter itself, or to `map(...)` and `Signal.from(...)`, is not part of an expression: the table of the previous section applies to it.

A frozen value keeps the result of the first call.
In [dev mode](/use/get-started/dev-tools/), JOID prints a warning once per call, in this form, here with the reason "the local variable row is combined with a signal" and the advice "Use a field, map(...) or a lambda.":

```text
[JOID] FollowUI.java:33 width(...) reads clicks but cannot follow it: <reason>. The value stays "40.0". <advice>
```

[Warnings and errors](/reference/errors/#but-cannot-follow-value) explains this warning.

The usual case is a loop that offsets each row:

```java fragment
for (int row = 0; row < 3; row++) {
	final RectNode bar = RectNode.create(60, 140 + row * 70, 0, 40).attach(card);
	bar.width(40D + row * 60D + this.clicks.get() * 40D); // ✗ row changes at each turn: frozen
}
```

`row` changes at each turn of the loop, so JOID cannot replay it next to `clicks`.
A value derived with `map` is computed by your own lambda, which reads `offset` like any Java lambda:

```java file="com/example/interactivity/signals/FollowUI.java" region="loop"
```

Three fixes exist, in this order of preference: a field in place of the local variable when the value is the same for every call, a derived value with `map(...)`, as here, where each row has its own offset, or a lambda.

## Read a value without following it

[[Signal#peek]] returns the same value as `get()`, but nothing follows it:

```java file="com/example/interactivity/signals/CounterUI.java" region="peek"
```

In the capture above, "At open: 0" stays at `0` while "Clicks: 3" follows.
Use `peek()` for a value that you read once, at the moment the code runs, such as in a listener.

## Choose a signal type

[[Signal]] holds any value: `new Signal<>(value)`.
The typed signals add the operations of their type:

| Type | Operations that change it |
|---|---|
| [[IntegerSignal]], [[LongSignal]], [[FloatSignal]], [[DoubleSignal]] | `increment`, `decrement`, `add`, `subtract`, `multiply`, `divide` |
| [[BooleanSignal]] | `toggle` |
| [[StringSignal]] | `append`, `replace`, `toUpperCase`, `toLowerCase`, `trim`, `substring` |
| [[ListSignal]] | `add`, `remove`, `set(index, element)`, `clear` |
| [[SetSignal]] | `add`, `remove`, `clear` |
| [[MapSignal]] | `put`, `remove`, `clear` |

```java file="com/example/interactivity/signals/TypesUI.java" region="types"
```

The reads of a collection signal (`size()`, `get(index)`, `contains(...)`, `isEmpty()`, `indexOf(...)` for a list, `containsKey(...)`, `keySet()` and `values()` for a map) are followed like `get()`:

```java file="com/example/interactivity/signals/TypesUI.java" region="use"
```

<figure>

![A card with three buttons, "Add Linus", "Remove Ada" and "Details", and the texts "Count: 2", "First: Ada" and "Has Linus: false"; the pointer clicks "Add Linus", then "Remove Ada" twice, and the texts change at the first two clicks only, ending at "Count: 2", "First: Grace" and "Has Linus: true"](/captures/interactivity-types.webp)

<figcaption>
"Add Linus", then "Remove Ada" twice: the second removal changes nothing, so nothing is computed again.
</figcaption>
</figure>

An operation of a collection signal notifies only when it changes something: removing an absent element computes nothing again.
`get()` of a collection signal returns a copy that cannot change: adding to it throws an `UnsupportedOperationException`, so change the collection through the operations of the signal.

A signal follows its value, not the inside of an object.
After you change an object held by a `Signal<T>` in place, call [[Signal#fireChange]] so that what reads it is computed again.

`new Signal<>()` holds `null`, and [[Signal#isPresent]] stays `false` until the first `set` of a value; the typed signals start at `0`, `false`, `""` or an empty collection.
[Rebuilding the tree](/use/learn/state/rebuilding/) uses an empty signal to wait for a value.

:::note
A signal can be written from any thread: JOID applies the write on the render thread ([Tasks and background updates](/use/learn/state/tasks/#set-a-signal-from-another-thread)).
:::

## Recap

- A **signal** holds a value; `set` and the typed operations change it, and an equal value changes nothing.
- For any setter call, ask **"When does JOID compute this value again?"**: a plain value never, an expression that reads signals or a signal when they change, a lambda or a method reference on every frame.
- Write a value that depends on signals as an **expression** that calls `get()`, or pass the signal itself; keep **lambdas** for values that change without a signal.
- The variable of a loop combined with a signal, a lambda inside the expression, the time or a random number **freeze** the expression; fix it with a **field**, **`map(...)`** or a **lambda**.
- A **getter** of a node, a control or the UI returns a field that JOID does not follow: pass a lambda, or bind the control to a signal.
- `peek()` reads a signal **without following** it.
- **Collection signals** follow their reads, change through their own operations and notify only on a real change; **`fireChange()`** notifies after a change made in place.

## Try it

**A capped count.**
In `CounterUI`, show "Clicks: 3 or more" in place of the count from the third click on.

<details>
<summary>Solution</summary>

One expression decides the whole text, so the text follows `clicks`:

```java fragment
TextNode
.create(60, 160)
.text(Text.create(this.clicks.get() >= 3 ? "Clicks: 3 or more" : "Clicks: " + this.clicks.get(), info))
.attach(card);
```

</details>

**Follow without map.**
In `FollowUI`, follow `clicks` without `map`.

<details>
<summary>Solution</summary>

A lambda runs on every frame, so it can read the local variable:

```java fragment
RectNode
.create(60, 140 + row * 70, 0, 40)
.color(FollowUI.ACCENT)
.width(() -> offset + this.clicks.get() * 40D)
.attach(card);
```

The value is right, at the cost of a computation on every frame.

</details>
