---
title: "UIs"
description: "Write a UI, open and close it, keep it open, and configure it with @UIData."
genre: learn
sidebar:
  order: 1
---

Every interface you build with JOID is a UI: a menu, a settings panel, a dialog.
This page shows how to write one, how it opens and closes, and how to configure it.

**You will learn**
- How to write a UI and build its nodes in `init()`
- How to open and close a UI, and what happens in between
- How to keep a UI open while it has unsaved work
- How opening a screen replaces the current one
- How to configure a UI with annotations

## Write a UI

A UI is a class that extends [[UI]] and builds its content in `init()`:

```java file="com/example/describing/uis/MenuUI.java" region="class"
```

<figure>

![A dark square card with the word Menu in light letters at its top and three dark grey bars stacked below it](/captures/describing-uis-menu.webp)

<figcaption>
`MenuUI` on screen: everything a UI shows is a node that `init()` creates.
</figcaption>
</figure>

`init()` creates nodes and attaches them to the UI with `attach(this)`.
Positions and sizes are design pixels on a 1920 × 1080 canvas, whatever the size of the window: [The design canvas](/use/learn/describing-the-ui/design-canvas/) explains the scaling, and [Nodes and the tree](/use/learn/describing-the-ui/nodes/) explains the nodes.

[[UI]] has no abstract method.
You override the hooks you need: [[UI#init]] builds the content, [[UI#update]] runs code on every frame, before the nodes are drawn.
A UI also holds input listeners ([Events](/use/learn/interactivity/events/)), shortcuts ([Keyboard and shortcuts](/use/learn/interactivity/keyboard/)), tasks ([Tasks and background updates](/use/learn/state/tasks/)) and stores ([Stores and remembered properties](/use/learn/state/stores-and-properties/)).

## Open and close a UI

Open a UI with [[JOID#open]]:

```java file="com/example/describing/uis/Navigation.java" region="open"
```

Close it with [[JOID#close]], or let the user press Escape.
[[JOID#getUi]] returns the open UI of a class, or `null` when none is open:

```java file="com/example/describing/uis/Navigation.java" region="close"
```

[[JOID#isOpen]] tells whether a UI is open: `JOID.isOpen(ui)` for an instance, `JOID.isOpen(MenuUI.class)` for any UI of that class.
You can call `JOID.open` and `JOID.close` from any thread: off the render thread, JOID runs them at the next frame ([Threads](/use/learn/running/threads/)).

A UI goes through these moments:

| Moment | What JOID does |
|---|---|
| `new MenuUI()` | Runs the constructor and reads the annotations of the class. No node exists yet. |
| `JOID.open(ui)` | Adds the UI, runs `init()`, then plays the opening transition if the UI has one. |
| Every frame | Calls `update()`, then draws the nodes. The input reaches the nodes as it comes. |
| The window resizes | Fits the canvas to the new size. `init()` does not run again. |
| `JOID.close(ui)` or Escape | Asks the `beforeClose` listeners, plays the closing transition, detaches the nodes, saves the stores and properties of the UI, then fires `onClose`. |

`init()` runs at every opening, on the same instance: a UI that you close and open again starts from a fresh node tree, and its fields keep their values.
A listener, a task or a shortcut that you add in the constructor stays for the whole life of the instance; one that you add in `init()` lasts until the UI closes, and the next `init()` adds it again.

:::caution
Build the nodes in `init()`, never in the constructor.
Each opening clears the node list of the UI before `init()` runs, so a node attached in the constructor never shows.
:::

Escape closes the top UI when it is closeable and no node took the key ([Keyboard and shortcuts](/use/learn/interactivity/keyboard/#know-who-receives-the-keyboard)).
The opening and closing animations are transitions ([Transitions](/use/learn/style-and-motion/transitions/)).

## Keep a UI open

A [[UI#beforeClose]] listener that calls `cancel()` refuses the close.
It runs for `JOID.close(ui)` and for Escape:

```java file="com/example/describing/uis/EditorUI.java" region="class"
```

The editor stays open until the user clicks the save button.
[[UI#onClose]] runs once the UI has closed, when you need to react to it.

[[JOID#forceClose]] closes a UI without asking the `beforeClose` listeners and without its closing transition.
Use it when the UI must go, for example when the application quits.

`beforeClose` runs only where a listener can refuse, and `onClose` runs on every closing.
`JOID.forceClose`, `JOID.forceOpen`, the `closeAll()` of a UI bridge and the end of the program close without `beforeClose`, and fire `onClose` ([Close the UIs before the program ends](/use/learn/running/backends/#close-the-uis-before-the-program-ends)).

## Replace the current screen

A UI that is neither an overlay nor a popup is a screen.
Opening a screen closes the open screens first, with the popups open above them:

```java file="com/example/describing/uis/Navigation.java" region="replace"
```

`MenuUI` closes, then `EditorUI` opens.

<figure>

![The Menu card with its three grey buttons; the editor card, with its blue Save button, opens in its place; Escape, shown in a badge, leaves the editor open; the pointer clicks Save, which changes nothing on screen; a second later MenuUI opens again and replaces the editor, which no longer refuses](/captures/describing-uis-replace.webp)

<figcaption>
Opening a screen closes the one before it: the editor refuses to close until Save is clicked, then opening `MenuUI` replaces it.
</figcaption>
</figure>

When a UI that must close refuses, the new screen does not open; in dev mode, JOID prints a warning that names both UIs.

[[JOID#forceOpen]] closes every open UI without asking them, then opens the new one:

```java file="com/example/describing/uis/Navigation.java" region="force"
```

Overlays and popups open above the current screen instead of replacing it, so several UIs can be open at once: [Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/#see-how-uis-stack) shows how they stack.

## Rebuild an open UI

This UI counts in a field how many times `init()` built it, and gives `reload()` and `renew()` a button each:

```java file="com/example/describing/uis/ReloadUI.java" region="rebuild"
```

<figure>

![The text Builds: 1 above two blue buttons, Reload and Renew; the pointer clicks Reload twice and the text counts to 3, then clicks Renew and the text is back to Builds: 1](/captures/describing-uis-rebuild.webp)

<figcaption>
Reload keeps the instance and its field, so the count goes on; Renew replaces the instance, so the count starts again from 1.
</figcaption>
</figure>

[[UI#reload]] rebuilds an open UI on the same instance: it reads the annotations of the class again, then runs `init()` again, so its fields keep their values.
[[UI#renew]] starts from a fresh state instead: it replaces the open UI by a new instance of its class, built by its constructor without argument, and returns that instance.
In dev mode, shortcuts call each of them for you ([Dev tools](/use/get-started/dev-tools/)).

## Configure a UI

Annotate the class with [[UIData]] to change how the UI behaves:

```java file="com/example/describing/uis/PauseUI.java" region="class"
```

The options of `@UIData`:

- `background = true` draws the UI backdrop over the whole window, behind the nodes of the UI, in the color of `backgroundColor` (a hexadecimal string, a dark veil by default).
- `closeable = false` makes Escape leave the UI open.
  `JOID.close` still closes it.
- `zoomable = false` turns off the zoom shortcuts of dev mode ([The design canvas](/use/learn/describing-the-ui/design-canvas/#zoom-a-ui)).
- `active = false` keeps the UI drawn but sends it no input.
- `visible = false` neither draws the UI nor sends it input.
- `anchorX` and `anchorY` pin the canvas to a side or a corner of the window ([The design canvas](/use/learn/describing-the-ui/design-canvas/#pin-a-ui-to-a-side-or-a-corner)).
- `zindex` places the UI among the open UIs ([Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/#see-how-uis-stack)).
- `projection` and `zlevel` matter only when JOID draws inside the scene of a host application ([Draw a UI inside your own projection](/use/learn/running/embedding/#draw-a-ui-inside-your-own-projection)).

Three more annotations configure a UI:

- [[UIDataPopup]] makes it a popup and [[UIDataOverlay]] an overlay ([Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/)); a UI cannot be both.
- [[UIDataScale]] controls how the interface scale applies to it ([The design canvas](/use/learn/describing-the-ui/design-canvas/#follow-the-interface-scale)).
- [[UIDataDebug]] turns off, for this UI, the profiler and the hot reload of dev mode ([Dev tools](/use/get-started/dev-tools/)).

The annotations are inherited: a subclass keeps the options of its parent class unless it declares the annotation again.

[[UI#getData]] returns the options of the running UI, and each setter changes them from then on.
[[UI#getPopup]], [[UI#getOverlay]], [[UI#getScale]] and [[UI#getDebug]] do the same for the other annotations.
In `PauseUI`, a click on the card makes the UI closeable, so Escape closes it from the next press.

## Recap

- A **UI** extends `UI` and builds its nodes in **`init()`**, never in its constructor.
- **`JOID.open(ui)`** opens a UI and runs `init()` at every opening; **`JOID.close(ui)`** and Escape close it.
- A **`beforeClose`** listener that calls `cancel()` keeps the UI open; **`JOID.forceClose`** skips it.
- Listeners added in the **constructor** last for the instance; those added in **`init()`** last for one opening.
- Opening a **screen** closes the open screens and their popups first; overlays and popups open above it.
- **`reload()`** runs `init()` again on the same instance; **`renew()`** replaces it by a fresh one.
- **`@UIData`** configures the UI backdrop, Escape, zoom, input, visibility, anchors and `zindex`.

## Try it

**Close with a button.**
Add a fourth bar to `MenuUI` that closes the menu when clicked.

<details>
<summary>Solution</summary>

A click listener calls `JOID.close(this)` from inside the UI.
The card is 400 design pixels high: make it 500 high so that the bar fits.

```java fragment
RectNode.create(50, 400, 300, 70).color(MenuUI.ELEMENT).onClick(event -> JOID.close(this)).attach(card);
```

</details>

**Lock the menu.**
Make Escape leave `MenuUI` open, while your button still closes it.

<details>
<summary>Solution</summary>

`closeable = false` concerns Escape only; `JOID.close` keeps working:

```java fragment
@UIData(closeable = false)
public class MenuUI extends UI {
```

</details>

**Count the openings.**
Show how many times `MenuUI` has been opened.

<details>
<summary>Solution</summary>

`init()` runs at every opening and the fields keep their values: increment a field in `init()`, then show it in a [[TextNode]].

```java fragment
this.openings++;
final Text count = Text.create("Opened " + this.openings + " times", title);
TextNode.create(200, 450).text(count).anchor(Align.CENTER).attach(card);
```

</details>
