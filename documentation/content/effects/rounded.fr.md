# RoundedNodeEffect

Arrondit les coins d'un nœud. Contrôle par coin — vous pouvez n'arrondir que top-left et top-right pour des onglets ou formes pill.

## Appliquer

```java
node.effect(RoundedNodeEffect.create(16F));
```

Tous les coins à 16px de rayon.

## Par coin

```java
node.effect(RoundedNodeEffect.create(16F, true, true, false, false));
```

Le constructeur est `(radius, left, top, right, bottom)` — chaque booléen active l'arrondi sur ce côté. Deux booléens à `true` arrondissent le coin où les deux côtés se rencontrent :

- `left=true, top=true` → coin top-left arrondi
- `left=true, bottom=true` → bottom-left
- `right=true, top=true` → top-right
- `right=true, bottom=true` → bottom-right

Donc `(16F, true, true, false, false)` arrondit uniquement le coin **top-left**. Pour des onglets avec le haut arrondi :

```java
RoundedNodeEffect.create(16F, true, true, true, false);  // arrondit top-left ET top-right
```

## Rayon réactif

```java
node.effect(RoundedNodeEffect.create(() -> isSelected ? 20F : 8F));
```

## Modifier après création

```java
RoundedNodeEffect<?> rounded = node.getEffect(RoundedNodeEffect.class);
rounded.radius(24F);
rounded.left(true);
```

## Fonctionnement

`RoundedNodeEffect` produit un `RoundedShaderPass` de priorité 100. La passe utilise `RoundedShader` pour échantillonner un signed distance field MSDF, donnant des coins nets à n'importe quelle échelle.

## Bonnes pratiques

- **Radius ≤ min(width, height) / 2.** Des valeurs plus grandes clippent en forme pill/cercle.
- **Arrondissez par multiples de 4.** Cohérence visuelle entre composants.
- **Combinez avec `BorderNodeEffect`.** La bordure suit la forme arrondie automatiquement.

## Voir aussi

- [Effects Overview](overview.md).
- [Shader Pipeline](../shaders/pipeline.md).
- [CircleNodeEffect](circle.md) — forme cercle complet.