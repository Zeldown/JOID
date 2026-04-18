# FlexNode

Layout linéaire automatique — horizontal ou vertical — avec un gap configurable entre enfants et un alignement optionnel sur l'axe transversal. La taille du nœud sur l'axe principal croît pour tenir ses enfants ; la taille transversale est fixée par la factory choisie.

## Créer

```java
FlexNode.horizontal(x, y, height)         // row, hauteur fixe, largeur croissante
    .body(flex -> { /* enfants */ })
    .attach(parent);

FlexNode.vertical(x, y, width)             // column, largeur fixe, hauteur croissante
    .body(flex -> { /* enfants */ })
    .attach(parent);
```

Les enfants sont positionnés séquentiellement le long de l'axe principal avec des gaps `margin` entre eux.

## API

```java
FlexNode margin(double value)              // gap entre enfants (axe principal)
FlexNode align(Align align)                // START / CENTER / END (axe transversal)
FlexNode direction(FlexDirection dir)      // COLUMN / ROW — matche la factory
```

Seuls les enfants visibles (`isVisibleProperty()`) contribuent au layout — les enfants cachés gardent leur slot mais ne prennent pas d'espace.

## Exemple — toolbar

```java
FlexNode.horizontal(40, 40, 48).margin(8).body(toolbar -> {
    RectNode.create(0, 0, 48, 48).color(Color.decode("#1f2937")).attach(toolbar);
    RectNode.create(0, 0, 48, 48).color(Color.decode("#1f2937")).attach(toolbar);
    RectNode.create(0, 0, 48, 48).color(Color.decode("#1f2937")).attach(toolbar);
}).attach(parent);
```

Trois boutons 48×48 en ligne, 8px entre chaque. Pas de tracking X manuel.

## Exemple — liste de cartes

```java
FlexNode.vertical(40, 40, 400).margin(12).body(list -> {
    for (final Item item : items) {
        RectNode.create(0, 0, 400, 80)
            .color(Color.decode("#1f2937"))
            .effect(RoundedNodeEffect.create(8F))
            .body(card -> {
                TextNode.create(16, 16)
                    .text(Text.create(item.name, info))
                    .attach(card);
            })
            .attach(list);
    }
}).attach(parent);
```

## Enfants réactifs

`FlexNode` recalcule les positions à chaque frame (`draw`, `update`, `drawSkeleton`) — ajouter ou retirer des enfants met le layout à jour au tick suivant.

Pour des listes data-driven réactives :

```java
FlexNode.vertical(0, 0, 400).margin(8)
    .watch(itemSignal, WatchProperty.BODY)
    .body(list -> {
        for (Item item : itemSignal.getOrDefault()) {
            // rendre chaque
        }
    })
    .attach(parent);
```

`WatchProperty.BODY` relance le consumer `body` au changement de signal sans reconstruire toute l'UI.

## Bonnes pratiques

- **Préférez `FlexNode` au tracking X/Y manuel.** Moins de bugs à l'ajout/retrait.
- **Utilisez `margin` pas des offsets manuels.** `FlexNode` gère les gaps entre enfants proprement.
- **Pour les listes dynamiques, combinez avec `watch(signal, BODY)`** plutôt que de reconstruire toute l'UI.

## Voir aussi

- `GridNode` — layouts en grille stricte avec tailles égales.
- `ContainerNode` — wrapper sans layout.