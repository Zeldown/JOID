# ModelNode

Rend n'importe quel `IDrawableModel` dans l'UI. Le nœud auto-scale le modèle pour que sa largeur rentre dans la largeur du nœud, puis applique un scale utilisateur optionnel ainsi qu'un yaw et un pitch autour du centre du nœud.

## Créer

```java
ModelNode.create(x, y, width, height)
```

## API

```java
T model(IDrawableModel model)
T size(double size)                     // multiplicateur de scale additionnel (défaut 1.0)
T rotationYaw(double degrees)           // rotation autour de l'axe Y
T rotationPitch(double degrees)         // rotation autour de l'axe X
T pipeLineLevel(double level)           // hint de profondeur de rendu (défaut -1 = auto depuis la diagonale du modèle)
```

Il n'y a **pas** de `rotation(x, y, z)`, pas de `rotationSpeed`, pas de `translate`, pas de callback. Le nœud ne dessine rien si `model` est `null` ou si `size == 0`. Les implémentations de `IDrawableModel` vivent hors de JOID — vous fournissez le loader.

## `ModelViewerNode`

Sous-classe viewer interactive. Même API que `ModelNode`, plus :

```java
ModelViewerNode.create(x, y, width, height)

T rotationYawRange(double min, double max)
T rotationPitchRange(double min, double max)
T sizeRange(double min, double max)            // défauts [0.1, 2.0]
T zoom(double zoom)                            // clamp à la size range
```

Press + drag souris tourne le modèle (yaw depuis le mouvement X, pitch depuis le mouvement Y). La molette zoome via `value / 3000` par tick. `size`, `rotationYaw`, `rotationPitch` sont animés vers leurs valeurs cibles via `UI.lerpByFramerate`.

## Exemple — preview interactive

```java
ModelViewerNode.create(0, 0, 400, 400)
    .model(myModel)
    .size(1.2D)
    .rotationYawRange(-45D, 45D)
    .rotationPitchRange(-30D, 30D)
    .sizeRange(0.5D, 3D)
    .attach(parent);
```

## Voir aussi

- `DrawUtils.MODEL` — la couche de dessin qu'utilise ce nœud.