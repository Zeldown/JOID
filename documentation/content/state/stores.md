# Stores

A store is a state object you get with `useStore(...)`. Depending on its context, there is one instance per UI, one instance shared by every UI, or one shared instance saved to disk between runs. Use a store for state that outlives a node tree: a cart, the settings of a menu, a session.

## Defining and using a store

```java
@UIStoreData(id = "cart", context = StoreContext.GLOBAL)
public class CartStore extends UIStore {

    private final ListSignal<String> items = new ListSignal<>(new ArrayList<>());

    public ListSignal<String> getItems() {
        return this.items;
    }

}
```

```java
public class ShopUI extends UI {

    @Override
    public void init() {
        final CartStore cart = this.useStore(CartStore.class);

        TextNode
        .create(100, 100)
        .text(Text.create("", info))
        .<TextNode>onInit(node -> node.getText().text(cart.getItems().size() + " items"))
        .watch(cart.getItems())
        .attach(this);

        RectNode
        .create(100, 160, 200, 60)
        .color(Color.WHITE)
        .onClick((node, mouseX, mouseY, clickType) -> cart.getItems().add("Sword"))
        .attach(this);
    }

}
```

Every UI that calls `useStore(CartStore.class)` gets the same instance, so the cart survives when the shop closes and opens again. Holding [signals](signals.md) in a store lets nodes [watch](watch.md) them. `UIStore` is in `dev.joid.lib.ui.core.hook.store`, `UIStoreData` in `dev.joid.lib.ui.core.hook.store.data` and `StoreContext` in `dev.joid.lib.ui.core.hook.store.context`; `info` is a `TextInfo` (see [Text Model](../text/text-and-textinfo.md)).

## Store contexts with StoreContext

| Context | Instances | Lifetime | Saved to disk |
|---|---|---|---|
| `LOCAL` (default) | One per UI instance. | Destroyed when that UI closes. | No |
| `GLOBAL` | One shared by every UI. | Until `UIStoreHook.destroyStore(store)`. | No |
| `PERMANENT` | One shared by every UI. | Until `UIStoreHook.destroyStore(store)`; restored from its file on the next run. | Yes |

`isLocal()` is `true` for `LOCAL`; `isGlobal()` is `true` for `GLOBAL` and `PERMANENT`.

## @UIStoreData

Every store class needs the `@UIStoreData` annotation; a store without it throws `IllegalStateException: StoreData annotation is missing on <class>` when it is used.

| Attribute | Default | Description |
|---|---|---|
| `id` | `""` | Name of the file of a `PERMANENT` store. Give each permanent store its own id: two stores with the same id share one file. |
| `context` | `StoreContext.LOCAL` | Context of the store. |

`getData()` returns the annotation of a store.

## Getting a store with useStore

| Method | Description |
|---|---|
| `UI.useStore(Class<T> clazz, Object... args)` | Returns the store of this UI or the shared one, creating it on first use with `args` as constructor arguments. |
| `Node.useStore(Class<T> clazz)` | `getUi().useStore(clazz)`: the store of the node's UI, without constructor arguments. Works in a UI's `init()` before the node is attached. |
| `UIStoreHook.useStore(Class<T> clazz, Object... args)` | Same lookup outside any UI: returns the shared `GLOBAL` or `PERMANENT` instance, but creates a new `LOCAL` store on every call. |

`UI.useStore` looks up the store in this order:

1. A `LOCAL` instance this UI already created.
2. The shared instance of a `GLOBAL` or `PERMANENT` store already created by any UI.
3. A new instance: JOID calls the public constructor of the store whose parameter count equals the number of `args`, then restores it from its file (`PERMANENT` store with a file) or calls `init()`. A `LOCAL` store is kept by the UI, a `GLOBAL` or `PERMANENT` one is shared.

The arguments are only used when the store is created; they are ignored afterwards. When no constructor matches, or the constructor throws, `useStore` throws a `RuntimeException` (`Failed to create store instance for class <class>`) whose cause is the original error. Constructors are matched by their number of parameters only.

```java
@UIStoreData(id = "session")
public class SessionStore extends UIStore {

    private final String player;

    public SessionStore(final String player) {
        this.player = player;
    }

    public String getPlayer() {
        return this.player;
    }

}
```

```java
@Override
public void init() {
    this.useStore(SessionStore.class, "Alex");

    TextNode
    .create(100, 100)
    .text(Text.create("", info))
    .<TextNode>onInit(node -> node.getText().text("Player: " + node.useStore(SessionStore.class).getPlayer()))
    .attach(this);
}
```

The node gets the same local instance as the UI, because the UI created it first with its argument.

## Lifecycle of a store

| Method | Called |
|---|---|
| `init()` | When the store is created and not restored from a file. |
| `load(JsonObject json)` | When a `PERMANENT` store is created and its file was read; `init()` is not called then. |
| `save(JsonObject json)` | Before a `PERMANENT` store is written: fill `json` with the state to keep. |
| `save()` | Call it to write a `PERMANENT` store immediately; does nothing for the other contexts. |
| `destroy()` | When the store is destroyed: a `LOCAL` store when its UI closes, any store through `UIStoreHook.destroyStore`. |

When a UI closes, JOID saves every `PERMANENT` store in use (not only the ones of that UI), then destroys the `LOCAL` stores of that UI.

> NOTE: A UI keeps its `LOCAL` stores for its whole lifetime. Opening the same UI instance again returns the stores destroyed at its previous close; open a new UI instance to start with fresh local stores.

## Persistence of PERMANENT stores

```java
@UIStoreData(id = "settings", context = StoreContext.PERMANENT)
public class SettingsStore extends UIStore {

    private final FloatSignal volume = new FloatSignal(1F);

    @Override
    public void load(final JsonObject json) {
        if (json.has("volume")) {
            this.volume.set(json.get("volume").getAsFloat());
        }
    }

    @Override
    public void save(final JsonObject json) {
        json.addProperty("volume", this.volume.getOrDefault());
    }

    public FloatSignal getVolume() {
        return this.volume;
    }

}
```

- The file is `<config dir>/store/<id>.store`, where the config dir is `JOID.inst().getConfigDir()` (`config` in the working directory unless you call `setConfigDir(File)`). The folder is created on the first save. The file is JSON, read and written in UTF-8 on every platform.
- The file holds the `JsonObject` (`com.google.gson.JsonObject`) filled by `save(JsonObject)`, written by Gson.
- The file is read once, when the store is created. Field initializers have run by then, so `load` only overrides what the file contains.
- An empty file, or a file that cannot be read or parsed, is deleted and the store starts from `init()`; read and parse errors also print `Failed to load store file: <id>`.
- The store is written when any UI closes, when you call `save()`, `UIStoreHook.saveStore(store)` or `UIStoreHook.saveAll()`. A write error prints `Failed to save store file: <id>`.

> WARNING: Nothing is saved when the application exits with UIs still open. Call `UIStoreHook.saveAll()` in your shutdown path, or `save()` after each change that must not be lost.

## UIStoreHook

| Method | Description |
|---|---|
| `useStore(Class<T> clazz, Object... args)` | Gets or creates a store (see above). |
| `saveStore(UIStore store)` | Writes a `PERMANENT` store to its file; does nothing for the other contexts. |
| `saveAll()` | Writes every shared `PERMANENT` store. |
| `destroyStore(UIStore store)` | Removes a `GLOBAL` or `PERMANENT` store from the shared instances (the next `useStore` creates a new one), deletes the file of a `PERMANENT` store, then calls `destroy()`. It does not save the store first. |

`destroyStore` is the way to reset a shared store, for example on logout.

## See also

- [Signals](signals.md)
- [Watching Signals](watch.md)
- [Persistent UI Properties](properties.md)
- [The UI Class](../ui/ui-class.md)