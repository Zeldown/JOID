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

Effets qui produisent une ou plusieurs `ShaderPass`, composées via le [Shader Pipeline](../shaders/pipeline.md) avec rendu multi-passe backed par des FBOs. Exemples : `BlurNodeEffect`, `BorderNodeEffect`, `CircleNodeEffect`, `RoundedNodeEffect`.

Identifier un effet shader :

```java
effect.isShaderEffect();   // true / false
```

## Scope

Chaque effet déclare s'il enveloppe le draw du nœud, celui de ses enfants, ou les deux, via `NodeEffectScope` :

```java
node.effect(BorderNodeEffect.create(Color.WHITE, 2F).scope(NodeEffectScope.SELF));
node.effect(CircleNodeEffect.create().scope(NodeEffectScope.CHILDREN));
```

- **`SELF`** (défaut) — l'effet enveloppe uniquement le draw du nœud. Les enfants se rendent normalement par-dessus. À utiliser pour des contours, remplissages, coins arrondis sur le nœud lui-même.
- **`CHILDREN`** — l'effet enveloppe le rendu des enfants à la place. Le draw du nœud reste intact, et l'effet masque/compose uniquement le sous-arbre. À utiliser pour appliquer un clip circulaire au contenu interne d'une carte sans toucher au fond.

`NodeEffectScope` est une enum interne à `NodeEffect`.

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
| `MaskNodeEffect` | Pre/Post | Masque stencil à une forme |
| `TransformNodeEffect` | Pre/Post | translate/scale/rotate GL |

> Vous cherchez un effet gradient ? Les gradients sont désormais natifs sur [`Color`](../drawing/color.md) — utilisez `Color.toGradient(autre)` directement sur `RectNode.color(...)`, `BorderNodeEffect.create(...)`, ou n'importe quel `TextInfo`. Le `GradientNodeEffect` dédié a été retiré en 6.2.0.

## Bonnes pratiques

- **Empilez formes + shaders librement.** Un `RectNode` avec rounded + border + blur fonctionne tel quel.
- **Appliquez les effets sur le nœud le plus interne possible.** Un flou sur un panel entier est cher ; sur un label, bon marché.
- **Mesurez avant d'optimiser.** Un blur multi-passe coûte ~1-2ms en 4K sur GPU mid-tier. Ne pré-optimisez pas.

## Voir aussi

- [Shader Pipeline](../shaders/pipeline.md) — comment les effets se composent en passes GL.
- [Custom Shaders](../shaders/custom.md) — écrire votre propre effet.