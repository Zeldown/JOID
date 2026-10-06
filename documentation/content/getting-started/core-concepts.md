# Core Concepts

This page is the mental model behind what you used in the [Tutorial](../tutorial/setup.md): the singleton you configure once, the bridges that connect JOID to a host, the UIs and their node trees, the virtual canvas, what happens in a frame, and the conventions of the fluent API. Read it after the tutorial, or whenever something in JOID surprises you; each section links to the page that covers the topic in full.

## The JOID singleton

`JOID` (`dev.joid.internal.JOID`) is the entry point. `JOID.inst()` returns the single instance, creating it on first use. Configure it with chained setters, then call `load()` once at startup, after registering the bridges and before opening UIs:

```java
JOID
.inst()
.setConfigDir(new File("config"))
.setDevMode(false)
.setDemoMode(false)
.load();
```

| Member | Description |
| --- | --- |
| `static JOID inst()` | The singleton. Its constructor creates the `config` folder in the working directory if it does not exist. |
| `JOID setConfigDir(File configDir)` | Folder for persistent data: stores are written to `<configDir>/store`, UI properties to `<configDir>/property`. Default: `new File("config")`. |
| `JOID setDevMode(boolean devMode)` | Turns the developer tools on or off. Default `false`. Throws `IllegalStateException` when turned on with a `-prod` jar. See [Developer Tools](dev-tools.md). |
| `JOID setDemoMode(boolean demoMode)` | Loads the demo fonts used by the demo UIs. Default `false`. Throws `IllegalStateException` when turned on with a `-prod` jar. |
| `JOID load()` | Creates the configuration folder if missing, prints a banner with the settings and the version, and loads the bundled fonts when the dev or demo mode is on. |
| `File getConfigDir()`, `boolean isDevMode()`, `boolean isDemoMode()` | The current settings. |
| `static final String VERSION` | The library version, `"8.0.0"`. |
| `static boolean checkVersion(String version)` | For backend authors: `true` when `version` has the same major version as the loaded JOID, otherwise prints a warning and returns `false`. See [Writing a Backend](../integration/writing-a-backend.md). |

`JOID` also holds the static methods that open and close UIs (`open`, `close`, `isOpen`, `getUI`), described in [Opening and Closing UIs](../ui/managing-uis.md).

## Bridges

The core never calls a windowing, graphics or audio API itself. It goes through bridges registered in `BridgeHandler` (`dev.joid.lib.bridge`):

| Registry | Interface | Provided by |
| --- | --- | --- |
| `BridgeHandler.UI` | `IUIBridge` | You or your host: holds the open UIs, feeds them input, updates and draws them. Usually a subclass of `UIBridge`. |
| `BridgeHandler.WINDOW` | `IWindowBridge` | The backend: window size, mouse position, key states, clipboard. |
| `BridgeHandler.RENDER` | `IRenderBridge` | The backend: matrices, render state, textures, shaders, framebuffers, draw calls. |
| `BridgeHandler.AUDIO` | `IAudioBridge` | The backend: audio sources for video playback. |
| `BridgeHandler.CLOCK` | `IClockBridge` | Registered by default (`SystemClockBridge`); tests register a `ManualClockBridge`. |

This is what makes JOID renderer-agnostic: your UIs depend only on these interfaces, never on the engine behind them. Moving to another backend, or to a new version of an engine, changes the backend you register and nothing in your UI code, and the rendering stays the same.

A backend's `Backend.register(...)` registers the window, render and audio bridges; you register the UI bridge. Using JOID before the window or render bridge is registered fails with an `IllegalStateException` that names the missing bridge. See [Bridges](../integration/bridges.md).

## UIs and the node tree

A screen is a subclass of `UI` (`dev.joid.lib.ui.core`). It is opened with `JOID.open(ui)`, which hands it to the UI bridge that accepts it. When the bridge loads it the first time, the UI runs `init()`, where you build its tree of nodes:

```
CounterUI
├── RectNode  (button)
│   └── TextNode
└── TextNode  (counter)
```

Nodes (`dev.joid.lib.ui.node.Node` and its subclasses) are retained: they stay in memory between frames, keep their state, and are drawn every frame until removed. Layout nodes (`ContainerNode`, `FlexNode`, `GridNode`, `ReorderableFlexNode`) place their children; visual nodes (`RectNode`, `TextNode`, `ResourceNode`...) draw; input nodes (`TextFieldNode`, `SliderNode`...) handle the user. A child's position is relative to its parent.

Several UIs can be open at once, ordered by their `zlevel`; the last one is on top and receives input first. See [The UI Class](../ui/ui-class.md), [Opening and Closing UIs](../ui/managing-uis.md) and [Node Fundamentals](../nodes/node-fundamentals.md).

## The virtual canvas

You design every UI on a virtual canvas of 1920×1080 units. Positions, sizes and mouse coordinates in nodes and UI hooks are in those units. Each UI owns a `UIView` that fits the canvas into the window without stretching it: a 1280×720 window shows the canvas at two thirds of its size, and a window wider or taller than 16:9 shows more canvas on the sides. On top of that fit, the bridge's interface scale and the user's zoom scale the canvas around the UI's anchor. See [View and Scaling](../ui/view-and-scaling.md).

## The frame lifecycle

The host drives JOID. A frame of the loop you wrote in the [Quick Start](quick-start.md) or the [Tutorial](../tutorial/setup.md) runs three phases through the UI bridge:

1. **Input.** The host forwards each event to the bridge: `keyTyped(char, Key)`, `mousePressed(ClickType)`, `mouseReleased(ClickType)`, `mouseDragged(ClickType, long)`, `mouseScroll(int)`. The bridge offers the event to its active, visible UIs from the top down. Inside a UI, the nodes see the event first, the top-most in drawing order first, then the UI's own hook. A UI that consumes the event, or that is a popup, stops it from reaching the UIs below.
2. **Update.** `bridge.update()` calls, for each UI in order, `update()` on its nodes and then the UI's `update()` hook.
3. **Draw.** `bridge.draw()` draws each visible UI in order. A UI first runs its due scheduled tasks and draws its background, then draws its nodes and its `preDraw`/`postDraw` hooks inside its view, and finally the tooltip of the hovered node when it is on top.

Event dispatch uses an `InternalContext` (`dev.joid.lib.utils.context`): a node or hook calls `context.cancel()` to consume the event. See [Callbacks](../interactions/callbacks.md) and [Mouse and Keyboard](../interactions/mouse-and-keyboard.md).

> NOTE: JOID is not thread-safe. Forward input, call `update()` and `draw()`, open and close UIs and change nodes from the thread that owns the graphics context. To run code on that thread from another one, use `ui.schedule(runnable)`: the task list is thread-safe and the task runs at the start of the UI's next draw. Font and resource loading run on background threads and hand their result back through futures and callbacks.

Time in JOID (frame time, scheduled tasks, animations) comes from the clock bridge, in milliseconds.

## Fluent API conventions

JOID builds trees with chained calls. In the snippets below, `this` is the UI being built in `init()` and `info` is a `TextInfo` created as in the [Quick Start](quick-start.md):

```java
RectNode
.create(100, 100, 300, 80)
.color(Color.DARKGRAY)
.onClick((node, mouseX, mouseY, clickType) -> System.out.println("clicked"))
.body(rect -> {
    TextNode.create(rect.dw(2), rect.dh(2)).text(Text.create("Save", info)).anchor(Align.CENTER).attach(rect);
})
.attach(this);
```

| Convention | Meaning |
| --- | --- |
| `create(...)` | Static factory of nodes and effects; some classes add named factories such as `FlexNode.vertical(...)`. Constructors are not public. |
| Setters named after the property | `color(...)`, `anchor(...)`, `zindex(...)`: they return the node, so calls chain. |
| `attach(UI)` / `attach(Node)` | Adds the node to a UI or to a parent node; usually the last call of a chain. |
| `append(Node...)` | Adds children to a node, the reverse of `attach`. |
| `body(Consumer)` / `body(Runnable)` | Runs the given code right away with the node, to create its children inline. The node keeps it so it can run it again (see `WatchProperty.BODY`). |
| `onXxx(callback)` | Registers a callback: `onClick`, `onHover`, `onUpdate`, `onWatch`... See [Callbacks](../interactions/callbacks.md). |

Setters are generic: `public final <T extends Node> T anchor(Align anchor)`. The returned type is inferred by the compiler:

- In a chain, a setter declared in `Node` returns `Node`, and a setter declared in `RectNode` returns `RectNode`. Call the setters of the subclass first, then the ones of `Node`. The `color(...)` of `RectNode` cannot follow `onClick(...)` in a chain.
- The parameter of a callback or `body` lambda has the type the chain has reached: in the example above, `rect` is a `Node`.
- An explicit type argument or an assignment fixes the type: `.<RectNode>body(rect -> ...)` gives a `RectNode` parameter, and `final RectNode button = RectNode.create(...).onClick(...);` compiles.

## Signals and effects at a glance

State lives in signals (`dev.joid.lib.utils.signal`): `Signal<T>` and typed variants such as `IntegerSignal`, `StringSignal` or `ListSignal`. `set(value)` notifies the subscribers when the value changes. Nodes watch signals: on each change, a node reloads to update itself, or rebuilds its children:

```java
final StringSignal name = new StringSignal("world");

TextNode
.create(100, 100)
.text(Text.create("", info))
.<TextNode>onInit(node -> node.getText().text("Hello " + name.getOrDefault()))
.watch(name)
.attach(this);

ContainerNode
.create(100, 200, 400, 300)
.watch(name, WatchProperty.CLEAR_CHILDREN, WatchProperty.BODY)
.body(container -> {
    TextNode.create(0, 0).text(Text.create("Rebuilt for " + name.getOrDefault(), info)).attach(container);
})
.attach(this);
```

The text node reloads on each change, which runs its `onInit` callback again with the new name; the container removes its children and runs its `body` again.

Effects change how a node is drawn. They are applied with `effect(...)` and run through the shader pipeline:

```java
RectNode.create(100, 100, 300, 80).color(Color.BLUE).effect(RoundedNodeEffect.create(16F)).attach(this);
```

See [Signals](../state/signals.md), [Watching Signals](../state/watch.md), [Effects](../styling/effects.md) and [Shader Pipeline](../shaders/pipeline.md).

## Where to go next

The [Essentials](../essentials/uis.md) pages take each of these topics one at a time, with short examples, in this order: UIs, nodes, layout, styling, input, state, text, media and animation.

## See also

- [Quick Start](quick-start.md)
- [Tutorial 4: Polish](../tutorial/polish.md)
- [Essentials: UIs](../essentials/uis.md)
- [The UI Class](../ui/ui-class.md)
- [Node Fundamentals](../nodes/node-fundamentals.md)
- [Bridges](../integration/bridges.md)
- [Developer Tools](dev-tools.md)