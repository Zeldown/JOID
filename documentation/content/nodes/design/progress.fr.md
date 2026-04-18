# ProgressNode

Une barre de progression qui se remplit dans l'une des quatre directions. Rend soit avec des couleurs flat (fond + premier plan), soit avec des textures `Resource` jumelées.

## Créer

```java
ProgressNode.create(x, y, width, height)
    .color(Color.decode("#1f2937"), Color.decode("#3b82f6"))
    .progress(0.5F)
    .attach(parent);
```

## API

```java
T progress(float value)                           // 0.0 → 1.0
T progress(float min, float max, float value)     // valeur normalisée sur [0, 1]
T direction(ProgressDirection direction)          // direction de remplissage

T color(Color background, Color foreground)       // les deux couleurs en une fois
T background(Color color)
T foreground(Color color)

T resource(Resource background, Resource foreground)
T background(Resource resource)
T foreground(Resource resource)
```

### `ProgressDirection`

```java
LEFT_TO_RIGHT     // défaut
RIGHT_TO_LEFT
TOP_TO_BOTTOM
BOTTOM_TO_TOP
```

## Rendu couleur vs ressource

Si `background` et `foreground` sont tous deux des ressources, elles sont dessinées avec `DrawUtils.RESOURCE`, le foreground étant masqué à la fraction de progression courante (`UI.mask(...)`). Sinon, le nœud fallback sur deux appels `DrawUtils.SHAPE.drawRect` avec les deux couleurs.

## Progression réactive

`ProgressNode` est un `Node` standard — liez un signal via `.watch(...)` et mettez-le à jour de l'extérieur :

```java
final Signal<Float> loading = new Signal<>(0F);

ProgressNode.create(0, 0, 300, 8)
    .color(Color.decode("#1f2937"), Color.decode("#3b82f6"))
    .progress(loading.getOrDefault())
    .watch(loading, (node, value) -> node.progress(value))
    .effect(RoundedNodeEffect.create(4F))
    .attach(parent);
```

## Exemple — barre d'XP avec label

```java
final Signal<Float> xp = new Signal<>(0.73F);

RectNode.create(0, 0, 400, 24)
    .color(Color.decode("#111827"))
    .effect(RoundedNodeEffect.create(12F))
    .body(bar -> {
        ProgressNode.create(2, 2, 396, 20)
            .color(Color.decode("#0f172a"), Color.decode("#10b981"))
            .progress(xp.getOrDefault())
            .watch(xp, (n, v) -> n.progress(v))
            .effect(RoundedNodeEffect.create(10F))
            .attach(bar);

        TextNode.create(bar.dw(2), bar.dh(2))
            .text(() -> Text.create(
                String.format("%.0f%%", xp.getOrDefault() * 100F),
                TextInfo.create(myFont, 14, Color.WHITE),
                Align.CENTER, Align.CENTER
            ))
            .watch(xp)
            .attach(bar);
    })
    .attach(parent);
```

## Voir aussi

- `Signals` — binding de valeur réactif.
- `TweenAnimator` — animer `progress` dans le temps.