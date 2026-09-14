# Introduction

A component-based UI toolkit that runs on any rendering engine through bridges — LWJGL 2, LWJGL 3 (OpenGL 3.3) and Vulkan backends are provided. Pure Java, no CSS, no XML, no runtime parser — the code you write is the layout the GPU draws.

## What it is

JOID gives you a retained-mode node tree, a reactive state system built around `Signal<T>`, a shader pipeline for composable GPU effects, and a set of bridges that adapt the library to any host and any rendering engine. You build the tree once, mutate it through signals, and JOID handles composition and rendering. There is no DSL to learn, no scene-graph format to serialize, no runtime engine to boot — the library is a set of chainable classes that you wire together in Java.

## Why not a CSS-driven UI

A CSS-driven UI walks a familiar pipeline on every change: tokenize the stylesheet, parse it, build an AST, resolve selectors, diff the tree, then translate the result into draw calls. That pipeline exists because the UI's source of truth is a string. JOID doesn't have that source of truth — the source of truth is your code, and it compiles straight into method calls that map to the draw layer.

The practical consequence is that the compiler becomes the UI linter. Rename `RectNode` and every reference follows. Wire the wrong signal type into a watcher and you get a compile error instead of a silent failure at runtime. Refactor a screen and the IDE actually helps — there is no string selector, no `#foo` lookup, no cascade to argue with.

## Targeted reactivity

JOID's reactive system does not re-render subtrees. A `Signal` only notifies the nodes that explicitly watch it; nothing else in the tree is walked or touched.

```java
final Signal<Integer> score = new Signal<>(0);

TextNode.create("0")
    .watch(score, (node, value) -> node.text(String.valueOf(value)))
    .attach(hud);

score.set(42);
```

The `watch` callback fires, the target node updates exactly the property it was told to update, and the rest of the UI is untouched. No diff phase, no reconciliation, no cached AST. `Node.watch(signal, condition, properties)` goes further: fire only when a predicate holds, and narrow the update to specific properties — color only, body only, layout only.

## Host-agnostic by design

JOID talks to its host through bridges — UI hosting, window and input, rendering, audio. Every host is a small adapter class; nothing else about the library changes between environments.

The same node tree, the same effects, the same signals, and the same stores run inside a standalone window, an editor panel embedded in a larger engine, a debug overlay bolted onto a game, or any custom runtime. Swap the bridges, keep the UI — see [Backends](../ui/backends.md). That's the core reason JOID exists as a separate, self-contained library.

## What JOID is not

JOID is not a browser runtime: there is no DOM, no CSS, no HTML subset. It is not immediate-mode: nodes are retained, and you mutate the tree through signals and direct setters. It is not a replacement for every UI toolkit — for native OS menus or accessibility tree integration, pick Swing or JavaFX. JOID targets rendered, GPU-composed UIs that live inside an existing OpenGL or Vulkan application.

## Who it's for

Developers who embed UI into a Java application rendered with OpenGL or Vulkan — games, tools, editors, overlays — and want to own the rendering path without pulling in a web runtime. Anyone comfortable with Java 8+ who prefers compiler guarantees over stylesheet debugging.

## Next steps

- `Installation` — Gradle, Maven, native libraries.
- `Quick Start` — a UI in under thirty lines.
- `Core Concepts` — the mental model behind nodes, effects, and signals.