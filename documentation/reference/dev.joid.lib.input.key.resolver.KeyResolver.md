---
summary: The registry of the key resolvers that turn the bindings of your application into keys for shortcuts.
---

A [[Shortcut]] created from an object that is not a [[Key]] asks this registry which key it stands for.
In dev mode, a shortcut whose binding no resolver knows prints a warning when it is created, and fires once a resolver that knows it is registered.

Learn: [Keyboard](/use/learn/interactivity/keyboard/#use-the-key-bindings-of-your-application).

## register(IKeyResolver)

Adds a resolver, asked before every resolver registered earlier.
Registering the same resolver again moves it to the front.

## unregister(IKeyResolver)

Removes a resolver; the shortcuts whose binding only it knew stop firing.

## supports(Object)

If `true`, the binding is a [[Key]] or when a registered resolver supports it.

## resolve(Object)

Returns the key of a binding: a [[Key]] as it is, any other object through the first resolver that supports it.
Throws an `IllegalArgumentException` when no resolver supports the binding.
