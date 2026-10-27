---
title: "Make a modal popup"
description: "Open a confirmation popup that dims the screen, blocks it and runs an action on confirm."
genre: guide
sidebar:
  order: 1
---

A modal popup asks a question and keeps the user on it until they answer.
This guide builds a confirmation popup that dims the screen behind it, blocks it, and runs an action when the user confirms.

## What you will build

<figure>

![A dimmed screen behind a light grey card that asks Delete the file?, with a Cancel and a Delete button](/captures/guides-popup-open.webp)

<figcaption>
The popup over the screen that opened it.
The screen behind is dimmed and receives no input.
</figcaption>
</figure>

## Before you start

- A UI that opens on a backend: [UIs](/use/learn/describing-the-ui/uis/).
- What a popup is and how UIs stack: [Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/).
- Click callbacks: [Events](/use/learn/interactivity/events/).

## 1. Declare the popup

A popup is a [[UI]] with two annotations: [[UIDataPopup]] makes it a popup, and `background = true` of [[UIData]] draws the dimmed backdrop behind it.
The popup takes the action to run in its constructor, so the screen that opens it decides what "Delete" does:

```java file="com/example/guides/PopupGuideConfirmPopup.java" region="data"
```

Give it an empty `init()` for now.

**Check:** the class compiles.

## 2. Open it from the screen

The screen shows a file name held in a [[StringSignal]].
Its Delete button opens the popup with [[JOID#open]] and passes the action, which clears the name:

```java file="com/example/guides/PopupGuideUI.java" region="open"
```

**Check:** click Delete.
The screen dims behind the empty popup, and its button ignores the mouse; `Escape` closes the popup.

## 3. Draw the question and the answers

Build the card in `init()`, as in any UI.
Each button closes the popup with [[JOID#close]]; the Delete button runs the action first:

```java file="com/example/guides/PopupGuideConfirmPopup.java" region="card"
```

`JOID.close(this)` plays the closing transition of the popup, then removes it.

**Check:** click Delete on the screen.
The popup opens with its transition; a click outside the card does nothing; Cancel closes it; Delete changes the file name to `No file`, then closes it.

## 4. Force an answer

Add `closeable = false` to the `@UIData` of the popup: `Escape` then leaves it open, and `JOID.close` still closes it.
The color of the backdrop is `backgroundColor` of the same annotation ([Configure a UI](/use/learn/describing-the-ui/uis/#configure-a-ui)), and the opening and closing animations are the `transition` of `@UIDataPopup` ([Transitions](/use/learn/style-and-motion/transitions/)).

**Check:** with `closeable = false`, `Escape` leaves the popup open.

## If it does not work

1. The popup replaces the screen instead of opening over it: the class misses `@UIDataPopup(active = true)`.
2. The screen behind is not dimmed: the class misses `@UIData(background = true)`.
   A popup blocks the input of the UIs under it either way.
3. `JOID.open` throws `IllegalStateException: No IUIBridge can open …`: no backend is registered yet.
   Open the popup from a callback, or see [Run code once JOID is ready](/use/learn/running/backends/#run-code-once-joid-is-ready).
4. `Escape` does not close the popup: `closeable` is `false`, or a node of the popup, such as a focused text field, consumed the key ([Know who receives the keyboard](/use/learn/interactivity/keyboard/#know-who-receives-the-keyboard)).
5. The popup disappears without its closing animation: it was closed with [[JOID#forceClose]], which skips the transition.

## Reference

- [[UIDataPopup]]
- [[UIData]]
- [[JOID]]
- [[UI]]
