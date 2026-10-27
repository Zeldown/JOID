---
title: "Build a UI kit"
description: "Write skins for the controls, gather them in kits behind one interface, and switch kits at run time."
genre: guide
sidebar:
  order: 6
---

A skin gives one control its look; a UI kit gathers a skin for each control behind one interface.
This guide writes two kits for a checkbox and a slider, and switches the kit of a screen while it runs.

## What you will build

<figure>

![A light grey card with a square checkbox and a flat square slider, above two white buttons Plain kit and Round kit; the pointer drags the slider, then clicks Round kit, and the card turns white with a rounded checkbox and a pill-shaped slider with a round knob, at the same value; it drags the slider back and clicks Plain kit](/captures/guides-kit.webp)

<figcaption>
A click on a kit changes the skins of the controls; their values stay.
</figcaption>
</figure>

## Before you start

- Abstract controls, their parts and how a skin sets them: [Give your controls a look](/use/learn/going-further/ui-kit/).
- Binding a control to a signal: [Controls](/use/learn/interactivity/controls/).
- Rebuilding the children of a node when a signal changes: [Rebuilding the tree](/use/learn/state/rebuilding/).

## 1. Write a skin for each control

A skin extends a control of JOID, such as [[CheckboxNode]] or [[IntegerSliderNode]], and gives it its parts in its constructor.
The round checkbox sets a box and a mark with rounded corners:

```java file="com/example/guides/RoundGuideCheckboxNode.java" region="skin"
```

Write the round slider the same way, with a pill-shaped track and a [[CircleNode]] as its thumb, then a plain checkbox and a plain slider with square parts: `PlainGuideCheckboxNode`, `PlainGuideSliderNode`, `RoundGuideCheckboxNode` and `RoundGuideSliderNode` in the examples.

**Check:** put `RoundGuideCheckboxNode.create(100, 100, 50, 50)` in a UI: it checks and unchecks on click.

## 2. Gather the skins behind an interface

A screen that names the skins would have to change to switch kits.
Declare what a kit provides in an interface:

```java file="com/example/guides/IGuideKit.java"
```

Each kit implements it with its own skins and colors:

```java file="com/example/guides/RoundGuideKit.java" region="kit"
```

`PlainGuideKit` returns the plain skins with the same signatures, and a light grey surface.

**Check:** put `new RoundGuideKit().checkbox(100, 100)` in a UI: it shows the round checkbox of step 1.

## 3. Build the screen from the kit

Keep the kit in a signal, and the values in signals of their own, outside the controls:

```java file="com/example/guides/KitGuideUI.java" region="state"
```

The card rebuilds its children each time the kit changes ([Rebuilding the tree](/use/learn/state/rebuilding/)), and its color derives from the kit with `map(...)`.
The controls bind the same signals in each rebuild, so the values survive the change of kit:

```java file="com/example/guides/KitGuideUI.java" region="screen"
```

**Check:** the card shows the plain checkbox and slider.

## 4. Switch kits at run time

A click sets the kit signal, and the card rebuilds itself:

```java file="com/example/guides/KitGuideUI.java" region="switch"
```

**Check:** uncheck the box, move the slider, then click Round kit.
The card turns white, the controls change their look, the box stays unchecked and the slider keeps its value.

## If it does not work

1. Attaching a skin throws `IllegalStateException: The … has no mark, give it one with mark(...)`: the skin does not set that part in its constructor.
2. The values reset when the kit changes: the controls received values instead of signals; bind them to signals declared outside the rebuilt body.
3. Nothing changes on click: the card does not watch the kit signal, or the body reads the kit outside the card.
4. A screen keeps an old skin: it creates the skin directly; go through the kit interface.

## Reference

- [[CheckboxNode]]
- [[IntegerSliderNode]]
- [[SliderNode]]
- [[WatchProperty]]
