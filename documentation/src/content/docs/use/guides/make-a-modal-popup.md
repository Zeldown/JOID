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

![A dark screen with a file name and a blue Delete button; the pointer clicks Delete, the screen dims behind a dark card that asks Delete the file? with a blue Cancel and a blue Delete button, and a click on Cancel closes it](/captures/guides-popup-open.webp)

<figcaption>
The popup over the screen that opened it.
The screen behind is dimmed and receives no input.
</figcaption>
</figure>

## Before you start

- A UI that opens on a backend: [UIs](/use/learn/describing-the-ui/uis/).
- What a popup is and how UIs stack: [Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/).
- Click listeners: [Events](/use/learn/interactivity/events/).

## 1. Start from a confirmation popup

Start from the popup of [Open a popup above a screen](/use/learn/describing-the-ui/overlays-and-popups/#open-a-popup-above-a-screen): a [[UI]] marked with [[UIDataPopup]] and [[UIData]], whose card asks a question with two buttons.
Change its question to "Delete the file?", its second button to Delete, and give the popup the action to run in its constructor, so the screen that opens it decides what Delete does:

```java file="com/example/guides/PopupGuideConfirmPopup.java" region="data"
```

**Check:** the class compiles.

## 2. Open it from the screen

The screen shows a file name held in a [[StringSignal]].
Its Delete button opens the popup with [[JOID#open]] and passes the action, which clears the name:

```java file="com/example/guides/PopupGuideUI.java" region="open"
```

**Check:** click Delete.
The popup opens over the dimmed screen, and the button of the screen ignores the mouse; Cancel or `Escape` closes the popup.

## 3. Run the action on confirm

The Delete button of the popup runs the action, then closes the popup with [[JOID#close]]:

```java file="com/example/guides/PopupGuideConfirmPopup.java" region="delete"
```

**Check:** click Delete on the screen, then Delete in the popup: the file name changes to `No file`, and the popup closes.

## 4. Force an answer

Add `closeable = false` to the `@UIData` of the popup, so that only its buttons close it ([Configure a UI](/use/learn/describing-the-ui/uis/#configure-a-ui)).
The color of the backdrop is `backgroundColor` of the same annotation, and the opening and closing animations are the `transition` of `@UIDataPopup` ([Transitions](/use/learn/style-and-motion/transitions/)).

**Check:** with `closeable = false`, `Escape` leaves the popup open.

## If it does not work

1. The popup replaces the screen instead of opening over it: the class misses `@UIDataPopup(active = true)`; with `@UIDataPopup` alone, dev mode prints `@UIDataPopup on … leaves active = false and changes nothing`.
2. The screen behind is not dimmed: the class misses `@UIData(background = true)`.
3. `JOID.open` throws `IllegalStateException: No IUIBridge can open …`: no backend is registered yet.
   Open the popup from a listener, or see [Run code once JOID is ready](/use/learn/running/backends/#run-code-once-joid-is-ready).
4. `Escape` does not close the popup: `closeable` is `false`, or a node of the popup, such as a focused text field, consumed the key ([Know who receives the keyboard](/use/learn/interactivity/keyboard/#know-who-receives-the-keyboard)).
5. The popup disappears without its closing animation: it was closed with [[JOID#forceClose]], which skips the transition.

## Reference

- [[UIDataPopup]]
- [[UIData]]
- [[JOID]]
- [[UI]]
