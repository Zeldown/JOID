# Quick Start

This walkthrough gets a JOID window on screen in under 30 lines, then shows how to add real components. It assumes you've completed [Installation](installation.md).

## A minimal window

JOID ships a ready-to-use `DemoWindow` that opens an LWJGL 2 window, registers itself as the UI bridge, and runs the main loop. For your own host you'd implement `UIBridge` yourself (see [Bridge](../ui/bridge.md)), but to get started:

```java
public static void main(final String[] args) {
    JOID.inst().setDevMode(true).setDemoMode(true).load();
    try {
        final DemoWindow window = new DemoWindow();
        BridgeHandler.register(window);
        window.run();
    } catch (final LWJGLException e) {
        e.printStackTrace();
    }
}
```

Running this opens a 1920×1080 black window. Press ESC to close.

## Your first UI

A UI class extends `UI` and implements `init()` — called once when the UI is opened. Use it to attach nodes.

```java
public class MyFirstUI extends UI {

    @Override
    public void init() {
        RectNode.create(760, 440, 400, 200)
            .color(Color.BLUE.toGradient(Color.MAGENTA))
            .effect(RoundedNodeEffect.create(20F))
            .effect(BorderNodeEffect.create(Color.WHITE, 2F))
            .attach(this);
    }
}
```

Open it from your main:

```java
JOID.open(new MyFirstUI());
```

You now have a rounded gradient rectangle with a white border centered on screen.

## Adding interaction

Every node has `onClick`, `onHover`, and dozens of other callbacks. Chain them directly:

```java
RectNode.create(760, 440, 400, 200)
    .color(Color.BLUE, Color.CYAN)                // normal, hovered
    .effect(RoundedNodeEffect.create(20F))
    .onClick((node, mouseX, mouseY, clickType) -> {
        System.out.println("Clicked at " + mouseX + ", " + mouseY);
    })
    .hover(() -> "Click me!")
    .attach(this);
```

`color(normal, hovered)` interpolates automatically based on hover state. `hover(() -> ...)` supplies a tooltip — string, list of strings, or a full node.

## Text and layouts

Wrap children in a layout node to organize them:

```java
FlexNode.horizontal(40, 40, 80).margin(20).body(flex -> {
    RectNode.create(0, 0, 200, 80).color(Color.RED).attach(flex);
    RectNode.create(0, 0, 200, 80).color(Color.GREEN).attach(flex);
    RectNode.create(0, 0, 200, 80).color(Color.BLUE).attach(flex);
}).attach(this);
```

`FlexNode.horizontal(x, y, height)` auto-positions its children in a row, handling margin and overflow.

For text, use `TextNode` with the `Text` builder:

```java
TextNode.create(100, 100)
    .text(Text.create("Hello JOID", TextInfo.create(myFont, 32, Color.WHITE)))
    .attach(this);
```

Fonts are loaded via MSDF atlases — see [Custom Fonts](../fonts/custom-font.md).

## Reactive state

Instead of recomputing manually, bind nodes to `Signal<T>` observables:

```java
final IntegerSignal count = new IntegerSignal(0);

TextNode.create(100, 100)
    .text(() -> Text.create("Count: " + count.getOrDefault(), info))
    .watch(count)
    .attach(this);

RectNode.create(100, 200, 100, 40)
    .color(Color.GRAY)
    .onClick((n, mx, my, ct) -> count.set(count.getOrDefault() + 1))
    .attach(this);
```

Clicking the rectangle increments `count`; the text auto-reloads via `.watch(count)`.

## What to read next

- [Core Concepts](core-concepts.md) — the mental model of nodes, signals, and rendering.
- [UI Class](../ui/ui-class.md) — full lifecycle, annotations, keybinds.
- [Node Fundamentals](../nodes/node-fundamentals.md) — position, size, effects, callbacks.
