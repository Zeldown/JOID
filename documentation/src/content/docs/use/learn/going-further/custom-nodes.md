---
title: "Custom nodes"
description: "Write a node of your own: drawing, hooks, properties, input, a value the user changes and events of your own."
genre: learn
sidebar:
  order: 1
---

When no built-in node draws what you need or behaves the way you need, write your own.
A custom node is a class that extends [[Node]]: it draws itself, takes its properties through fluent setters, reacts to the mouse and fires events, like the nodes JOID ships.

**You will learn**
- How to write a node that draws itself
- Which hooks of a node you can override
- How to give a node a property that follows a signal
- How a node reacts to the mouse and holds a value that the user changes
- How to fire an event of your own, and make the node duplicable

## Write a node that draws itself

A node needs a constructor, a factory and a `draw` method.
This one draws a dot inside its bounds:

```java file="com/example/further/DotNode.java" region="node"
```

It attaches, nests and positions like any built-in node:

```java file="com/example/further/CustomNodesUI.java" region="dots"
```

<figure>

![Three white discs side by side on a light grey background, each with a smaller grey disc at its center](/captures/further-custom-nodes-dots.webp)

<figcaption>
Each `DotNode` draws two circles in its own bounds.
</figcaption>
</figure>

The constructor is `protected`, so that the class can be extended, and takes the position and the size in design pixels.
The `public static` factory `create(...)` returns the concrete type, so that a chain of setters can follow it.

`draw(mouseX, mouseY)` runs on every frame while the node is visible.
It draws in the coordinates of the parent: the node sits at `getX()`, `getY()`, and its size is `getWidth()` × `getHeight()`.
`mouseX` and `mouseY` are in design pixels of the [[UI]]; [[Node#toDrawX]] and `toDrawY(y)` convert such a position into the coordinates where `draw` draws.

`dw(2D)` is the width divided by 2; [Position and size](/use/learn/describing-the-ui/position-and-size/) lists these helpers.
[Drawing shapes and text](/use/learn/going-further/drawing/) teaches the [[DrawUtils]] calls.

## Override the hooks you need

[[Node]] implements [[INode]], whose hooks JOID calls at each step of the life of a node.
Every hook does nothing by default, except `drawSkeleton`; override only the ones your node needs:

| Hook | JOID calls it |
|---|---|
| `init(UI ui)` | Each time the node joins a UI, after its children. A node can join several times: keep it repeatable. |
| `update()` | Once per frame, in the update step, after its children; a hidden node runs it too. |
| `layout()` | On every frame while the node is visible, right before it draws: place the children here. |
| `draw(double mouseX, double mouseY)` | On every frame while the node is visible and its `wait` conditions hold. |
| `drawSkeleton(double mouseX, double mouseY)` | Instead of `draw` while a `wait` condition fails. It fills the bounds with [[Color#LOADING]] by default. |
| `mousePressed`, `mouseReleased`, `mouseDragged`, `mouseScroll` | For every mouse event that reaches the node. |
| `keyPressed`, `keyReleased`, `charTyped` | For every key and every typed character that reach the node. |
| `detach()` | When the node leaves its UI, after its children. Release here what `init` or `draw` acquired. |

An exception thrown by `update` or `detach` is printed once as a warning, and the next frames go on.
[Node lifecycle](/use/learn/describing-the-ui/node-lifecycle/) places each step in the life of a node, and [The frame lifecycle](/use/in-depth/frame-lifecycle/) places `draw` among the children, the effects and the layers.

A hook is the node's own step; the listeners that users add with `onX(...)` and `handleX(...)` run around it.
None of these hooks is `final`, so a subclass of a built-in node overrides them too.

## Give the node a property

The swatch of the next sections is a square of color that the user selects with a click.
Its color is a property: a value setter and a `Supplier` setter, like every property of the built-in nodes.

```java file="com/example/further/SwatchNode.java" region="color"
```

[[Node#follow]] applies the value at once and keeps the source: before the node draws, JOID asks the source again and calls the consumer when the value differs.
A `Supplier` that reads signals is computed again only when one of them changes; [Signals](/use/learn/interactivity/signals/) explains when JOID computes a value again.
The value setter passes its argument to [[Signal#from]], which gives a constant that `follow` applies once and does not keep.
The first argument names the property: a second call with the same name replaces the first source.
[[Node#start]] applies a plain value once and drops the source of the property, and [[Node#bind]] binds the property to a writable signal both ways, [[Node#bindNullable]] when the signal can hold `null`.

A setter is `public final`, generic in `<T extends YourNode>`, returns `(T) this` through the helper, and is named after the property, without `set`.
The node draws with the field:

```java file="com/example/further/SwatchNode.java" region="draw"
```

When a property is required, refuse the node at once with [[Node#require]] in the constructor: the node throws `IllegalStateException` with your advice when it joins a UI without it.

## React to the mouse

The swatch overrides `mousePressed` to toggle its selection:

```java file="com/example/further/SwatchNode.java" region="input"
```

The hook receives every press that reaches the node, wherever the pointer is.
`isHovered()` tells whether the pointer is on the node, `event.isConsumed()` whether a node in front already took the press, and `event.consume()` stops it for the nodes behind and the UIs below.
The children in front of the node and its own `handleMousePressed` listeners hear the press first; [Events](/use/learn/interactivity/events/) explains this path and consumption.
The other input hooks follow the same pattern.
A node scrolled out of the area of its container receives no input, unless [[Node#holdsInput]] returns `true`: override it to keep the input while a gesture of the node goes on, as a focused text field does.

## Hold a value that the user changes

`selected` changes on a click, from a setter and from a signal, and the users of the swatch want to hear each change, or refuse it.
An [[InteractiveValue]] holds such a value, like the value of every built-in control:

```java file="com/example/further/SwatchNode.java" region="class"
```

[[InteractiveValue#create]] takes the bindings of the node (`getBindings()`), the name of the property, a getter and a setter of the field.
`events(...)` names the two events of a change: a `beforeX` event that a listener can cancel, then an `onX` event that tells what happened.
The fluent methods of the node hand each call to the value:

```java file="com/example/further/SwatchNode.java" region="selected"
```

- `start(value)` gives a start value.
- `bind(signal)` follows a writable signal and writes the changes of the user back to it; a signal that cannot be written throws `IllegalArgumentException`.
- `change(value)` changes the value as the user would: `beforeSelect` can refuse it, then `onSelect` fires.
- In an input hook, `set(value)` keeps the value pending until the hook ends, and `get()` reads the pending value.

The UI owns the signal and reads it anywhere:

```java file="com/example/further/CustomNodesUI.java" region="signal"
```

```java file="com/example/further/CustomNodesUI.java" region="swatch"
```

<figure>

![The pointer moves onto a grey square and clicks it: a white frame appears inside the square, and the text next to it changes from Plain and 0 changes to Favorite and 1 changes](/captures/further-custom-nodes-swatch.webp)

<figcaption>
The click selects the swatch, writes the signal and fires `onSelect`.
</figcaption>
</figure>

The events fire on every real change, whatever its source, and never when the value stays the same.
[Controls](/use/learn/interactivity/controls/) explains the order of these events and the pending value.

## Fire an event of your own

An event that is not the change of a value, such as a reset, needs an [[EventType]], a listener method and a call to [[Node#fire]]:

```java file="com/example/further/SwatchNode.java" region="reset"
```

`EventType.create(name)` creates the type once, in a static field; JOID prints the name in the warning of a listener that fails.
[[Node#listen]] adds a listener of that type, and `fire(type, supplier)` builds the event and calls the listeners.
JOID builds the event only when the node has a listener of that type.

The event is a [[NodeEvent]] that carries the node.
For an event that carries a change, use [[NodeChangeEvent]], whose `Pre` form a listener can cancel.

## Make your node duplicable

`copy()` and the ghost of a drag of type `COPY` build your node again with its `(x, y, width, height)`, `(x, y)` or argument-free constructor, the first one it finds, then copy its fields.
A node with none of them overrides [[Node#instantiate]] to build itself.

Mark `transient` a field that belongs to one instance only, such as a framebuffer or a cache: the copy leaves it out and builds its own.
A field that the copy cannot set throws `IllegalStateException` with the name of the field.
[Position and size](/use/learn/describing-the-ui/position-and-size/) explains what `copy()` gives.

## Recap

- A custom node extends [[Node]], with a **`protected` constructor** `(x, y, width, height)` and a **`create(...)` factory**.
- **`draw`** runs on every frame, in the coordinates of the parent: the node sits at `getX()`, `getY()`.
- Override only the **hooks** you need: `init`, `update`, `layout`, `draw`, the input hooks and `detach`.
- Each property has a **value setter and a `Supplier` setter**, both through **`follow`**.
- An **input hook** checks `isHovered()` and `isConsumed()`, then calls `consume()`.
- A value that the user changes lives in an **`InteractiveValue`**, with `start`, `bind`, `change` and a **`beforeX`/`onX`** pair.
- An event of your own is an **`EventType`**, a **`listen`** method and a **`fire`** call.
- A field of one instance only is **`transient`**, so that `copy()` leaves it out.

## Try it

**React to the right button.**
Make the swatch toggle on a right click only.

<details>
<summary>Solution</summary>

In `mousePressed`, test the other button:

```java fragment
if (event.isConsumed() || !event.getButton().isRight() || !super.isHovered()) {
```

</details>

**Refuse a selection.**
In the UI, keep the swatch from being selected while a [[BooleanSignal]] `locked` holds `true`.

<details>
<summary>Solution</summary>

Cancel the `beforeSelect` event:

```java fragment
.beforeSelect(event -> {
	if (locked.get()) {
		event.cancel();
	}
})
```

</details>

**Count the resets.**
Add a button that calls `reset()` on the swatch, and show how many times `onReset` fired.

<details>
<summary>Solution</summary>

Keep the swatch in a local variable, call `swatch.reset()` from the `onClick` of a [[RectNode]], and count in `onReset`:

```java fragment
swatch.onReset(event -> resets.set(resets.get() + 1));
```

</details>
