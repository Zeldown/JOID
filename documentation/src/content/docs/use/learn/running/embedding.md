---
title: "Embedding JOID"
description: "Run JOID inside your own loop, next to your own scene and input, and register UI bridges of your own."
genre: learn
sidebar:
  order: 2
---

Your application owns the window and the loop, and often draws other things than UIs: a game scene, a viewport, a video.
This page fits JOID into that loop without taking it over.

**You will learn**
- Where `frame()` goes in your loop, and what it leaves untouched
- How to keep your own input handlers next to JOID
- How to draw JOID over a scene of your own and follow the screens it opens
- How to draw a UI inside your own projection
- How to add a UI bridge of your own and order the bridges with priorities

## Fit JOID in your loop

Your application is the **host**: it owns the window and the loop, and JOID runs inside them.
JOID reaches your window through **bridges**, small interfaces that the backend fills in.
The one you meet on this page is the **UI bridge**: it holds the open UIs, routes the input to them and draws them every frame.
The backend registers one, [the window UI bridge](/use/learn/running/backends/#style-the-window-ui-bridge), and you can register more.

A loop that shows JOID polls the window events, calls `frame()` of the backend, then shows the frame, as the loop of [Choosing a backend](/use/learn/running/backends/#start-a-backend) does.
One call to [[Backend#frame]] follows the size of the window, updates the open UIs of every UI bridge, clears the window, then draws the bridges from the lowest priority to the highest.
[The frame lifecycle](/use/in-depth/frame-lifecycle/) details each step.

On the OpenGL backends, the frame gives back the OpenGL state it found, so your own drawing code before and after `frame()` runs unchanged.
After the loop, `closeAll()` of each UI bridge [closes the UIs still open](/use/learn/running/backends/#close-the-uis-before-the-program-ends).

Call `frame()` from the thread that registered the backend: [Threads](/use/learn/running/threads/) explains how other threads hand their work to it.
[[UIBridgeRegistry#onFrame]] of [[BridgeHandler#UI]] runs a task of yours at the start of every frame, before the UIs update.
Outside a UI, `BridgeHandler.WINDOW.get()` returns the window as JOID reads it, an [[IWindowBridge]]: the size of the window in screen pixels, the position of the mouse, the keys held, and the clipboard of the system.

## Keep your own input handlers

On LWJGL 3 and Vulkan, [`register()` attaches](/use/learn/running/backends/#know-what-happens-at-register) a [[GlfwInputForwarder]] to the window, which installs the GLFW callbacks.
Each event goes to the UIs first; an event that no UI [consumes](/use/learn/interactivity/events/) goes on to the callback your application set before `register()`:

```java file="com/example/running/HostApp.java" region="callbacks"
```

Here, Escape closes the window only when no UI used it, for example to [close a screen](/use/learn/interactivity/keyboard/#know-who-receives-the-keyboard).
To compare a position of your own with the nodes of a UI, [[UI#getView]] converts between screen pixels and design pixels ([Convert between the window and the UI](/use/learn/describing-the-ui/design-canvas/#convert-between-the-window-and-the-ui)).
The same rule holds for releases: when a UI consumed a press, your callback receives neither the press nor its release.
An overlay that receives the input is the exception: `cancelClick`, `cancelScroll` and `cancelKeyboard` of [[UIDataOverlayInteraction]] decide whether an event it used still reaches your callback ([Overlays and popups](/use/learn/describing-the-ui/overlays-and-popups/)).

:::caution
Set your GLFW callbacks before `register()`.
A callback set after it replaces the one of JOID, and the UIs stop receiving that kind of event.
:::

With LWJGL 2, `poll()` of [[Lwjgl2InputForwarder]] empties the `Mouse` and `Keyboard` queues.
To share them with your code, read the queues yourself and give each event to the matching method of `BridgeHandler.UI` (`mousePressed`, `keyPressed`, `charTyped`…), which returns `true` when a UI consumed it.

## Draw JOID over your own scene

The window UI bridge clears the window before every frame, so it would erase a scene you drew before `frame()`.
Register a bridge that keeps the colors and clears only the depth and the stencil:

```java file="com/example/running/SceneUIBridge.java"
```

Register it before the backend: the backend then adds no window UI bridge of its own.
Give it a text style for the tooltips once the backend is registered, so that the font loads [after the folders apply](/use/learn/running/files/#move-the-folders):

```java file="com/example/running/HostApp.java" region="scene"
```

Draw your scene, then let JOID draw over it:

```java file="com/example/running/HostApp.java" region="loop"
```

`HostScene` stands for your scene: it fills the window and slides a square with plain OpenGL calls.

<details>
<summary>The complete <code>HostScene.java</code></summary>

```java file="com/example/running/HostScene.java"
```

</details>

`HostOverlayUI` is an overlay ([[UIDataOverlay]]) that receives the input, so its Tab shortcut opens `HostMenuUI`, a screen:

```java file="com/example/running/HostOverlayUI.java"
```

It sets no `background` in [[UIData]], so the scene shows around its hint; a UI backdrop (`background = true`) covers the whole window and hides the scene.
`SceneUIBridge` extends [[WindowUIBridge]], so it keeps the tooltips and every setting of the window UI bridge.
On Vulkan, the frame of JOID loads the image of the host without clearing it: the scene is what the host recorded into its command buffer before `frame()`.

Your application can also hide every overlay of JOID, for a screenshot or a cutscene: override `isOverlayHidden()` of [[UIBridge]] in your bridge and return `true` while the interface must disappear.
Overlays with `always = true` in [[UIDataOverlayRender]] stay visible.
To make your UIs larger or smaller, such as for a scale option of your settings, override `getInterfaceScale(ui)` in your bridge and return the [interface scale](/use/learn/describing-the-ui/design-canvas/#follow-the-interface-scale) of each UI; the window UI bridge returns `1D`.

## Follow the screens that JOID opens

A game pauses its scene and frees the mouse it captures while a menu of JOID is open.
Your bridge follows the screens of JOID with two hooks of [[StackUIBridge]], `attachScreen()` and `detachScreen()`:

```java file="com/example/running/GameUIBridge.java"
```

JOID calls `attachScreen()` when the first screen or popup of the bridge opens, and `detachScreen()` when the last one closes.
Overlays do not count, so `HostOverlayUI` never pauses the game, and a screen that replaces another one calls neither hook.

`GameUIBridge` extends `SceneUIBridge`, so the scene stays under the UIs.
`GameApp` registers it in place of `SceneUIBridge`, and captures the mouse for its game:

```java file="com/example/running/GameApp.java" region="game"
```

In the loop, the game skips its update while `isPaused()` returns `true`:

```java file="com/example/running/GameApp.java" region="pause"
```

<figure>

![A dark scene where a blue square slides from side to side, with the hint "Tab: menu" of HostOverlayUI in its corner and no pointer; Tab, shown in a badge, opens the Menu card of HostMenuUI, the pointer appears and the square stops; Escape, shown in a badge, closes the card, the pointer disappears and the square slides again](/captures/running-embedding-pause.webp)

<figcaption>
The scene of the host shows around the overlay and under the screen.
Only the screen pauses the game and frees the mouse.
</figcaption>
</figure>

<details>
<summary>The complete <code>HostMenuUI.java</code></summary>

```java file="com/example/running/HostMenuUI.java"
```

</details>

## Draw a UI inside your own projection

JOID draws each UI with its own orthographic projection over the window.
`@UIData(projection = false)` keeps the projection already in place on the render bridge ([[IRenderBridge#getProjection]]) instead: a host that sets a perspective there draws the UI inside its 3D scene.
`zlevel` of [[UIData]] adds a depth offset before the UI draws, so that the UIs of that scene keep their order under the depth test.

## Add a UI bridge of your own

A second UI bridge gives a group of UIs its own place in the window, with its own routing and tooltips.
For example, a library shows its panels above every UI of the application.
Each bridge has a **priority**, an `int`: the highest priority receives the input first and is drawn on top.

<figure>

![Three stacked dark boxes: PanelUIBridge with priority 10 on top, your bridge with priority 0 in the middle, and WindowUIBridge, the default that clears the window, at the bottom. A blue arrow on the left goes down, labeled Input; a blue arrow on the right goes up, labeled Drawing.](/captures/running-embedding-bridges.webp)

<figcaption>
Input tries the highest bridge first; drawing starts from the lowest, so the highest is drawn on top.
</figcaption>
</figure>

The bridge decides which UIs it hosts with `canHandle`:

```java file="com/example/running/PanelUIBridge.java"
```

The library registers it with a priority, once JOID is ready, and gives it its own tooltip style:

```java file="com/example/running/PanelPlugin.java" region="ready"
```

The UI registry, [[BridgeHandler#UI]], orders its bridges by priority:

- [[BridgeRegistry#register]] without a priority uses `0`.
  Between equal priorities, the latest registered counts as the highest, and registering a bridge again moves it.
- The backend adds its window UI bridge only into an empty registry, and below every priority, so any bridge you register later sits above it.
- [[JOID#open]] gives the UI to the highest bridge whose `canHandle` accepts it.
  An open UI stays with the bridge that holds it, whatever you register afterwards.
- Input goes to the bridges from the highest and stops at the first one that consumes it.
  A key release goes to every bridge, and a mouse release to every bridge that received the press.
- Only the lowest bridge clears the window, so the bridges above it never clear the window background.
- Each bridge draws its own tooltips with its own `hoverInfo`, and the window shows the cursor of the hovered node of the highest bridge that has one, the default arrow when that node and its parents set none.

[[BridgeRegistry#getBridgeList]] lists the bridges from the lowest to the highest priority, and [[BridgeRegistry#unregister]] removes one.
Writing the contract of a UI bridge of your own belongs to the Build on JOID documentation.

:::note
`JOID.open` throws `IllegalStateException: No IUIBridge can open …: register one whose canHandle accepts it` when no bridge accepts the UI, for example when every bridge is a `PanelUIBridge`.
:::

## Recap

- Call **`frame()`** once per frame, between polling the events and showing the frame.
- An event that no UI **consumes** reaches the GLFW callbacks you set **before `register()`**.
- To draw JOID **over your own scene**, register before the backend a bridge whose `drawBackground()` clears only the depth and the stencil.
- **`attachScreen()`** and **`detachScreen()`** tell your bridge when the first JOID screen or popup opens and when the last one closes.
- **`projection = false`** draws a UI inside the projection of the host, and `zlevel` keeps the UIs of that scene in order.
- **`register(bridge, priority)`**: the highest priority receives input first and is drawn on top; equal priorities favor the latest.
- The window UI bridge of the backend is a **default**: it sits below every bridge you register and clears the window.
- `canHandle` decides which bridge receives a UI; an open UI **stays with its bridge**.

## Try it

**Panels under the game.**
The application registers a UI bridge of its own with `register(game)`, and opens its UIs there.
Your panels must stay below them.
Which priority do you give the bridge of the panels?

<details>
<summary>Solution</summary>

A negative one, such as `BridgeHandler.UI.register(panels, -10)`: the bridge of the application has the priority `0`, so the panels draw under it and receive the input after it.
No priority goes below the default window UI bridge, so panels can never sit under the UIs it holds.

</details>

**Replace a screen.**
`GameUIBridge` pauses the game when the menu opens.
The player opens the settings from the menu, which replaces the menu, then closes them: is the game still paused?

<details>
<summary>Solution</summary>

No.
Replacing the menu by the settings calls neither hook, so the game stays paused while the settings are open, and closing the last screen calls `detachScreen()`, which resumes it.

</details>
