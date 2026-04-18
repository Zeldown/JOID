# Watch

`watch(signal, properties)` lie un nœud à un `Signal<T>` pour que le nœud réagisse automatiquement aux changements.

## Usage de base

```java
final StringSignal name = new StringSignal("world");

TextNode.create(0, 0)
    .text(() -> Text.create("Hello, " + name.getOrDefault(), info))
    .watch(name)
    .attach(parent);
```

Sur `name.set(...)`, le nœud se recharge.

## `WatchProperty`

Le deuxième argument contrôle ce qui se passe au changement :

| Property | Comportement |
|---|---|
| `RELOAD` (défaut) | Exécute `reload()` du nœud — ré-exécute son body/builder |
| `BODY` | Relance uniquement le `body(consumer)` — moins coûteux qu'un reload complet |
| `CLEAR_CHILDREN` | Retire les enfants ; utile quand le body les reconstruit |
| `NONE` | Marqueur seul — aucune action automatique |

```java
node.watch(signal);                                    // RELOAD
node.watch(signal, WatchProperty.BODY);
node.watch(signal, WatchProperty.CLEAR_CHILDREN, WatchProperty.RELOAD);  // plusieurs props
```

Quand plusieurs properties sont passées, elles s'exécutent dans l'ordre.

## Watches conditionnels

Conditionner le watch par un prédicat :

```java
node.watch(signal, () -> ZUI.isOpen(this.ui), WatchProperty.RELOAD);
```

Le signal ne se déclenche que si la condition retourne `true`. Utile pour les UIs qui ne doivent pas reload quand fermées ou en arrière-plan.

## Plusieurs signaux

Lier un nœud à plusieurs signaux :

```java
node.watch(signalA);
node.watch(signalB, WatchProperty.BODY);
node.watch(signalC, () -> isReady(), WatchProperty.RELOAD);
```

Chaque appel est indépendant.

## Callback `onWatch`

Attacher un callback qui se déclenche dès qu'un watch tire :

```java
node.onWatch((n, signal, properties) -> {
    System.out.println("Signal fired: " + signal);
});
```

## Exemple — reconstruction de liste au changement de filtre

```java
final StringSignal filter = new StringSignal("");
final ListSignal<Item> items = new ListSignal<>();

FlexNode.vertical(0, 0, 400).margin(8)
    .watch(filter, WatchProperty.BODY)
    .watch(items, WatchProperty.BODY)
    .body(list -> {
        final String f = filter.getOrDefault().toLowerCase();
        for (Item item : items) {
            if (!item.name.toLowerCase().contains(f)) continue;
            renderRow(item, list);
        }
    })
    .attach(parent);

TextFieldNode.create(0, 0, 400, 30)
    .onChange((tf, value) -> filter.set(value))
    .attach(parent);
```

Les deux signaux déclenchent un re-run de body, donc la liste se met à jour à la saisie du filtre et aux changements d'items.

## Cycle de vie

Les watches sont automatiquement désenregistrés quand le nœud est détaché (via `clearChildren` ou fermeture d'UI). Pas besoin de désabonner manuellement.

## Bonnes pratiques

- **Préférez `BODY` à `RELOAD` pour les scénarios de reconstruction de liste** — inutile de ré-exécuter tout `init()`.
- **Utilisez les watches conditionnels dans les setups multi-UI** — évite les reloads inutiles quand l'UI n'est pas au premier plan.
- **Watchez le plus petit nœud possible.** Watcher un parent recharge tout ; watcher un nœud feuille ne recharge que lui.

## Voir aussi

- [Signals](signals.md).
- [Stores](stores.md) — signaux persistants avec auto-watch.