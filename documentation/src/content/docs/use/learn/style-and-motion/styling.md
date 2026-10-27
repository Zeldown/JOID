---
title: "Colors and styling"
description: "Create colors, derive a palette from one color, fill shapes with gradients, and keep a palette safe from changes."
genre: learn
sidebar:
  order: 1
---

JOID imposes no look: every pixel takes the color you give it.
This page shows how to create colors, derive a palette from one of them, fill shapes with gradients and keep shared colors safe.

**You will learn**
- How to create a color from components or from a string
- How to derive a palette from one color
- How to fill a shape with a gradient
- How to keep a palette safe from changes
- Which colors change on their own

## Create a color

A [[Color]] is an RGBA value.
Pass it to the `color(...)` setter of a shape, to a [[TextInfo]] for text, or to an effect:

```java file="com/example/style/ColorsUI.java" region="create"
```

<figure>

![Five squares on a white card: grey, a medium blue, an orange, a green and a half-transparent blue](/captures/style-create.webp)

<figcaption>A preset, float components, integer components, a hex string and an `rgba(...)` string.</figcaption>
</figure>

There are four ways to build a color:
- a preset such as `Color.WHITE`, `Color.GRAY` or `Color.TRANSPARENT`;
- float components from `0F` to `1F`: `new Color(0.2F, 0.4F, 0.8F)`, with an optional alpha as a fourth argument;
- integer components from `0` to `255`: `new Color(230, 120, 50)`, with an optional alpha from `0` to `255`;
- a string read by [[Color#decode]]: `"#RRGGBB"`, `"#RRGGBBAA"`, `"rgb(r, g, b)"`, or `"rgba(r, g, b, a)"` with an alpha from `0` to `1`.

`Color.decode` is the shortest way to copy a color from a design tool, and throws a `NumberFormatException` on a string it cannot read.
[[Color#rgb]] and [[Color#argb]] read a packed `int` such as `0x3366CC`, and [[Color#encode]] writes `#RRGGBBAA` back.

The constructor picks its scale from the type of its arguments: `new Color(1, 0, 0)` is a nearly black red (1 out of 255), `new Color(1F, 0F, 0F)` a full red.

## Derive a palette from one color

`darker`, `brighter`, `withAlpha` and `to` return a new color and leave the original as it is, so one base color gives a consistent palette:

```java file="com/example/style/ColorsUI.java" region="derive"
```

<figure>

![Six blue squares: the base, two darker shades, a lighter tint, a half-transparent copy and a mix with white](/captures/style-derive.webp)

<figcaption>From left to right: the base, `darker()`, `darker(0.25F)`, `brighter(0.6F)`, `withAlpha(0.5F)` and `to(Color.WHITE, 0.5F)`.</figcaption>
</figure>

- `darker(scale)` multiplies the red, green and blue components by `1 - scale` (`0.5F` without argument).
- `brighter(scale)` multiplies them by `1 + scale` (`0.2F` without argument); a component never goes above `1F`.
- `withAlpha(alpha)` keeps the color and replaces its opacity; `withRed`, `withGreen` and `withBlue` replace one component.
- `to(target, progress)` blends toward `target`: `0F` gives the color itself, `1F` the target, `0.5F` the middle.

`multiply`, `add` and `scale` combine colors component by component.

## Fill with a gradient

[[Color#toGradient]] turns a color into a linear gradient from that color to another.
A gradient is a `Color` too, so it goes where a color goes: fills, borders, text and image tints.

```java file="com/example/style/ColorsUI.java" region="gradient"
```

<figure>

![Four gradients from blue: to white left to right, to white top to bottom, to white along the diagonal, and blue fading to transparent](/captures/style-gradient.webp)

<figcaption>The default direction, then from the top to the bottom, along the diagonal, and toward the same blue with an alpha of 0.</figcaption>
</figure>

Without points, the gradient goes from the left edge to the right edge.
Two [[Point]] values set another direction, in fractions of the drawn shape: `Point.of(0D, 0D)` is the top-left corner and `Point.of(0D, 1D)` the bottom-left one.
`Color.decode` reads gradients as well: `"gradient(#3366CC, #FFFFFF)"`, with four optional numbers after the colors for the two points.

Fading to the same color with an alpha of `0` keeps the hue; `Color.TRANSPARENT` is a transparent black, which greys the middle of the gradient.

## Keep a palette safe

A color that you create with `new` can change in place: `red(...)`, `green(...)`, `blue(...)` and `alpha(...)` modify it, and every node that uses it shows the change at its next frame.
[[LockableValue#lock]] freezes a color, so that a palette shared by your UIs never changes by accident:

```java file="com/example/style/ColorsUI.java" region="palette"
```

A locked color refuses `alpha(...)` and the other changes with an `IllegalStateException` that names the copy to use, such as `withAlpha(...)`.
The presets of `Color` are locked, and [[Color#copy]] gives an unlocked copy of any color.

The color of a node can also follow the state of your UI: give `color(...)` an expression that reads a signal, or a lambda ([Signals](/use/learn/interactivity/signals/)).
A second color for the hovered state is set with `hoveredColor(...)` ([Hover, tooltips and cursors](/use/learn/interactivity/hover-and-tooltips/)).

## Use the colors that change on their own

Two presets change with the clock of JOID:
- `Color.RAINBOW` cycles through the hues every 3 s;
- `Color.LOADING` pulses slowly between two dark greys, for loading placeholders ([Rebuilding the tree](/use/learn/state/rebuilding/#wait-for-data-before-showing-a-node)).

A color derived from them changes with them: `Color.RAINBOW.darker()` is a darker rainbow, and `Color.LOADING.withAlpha(0.5F)` a half-transparent pulse.
`Color.RAINBOW()` and `Color.LOADING()` return the shade of this instant, as a color that stays put.

## Recap

- Build a **`Color`** from a preset, float components (`0F` to `1F`), integer components (`0` to `255`) or **`Color.decode`**.
- **Derive** a palette with `darker`, `brighter`, `withAlpha` and `to`: each call returns a new color.
- A **gradient** from `toGradient` is a color, with a direction given by two points in fractions of the shape.
- **`lock()`** your shared colors: a locked color refuses in-place changes, as the presets do.
- **`RAINBOW`** and **`LOADING`** change on their own, and so do the colors derived from them.

## Try it

**Fade a card out.**
Fill a card with `#3366CC` at the top, fading to transparent at the bottom.

<details>
<summary>Solution</summary>

```java fragment
final Color blue = Color.decode("#3366CC");
final Color fade = blue.toGradient(blue.withAlpha(0F), Point.of(0D, 0D), Point.of(0D, 1D));
RectNode.create(0, 0, 300, 200).color(fade).attach(this);
```

</details>

**Darken on hover.**
Make a button 25 % darker while the mouse is over it, without writing the darker color by hand.

<details>
<summary>Solution</summary>

```java fragment
RectNode.create(0, 0, 200, 60).color(ColorsUI.BRAND).hoveredColor(ColorsUI.BRAND.darker(0.25F)).attach(this);
```

</details>
