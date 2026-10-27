---
title: "Keep the state"
description: "Hold the rounds of the player in a signal, and let the profile panel follow it."
genre: tutorial
sidebar:
  order: 5
---

In this lesson Play starts a round, and the whole panel follows: the number of games, the best score, the total and the list of recent rounds.

<figure>

![The menu and its profile panel, with no round yet; Play starts three rounds, and at each round the cards Games, Best and Total and the list of recent rounds follow, up to Round 3, Round 2 and Round 1 with their points](/captures/tutorial-state.webp)

<figcaption>
The result of this lesson: no line of code updates the panel, it follows the rounds.
</figcaption>
</figure>

## Starting point

The program of [the previous lesson](/use/tutorial/interaction/) reacts to the mouse and the keyboard, but Play does nothing.
The numbers of the panel are written in the code, so they can never change.

## 1. Hold the rounds in a signal

Add these fields to `MenuUI`:

```java file="com/example/tutorial/state/MenuUI.java" region="fields" title="MenuUI.java"
```

- `rounds` is a [[ListSignal]]: a list that tells JOID when it changes.
- `total` and `best` are derived from `rounds` with `map(...)`: they compute their value from the list, and compute it again when the list changes.
- `random` draws the points of a round.
  Its fixed seed gives the same rounds at every run, so your panel shows the numbers of the capture above.

Nothing reads these fields yet, so the menu looks the same.

## 2. Start a round on Play

Give Play an `onClick` that adds a round of 100 to 999 points:

```java file="com/example/tutorial/state/MenuUI.java" region="play"
```

Play now adds rounds, but nothing shows them yet: the panel still has fixed texts.

## 3. Show the numbers from the signals

Build the value of each card from the signals instead of a fixed text:

```java file="com/example/tutorial/state/MenuUI.java" region="grid" ins={11,20,29}
```

[[Text]]`.create(String.valueOf(this.best.get()), value)` reads the signal `best`.
JOID remembers that this text depends on it, and builds the text again each time `best` changes: [Signals](/use/learn/interactivity/signals/) explains how.

**Run it.**
The three cards start at 0.
Click Play: they update at each click, while the list still shows its fixed rows.

## 4. Rebuild the list of rounds

A list does not hold one value: it holds as many rows as there are rounds.
`watch(...)` rebuilds the children of a node when a signal changes:

```java file="com/example/tutorial/state/MenuUI.java" region="rounds" ins={4,6-9,11-12}
```

- [[WatchProperty#CLEAR_CHILDREN]] removes the rows, then [[WatchProperty#BODY]] runs the body again.
- The body reads the list with `peek()`, which reads without following: the `watch` already runs the body again at each change ([Read a value without following it](/use/learn/interactivity/signals/#read-a-value-without-following-it)).
- It shows the four latest rounds, the newest first, or a hint when there is none.

**Run it.**
Click Play a few times: each click adds a row at the top of the list and updates the three cards.

Close the program and run it again: the rounds are gone, since they live in memory only.
Open the settings: the music and the volume are still fixed texts.
The next lesson stores the settings.

<details>
<summary>The complete <code>MenuUI.java</code></summary>

```java file="com/example/tutorial/state/MenuUI.java"
```

</details>

## Challenge

Let the player start a round with the P key.

<details>
<summary>Solution</summary>

Add a second shortcut next to the one of S:

```java file="com/example/tutorial/state/ChallengeUI.java" region="solution"
```

The shortcut changes the same signal as the button, so the panel follows in the same way: nothing else to write.

</details>

## What you learned

- A [signal](/use/learn/interactivity/signals/) holds a value, and a node set from an expression that reads it follows it.
- `map(...)` derives a signal from another ([Derived values](/use/learn/state/derived-values/)).
- `watch(...)` rebuilds the children of a node when a signal changes ([Rebuilding the tree](/use/learn/state/rebuilding/)).
- You change the data, never the nodes.
