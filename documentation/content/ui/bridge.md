# Bridge

The bridge is the glue between JOID and your host application. It owns the list of active UIs, dispatches input events, and drives rendering.

## The built-in `DemoWindow`

For standalone apps and quick tests, JOID ships `DemoWindow` — a ready-to-use LWJGL 2 window that:

- Creates a 1920×1080 resizable GL context.
- Listens to mouse and keyboard.
- Loops `update` → `render` → `Display.update()`.
- Registers itself as a `UIBridge` automatically when you call `BridgeHandler.register(window)`.

```java
JOID.inst().setDevMode(true).setDemoMode(true).load();
final DemoWindow window = new DemoWindow();
BridgeHandler.register(window);
window.run();
```

Use it as-is for tools, demos, or prototypes.

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
BridgeHandler.register(new MyBridge());
```

## Wiring input

Your main loop must forward input to the bridge:

```java
while (running) {
    while (Mouse.next()) {
        final int button = Mouse.getEventButton();
        final boolean state = Mouse.getEventButtonState();
        if (state && button != -1) {
            bridge.mousePressed(ClickType.from(button));
        } else if (button != -1) {
            bridge.mouseReleased(ClickType.from(button));
        }
        final int scroll = Mouse.getEventDWheel();
        if (scroll != 0) bridge.mouseScroll(scroll);
    }

    while (Keyboard.next()) {
        if (Keyboard.getEventKeyState()) {
            bridge.keyTyped(Keyboard.getEventCharacter(), Keyboard.getEventKey());
        }
    }

    bridge.update();
    bridge.draw();
    Display.update();
}
```

`UIBridge` already implements mouse drag tracking and ESC-to-close; you just need to feed it events.

## Multiple bridges

A real app often has multiple bridges — e.g., one for in-world UIs, one for main menu. `BridgeHandler` routes each `UI` to the appropriate bridge based on `canHandle(Class<? extends UI>)`.

```java
BridgeHandler.register(new MainMenuBridge());
BridgeHandler.register(new HUDBridge());
BridgeHandler.register(new WorldUIBridge());

// JOID.open() automatically picks the right bridge for the UI class.
JOID.open(new SettingsUI());  // → MainMenuBridge (because it canHandle SettingsUI)
```

Use the `@UIBridge` annotation on your UI classes to hint the routing, or override `canHandle` logic in each bridge.

## Bridge ordering

Bridges are iterated in registration order. The first one whose `canHandle(ui)` returns `true` wins. Keep your routing deterministic.

## Best practices

- **One bridge per rendering context.** Don't try to multiplex different GL contexts in one bridge.
- **Keep `drawHover` fast.** It runs after every node render. Use your host's cached font system.
- **Never manipulate `uiList` directly.** Go through `open`/`close`/`add`/`remove` to keep internal state consistent.
- **Register bridges before opening any UI.** `JOID.open()` fails silently if no bridge can handle the UI class.

## See also

- [UI Class](ui-class.md) — how UIs hook into the bridge.
- [Transitions](transitions.md) — in/out animations driven by the bridge `open` / `close`.
