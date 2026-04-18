# Drag & Drop

Rendre des nœuds déplaçables à la souris. Le mouvement est contraint par un **area type**, des cibles de snap peuvent être enregistrées, et le drag peut soit déplacer le nœud lui-même soit un clone.

## Activer le drag

`DraggableProperty` expose un jeu de factories statiques — une par area type. Il n'y a pas de factory `horizontal` ou `vertical` ; restreignez un axe en clampant les coordonnées vous-même dans `onDrag`.

```java
node.draggable(DraggableProperty.free());                                  // sans restriction
node.draggable(DraggableProperty.parent());                                // reste dans le parent
node.draggable(DraggableProperty.node(otherNode));                         // reste dans un autre nœud
node.draggable(DraggableProperty.ui());                                    // reste dans l'UI 1920×1080
node.draggable(DraggableProperty.screen());                                // reste dans le viewport
node.draggable(DraggableProperty.custom(x, y, width, height));             // reste dans une box custom
node.draggable(DraggableProperty.disabled());                              // jamais draggable
```

Sous le capot, chaque factory appelle `area(DraggableAreaType, Object)` avec une valeur parmi `FREE`, `CUSTOM`, `PARENT`, `NODE`, `UI`, `SCREEN`.

## Types de drag

Configurer ce qui est déplacé :

```java
DraggableProperty.free().type(DraggableType.MOVE);     // défaut — le nœud lui-même bouge
DraggableProperty.free().type(DraggableType.COPY);     // un clone est draggé, l'original reste
```

`COPY` est utile pour les palettes d'outils — draggez une copie de la palette dans le workspace.

## Prédicat enabled

Toutes les factories acceptent un prédicat optionnel :

```java
DraggableProperty.free().enabled(n -> !n.isLocked());
```

Si le prédicat retourne `false`, le drag est bloqué. `DraggableProperty.disabled()` est le raccourci pour `enabled(n -> false)`.

## Cibles de snap

Les cibles de snap sont enregistrées via les setters `snap(...)` :

```java
DraggableProperty.free()
    .snap(otherNode, anotherNode, aThirdOne);                              // append des cibles
DraggableProperty.free()
    .snap(DraggableSnapType.NEAREST, otherNode, anotherNode);              // reset + définit le type
```

`DraggableSnapType` vaut :

- `NEAREST` (défaut) — snap sur la cible dont le centre est le plus proche du nœud draggé.
- `OVERLAP` — snap sur la première cible que le nœud draggé chevauche.

Quand un snap se déclenche, le callback `onSnap` reçoit la cible :

```java
node.onSnap((dragged, context, snapTarget) -> {
    System.out.println("Snapped onto " + snapTarget);
});
```

## Callbacks de drag

```java
node.onDrag((n, context) -> {
    // se déclenche à chaque frame pendant le drag
});
```

Pour les hooks start/stop, utilisez les callbacks souris existants :

```java
node.onMousePressed((n, mx, my, ct, ctx) -> { if (ct == ClickType.LEFT) startDrag(); });
node.onMouseReleased((n, mx, my, ct, ctx) -> { if (ct == ClickType.LEFT) stopDrag(); });
```

Le drag lui-même est câblé automatiquement par `Node` quand `draggable(...)` est défini — vous n'appelez `startDragging()` à la main que pour un contrôle programmatique.

## Exemple — carte contrainte à une zone custom

```java
RectNode.create(0, 0, 120, 60)
    .color(Color.decode("#1f2937"))
    .effect(RoundedNodeEffect.create(6F))
    .draggable(DraggableProperty.custom(0, 0, 1920, 200))                  // uniquement dans une bande haute
    .attach(parent);
```

## Exemple — palette → workspace (COPY)

```java
final DraggableProperty palette = DraggableProperty.ui().type(DraggableType.COPY);

for (final Icon icon : icons) {
    RectNode.create(0, 0, 48, 48)
        .color(Color.decode("#1f2937"))
        .draggable(palette)
        .attach(paletteRow);
}
```

Un clone de chaque icône de palette est draggé ; les originaux restent en place.

## État global du drag

Accéder au nœud actuellement draggé depuis n'importe où :

```java
Node dragged = ui.getDraggedNode();    // null si rien n'est draggé
```

Utile pour des drop zones qui surlignent quand un drag compatible est en cours.

## Bonnes pratiques

- **Choisissez l'area type la plus serrée.** `screen()` laisse les nœuds sortir du layout UI ; préférez `parent()` ou `custom()` quand vous voulez du containment.
- **Utilisez snap pour les puzzles UI.** Mini-jeux sort-into-bucket, docking de panels, reordering de form fields.
- **Ne mutez pas l'arbre dans `onDrag`.** Mettez à jour des signaux et laissez l'arbre réagir — ajouter/retirer des nœuds en plein drag est unsafe.

## Voir aussi

- [Node Fundamentals](../nodes/node-fundamentals.md) — property draggable.
- [Callbacks](callbacks.md) — événements souris.