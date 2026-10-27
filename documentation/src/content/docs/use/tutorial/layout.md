---
title: "Lay out the profile panel"
description: "Stack the menu buttons with a flex layout, and build the profile panel with a grid and a list."
genre: tutorial
sidebar:
  order: 3
---

In this lesson a layout places the menu buttons for you, then you build the profile panel on the right: three statistic cards and a list of recent rounds.

<figure>

![A white panel titled Profile with three light grey cards Games 3, Best 640 and Total 1370, and a list of three rounds under the heading Recent rounds](/captures/tutorial-layout.webp)

<figcaption>
The result of this lesson: no position inside the column, the grid or the list is computed by hand.
</figcaption>
</figure>

## Starting point

The program of [the previous lesson](/use/tutorial/main-menu/) places each button by hand, 100 design pixels apart.
Inserting a button means moving every button below it, and the right half of the window is empty.

## 1. Stack the buttons in a column

Replace the three buttons with a [[FlexNode]] that holds them:

```java file="com/example/tutorial/layout/MenuUI.java" region="buttons" title="MenuUI.java"
```

- `FlexNode.vertical(160, 420, 360)` is a column 360 design pixels wide at (160, 420); it grows to the height of its children.
- `margin(20D)` leaves 20 design pixels between two children.
- Each button is now created at (0, 0) and attached to `flex`: the column places them one under the other, in the order you attach them.

**Run it.**
The menu looks the same as before, but you can insert a button anywhere by adding one line.

## 2. Add the profile panel

Add a light grey, `PAPER`, to the palette of `Theme`, for the cards and the rows:

```java file="com/example/tutorial/layout/Theme.java" title="Theme.java" ins={15}
```

The panel needs three more styles: a heading, a caption and a large value.
Declare them at the top of `init()`, with the other styles:

```java file="com/example/tutorial/layout/MenuUI.java" region="styles"
```

The panel is a white rectangle on the right half of the canvas.
Add it after the column:

```java file="com/example/tutorial/layout/MenuUI.java" region="panel"
```

- `body(...)` creates the children inside their parent, which the lambda receives ([Nodes and the tree](/use/learn/describing-the-ui/nodes/)).
- A [[GridNode]], `GridNode.create(48, 120, 904, 150)`, places its children in rows, from left to right, and starts a new row when the next child does not fit in its 904 design pixels.
  Three cards of 280 with two gaps of 32 fill one row exactly.
- The recent rounds are a second column: a `FlexNode.vertical` whose rows are 72 design pixels high, 12 apart.
  A plain Java loop creates the rows, one per entry of the array.
- In each row, the points sit against the right edge: `row.aw(-24)` is the width of the row minus 24, and [[Align]]`.END`, given to `anchorX(...)`, puts the end of the text on that point.
  `row.dh(2)` is half the height of the row, and `anchorY(Align.CENTER)` centers each text on it vertically ([Position and size](/use/learn/describing-the-ui/position-and-size/)).

**Run it.**
You should see the panel with its three cards and its three rounds.

The numbers are written in the code: Play does nothing and the panel never changes.
Lesson 5 connects them to real data, after lesson 4 makes the buttons react.

<details>
<summary>The complete <code>MenuUI.java</code></summary>

```java file="com/example/tutorial/layout/MenuUI.java"
```

</details>

## Challenge

Add a Credits button between Settings and Quit, and space the buttons 40 design pixels apart.

<details>
<summary>Solution</summary>

```java file="com/example/tutorial/layout/ChallengeUI.java" region="solution" ins={3,7}
```

One setter changes every gap, and the new button takes its place by being attached third.

</details>

## What you learned

- A [layout node](/use/learn/describing-the-ui/layout/) places its children, so they are created at (0, 0).
- A [flex layout](/use/learn/describing-the-ui/flex-layout/) stacks its children in a row or a column, with a margin between them.
- A [grid layout](/use/learn/describing-the-ui/grid-layout/) fills rows and wraps to the next one.
- `aw(...)` and anchors place a node against the edge of its parent ([Position and size](/use/learn/describing-the-ui/position-and-size/)).
