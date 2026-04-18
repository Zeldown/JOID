# BlurNodeEffect

Flou gaussien appliqué au rendu d'un nœud. Deux passes séparables — horizontal puis vertical.

## Appliquer

```java
node.effect(BlurNodeEffect.create(8F));
```

Rayon `8F` = 8 pixels logiques de flou dans chaque direction.

## Rayon réactif

```java
node.effect(BlurNodeEffect.create(() -> isFocused ? 0F : 12F));
```

Ou muter à l'exécution :

```java
node.getEffect(BlurNodeEffect.class).radius(20F);
```

## Cas d'usage

- **Flou d'arrière-plan** derrière une popup.
- **Profondeur de champ** — flouter les zones non focalisées.
- **État de chargement** sur un panel.

## Exemple — flou quand non focalisé

```java
final BooleanSignal focused = new BooleanSignal(false);

RectNode.create(0, 0, 600, 400)
    .color(Color.decode("#1f2937"))
    .effect(RoundedNodeEffect.create(12F))
    .effect(BlurNodeEffect.create(() -> focused.getOrDefault() ? 0F : 6F))
    .watch(focused)
    .attach(parent);
```

## Fonctionnement

Produit deux `BlurShaderPass` — horizontal (priorité 150) + vertical (priorité 151). Chaque passe est un blit de framebuffer complet avec un kernel gaussien dynamique à 65 échantillons.

## Performance

Le flou est l'effet intégré le plus coûteux. Le coût dépend de :

- **Taille du nœud** — plus grand = plus de pixels à flouter.
- **Rayon** — ajuste dynamiquement le nombre d'échantillons (`step = max(radius / 32, 1)`). Des rayons très grands (>64) restent rapides mais perdent en précision.
- **Scale factor** — le pipeline JOID supersample selon `viewportWidth / uiWidth`. Sur un écran 4K avec UI 1920×1080, le coût pixel est 4×.

## Bonnes pratiques

- **Floutez la plus petite région possible.** Ne floutez pas toute l'UI ; floutez l'arrière-plan de la popup.
- **Rayon ≤ 16** pour un retour temps réel. Des rayons plus grands sont OK pour des scènes statiques.
- **Combinez avec `RoundedNodeEffect`** — le flou respecte les bounds arrondis grâce à la gestion de l'alpha prémultiplié.

## Voir aussi

- [Effects Overview](overview.md).
- [Shader Pipeline](../shaders/pipeline.md) — comment le multi-pass FBO fonctionne.