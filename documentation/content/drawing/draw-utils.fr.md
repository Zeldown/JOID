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

## Alignement sur les pixels

Les coordonnées sont des unités d'UI, et une unité couvre rarement un nombre entier de pixels de la fenêtre : 0,7115 pixel dans une fenêtre de 1366×768, 0,8 avec une mise à l'échelle Windows de 125 %. JOID garde ce qu'il dessine sur les pixels de la fenêtre, pour que rien ne scintille, ne saute ni ne disparaisse quand une UI défile ou glisse :

- **Les rectangles calent leurs bords.** Quand la transformation n'est ni tournée ni cisaillée, `drawRect`, `drawRoundedRect`, les masques et les images posent chaque bord sur le pixel de la fenêtre le plus proche : deux bords à la même position tombent toujours sur le même pixel, donc un enfant qui remplit son parent ne le laisse jamais transparaître. La ligne de base du texte se cale de la même façon. Les cercles, les lignes et les polygones gardent leur géométrie exacte, et les translations restent exactes.
- **Les rectangles fins sont des traits.** Sur un axe où un rectangle couvre moins de trois pixels — soulignement, séparateur, curseur, bordure — il garde un nombre entier de pixels centré sur sa position exacte, et un trait plus fin qu'un pixel est dessiné sur un pixel avec une opacité proportionnelle : il a la même épaisseur partout à l'écran, garde son poids aux petites tailles et ne disparaît jamais.
- **Le mouvement avance par pixels entiers.** Un nœud dont la position change d'une frame à l'autre — scroll, drag, animation, layout ou votre propre code — avance par pixels entiers de la fenêtre depuis l'endroit où il était au repos, avec ses enfants, et se redessine à sa position exacte dès qu'il s'arrête. Le décalage de scroll d'un nœud en overflow est lui-même arrondi à des pixels entiers, donc le contenu scrollé ne bouge même pas quand il s'arrête. Les translations d'une `Transformation` — transitions, `TransformNodeEffect` — sont arrondies de la même façon. Le contenu qui bouge garde sa position dans ses pixels, il ne scintille donc jamais et ne saute pas par rapport à ce qui l'entoure. Le layout et les tests de survol gardent les positions exactes.

Les dessins qui suivent la position de leur nœud bougent avec lui. Seul un dessin qui bouge par lui-même dans son nœud — ses coordonnées changent alors que le nœud reste immobile — arrondit lui-même son mouvement avec `IRenderBridge.quantize`, jusqu'au dépilement de la matrice :

```java
final IRenderBridge render = BridgeHandler.RENDER.get();
final double offset = this.slide.getValue() * 300D;
render.pushMatrix();
try {
    render.quantize(offset, 0D);
    DrawUtils.SHAPE.drawRect(getX() + offset, getY(), 40, 40, Color.WHITE);
} finally {
    render.popMatrix();
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