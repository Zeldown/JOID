---
title: "Grid layout"
description: "Fill rows of cells that wrap at a width with GridNode: gaps, row alignment, height and scrolling."
genre: learn
sidebar:
  order: 9
---

Inventories, galleries and color palettes show many items in rows that wrap.
A [[GridNode]] fills a row from left to right, starts the next row when the width is full, and grows to hold its rows.

**You will learn**
- How to fill rows of cells that wrap at the width of the grid
- How to space the cells on each axis
- How to align each row, and how rows of different heights stack
- How a grid takes its height, and how to make it scroll

## Fill rows that wrap

[[GridNode#create]] takes a position and a size; its children are its cells:

```java file="com/example/describing/grid/GridCasesUI.java" region="wrap"
```

<figure>

![On a light grey card, nine grey squares: two full rows of four, then a single square on a third row, all aligned to the left](/captures/describing-grid-wrap.webp)

<figcaption>
Four squares and their gaps fit in 360 design pixels: the fifth starts the second row.
</figcaption>
</figure>

A cell goes on the current row while the row, gaps included, stays within the width of the grid; otherwise it starts a new row.
A cell wider than the grid gets a row of its own.
The cells keep their own size, and take their places in the order you attach them, again on every frame: the grid is a [layout node](/use/learn/describing-the-ui/layout/#know-who-places-a-child).

## Space the cells

[[GridNode#margin]] sets the same gap on both axes.
[[GridNode#marginHorizontal]] sets the gap between two cells of a row, [[GridNode#marginVertical]] the gap between two rows:

```java file="com/example/describing/grid/GridCasesUI.java" region="margins"
```

<figure>

![On a light grey card, two rows of four grey squares, with wide gaps between the squares of a row and a thin gap between the rows](/captures/describing-grid-margins.webp)

<figcaption>
20 design pixels between the cells of a row, 5 between the rows.
</figcaption>
</figure>

There is no gap before the first cell nor after the last one of a row.

## Align each row

[[GridNode#alignX]] takes an [[Align]] and places each row on the width of the grid, on its own width:

```java file="com/example/describing/grid/GridCasesUI.java" region="align"
```

<figure>

![On a light grey card, a full row of four grey squares, and below it a row of two squares centered under them](/captures/describing-grid-align.webp)

<figcaption>
`Align.CENTER` centers the full row and the short last row each on its own width.
</figcaption>
</figure>

`Align.START`, the default, puts every row against the left edge, and `Align.END` against the right edge.
The own `x` and `y` of a cell add to its place after the alignment, as in every [layout node](/use/learn/describing-the-ui/layout/#know-who-places-a-child).

## Mix cells of different sizes

A row is as high as its tallest cell, and the next row starts below it:

```java file="com/example/describing/grid/GridCasesUI.java" region="rows"
```

<figure>

![On a light grey card, a first row of three cells of the same width and different heights, the white one tallest, then a second row of two wider grey cells that starts below the white cell](/captures/describing-grid-rows.webp)

<figcaption>
The white cell sets the height of the first row; the cells of a row are aligned at its top.
</figcaption>
</figure>

## Let the grid take its height

A grid created with a height of 0 takes the height of its rows, gaps included, and keeps the width you gave it.
A height that you give follows the [rule of the layout nodes](/use/learn/describing-the-ui/layout/#scroll-a-node), here a fixed height with an overflow, which lets the rows scroll.

```java file="com/example/describing/grid/GridCasesUI.java" region="scroll"
```

<figure>

![On a light grey card, rows of grey and white squares cut off near the bottom of the card; the wheel moves them up so that the next rows come into view, then moves them back down](/captures/describing-grid-scroll.webp)

<figcaption>
The grid stays 260 design pixels high, and its five rows scroll inside it.
</figcaption>
</figure>

A hidden, removed or `ABSOLUTE` child gets no cell ([Know who places a child](/use/learn/describing-the-ui/layout/#know-who-places-a-child)), and the next cells move up from the next frame.

## Recap

- **`GridNode.create(x, y, width, height)`** places its children in rows, in attach order, and **wraps** at its width.
- **`margin`** spaces the cells on both axes; **`marginHorizontal`** and **`marginVertical`** on one axis.
- **`alignX`** places each row on the width of the grid.
- A row is as high as its **tallest cell**.
- A height of **0** follows the rows; a declared height follows the **rule of the layout nodes**.

## Try it

**A palette.**
Build a grid of 16 white squares of 40 design pixels, four per row, 8 design pixels apart.

<details>
<summary>Solution</summary>

Four squares and three gaps take 4 × 40 + 3 × 8 = 184 design pixels: a grid of that width wraps after the fourth square.

```java file="com/example/describing/grid/PaletteUI.java" region="palette"
```

</details>

**Right to left.**
Make the last row of the `align` example sit against the right edge.

<details>
<summary>Solution</summary>

Replace `Align.CENTER` by `Align.END`: each row goes against the right edge, the full row included, which moves by 10 design pixels since it is 350 wide.

</details>
