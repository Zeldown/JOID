# RectNode

Le cheval de trait de JOID. Un rectangle rempli avec bordure optionnelle, gradient, coins arrondis et couleurs de hover. La plupart des éléments UI sont construits sur `RectNode`.

## Créer

```java
RectNode.create(x, y, width, height)
    .color(Color.RED)
    .attach(parent);
```

## Couleur

```java
node.color(Color color);
node.color(Color normal, Color hovered);
node.color(Supplier<Color> colorFn);
node.color(Supplier<Color> colorFn, Supplier<Color> hoveredColorFn);
node.hoveredColor(Color color);
```

Quand `color` et `hovered` sont tous deux fournis, le renderer interpole via `node.hoverValue(1F)` automatiquement.

Les couleurs peuvent être flat ou **gradients** — le renderer le détecte et utilise le pipeline de shaders :

```java
node.color(Color.RED.toGradient(Color.BLUE));
node.color(Color.CYAN.toGradient(Color.MAGENTA, new Vector4f(0F, 0F, 0F, 1F))); // vertical
```

## Bordure

```java
node.border(Color color, double stroke);
node.border(Color color, double stroke, boolean fill);
node.border(Color color, Color hoveredColor, double stroke, boolean fill);
```

- `stroke` — largeur de bordure en unités logiques.
- `fill` — `true` (défaut) : la bordure remplit la zone de stroke. `false` : la bordure est un contour laissant la stroke vide.

Les bordures supportent aussi les gradients :

```java
node.border(Color.WHITE.toGradient(Color.GRAY), 2D);
```

## Effets

Combinez avec des effets pour coins arrondis, cercles, flou :

```java
RectNode.create(0, 0, 200, 100)
    .color(Color.BLUE.toGradient(Color.MAGENTA))
    .border(Color.WHITE, 2D)
    .effect(RoundedNodeEffect.create(16F))
    .attach(parent);
```

Quand `RoundedNodeEffect` ou `CircleNodeEffect` est présent, `RectNode` route son rendu via le [Shader Pipeline](../../shaders/pipeline.md). Toutes les combinaisons fonctionnent :

```java
RectNode.create(0, 0, 200, 100)
    .color(Color.RED.toGradient(Color.YELLOW))
    .effect(CircleNodeEffect.create())
    .effect(BorderNodeEffect.create(Color.WHITE, 5F, BorderMode.IN))
    .attach(parent);
```

## Exemple — carte avec hover

```java
RectNode.create(0, 0, 400, 120)
    .color(
        Color.decode("#1f2937"),
        Color.decode("#374151")
    )
    .effect(RoundedNodeEffect.create(12F))
    .effect(BorderNodeEffect.create(Color.decode("#4b5563"), 1F))
    .body(card -> {
        TextNode.create(16, 16)
            .text(Text.create("Hello world", info))
            .attach(card);
    })
    .attach(parent);
```

## Bonnes pratiques

- **Définissez toujours une couleur.** Un `RectNode` sans `color()` est transparent (défaut `Color.TRANSPARENT`).
- **Préférez les effets au dessin manuel.** N'imbriquez pas un `RectNode` plus petit dans un arrondi pour faker une bordure — utilisez `border()` et `effect()`.
- **Utilisez les gradients via `Color.toGradient()`.** Ne bindez pas les shaders à la main ; le renderer s'en occupe.

## Voir aussi

- [Rounded Effect](../../effects/rounded.md)
- [Circle Effect](../../effects/circle.md)
- [Border Effect](../../effects/border.md)
- [Color](../../drawing/color.md) — `Color.toGradient(...)` pour les remplissages en gradient.