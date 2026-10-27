---
title: "Polish with effects and motion"
description: "Round the corners, add a shadow, show a hand pointer on the buttons and slide the panel in."
genre: tutorial
sidebar:
  order: 7
---

In this last lesson you give Starfall its finish: rounded corners, a soft shadow under the panel, a hand pointer over the buttons, and a panel that slides into place when the menu opens.

<figure>

![The profile panel of Starfall rises into place: a white panel with rounded corners and a soft shadow, with rounded statistic cards and the hint Press Play to start a round](/captures/tutorial-polish.webp)

<figcaption>
The result of this lesson: the same tree of nodes, drawn with effects and moved by an animator.
</figcaption>
</figure>

## Starting point

The program of [the previous lesson](/use/tutorial/settings/) works: Play fills the panel, and the settings survive a restart.
Every shape is a sharp rectangle, the panel appears at once, and nothing tells the player that the buttons are clickable.

## 1. Round the corners and add a shadow

An effect changes how a node is drawn, never where it sits or how it reacts.
Add a shade to `Theme`, the grey of the palette at 40 % opacity:

```java file="com/example/tutorial/polish/Theme.java" title="Theme.java" ins={17}
```

Then round the panel, and draw its shadow 12 design pixels lower, blurred over 40.
Add the two effects to the panel, after its color:

```java file="com/example/tutorial/polish/MenuUI.java" region="panel" title="MenuUI.java" ins={4-5}
```

[[RoundedNodeEffect]] rounds the corners, and [[ShadowNodeEffect]] follows the rounded shape: [Effects](/use/learn/style-and-motion/effects/#control-the-order-of-effects) explains how effects combine.
Round the cards with 16 in the same way:

```java file="com/example/tutorial/polish/MenuUI.java" region="grid" ins={8,18,28}
```

And round the rows of the list with 12:

```java file="com/example/tutorial/polish/MenuUI.java" region="rounds" ins={16}
```

**Run it.**
The panel, its cards and its rows have rounded corners, and a soft shadow lies under the panel.

## 2. Show a hand over the buttons

`cursor(...)` sets the pointer that the window shows while it is over a node.
Give each button the hand pointer, [[Cursor#POINTER]], and rounded corners:

```java file="com/example/tutorial/polish/MenuUI.java" region="buttons" ins={9-10,18-19,27-28}
```

Do the same for the buttons of `SettingsUI`, with a radius of 12, and round its card with 24 and the same shadow as the panel.

**Run it.**
Move the pointer over a button: it turns into a hand.
Open the settings: the card and its buttons are rounded too.

## 3. Slide the panel in

A [[TweenAnimator]] moves a value over time.
At the start of `init()`, create one that goes from 0 to 1 in 700 ms, fast at first and slow at the end, and start it:

```java file="com/example/tutorial/polish/MenuUI.java" region="intro"
```

Then let the panel read it.
Add this setter to the panel, after its effects:

```java file="com/example/tutorial/polish/MenuUI.java" region="slide"
```

The lambda runs on every frame, so the panel starts 80 design pixels lower and rises to its place as the value reaches 1.
`init()` creates a new animator each time it runs, so the panel slides in again at each opening of the menu, and at each [reload](/use/get-started/dev-tools/#reload-a-ui-while-you-work).

**Run it.**
The panel slides up into place when the menu opens.
Open the settings: the popup plays its own opening transition, which every popup has by default.

<details>
<summary>The complete <code>MenuUI.java</code></summary>

```java file="com/example/tutorial/polish/MenuUI.java"
```

</details>

<details>
<summary>The complete <code>SettingsUI.java</code></summary>

```java file="com/example/tutorial/polish/SettingsUI.java"
```

</details>

## Challenge

Slide the column of buttons in from the left with the same animator, while the panel rises.

<details>
<summary>Solution</summary>

Add the highlighted setter to the column of buttons in `MenuUI`, after its margin, and keep its body as it is:

```java file="com/example/tutorial/polish/ChallengeUI.java" region="solution" ins={4}
```

Both nodes read the same value, so they move together.
At 0 the column sits 520 design pixels to the left of its place, outside the canvas.

</details>

## What you learned

- An [effect](/use/learn/style-and-motion/effects/) changes how a node is drawn: rounded corners, shadows, and more.
- A [color](/use/learn/style-and-motion/styling/) carries its own opacity.
- `cursor(...)` sets the pointer over a node ([Hover, tooltips and cursors](/use/learn/interactivity/hover-and-tooltips/#change-the-cursor-over-a-node)).
- A `TweenAnimator` moves a value over time, and a setter that reads it in a lambda follows it on every frame ([Animation](/use/learn/style-and-motion/animation/)).
- Popups play an opening and a closing [transition](/use/learn/style-and-motion/transitions/).

## Where to go next

You have used most of what JOID offers.
Read [How JOID works](/use/learn/how-joid-thinks/), then follow the Learn chapters to understand each tool in full, or browse the [Guides](/use/guides/) for precise tasks.
