---
title: "Testing your UIs"
description: "Render your screens offscreen in a JUnit test, play the mouse and the keyboard on them, and compare the pictures."
genre: learn
sidebar:
  order: 5
---

A screen test renders a UI, plays the mouse and the keyboard on it, and checks the pixels.
The testkit does this offscreen, in a JUnit test, and gives the same pixels on every run.

**You will learn**
- What your build needs to test screens
- How to render a UI and save the picture
- How to click, type and wait in a test
- How to compare a picture with a reference
- How the testkit keeps every run identical

## Add the testkit to your tests

Screen tests need three things on the test classpath:

- the **dev jar** of your backend, which holds its snapshot backend ([[Lwjgl3SnapshotBackend]], [[Lwjgl2SnapshotBackend]] or [[VulkanSnapshotBackend]]); the prod jar has none;
- the testkit jar, `joid-tool-testkit`;
- JUnit 4.

[Installation](/use/get-started/installation/) shows where the jars come from, and how to give the tests the dev jar while the application ships the prod jar.

The snapshot backend draws on a hidden surface, so the machine that runs the tests needs a graphics driver, even a software one on a CI server.

## Render a screen

A [[SnapshotRunner]] starts the snapshot backend once for the test class, and stops it at the end:

```java file="com/example/running/MenuSnapshotTest.java" region="start"
```

`start(...)` creates a hidden 1920×1080 surface, registers the backend with the testkit's own UI bridge, window, clock and audio bridges, and loads JOID.
`stop()` closes every UI, destroys the surface and gives back the bridges that were there before.

Each test gives the runner a list of commands and gets the pictures back by name:

```java file="com/example/running/MenuSnapshotTest.java" region="shot"
```

`execute(...)` starts from a clean state: no UI open, the window at 1920×1080, no key held, the clock at its start.
`ui <class>` creates the UI with its constructor without argument, of any visibility, and shows it alone, with the mouse outside the window; `shot menu` captures the window.
`open <class>` creates a UI the same way but opens it with [[JOID#open]], over what is already open: use it for a popup or an overlay.
Both play the opening transition: add a `wait` before the shot to capture the UI fully open.

A [[SnapshotImage]] holds the pixels as ARGB `int` values, row after row, from the top-left corner.
`write(file)` saves it as a PNG, to look at it or to keep it as a reference.

<figure>

![A dark grey Play button on a dark background](/captures/running-testing-menu.webp)

<figcaption>
The middle of `menu.png`.
The test writes the whole 1920×1080 window.
</figcaption>
</figure>

## Drive the UI with commands

Commands play the input of a user, one line each:

```java file="com/example/running/MenuSnapshotTest.java" region="click"
```

<figure>

![The Play button now reads Playing](/captures/running-testing-clicked.webp)

<figcaption>The shot `clicked`: the pointer moved to the button, pressed and released, and the label changed.</figcaption>
</figure>

`moveto <x> <y> <ms>` moves the pointer over that time, in screen pixels, one frame every 16 ms; `press LEFT` and `release LEFT` click; `wait <ms>` advances the clock by exactly that time, one frame every 16 ms.
Other commands type text, press keys, scroll, resize the window, zoom, turn the dev mode on, check the cursor and mask what changes between runs: [[SnapshotRunner#execute]] lists every command.

## Compare with a reference

`compare(reference, tolerance)` returns a [[SnapshotDifference]]:

- `getPixels()` counts the pixels whose largest channel difference is above the tolerance;
- `getMaximum()` gives the largest channel difference found, from `0` to `255`.

`isSame(other)` checks that two pictures are identical.

To create a reference, let the test write the shot once, check it by eye, then copy it into your test resources.
Drivers round colors differently, so a tolerance of 2 or 3 keeps a reference valid from one machine to another; record the references on the machine that runs the tests when it uses another GPU.
`getRenderer()` of the runner names that GPU, for example to keep one folder of references per GPU.

## Keep every run identical

The testkit removes what changes from one run to the next:

- **Time**: the runner drives a manual clock that starts at 2025-01-01 00:00 UTC and moves only with `wait` and `moveto`.
  Animations, transitions and [tasks](/use/learn/state/tasks/) follow it.
- **Loading**: before each shot, the runner waits until every resource is loaded, then until two frames in a row are identical.
  It gives up after 30 seconds with `A resource never finished loading in 30s, …`.
- **Network**: a resource read from an `http` or `https` URL is downloaded once into the `snapshot` area of the [cache folder](/use/learn/running/files/), then read from there, so later runs work offline.
- **Sound**: the audio bridge of the testkit plays nothing, so no sound device is needed.
- **Dev tools**: the dev mode stays off unless a `dev true` command turns it on.

Content that cannot be the same on two runs, such as the current date, goes under a `mask`.

:::caution
A UI that reads the time of the system never becomes stable, and the shot fails with `… never became stable while the clock was paused, … pixels kept changing: mask the area that moves or stop what moves it`.
Read the time from [[BridgeHandler#CLOCK]], with `BridgeHandler.CLOCK.get().currentTimeMillis()`, which follows the clock of the test.
:::

## Recap

- Tests need the **dev jar** of the backend, which holds the **snapshot backend**, plus `joid-tool-testkit` and JUnit 4.
- **`SnapshotRunner.start(...)`** once per test class, **`stop()`** at the end.
- **`execute(...)`** plays commands from a clean state and returns the shots by name.
- **Commands** play the user: `ui`, `moveto`, `press`, `release`, `wait`, `shot`; the reference lists the others.
- **`compare(reference, tolerance)`** counts the pixels that differ; write the shot once to create the reference.
- **Time** moves only with `wait` and `moveto`: read the time from `BridgeHandler.CLOCK`, never from the system.

## Try it

**Type in a field.**
Test that typing `Ada` in a text field at the center of the window shows it.
Which commands do you play?

<details>
<summary>Solution</summary>

```java fragment
final String ui = "ui " + FormUI.class.getName();
runner.execute(ui, "moveto 960 540 200", "press LEFT", "release LEFT", "type Ada", "shot typed");
```

The click focuses the field at the center of the window, `type` sends the three characters, and the shot shows the result.

</details>

**A popup fully open.**
A popup opens with a 300 ms transition.
Capture it fully open.

<details>
<summary>Solution</summary>

Play `wait 300` between the `open <class>` and the `shot`.
The clock of the test stands still between commands, so the transition advances only with `wait`.

</details>
