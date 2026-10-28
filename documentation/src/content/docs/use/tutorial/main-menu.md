---
title: "Build the main menu"
description: "Place the title, the tagline and the buttons of the main menu on the design canvas."
genre: tutorial
sidebar:
  order: 2
---

In this lesson you move the title to the top-left corner and add the three buttons of the menu: Play, Settings and Quit.

<figure>

![The Starfall title and the tagline Catch the falling stars at the top left of a dark window, above three dark grey buttons labelled Play, Settings and Quit](/captures/tutorial-menu.webp)

<figcaption>
The result of this lesson: each button is a rectangle with a label.
</figcaption>
</figure>

## Starting point

The program of [the previous lesson](/use/tutorial/setup/) shows the title alone at the center.
The menu has no button, and nothing tells the player what the game is.

## 1. Move the title and add a tagline

The tagline and the labels of the buttons need two more styles.
Declare them next to `title`, at the top of `init()`:

```java file="com/example/tutorial/menu/MenuUI.java" region="styles"
```

Then replace the title with two texts at the top left of the canvas:

```java file="com/example/tutorial/menu/MenuUI.java" region="title"
```

Without an anchor, the position of a node is its top-left corner.
You copy these coordinates from a 1920 × 1080 mockup: (160, 160) is 160 design pixels from the left edge and from the top edge, at any window size.

**Run it.**
You should see the title at the top left, and the tagline under it.

## 2. Add the Play button

A button is a rectangle with a label.
Add it after the tagline:

```java file="com/example/tutorial/menu/MenuUI.java" region="play"
```

- [[RectNode]]`.create(160, 420, 360, 80)` creates a 360 × 80 rectangle with its top-left corner at (160, 420).
- `color(...)` fills it with `ELEMENT`, the dark grey of `Theme`.
- [[Node#label]] draws a text centered on the node, so the button needs no extra node for its text.

**Run it.**
You should see a dark grey Play button under the tagline.

## 3. Add Settings and Quit

Copy the button twice, 100 design pixels lower each time:

```java file="com/example/tutorial/menu/MenuUI.java" region="buttons" ins={7-11,13-17}
```

**Run it.**
You should see the title, the tagline and three dark grey buttons in a column on the left.

Each button is placed by hand: to insert a button between two others, you would move every button below it.
The right half of the window is empty, and the buttons do nothing yet.
The next lesson solves the first two problems.

<details>
<summary>The complete <code>MenuUI.java</code></summary>

```java file="com/example/tutorial/menu/MenuUI.java"
```

</details>

## Challenge

Add a fourth button, Credits, under Quit.

<details>
<summary>Solution</summary>

```java file="com/example/tutorial/menu/ChallengeUI.java" region="solution"
```

Quit ends at y = 700, so Credits starts at 720 to keep the gap of 20 design pixels.
You computed that position yourself: the next lesson lets a layout compute it.

</details>

## What you learned

- A [node](/use/learn/describing-the-ui/nodes/) is created by a `create(...)` factory, set up by chained setters, and attached to its parent.
- The position of a node is its top-left corner, unless an anchor moves it ([Position and size](/use/learn/describing-the-ui/position-and-size/)).
- A label centers a text on any node ([Text](/use/learn/describing-the-ui/text/#align-a-text-in-a-box)).
- Coordinates come straight from a 1920 × 1080 mockup ([The design canvas](/use/learn/describing-the-ui/design-canvas/)).
