---
title: "Interactivity"
description: "How a JOID interface answers the mouse and the keyboard, and shows values that change."
genre: learn
sidebar:
  order: 0
---

An interface that only draws is a picture.
This chapter makes it answer the user: nodes react to the mouse and the keyboard, show values that change, follow the pointer, move under a drag, take text and hold values.
Each page builds on the previous one.

## Events

A node reports what happens to it through listeners that you give to its methods.
An input travels through the node tree from the front to the back, and the first listener that consumes it keeps it.

```java fragment
RectNode.create(100, 80, 200, 60).color(Color.WHITE).onClick(event -> clicks.increment()).attach(card);
```

Read [Events](/use/learn/interactivity/events/) to learn the three kinds of events, how to consume an input and how to refuse a change.

## Signals

A signal holds a value that changes; a setter given an expression that reads it follows it.

```java fragment
TextNode.create(60, 160).text(Text.create("Clicks: " + this.clicks.get(), info)).attach(card);
```

Read [Signals](/use/learn/interactivity/signals/) to learn when JOID computes a value again, what an expression can follow, and which signal type to pick.

## Hover, tooltips and cursors

A node tracks whether the pointer is over it, and can change its look, show a tooltip or set the cursor.

```java fragment
button.hoveredColor(Color.GRAY).hover(() -> "Opens the shop").cursor(Cursor.POINTER);
```

Read [Hover, tooltips and cursors](/use/learn/interactivity/hover-and-tooltips/) to learn the hover animation, the tooltips and the cursors.

## Drag and drop

One setter makes a node draggable inside an area, and a list of nodes gives it targets to drop on.

```java fragment
square.draggable(DraggableProperty.parent().snap(left, middle, right));
```

Read [Drag and drop](/use/learn/interactivity/drag-and-drop/) to learn the areas, the drop targets and the copies.

## Keyboard and shortcuts

The keyboard sends keys and typed characters to the nodes, then to the UI and its shortcuts.

```java fragment
Shortcut.create(Key.S).ctrl(true).onTrigger(event -> this.save()).attach(this);
```

Read [Keyboard and shortcuts](/use/learn/interactivity/keyboard/) to learn who receives the keys, how to tell a letter from its place, and how to bind shortcuts.

## Controls

Every control follows the same pattern: options, a value bound to a signal, `beforeChange` and `onChange`.

```java fragment
new IntegerSliderNode(240, 140, 300, 40) {}.track(...).thumb(...).range(0, 10).value(this.volume).attach(card);
```

Read [Controls](/use/learn/interactivity/controls/) to learn the pattern, the order of a change and the options of each control.

## Text fields

A text field turns what the user types into a value: a text, a number, or a type of your own.

```java fragment
IntegerFieldNode.create(60, 60, 240, 60).min(1).max(16).value(this.players).info(info).attach(card);
```

Read [Text fields](/use/learn/interactivity/text-fields/) to learn the focus, the rules on the text and your own fields.

## Reorderable lists

A [[ReorderableFlexNode]] is a row or a column whose items the user drags into a new order.

```java fragment
ReorderableFlexNode.vertical(60, 40, 480).onReorderEnd(event -> this.save()).attach(card);
```

Read [Reorderable lists](/use/learn/interactivity/reorderable-lists/) to learn the reorder events, the handles and the pinned items.
