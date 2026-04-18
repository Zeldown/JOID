# DrawUtils

`DrawUtils` est le point d'entrée pour dessiner en ad-hoc hors de l'arbre de nœuds. À utiliser dans `preDraw` / `postDraw`, à l'intérieur de surcharges custom de `Node.draw()`, ou dans des prototypes jetables. Pour tout ce qui nécessite du hover, drag, effets ou réactivité, préférez un `Node`.

## Les quatre façades

`DrawUtils` n'est que quatre singletons `public static final` — pas de constructeur, rien à instancier.

```java
DrawUtils.SHAPE       // DrawShape
DrawUtils.TEXT        // DrawText
DrawUtils.RESOURCE    // DrawResource
DrawUtils.MODEL       // DrawModel
```

Chaque façade a sa page dédiée listant chaque méthode :

- `Shapes` — rectangles, rectangles arrondis, cercles, bordures, lignes, courbes, polygones.
- `Text` — chaînes et builders `Text` riches avec alignement et overflow.
- `Resources` — images et vidéos depuis un `Resource`.
- `Models` — instances 3D `IDrawableModel`.

## Quand l'utiliser

### Overlay dans `postDraw`

```java
@Override
public void postDraw(final double mouseX, final double mouseY) {
    DrawUtils.SHAPE.drawCircle(mouseX, mouseY, Color.WHITE, 4D);
}
```

### Fond dans `preDraw`

```java
@Override
public void preDraw(final double mouseX, final double mouseY) {
    DrawUtils.SHAPE.drawRect(0, 0, getWidth(), getHeight(), Color.decode("#111827"));
}
```

### `draw()` d'un nœud custom

```java
@Override
public void draw(final double mouseX, final double mouseY) {
    DrawUtils.SHAPE.drawRect(getX(), getY(), getWidth(), getHeight(), Color.RED);
    DrawUtils.TEXT.drawText(getX() + 8, getY() + 8, "Custom node", info,
        Align.START, Align.START);
}
```

## Quand ne pas l'utiliser

- **Ne recréez pas un `RectNode` à la main.** `RectNode` + `RoundedNodeEffect` est moins de code et s'intègre avec hover/drag/effets.
- **Ne layoutez pas avec `DrawUtils`.** Tournez-vous vers `FlexNode`, `GridNode`, ou `ContainerNode`.
- **Ne réagissez pas à l'état via `DrawUtils`.** Le dessin est immédiat — utilisez `Signal` + `Node.watch` pour piloter le contenu.

## Accès direct aux singletons

Chaque façade expose son instance si vous préférez contourner `DrawUtils` :

```java
DrawShape.getInstance().drawRect(...);
DrawText.getInstance().drawText(...);
DrawResource.getInstance().drawResource(...);
DrawModel.getInstance().drawModel(...);
```

Pratiquement identique au raccourci `DrawUtils.XXX` — choisissez ce qui se lit le mieux en contexte.

## Voir aussi

- `Shapes` — tous les primitifs de forme.
- `Text` — chaînes brutes, builders `Text`, modifiers.
- `Resources` — dessin d'images / vidéos.
- `Models` — dessin de modèles 3D.
- `Color` — construction et binding de couleur.