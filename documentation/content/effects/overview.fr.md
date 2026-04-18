# Effects Overview

Les effets modifient la façon dont un nœud se rend. Ils se répartissent en deux familles, se composent par ordre de priorité, et se chaînent sans limite.

## Appliquer des effets

```java
node.effect(RoundedNodeEffect.create(12F));
node.effect(BorderNodeEffect.create(Color.WHITE, 2F));
node.effect(BlurNodeEffect.create(4F));
```

Les effets sont stockés dans une map indexée par classe — ajouter un deuxième `RoundedNodeEffect` remplace le premier.

Retirer un effet :

```java
node.clearEffect(RoundedNodeEffect.class);
```

Récupérer un effet (par exemple pour lire son état) :

```java
RoundedNodeEffect<?> rounded = node.getEffect(RoundedNodeEffect.class);
```

## Deux familles

### Effets Pre / Post

Effets qui encadrent le rendu du nœud avec `pre(node)` / `post(node)` — typiquement des changements d'état GL (stencil, scissor, transform). Exemples : `MaskNodeEffect`, `TransformNodeEffect`.

### Effets shader

Effets qui produisent une ou plusieurs `ShaderPass`, composées via le [Shader Pipeline](../shaders/pipeline.md) avec rendu multi-passe backed par des FBOs. Exemples : `BlurNodeEffect`, `BorderNodeEffect`, `GradientNodeEffect`, `CircleNodeEffect`, `RoundedNodeEffect`.

Identifier un effet shader :

```java
effect.isShaderEffect();   // true / false
```

## Priorité

Chaque effet a un setter `priority(int)` ; les priorités plus élevées s'appliquent plus tard. Les passes shader ont aussi des priorités internes — voir [Shader Pipeline](../shaders/pipeline.md).

```java
myEffect.priority(100);
```

## Effets intégrés

| Effet | Famille | Usage |
|---|---|---|
| [RoundedNodeEffect](rounded.md) | Shader | Coins arrondis |
| [CircleNodeEffect](circle.md) | Shader | Masque circulaire |
| [BlurNodeEffect](blur.md) | Shader | Flou gaussien |
| [BorderNodeEffect](border.md) | Shader | Contour avec support gradient |
| [GradientNodeEffect](gradient.md) | Shader | Remplissage gradient linéaire |
| `MaskNodeEffect` | Pre/Post | Masque stencil à une forme |
| `TransformNodeEffect` | Pre/Post | translate/scale/rotate GL |

## Bonnes pratiques

- **Empilez formes + shaders librement.** Un `RectNode` avec rounded + border + blur fonctionne tel quel.
- **Appliquez les effets sur le nœud le plus interne possible.** Un flou sur un panel entier est cher ; sur un label, bon marché.
- **Mesurez avant d'optimiser.** Un blur multi-passe coûte ~1-2ms en 4K sur GPU mid-tier. Ne pré-optimisez pas.

## Voir aussi

- [Shader Pipeline](../shaders/pipeline.md) — comment les effets se composent en passes GL.
- [Custom Shaders](../shaders/custom.md) — écrire votre propre effet.