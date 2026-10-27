---
summary: Remembers the bridges of every registry and later unregisters every bridge registered since.
---

A snapshot backend creates one before it registers, and calls `restore()` in `destroy()`, so that the next suite starts clean.

Learn: [Give the testkit a surface](/build/testing-a-backend/#give-the-testkit-a-surface).

## SnapshotBridgeRestorer()

Remembers the bridges of the seven registries of `BridgeHandler`.

## restore()

Unregisters every bridge that is not among the remembered ones.
