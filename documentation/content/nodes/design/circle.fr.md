# CircleNode

Un cercle plein. Plus simple que `RectNode + CircleNodeEffect` quand on veut uniquement une forme circulaire solide.

## Créer

```java
CircleNode.create(x, y, diameter)
    .color(Color.RED)
    .attach(parent);
```

Le troisième paramètre est le **diamètre** (le côté du carré englobant). `x / y` pointent vers le coin haut-gauche de ce carré ; le cercle y est inscrit. Les `width` et `height` du nœud valent tous deux `diameter`.

## API

```java
T color(Color color)
T color(Color normal, Color hovered)
T hoveredColor(Color color)
```

C'est toute la surface : il n'y a **pas** de `border(...)` sur `CircleNode`. Pour un cercle avec contour, utilisez `RectNode + BorderNodeEffect + CircleNodeEffect`, ou dessinez le contour vous-même avec `DrawUtils.SHAPE`.

La couleur de hover est interpolée via `hoverValue(1F)` à chaque draw.

## Quand utiliser `RectNode + CircleNodeEffect` à la place

- **Gradients** sur le cercle — `RectNode.color(gradient).effect(CircleNodeEffect.create())` passe par le pipeline de shaders et gère le gradient correctement.
- **Formes d'ellipse** — l'effet utilise `min(width, height)` comme diamètre, donc un `RectNode` non-carré donne un masque en forme d'ellipse.
- **Bordures ou autres effets** — seul le pipeline de `RectNode` compose les effets.

Le `CircleNode` brut est basé sur `DrawUtils.SHAPE.drawCircle` et ne compose pas de shader passes.

## Exemple — badge d'avatar

```java
CircleNode.create(0, 0, 48)
    .color(Color.decode("#3b82f6"))
    .hover(() -> "Online")
    .attach(parent);
```

## Voir aussi

- [RectNode](rect.md) — quand vous avez besoin de gradients, bordures ou compositing.
- [Circle Effect](../../effects/circle.md) — masque circulaire sur n'importe quel nœud.