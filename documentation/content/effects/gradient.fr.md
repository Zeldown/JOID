# GradientNodeEffect

Applique un gradient linéaire en post-processing sur un nœud — fonctionne sur n'importe quel contenu, y compris texte et images.

## Appliquer

```java
node.effect(GradientNodeEffect.create(Color.RED, Color.BLUE));
```

La direction par défaut est horizontale (gauche → droite).

## Direction custom

La direction est un `Vector4f(startX, startY, endX, endY)` dans l'espace de coordonnées normalisé du nœud (0.0 → 1.0) :

```java
// horizontal (défaut)
GradientNodeEffect.create(Color.RED, Color.BLUE, new Vector4f(0F, 0F, 1F, 0F));

// vertical
GradientNodeEffect.create(Color.CYAN, Color.MAGENTA, new Vector4f(0F, 0F, 0F, 1F));

// diagonal
GradientNodeEffect.create(Color.YELLOW, Color.RED, new Vector4f(0F, 0F, 1F, 1F));
```

## Couleurs réactives

```java
node.effect(GradientNodeEffect.create(
    () -> startColorSignal.get(),
    () -> endColorSignal.get()
));
```

## Cas d'usage

- **Texte en gradient** sans second shader.
- **Progress bars multi-couleurs** avec remplissages en gradient.
- **Panels thématiques** — décalages subtils de couleur sur des cartes.

## Exemple — texte en gradient

```java
TextNode.create(0, 0)
    .text(Text.create("GRADIENT", TextInfo.create(myFont, 48, Color.WHITE)))
    .effect(GradientNodeEffect.create(Color.decode("#3b82f6"), Color.decode("#a855f7")))
    .attach(parent);
```

En coulisses, `GradientShaderPass` force la voie FBO pour que le texte soit rastérisé en premier, puis le gradient appliqué en overlay coloré de texture.

## Fonctionnement

Produit un `GradientShaderPass` de priorité 0 (appliqué en premier, avant les passes de forme/bordure). Le shader :

1. Calcule la position normalisée dans le canvas du nœud.
2. La projette sur le vecteur de direction pour obtenir `t ∈ [0, 1]`.
3. Mélange `startColor` et `endColor` par `t`.
4. Multiplie avec le pixel existant (`texColor.rgb * gradientColor.rgb`) s'il vient d'une texture FBO.

## `Color.toGradient()` vs `GradientNodeEffect`

Les deux créent des gradients, mais différemment :

| Approche | Quand l'utiliser |
|---|---|
| `Color.toGradient(otherColor)` | Sur le `color()` d'un `RectNode` — le renderer choisit `GradientShaderPass` automatiquement. |
| `GradientNodeEffect.create(...)` | Sur n'importe quel nœud (texte, image, custom) — applique le gradient comme coloration **post-process**. |

Pour un `RectNode`, les deux donnent le même résultat visuel. Pour d'autres nœuds, seul `GradientNodeEffect` fonctionne.

## Bonnes pratiques

- **Utilisez des gradients à 2 couleurs.** Plus de 2 n'est pas supporté directement — chaînez plusieurs effets ou empilez des nœuds.
- **Choisissez des couleurs de luminosité similaire** sauf si vous voulez une bande visible au milieu.
- **Combinez avec `BorderNodeEffect`** pour un double gradient (remplissage + contour).

## Voir aussi

- [Effects Overview](overview.md).
- [Color](../drawing/color.md) — API `toGradient()`.