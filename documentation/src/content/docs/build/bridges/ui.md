---
title: "UI bridge"
description: "Write your own UI bridge: the class to extend, what you implement, what UIBridge gives you, and what a bridge must ensure."
genre: build
sidebar:
  order: 6
---

A UI bridge holds a set of open UIs, lays them out, draws them each frame and hands them the input.
Every backend registers a default one, a [[WindowUIBridge]], whose settings are on [Style the window UI bridge](/use/learn/running/backends/#style-the-window-ui-bridge).
Write your own when some UIs must live apart from the others (above them, at another scale), when a host must react to the opening of a screen, or when the window background belongs to the host.
How JOID orders several bridges and routes the UIs, the input and the cursor across them is on [Add a UI bridge of your own](/use/learn/running/embedding/#add-a-ui-bridge-of-your-own).

## Pick the class to extend

[[BridgeHandler#UI]] holds [[UIBridge]]s: a bridge that implements only [[IUIBridge]] cannot be registered.

| Class | Gives | Extend it when |
|---|---|---|
| [[UIBridge]] | Input dispatch, update, drawing by layer, cursor, consumed presses. | Your bridge decides itself what opening and closing a UI mean. |
| [[StackUIBridge]] | The screen stack: opening a screen closes the open screens and popups, overlays and popups stack above them. | You keep the usual opening and closing and change the rest. This is the usual choice. |
| [[WindowUIBridge]] | Clears the window to a color each frame, and draws text tooltips once its `hoverInfo` is set. | Your bridge also paints the window. |

## Required

What a subclass of [[UIBridge]] implements; a [[StackUIBridge]] implements all of them already, as the last column shows.

| Member | Called when | On a `StackUIBridge` |
|---|---|---|
| [[IUIBridge#open]], [[IUIBridge#close]] | [[JOID#open]] and `JOID.close` reach this bridge, and Escape closes the top closeable UI that is not an overlay. | An overlay or a popup is added; a screen first closes the open screens and popups that `canReplace` accepts, waiting for their closing transition. `close` removes the UI. |
| [[IUIBridge#canHandle]] with a class | `JOID.open(ui)` looks for a bridge, through `canHandle(ui)`, which asks it for the class of the UI. | `true`. |

## Provided

What [[UIBridge]] does for you.

| Member | What you get |
|---|---|
| [[UIBridge#mousePressed]], [[UIBridge#mouseReleased]], [[UIBridge#mouseMoved]], [[UIBridge#mouseScroll]], [[UIBridge#charTyped]], [[UIBridge#keyPressed]], [[UIBridge#keyReleased]] | The dispatch of each event to the UIs from the top down, with the consumption rules of [Input forwarding](/build/input/#provided): each returns `true` when JOID consumed the event. |
| [[UIBridge#load]], [[UIBridge#update]], [[UIBridge#draw]] | The steps of a frame for this bridge, in the order given at the end of [Guarantees](#guarantees). |
| [[UIBridge#add]], [[UIBridge#remove]], [[UIBridge#isOpen]], [[UIBridge#getUiList]] | The list of the open UIs, sorted by `zindex`. A `StackUIBridge` also lays a UI out when it is added. |
| [[UIBridge#isOnTop]] | Whether a UI is the first of its kind (screens and popups, or overlays) to receive the input. |
| [[UIBridge#hasScreen]], [[UIBridge#isScreenOpen]] | Whether this bridge holds a UI that is not an overlay; JOID never calls `isScreenOpen` itself, so a host can override it to answer for its own screens too. |
| [[IUIBridge#closeAll]], [[IUIBridge#forceOpen]], [[IUIBridge#replace]] | Disposing and removing every UI, opening a UI alone, and swapping one UI for another without a transition. |

`draw()` draws the overlays that stay below the screens, then the screens and popups, then the overlays drawn above the screens, each group in the order of `zindex` ([See how UIs stack](/use/learn/describing-the-ui/overlays-and-popups/#see-how-uis-stack)).
It sets the cursor of the hovered node, or of the pressed node during a drag, and leaves the cursor alone while the window bridge reports a grabbed mouse.
An exception thrown while a UI draws skips the rest of the frame of this bridge, puts the matrix and state stacks back, and prints one warning.

The members with a default, which you override when your host needs it:

| Member | Default | Override it to |
|---|---|---|
| [[IUIBridge#getInterfaceScale]] | `1D`. | Scale the UIs of this bridge on top of the canvas scaling ([interface scale](/use/learn/describing-the-ui/design-canvas/)); JOID reads it every frame. |
| [[IUIBridge#drawHover]] | [[UIBridge]]: nothing, with a dev warning; [[WindowUIBridge]]: a rounded box, once its `hoverInfo` is set. | Draw the text tooltip that a node shows. |
| [[UIBridge#drawBackground]] | [[UIBridge]]: nothing; [[WindowUIBridge]]: clears color, depth and stencil. | Paint the window before any UI is drawn; JOID calls it on the bridge of the lowest priority only. |
| [[UIBridge#isOverlayHidden]] | `false`. | Return `true` while the host hides its own interface: the overlays that do not ask to stay are then neither drawn nor given the input. |
| [[StackUIBridge#canReplace]] | `true`. | Return `false` to keep an open screen or popup below a new screen; a UI that refuses to close blocks the new screen. |
| [[StackUIBridge#attachScreen]], [[StackUIBridge#detachScreen]] | Nothing. | Free or lock the cursor of the host ([Follow the screens that JOID opens](/use/learn/running/embedding/#follow-the-screens-that-joid-opens)). |

## Guarantees

What your bridge must ensure.

| Rule | Why |
|---|---|
| `canHandle` answers from the class of the UI alone. | `canHandle(ui)` asks it with the class, and the answer must not change while the UI is open. |
| A bridge that is the lowest of the registry clears the depth and the stencil in `drawBackground()`, and the color unless the host drew its scene beneath (`WindowUIBridge` clears all three). | Only the lowest bridge receives `drawBackground()`: a bridge registered before the backend takes the place of the default one, and without a depth clear the 3D content and the UIs test against the depth of the previous frame. |
| `add` and `remove` keep `getUiList()` in step with the open UIs. | `JOID.isOpen`, `JOID.getUi`, the frame and the input all read that list. |
| `draw()` runs only between `beginFrame()` and `endFrame()` of the render bridge. | It throws an `IllegalStateException` outside a frame; [[UIBridgeRegistry#frame]] opens the frame for every bridge. |

[[UIBridgeRegistry#frame]], called by [[Backend#frame]], gives each bridge `load()` when the window changes size and `update()`, opens the frame of the render bridge, gives the lowest bridge `drawBackground()` and every bridge `draw()`, from the lowest priority to the highest.
[The frame lifecycle](/use/in-depth/frame-lifecycle/) places these steps in a frame.

## Example

This bridge takes every overlay, draws it 25 % larger, and hides the overlays while the host hides its own interface:

```java file="com/example/backend/OverlayUIBridge.java" region="bridge"
```

An application registers it above the default bridge ([Add a UI bridge of your own](/use/learn/running/embedding/#add-a-ui-bridge-of-your-own)): overlays then open in it, and every other UI keeps opening in the default bridge.
