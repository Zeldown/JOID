# GridNode

Grille à colonnes fixes avec wrap automatique de rangées. Enfants de même taille, espacés régulièrement, disposés de gauche à droite puis de haut en bas.

## Créer

```java
GridNode.create(x, y, width, height)
    .horizontalMargin(12D)
    .verticalMargin(12D)
    .body(grid -> {
        // enfants
    })
    .attach(parent);
```

La largeur des enfants détermine le nombre de colonnes : `floor(width / (childWidth + horizontalMargin))`. Une nouvelle rangée commence quand la courante déborde.

## API

```java
grid.horizontalMargin(double);    // gap entre colonnes
grid.verticalMargin(double);      // gap entre rangées
```

## Exemple — grille d'icônes

```java
GridNode.create(40, 40, 800, 600)
    .horizontalMargin(16D)
    .verticalMargin(16D)
    .body(grid -> {
        for (final Icon icon : icons) {
            RectNode.create(0, 0, 80, 80)
                .color(Color.decode("#1f2937"))
                .effect(RoundedNodeEffect.create(8F))
                .hover(() -> icon.name)
                .body(card -> {
                    try {
                        ResourceNode.create(16, 16, 48, 48)
                            .resource(Resource.of(icon.stream))
                            .attach(card);
                    } catch (IOException e) { e.printStackTrace(); }
                })
                .attach(grid);
        }
    })
    .attach(parent);
```

## Auto-croissance

Si l'`overflow` du `GridNode` est `NONE` et que les enfants dépassent la hauteur fixée, la grille croît automatiquement pour les contenir.

## Bonnes pratiques

- **Utilisez `GridNode` pour des éléments de taille uniforme.** Pour des tailles mixtes, `FlexNode` avec `wrap` est plus flexible.
- **Combinez avec `OverflowProperty.SCROLL`** pour les longues listes à scroller :

```java
RectNode.create(40, 40, 800, 400)
    .overflow(OverflowProperty.SCROLL)
    .body(wrapper -> {
        GridNode.create(0, 0, 800, 400).body(grid -> { ... }).attach(wrapper);
    })
    .attach(parent);
```

## Voir aussi

- [FlexNode](flex.md) — layouts linéaires.
- [ScrollbarNode](scrollbar.md) — overflow de scroll.