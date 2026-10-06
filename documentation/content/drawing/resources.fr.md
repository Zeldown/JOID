# Resources

`DrawResource` dessine à l'écran une `Resource` (image ou vidéo) déjà chargée. C'est le pendant bas niveau de `ResourceNode` — même setup de blend mode, mêmes coordonnées de texture, mais vous choisissez quand et où dessiner. Accessible via `DrawUtils.RESOURCE` ou directement via `DrawResource.getInstance()` — les deux retournent le même singleton. La ressource doit être déjà préparée (typiquement en la bindant via un nœud en amont, ou en déclenchant un `ResourceBuilder.of(...)`).

## Dimensionnement

### `drawResource` (taille naturelle)

```java
void drawResource(double x, double y, Resource resource)
```

Dessine la ressource à sa largeur et hauteur intrinsèques. Le coin haut-gauche est `(x, y)`.

```java
DrawUtils.RESOURCE.drawResource(40, 40, logo);
```

### `drawResource` (taille explicite)

```java
void drawResource(double x, double y, double width, double height, Resource resource)
```

Étire la ressource pour remplir le rectangle donné. Le ratio d'aspect n'est **pas** préservé : pour le garder, dessinez via un `ResourceNode` ou un `ResourcePlayerNode` avec `StretchType.CONTAIN` ou `StretchType.COVER`.

```java
DrawUtils.RESOURCE.drawResource(0, 0, 1920, 1080, background);
```

### `drawResource` (région)

```java
void drawResource(double x, double y, double width, double height, double u, double v, double regionWidth, double regionHeight, Resource resource)
```

Dessine seulement la région `(u, v, regionWidth, regionHeight)` de l'image, en pixels de l'image, étirée au rectangle. `StretchType.COVER` s'en sert pour rogner la partie centrée d'une ressource à son nœud.

```java
DrawUtils.RESOURCE.drawResource(0, 0, 200, 200, 100, 0, 200, 200, banner);
```

## Override de texture-coord

Si les `ResourceProperties` de la `Resource` ont des `textureCoords` custom (définis via `ResourceBuilder.textureCoords(u, v, width, height)`, en pixels de l'image), chaque appel `drawResource` les respecte — seul le sous-rectangle est échantillonné. `drawResource(x, y, resource)` le dessine à sa taille, et une taille explicite l'étire. Utile pour les sprite sheets.

```java
Resource sprite = ResourceBuilder.create()
    .textureCoords(0, 0, 64, 64)
    .of(spriteSheet);
```

## État de rendu

Chaque appel push la matrice, active `BlendState.NORMAL` (`SRC_ALPHA`, `ONE_MINUS_SRC_ALPHA`), bind la texture de la ressource avec `TextureWrap.CLAMP_TO_EDGE` — `CLAMP_TO_BORDER` quand la transformation est tournée ou cisaillée — dessine un quad texturé via le `Tessellator`, puis désactive le blending et pop la matrice. Vous n'avez pas à préconfigurer le blending.

Quand la transformation n'est ni tournée ni cisaillée, les coins du quad tombent sur des pixels entiers de la fenêtre, comme pour tout rectangle (voir [Alignement sur les pixels](draw-utils.md#alignement-sur-les-pixels)) : une image dessinée en `100.5` ou sous une échelle fractionnaire reste nette au lieu d'être rééchantillonnée entre deux pixels, et garde au moins un pixel. Le décodeur donne à chaque pixel transparent la couleur du pixel visible le plus proche, pour que le filtrage linéaire n'assombrisse jamais les bords d'une image transparente.

## Voir aussi

- `ResourceBuilder` — comment charger et configurer les ressources.
- `ResourceNode` — wrapper nœud avec intégration hover/click.
- `Decoders` — décodeurs d'images et de vidéos.