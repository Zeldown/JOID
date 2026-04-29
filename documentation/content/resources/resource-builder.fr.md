# ResourceBuilder

Le point d'entrée pour charger des ressources (images, vidéos, GIFs) dans JOID. Gère le cache, le chargement async et le routage vers le bon décodeur.

## Chargement rapide

Une seule méthode statique gère tous les types de source supportés :

```java
Resource res = Resource.of(myStream);              // InputStream
Resource res = Resource.of(myImage);               // BufferedImage
Resource res = Resource.of("https://...");         // String URL — télécharge en async
Resource res = Resource.of(textureId);             // Integer — enrobe un id de texture GL

Resource res = Resource.of(input, callback);       // n'importe lequel + notification quand prêt
```

L'input est routé vers le [resolver](resolvers.md) approprié selon son type runtime. Branchez votre propre resolver pour gérer des inputs custom (chemins de fichiers, assets bundlés, `ResourceLocation` MC, etc.) — voir [Resolvers](resolvers.md).

Toutes les formes passent par un `ResourceBuilder` par défaut (`.async().linear()`) qui écrit dans `ResourceBuilder.DEFAULT_CACHE` — un cache à TTL de 5 minutes partagé entre tous les chargements par défaut.

## Builder custom

Pour plus de contrôle, créez votre propre builder :

```java
final ResourceBuilder builder = ResourceBuilder.create()
    .async()                       // ou .blocking()
    .linear()                      // ou .nearest() — filtrage de texture
    .textureCoords(0, 0, 1, 1)     // mapping UV custom
    .cache(myCache);

Resource res = builder.of(myStream);
```

Les setters sont chaînables et retournent le builder.

## Cache

Par défaut, un `DEFAULT_CACHE` partagé expire les entrées après 5 minutes d'inactivité :

```java
ResourceBuilder.DEFAULT_CACHE     // cache global partagé
```

Désactiver ou fournir le vôtre :

```java
final ResourceBuilder builder = ResourceBuilder.create().cache(null);  // pas de cache
final ResourceBuilder builder = ResourceBuilder.create().cache(myCache);
```

## Chargement async

```java
Resource res = ResourceBuilder.create().async().of(stream);
```

Avec `async()`, le decode tourne sur un pool de threads en arrière-plan. Le nœud rend un placeholder skeleton en attendant.

`blocking()` force un decode synchrone — l'appel bloque jusqu'à ce que l'image soit sur le GPU. À réserver aux assets de démarrage.

## Chargement depuis une URL

```java
Resource res = Resource.of("https://example.com/image.png");
```

Le `UrlResourceResolver` par défaut télécharge l'URL sur un thread dédié (avec fallback automatique HTTPS → HTTP pour les hôtes mal configurés) et route les octets vers le bon décodeur. Le download ne se déclenche qu'au cache miss — des appels `of(sameUrl)` consécutifs réutilisent le `Resource` caché.

## Détection magic-bytes

Les resolvers `InputStream` et URL lisent les 12 premiers octets (ou l'extension de l'URL) pour choisir le bon décodeur :

| Signature / extension | Décodeur |
|---|---|
| `GIF87a` / `GIF89a` | `VideoResourceDecoder` (loop activé) |
| `ftyp` à l'offset 4 | `VideoResourceDecoder` (MP4/MOV) |
| `1A 45 DF A3` | `VideoResourceDecoder` (WebM/MKV) |
| `RIFF...AVI` | `VideoResourceDecoder` |
| autre | `ImageResourceDecoder` (ImageIO) |

Pas besoin de pré-classifier — déposez n'importe quel format supporté et ça marche.

## Construire la Resource depuis un resolver custom

Si vous écrivez un resolver, vous construisez le `Resource` caché via `compute(...)` :

```java
public class MySourceResolver implements IResourceResolver {

    @Override
    public boolean supports(final @NonNull Object input) {
        return input instanceof MySource;
    }

    @Override
    public @NonNull Resource resolve(final @NonNull ResourceBuilder builder, final @NonNull Object input, final Consumer<Resource> callback) {
        final MySource source = (MySource) input;
        final String uniqueId = source.getId();

        final Resource resource = builder.compute(uniqueId, () -> new ResourceData(uniqueId, ResourceDecoder.image(source.openStream())));

        if (callback != null) {
            callback.accept(resource);
        }
        return resource;
    }
}
```

Pour un load async (download / IO sur un worker thread), utilisez l'overload à 3 args — le bloc `onCreate` ne tourne qu'au cache miss, donc des appels `of(...)` concurrents réutilisent le même download in-flight :

```java
return builder.compute(url, () -> new ResourceData(url, null), resource -> new MyDownloadThread(url, stream -> {
    resource.decoder(ResourceDecoder.image(stream));
    if (callback != null) {
        callback.accept(resource);
    }
}).start());
```

## Copier le builder

```java
final ResourceBuilder base = ResourceBuilder.create().async().linear();
final ResourceBuilder nearestBuilder = base.copy().nearest();
```

`copy()` clone la config du builder pour des variations parallèles.

## Invalider le cache

```java
builder.reload();  // invalide toutes les entrées du cache du builder
```

Utile pour le hot-reload en dev.

## Bonnes pratiques

- **Gardez les ressources long-lived.** Charger une ressource à chaque frame tue le cache. Stockez `Resource` dans un champ.
- **Utilisez la forme URL pour les images user-fournies.** Elle gère download, cache et détection de format.
- **Préchargez à l'ouverture d'UI.** Fetch dans `init()` lance les decodes async pendant que l'UI apparaît.
- **Ne fermez pas les streams que vous passez à JOID.** `Resource.of(InputStream)` consomme le stream — n'appelez pas `close()` dessus.
- **Enregistrez vos resolvers une fois au démarrage.** `ResourceResolver.register(myResolver)` ajoute le resolver en tête de queue, donc les resolvers custom prennent priorité sur les défauts.

## Voir aussi

- [Resolvers](resolvers.md) — `IResourceResolver`, registry, et écrire des resolvers custom pour de nouveaux types d'input.
- [Decoders](decoders.md) — `ImageResourceDecoder`, `VideoResourceDecoder`.
- [ResourceNode](../nodes/design/resource.md) — rendre des ressources en tant que nœuds.