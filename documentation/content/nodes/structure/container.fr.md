# ContainerNode

Un wrapper passif. Pas de visuel, pas de logique de layout — juste un parent pour des enfants avec sa propre position et taille. Pensez-y comme un `<div>` pour l'organisation et le clipping.

## Créer

```java
ContainerNode.create(x, y, width, height)
    .body(container -> {
        // enfants
    })
    .attach(parent);
```

## Quand l'utiliser

- Grouper des nœuds pour l'application d'un effet (un `RoundedNodeEffect` partagé).
- Isoler des régions de clipping (`overflow(OverflowProperty.HIDDEN)`).
- Fournir une origine de coordonnées pour des sous-layouts complexes.
- Un container racine à `0, 0, 1920, 1080` pour toute l'UI.

## Exemple — container racine

```java
@Override
public void init() {
    ContainerNode.create(0, 0, 1920, 1080)
        .body(root -> {
            FlexNode.vertical(0, 0, 200).body(nav -> { ... }).attach(root);
            ContainerNode.create(200, 0, 1720, 1080).body(main -> { ... }).attach(root);
        })
        .attach(this);
}
```

## Bonnes pratiques

- **N'abusez pas.** Si les seuls enfants d'un nœud sont un `RectNode`, passez-vous du container.
- **Utilisez les containers pour grouper des effets.** Un effet sur un container s'applique à tous ses enfants (via composition de framebuffer).
- **Nommez les containers en les extrayant.** Pas dans le code — dans votre modèle mental. « Ça c'est le container de la sidebar. » La clarté paye lors du `reload()`.

## Voir aussi

- [Node Fundamentals](../node-fundamentals.md) — overflow, draggable, effets.