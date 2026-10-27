---
summary: Turns a key binding of your application into the key that it stands for, so that a shortcut can take that binding.
---

Implement it when your application stores its key bindings in objects of its own, such as a key that the user chose in the settings, and register it with [[KeyResolver#register]].
JOID asks the resolver again at every key, so a binding that changes applies at once.

Learn: [Keyboard](/use/learn/interactivity/keyboard/#use-the-key-bindings-of-your-application).

## supports(Object)

Returns `true` when this resolver knows the binding.
JOID calls `resolve` only for a binding that this method accepts.

## resolve(Object)

Returns the [[Key]] that the binding stands for now, compared with [[NodeKeyEvent#getKey]], the key that the layout names.
A `null` key matches no key.
