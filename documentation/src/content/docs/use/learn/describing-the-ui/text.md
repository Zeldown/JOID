---
title: "Text"
description: "Draw text with TextNode, Text and TextInfo: styles, alignment, long texts, runs and measures."
genre: learn
sidebar:
  order: 10
---

Titles, labels, paragraphs: text is in every interface.
JOID builds it from three pieces: a style that names a font, the text itself, and a node that draws it.

**You will learn**
- How to show a text and describe its style
- How to align a text in a box, or label any node
- How to handle a text longer than its box
- How to mix styles on one line and keep a text up to date
- How to measure a text

## Show a text

A [[TextNode]] draws a [[Text]], which pairs a string with a [[TextInfo]], its style:

```java file="com/example/describing/text/GreetingUI.java" region="opener"
```

Created by `TextNode.create(x, y)`, without a size, the node takes the size of its text.
The style names a font that your application loads once: [Fonts](/use/learn/describing-the-ui/fonts/) shows how.

## Describe a style

[[TextInfo#create]] takes a font and a size in design pixels; each setter changes one more property:

```java file="com/example/describing/text/GreetingUI.java" region="styles"
```

<figure>

![A light grey card with Hello JOID in large bold dark grey letters, a grey sentence in regular weight below it, and a small spaced caption in capitals at the bottom](/captures/describing-text-styles.webp)

<figcaption>
Three styles from one font: a bold title, a body, and a smaller caption with spaced letters.
</figcaption>
</figure>

| Setter | Changes |
|---|---|
| `fontSize(float)` | The size, in design pixels. |
| `weight(...)` | The weight, a [[FontWeight]] from `THIN` (100) to `BLACK` (900): [Fonts](/use/learn/describing-the-ui/fonts/#let-a-style-pick-a-face) explains which face it draws. |
| `italic(boolean)` | The italic face, or the upright face slanted when the font has none. |
| `color(...)` | The color of the glyphs, a [[Color]] ([Colors and styling](/use/learn/style-and-motion/styling/)). |
| `letterSpacing(float)` | The space added between letters, on 1 of the size: `0.15F` adds 15 % of the size. |
| `lineHeight(float)` | The height of a line, on 1 of the size: `1.5F` is 150 %; `0F` keeps the height of the font. |
| `shadow()`, `shadow(Color)` | Adds a shadow under the text; `shadow()` follows the color of the text, 30 % darker, and `shadow(Color)` keeps a fixed color. |
| `shadow(float, float)` | Moves the shadow, in design pixels; by default it sits one 13.5th of the size to the right and below. |
| `shadowTint(Float)` | Draws the shadow in the color of each glyph multiplied by the tint (`0.3F` gives a dark shade of each color), for a text of several colors. |

The other properties, markup and effects, belong to [Text markup and effects](/use/learn/describing-the-ui/text-markup-and-effects/).

Each setter changes the `TextInfo` itself, and every text built on it follows.
Each one has a `with...` twin, such as [[TextInfo#withFontSize]], that leaves the style as it is and returns a changed copy: the caption above derives from the body without changing it.

:::caution
Changing a shared style with a setter, `body.fontSize(20F)`, changes every text that uses `body`.
Derive a variant with a `with...` method instead.
:::

## Align a text in a box

The alignment belongs to the `Text`: [[Text#alignX]] and [[Text#alignY]] take an [[Align]] (`START`, `CENTER` or `END`), and [[Text#align]] sets both.
Give the `TextNode` a size, and the text aligns inside it:

```java file="com/example/describing/text/AlignUI.java" region="align"
```

The text node covers the button, so "Play" stays at its center whatever the font.

Any node can also carry a label: [[Node#label]] draws a `Text` centered on the node, above what the node draws and below its children.

```java file="com/example/describing/text/AlignUI.java" region="label"
```

<figure>

![A light grey card with two white buttons, the word Play centered in the first and Settings centered in the second](/captures/describing-text-align.webp)

<figcaption>
A centered text node and a label give the same result.
A label needs no extra node, a text node lets you place the text anywhere in the box.
</figcaption>
</figure>

## Handle a long text

By default, a `TextNode` draws its text on one line and never cuts it.
A [[TextMode]], given to [[TextNode#mode]], changes that inside the width of the node:

```java file="com/example/describing/text/ModesUI.java" region="modes"
```

<figure>

![Four white boxes one line high with the same sentence. OVERFLOW: one line cut with an ellipsis at the edge of the box. SPLIT: the sentence wrapped on two lines, the second below the box. NORMAL: one line that goes beyond the box. BOX: wrapped, only the first line, which fits in the box, is drawn.](/captures/describing-text-modes.webp)

<figcaption>
The same sentence in four boxes of the same size: cut, wrapped and grown, untouched, or wrapped and clipped.
</figcaption>
</figure>

| Mode | What it draws |
|---|---|
| `NORMAL` | One line, never cut. A width or a height of `0` takes the size of the text. |
| `OVERFLOW` | One line, cut at the width of the node and ended with the mark of the `Text`. |
| `SPLIT` | As many lines as needed, broken at spaces, at `\n` or at `<br>`, and inside a word longer than the width; the height of the node follows the lines. |
| `BOX` | Like `SPLIT`, but only the lines that fit in the height of the node. |

The mark of a cut text is a [[TextOverflow]], given to [[Text#overflow]]: `NONE` (no mark), `ELLIPSIS` (`...`), `DOT` (`.`) or `HYPHEN` (`-`).

## Mix styles on one line

A `Text` is a line of runs, each a [[TextElement]] with its own style:

```java file="com/example/describing/text/RunsUI.java" region="runs"
```

The runs follow each other on one baseline, so a bold name sits in the line like a word of the sentence.
For a style inside one string, such as a bold word in a translated sentence, use markup instead ([Text markup and effects](/use/learn/describing-the-ui/text-markup-and-effects/#style-parts-of-a-string)).

## Keep a text up to date

`Text.create(...)` also takes a `Supplier` instead of a string.
JOID calls it each time it measures or draws the text, so the text follows your data:

```java file="com/example/describing/text/RunsUI.java" region="supplier"
```

<figure>

![A light grey card with Welcome back, Ada on its first line, Ada in bold, a white button that the pointer clicks three times, its text going from Clicked 0 times to Clicked 3 times, and a white box that fits the word Settings](/captures/describing-text-runs.webp)

<figcaption>
Two runs on one line, a text that counts the clicks of its button, and a box sized from its label.
</figcaption>
</figure>

A text can be any object, such as a number: JOID turns it into a string ([Convert your own types](/use/learn/describing-the-ui/text-markup-and-effects/#show-your-own-types-as-text)).
To make the whole `Text` of a node follow a signal, [Signals](/use/learn/interactivity/signals/) explains the reactive setters.

## Measure a text

A `TextInfo` measures a string in its style: [[TextInfo#getWidth]] gives its width, [[TextInfo#getHeight()]] the height of one line.
[[TextInfo#aw]] and [[TextInfo#ah(double)]] add a value to them, to size a box around a label:

```java file="com/example/describing/text/RunsUI.java" region="measure"
```

The box is the width of "Settings" plus 48 and the line height plus 24, so the label sits 24 design pixels from each side and 12 from the top.
A `Text` measures itself too, with [[Text#getWidth]] and [[Text#getHeight]].

## Recap

- A **`TextNode`** draws a **`Text`**, a string paired with a **`TextInfo`**; without a size, it takes the size of its text.
- A `TextInfo` holds the font, size, weight, color, spacing and shadow; its setters change it, its **`with...`** methods return a copy.
- The **alignment** belongs to the `Text`, inside the size of the node; **`label(...)`** centers a text on any node.
- **`TextMode`** chooses one line (`NORMAL`), a cut line (`OVERFLOW`), wrapped lines (`SPLIT`) or wrapped lines in a box (`BOX`).
- **`TextElement`** runs mix styles on one line; a `Supplier` keeps a text up to date.
- `TextInfo.getWidth(...)`, `aw(...)` and `ah(...)` **measure** a text to size what is around it.

## Try it

**A title bar.**
Center the word "Settings" in a 1920 × 100 bar at the top of the canvas.

<details>
<summary>Solution</summary>

Give the bar a label, the text is centered on it:

```java fragment
RectNode.create(0, 0, 1920, 100).color(Color.WHITE).label(Text.create("Settings", info)).attach(this);
```

</details>

**A two-line summary.**
Show the description of an item in a 400-wide box, wrapped, and never taller than two lines.

<details>
<summary>Solution</summary>

`BOX` draws the lines that fit in the height of the node: give it the height of two lines.

```java fragment
TextNode.create(0, 0, 400, info.getHeight() * 2D).text(Text.create(description, info)).mode(TextMode.BOX).attach(card);
```

</details>
