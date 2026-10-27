---
title: "Flex layout"
description: "Line children up in a column or a row with FlexNode: gaps, alignment, size and scrolling."
genre: learn
sidebar:
  order: 8
---

Lists, menus and toolbars put their items one after the other.
A [[FlexNode]] does it for you: it stacks its children in a column or a row on every frame, and grows to hold them.

**You will learn**
- How to stack children in a column or a row, with a gap between them
- How to align children across the line, or shift one of them
- How a flex node takes its size, and how to make it scroll
- How hidden and removed children leave the line
- How to nest rows inside a column

## Stack children in a column

[[FlexNode#vertical]] creates a column from its position and its width:

```java file="com/example/describing/flex/FlexCasesUI.java" region="column"
```

<figure>

![On a light grey card, four grey bars of different widths, stacked from the top with an equal gap between them](/captures/describing-flex-column.webp)

<figcaption>
Every child is created at (0, 0): the column gives each one its place, in the order you attach them.
</figcaption>
</figure>

[[FlexNode#margin]] is the gap between two children, in design pixels; there is none before the first child nor after the last.
The column is a [layout node](/use/learn/describing-the-ui/layout/#know-who-places-a-child): it places its children again on every frame, so a child that grows pushes the next ones down.

## Line children up in a row

[[FlexNode#horizontal]] creates a row from its position and its height:

```java file="com/example/describing/flex/FlexCasesUI.java" region="row"
```

<figure>

![On a light grey card, four grey bars of different widths and heights, side by side from the left, all touching the top](/captures/describing-flex-row.webp)

<figcaption>
A row works like a column turned on its side: the children follow each other from left to right.
</figcaption>
</figure>

[[FlexNode#direction]] turns a column into a row or back, with an [[Axis]]: `.direction(Axis.HORIZONTAL)`.

## Align children across the line

[[FlexNode#align]] takes an [[Align]] and places every child across the line: on the width of a column, on the height of a row.

```java file="com/example/describing/flex/FlexCasesUI.java" region="align"
```

<figure>

![On a light grey card, four grey bars of different widths, stacked and centered on the width of the card](/captures/describing-flex-align.webp)

<figcaption>
`Align.CENTER` centers each bar on the 360 design pixels of the column.
</figcaption>
</figure>

`Align.START` puts the children against the left of a column or the top of a row, `Align.END` against the right or the bottom.
Without `align`, the default, each child keeps its own position across the line: its `x` in a column, its `y` in a row.

## Shift one child

The own position of a child adds to the place that the flex node gives it:

```java file="com/example/describing/flex/FlexCasesUI.java" region="offset"
```

<figure>

![On a light grey card, three bars stacked: the white middle bar sits further right than the grey ones, and the gap above it is larger than the gap below it](/captures/describing-flex-offset.webp)

<figcaption>
The white bar, created at (40, 30), moves 40 design pixels right and gets 30 more above it.
</figcaption>
</figure>

Along the line, the offset of a child is extra space before it: the children after it move too.
Across the line, the offset applies only while `align` is not set: an alignment places the child exactly.

## Let the flex node take its size

A flex node created with a length of 0 along its line takes the length of its content: a column as high as its children and their gaps, a row as wide.
Here a white panel reads the height of the column to wrap it:

```java file="com/example/describing/flex/FlexCasesUI.java" region="grow"
```

<figure>

![On a light grey card, a white panel that wraps three grey bars with an even white margin around them](/captures/describing-flex-grow.webp)

<figcaption>
The column measures 170 design pixels, three bars and two gaps, and the panel follows it.
</figcaption>
</figure>

A length that you give with [[Node#height]] on a column, or [[Node#width]] on a row, is a minimum: the flex node keeps it while its children fit, and grows past it when they need more room.
A flex node with no child measures 0 along its line.

## Scroll a long list

A declared length with an [[OverflowProperty]] other than `NONE` is a fixed length: the flex node keeps it whatever its content, and the content scrolls or is clipped ([Layout](/use/learn/describing-the-ui/layout/#scroll-a-node)).

```java file="com/example/describing/flex/FlexCasesUI.java" region="scroll"
```

<figure>

![On a light grey card, a column of grey and white bars cut off near the bottom of the card; the wheel moves them up and the next bars come into view](/captures/describing-flex-scroll.webp)

<figcaption>
The column stays 260 design pixels high, and its eight bars scroll inside it.
</figcaption>
</figure>

:::caution
A flex node created with a length of 0 keeps growing with its content even with `OverflowProperty.SCROLL`, so it never scrolls.
Give it a length along its line, such as `.height(260)` on a column.
:::

## Hide or remove a child

A child that is not visible, `visible(false)`, gets no place in the line: the next children close the gap, and open it again when it shows.
A removed child leaves the line the same way, and an appended child takes its place at the end, both from the next frame.
A child whose position is `ABSOLUTE` stays out of the line, where you put it ([Position and size](/use/learn/describing-the-ui/position-and-size/)).

## Nest rows inside a column

A flex node is a node like any other: a column of rows builds a list whose lines hold several items.

```java file="com/example/describing/flex/SettingsListUI.java" region="list"
```

<figure>

![On a light grey card, three lines stacked from the top: each line is a white disc followed by a grey label, Sound, Music and Subtitles, centered on the disc](/captures/describing-flex-list.webp)

<figcaption>
Each row centers its disc and its label on its 90 design pixels; the column stacks the rows.
</figcaption>
</figure>

Each row takes the width of its content, and the column gives the rows their place.
A row that the user can drag into another order is a [[ReorderableFlexNode]] ([Reorderable lists](/use/learn/interactivity/reorderable-lists/)).

## Recap

- **`FlexNode.vertical(x, y, width)`** stacks children in a column, **`FlexNode.horizontal(x, y, height)`** in a row, in attach order.
- **`margin`** is the gap between two children.
- **`align`** places every child across the line; without it, each child keeps its own cross position.
- The own **offset** of a child adds to its place; along the line, it pushes the next children.
- A length of **0** follows the content; a declared length is a **minimum**; a declared length with an **overflow** is fixed.
- Hidden, removed and `ABSOLUTE` children **leave no gap**.

## Try it

**A toolbar.**
Build a row of five white squares of 60 design pixels, 15 design pixels apart, centered on a row 100 design pixels high.

<details>
<summary>Solution</summary>

The row is 100 high; `align(Align.CENTER)` centers each square on that height:

```java fragment
FlexNode.horizontal(20, 20, 100).margin(15D).align(Align.CENTER).body(flex -> {
	for (int i = 0; i < 5; i++) {
		RectNode.create(0, 0, 60, 60).color(Color.WHITE).attach(flex);
	}
}).attach(card);
```

</details>

**Indent one line.**
In `SettingsListUI`, indent the line Music by 40 design pixels without changing the other lines.

<details>
<summary>Solution</summary>

The column has no `align`, so the own `x` of a row stays: create the Music row at `x = 40`, for example with `FlexNode.horizontal(name.equals("Music") ? 40 : 0, 0, 90)`.

</details>
