---
title: "Stores and remembered properties"
description: "Share values between UIs with a store, keep them across launches, and remember a field of a UI."
genre: learn
sidebar:
  order: 3
---

A signal created in `init()` lives as long as one opening of its UI.
A volume setting must outlive that: several UIs read it, and it must still be there at the next launch.
A store holds such values; a remembered property keeps one field of a UI across launches.

**You will learn**
- How long a value lives, from a local variable to the disk
- How to write a store and use it from several UIs
- How a store reaches the disk and comes back
- How to remember a field of a UI with [[UIProperty]]

## Choose where a value lives

Pick the shortest life that fits:

| The value must live | Put it in |
|---|---|
| For one opening of the UI | A local variable or signal of `init()` |
| As long as the UI object | A field of the UI |
| Across launches, for one UI | A field marked [[UIProperty]] |
| As long as the UI is open, shared by its parts | A `LOCAL` store |
| As long as the program, shared by every UI | A `GLOBAL` store |
| Across launches, shared by every UI | A `PERMANENT` store |

`init()` runs again at each opening and at each [reload](/use/get-started/dev-tools/), so a signal created there starts over each time.

## Write a store

A store is a class that extends [[UIStore]] and carries [[UIStoreData]].
Its values are signals, so the nodes that show them follow them:

```java file="com/example/state/SettingsStore.java"
```

`scope` takes a [[UIStoreScope]]:

| Scope | One instance | Ends |
|---|---|---|
| `LOCAL`, the default | Per UI that uses it | When that UI closes, with a call of `destroy()` |
| `GLOBAL` | For the whole program | With the program |
| `PERMANENT` | For the whole program | With the program, after its values reach the disk |

`toJson()` and `fromJson(...)` matter only for a `PERMANENT` store: `toJson()` puts its values in a `Map<String, Object>`, and `fromJson(...)` receives that map back.
JOID turns the map into JSON and back itself ([Keep a store on disk](#keep-a-store-on-disk)).

A class without `@UIStoreData` is refused with an `IllegalStateException` at its first use.

## Use a store from several UIs

[[UI#useStore]] gives the store of a class, and creates it at the first call:

```java file="com/example/state/SettingsUI.java" region="use"
```

```java file="com/example/state/SettingsUI.java" region="bind"
```

The overlay that shows the volume asks for the same store, and gets the same instance:

```java file="com/example/state/VolumeOverlay.java" region="overlay"
```

<figure>

![A dark settings card reading "Volume: 5" and "Sound on", and above it, in the top right corner of the screen, a blue overlay reading "Volume 5"; the pointer clicks + twice and - twice, and the card and the overlay show the same volume at each click](/captures/state-stores.webp)

<figcaption>
Two UIs, one store: the overlay follows the volume that the settings screen changes.
</figcaption>
</figure>

The second argument is the factory: JOID calls it when no instance exists yet, then calls `init()` on the new store.
A `LOCAL` store is the same instance for every call made by one UI, and stays through its reloads; another UI gets its own.
A node reaches the store of its UI with [[Node#useStore]].

## Keep a store on disk

The store of [Write a store](#write-a-store) is `PERMANENT`:

```java file="com/example/state/SettingsStore.java" region="permanent"
```

A `PERMANENT` store is written to `store/<id>.store` in the [data folder](/use/learn/running/files/), here `store/sound.store`, as the JSON of `toJson()`:
- when any UI closes;
- when the program ends normally;
- when you call `save()` on it.

At its first use in a launch, JOID creates it with the factory, calls `init()`, then `fromJson(...)` with the content of the file when the file exists.
`fromJson` receives only the keys that the file holds, so test each key: a store saved before a new value existed starts that value from its default.

The map comes back with the types of JSON, whatever the type you put in it:

| In the file | In the map of `fromJson` |
|---|---|
| A whole number | `Long` |
| A decimal number | `Double` |
| `true` or `false` | `Boolean` |
| A text | `String` |
| An array | `List<Object>` |
| An object | `Map<String, Object>` |

Read a number through `Number`, as in `((Number) json.get("volume")).intValue()`, never with a cast to `Integer`.
`toJson()` writes any other object field by field, and it comes back as a `Map`; a `null` value is not written.

Without an `id`, the file takes the full name of the class: give an `id` to a store that you may rename or move.

When the file holds invalid JSON, or when `fromJson` throws, JOID renames the file to `<id>.store.corrupt`, warns, and starts the store from `init()`: the next save writes a fresh file and the old data stays for you to inspect.

[Build a settings screen](/use/guides/build-a-settings-screen/) binds controls to a store kept on disk.

:::danger
A process that is killed or crashes saves nothing: call `save()` on a store that must survive a crash, at the moment its values change.
:::

## Remember a field of a UI

A field marked [[UIProperty]] keeps its value from one launch to the next:

```java file="com/example/state/SettingsUI.java" region="property"
```

```java file="com/example/state/SettingsUI.java" region="opened"
```

<figure>

![The settings card, which reads "Openings: 2" at its bottom; Escape, shown in a badge, closes it, and it opens again reading "Openings: 3", then closes and opens again reading "Openings: 4"](/captures/state-property.webp)

<figcaption>
The count is written when the UI closes and read back before the next `init()`, so each opening adds one to it.
</figcaption>
</figure>

JOID writes the remembered fields of a UI to `property/<class name>.property` in the data folder, as JSON, and reads them back into the fields before each `init()`.
It writes them when the UI closes, before a reload, and when the program ends, if their text changed.

- The field is not `final` nor `static`; any type that Gson can convert works, from an `int` to a list of records.
- The key is the name of the field; `@UIProperty("key")` gives another one, to rename the field without losing the value.
- A field set to `null` is removed from the file, and comes back with its default value.
- A value that the file holds with another type is refused with a warning, and the field keeps its default.
- The file belongs to the class: two instances of one UI class share their remembered values.

## Recap

- Give a value the **shortest life** that fits: `init()`, a field, `@UIProperty`, then a `LOCAL`, `GLOBAL` or `PERMANENT` store.
- A **store** extends `UIStore`, carries `@UIStoreData`, and holds signals.
- **`useStore(Class, factory)`** gives the one instance of its scope, created by the factory at the first call.
- A **`PERMANENT`** store is saved through `toJson()` when a UI closes and when the program ends, and restored through `fromJson()`.
- A store file of invalid JSON becomes **`.store.corrupt`**, and the store starts from `init()`.
- **`@UIProperty`** remembers a field of a UI across launches, read before each `init()`.

## Try it

**Remember the last tab.**
A settings screen has three tabs, and must open on the tab that was open last time.
Where do you keep the index of the tab?

<details>
<summary>Solution</summary>

In a field of the screen marked `@UIProperty`, such as `@UIProperty private int tab;`.
Only that screen reads it, so a store would share it for nothing.

</details>

**A store that forgets.**
A store keeps the name of the player, but every launch starts with an empty name.
Which two places of the store do you check?

<details>
<summary>Solution</summary>

The scope: only `@UIStoreData(scope = UIStoreScope.PERMANENT)` reaches the disk.
Then `toJson()` and `fromJson(...)`: the default `toJson()` writes an empty object, so the store writes the name there and reads it back.

</details>
