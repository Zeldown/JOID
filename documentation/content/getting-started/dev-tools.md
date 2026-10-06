# Developer Tools

JOID has a dev mode with an on-screen inspector, reload and zoom shortcuts, a profiler, hot reload and extra warnings, and a demo mode that runs the bundled demo UIs. Both exist only in the `-dev` jars: use them while you build your UIs, and ship the `-prod` jar.

## Enabling the dev and demo modes

Turn the modes on before `load()`, which loads the fonts the tools draw with:

```java
JOID.inst().setDevMode(true).setDemoMode(true).load();
```

| Mode | Turned on with | What it adds |
| --- | --- | --- |
| Dev mode | `setDevMode(true)` | The `DevNode` inspector, the dev shortcuts, Alt + wheel zoom, the profiler, hot reload, dev warnings. Loads the bundled Montserrat family used by the inspector. |
| Demo mode | `setDemoMode(true)` | Loads `DemoFont` (`dev.joid.demo`): `MONTSERRAT`, `PACIFICO` and `PLAYFAIR_DISPLAY`, used by the demo UIs. |

The `-prod` jars do not contain the classes and assets of these modes. There, `setDevMode(true)` throws `IllegalStateException("The dev mode is not part of the prod jar of JOID, use the dev jar of your backend")` and `setDemoMode(true)` throws the same message for the demo mode. `setDevMode(false)` and `setDemoMode(false)` always succeed. Query the modes with `JOID.inst().isDevMode()` and `isDemoMode()`.

> NOTE: The bundled fonts are TTF files. Their MSDF atlases are generated into the MSDF cache the first time they load, which makes the first start in dev or demo mode slower; later starts read the cache. See [Fonts](../fonts/adding-fonts.md).

## Keyboard and mouse shortcuts

Shortcuts of the UI that receives the key (the dev shortcuts only work in dev mode, and only when no node, keybind or zoom key consumed the key first):

| Shortcut | Mode | Effect |
| --- | --- | --- |
| `F3` | Dev | Shows or hides the `DevNode` inspector panel. |
| `Ctrl+R` (left Ctrl) or `F5` | Dev | Reloads the UI: runs `init()` again on a fresh tree. |
| `Ctrl+Shift+R` (left Ctrl and left Shift) | Dev | Resets the zoom to 1, then reloads. |
| Left `Alt` + mouse wheel | Dev | Zooms by wheel value / 10000 (so 0.012 for a 120-unit notch); with left `Shift` held, wheel value / 1000. The scroll does not reach the nodes. |
| `Ctrl` or `Alt` + `+` (character or numpad) | All modes | Zooms in by 0.1, when the UI is `zoomable`. |
| `Ctrl` or `Alt` + `-` (character or numpad) | All modes | Zooms out by 0.1, when the UI is `zoomable`. |

The zoom stays between 0.1 and its maximum; see [View and Scaling](../ui/view-and-scaling.md).

## The DevNode inspector

In dev mode, every UI creates a `DevNode` (`dev.joid.lib.ui.node.impl.dev`) when it loads; `F3` attaches it to the UI or removes it, and it stays across reloads. The panel sits in the bottom-right corner of the canvas, above every other node, is visible and usable only while its UI is on top, and can be dragged anywhere on the screen. `ui.getDevNode()` returns it (typed `Node`, `null` outside the dev mode).

The panel shows four tool buttons and the frame rate of the UI:

| Button | Key | Default | Effect |
| --- | --- | --- | --- |
| Inspect | `I` | On | Highlights the node under the mouse with its class name, the number of times it was loaded or reloaded, and its last render time. |
| Reload | `R` (without left Ctrl) | | Reloads the UI. The button flashes on every reload. |
| Update | `U` | On | Outlines in red every node loaded or reloaded in the last 2 seconds, fading out; useful to see what a signal rebuilds. |
| Grid | `G` | Off | Draws the center lines of the canvas and the distances in canvas units from the mouse to each edge. Right-click switches its color between blue and red. |

The single-letter keys work while the panel is shown and do not consume the key.

### Inspecting a node

With Inspect on:

| Action | Effect |
| --- | --- |
| Move the mouse | Highlights the front-most node under the cursor. Hold `Ctrl` to pick the back-most one instead. |
| Left-click | Locks the highlighted node and expands the panel with its details. The click does not reach the node. |
| Right-click | Unlocks and collapses the panel. |
| `Enter` or numpad `Enter` | Selects the parent of the inspected node. |
| Click the `hierarchy` line | Selects the parent. |
| Click a child name | Selects that child. |

The details list the node's class, update count and render time, its bounds (`position`, `x` and `y` as relative / absolute (default), `width` and `height` with their defaults), the number of callbacks of each type, its hierarchy and children, and the value of every non-static field declared by its class and its superclasses below `Node`.

> TIP: Press `I` to turn Inspect off when you want to click your own nodes while the panel is shown.

## Profiler

Each UI has a profiler, on by default, configured with `@UIDataDebug` (`dev.joid.lib.ui.core.data.debug`). In dev mode it prints:

- `Starting load...` and `Load completed in <ms>ms` around the first load of the UI, on `System.out`;
- `[!] Frame took <ms>ms to render` on `System.err` for every frame of the UI that takes more than 16.66 ms to draw.

```java
@UIDataDebug(profiler = false)
public final class MenuUI extends UI {}
```

`ui.getDebug()` returns the settings of a UI as a `UIDataDebugObject`, whose `setProfiler(boolean)` and `setHotreload(boolean)` change them at runtime. The annotation is looked up on the class, then on its superclasses.

| `@UIDataDebug` attribute | Default | Effect in dev mode |
| --- | --- | --- |
| `profiler` | `true` | Load time and slow frame messages. |
| `hotreload` | `true` | Reload when the file of the UI class changes. |

## Hot reload

In dev mode, a UI with `hotreload` on watches the file its class was loaded from (the location of its code source), polling every 500 ms. When that file changes, the UI waits one second and reloads, printing `Detected file change`, `Starting reload...` and `Reload completed in <ms>ms`. It prints `Hot-reload enabled on <class>` when the watch starts, and stops watching when the UI closes.

- The watched file is the jar that contains the UI class. When the class is loaded from a folder of class files, the location is that folder, and changes of the class files inside it do not trigger a reload.
- JOID does not load new classes: the reload runs `init()` again with the code currently loaded in the JVM. Combine it with your IDE's HotSwap in debug mode to pick up changed method bodies.
- The reload runs on the watcher thread, not on the thread that draws the UI.

## Warnings and logs in dev mode

| Message | Stream | When |
| --- | --- | --- |
| `[JOID] The font weight <w> is not loaded in the family of <font>, <w2> is drawn instead (loaded: ...)` followed by the stack trace of the code that created the text | `System.err` | A text asks for a weight its font family does not have. Printed once per missing weight and family. |
| `[JOID] Font <name> generated into the msdf cache (...) in <ms>ms`, or `read from the msdf cache`, or `read from a .msdf file` | `System.out` | Each font face loaded by `MsdfFontLoader`. |
| Profiler and hot reload messages | `System.out` / `System.err` | See above. |

In dev mode, JOID also records where each text element is created (for the font weight warning), and `Node.toString()` returns indented JSON with extra fields (scroll, visibility, hover, hierarchy).

Outside the dev mode, `JOID.checkVersion` still prints `[JOID] This backend targets JOID <x> but JOID <y> is loaded` when a backend targets another major version.

## The demo UIs and the demo window

Each backend `-dev` jar contains a demo window, `dev.joid.impl.lwjgl2.demo.DemoWindow`, `dev.joid.impl.lwjgl3.demo.DemoWindow` or `dev.joid.impl.vulkan.demo.DemoWindow`. Its `main` method opens a resizable 1920×1080 window titled `JOID - Demo (<engine>)`, registers the backend and itself as the UI bridge (a `DemoUIBridge`), turns the dev and demo modes on, and opens the `UIDemoChoice` menu.

Run it from the repository with `./gradlew :lwjgl3:runDemo` (also `:lwjgl2:runDemo`, `:vulkan:runDemo`), or from a release jar with the classpath of [Installation](installation.md):

```
java -cp "joid-lwjgl3-8.0.0-dev.jar:libs/*" dev.joid.impl.lwjgl3.demo.DemoWindow
```

Use `;` instead of `:` as classpath separator on Windows, and add `-XstartOnFirstThread` on macOS for LWJGL 3 and Vulkan.

In the demo, click an entry of the menu to open it; it slides in. `Escape` in a demo UI goes back to the menu. `Ctrl+K` on the menu opens a popup with a text field, and `Ctrl+K` in a popup opens another one.

| Demo UI | Shows | Read |
| --- | --- | --- |
| `UIDemoAnimation` | Rectangles animated with tweens | [TweenAnimator](../animation/tween-animator.md) |
| `UIDemoSimple` | Nested rectangles, hover colors, tooltips, click logging, `preDraw`/`postDraw` drawing | [Node Fundamentals](../nodes/node-fundamentals.md) |
| `UIDemoOverflow` | Scrolling containers, custom scrollbars, masks, scroll callbacks | [Overflow and Scrolling](../nodes/layout/overflow-and-scroll.md) |
| `UIDemoDraggable` | Draggable nodes | [Drag and Drop](../interactions/drag-drop.md) |
| `UIDemoFont` | Font families, weights, markup and text effects | [Markup and Text Effects](../text/markup-and-effects.md) |
| `UIDemoFlex` | Flex layouts | [FlexNode](../nodes/layout/flex.md) |
| `UIDemoReorderable` | Drag-to-reorder lists | [ReorderableFlexNode](../nodes/layout/reorderable-flex.md) |
| `UIDemoResource` | Images, vector images, animated images and videos | [ResourceNode](../nodes/visual/resource.md) |
| `UIDemoShader` | Built-in and custom effects | [Effects](../styling/effects.md) |
| `UIDemoWait` | A node waiting for a signal, with a skeleton | [Watching Signals](../state/watch.md) |
| `UIDemoWatch` | A node watching a list signal | [Watching Signals](../state/watch.md) |
| `UIDemoTextField` | Single-line and multiline text fields | [TextFieldNode](../nodes/input/text-field.md) |
| `UIDemoSelector` | Selectors | [SelectorNode](../nodes/input/selector.md) |
| `UIDemoGrid` | Grid layouts | [GridNode](../nodes/layout/grid.md) |
| `UIDemoSlider` | Sliders | [SliderNode](../nodes/input/slider.md) |
| `UIDemoCheckbox` | Checkboxes | [CheckboxNode](../nodes/input/checkbox.md) |
| `UIDemoToggle` | Toggles | [ToggleNode](../nodes/input/toggle.md) |
| `UIDemoSwitch` | Switches | [SwitchNode](../nodes/input/switch.md) |
| `UIDemoChart` | Line and radar charts | [ChartNode](../nodes/data/chart.md) |
| `UIDemoStore`, `UIDemoOtherStore` | Global, local and permanent stores shared between two UIs | [Stores](../state/stores.md) |
| `UIDemoPopup` | A popup UI | [Opening and Closing UIs](../ui/managing-uis.md) |

The sources of these UIs, in `dev.joid.demo`, are working examples of every feature. The `DemoUIBridge` they run in replaces the open UIs when a non-popup UI opens; see [Opening and Closing UIs](../ui/managing-uis.md#what-open-does-depends-on-the-bridge).

## See also

- [Installation](installation.md)
- [The UI Class](../ui/ui-class.md)
- [View and Scaling](../ui/view-and-scaling.md)
- [Fonts](../fonts/adding-fonts.md)