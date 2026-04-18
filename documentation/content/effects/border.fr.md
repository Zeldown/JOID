# BorderNodeEffect

Entoure un nœud d'une bordure colorée (ou en gradient). Respecte la forme du nœud — les coins suivent `RoundedNodeEffect`, `CircleNodeEffect`.

## Appliquer

```java
node.effect(BorderNodeEffect.create(Color.WHITE, 2F));
```

Bordure blanche 2px.

## Modes

Deux modes contrôlent si la bordure est **extérieure** (défaut) ou **intérieure** aux bounds du nœud :

```java
node.effect(BorderNodeEffect.create(Color.WHITE, 2F, BorderMode.OUT));  // défaut
node.effect(BorderNodeEffect.create(Color.WHITE, 2F, BorderMode.IN));
```

- **OUT** — la bordure s'étend vers l'extérieur du bord du nœud ; le nœud lui-même est inchangé.
- **IN** — la bordure mange sur le bord du nœud ; le contenu visible rétrécit de `width` sur tous les côtés.

## Bordures en gradient

La couleur de la bordure peut être un gradient — le renderer interpole le long d'un vecteur :

```java
node.effect(BorderNodeEffect.create(
    Color.BLUE.toGradient(Color.MAGENTA),
    3F
));
```

## Couleur/largeur réactives

```java
node.effect(BorderNodeEffect.create(
    () -> hovered ? Color.RED : Color.WHITE,
    () -> hovered ? 3F : 1F
));
```

## Exemple — contour animé

```java
final BooleanSignal active = new BooleanSignal(false);

RectNode.create(0, 0, 200, 100)
    .color(Color.decode("#1f2937"))
    .effect(RoundedNodeEffect.create(8F))
    .effect(BorderNodeEffect.create(
        () -> active.getOrDefault() ? Color.decode("#3b82f6") : Color.decode("#374151"),
        () -> active.getOrDefault() ? 2F : 1F
    ))
    .watch(active)
    .attach(parent);
```

## Fonctionnement

Produit un `BorderShaderPass` de priorité 200 (appliqué après tous les autres effets). Le shader échantillonne dans 24 directions × 3 distances pour détecter les bords, puis mélange la couleur de bordure.

## Bonnes pratiques

- **Mode IN pour les cartes/panels** où la bordure fait partie de la structure visuelle (pas extérieure).
- **Mode OUT pour les focus rings** — la bordure apparaît par-dessus sans perturber le layout.
- **Bordures en gradient avec remplissages en gradient** — les deux peuvent matcher ou contraster.

## Voir aussi

- [Effects Overview](overview.md).
- [Rounded Effect](rounded.md) — la bordure suit les coins arrondis.
- [Gradient Effect](gradient.md) — gradients de remplissage.