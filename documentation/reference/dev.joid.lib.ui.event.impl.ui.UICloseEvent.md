---
summary: The closing of a UI, in a cancellable `Pre` before it starts and a `Post` once the UI is closed.
---

Every closing goes through it: `JOID.close(...)`, Escape on a closeable UI, or a UI that a new one replaces.
[[UI#beforeClose]] receives a [[UICloseEvent.Pre]] and can keep the UI open; [[UI#onClose]] receives a [[UICloseEvent.Post]] after the closing transition, once the UI saved its stores and its remembered fields.

Learn: [UIs](/use/learn/describing-the-ui/uis/), [Events](/use/learn/interactivity/events/#refuse-a-change-before-it-happens).
