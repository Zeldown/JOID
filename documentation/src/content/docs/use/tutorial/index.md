---
title: "Tutorial overview"
description: "Build Starfall, the main menu and profile panel of a small game, one lesson at a time."
genre: tutorial
sidebar:
  order: 0
---

In this tutorial you build Starfall: the main menu of a small game, with a profile panel that shows the rounds the player played and a settings popup that remembers its values.
Each lesson starts from a program that runs, points at what it lacks, and closes the gap.

<figure>

![The main menu of Starfall at the end of the tutorial: the title and its tagline, three dark grey buttons Play, Settings and Quit, the line of the settings, and a dark profile panel with rounded corners and a soft shadow that shows three rounded statistic cards and the three rounds played; the pointer rests on Play, which lights up under a hand, then on the Games card, which shows the tooltip Rounds you played, then clicks Settings: the settings popup opens over the darkened menu, and a click on Close closes it](/captures/tutorial-overview.webp)

<figcaption>
Starfall at the end of the tutorial: every part of it is a node you write yourself.
</figcaption>
</figure>

## The lessons

| Lesson | You build | You practice | Code at the end |
|---|---|---|---|
| 1. [Set up the project](/use/tutorial/setup/) | A window with the game title | A UI, a font, the program loop | [`setup`](https://github.com/Zeldown/JOID/tree/main/documentation/examples/src/main/java/com/example/tutorial/setup) |
| 2. [Build the main menu](/use/tutorial/main-menu/) | The title and three buttons | Nodes, children, the design canvas | [`menu`](https://github.com/Zeldown/JOID/tree/main/documentation/examples/src/main/java/com/example/tutorial/menu) |
| 3. [Lay out the profile panel](/use/tutorial/layout/) | A column of buttons and the profile panel | Flex and grid layouts | [`layout`](https://github.com/Zeldown/JOID/tree/main/documentation/examples/src/main/java/com/example/tutorial/layout) |
| 4. [React to the player](/use/tutorial/interaction/) | Hover colors, a settings popup, a shortcut, tooltips, Quit | Events, popups, the keyboard | [`interaction`](https://github.com/Zeldown/JOID/tree/main/documentation/examples/src/main/java/com/example/tutorial/interaction) |
| 5. [Keep the state](/use/tutorial/state/) | Rounds that fill the panel | Signals, derived values, rebuilding a list | [`state`](https://github.com/Zeldown/JOID/tree/main/documentation/examples/src/main/java/com/example/tutorial/state) |
| 6. [Save the settings](/use/tutorial/settings/) | Settings that survive a restart | Stores | [`settings`](https://github.com/Zeldown/JOID/tree/main/documentation/examples/src/main/java/com/example/tutorial/settings) |
| 7. [Polish with effects and motion](/use/tutorial/polish/) | Rounded corners, shadows, a hand pointer, a panel that slides in | Effects, cursors, animation | [`polish`](https://github.com/Zeldown/JOID/tree/main/documentation/examples/src/main/java/com/example/tutorial/polish) |

## What you need

- A project set up as in [Installation](/use/get-started/installation/), with the LWJGL 3 dev jar.
- A font with a regular and a bold face, as TrueType or OpenType files.
  The captures use Montserrat.

The [Quick Start](/use/get-started/quick-start/) tours the topics that the lessons practice: read it first if you have not.

## How the lessons work

Each lesson shows the code it changes, with the changed lines highlighted, and the complete file when most of it changes.
Each lesson edits the same files.

The last column of the table links the complete code at the end of each lesson, a program that runs on its own.
To start a lesson from there, or to compare your code, copy the folder of the lesson before it; `polish` is the finished game.
In the repository, each folder keeps a package of its own, `com.example.tutorial.<folder>`, so that the seven programs live side by side: when you copy one into your project, change its package to `com.example.starfall`.

Each lesson ends with a challenge and its solution, and with links to the Learn pages that explain what you used.
The tutorial never explains a notion in depth: follow the links when you want the full picture.
