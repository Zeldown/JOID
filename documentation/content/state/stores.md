# Stores

`UIStore` is a persistent state container. Annotate fields with `@UIStoreData`, and they're serialized to JSON in `config/store/` automatically — save on UI close, restore on load.

## Define a store

```java
public class AppSettings extends UIStore {

    @UIStoreData
    public BooleanSignal darkMode = new BooleanSignal(true);

    @UIStoreData
    public FloatSignal volume = new FloatSignal(0.75F);

    @UIStoreData
    public StringSignal locale = new StringSignal("en");

    @UIStoreData("history")              // explicit key
    public ListSignal<String> recent = new ListSignal<>();
}
```

Fields must be `Signal<T>` subclasses (primitive or composite). Non-signal fields are ignored.

## Use in a UI

```java
public class MyUI extends UI {

    private AppSettings settings;

    @Override
    public void init() {
        this.settings = useStore(AppSettings.class);

        SwitchNode.create(0, 0, 56, 28)
            .value(settings.darkMode.getOrDefault())
            .onChange((s, val) -> settings.darkMode.set(val))
            .attach(this);
    }
}
```

`useStore(Class<T>)`:

- Returns the existing store instance if already loaded (shared across UIs).
- Constructs a new one, loads JSON from disk, and hydrates signals.

## Scope

Stores are **global**. Changes in one UI propagate to all UIs using the same store — `watch()` on the signals reacts everywhere.

## Persistence

On `UI.properlyClose()`, `UIStoreHook.saveAll()` writes all loaded stores to disk:

```
config/
└── store/
    ├── AppSettings.store
    └── PlayerPrefs.store
```

Format is JSON (Gson). Unknown keys are ignored on load (forward-compatible).

## Silent hydration

During load, signals are set with `setSilent` — no subscribers fire. This prevents cascading reloads on startup.

## Manual save / load

```java
UIStoreHook.saveAll();              // all stores
UIStoreHook.save(store);            // specific store
UIStoreHook.load(store);            // force reload from disk
```

## Destroying stores

```java
UIStoreHook.destroyStore(store);    // save + remove from hook map
```

Called automatically on UI close for stores only used by one UI.

## Example — preferences

```java
public class PlayerPrefs extends UIStore {

    @UIStoreData
    public IntegerSignal level = new IntegerSignal(1);

    @UIStoreData
    public FloatSignal xp = new FloatSignal(0F);

    @UIStoreData("unlocked_items")
    public SetSignal<String> unlockedItems = new SetSignal<>();

    public void grantXP(float amount) {
        float current = xp.getOrDefault() + amount;
        while (current >= 100F) {
            current -= 100F;
            level.set(level.getOrDefault() + 1);
        }
        xp.set(current);
    }
}
```

```java
PlayerPrefs prefs = useStore(PlayerPrefs.class);
prefs.grantXP(45F);    // automatically persists on UI close
```

## Best practices

- **Keep stores small and focused.** One store per domain — `AppSettings`, `PlayerPrefs`, `UIState`. Not one `GlobalState` with 200 fields.
- **Don't put heavy objects in stores.** Lists of IDs or configs, not full game entities.
- **Version migrations manually.** If you rename a field, old JSON has the old key — either rename via `@UIStoreData("oldName")` or handle migration in `load(JsonObject)` override.
- **Default values matter.** The first time a user runs your app, signals have their initial values. Pick sensible defaults.

## See also

- [Signals](signals.md).
- [Watch](watch.md) — nodes react to store changes.
- [UI Class](../ui/ui-class.md) — `useStore` API.
