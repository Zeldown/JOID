# Core Concepts

Un modèle mental court pour construire avec JOID. Chaque concept a sa page dédiée — celle-ci en est la carte.

## Bootstrap

Avant d'ouvrir la moindre UI, initialisez la bibliothèque une fois avec `JOID.inst().load()`. C'est un builder — chaînez les flags qui vous intéressent et terminez par `load()` :

```java
JOID.inst()
    .setConfigDir(new File("config"))
    .setDevMode(false)
    .setDemoMode(false)
    .load();
```

### `setConfigDir(File)`

Dossier racine de l'état persistant. Les fichiers JSON des `UIStore` (`@UIStoreData`) et toute persistance interne atterrissent dans ce dossier. S'il n'existe pas, JOID le crée au `load()`. Défaut : `./config`.

### `setDevMode(boolean)`

Active des comportements réservés au développement. Quand `true` :

- **Alt-glisser** sur un nœud imprime ses coordonnées et permet de le déplacer à la volée — utile pendant qu'on positionne à l'œil.
- **Alt + flèches** nudge le nœud survolé d'un pixel.
- **Overlay profiler** disponible quand l'UI déclare `@UIData(debug = @Debug(profiler = true))`.
- **Hot-reload** surveille les fichiers source et relance `init()` en cas de changement (même flag `@Debug`).
- **Logs d'introspection de layout** pour les problèmes structurels.

Laissez `false` en production — les gestes de debug et le file-watcher sont inutiles, et les bindings Alt peuvent entrer en conflit avec vos propres raccourcis.

### `setDemoMode(boolean)`

Charge la `DemoFont` embarquée (shippée avec l'artefact `-dev`) pour que les snippets du quick-start, les UIs de démo et les exemples de doc aient une police utilisable sans que vous fournissiez votre propre atlas MSDF. Une fois vos polices shippées via un `CustomFontProvider`, désactivez-le.

`load()` doit être appelée **exactement une fois**, avant d'enregistrer des bridges ou d'ouvrir des UIs.

## La racine UI

Chaque écran est une classe qui étend `UI` (qui implémente `IUI`). Les hooks que vous surchargez sont définis sur `IUI` avec des valeurs par défaut sensées — implémentez uniquement ce qui vous intéresse :

1. `new YourUI()` — constructeur.
2. `JOID.open(ui)` — remet l'UI à son bridge.
3. `init()` — vous y attachez les nœuds, une seule fois.
4. À chaque frame : `preDraw(mouseX, mouseY)` → rendu interne des nœuds → `postDraw(mouseX, mouseY)`, plus `update()` pour la logique par frame.
5. `close()` — retourne `true` si l'UI peut se fermer (défaut `true`).

Quand la fermeture est accordée, JOID appelle `properlyClose()` en interne : les stores sont sauvegardés, les nœuds détachés, et le bridge retire l'UI. Le rendu dans la boucle globale passe par `UI.draw(mouseX, mouseY)`, qui est `final` — utilisez `drawBackground`, `preDraw`, `postDraw` pour dessiner vous-même.

Les UIs se configurent via l'annotation `@UIData` (zoomable, pausable, backgroundColor, closeable…). Voir `UI Class`.

## L'arbre de nœuds

Tout ce qui est visible est un `Node`. Les nœuds forment un arbre :

```
UI
├── FlexNode
│   ├── RectNode
│   ├── RectNode
│   └── TextNode
└── ContainerNode
    └── ImageNode
```

Chaque nœud connaît sa position (`x`, `y`), sa taille (`width`, `height`), son z-index, son parent et ses enfants. Vous construisez l'arbre via des **chaînes fluides** qui se terminent par `.attach(parent)`.

```java
RectNode.create(0, 0, 100, 50)
    .color(Color.RED)
    .onClick(handler)
    .attach(parent);
```

**Règles du pattern builder** (respectez-les pour chaîner ergonomiquement) :

- `create(...)` construit.
- Les setters retournent `this` typé `<T extends Node>` pour un chaînage safe dans les sous-classes.
- `body(consumer)` configure les enfants en ligne.
- `attach(parent)` est toujours en dernier et attache le nœud.

Voir [Node Fundamentals](../nodes/node-fundamentals.md).

## Nœuds de layout vs de design

Deux familles de nœuds :

- **Nœuds de structure** calculent le layout de leurs enfants — `FlexNode`, `GridNode`, `ContainerNode`, `ScrollbarNode`. Vous surchargez rarement leur rendu.
- **Nœuds de design** dessinent du contenu — `RectNode`, `CircleNode`, `TextNode`, `ResourceNode`, `TextFieldNode`, `VideoPlayerNode`. Vous les stylisez avec des effets et des couleurs.

Une UI bien construite est majoritairement *des nœuds de design à l'intérieur de nœuds de structure*.

## Effets

Les effets sont un post-processing appliqué à la sortie rendue d'un nœud. Il en existe deux saveurs :

- **Effets de forme** : `RoundedNodeEffect`, `CircleNodeEffect` — modifient la silhouette.
- **Effets de shader** : `BlurNodeEffect`, `BorderNodeEffect`, `RoundedNodeEffect`, `CircleNodeEffect` — passes GL complètes composées via le [Shader Pipeline](../shaders/pipeline.md). Les gradients sont natifs sur `Color` (voir [`Color.toGradient`](../drawing/color.md)).

Chaînez plusieurs effets ; ils se composent par ordre de priorité.

```java
RectNode.create(0, 0, 200, 100)
    .color(Color.BLUE)
    .effect(RoundedNodeEffect.create(16F))
    .effect(BorderNodeEffect.create(Color.WHITE, 2F))
    .effect(BlurNodeEffect.create(4F))
    .attach(parent);
```

## État réactif

L'état vit dans des observables `Signal<T>`. Les nœuds s'abonnent via `.watch(signal, property)` :

```java
final StringSignal name = new StringSignal("world");

TextNode.create(0, 0)
    .text(() -> Text.create("Hello, " + name.getOrDefault(), info))
    .watch(name)  // auto-reload sur name.set(...)
    .attach(parent);
```

L'enum `WatchProperty` contrôle ce qui se passe au changement — `RELOAD` (défaut, relance `init()`), `BODY` (relance le body consumer uniquement), `CLEAR_CHILDREN` ou `NONE`. Voir [Signals](../state/signals.md) et [Watch](../state/watch.md).

Pour un état cross-UI / persistant, utilisez `UIStore` avec l'annotation `@UIStoreData` — sérialisé en JSON dans `config/store/`. Voir [Stores](../state/stores.md).

## Ressources

Tout visuel qui n'est pas une forme ou du texte est une `Resource` : image, vidéo, GIF. Chargement via `ResourceBuilder` :

```java
Resource image = Resource.of(MyClass.class.getResourceAsStream("/icon.png"));
Resource remote = Resource.of("https://example.com/image.png");
Resource video = Resource.of(MyClass.class.getResourceAsStream("/movie.mp4"));  // auto-détecté
```

Placez-les dans un `ResourceNode`, un `VideoPlayerNode`, ou dessinez directement via `DrawUtils.RESOURCE`. Voir [ResourceBuilder](../resources/resource-builder.md).

## Bonnes pratiques

- **Construisez dans `init()`, mutez via des signaux.** Ne pas appeler `this.append(...)` depuis `draw()` — la reconstruction déclenche un reload complet coûteux.
- **Utilisez des nœuds de structure pour le layout.** Une grille de `RectNode` positionnée à la main est un code smell ; `GridNode` existe.
- **Mettez vos `TextInfo` en cache.** Ils contiennent la police, la taille, la couleur — en créer un par frame est du gaspillage.
- **Libérez les ressources lourdes.** `VideoPlayerNode` le fait déjà via `detach()` ; pour vos propres décodeurs lourds, surchargez `detach()` de la même façon.
- **Préférez `toGradient(other)` aux shaders de gradient manuels.** `Color` supporte les gradients nativement et le renderer choisit le bon shader automatiquement.

## Modèle de rendu

Chaque frame :

1. `UI.onUpdate()` — `update()` récursif sur tous les nœuds, avancement des tweens.
2. `UI.draw()` — setup de la projection, puis `Node.render()` récursif depuis la racine.
3. Chaque `Node.render()` :
   - exécute `CALLBACK_MOUNT` une fois,
   - sépare les effets en shader / non-shader,
   - les effets non-shader wrappent `pre/post`,
   - les effets shader construisent une liste `ShaderPass` et la déléguent à `ShaderPipeline.render(node, passes, baseDraw)`,
   - `baseDraw` masque aux bounds du nœud, rend les enfants et appelle votre `draw()`.

Vous n'avez presque jamais besoin de vous en soucier en interne — mais le savoir aide à débugger les états GL.

Ensuite, sautez dans [UI Class](../ui/ui-class.md) ou [Node Fundamentals](../nodes/node-fundamentals.md) selon ce que vous voulez construire en premier.