# ResourceBuilder

Le point d'entrée pour charger des ressources (images, vidéos, GIFs) dans JOID. Gère le cache, le chargement async et le routage vers le bon décodeur.

## Chargement rapide

Une seule méthode statique gère tous les types de source supportés :

```java
Resource res = Resource.of(myStream);              // InputStream
Resource res = Resource.of(myImage);               // BufferedImage
Resource res = Resource.of("https://...");         // String URL — télécharge en async
Resource res = Resource.of(texture);               // ITexture — enrobe une texture créée par le bridge de rendu

Resource res = Resource.of(input, callback);       // n'importe lequel + notification quand prêt
```

Les entrées déjà décodées partent vers un [résolveur](resolvers.md) ; tout le reste devient un [asset](assets.md) et se décode depuis ses octets. Pour apprendre à JOID un handle à vous — une `ResourceLocation` MC, une clé de CDN, une entrée d'archive — enregistrez un localisateur d'asset : il servira d'un coup les textures, les vidéos et les polices.

Toutes les formes passent par un `ResourceBuilder` par défaut (`.async().linear()`) qui écrit dans `ResourceBuilder.DEFAULT_CACHE` — un cache à TTL de 5 minutes partagé entre tous les chargements par défaut.

## Builder custom

Pour plus de contrôle, créez votre propre builder :

```java
final ResourceBuilder builder = ResourceBuilder.create()
    .async()                       // ou .blocking()
    .linear()                      // ou .nearest() — filtrage de texture
    .mipmap(true)                  // ou false — mipmaps dès l'upload, ou jamais
    .textureCoords(0, 0, 1, 1)     // mapping UV custom
    .cache(myCache);

Resource res = builder.of(myStream);
```

Les setters sont chaînables et retournent le builder.

Sans `.mipmap(...)`, une ressource linéaire génère ses mipmaps la première fois qu'elle est dessinée plus petite que sa taille : une image réduite reste lisse au lieu de crénelée, et une image jamais réduite ne coûte aucune mémoire en plus. `.mipmap(true)` les génère dès l'upload, `.mipmap(false)` jamais — les atlas de fonts l'utilisent, car les mipmaps floutent les champs de distance. Les ressources avec `textureCoords` gardent le choix explicite, les niveaux d'un atlas mélangeant les sprites voisins.

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

Une `String` devient un `UrlAsset`, qui se déclare distant : son décodeur est choisi sur un thread dédié (avec fallback automatique HTTPS → HTTP pour les hôtes mal configurés), si bien que l'appel ne bloque jamais sur le réseau. Le download ne se déclenche qu'au cache miss — des appels `of(sameUrl)` consécutifs réutilisent le `Resource` caché.

## Détection magic-bytes

`ResourceDecoder.of(asset)` lit les 12 premiers octets via `peek(...)` pour choisir le bon décodeur :

| Signature / extension | Décodeur |
|---|---|
| `GIF87a` / `GIF89a` | `VideoResourceDecoder` (loop activé) |
| `ftyp` à l'offset 4 | `VideoResourceDecoder` (MP4/MOV) |
| `1A 45 DF A3` | `VideoResourceDecoder` (WebM/MKV) |
| `RIFF...AVI` | `VideoResourceDecoder` |
| autre | `ImageResourceDecoder` (ImageIO) |

Pas besoin de pré-classifier — déposez n'importe quel format supporté et ça marche.

## Construire la Resource depuis un resolver custom

Une entrée custom qui désigne des octets relève d'un [localisateur d'asset](assets.md), pas d'ici. Un résolveur sert aux entrées déjà décodées, et construit son `Resource` caché via `compute(...)` :

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