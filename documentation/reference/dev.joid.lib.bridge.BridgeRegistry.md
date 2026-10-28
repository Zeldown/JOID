---
summary: An ordered set of bridges of one kind, kept sorted by priority, whose highest bridge answers `get()`.
---

Every registry of [[BridgeHandler]] is a `BridgeRegistry`.
Registering and unregistering are synchronized and publish a new read-only list, so reading never locks.

Learn: [Add a UI bridge of your own](/use/learn/running/embedding/#add-a-ui-bridge-of-your-own), [The bridges](/build/bridges/#priorities-order-a-registry).

## BridgeRegistry(String)

Creates an empty registry; the name, such as `"RENDER"`, appears in the exception of `get()` on an empty registry.

## register(T)

Registers a bridge with the priority `0`.

## register(T,int)

Registers a bridge with a priority: the highest priority comes first.
Among bridges of the same priority, the last one registered comes first; a bridge that is already registered moves to its new priority.

## unregister(T)

Removes a bridge; does nothing when the bridge is not registered.

## registerDefault(T)

Registers a bridge with the lowest priority, `Integer.MIN_VALUE`, only when the registry is empty, so that any bridge registered later comes before it.
Backends use it for their default UI bridge.

## get()

Returns the bridge of highest priority.
Throws an `IllegalStateException` that names the registry when it is empty, which usually means that no backend is registered.

## getBridge(Class)

Returns the bridge of highest priority that is an instance of the class, or `null` when there is none.

## find(Predicate)

Returns the bridge of highest priority that the filter accepts, or `null` when the filter accepts none.

## getBridgeList()

Returns the registered bridges, read-only, from the lowest priority to the highest.
The list is a snapshot: a later registration does not change it.
