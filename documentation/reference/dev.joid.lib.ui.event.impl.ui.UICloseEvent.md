---
summary: The closing of a UI, in a cancellable `Pre` before it starts and a `Post` once the UI is closed.
---

The `Pre` fires for `JOID.close(...)`, Escape on a closeable UI and a screen that a new screen replaces; the `Post` fires on every closing, `forceClose`, `closeAll()` and `renew()` included.
[[UI#beforeClose]] receives a [[UICloseEvent.Pre]] and can keep the UI open; [[UI#onClose]] receives a [[UICloseEvent.Post]] after the closing transition, once the UI saved its stores and its remembered fields.

Learn: [UIs](/use/learn/describing-the-ui/uis/), [Events](/use/learn/interactivity/events/#refuse-a-change-before-it-happens).
