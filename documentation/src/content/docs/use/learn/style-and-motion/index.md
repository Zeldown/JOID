---
title: "Style and motion"
description: "Color, decorate and animate a JOID interface."
genre: learn
sidebar:
  order: 0
---

JOID ships no theme: an interface looks the way you draw it, from the first color to the last animation.
This chapter gives you the tools to build that look and to make it move: colors, effects on nodes, animations and transitions between UIs.

## Colors and styling

Every shape takes a [[Color]]: a preset, components, a string copied from a design tool, or a gradient.

```java file="com/example/style/ColorsUI.java" region="palette"
```

Read [Colors and styling](/use/learn/style-and-motion/styling/) to learn how to create colors, derive a palette from one color, fill shapes with gradients and keep a palette safe.

## Effects

An effect changes how a node is drawn without touching the layout: rounded corners, borders, shadows, blur, masks and transforms.

```java file="com/example/style/EffectsUI.java" region="stack"
```

Read [Effects](/use/learn/style-and-motion/effects/) to learn how to stack effects, drive them with changing values, and predict the order in which JOID applies them.

## Animation

JOID animates values on its own clock, and a node reads the animated value in a lambda.

```java file="com/example/style/AnimationUI.java" region="appear"
```

Read [Animation](/use/learn/style-and-motion/animation/) to learn how to choose the right tool, shape the motion with easing, and chain, repeat or replay an animation.

## Transitions

A transition animates a whole UI when it opens and closes; popups play one by default.

```java file="com/example/style/SlidePopupUI.java" region="set"
```

Read [Transitions](/use/learn/style-and-motion/transitions/) to learn how to configure the popup transition and write a transition of your own.
