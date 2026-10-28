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

![A dark card with Hello JOID in large bold light letters, a grey sentence in regular weight below it, and a small spaced caption in capitals at the bottom](/captures/describing-text-styles.webp)

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
| `letterSpacing(float)` | The space added between letters, as a share of the size (`1F` = 100 %): `0.15F` adds 15 % of the size. |
| `lineHeight(float)` | The height of a line, as a share of the size (`1F` = 100 %): `1.5F` is 150 %; `0F` keeps the height of the font. |
| `shadow()`, `shadow(Color)` | Adds a shadow under the text; `shadow()` follows the color of the text, 30 % darker, and `shadow(Color)` keeps a fixed color. |

[[TextInfo]] lists the other settings, such as the offset and the tint of the shadow.
The markup and the effects belong to [Text markup and effects](/use/learn/describing-the-ui/text-markup-and-effects/).

Each setter changes the `TextInfo` itself, and every text built on it follows.
Each one has a `with...` twin, such as [[TextInfo#withFontSize]], that leaves the style as it is and returns a changed copy: the caption above derives from the body without changing it.

:::caution
Changing a shared style with a setter, `body.fontSize(20F)`, changes every text that uses `body`.
Derive a variant with a `with...` method instead.
:::

[[LockableValue#lock]] makes a shared style safe: its setters then throw an `IllegalStateException`, and its `with...` copies still work ([Keep a palette safe](/use/learn/style-and-motion/styling/#keep-a-palette-safe)).

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

![A dark card with two dark grey buttons, the word Play centered in the first and Settings centered in the second](/captures/describing-text-align.webp)

<figcaption>
To center a text on a node, `label(...)` needs no extra node.
A text node in the box places the text anywhere else in it, with the alignments of the `Text`.
</figcaption>
</figure>

## Handle a long text

By default, a `TextNode` draws its text on one line and never cuts it.
A [[TextMode]], given to [[TextNode#mode]], changes that inside the width of the node:

```java file="com/example/describing/text/ModesUI.java" region="modes"
```

<figure>

![Four dark grey boxes one line high with the same sentence: on the first row, OVERFLOW, one line cut with an ellipsis at the edge of the box, and SPLIT, the sentence wrapped on two lines, the second below the box; on the second row, BOX, wrapped, only the first line, which fits in the box, is drawn; on the third row, NORMAL, one line that goes beyond the box.](/captures/describing-text-modes.webp)

<figcaption>
The same sentence in four boxes of the same size: cut, wrapped onto a second line below the box, wrapped and clipped, or untouched.
</figcaption>
</figure>

| Mode | What it draws |
|---|---|
| `NORMAL` | One line, never cut. A width or a height of `0` takes the size of the text. |
| `OVERFLOW` | One line, cut at the width of the node and ended with the mark of the `Text`. |
| `SPLIT` | As many lines as needed, broken at spaces, at `\n` or at `<br>`, and inside a word longer than the width; the height of the node follows the lines. |
| `BOX` | Like `SPLIT`, but only the lines that fit in the height of the node. |

<figure>

![The same four boxes and sentence, whose width shrinks to about half and grows back: the ellipsis of OVERFLOW follows the edge of its box, SPLIT wraps onto more lines below its box, BOX keeps only its first line, and NORMAL stays on one line beyond its box](/captures/describing-text-flow.webp)

<figcaption>
Each mode works from the current width of the node: a box that changes width cuts or wraps its text again.
</figcaption>
</figure>

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

![A dark card with Welcome back, Ada on its first line, Ada in bold and blue, and a dark grey button that the pointer clicks three times, its text going from Clicks: 0 to Clicks: 3](/captures/describing-text-runs.webp)

<figcaption>
Two runs on one line, and a text that counts the clicks of its button.
</figcaption>
</figure>

A text can be any object, such as a number: JOID turns it into a string ([Convert your own types](/use/learn/describing-the-ui/text-markup-and-effects/#show-your-own-types-as-text)).
To make the whole `Text` of a node follow a signal, [Signals](/use/learn/interactivity/signals/) explains the reactive setters.

## Measure a text

A `TextInfo` measures a string in its style: [[TextInfo#getWidth]] gives its width, [[TextInfo#getHeight()]] the height of one line.
[[TextInfo#aw]] and [[TextInfo#ah(double)]] add a value to them, to size a box around a label:

```java file="com/example/describing/text/RunsUI.java" region="measure"
```

<figure>

![A dark card with a dark grey box sized to the word Settings, with the same space on its left and on its right](/captures/describing-text-measure.webp)

<figcaption>
Measured from its label, the box fits the word in any font and at any size.
</figcaption>
</figure>

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
RectNode.create(0, 0, 1920, 100).color(GreetingUI.ELEMENT).label(Text.create("Settings", info)).attach(this);
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
