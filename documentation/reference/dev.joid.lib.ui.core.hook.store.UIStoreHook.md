---
summary: Creates, caches, saves and destroys the stores that the UIs use.
---
[[UI#useStore]] goes through it; you call it yourself only to reach a shared store outside a UI.

Learn: [Stores and remembered properties](/use/learn/state/stores-and-properties/).

## useStore(Class,Supplier)

Returns the shared store of this class when it exists; otherwise creates one with the factory, runs `init()` and, for a `PERMANENT` store, restores its file.
A `GLOBAL` or `PERMANENT` store is kept for the next calls; a `LOCAL` store is not.

## destroyStore(UIStore)

Forgets a shared store and runs its `destroy()`.

## saveStore(UIStore)

Writes a `PERMANENT` store to its file; a store of another scope is ignored.

## saveAll()

Writes every shared `PERMANENT` store to its file.

## getData(Class)

The `@UIStoreData` annotation of a store class.
Throws an `IllegalStateException` when the class has none.
