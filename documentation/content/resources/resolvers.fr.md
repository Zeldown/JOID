# Résolveurs

Un résolveur transforme une entrée **déjà décodée** en `Resource` — une `BufferedImage` construite en mémoire, un `ITexture` que le bridge de rendu possède déjà. Il n'y a aucun octet à lire, donc ces entrées n'atteignent jamais la couche [asset](assets.md).

Tout ce qui *est* des octets passe par un [localisateur d'asset](assets.md). C'est presque toujours le point d'extension que vous cherchez.

## Lequel me faut-il ?

| Votre entrée | Point d'extension |
|---|---|
| Un handle qui désigne un fichier — `ResourceLocation`, clé de CDN, entrée d'archive | [`IAssetLocator`](assets.md) |
| Un objet qui porte déjà des pixels ou une texture | `IResourceResolver` |
| Un format que JOID ne sait pas décoder — SVG, KTX | [`IResourceDecoder`](decoders.md) derrière un localisateur |

Le localisateur est la réponse la moins chère : il est réutilisé par les polices et par tout futur consommateur d'octets, là où un résolveur ne produit jamais qu'une `Resource`.

## `IResourceResolver`

```java
public interface IResourceResolver {

    public boolean supports(final @NonNull Object input);

    public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback);

}
```

- **`supports(input)`** — `true` quand ce résolveur prend l'entrée en charge. En général un seul `instanceof`.
- **`resolve(builder, input, callback)`** — produire une `Resource` cachée via `builder.compute(...)`, jamais en instanciant `Resource` directement. Déclencher `callback` quand la ressource est prête.

## Résolveurs fournis

| Résolveur | Entrée | Comportement |
|---|---|---|
| `BufferedImageResourceResolver` | `BufferedImage` | Enveloppe l'image dans un `RasterResourceDecoder`. |
| `TextureResourceResolver` | `ITexture` | Enveloppe une texture du bridge de rendu, sans décodeur. |

## Ordre de résolution

`ResourceBuilder.of(input, callback)` interroge d'abord les résolveurs, puis retombe sur la couche asset :

```
Resource.of(handle)
├─ un résolveur le prend ───────────► IResourceResolver
└─ sinon ─► Asset.of(handle) ─► ResourceFormat.decoder(asset)
```

Enregistrer un résolveur permet donc de confisquer complètement un type d'entrée, y compris un type que la couche asset revendiquerait. Si rien ne correspond et qu'aucun localisateur ne reconnaît le handle, `Asset.of(...)` lève `IllegalArgumentException`.

## Enregistrement

```java
ResourceResolver.register(new MyResolver());
```

`register(...)` insère en tête : le dernier enregistré gagne. Enregistrez au démarrage — un résolveur ajouté après le premier `Resource.of(...)` n'affectera pas les handles déjà mis en cache.

## Écrire un résolveur

```java
public class PixelsResolver implements IResourceResolver {

    @Override
    public boolean supports(final @NonNull Object input) {
        return input instanceof Pixels;
    }

    @Override
    public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback) {
        final Pixels pixels = (Pixels) input;
        final String uniqueId = "pixels:" + pixels.getId();

        final Resource resource = builder.compute(uniqueId, () -> new ResourceData(uniqueId, new RasterResourceDecoder(pixels.toBufferedImage())));
        if (callback != null) {
            callback.accept(resource);
        }
        return resource;
    }

}
```

La surcharge à trois arguments `compute(uniqueId, supplier, onCreate)` n'exécute `onCreate` qu'en cas de cache manquant : des `Resource.of(mêmeEntrée)` concurrents partagent un seul chargement en vol au lieu d'en lancer plusieurs.

## Bonnes pratiques

- **Préférez un localisateur.** Un résolveur qui ouvre un flux refait à la main le travail de la couche asset, et ne sert que les textures.
- **Un type d'entrée par résolveur.** Séparez `String` et `Path` plutôt que de brancher dans `supports(...)`.
- **Choisissez un `uniqueId` stable.** C'est la clé du cache.
- **Enregistrez au démarrage**, avant le premier chargement.

## Voir aussi

- [Assets](assets.md) — la couche octets et ses localisateurs.
- [ResourceBuilder](resource-builder.md) — cache et `compute(...)`.
- [Décodeurs](decoders.md) — ce qui tourne une fois le résolveur passé.