# Hover

Tooltips et état de survol. JOID tracke le hover automatiquement et l'expose aux callbacks, aux couleurs et aux nœuds d'infobulle.

## État de hover

```java
node.hovered();                   // boolean : la souris est-elle dessus ?
node.hoverValue(float max);       // 0 → max, interpolé sur hoverDuration
node.hoverDuration(long ms);      // durée du fade (défaut 200ms)
node.hoverEquation(TweenEquation); // easing du fade (défaut LINEAR)
```

`hoverValue` est le cœur — utilisez-le pour lerp des couleurs, scales, bordures :

```java
RectNode.create(0, 0, 100, 50)
    .color(() -> Color.decode("#1f2937").to(Color.decode("#3b82f6"), node.hoverValue(1F)))
    .attach(parent);
```

Ou utilisez le raccourci intégré :

```java
node.color(Color.decode("#1f2937"), Color.decode("#3b82f6"));
```

qui fait exactement la même chose en interne.

## Tooltips via `hover(...)`

Chaîne simple :

```java
node.hover(() -> "Click me!");
```

Plusieurs lignes :

```java
node.hover(() -> Arrays.asList(
    "Save document",
    "Shortcut: Ctrl+S"
));
```

Conditionnel :

```java
node.hover(() -> isDirty() ? "Save (unsaved changes)" : null);   // null = pas de tooltip
```

## Nœuds tooltip custom

Pour des tooltips plus riches (icônes, couleurs, layouts) :

```java
node.hover(CustomHoverElement.follow(new MyTooltipNode()));
```

Trois modes de positionnement via `HoverElementPosition` :

```java
CustomHoverElement.follow(element)      // suit la souris
CustomHoverElement.relative(element)    // relatif au nœud
CustomHoverElement.fixed(element)       // fixé en (0, 0)
```

`HoverElement` est une interface implémentable :

```java
HoverElement tooltip = new HoverElement() {
    @Override
    public void render(Node node, double mx, double my) {
        DrawUtils.SHAPE.drawRect(0, 0, 200, 60, Color.decode("#1f2937"));
        DrawUtils.TEXT.drawText(8, 8, Text.create("Rich content", info));
    }

    @Override public double getWidth() { return 200; }
    @Override public double getHeight() { return 60; }
};

node.hover(CustomHoverElement.follow(tooltip));
```

`getWidth` / `getHeight` sont utilisés par le clamp aux bords d'écran pour éviter que le tooltip sorte de l'écran.

## Nœud comme tooltip

Utiliser un autre nœud comme contenu de tooltip :

```java
final RectNode tooltipNode = RectNode.create(0, 0, 200, 60)
    .color(Color.decode("#1f2937"))
    .effect(RoundedNodeEffect.create(6F));

node.hover(NodeHoverElement.follow(tooltipNode));
```

Le nœud tooltip est rendu dans l'overlay de hover, pas comme enfant — il n'affecte pas le layout.

## Clamp aux bords d'écran

Si un tooltip dépasserait le bord droit de l'écran, JOID le décale vers la gauche. S'il dépasse le haut, il le décale vers le bas. Activé automatiquement pour les positions `FOLLOW` et `RELATIVE`.

## Détecter le hover

Il n'y a **pas** de callback `onHover` par frame — interrogez plutôt l'état via `node.isHovered()` depuis `onUpdate` ou un override de `draw()`. Pour la sémantique enter/leave, tenez un flag local :

```java
final boolean[] wasHovered = { false };

node.onUpdate((n, ctx) -> {
    final boolean now = n.isHovered();
    if (now && !wasHovered[0]) onEnter();
    if (!now && wasHovered[0]) onLeave();
    wasHovered[0] = now;
});
```

## Bonnes pratiques

- **Utilisez des chaînes pour les tooltips simples.** Ne construisez pas un nœud complet pour un hint d'une ligne.
- **Cachez les nœuds tooltip.** Créez une fois, réutilisez l'instance dans `node.hover(...)`.
- **Gardez le contenu du tooltip petit.** 200×80 px est un soft cap ; plus grand devient intrusif.
- **Alignez le style tooltip sur le thème de l'app.** `Color.decode("#1f2937")` pour sombre, `#f3f4f6` pour clair.

## Voir aussi

- [Node Fundamentals](../nodes/node-fundamentals.md) — API de l'état de hover.
- [Callbacks](callbacks.md) — détails des callbacks hover.