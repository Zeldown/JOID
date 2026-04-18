# ResourceBuilder

Le point d'entrée pour charger des ressources (images, vidéos, GIFs) dans JOID. Gère le cache, le chargement async et la sélection du décodeur.

## Chargement rapide

Pour la plupart des cas, utilisez les helpers statiques `Resource.of(...)` :

```java
Resource image = Resource.of(InputStream stream);                      // throws IOException
Resource image = Resource.of(BufferedImage image);
Resource image = Resource.of(String url);                               // télécharge en async
Resource image = Resource.of(String url, Consumer<Resource> callback);  // async + callback
```

Ces quatre passent par un `ResourceBuilder` par défaut (`.async().linear()`) qui écrit dans `ResourceBuilder.DEFAULT_CACHE` — un cache à TTL de 5 minutes partagé entre tous les chargements par défaut.

Pour enrober une texture GL existante, utilisez la méthode d'instance : `ResourceBuilder.create().of(int id)` retourne un `Resource` sans décodeur et avec l'id de texture défini.

## Builder custom

Pour plus de contrôle, créez votre propre builder :

```java
final ResourceBuilder builder = ResourceBuilder.create()
    .async()                       // ou .blocking()
    .linear()                      // ou .nearest() — filtrage de texture
    .textureCoords(0, 0, 1, 1)     // mapping UV custom
    .cache(myCache);

Resource res = builder.of(stream);
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

## Détection magic-bytes

`of(InputStream)` lit les 12 premiers octets pour détecter le format :

| Signature | Décodeur |
|---|---|
| `GIF87a` / `GIF89a` | `VideoResourceDecoder` (loop activé) |
| `ftyp` à l'offset 4 | `VideoResourceDecoder` (MP4/MOV) |
| `1A 45 DF A3` | `VideoResourceDecoder` (WebM/MKV) |
| `RIFF...AVI` | `VideoResourceDecoder` |
| autre | `ImageResourceDecoder` (ImageIO) |

Pas besoin de pré-classifier — déposez n'importe quel format supporté et ça marche.

## Chargement depuis une URL

```java
Resource res = Resource.of("https://example.com/image.png");
```

Télécharge en tâche de fond via `ResourceDownloadThread`. Fallback automatique HTTPS → HTTP pour les hôtes mal configurés. Au download, la même détection magic-bytes choisit le décodeur.

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

## Voir aussi

- [Decoders](decoders.md) — `ImageResourceDecoder`, `VideoResourceDecoder`.
- [ResourceNode](../nodes/design/resource.md) — rendre des ressources en tant que nœuds.