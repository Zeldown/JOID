---
summary: A UI bridge in which opening a screen closes the open screens and popups, while overlays and popups stack above them.
---

It is the usual base of a UI bridge of your own: you keep the usual opening and closing and override what your host needs.
[UI bridge](/build/bridges/ui/#example) shows one that takes every overlay.

Learn: [Add a UI bridge of your own](/use/learn/running/embedding/#add-a-ui-bridge-of-your-own), [UI bridge](/build/bridges/ui/).

## StackUIBridge()

Creates a UI bridge with no open UI, for a subclass.

## open(UI)

Opens the UI.
An open UI moves to the front and its closing transition stops; an overlay or a popup is added; a screen first closes every open screen and popup that `canReplace` accepts, and waits for their closing transition before it is added.
A UI that refuses to close keeps the screen from opening, with a dev warning.

## close(UI)

Removes the UI; when it was leaving for a screen waiting behind its transition, that screen opens.

## add(UI)

Adds the UI and lays it out at the size of the window; calls `attachScreen()` when it is the first screen or popup.

## remove(UI)

Removes the UI; calls `detachScreen()` when it was the last screen or popup.

## canHandle(Class)

Returns `true`: the bridge accepts every UI.

## forceOpen(UI)

Closes every UI and opens this one, without calling `detachScreen()` and `attachScreen()` between them when a screen stays open.

## replace(UI,UI)

Replaces the first UI with the second, without calling `detachScreen()` and `attachScreen()` between them.

## canReplace(UI)

If `true`, opening a screen closes this open screen or popup; it returns `true` by default.
Return `false` to keep the UI below the new screen.

## attachScreen()

JOID calls it when the first screen or popup of the bridge opens; overlays do not count.
Override it to free the cursor of the host or pause its input, as in [Follow the screens that JOID opens](/use/learn/running/embedding/#follow-the-screens-that-joid-opens); it does nothing by default.

## detachScreen()

JOID calls it when the last screen or popup of the bridge closes; overlays do not count.
Override it to give the cursor and the input back to the host; it does nothing by default.
