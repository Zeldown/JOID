# ResourceNode

Affiche une `Resource` — PNG, JPG, GIF, n'importe quel format produit par votre loader.

## Créer

```java
try {
    ResourceNode.create(0, 0)
        .resource(Resource.of(MyClass.class.getResourceAsStream("/icon.png")))
        .attach(parent);
} catch (IOException e) {
    e.printStackTrace();
}
```

Règles de dimensionnement :

- `create(x, y)` — auto-size aux dimensions naturelles de l'image.
- `create(x, y, w, h)` — taille forcée. Comportement selon `stretch()`.

## Sources

```java
node.resource(Resource resource);
node.resource(String url);                       // télécharge en async
node.resource(Resource main, Resource hovered);  // swap à deux états
node.resource(String url, String hoveredUrl);
```

`Resource.of(String url)` met un download HTTP en queue en arrière-plan. Le nœud rend un placeholder skeleton jusqu'à ce que chargé.

## Swap au hover

```java
ResourceNode.create(0, 0, 48, 48)
    .resource(
        Resource.of(getClass().getResourceAsStream("/icon-off.png")),
        Resource.of(getClass().getResourceAsStream("/icon-on.png"))
    )
    .attach(parent);
```

La ressource hover fait un fade-in automatique via `hoverValue(1F)`.

## Modes de stretch

```java
node.stretch(StretchType.STRETCH);  // remplit les bounds (défaut)
node.stretch(StretchType.CONTAIN);  // tient en préservant le ratio, centré
```

## Filtrage

```java
node.linear(true);   // smooth (défaut)
node.linear(false);  // nearest-neighbor (pixel art)
```

## Teinte de couleur

```java
node.color(Color.decode("#4a90e2"));              // tint
node.color(Color.WHITE, Color.decode("#ef4444")); // tint + tint de hover
```

## Dimensionnement partiel

Si seule `width` est définie, la hauteur est calculée depuis le ratio ; et inversement :

```java
ResourceNode.create(0, 0).resource(r).height(100).attach(parent);  // largeur auto
ResourceNode.create(0, 0).resource(r).width(200).attach(parent);   // hauteur auto
```

## Ressources animées

JOID auto-détecte les streams GIF / APNG / MP4 / WebM / MKV via magic bytes — vous obtenez un `VideoResourceDecoder` en coulisses. `ResourceNode` les lit automatiquement. Pour le contrôle (pause, seek, callbacks) utilisez plutôt [VideoPlayerNode](video-player.md).

## Bonnes pratiques

- **Chargez les ressources une seule fois.** Le `ResourceBuilder` cache par `uniqueId`, donc `Resource.of(stream)` avec le même stream retourne l'entrée en cache. Si vous rechargez depuis le disque, cachez la `Resource` vous-même.
- **Fermez les streams en `try-with-resources` seulement si vous n'utilisez pas `Resource.of(InputStream)`.** La ressource consomme le stream.
- **Préférez `StretchType.CONTAIN` pour les icônes** dans un slot fixe ; `STRETCH` fait cheap.

## Voir aussi

- [ResourceBuilder](../../resources/resource-builder.md)
- [Decoders](../../resources/decoders.md)
- [VideoPlayerNode](video-player.md)