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

Étire la ressource pour remplir le rectangle donné. Le ratio d'aspect n'est **pas** préservé.

```java
DrawUtils.RESOURCE.drawResource(0, 0, 1920, 1080, background);
```

## Préservation du ratio

### `drawScaledResourceWidth`

```java
void drawScaledResourceWidth(double x, double y, double width, Resource resource)
```

Calcule `height = width × (resource.height / resource.width)` — ratio préservé, seule la largeur est spécifiée.

```java
DrawUtils.RESOURCE.drawScaledResourceWidth(20, 20, 400, photo);
```

### `drawScaledResourceHeight`

```java
void drawScaledResourceHeight(double x, double y, double height, Resource resource)
```

Symétrique : `width = height × (resource.width / resource.height)`.

```java
DrawUtils.RESOURCE.drawScaledResourceHeight(20, 20, 200, photo);
```

### `drawCenteredResource`

```java
void drawCenteredResource(double x, double y, double width, double height, Resource resource)
```

Scale la ressource pour tenir dans `(x, y, width, height)` en préservant le ratio, et centre le résultat. Équivalent au CSS `object-fit: contain`.

```java
DrawUtils.RESOURCE.drawCenteredResource(0, 0, 400, 400, thumbnail);
```

## Override de texture-coord

Si les `ResourceProperties` de la `Resource` ont des `textureCoords` custom (définis via `ResourceBuilder.textureCoords(u, v, w, h)`), chaque appel `drawResource` les respecte — seul le sous-rectangle est échantillonné. Utile pour les sprite sheets.

```java
Resource sprite = ResourceBuilder.create()
    .textureCoords(0, 0, 64, 64)
    .of(spriteSheet);
```

## État GL

Chaque appel push la matrice, active `GL_BLEND` avec `(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)` et `GL_POINT_SMOOTH`, bind la texture de la ressource avec `GL_CLAMP` sur les deux axes, dessine un quad texturé et restaure l'état. Vous n'avez pas à préconfigurer le blending.

## Voir aussi

- `ResourceBuilder` — comment charger et configurer les ressources.
- `ResourceNode` — wrapper nœud avec intégration hover/click.
- `Decoders` — décodeurs d'images et de vidéos.