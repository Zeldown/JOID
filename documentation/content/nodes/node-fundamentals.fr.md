# Node Fundamentals

Un `Node` est la brique atomique d'une UI — position, taille, effets, callbacks, enfants. Tout ce qui est visible étend `Node`. Cette page couvre l'API utilisée sur chaque nœud, quel que soit son type concret.

## Création & attachement

Tous les nœuds suivent le même pattern de factory :

```java
ConcreteNode.create(x, y, width, height)
    .<setters...>
    .body(node -> {
        // enfants
    })
    .attach(parent);
```

- `create` est une factory statique. Certains nœuds ont plusieurs overloads.
- Les setters retournent le nœud typé pour un chaînage safe dans les sous-classes.
- `body(Consumer<Node>)` permet de configurer les enfants en ligne sans casser la chaîne.
- `attach(parent)` ajoute le nœud au parent et retourne le nœud.

> TIP: **Chaînez court.** Une chaîne fluide de 40 lignes devient illisible. Extrayez les sous-arbres complexes dans des méthodes helpers qui retournent un nœud configuré.

## Position & taille

Les nœuds utilisent un espace logique 1920×1080, indépendamment de la résolution réelle du viewport.

### Valeurs directes

```java
node.x(double);
node.y(double);
node.width(double);
node.height(double);
node.position(double x, double y);
node.size(double w, double h);
```

### Helpers relatifs

Calculer des positions relatives au parent :

| Méthode | Signification |
|---|---|
| `dw(n)` | Width divisé par `n` (par ex. `dw(2)` = moitié de la largeur) |
| `dh(n)` | Height divisé par `n` |
| `ax(value)` | X absolu, compte tenu de l'ancre |
| `ay(value)` | Y absolu, compte tenu de l'ancre |
| `aw(delta)` | Offset X depuis le bord droit (`aw(-100)` = 100 à gauche du bord droit) |
| `ah(delta)` | Offset Y depuis le bord bas |

Exemple — centrer un enfant dans son parent :

```java
parent.body(p -> {
    RectNode.create(p.dw(2) - 50, p.dh(2) - 25, 100, 50)
        .color(Color.RED)
        .attach(p);
});
```

### Ratio d'aspect

```java
node.aspectRatio(1.77D);  // width = height * 1.77, recalculé au resize
```

## Z-index & ordre

```java
node.zindex(int);
node.zlevel(double);
```

- `zindex` réordonne parmi les enfants du même parent. Plus grand = dessiné plus tard (au-dessus).
- `zlevel` translate le nœud sur Z en GL — utile pour les tooltips qui doivent toujours rendre au-dessus des siblings.

## Hover

JOID tracke l'état de survol automatiquement via `isHovered(mouseX, mouseY)`. Deux choses sont exposées :

```java
node.hoverDuration(long);              // ms du fade hover, défaut 200
node.hoverEquation(TweenEquation);     // easing du fade, défaut LINEAR
node.hovered();                        // boolean : actuellement survolé ?
node.hoverValue(float max);            // 0F → max, interpolé pendant le fade
```

Utilisez `hoverValue(1F)` pour blender des couleurs ou scale au hover :

```java
RectNode.create(0, 0, 100, 50)
    .color(() -> Color.WHITE.to(Color.RED, this.hoverValue(1F)))
    .attach(parent);
```

Pour des états de hover statiques (switch entre deux couleurs), le setter `color(normal, hovered)` fait ça automatiquement.

## Effets

Les effets modifient la façon dont un nœud est rendu. Empilez-en autant que vous voulez :

```java
node.effect(RoundedNodeEffect.create(12F));
node.effect(BorderNodeEffect.create(Color.WHITE, 2F));
node.effect(BlurNodeEffect.create(4F));
```

Les effets se composent par ordre de priorité. Récupérer un effet :

```java
RoundedNodeEffect<?> rounded = node.getEffect(RoundedNodeEffect.class);
```

Voir [Effects Overview](../effects/overview.md).

## Overflow

Contrôlez comment les enfants au-delà des bounds sont traités :

```java
node.overflow(OverflowProperty.HIDDEN);  // clip
node.overflow(OverflowProperty.SCROLL);  // active le scroll (attachez un ScrollbarNode)
node.overflow(OverflowProperty.NONE);    // pas de clip (défaut)
```

Avec `SCROLL`, les `scrollX` / `scrollY` du nœud se mettent à jour selon la molette et les positions des enfants sont offsetées en conséquence.

## Draggable

Rendre un nœud déplaçable à la souris. `DraggableProperty` expose une factory par area type :

```java
node.draggable(DraggableProperty.free());                    // sans restriction
node.draggable(DraggableProperty.parent());                  // reste dans le parent
node.draggable(DraggableProperty.node(other));               // reste dans un autre nœud
node.draggable(DraggableProperty.ui());                      // reste dans l'UI 1920×1080
node.draggable(DraggableProperty.screen());                  // reste dans le viewport
node.draggable(DraggableProperty.custom(x, y, w, h));        // reste dans une box custom
node.draggable(DraggableProperty.disabled());                // jamais draggable
```

Il n'y a pas de factory `horizontal()` / `vertical()` / `zone()` — contraignez un axe en clampant la valeur vous-même dans `onDrag`. Voir [Drag & Drop](../interactions/drag-drop.md) pour le snap, les drag copies et les callbacks.

## Callbacks

Attachez le comportement via l'API fluide. Chaque setter prend une lambda dont les paramètres matchent la signature `apply(...)` du callback :

```java
node.onInit((n) -> { });
node.onRender((n, mouseX, mouseY, partialTicks) -> { });
node.onDraw((n, mouseX, mouseY, partialTicks) -> { });
node.onUpdate((n) -> { });
node.onReload((n) -> { });
node.onAppend((n, child) -> { });
node.onDetach((n) -> { });                                                       // cleanup — libérez les ressources ici
node.onMount((n) -> { });                                                        // première frame rendue
node.onClick((n, mouseX, mouseY, clickType) -> { });
node.onMousePressed((n, mouseX, mouseY, clickType) -> { });
node.onMouseReleased((n, mouseX, mouseY, clickType) -> { });
node.onMouseDragged((n, mouseX, mouseY, clickType, deltaTime) -> { });
node.onMouseScroll((n, mouseX, mouseY, value) -> { });
node.onKeyPressed((n, character, keyCode) -> { });
node.onScrollUpdate((n, value) -> { });
node.onScrollEnd((n, scrollX, scrollY) -> { });
node.onAnimation((n, animator, value) -> { });
node.onDrag((n) -> { });
node.onSnap((n, snapTarget) -> { });
node.onWatch((n, signal, properties) -> { });                                   // signaux réactifs
```

Voir [Callbacks](../interactions/callbacks.md) pour tous les détails (phases PRE/POST, `InternalContext`).

## Tooltips de hover

Afficher un tooltip au survol :

```java
node.hover(() -> "Simple text");
node.hover(() -> Arrays.asList("Line 1", "Line 2"));
node.hover(MyTooltipNode.create(...));   // nœud custom complet
```

Voir [Hover](../interactions/hover.md).

## Watches réactifs

Lier un nœud à un signal — le nœud se recharge automatiquement quand le signal change :

```java
node.watch(signal);                                // défaut : WatchProperty.RELOAD
node.watch(signal, WatchProperty.CLEAR_CHILDREN);
node.watch(signal, () -> condition, properties);   // watch conditionnel
```

Voir [Watch](../state/watch.md).

## Opérations sur l'arbre

```java
node.append(childA, childB, childC);
node.clearChildren();
node.getChildren();
node.getChildren(Class<T>);        // filtre typé
node.getChild(index, Class<T>);
node.getParent();
node.getUi();                      // UI racine
```

`clearChildren()` déclenche `onDetach()` sur chaque enfant — respectez-le pour le cleanup des ressources.

## Visibility & enabled

```java
node.visible(boolean);   // non dessiné, non interactif
node.enabled(boolean);   // dessiné, mais pas de click/drag/scroll
```

Les deux acceptent un `Supplier<Boolean>` pour des versions réactives :

```java
node.visible(() -> !someBoolean.getOrDefault());
```

## Bonnes pratiques

- **Attachez en dernier, toujours.** Les chaînes qui ne finissent pas par `.attach(parent)` leak des nœuds.
- **Utilisez `body()` pour grouper les enfants.** Plus clair que de rouvrir une chaîne `.attach(...).getChildren().add(...)`.
- **Ne pollez pas — watchez.** Les bindings réactifs sont peu coûteux ; poller dans `update()` l'est.
- **Libérez dans `onDetach()`.** Si votre nœud tient une ressource (socket, thread, texture), overridez `detach()` ou enregistrez un `onDetach(callback)` pour la libérer.
- **Ne traversez pas les frontières d'UI.** N'attachez jamais un nœud d'une UI dans une autre — la référence `ui` ne matchera pas.

## Voir aussi

- [Callbacks](../interactions/callbacks.md) — catalogue complet.
- [Hover](../interactions/hover.md) — tooltips et interactions.
- [Drag & Drop](../interactions/drag-drop.md).
- [Signals](../state/signals.md) + [Watch](../state/watch.md) — state réactif.