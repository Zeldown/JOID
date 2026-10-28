---
title: "Set up the project"
description: "Create the project of Starfall, open a first window and show the title of the game."
genre: tutorial
sidebar:
  order: 1
---

In this lesson you create the Starfall project and open a window that shows the title of the game.

<figure>

![The word Starfall in large bold light grey letters at the center of a dark background](/captures/tutorial-setup.webp)

<figcaption>
The result of this lesson: one UI with one text node.
</figcaption>
</figure>

## Starting point

Start from the program of the [Quick Start](/use/get-started/quick-start/#write-your-first-program), with the LWJGL 3 dev jar.
It opens a window with a counter button: this lesson replaces that UI with the menu of the game.

## 1. Open a window

Every file of the game goes in the package `com.example.starfall`, under `src/main/java/com/example/starfall/`.
Set the main class of the `application` block of `build.gradle`:

```groovy
application {
	mainClass = 'com.example.starfall.Main'
	if (System.getProperty('os.name').toLowerCase().contains('mac')) {
		applicationDefaultJvmArgs = ['-XstartOnFirstThread']
	}
}
```

`MenuUI` is the main menu.
For now it is empty: a UI draws nothing by itself.

```java file="com/example/tutorial/window/MenuUI.java" title="MenuUI.java"
```

`Main` is the program of the Quick Start, with the title of the game and the menu as the UI that [[JOID#open]] opens.
Leave out the call to `closeAll()` for now: lesson 6 brings it back with its reason.

```java file="com/example/tutorial/setup/Main.java" title="Main.java" ins={22,31}
```

**Run it** with `gradle run`.
You should see a black window: the menu is open, but it has no node yet.
Press Escape: the menu closes, and the window stays black, because no UI is open anymore.

## 2. Show the title

JOID ships no font for your application.
Put the regular and the bold faces of your font in `src/main/resources/fonts/Montserrat/`, as `Montserrat-Regular.ttf` and `Montserrat-Bold.ttf`.
With another family, keep the folder and change the names in `Theme`.

`Theme` holds what every screen of the game shares: the font and the colors of its palette.

```java file="com/example/tutorial/setup/Theme.java" title="Theme.java"
```

- [[MsdfFontLoader#load]] reads both faces into one font family, and `join()` waits until it is ready.
- `lock()` keeps the shared color from being changed by accident ([Keep a palette safe](/use/learn/style-and-motion/styling/#keep-a-palette-safe)).

The menu now shows the title at the center of the canvas:

```java file="com/example/tutorial/setup/MenuUI.java" title="MenuUI.java" ins={3-5,7-8,13-14,16-21}
```

- `init()` creates the nodes of the UI; `attach(this)` adds the text to it.
- [[TextInfo]] describes the style of a text: the font, the size, the weight and the color.
- (960, 540) is the center of the 1920 × 1080 design canvas, and [[Align]]`.CENTER`, given to `anchor(...)`, puts the center of the text on that point.

**Run it.**
You should see the title in bold light grey at the center of the window.
Resize the window: the title stays centered and scales with it.
If the run stops with an `ExceptionInInitializerError` in `Theme`, check the folder and the names of the font files.

The menu has a title and nothing else: the next lesson adds the buttons.

## Challenge

Show the version of JOID in small letters under the title, with the constant [[JOID#VERSION]].

<details>
<summary>Solution</summary>

Add a second style, then a second text 100 design pixels lower:

```java file="com/example/tutorial/setup/ChallengeUI.java" region="style"
```

```java file="com/example/tutorial/setup/ChallengeUI.java" region="solution"
```

Its own anchor centers it on the same column as the title.

</details>

## What you learned

- A [UI](/use/learn/describing-the-ui/uis/) is a class that creates its nodes in `init()`, opened by `JOID.open(...)`.
- Your program owns the window and the loop; the [backend](/use/learn/running/backends/) draws a frame when you call `frame()`.
- JOID ships no font: you load your own [font](/use/learn/describing-the-ui/fonts/) and describe the style of a [text](/use/learn/describing-the-ui/text/) with a `TextInfo`.
- Positions are [design pixels](/use/learn/describing-the-ui/design-canvas/) on a 1920 × 1080 canvas, scaled to the window.
