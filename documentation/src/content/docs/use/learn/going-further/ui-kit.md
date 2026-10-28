---
title: "Give your controls a look"
description: "Skin the controls of JOID with parts, draw a control entirely your own way, and gather your skins in a UI kit."
genre: learn
sidebar:
  order: 9
---

The checkbox, the toggle, the switch, the selector, the slider and the scrollbar of JOID hold a value and react to the mouse, but they draw nothing of their own: each one is an abstract [[Node]].
A small subclass, a skin, gives it a look; a set of skins that share their colors forms your UI kit.

**You will learn**
- How a control splits its behavior and its look
- How to skin a control with parts
- Which parts each control takes
- How to draw a control entirely your own way
- How to gather your skins in a UI kit

## See how a control is built

Each control comes in two classes:

- `Base<X>Node`, such as [[BaseCheckboxNode]], holds the behavior: the value, the mouse gestures, `beforeChange` and `onChange`, and what a look needs to know, such as `getProgress()` of a slider.
- `<X>Node`, such as [[CheckboxNode]], extends it with named parts: nodes that you give it, and that it places, sizes and animates.

Extend `<X>Node` for a look made of the usual parts; extend `Base<X>Node` only when the look does not fit them.
Either way, the skin is a subclass with a `protected` constructor and a `create(...)` factory, like any [custom node](/use/learn/going-further/custom-nodes/), and the value, its signals and its events work as [Controls](/use/learn/interactivity/controls/) teaches.

<figure>

![A checked dark grey checkbox with a blue mark, a slider with its blue thumb near the middle, a switch with Medium selected in blue among Low, Medium and High, and a pill toggle switched on, blue with its dark knob on the right](/captures/further-ui-kit-controls.webp)

<figcaption>Four skins of this page.
The first three give parts to their control; the pill toggle draws itself from its base class.</figcaption>
</figure>

## Skin a control with parts

A skin passes its parts in its constructor:

```java file="com/example/kit/KitCheckboxNode.java" region="checkbox"
```

```java file="com/example/further/KitUI.java" region="use"
```

The control places its parts on every frame, so a part is created at `0, 0`:

- A part of size `0` takes the size the control gives it: here the box covers the whole checkbox.
- A part with a size keeps it, and the control centers or moves it: here the mark stays half the size of the box.
- The parts that sit behind the others (box, track, field, background) get `zindex(-1)`, so the other parts and the children you add draw over them.

A part follows the state of its control through a reactive setter, as the thumb of the slider does while it slides:

```java file="com/example/kit/KitIntegerSliderNode.java" region="slider"
```

A control without one of its required parts throws when it is attached, for example `The KitCheckboxNode has no mark, give it one with mark(...)`.
A control of your own that takes parts sets each one with [[Node#part]], which puts the new part at the place of the previous one among the children.

## Know the parts of each control

| Control | Parts | What the control does with them |
|---|---|---|
| [[CheckboxNode]] | `box`, `mark` (required) | The box covers the control; the mark is centered and shown while checked. |
| [[ToggleNode]] | `track`, `knob` (required) | The track covers the control; the knob is half its width by default and slides from left to right. |
| [[SliderNode]] | `track`, `thumb` (required) | The track covers the control; the thumb is a square of its height by default and moves with the value. |
| [[SwitchNode]] | `option((value, index) -> node)` (required) | Creates one node per value and splits the width between them; a click on one selects its value. |
| [[SelectorNode]] | `field`, `option(value -> node)` (required) | The field covers the list, open or closed; each option is one row of the height of the control, shown while the list is open, and the selected one while it is closed. |
| [[ProgressNode]] | `fill` (required), `track` | The track covers the control; the fill covers the part that the progress reaches, from the side of its direction. |
| [[ScrollbarNode]] | `track`, `thumb` (required) | The track covers the bar; the thumb is sized and moved from the scroll of the node it serves. |
| Text fields ([[FieldNode]]) | `background` | The background covers the field. A text field draws its text itself and needs no skin. |

The switch and the selector call your `option` function again each time their values change.
[Text fields](/use/learn/interactivity/text-fields/) shows the hooks of a field of your own, and [Charts](/use/learn/going-further/charts/) the skins of a chart.

## Draw a control entirely your own way

When the parts do not fit, extend the base class and build the look yourself.
This pill toggle slides a circle in a rounded track whose color follows the circle:

```java file="com/example/kit/KitPillToggleNode.java" region="pill"
```

- Create the children each time the control joins a [[UI]], in `init(UI)`, and call `clearChildren()` first so that a second opening does not add them twice.
- Build the look from nodes and effects rather than drawing by hand: they get hover, effects and layout like any node.
- Animate on a change with a [[TweenAnimator]] started in `onChange`; `sequence` starts from the value the animator holds, so a quick second click turns the animation back from where it is ([Animation](/use/learn/style-and-motion/animation/) teaches animators).
- Set the animator to the current value in `init`, so that the control opens in place, without animating.

A listener added in the constructor of a skin lasts as long as the node, as one added in the constructor of a UI does ([Open and close a UI](/use/learn/describing-the-ui/uis/#open-and-close-a-ui)).

## Gather your skins in a UI kit

A UI kit is your set of skins and the colors and sizes they share.
Keep these values in one class, and read them from every skin:

```java file="com/example/kit/KitColors.java" region="colors"
```

Changing a color there changes it in every control of every UI.
The skins themselves stay small: the controls keep the behavior, so a kit never repeats it.
[Build a UI kit](/use/guides/build-a-ui-kit/) builds a complete kit, step by step.

## Recap

- A control is a **`Base<X>Node`** that holds the behavior, extended by an **`<X>Node`** that places its **parts**.
- A **skin** passes its parts in its constructor and has a `create(...)` factory.
- A part of **size `0`** takes the size of the control; a part with a size keeps it.
- The parts behind the others get **`zindex(-1)`**; a missing required part **throws** on attach.
- A look of your own extends **`Base<X>Node`**, builds its children in **`init(UI)`** after `clearChildren()`, and animates in **`onChange`**.
- A **UI kit** is a set of skins that read their colors and sizes from one place.

## Try it

**A round mark.**
Draw the mark of the checkbox as a disc.

<details>
<summary>Solution</summary>

```java fragment
super.mark(CircleNode.create(0, 0, width / 2D).color(KitColors.ACCENT));
```

</details>

**A slower toggle.**
Write a toggle skin whose knob takes longer to slide.

<details>
<summary>Solution</summary>

In the constructor of a subclass of [[ToggleNode]], after its `track` and `knob`:

```java fragment
super.speed(0.1F);
```

A lower speed covers a smaller part of the distance on each frame.

</details>
