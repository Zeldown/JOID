# Stores

`UIStore` est un conteneur d'état persistant. Annotez les champs avec `@UIStoreData`, ils sont sérialisés en JSON dans `config/store/` automatiquement — sauvés à la fermeture d'UI, restaurés au chargement.

## Définir un store

```java
public class AppSettings extends UIStore {

    @UIStoreData
    public BooleanSignal darkMode = new BooleanSignal(true);

    @UIStoreData
    public FloatSignal volume = new FloatSignal(0.75F);

    @UIStoreData
    public StringSignal locale = new StringSignal("en");

    @UIStoreData("history")              // clé explicite
    public ListSignal<String> recent = new ListSignal<>();
}
```

Les champs doivent être des sous-classes de `Signal<T>` (primitive ou composite). Les champs non-signaux sont ignorés.

## Utiliser dans une UI

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

`useStore(Class<T>)` :

- Retourne l'instance existante du store si déjà chargée (partagée entre UIs).
- Construit une nouvelle, charge le JSON depuis le disque, et hydrate les signaux.

## Portée

Les stores sont **globaux**. Les changements dans une UI se propagent à toutes les UIs utilisant le même store — les `watch()` sur les signaux réagissent partout.

## Persistance

À `UI.properlyClose()`, `UIStoreHook.saveAll()` écrit tous les stores chargés sur le disque :

```
config/
└── store/
    ├── AppSettings.store
    └── PlayerPrefs.store
```

Format JSON (Gson). Les clés inconnues sont ignorées au load (forward-compatible).

## Hydratation silencieuse

Pendant le load, les signaux sont définis avec `setSilent` — aucun abonné ne se déclenche. Ça évite les reloads en cascade au démarrage.

## Save / load manuels

```java
UIStoreHook.saveAll();              // tous les stores
UIStoreHook.save(store);            // un store spécifique
UIStoreHook.load(store);            // force le reload depuis le disque
```

## Détruire un store

```java
UIStoreHook.destroyStore(store);    // save + retrait de la hook map
```

Appelé automatiquement à la fermeture d'UI pour les stores utilisés par une seule UI.

## Exemple — préférences joueur

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
prefs.grantXP(45F);    // persisté automatiquement à la fermeture d'UI
```

## Bonnes pratiques

- **Gardez les stores petits et focalisés.** Un store par domaine — `AppSettings`, `PlayerPrefs`, `UIState`. Pas un `GlobalState` avec 200 champs.
- **Ne mettez pas d'objets lourds dans les stores.** Des listes d'IDs ou de configs, pas des entités de jeu complètes.
- **Gérez les migrations de version manuellement.** Si vous renommez un champ, l'ancien JSON a l'ancienne clé — soit renommez via `@UIStoreData("oldName")`, soit gérez la migration dans un override de `load(JsonObject)`.
- **Les valeurs par défaut comptent.** À la première exécution, les signaux ont leur valeur initiale. Choisissez des défauts sensés.

## Voir aussi

- [Signals](signals.md).
- [Watch](watch.md) — les nœuds réagissent aux changements de store.
- [UI Class](../ui/ui-class.md) — API `useStore`.