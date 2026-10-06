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
constructeur → JOID.open(ui) → load() → init() → (boucle de frames) → close() → properlyClose()
```

### `init()`

Appelée une fois à l'ouverture de l'UI (ou au rechargement via hot-reload). Attachez-y tous les nœuds.

> TIP: **N'appelez jamais** `init()` vous-même. JOID l'appelle au bon moment, avec `UI.current` défini pour que les nœuds créés pendant `init` puissent résoudre `getUi()` avant d'être attachés.

### `update()`

Appelée à chaque tick du jeu (chaque frame en mode autonome). Override pour de la logique qui n'a pas besoin de l'état de rendu :

```java
@Override
public void update() {
    // votre logique, exécutée une fois les nœuds mis à jour
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

### `close()`

Retournez `false` pour bloquer la fermeture demandée par ESC ou `JOID.close(ui)` (par exemple avertissement de modifs non sauvegardées). Retournez `true`, la valeur par défaut, pour laisser JOID fermer l'UI. La méthode finale `onClose()` la consulte, puis lance la transition de sortie.

### `properlyClose()`

Interne. Appelle `onDetach()` sur tous les nœuds, sauvegarde stores et properties, arrête les file monitors. Ne pas override.

## Raccourcis clavier

Enregistrez des shortcuts globaux avec `keybind(Runnable, Key... keys)` :

```java
@Override
public void init() {
    this.keybind(() -> JOID.open(new SettingsUI()), Key.TAB);
    this.keybind(() -> this.reload(), Key.R, Key.LEFT_CONTROL);
}
```

Plusieurs touches = combinaison (toutes appuyées simultanément). Sur une UI fermable — le défaut de `@UIData` — ESC ferme l'UI avant que les keybinds ne la voient : n'associez `Key.ESCAPE` qu'à une UI déclarée `@UIData(closeable = false)`.

## Rechargement

`this.reload()` relance `init()` après avoir nettoyé les nœuds. Utile pour les signaux avec `WatchProperty.RELOAD` (défaut).

```java
mySignal.subscribe(val -> this.reload());  // ou utilisez .watch() sur un nœud spécifique
```

En interne, reload détache les nœuds précédents (`onDetach`) et les vide, les callbacks stockés sont préservés, et `init()` tourne à nouveau avec un état frais.

## Tâches planifiées

Planifiez des callbacks à exécuter après un délai ou périodiquement :

```java
this.schedule(() -> System.out.println("Pong"), 1000L);            // une fois après 1s
this.schedule(() -> this.tick(), 0L, 100L);                        // toutes les 100ms
```

Sans période la tâche tourne une fois, et une période de `0L` la lance à chaque frame. Les tâches tournent sur le thread de rendu ; utilisez-les pour de la logique UI pilotée par le temps (timers, polling). Elles sont thread-safe (backing `CopyOnWriteArrayList`).

## Stores & properties

L'état persistant est sauvegardé automatiquement via `@UIStoreData` sur les champs d'un `UIStore`, ou via `@UIProperty` sur les champs de l'UI elle-même. Voir [Stores](../state/stores.md).

```java
@UIProperty
private double zoomLevelConfig = 1D;

// Automatiquement persisté à la fermeture, restauré au chargement.
```

Chaque UI garde ses propriétés dans `config/property/<nom de la classe>.property`, en JSON : tout type que Gson sait écrire fait l'aller-retour — primitifs, chaînes, listes, maps et objets simples, génériques compris. `@UIProperty("key")` nomme l'entrée `key` au lieu du champ, et un champ remis à `null` est retiré du fichier, pour que le prochain chargement garde sa valeur par défaut. Un fichier corrompu est supprimé et les valeurs par défaut restent.

## Coordonnées et échelle

Une UI se conçoit sur un canevas de 1920×1080. `ui.getView()` renvoie la `UIView` qui place ce canevas dans la fenêtre, et toutes les conversions passent par elle — dessin, souris, infobulles, résolution des effets :

1. le canevas remplit la fenêtre, élargi ou rehaussé quand le ratio de la fenêtre diffère, et placé selon `anchorX` / `anchorY` ;
2. l'**échelle d'interface** choisie par l'hôte s'applique autour de l'ancre (voir [Échelle d'interface](bridge.md#chelle-d-interface)) ;
3. le **zoom** de l'utilisateur (CTRL + `+` / `-`) s'applique autour de l'ancre.

```java
ui.getMouseX();                       // souris sur le canevas, toutes transformations comprises
ui.getView().toScreenX(x);            // canevas → pixels de la fenêtre (aussi toScreenY / toScreenWidth / toScreenHeight)
ui.getView().toUiX(screenX);          // pixels de la fenêtre → canevas (aussi toUiY)
ui.getView().getVisibleWidth();       // largeur du canevas visible à l'écran, reflétée par le signal scaledWidth
ui.zoom(0.8D);
```

Le zoom va de `0.1` à `max(1, 1 / échelle d'interface)` : une interface réduite peut toujours être zoomée jusqu'à sa pleine taille. Les signaux `zoomLevel`, `scaledWidth` et `scaledHeight` suivent chaque changement.

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