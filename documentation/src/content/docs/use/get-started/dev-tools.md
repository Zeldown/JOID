---
title: "Dev tools"
description: "Inspect, reload and profile a UI with the dev jar, and read the warnings of JOID."
genre: learn
sidebar:
  order: 3
---

While you build a UI, the dev jar adds an inspector, reload shortcuts, a hot reload, a profiler and warnings.
The prod jar has none of them, so the application you ship carries no debug code.

**You will learn**
- How to turn the dev tools on
- How to inspect the nodes of a UI
- How to reload a UI while you work
- How to find slow loads and slow frames
- How to read the warnings of JOID

## Turn the dev tools on

Put the dev jar of your backend on the classpath, as [Installation](/use/get-started/installation/#choose-the-dev-or-the-prod-jar) shows: JOID turns its **dev mode** on when it finds the dev classes, and there is nothing to call.
The line that JOID prints when the backend registers confirms it with `loaded in dev mode`.

Your own code reads the mode with [[JOIDEnv#isDevMode]], for example to show a text during development only:

```java fragment
if (JOIDEnv.isDevMode()) {
	TextNode.create(20, 20).text(Text.create("Dev build", style)).attach(this);
}
```

[[JOIDEnv#devMode]]`(false)` turns the dev mode off while the dev jar runs, to see a screen as the prod jar draws it; turning it on throws `IllegalStateException` on the prod jar, which has no dev classes.

## Inspect a UI

Press `F3` to show or hide the dev panel, in the bottom-right corner of the UI on top.
Drag it to move it.
Its three buttons are Inspect, Reload and Grid, next to the frame rate of the UI; hover one to read its shortcut.
Inspect is on when the panel shows.

<figure>

![A small dark card titled Profile; F3, shown in a badge, brings up the dev panel in the bottom-right corner of the window; the pointer rests on its Inspect icon, which shows the tooltip "[I] Inspect", then drags the panel by its frame rate toward the card and back to its corner, and F3 hides it again](/captures/start-dev-move.webp)

<figcaption>
The panel stays out of the way: drag it off the part of the UI that you are checking.
</figcaption>
</figure>

<figure>

![The Profile card, drawn large; the pointer moves onto its title, which gets a dashed blue frame under a dark label that reads TextNode and 0.00ms, then onto the blue bar, labelled RectNode; while Ctrl is held, shown in a badge, the frame jumps to the whole card behind the bar](/captures/start-dev-inspect.webp)

<figcaption>
With Inspect on, the node under the pointer is outlined and named, with its render time in milliseconds; `Ctrl` reaches the node behind it.
</figcaption>
</figure>

| Key or click, panel shown | Effect |
|---|---|
| `I` | Turns Inspect off or on: the node under the pointer is outlined, with its class and its render time. Hold `Ctrl` to outline the node at the back of the pile under the pointer instead of the one in front. |
| Left click | Locks the outlined node; the panel grows and lists its bounds, its listeners, its hierarchy, its children and the values of its fields, and the wheel scrolls the list. A click on the hierarchy line locks the parent, a click on a child locks that child. |
| `Enter` | Locks the parent of the inspected node. |
| Right click | Unlocks the node. |
| `G` | Shows the grid: the center lines of the canvas and the distances from the pointer to its four edges, in design pixels. A right click changes its color. |
| `R` | Reloads the UI, as `Ctrl+R` does. |

<figure>

![The Profile card and the dev panel; a click on the card locks it, a dashed blue frame stays around it and the panel grows to list its bounds, x 760, y 330, width 200 and height 240; the wheel, shown in a badge, scrolls the list down to the hierarchy and the two children, TextNode and FlexNode; a click on FlexNode moves the lock to the column of bars; Enter, shown in a badge, locks the card again, and a right click unlocks it](/captures/start-dev-panel.webp)

<figcaption>
A locked node shows where it is, what listens to it and what it holds: walk down to a child or up to the parent to find the node that is off.
</figcaption>
</figure>

<figure>

![The middle of the canvas of the counter, crossed by two blue lines through the center of the canvas and by four blue lines from the pointer to the four edges, each labelled with its length in pixels; as the pointer moves, the lines and the four lengths follow it; a right click turns the grid red, a second one blue again](/captures/start-dev-grid.webp)

<figcaption>
The grid measures the canvas from the pointer: copy a position from a mockup and check it here, in the color that stands out on your UI.
</figcaption>
</figure>

The dev mode adds zoom shortcuts to every zoomable UI: `Ctrl` or `Alt` with `+` and `-` [zoom](/use/learn/describing-the-ui/design-canvas/#zoom-a-ui) it by steps of 0.1, and `Alt` with the wheel by small steps, with `Shift` for larger ones.

<figure>

![The blue Clicks button of the counter; Ctrl + -, shown in a badge, makes the whole UI smaller twice, then Ctrl + = brings it back to its size in two steps](/captures/start-dev-zoom.webp)

<figcaption>
Zoom a UI to check it smaller or larger without changing its code or the window.
</figcaption>
</figure>

## Reload a UI while you work

| Shortcut | Calls |
|---|---|
| `Ctrl+R` or `F5` | [[UI#reload]] |
| `Ctrl+Shift+R` or `Shift+F5` | [[UI#renew]] |

<figure>

![The blue Clicks button of the counter; the pointer clicks it three times and it reads Clicks: 3; Ctrl + R, shown in a badge, reloads the UI and the count stays at 3; Ctrl + Shift + R renews it and the count goes back to 0](/captures/start-dev-reload.webp)

<figcaption>
A reload keeps the fields of the UI; a renew starts from a new instance.
</figcaption>
</figure>

[Rebuild an open UI](/use/learn/describing-the-ui/uis/#rebuild-an-open-ui) explains what each of them keeps.
A reload that fails prints the exception instead of stopping the program: `[JOID] The reload of MenuUI failed (…), fix init() then reload it again`.
A UI without a constructor without argument cannot be renewed: `[JOID] The UI … has no constructor without argument, it cannot be renewed: use Ctrl + R to reload it instead`.

**Hot reload** reloads a UI by itself.
JOID watches the folder or the jar file that the classes of the UI come from, and reloads the UI about a second after a `.class` file of that folder, or the jar file, changes.
The console tells you each step, and every reload, by a shortcut or by hot reload, prints its time on one line:

```text
[JOID] Hot reload is on for MenuUI, it reloads when its classes change
[JOID] MenuUI.class changed, the UIs that use it reload
[JOID] MenuUI reloaded in 4.12ms
```

JOID loads no new code itself: start your application in debug mode from your IDE, so that the IDE replaces the method bodies when you compile, and hot reload runs the new `init()`.
A change that the JVM cannot swap, such as an added field or an added method, needs a restart.

When the classes of a UI come from neither a folder nor a jar file, JOID prints `[JOID] Hot reload is off for …: its classes are not loaded from a folder or a jar file, run it from one of them to reload it` once.

## Find slow loads and slow frames

The profiler writes each load of a UI on the standard output, except a reload, which prints its own line, and each slow frame on the error output:

```text
[JOID] MenuUI loaded in 18.40ms
[JOID] MenuUI took 21.73ms to draw a frame, more than the 16.66ms of 60 fps: …
```

The slow-frame line ends with its advice: move the heavy work out of `draw()`, or spread it over several frames.
The inspector tells you which node is slow: its label gives the render time of the node under the pointer.
[Performance](/use/in-depth/performance/#find-the-slow-part) explains what to look for.

Turn the profiler or the hot reload off for one UI with [[UIDataDebug]], for example a UI that is slow on purpose or loaded from generated classes:

```java fragment
@UIDataDebug(profiler = false, hotreload = false)
public class ChartUI extends UI {
```

While the UI runs, [[UI#getDebug]] changes the same settings: `super.getDebug().profiler(false)` silences the profiler at once, and a change of `hotreload` applies at the next frame.

## Read the warnings

When JOID cannot do what your code asks, it prints a line that starts with `[JOID]` on the error output, most of the time once per cause.
For example: a setter that cannot follow its signals, a shader that the driver refuses, a resource that cannot be read, a font weight that the font does not have, a window UI bridge without a tooltip style.

Most warnings print with the dev jar only.
The dev mode also draws a resource that cannot be read as a magenta and black checkerboard, where the prod jar draws nothing ([Know when an image is loaded](/use/learn/describing-the-ui/media/#know-when-an-image-is-loaded)).

[Warnings and errors](/reference/errors/) lists every message with its cause and its fix.

:::tip
Run your application with the dev jar until the error output shows no `[JOID]` line: each one points at code that does not draw what you expect.
:::

Before you ship, [Ship your application](/use/guides/ship-your-application/) switches to the prod jar and checks what changes.

## Recap

- The **dev jar** turns the **dev mode** on by itself; `JOIDEnv.isDevMode()` reads it.
- **`F3`** shows the dev panel: **Inspect** (`I`), **Grid** (`G`), **Reload** (`R`); a click locks a node and lists its properties.
- **`Ctrl+R`** runs `init()` again on the same instance; **`Ctrl+Shift+R`** renews the UI from its constructor.
- **Hot reload** reloads a UI when its classes change; pair it with the debug mode of your IDE.
- The **profiler** prints load times and frames over 16.66 ms; `@UIDataDebug` turns it off for one UI.
- **`[JOID]`** warnings explain what JOID could not do; the reference lists them all.

## Try it

**A reload that changes nothing.**
You changed a color in `init()` and pressed `Ctrl+R`, but the UI still shows the old color.
What is missing?

<details>
<summary>Solution</summary>

The new code never reached the JVM.
Start the application in debug mode so that the IDE swaps the method body when you compile; then `Ctrl+R`, or the hot reload, runs the new `init()`.

</details>

**A counter from zero.**
A counter of your UI must start from zero each time you test a change.
Which shortcut do you use?

<details>
<summary>Solution</summary>

`Ctrl+Shift+R`: renewing creates a new instance, so every field starts again from its initial value.
`Ctrl+R` keeps the instance and its counter.

</details>
