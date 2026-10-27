---
summary: Base class of a store, a container of values that lives as long as its scope and that several UIs can share.
---
Annotate the subclass with [[UIStoreData]] and get it with [[UI#useStore]] or [[Node#useStore]].
A `PERMANENT` store is read from the data folder when it is created and written back when a UI closes and when the JVM stops.

Learn: [Stores and properties](/use/learn/state/stores-and-properties/).

## UIStore()

Creates a store, for a subclass.

## init()

Fills the store with its starting values.
JOID calls it once, right after the factory creates the store and before `fromJson`.

## destroy()

Releases what the store holds when its scope ends: JOID calls it for a `LOCAL` store when the UI that holds it closes.
A `destroy()` that throws prints a warning, and the store is forgotten anyway.

## toJson()

Returns the values to save for a `PERMANENT` store.
A `toJson()` that throws prints a warning, and the other stores are saved anyway.

## fromJson(JsonObject)

Restores the values that `toJson()` saved, right after `init()`.
A `fromJson()` that throws prints a warning, keeps a copy of the file aside, and the store starts again from `init()`.

## save()

Writes a `PERMANENT` store to its file now; a store of another scope ignores it.

## getData()

The `@UIStoreData` annotation of the store.
Throws an `IllegalStateException` when the class has none.
