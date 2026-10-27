---
summary: Annotation that sets how the interface scale of the UI bridge applies to a UI.
---
Without it, a UI follows the interface scale of its bridge.

Learn: [The design canvas](/use/learn/describing-the-ui/design-canvas/#follow-the-interface-scale).

## active()

If `false`, the UI ignores the interface scale and stays at a scale of 1.

## limited()

If `true`, the interface scale applied to the UI never goes above `limit`.

## limit()

Largest interface scale applied to the UI when `limited` is `true`.
