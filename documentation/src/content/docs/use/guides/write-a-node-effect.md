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

![A dark rounded button labeled Inbox, with a blue dot on its top-right corner; the pointer clicks the button, and the dot disappears](/captures/guides-effect-badge.webp)

<figcaption>
The badge that the effect draws over the button, until a click sets the count to zero.
</figcaption>
</figure>

## Before you start

- What effects are and how they combine: [Effects](/use/learn/style-and-motion/effects/).
- The model of an effect of your own: [Custom effects](/use/learn/going-further/custom-effects/).
- The shapes you can draw: [Drawing shapes and text](/use/learn/going-further/drawing/).

## 1. Create the effect

Extend [[NodeEffect]] typed for [[Node]], so that every node takes it, keep each setting in a `Supplier`, and give the class a `create(...)` factory for its required value ([Custom effects](/use/learn/going-further/custom-effects/)):

```java file="com/example/guides/BadgeNodeEffect.java" region="class"
```

**Check:** the class compiles.

## 2. Draw the badge

Run `body` first, then draw the badge on top of it ([Draw around a node](/use/learn/going-further/custom-effects/#draw-around-a-node)):

```java file="com/example/guides/BadgeNodeEffect.java" region="draw"
```

**Check:** give a node the effect in white [[Color]], with `effect(BadgeNodeEffect.create(Color.WHITE))`: a dot shows on its top-right corner.

## 3. Add the setters

Give each setting a value setter and a `Supplier` setter, as a node does for its properties ([Give the node a property](/use/learn/going-further/custom-nodes/#give-the-node-a-property)):

```java file="com/example/guides/BadgeNodeEffect.java" region="setters"
```

**Check:** `BadgeNodeEffect.create(Color.WHITE).shown(false)` compiles, and the dot still shows: nothing reads `shown` yet.

## 4. Hide it when the count is zero

Return the `shown` setting from `shouldApply(node)`, so the badge disappears while it is `false` ([Change how a node draws](/use/learn/going-further/custom-effects/#change-how-a-node-draws)):

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

**Check:** run the UI: the Inbox button shows a blue dot; a click sets the count to zero, `shouldApply` returns `false`, and the dot disappears.

## If it does not work

1. Nothing appears: `shouldApply` returns `false`, or the badge is drawn away from the node; draw from `node.getX()` and `node.getY()`.
2. The badge appears under the node: run `body` before drawing it ([Draw around a node](/use/learn/going-further/custom-effects/#draw-around-a-node)).
3. The badge does not follow the count: the count is a plain `int`, read once; keep it in a signal and pass `unread.get() > 0` ([What JOID follows](/use/learn/interactivity/signals/#what-joid-follows)).
4. `effect(...)` does not compile: type the effect `NodeEffect<Node>` ([Place your effect among the others](/use/learn/going-further/custom-effects/#place-your-effect-among-the-others)).
5. The badge is cut at the edge of a parent: the parent clips its children ([Layout](/use/learn/describing-the-ui/layout/)); move the badge inside the node, or leave room around the node.

## Reference

- [[NodeEffect]]
- [[DrawShape]]
- [[RoundedNodeEffect]]
