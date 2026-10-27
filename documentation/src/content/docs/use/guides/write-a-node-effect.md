---
title: "Write a node effect"
description: "Write a NodeEffect of your own, give it setters like the built-in effects, and apply it to any node."
genre: guide
sidebar:
  order: 5
---

An effect changes how a node is drawn without changing the node itself, and any node can take it.
This guide writes a badge effect: a dot on the top-right corner of a node, shown while a count is above zero.

## What you will build

<figure>

![A white rounded button labeled Inbox, with a grey dot on its top-right corner; the pointer clicks the button, and the dot disappears](/captures/guides-effect-badge.webp)

<figcaption>
The badge that the effect draws over the button, until a click sets the count to zero.
</figcaption>
</figure>

## Before you start

- What effects are and how they combine: [Effects](/use/learn/style-and-motion/effects/).
- The model of an effect of your own: [Custom effects](/use/learn/going-further/custom-effects/).
- The shapes you can draw: [Drawing](/use/learn/going-further/drawing/).

## 1. Create the effect

Extend [[NodeEffect]] typed for [[Node]], so that every node takes it.
Keep each setting in a `Supplier`, and give the class a `create(...)` factory for its required value:

```java file="com/example/guides/BadgeNodeEffect.java" region="class"
```

[[Signal#from]] turns the color into a `Supplier` that never changes.

**Check:** the class compiles.

## 2. Draw the badge

JOID calls `draw(node, body)` with the drawing of the node and its children as `body`.
Run `body` first, then draw the badge, so that it sits on top of them:

```java file="com/example/guides/BadgeNodeEffect.java" region="draw"
```

`node.getX()` and `node.getY()` are the position of the node in the coordinates the effect draws in.

**Check:** give a node the effect in white [[Color]], with `effect(BadgeNodeEffect.create(Color.WHITE))`: a dot shows on its top-right corner.

## 3. Add the setters

Give each setting a value setter and a `Supplier` setter, as the built-in effects do:

```java file="com/example/guides/BadgeNodeEffect.java" region="setters"
```

The value setter passes its value to `Signal.from`, so a value that reads a signal follows it, as with the setters of the nodes ([What JOID follows](/use/learn/interactivity/signals/#what-joid-follows)).

**Check:** `BadgeNodeEffect.create(Color.WHITE).shown(false)` compiles, and the dot still shows: nothing reads `shown` yet.

## 4. Hide it when the count is zero

JOID asks `shouldApply(node)` each time the node draws, before it runs the effect.
Return the `shown` setting, so the badge disappears while it is `false`:

```java file="com/example/guides/BadgeNodeEffect.java" region="apply"
```

**Check:** `BadgeNodeEffect.create(Color.WHITE).shown(false)` hides the dot.

## 5. Apply it to a node

The count of unread messages is an [[IntegerSignal]] that starts at 3:

```java file="com/example/guides/EffectGuideUI.java" region="count"
```

`effect(...)` adds the effect next to the other effects of the node, and a click sets the count to zero:

```java file="com/example/guides/EffectGuideUI.java" region="use"
```

**Check:** run the UI: the Inbox button shows a grey dot; a click sets the count to zero, `shouldApply` returns `false`, and the dot disappears.

## If it does not work

1. Nothing appears: `shouldApply` returns `false`, or the badge is drawn away from the node; draw from `node.getX()` and `node.getY()`.
2. The badge appears under the node: `draw` draws it before `body.run()`; run `body` first.
3. The badge does not follow the count: the count is a plain `int`, read once; keep it in a signal and pass `unread.get() > 0` ([What JOID follows](/use/learn/interactivity/signals/#what-joid-follows)).
4. `effect(...)` does not compile: the effect is typed for another node class; type it `NodeEffect<Node>` to accept every node.
5. The badge is cut at the edge of a parent: the parent clips its children ([Layout](/use/learn/describing-the-ui/layout/)); move the badge inside the node, or leave room around the node.

## Reference

- [[NodeEffect]]
- [[DrawShape]]
- [[RoundedNodeEffect]]
