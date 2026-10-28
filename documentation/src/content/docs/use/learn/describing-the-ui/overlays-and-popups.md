---
title: "Overlays and popups"
description: "Open a popup above a screen, keep an overlay open over every screen, and see how open UIs stack and share the input."
genre: learn
sidebar:
  order: 6
---

A screen fills the window, and opening another screen replaces it.
Some UIs must open above a screen instead: a confirmation dialog, a status bar, a minimap.
This page shows the two kinds of UI that do: popups and overlays.

**You will learn**
- How to open a popup above a screen and dim what is under it
- How to keep an overlay open while screens come and go
- How to let an overlay take the mouse and the keyboard
- How open UIs stack, and which one receives the input

## Open a popup above a screen

A popup is a UI annotated with [[UIDataPopup]] and `active = true`.
Without `active = true`, the annotation does nothing, and dev mode prints a warning.

```java file="com/example/describing/overlays/ConfirmPopup.java" region="class"
```

Open it like any UI, here from a button of the screen:

```java file="com/example/describing/overlays/SettingsUI.java" region="open"
```

<figure>

![A settings screen with the title Settings and the lines Music: on and Volume: 80 %; the pointer clicks its blue Quit button: a dark veil dims the screen, which stays visible around a dark card that opens in its middle, reading Quit the game? above a dark grey Cancel button and a blue Quit button; a click on Cancel closes it.](/captures/describing-overlays-popup.webp)

<figcaption>
The popup opens above `SettingsUI` without closing it, and its UI backdrop dims the screen.
</figcaption>
</figure>

A popup differs from a screen in three ways:

- Opening it keeps the screen under it open.
- It is modal: every input stops at the popup, even where it has no node, so the screen under it receives nothing until it closes.
- It opens and closes with a transition, set by `transition` of `@UIDataPopup` ([Transitions](/use/learn/style-and-motion/transitions/)).

`background = true` of [[UIData]] adds the UI backdrop that dims the screen; without it, the screen stays visible as it is.
Escape closes the popup when it is closeable, like a screen.
Opening another screen closes the current screen with every popup open above it ([UIs](/use/learn/describing-the-ui/uis/#replace-the-current-screen)).

[Make a modal popup](/use/guides/make-a-modal-popup/) builds a confirmation popup end to end.

## Keep an overlay over every screen

An overlay is a UI annotated with [[UIDataOverlay]] and `active = true`, which turns it on as for a popup:

```java file="com/example/describing/overlays/StatusOverlay.java" region="class"
```

An overlay stays open when screens open and close, and Escape never closes it: [[JOID#close]] does.
By default, an overlay is drawn below the screens, as a layer of information under the menus, and receives no input: clicks and keys go to the screens.

<figure>

![The blue bar of StatusOverlay above the Settings screen; the Settings screen gives way to the Menu screen, a dark card with three grey buttons, then comes back, while the bar stays where it is](/captures/describing-overlays-status.webp)

<figcaption>
`StatusOverlay` stays open while `SettingsUI` and `MenuUI` replace each other.
</figcaption>
</figure>

A UI cannot be both a popup and an overlay: JOID refuses to create it.

## Draw an overlay above the screens and let it take input

`render` and `interaction` of `@UIDataOverlay` change both defaults:

```java file="com/example/describing/overlays/MinimapOverlay.java" region="class"
```

<figure>

![The top-right corner of the window: a dark square with a blue disc in its middle; the pointer clicks the square twice: the disc doubles in size, then shrinks back.](/captures/describing-overlays-minimap.webp)

<figcaption>
The minimap overlay takes the clicks on it.
</figcaption>
</figure>

[[UIDataOverlayRender]] sets how the overlay is drawn:

- `aboveScreens = true` draws it above the screens and the popups.
- `always = true` draws it even when the UI bridge hides its overlays, as a host can do while it shows its own menus ([Embedding JOID](/use/learn/running/embedding/)).

[[UIDataOverlayInteraction]] sets its input:

- `active = true` sends it the input, in its place in the stack below.
- `cancelClick`, `cancelScroll` and `cancelKeyboard` decide whether a click, a scroll or a key that the overlay consumes counts as consumed for the application that runs JOID; set one to `false` to let the host see that input too.

An input that no node of the overlay consumes goes on to the UIs below it.

## See how UIs stack

<figure>

![A stack of three layers. At the top, overlays above the screens; in the middle, popups and screens, by zindex then in opening order; at the bottom, overlays, drawn below the screens by default. An arrow on the right goes from the top down: input goes from the top down and stops at the first UI that consumes it, or at a popup. A line at the bottom reads: overlays take input only with interaction active.](/captures/describing-overlays-stack.webp)

<figcaption>
JOID draws the open UIs from the bottom of this stack to its top, and sends the input from its top to its bottom.
</figcaption>
</figure>

- JOID draws three groups, one above the other: the overlays, then the screens and popups, then the overlays with `aboveScreens = true`.
- Inside a group, the `zindex` of `@UIData` orders the UIs, the highest on top; UIs with the same `zindex` keep their opening order, the last opened on top.
- The input goes from the top UI down: it skips the UIs that are not `active` or not `visible` and the overlays without interaction, and it stops at the first UI that consumes it, or at a popup.
- Opening a UI that is already open brings it to the top of its group.

[[UI#isOnTop]] tells whether a UI is the top one of its kind that receives input: the top screen or popup, or the top overlay with interaction.
With several UI bridges, each one stacks its own UIs, and the bridges stack by priority ([Embedding JOID](/use/learn/running/embedding/#add-a-ui-bridge-of-your-own)).

## Recap

- A **popup** (`@UIDataPopup(active = true)`) opens above the screen, keeps it open and is **modal**.
- `@UIData(background = true)` adds the **UI backdrop** that dims the screen under a popup.
- An **overlay** (`@UIDataOverlay(active = true)`) stays open across screens; Escape never closes it.
- An overlay is drawn **below the screens** and takes **no input** by default; `render` and `interaction` change that.
- UIs stack in **three groups**, then by **`zindex`**, then by opening order; input goes **from the top down**.

## Try it

**Let the status bar see the mouse.**
Make the bar of `StatusOverlay` react to a click, while it stays below the screens.

<details>
<summary>Solution</summary>

Turn its interaction on and leave `render` as it is:

```java fragment
@UIDataOverlay(active = true, interaction = @UIDataOverlayInteraction(active = true))
```

Drawn below the screens, it receives a click only where no screen consumed it before.

</details>

**Open a popup over a popup.**
Open a second popup from the Quit button of `ConfirmPopup`, and say which UI receives a click on the first one.

<details>
<summary>Solution</summary>

`JOID.open(new ConfirmPopup())` in the `onClick` of the button opens it above the first one.
The input stops at the top popup, which is modal: the first popup receives nothing until the second one closes.

</details>
