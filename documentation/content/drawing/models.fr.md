# Models

`DrawModel` rend tout objet implémentant `IDrawableModel` dans le contexte GL courant. Il gère le setup de la lumière, la désactivation du back-face, et une rotation 180° sur Y pour que les modèles OBJ standards fassent face à la caméra.

```java
DrawUtils.MODEL.method(...);
// ou
DrawModel.getInstance().method(...);
```

## `drawModel` (scale uniforme)

```java
void drawModel(double x, double y, double size, IDrawableModel model)
```

Translate en `(x, y, 0)` et applique `(size, size, size)` en scale.

```java
DrawUtils.MODEL.drawModel(100, 100, 40D, crate);
```

## `drawModel` (scale par axe)

```java
void drawModel(double x, double y, double sizeX, double sizeY, double sizeZ, IDrawableModel model)
```

Comme ci-dessus mais chaque axe scale indépendamment. Utile pour des modèles étirés ou compressés.

```java
DrawUtils.MODEL.drawModel(100, 100, 40D, 60D, 40D, crate);
```

## `IDrawableModel`

N'importe quelle bibliothèque de chargement de modèle peut fournir une implémentation compatible JOID :

```java
public interface IDrawableModel {
    void render();
    double getWidth();
    double getHeight();
    double getDepth();
}
```

- `render()` — appelée dans `drawModel`, émet les appels GL de sommets/indices.
- `getWidth / getHeight / getDepth` — dimensions de la bounding box, utile pour layout ou centrage.

JOID n'embarque pas de loader de modèles — `ModelNode` consomme n'importe quel `IDrawableModel` que vous lui passez. Un pairing commun est `net.obj` pour parser OBJ, mais tout ce qui produit un `IDrawableModel` fonctionne.

## État GL dans `drawModel`

Chaque appel configure :

- `GL_CULL_FACE` désactivé (les deux côtés de chaque triangle sont visibles).
- `GL_LIGHTING`, `GL_LIGHT0`, `GL_LIGHT1` activés.
- `GL_COLOR_MATERIAL` activé avec `GL_FRONT_AND_BACK` / `GL_AMBIENT_AND_DIFFUSE`.
- `GL_SHADE_MODEL` sur `GL_FLAT`.
- Lumière ambiante : `(0.6, 0.6, 0.6, 1.0)`.

Tout ça est restauré après le retour de `render()`.

## Voir aussi

- `ModelNode` — wrapper nœud avec callbacks hover et placement.
- `DrawUtils` — point d'entrée pour les quatre façades.