# JOID

**Java OpenGL Interface Development** — a component-based UI toolkit built on LWJGL 2 with no framework dependencies.

JOID gives you the building blocks to compose real-time, GPU-accelerated user interfaces: layout nodes, shape primitives, reactive state, shader pipelines, MSDF fonts, tween animations, and video playback. It's designed to be embedded inside any LWJGL-based host (game, tool, demo window) through its bridge pattern.

## What it is

- A **node tree**: you attach nodes to a root UI, each node has position/size/callbacks/effects.
- A **rendering pipeline**: shaders compose multi-pass effects (blur, border, gradient, rounded, circle).
- A **state system**: `Signal<T>` observables and `@UIStoreData` persistent stores drive automatic reloads.
- A **resource loader**: images, videos (FFmpeg), URLs, buffered images — all cached with async decode.

## What it isn't

- Not a browser runtime. No DOM, no CSS — just Java.
- Not a Minecraft mod. JOID has zero MC dependency.
- Not an immediate-mode API. Nodes are retained; you build the tree once and mutate it via signals or direct calls.

## Who it's for

- Game developers building menus, HUDs, overlays.
- Tool developers needing rich, customizable UIs inside LWJGL apps.
- Anyone comfortable with Java 8+ who wants OpenGL control without rebuilding the fundamentals.

## What's in 6.0.0

Version 6.0.0 is a major alignment release bringing a full shader pipeline, video playback, lifecycle hooks, and many fixes. See the [Changelog](changelog/6.0.0.md) for the complete list.

## Next steps

- Read [Installation](getting-started/installation.md) to get JOID on your classpath.
- Follow the [Quick Start](getting-started/quick-start.md) to render your first UI in under 30 lines.
- Skim [Core Concepts](getting-started/core-concepts.md) to understand the mental model.
