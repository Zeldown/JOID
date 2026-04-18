# UI Class

La classe `UI` est la racine de chaque écran. Étendez-la, implémentez `init()`, et vous avez une UI.

## Déclaration

```java
public class MyUI extends UI {

    @Override
    public void init() {
        // Attachez vos nœuds ici
    }
}
```

## Configuration via `@UIData`

Contrôlez le comportement global avec l'annotation `@UIData`. Tous les attributs ont des valeurs par défaut sensées :

```java
@UIData(
    active = true,
    visible = true,
    pause = true,
    closeable = true,
    zoomable = true,
    projection = true,
    background = true,
    backgroundColor = "#101010c0",
    zlevel = 0,
    anchorX = Align.CENTER,
    anchorY = Align.CENTER
)
public class MyUI extends UI { ... }
```

| Attribut | Défaut | Description |
|---|---|---|
| `active` | `true` | Si `false`, l'UI est entièrement ignorée (pas d'update, pas de draw). |
| `visible` | `true` | Si `false`, l'UI est mise à jour mais pas dessinée. |
| `pause` | `true` | Si l'hôte l'honore, met l'app/le jeu en pause quand l'UI est ouverte. |
| `closeable` | `true` | Si `false`, ESC ne ferme pas cette UI. Le code peut toujours appeler `JOID.close(this)`. |
| `zoomable` | `true` | Active le zoom CTRL+`+`/`-`. |
| `projection` | `true` | Met en place la projection orthographique automatiquement. Désactivez pour du 3D custom. |
| `background` | `true` | Rend un rectangle de fond rempli derrière l'arbre de nœuds. |
| `backgroundColor` | `"#101010c0"` | Couleur hex du fond. Supporte `rgba(...)` et `gradient(...)`. |
| `zlevel` | `0` | Ordre de dessin entre plusieurs UIs concurrentes. Plus haut = au-dessus. |
| `anchorX` / `anchorY` | `CENTER` | Ancre logique de l'espace de design 1920×1080. |

Muter à l'exécution est également possible :

```java
this.getData().setCloseable(false).setBackground(false);
```

## Cycle de vie

```
constructeur → JOID.open(ui) → load() → init() → (boucle de frames) → onClose() → properlyClose()
```

### `init()`

Appelée une fois à l'ouverture de l'UI (ou au rechargement via hot-reload). Attachez-y tous les nœuds.

> TIP: **N'appelez jamais** `init()` vous-même. JOID l'appelle au bon moment, avec `UI.current` défini pour que les nœuds créés pendant `init` puissent résoudre `getUi()` avant d'être attachés.

### `update()`

Appelée à chaque tick du jeu (chaque frame en mode autonome). Override pour de la logique qui n'a pas besoin de l'état de rendu :

```java
@Override
public void update() {
    super.update();  // exécute les updates des nœuds
    // votre logique
}
```

### `preDraw(mouseX, mouseY)` / `postDraw(mouseX, mouseY)`

Appelées avant / après le dessin de l'arbre de nœuds. Utilisez-les pour un overlay custom :

```java
@Override
public void postDraw(final double mouseX, final double mouseY) {
    DrawUtils.SHAPE.drawCircle(mouseX, mouseY, Color.WHITE, 10);
}
```

### `onClose()`

Retournez `false` pour bloquer la fermeture via ESC (par exemple avertissement de modifs non sauvegardées). Retournez `true` pour laisser JOID fermer l'UI.

### `properlyClose()`

Interne. Appelle `onDetach()` sur tous les nœuds, sauvegarde stores et properties, arrête les file monitors. Ne pas override.

## Raccourcis clavier

Enregistrez des shortcuts globaux avec `keybind(Runnable, Integer... keys)` :

```java
@Override
public void init() {
    this.keybind(() -> JOID.open(new SettingsUI()), Keyboard.KEY_ESCAPE);
    this.keybind(() -> this.reload(), Keyboard.KEY_R, Keyboard.KEY_LCONTROL);
}
```

Plusieurs touches = combinaison (toutes appuyées simultanément).

## Rechargement

`this.reload()` relance `init()` après avoir nettoyé les nœuds. Utile pour les signaux avec `WatchProperty.RELOAD` (défaut).

```java
mySignal.subscribe(val -> this.reload());  // ou utilisez .watch() sur un nœud spécifique
```

En interne, reload vide les enfants, les callbacks stockés sont préservés, et `init()` tourne à nouveau avec un état frais.

## Tâches planifiées

Planifiez des callbacks à exécuter après un délai ou périodiquement :

```java
this.schedule(() -> System.out.println("Pong"), 1000L, 0L);        // une fois après 1s
this.schedule(() -> this.tick(), 0L, 100L);                        // toutes les 100ms
```

Les tâches tournent sur le thread de rendu ; utilisez-les pour de la logique UI pilotée par le temps (timers, polling). Elles sont thread-safe (backing `CopyOnWriteArrayList`).

## Stores & properties

L'état persistant est sauvegardé automatiquement via `@UIStoreData` sur les champs d'un `UIStore`, ou via `@UIProperty` sur les champs de l'UI elle-même. Voir [Stores](../state/stores.md).

```java
@UIProperty
private double zoomLevelConfig = 1D;

// Automatiquement persisté à la fermeture, restauré au chargement.
```

## Bonnes pratiques

- **Une UI, une responsabilité.** N'amalgamez pas menu de paramètres, minimap et chat dans une seule `UI`. Utilisez plusieurs UIs ouvertes/fermées individuellement.
- **Gardez `init()` léger.** Si un signal déclenche `reload()`, `init()` retourne. Évitez les I/O lourds ici — chargez les ressources une fois à la construction ou via le cache `ResourceBuilder`.
- **Préférez `watch(signal)` aux callbacks manuels.** Les watchers se nettoient automatiquement à `properlyClose()`.
- **N'override pas `draw()` ou `render()`.** Utilisez `preDraw` / `postDraw` ou attachez des nœuds d'overlay.

## Voir aussi

- [Bridge](bridge.md) — comment les UIs se connectent à l'hôte.
- [Transitions](transitions.md) — animations d'entrée/sortie.
- [Stores](../state/stores.md) — système d'état persistant.
- [Node Fundamentals](../nodes/node-fundamentals.md) — construction de l'arbre.