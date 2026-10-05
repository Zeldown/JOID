# Bridge

The bridge is the glue between JOID and your host application. It owns the list of active UIs, dispatches input events, and drives rendering.

## Backends and `DemoWindow`

Each backend module (`lwjgl2`, `lwjgl3`, `vulkan`) ships a `DemoWindow` — a ready-to-use window that:

- Registers the window, render and audio bridges of its backend (see [Backends](backends.md)).
- Listens to mouse and keyboard.
- Loops `update` → `render` → present.
- Registers itself as a `UIBridge` when you call `BridgeHandler.UI.register(window)`.

```java
final DemoWindow window = new DemoWindow();
BridgeHandler.UI.register(window);
JOID.inst().setDevMode(true).setDemoMode(true).load();
window.run();
```

Run it with `./gradlew :lwjgl3:runDemo` (or `:lwjgl2`, `:vulkan`). With LWJGL 2, call `be.zeldown.joid.impl.lwjgl2.Backend.register()` before creating the window.

## Writing your own bridge

When embedding JOID in a custom host (a game, a tool with a different main loop), extend `UIBridge` and implement `IUIBridge`:

```java
public class MyBridge extends UIBridge {

    @Override
    public void drawHover(@NonNull UI ui, @NonNull List<@NonNull String> lines, double mouseX, double mouseY) {
        // Render tooltip strings using your host's font system
    }

    @Override
    public void open(@NonNull UI ui) {
        // Custom open logic — usually close others first, then add(ui)
    }

    @Override
    public void close(@NonNull UI ui) {
        remove(ui);
    }

    @Override
    public void add(@NonNull UI ui) {
        getUiList().add(ui);
        ui.load(/* width */ 1920, /* height */ 1080);
    }

    @Override
    public void remove(@NonNull UI ui) {
        getUiList().remove(ui);
    }

    @Override
    public boolean isOnTop(@NonNull UI ui) {
        return getUiList().ordered().getLast() == ui && ui.getData().active() && ui.getData().visible();
    }

    @Override
    public boolean canHandle(@NonNull Class<? extends UI> ui) { return true; }
    @Override
    public boolean canHandle(@NonNull UI ui) { return true; }

    @Override
    public int getIndex() { return 0; }

    @Override
    public @NonNull IUIBridge getInstance() { return this; }
}
```

Register it once at startup:

```java
BridgeHandler.UI.register(new MyBridge());
```

## Wiring input

Your main loop forwards the events of your windowing library to the bridge. Mouse buttons go through `ClickType.from(button)` and keys through the engine-neutral `Key` enum; the mouse position is read from the window bridge, in pixels from the top-left corner.

```java
bridge.mousePressed(ClickType.from(button));
bridge.mouseReleased(ClickType.from(button));
bridge.mouseDragged(clickType, System.currentTimeMillis() - pressTime);
bridge.mouseScroll(wheelDelta);
bridge.keyTyped(character, key);

bridge.update();
bridge.draw();
```

Every backend `DemoWindow` contains a complete loop for its windowing library — `Mouse` / `Keyboard` polling on LWJGL 2, GLFW callbacks in the `DemoWindow` of the `glfw` module, shared by LWJGL 3 and Vulkan.

`UIBridge` already implements mouse drag tracking and ESC-to-close; you just need to feed it events.

## Interface scale

A host that lets its users choose the size of the interface — a game GUI scale, an accessibility setting — tells JOID through `getInterfaceScale(UI)`. The bridge decides which UIs follow it and returns a normalized factor, `1` meaning full size:

```java
@Override
public double getInterfaceScale(final @NonNull UI ui) {
    return ui instanceof HudUI ? this.settings.getGuiScale() / (double) this.settings.getMaxGuiScale() : 1D;
}
```

The UI does the rest: it draws around its anchor at that scale, converts the mouse, places tooltips and sizes effects accordingly, and updates its `scaledWidth` / `scaledHeight` signals when the value changes. The bridge keeps passing the raw window size and the raw mouse position, in pixels — never rescale them yourself.

## Multiple bridges

A real app often has multiple bridges — e.g., one for in-world UIs, one for main menu. `BridgeHandler.UI` routes each `UI` to the appropriate bridge based on `canHandle(Class<? extends UI>)`. When several bridges can handle the same `UI`, the one with the highest `getIndex()` wins, then the latest registered.

```java
BridgeHandler.UI.register(new MainMenuBridge());
BridgeHandler.UI.register(new HUDBridge());
BridgeHandler.UI.register(new WorldUIBridge());

// JOID.open() automatically picks the right bridge for the UI class.
JOID.open(new SettingsUI());  // → MainMenuBridge (because it canHandle SettingsUI)
```

Use the `@UIBridge` annotation on your UI classes to hint the routing, or override `canHandle` logic in each bridge.

## Bridge ordering

Bridges are iterated in registration order. The first one whose `canHandle(ui)` returns `true` wins. Keep your routing deterministic.

## Best practices

- **One bridge per rendering context.** Don't try to multiplex different rendering contexts in one bridge.
- **Keep `drawHover` fast.** It runs after every node render. Use your host's cached font system.
- **Never manipulate `uiList` directly.** Go through `open`/`close`/`add`/`remove` to keep internal state consistent.
- **Register bridges before opening any UI.** `JOID.open()` fails silently if no bridge can handle the UI class.

## See also

- [UI Class](ui-class.md) — how UIs hook into the bridge.
- [Backends](backends.md) — the window, render and audio bridges.
- [Transitions](transitions.md) — in/out animations driven by the bridge `open` / `close`.
