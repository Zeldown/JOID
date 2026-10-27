---
title: "Events"
description: "React to a click, hear the raw input and consume it, refuse a change before it happens, and order the listeners of an event."
genre: learn
sidebar:
  order: 1
---

A node tells you what the user does to it through events.
You subscribe to an event with a method of the node, and JOID calls your listener when the event happens.
This page shows the three kinds of events, the path of an input through the node tree, and how a listener stops it.

**You will learn**
- How to react to a click
- How to let the pointer through a node that only decorates
- How to hear the raw input and consume it
- How to refuse a change before it happens
- How JOID orders the listeners of one event

## React to a click

A button is a node with a listener on [[Node#onClick]]:

```java file="com/example/interactivity/events/ClickUI.java" region="click"
```

<figure>

![A grey card with a white "Click me" button and the text "Clicks: 0" under it; the pointer clicks the button three times, and the count goes up to 3](/captures/interactivity-click.webp)

<figcaption>
Each press on the button runs its listener once.
</figcaption>
</figure>

The listener receives a [[NodeMouseEvent]]: `getNode()` is the node, `getMouseX()` and `getMouseY()` the pointer in [design pixels](/use/learn/describing-the-ui/design-canvas/), `getButton()` the button.
`onClick` runs when the left button goes down over the node, not when it goes up.
The text under the button reads `clicks`, so it changes with it: [Signals](/use/learn/interactivity/signals/) explains why.

## Let the pointer through a decoration

The pointer always hovers one node: the frontmost node under it that is interactive.
That node and all its parents are hovered, and `onClick` runs only on a hovered node.
A child that has no `onClick` of its own, such as the label of a button, lets the press reach the `onClick` of its parent.

A node drawn over the button that is not its child takes the pointer for itself.
The badge on the left blocks the button; the badge on the right is set with [[Node#interactive]] `(false)`, and the pointer goes through it:

```java file="com/example/interactivity/events/ThroughUI.java" region="blocked"
```

```java file="com/example/interactivity/events/ThroughUI.java" region="through"
```

<figure>

![Two white "Order" buttons with a grey badge on their top right corner; the pointer presses each badge twice: the count under the left button stays at "Clicks: 0", the one under the right button goes to "Clicks: 2"](/captures/interactivity-through.webp)

<figcaption>
Two presses on each badge: only the badge set with `interactive(false)` lets the press reach its button.
</figcaption>
</figure>

Three settings take a node out of the input, and each one applies to the children of the node too:

| Setting | The node is drawn | The pointer can hover it | Its listeners and its own handling run |
|---|---|---|---|
| [[Node#interactive]] `(false)` | Yes | No, the pointer reaches the node behind | No |
| [[Node#enabled]] `(false)` | Yes | Yes | No |
| [[Node#visible]] `(false)` | No | No | No |

The release of a button and of a key is the exception: it reaches every node, even hidden or disabled, so that a press that started a gesture always ends it.

## Hear the raw input

The methods whose name starts with `handle` give you the input itself, before the node handles it:
[[Node#handleMousePressed]], [[Node#handleMouseReleased]], [[Node#handleMouseDragged]], [[Node#handleMouseScroll]], [[Node#handleCharTyped]], [[Node#handleKeyPressed]] and [[Node#handleKeyReleased]].
Every node of the UI receives every press, wherever the pointer is: a listener that cares about the pointer tests `isHovered()` itself.

```java file="com/example/interactivity/events/PressUI.java" region="listen"
```

<figure>

![A grey card with a "Button" and four counts at 0; the pointer presses the button, the card, then leaves the card to press outside it, and the counts end at "Button: 1", "Card heard: 2", "Card hovered: 1" and "Monitor: 3"](/captures/interactivity-press.webp)

<figcaption>
Three presses: on the button, on the card, then outside the card.
The card heard the press outside it, and missed the press that the button consumed.
</figcaption>
</figure>

A listener stops an input with [[NodeInputEvent#consume]].
A consumed input reaches no other `handle` listener, no `onClick`, none of the handling that a node does by itself (a checkbox that toggles, a field that types), no UI below and not the host application.
The `onClick` of the button consumed the first press, so the listener of the card never heard it.
Only a listener of the priority `MONITOR` still hears a consumed input ([Order the listeners of one event](#order-the-listeners-of-one-event)).

Because the listeners of a node run before its own handling, a listener can keep an input from the node itself.
This field never receives a digit:

```java file="com/example/interactivity/events/NoDigitsUI.java" region="consume"
```

<figure>

![A white text field with the placeholder "No digits"; the pointer clicks it and the user types R, 2, D and 2, and only "RD" appears](/captures/interactivity-consume.webp)

<figcaption>
The user types `R2D2`: the listener consumes both digits before the field sees them.
</figcaption>
</figure>

### Follow an input through the tree

<figure>

![A diagram: on the left, a white card holds a violet button; on the right, eight numbered steps follow a press on the button, from the listeners of the button to the UIs below, with step 2, "Button: onClick runs and consumes the press", in violet](/captures/interactivity-dispatch.webp)

<figcaption>
The front child receives the press first, then its parent, up to the UI.
Once the press is consumed, only the `MONITOR` listeners still hear it.
</figcaption>
</figure>

JOID sends an input to the nodes of the UI from the front to the back: the node drawn last receives it first.
Inside one node, the order is:

1. its children, from the front to the back, each one with its own children first;
2. its `handle` listeners;
3. its own handling: `onClick` for a press, the toggle of a checkbox, the typing of a field;
4. its children with a negative `zindex`, drawn behind it;
5. its `MONITOR` listeners.

After the nodes, the UI receives the input that no node consumed: [[UI#handleMousePressed]] and the other `handle` methods of [[UI]] react to a press in an empty area, for example to close a menu.
An input that the UI does not consume goes on to the UI below it ([How UIs stack](/use/learn/describing-the-ui/overlays-and-popups/#see-how-uis-stack)).

[[Node#fireMousePressed]] and the other `fire` methods of a node send a simulated input to it and its children, and return `true` when a listener consumed it.

## Refuse a change before it happens

A change that the user or your code asks for, such as moving a node, first goes through a `before` event.
This square refuses to move past `x = 400`:

```java file="com/example/interactivity/events/LimitUI.java" region="before"
```

<figure>

![A grey square at the left of a card, a white "Move right" button and the text "Refused: 0"; the pointer presses the button four times: the square moves right three times, and the fourth press leaves it in place and shows "Refused: 1"](/captures/interactivity-before.webp)

<figcaption>
Four presses on the button: the fourth move, to `x = 440`, is refused.
</figcaption>
</figure>

[[Node#beforeMove]] receives a [[NodeChangeEvent.Pre]]: `getPrevious()` is the value now, `getValue()` the value asked for, and [[NodeChangeEvent.Pre#cancel]] refuses it.
After a change that went through, [[Node#onMove]] receives a [[NodeChangeEvent.Post]] with the same two values.

A `before` event guards the changes that a gesture asks for and the calls that make the same request, such as [[Node#move]] or the `change(...)` of a [control](/use/learn/interactivity/controls/#refuse-a-change).
The setters that give a node its value, such as `x(...)`, and the signals bound to it are never refused.
A UI has the same pair for its closing: [[UI#beforeClose]] can keep it open, and [[UI#onClose]] runs once it is closed.

## Know the three kinds of events

Every event method of JOID starts with one of three prefixes, and the prefix says what your listener can do:

| Prefix | When the listener runs | What it can do | Examples |
|---|---|---|---|
| `handle` | The raw input reaches the node, before the node handles it | Consume the input | `handleMousePressed`, `handleKeyPressed` |
| `before` | A change is asked for, before it applies | Cancel the change | `beforeMove`, `beforeChange`, `beforeClose` |
| `on` | Something happened | Read what happened | `onClick`, `onMove`, `onChange`, `onHoverStart` |

Each listener receives one event object, which holds the node (`getNode()`) or the UI (`getUi()`) and the values of the event.
A node also has events of its own life, such as [[Node#onInit]] or [[Node#onDetach]]: [Node lifecycle](/use/learn/describing-the-ui/node-lifecycle/) lists them.

## Order the listeners of one event

Every node and every UI holds its listeners in an [[EventBus]], one list per [[EventType]].
The listeners of one event run in the order of their [[EventPriority]], then in the order you added them.
[[EventListener#priority]] gives a listener another priority than `NORMAL`:

```java fragment
button
.onClick(event -> log.add("second"))
.onClick(EventListener.priority(EventPriority.HIGH, event -> log.add("first")));
```

The priorities run from `HIGHEST` to `LOWEST`, then `MONITOR`.
Once a listener consumes an input or cancels a change, the listeners after it do not run, except the `MONITOR` ones.
A `MONITOR` listener only reads: it runs even after a consume or a cancel, and calling `consume()` or `cancel()` there throws an `IllegalStateException`.

A listener that throws an exception does not stop the others: JOID prints a [warning](/reference/errors/) once and runs the next listener.

A listener that you add in the constructor of a UI stays as long as the UI object lives.
A listener that you add in `init()` lasts one opening: JOID removes it when the UI closes or reloads, and `init()` adds it again.

## Recap

- [[Node#onClick]] runs when the left button goes down over the node, and **consumes** the press.
- The **hovered** node is the frontmost interactive node under the pointer, with all its parents; `interactive(false)` lets the pointer through a node.
- The **`handle`** listeners hear the raw input of the whole UI, wherever the pointer is, before the node handles it; `consume()` stops the input.
- A consumed input reaches no other handler, no UI below and not the host; only the **`MONITOR`** listeners still hear it.
- A **`before`** listener cancels a change that a gesture or a call such as `move(...)` asks for; setters and signals are never refused.
- The **`on`** listeners tell what happened.
- Listeners run by **priority**, then in the order you added them.

## Try it

**Right clicks only.**
Count the presses of the right button on the button of `ClickUI`, and only those.

<details>
<summary>Solution</summary>

`onClick` hears only the left button, so listen to the raw press and test the pointer yourself:

```java fragment
.handleMousePressed(event -> {
	if (event.getNode().isHovered() && event.getButton().isRight()) {
		clicks.increment();
	}
})
```

</details>

**Count what nobody took.**
In `PressUI`, count the presses that no node consumed: a press on the button must not count.

<details>
<summary>Solution</summary>

The UI hears the input after its nodes, and only when no node consumed it:

```java fragment
super.handleMousePressed(event -> free.increment());
```

The press on the button is consumed by its `onClick`, so it never reaches this listener.

</details>
