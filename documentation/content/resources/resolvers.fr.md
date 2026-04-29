# Resolvers

Les resolvers transforment un `Object` générique en `Resource`. Le dispatch est piloté par le type : chaque resolver répond à `supports(Object)`, et le premier match dans le registre traite l'appel.

C'est le point d'extension pour brancher de nouvelles sources de ressources dans JOID — assets bundlés, chemins de fichiers, IDs custom, `ResourceLocation` MC, n'importe quoi qu'on peut mapper vers des octets.

## `IResourceResolver`

Le contrat :

```java
public interface IResourceResolver {

    public boolean supports(final @NonNull Object input);

    public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback);

}
```

- **`supports(input)`** — retourne `true` quand ce resolver peut gérer l'input. Typiquement un simple `instanceof`.
- **`resolve(builder, input, callback)`** — produit une `Resource` cachée via `builder.compute(...)`. Si `callback` est non-null, le déclencher quand la resource est prête (ou immédiatement pour les loads sync).

Un resolver n'instancie jamais `Resource` directement — il retourne un `Supplier<ResourceData>` à `builder.compute(...)`, qui s'occupe du caching et de la construction.

## Resolvers intégrés

JOID embarque quatre resolvers, enregistrés automatiquement :

| Resolver | Type d'input | Comportement |
|---|---|---|
| `InputStreamResourceResolver` | `InputStream` | Détection magic-bytes → image ou vidéo. Sync. |
| `BufferedImageResourceResolver` | `BufferedImage` | Enrobe l'image dans `ImageResourceDecoder`. Sync. |
| `UrlResourceResolver` | `String` | Télécharge sur un thread (avec fallback HTTPS → HTTP). Async. |
| `TextureIdResourceResolver` | `Integer` | Enrobe un id de texture GL existant sans décodeur. Sync. |

Les builtins sont enregistrés dans le static initializer de `ResourceResolver` et vivent en bas du registre — vos resolvers custom prennent toujours priorité.

## Enregistrer un resolver custom

Appeler une fois au démarrage de l'app, avant tout `Resource.of(...)` :

```java
ResourceResolver.register(new MyCustomResolver());
```

`register(...)` ajoute le resolver en **tête** de queue — les enregistrements récents battent les anciens pour les inputs que les deux peuvent matcher.

## Écrire un resolver

Exemple synchrone — enrober un POJO `Asset` custom :

```java
public class AssetResolver implements IResourceResolver {

    @Override
    public boolean supports(final @NonNull Object input) {
        return input instanceof Asset;
    }

    @Override
    public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback) {
        final Asset asset = (Asset) input;
        final String uniqueId = "asset:" + asset.getId();

        final Resource resource = builder.compute(uniqueId, () -> new ResourceData(uniqueId, ResourceDecoder.image(asset.openStream())));

        if (callback != null) {
            callback.accept(resource);
        }
        return resource;
    }

}
```

Exemple asynchrone — fetch sur un worker thread, avec le bloc `onCreate` qui ne se déclenche qu'au cache miss :

```java
public class CdnResolver implements IResourceResolver {

    @Override
    public boolean supports(final @NonNull Object input) {
        return input instanceof CdnRequest;
    }

    @Override
    public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback) {
        final CdnRequest request = (CdnRequest) input;
        final String uniqueId = request.getCacheKey();

        return builder.compute(uniqueId, () -> new ResourceData(uniqueId, null), resource -> new CdnDownloadThread(request, stream -> {
            resource.decoder(ResourceDecoder.image(stream));
            if (callback != null) {
                callback.accept(resource);
            }
        }).start());
    }

}
```

L'overload `compute(uniqueId, supplier, onCreate)` à 3 args ne tourne `onCreate` que quand aucune entrée de cache n'existe pour `uniqueId` — des appels `Resource.of(sameRequest)` concurrents partagent le download in-flight au lieu de lancer des parallèles.

## Ordre de résolution

`ResourceResolver.resolve(builder, input, callback)` parcourt la liste et retourne le premier match. Si rien ne match, il jette une `IllegalArgumentException("No resolver found for input of type ...")`.

Comme `register(...)` insère en tête, le registre est parcouru du plus récent au plus ancien. Ordre par défaut au runtime (haut → bas) :

1. Resolvers custom enregistrés après le démarrage (le plus récent gagne).
2. Resolvers intégrés (dans l'ordre de démarrage de JOID).

Si vous devez override un builtin (par exemple, gérer `String` différemment), enregistrez juste le vôtre — il sera vérifié avant `UrlResourceResolver`.

## Bonnes pratiques

- **Un resolver par type d'input.** N'essayez pas de faire un seul resolver pour `String` et `Path` ; séparez-les. Les checks `supports(...)` sont plus propres, et la précédence plus simple à raisonner.
- **Choisissez un `uniqueId` stable.** C'est la clé du cache. Deux inputs qui doivent résoudre vers la même texture doivent produire le même id ; deux sources distinctes ne doivent pas entrer en collision.
- **Utilisez `compute(uniqueId, supplier)` pour sync, `compute(uniqueId, supplier, onCreate)` pour async.** N'essayez pas de lancer un fetch async dans le supplier — le supplier doit être cheap et produire seulement le `ResourceData` placeholder.
- **Enregistrez au démarrage.** Les resolvers ajoutés après les premiers appels `Resource.of(...)` ne serviront pas pour les inputs déjà cachés.

## Voir aussi

- [ResourceBuilder](resource-builder.md) — caching, `compute(...)`, configuration du builder.
- [Decoders](decoders.md) — ce que `ResourceData.decoder` fait une fois que le resolver a fini.