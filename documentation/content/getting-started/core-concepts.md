# Core Concepts

A short mental model for building with JOID. Each concept has a dedicated page — this one is the map.

## Bootstrap

Before opening any UI, initialize the library once with `JOID.inst().load()`. It's a builder — chain the flags you care about and finish with `load()`:

```java
JOID.inst()
    .setConfigDir(new File("config"))
    .setDevMode(false)
    .setDemoMode(false)
    .load();
```

### `setConfigDir(File)`

Root folder for persistent state. `UIStore` JSON files (`@UIStoreData`) and any internal persistence land under this directory. If it doesn't exist, JOID creates it on `load()`. Defaults to `./config`.

### `setDevMode(boolean)`

Enables development-only behavior. When `true`:

- **Alt-drag** on a node prints its coordinates and lets you move it live — useful while laying out by eye.
- **Alt+arrow keys** nudge the hovered node by one pixel.
- **Profiler overlay** is available when the UI declares `@UIData(debug = @Debug(profiler = true))`.
- **Hot-reload** watches source files and re-runs `init()` when they change (same `@Debug` flag).
- **Layout introspection** logs are emitted for structural issues.

Leave it `false` in production — the debug gestures and file-watcher are unnecessary overhead, and the Alt-key bindings may collide with your own shortcuts.

### `setDemoMode(boolean)`

Loads the bundled `DemoFont` (shipped with the `-dev` artifact) so the quick-start snippets, demo UIs, and documentation examples have a usable font without you providing your own MSDF atlas. Once you ship your own fonts via a `CustomFontProvider`, turn it off.

`load()` must be called **exactly once**, before registering bridges or opening UIs.

## The UI root

Every screen is a class extending `UI` (which implements `IUI`). The hooks you override are defined on `IUI` and have sensible defaults — implement only what you need:

1. `new YourUI()` — constructor.
2. `JOID.open(ui)` — hands the UI to its bridge.
3. `init()` — you attach nodes here, once.
4. Each frame: `preDraw(mouseX, mouseY)` → internal node rendering → `postDraw(mouseX, mouseY)`, plus `update()` for per-frame logic.
5. `close()` — return `true` if the UI can close (the default is `true`).

When the UI is granted closure, JOID calls the internal `properlyClose()`: stores are saved, nodes are detached, and the bridge removes the UI. Rendering into the overall draw loop goes through `UI.draw(mouseX, mouseY)`, which is `final` — use `drawBackground`, `preDraw`, and `postDraw` for your own drawing.

UIs are configured via the `@UIData` annotation (zoomable, pausable, backgroundColor, closeable…). See `UI Class`.

## The node tree

Everything visible is a `Node`. Nodes form a tree:

```
UI
├── FlexNode
│   ├── RectNode
│   ├── RectNode
│   └── TextNode
└── ContainerNode
    └── ImageNode
```

Each node knows its position (`x`, `y`), size (`width`, `height`), z-index, parent, and children. You build the tree via **fluent chains** ending in `.attach(parent)`.

```java
RectNode.create(0, 0, 100, 50)
    .color(Color.RED)
    .onClick(handler)
    .attach(parent);
```

**Builder pattern rules** (respect them and chain ergonomically):

- `create(...)` constructs.
- Setters return `this` typed as `<T extends Node>` for subclass-safe chaining.
- `body(consumer)` configures children inline.
- `attach(parent)` is always last, attaches the node.

See [Node Fundamentals](../nodes/node-fundamentals.md).

## Layout vs design nodes

Two families of nodes:

- **Structure nodes** compute layout for their children — `FlexNode`, `GridNode`, `ContainerNode`, `ScrollbarNode`. You rarely override their rendering.
- **Design nodes** draw content — `RectNode`, `CircleNode`, `TextNode`, `ResourceNode`, `TextFieldNode`, `VideoPlayerNode`. You style them with effects and colors.

A well-built UI is mostly *design nodes inside structure nodes*.

## Effects

Effects are post-processing applied to a node's rendered output. They come in two flavors:

- **Shape effects**: `RoundedNodeEffect`, `CircleNodeEffect` — modify the silhouette.
- **Shader effects**: `BlurNodeEffect`, `BorderNodeEffect`, `GradientNodeEffect` — full GL shader passes composed through the [Shader Pipeline](../shaders/pipeline.md).

Chain multiple effects; they compose in order of priority.

```java
RectNode.create(0, 0, 200, 100)
    .color(Color.BLUE)
    .effect(RoundedNodeEffect.create(16F))
    .effect(BorderNodeEffect.create(Color.WHITE, 2F))
    .effect(BlurNodeEffect.create(4F))
    .attach(parent);
```

## Reactive state

State lives in `Signal<T>` observables. Nodes subscribe via `.watch(signal, property)`:

```java
final StringSignal name = new StringSignal("world");

TextNode.create(0, 0)
    .text(() -> Text.create("Hello, " + name.getOrDefault(), info))
    .watch(name)  // auto-reloads on name.set(...)
    .attach(parent);
```

The `WatchProperty` enum controls what happens on change — `RELOAD` (default, re-runs `init()`), `BODY` (re-runs the body consumer only), `CLEAR_CHILDREN`, or `NONE`. See [Signals](../state/signals.md) and [Watch](../state/watch.md).

For cross-UI / persistent state, use `UIStore` with `@UIStoreData` annotations — serialized to JSON in `config/store/`. See [Stores](../state/stores.md).

## Resources

Anything visual that isn't a shape or text is a `Resource`: image, video, GIF. Load via the `ResourceBuilder`:

```java
Resource image = Resource.of(MyClass.class.getResourceAsStream("/icon.png"));
Resource remote = Resource.of("https://example.com/image.png");
Resource video = Resource.of(MyClass.class.getResourceAsStream("/movie.mp4"));  // auto-detected
```

Put them into `ResourceNode`, `VideoPlayerNode`, or draw directly via `DrawUtils.RESOURCE`. See [ResourceBuilder](../resources/resource-builder.md).

## Best practices

- **Build in `init()`, mutate in signals.** Don't call `this.append(...)` from `draw()` — rebuild triggers a full reload, which is expensive.
- **Use structure nodes for layout.** A hand-placed `RectNode` grid is a code smell; `GridNode` exists.
- **Cache your `TextInfo`**. It holds font, size, color references — creating one per frame is wasteful.
- **Release heavy resources.** `VideoPlayerNode` already does this via `detach()`; for your own heavy decoders, override `detach()` similarly.
- **Favor `toGradient(other)` over manual gradient shaders.** `Color` supports gradients natively and the renderer picks the right shader automatically.

## Rendering model

Each frame:

1. `UI.onUpdate()` — recursive `update()` on every node, tween advance.
2. `UI.draw()` — projection setup, then recursive `Node.render()` starting from the root.
3. Each `Node.render()`:
   - runs `CALLBACK_MOUNT` once,
   - splits effects into shader / non-shader,
   - non-shader effects wrap `pre/post`,
   - shader effects build a `ShaderPass` list and delegate to `ShaderPipeline.render(node, passes, baseDraw)`,
   - `baseDraw` masks to the node's bounds, renders children, and calls your `draw()`.

You almost never need to care about this internally — but knowing it helps when debugging GL states.

Now jump into [UI Class](../ui/ui-class.md) or [Node Fundamentals](../nodes/node-fundamentals.md) depending on what you want to build first.
