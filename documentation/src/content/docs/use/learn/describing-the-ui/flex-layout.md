---
title: "Flex layout"
description: "Line children up in a column or a row with FlexNode: gaps, alignment and size."
genre: learn
sidebar:
  order: 8
---

Lists, menus and toolbars put their items one after the other.
A [[FlexNode]] does it for you: it stacks its children in a column or a row on every frame, and grows to hold them.

**You will learn**
- How to stack children in a column or a row, with a gap between them
- How to align children across the line, or shift one of them
- How a flex node takes its size
- How to nest rows inside a column

## Stack children in a column

[[FlexNode#vertical]] creates a column from its position and its width:

```java file="com/example/describing/flex/FlexCasesUI.java" region="column"
```

<figure>

![On a dark card, four blue bars of different widths, stacked from the top with an equal gap between them](/captures/describing-flex-column.webp)

<figcaption>
Every child is created at (0, 0): the column gives each one its place, in the order you attach them.
</figcaption>
</figure>

[[FlexNode#margin]] is the gap between two children, in design pixels; there is none before the first child nor after the last.
The column is a [layout node](/use/learn/describing-the-ui/layout/#know-who-places-a-child): it places its children again on every frame, so a child that grows pushes the next ones down.
A hidden, removed or `ABSOLUTE` child gets no place in the line ([Know who places a child](/use/learn/describing-the-ui/layout/#know-who-places-a-child)), and the next children close the gap from the next frame.

## Line children up in a row

[[FlexNode#horizontal]] creates a row from its position and its height:

```java file="com/example/describing/flex/FlexCasesUI.java" region="row"
```

<figure>

![On a dark card, four blue bars of different widths and heights, side by side from the left, all touching the top](/captures/describing-flex-row.webp)

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

![On a dark card, four blue bars of different widths, stacked and centered on the width of the card](/captures/describing-flex-align.webp)

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

![Three bars in a column on a dark card: the blue middle bar sits 40 design pixels right of the dark grey ones and 30 lower than its place, and the dark grey bar below it, which keeps its place, overlaps it](/captures/describing-flex-offset.webp)

<figcaption>
The blue bar, created at (40, 30), moves 40 design pixels right and 30 down, and the bar after it does not move.
</figcaption>
</figure>

Along the line, the offset moves the child alone: the next children keep their place, so an offset larger than the margin makes the child overlap the next one.
Across the line, the offset applies only while `align` is not set: an alignment places the child exactly.

## Let the flex node take its size

A flex node created with a length of 0 along its line takes the length of its content: a column as high as its children and their gaps, a row as wide.
Here a dark grey panel reads the height of the column to wrap it:

```java file="com/example/describing/flex/FlexCasesUI.java" region="grow"
```

<figure>

![On a dark card, a dark grey panel that wraps three blue bars with an even margin around them](/captures/describing-flex-grow.webp)

<figcaption>
The column measures 170 design pixels, three bars and two gaps, and the panel follows it.
</figcaption>
</figure>

The panel keeps wrapping the column when bars appear or disappear:

<figure>

![On a dark card, a dark grey panel wraps a column of blue bars, next to two blue buttons, Add a bar and Remove a bar; the pointer clicks Add a bar twice, then Remove a bar twice: the column goes from two bars to four and back to two, and the panel grows and shrinks with it](/captures/describing-flex-flow.webp)

<figcaption>
A hidden bar leaves the column, and the panel, which reads the height of the column, follows it.
</figcaption>
</figure>

A length that you give with [[Node#height]] on a column, or [[Node#width]] on a row, follows the [rule of the layout nodes](/use/learn/describing-the-ui/layout/#scroll-a-node): with an [[OverflowProperty]] other than `NONE`, it stays fixed and the children scroll inside it.

## Nest rows inside a column

A flex node is a node like any other: a column of rows builds a list whose lines hold several items.

```java file="com/example/describing/flex/SettingsListUI.java" region="list"
```

<figure>

![On a dark card, three lines stacked from the top: each line is a blue disc followed by a light label, Sound, Music and Subtitles, centered on the disc](/captures/describing-flex-list.webp)

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
- The own **offset** of a child adds to its place; along the line, it moves that child only.
- A length of **0** follows the content; a declared length follows the **rule of the layout nodes**.

## Try it

**A toolbar.**
Build a row of five blue squares of 60 design pixels, 15 design pixels apart, centered on a row 100 design pixels high.

<details>
<summary>Solution</summary>

The row is 100 high; `align(Align.CENTER)` centers each square on that height:

```java file="com/example/describing/flex/ToolbarUI.java" region="toolbar"
```

</details>

**Indent one line.**
In `SettingsListUI`, indent the line Music by 40 design pixels without changing the other lines.

<details>
<summary>Solution</summary>

The column has no `align`, so the own `x` of a row stays: create the Music row at `x = 40`, for example with `FlexNode.horizontal(name.equals("Music") ? 40 : 0, 0, 90)`.

</details>
