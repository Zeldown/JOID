---
title: "React to the player"
description: "Give the menu hover colors, a settings popup, a keyboard shortcut, tooltips and a working Quit button."
genre: tutorial
sidebar:
  order: 4
---

In this lesson the menu reacts: buttons light up under the pointer, Settings opens a popup, S opens it from the keyboard, the statistic cards explain themselves, and Quit ends the program.

<figure>

![The Starfall menu: the pointer moves onto the Settings button, which lights up, and clicks it; a settings popup opens over the darkened menu, a dark card titled Settings with the lines Music: on and Volume: 80 % and a Close button, which the pointer clicks to close it](/captures/tutorial-interaction-popup.webp)

<figcaption>
The result of this lesson: a click on Settings opens a popup above the menu.
</figcaption>
</figure>

## Starting point

The program of [the previous lesson](/use/tutorial/layout/) shows the whole menu, but nothing reacts.
The buttons keep their color under the pointer, a click does nothing, and the only way out is to close the window.

## 1. Write the settings popup

Add a lighter grey, `HOVER`, to the palette of `Theme`, for the buttons under the pointer:

```java file="com/example/tutorial/interaction/Theme.java" title="Theme.java" ins={14}
```

Settings opens a second UI above the menu.
The annotation [[UIDataPopup]], `@UIDataPopup(active = true)`, makes `SettingsUI` a popup, and the option `background = true` of [[UIData]] darkens the menu behind it with a veil, here black at 50 % opacity (`backgroundColor = "#00000080"`):

```java file="com/example/tutorial/interaction/SettingsUI.java" title="SettingsUI.java"
```

The Close button closes the popup with [[JOID#close]]`(this)`, and a popup also closes when the player presses Escape.
Nothing opens it yet, so the menu looks the same.

## 2. Make the buttons react

Give each button a `hoveredColor`, the color it turns to while the pointer is over it.
Then give Settings an `onClick` that opens the popup, and Quit one that closes the menu:

```java file="com/example/tutorial/interaction/MenuUI.java" region="buttons" title="MenuUI.java" ins={5-24}
```

**Run it.**
Hover a button: it lights up.
Click Settings: the popup grows into view over the darkened menu.
Click Close or press Escape: it shrinks away and the menu is back.

## 3. Open the popup with a key

A [[Shortcut]] runs code when the player presses a key.
Add it at the start of `init()` in `MenuUI`, after the styles:

```java file="com/example/tutorial/interaction/MenuUI.java" region="shortcut"
```

`attach(this)` gives the shortcut to the menu, so it works while the menu receives the keyboard.

**Run it.**
Press S: the settings open.
Press S again: nothing happens, because the keys of the player go to the popup, not to the menu under it.

## 4. Explain the cards with tooltips

Give each statistic card a tooltip with `hover(...)`:

```java file="com/example/tutorial/interaction/MenuUI.java" region="grid" ins={8,17,26}
```

The window UI bridge, the part of the backend that shows your UIs in the window, draws the tooltips, and it needs a text style for them, because JOID ships no font.
Set it in `Main`, right after `register()`:

```java file="com/example/tutorial/interaction/Main.java" region="tooltips"
```

**Run it.**
Rest the pointer on a card: its tooltip shows next to the pointer.

<figure>

![The profile panel: the pointer rests on the Games card, and a dark tooltip reading Rounds you played follows it, then on the Best card, with the tooltip Your highest score](/captures/tutorial-interaction-hover.webp)

<figcaption>
A tooltip follows the pointer while it stays over the card.
</figcaption>
</figure>

## 5. Quit the program

Quit closes the menu, but the loop of `Main` keeps running on an empty window.
Stop the loop when no menu is open anymore:

```java file="com/example/tutorial/interaction/Main.java" title="Main.java" ins={12,33-34,37,43}
```

The highlighted lines are every change to `Main` in this lesson, the tooltip style of step 4 included.
[[JOID#isOpen]]`(MenuUI.class)` tells whether a UI of that class is open.

A screen also closes on Escape, so Escape quits the game from the menu.

**Run it.**
Hover the buttons and the cards, open and close the settings with the mouse and with S, then click Quit: the window closes.

Play still does nothing, and the numbers of the panel never change: the next lesson gives them data.

<details>
<summary>The complete <code>MenuUI.java</code></summary>

```java file="com/example/tutorial/interaction/MenuUI.java"
```

</details>

## Challenge

Show the tooltip "Escape quits too" on the Quit button.

<details>
<summary>Solution</summary>

```java file="com/example/tutorial/interaction/ChallengeUI.java" region="solution" ins={6}
```

Any node takes a tooltip, a button as well as a card.

</details>

## What you learned

- `hoveredColor`, `onClick` and `hover` are the most common reactions of a node ([Events](/use/learn/interactivity/events/), [Hover, tooltips and cursors](/use/learn/interactivity/hover-and-tooltips/)).
- A popup is a UI opened above another with `JOID.open`, and closed with `JOID.close` or Escape ([Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/)).
- A shortcut runs code on a key press, for the UI that receives the keyboard ([Keyboard and shortcuts](/use/learn/interactivity/keyboard/#bind-a-shortcut)).
- The window UI bridge draws tooltips once it has a text style ([Choosing a backend](/use/learn/running/backends/#style-the-window-ui-bridge)).
