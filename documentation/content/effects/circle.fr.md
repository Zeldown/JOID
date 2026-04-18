# CircleNodeEffect

Masque le nœud en un cercle inscrit dans sa bounding box. `min(width, height)` sert de diamètre.

## Appliquer

```java
node.effect(CircleNodeEffect.create());
```

Aucun paramètre — l'effet utilise la taille actuelle du nœud au moment du rendu.

## Cas d'usage

- **Avatars.** `RectNode` avec une ressource + `CircleNodeEffect`.
- **Boutons.** Boutons icône circulaires avec `BorderNodeEffect` par-dessus.
- **Indicateurs.** Points d'état, bulles de notification.

## Exemple — avatar avec bordure

```java
RectNode.create(0, 0, 48, 48)
    .color(Color.decode("#3b82f6"))
    .effect(CircleNodeEffect.create())
    .effect(BorderNodeEffect.create(Color.WHITE, 2F))
    .attach(parent);
```

## Exemple — avatar image

```java
try {
    final Resource avatar = Resource.of(getClass().getResourceAsStream("/avatar.png"));

    RectNode.create(0, 0, 48, 48)
        .effect(CircleNodeEffect.create())
        .body(wrap -> {
            ResourceNode.create(0, 0, 48, 48)
                .resource(avatar)
                .stretch(StretchType.STRETCH)
                .attach(wrap);
        })
        .attach(parent);
} catch (IOException e) { e.printStackTrace(); }
```

L'effet cercle s'applique au conteneur, clippant l'image imbriquée.

## Fonctionnement

Produit un `CircleShaderPass` de priorité 100. Le shader calcule la distance au centre — les pixels hors du rayon sont transparents.

## Bonnes pratiques

- **Rendez le parent carré** pour un cercle parfait. Un rect `48×48` donne un cercle de 48px de diamètre ; `48×96` donne quand même 48px (min dim) mais gaspille de l'espace.
- **Superposez une bordure sur un cercle** pour un effet anneau.

## Voir aussi

- [RoundedNodeEffect](rounded.md) — coins doux plutôt que cercle complet.
- [CircleNode](../nodes/design/circle.md) — alternative plus simple quand vous voulez juste un cercle rempli.