# Decoders

Les objets bas niveau qui transforment des octets en textures GPU. Vous les instanciez rarement directement — `ResourceFormat.decoder(asset)` en choisit un d'après les premiers octets d'un [asset](assets.md) — mais connaître l'API aide pour écrire des décodeurs custom.

Un décodeur porte son asset, pas ses octets : rien n'est lu avant que `decode(...)` tourne, sur le worker de ressources.

## `IResourceDecoder`

Le contrat :

```java
public interface IResourceDecoder {
    default public void init(final @NonNull ResourceData resource) {}
    default public void prepare(final @NonNull ResourceData resource) {}
    default public void decode(final @NonNull ResourceData resource) {}
    default public void upload(final @NonNull ResourceData resource) {}
    default public void update(final @NonNull ResourceData resource) {}
    default public void clear(final @NonNull ResourceData resource) {}
}
```

Cycle de vie :

1. **`init`** — juste après construction, avec le `ResourceData` parent attaché.
2. **`prepare`** — appelée sur le thread de rendu avant decode. Créez ici les textures placeholder avec `BridgeHandler.RENDER.get().createTexture()`.
3. **`decode`** — décode les octets en pixels. Peut tourner sur un thread de fond (mode async).
4. **`upload`** — thread de rendu. Upload les pixels décodés via `ITexture.allocate` et `upload`.
5. **`update`** — à chaque frame avant le rendu de la ressource. Remplacez la texture courante avec `resource.texture(...)` quand elle change (frames vidéo).
6. **`clear`** — libère les ressources GPU quand le cache évince ou le nœud est détruit.

## Décodeurs intégrés

### `RasterResourceDecoder`

Formats d'image statique (PNG, JPG, BMP). Utilise le `ImageIO` de Java. Texture unique, pas d'animation.

```java
new RasterResourceDecoder(Asset);            // lu paresseusement, dans decode()
new RasterResourceDecoder(BufferedImage);    // depuis une image pré-décodée
```

### `VideoResourceDecoder`

Formats vidéo — MP4, MOV, WebM, MKV, AVI. Backed par FFmpeg via JavaCV.

Fonctionnalités :
- Queue de frames en ring buffer (5 frames).
- Textures ping-pong pour une lecture sans tearing.
- Streaming audio via le bridge audio, synchronisé à la vidéo.
- Seek, pause, resume, loop.
- Spatial audio 3D optionnel.

```java
new VideoResourceDecoder(Asset);
new VideoResourceDecoder(File);
```

Pour le contrôle de la lecture, enveloppez dans un [VideoPlayerNode](../nodes/design/video-player.md) — il expose `play / pause / seek / volume` et des callbacks de progression.

Pilotez la lecture de toute ressource animée via `Resource.getPlayback()` :

```java
resource.getPlayback().ifPresent(playback -> playback.seek(10D).play());
double progress = resource.getPlayback().map(IPlayback::getProgress).orElse(0D);
```

## Détection du format

`ResourceFormat.decoder(asset)` lit les 512 premiers octets de l'asset via `peek(...)`, sans le consommer, et demande à chaque `IResourceFormat` enregistré s'il les reconnaît, le dernier enregistré en premier. Si aucun ne les reconnaît, l'asset est lu comme une image raster.

## Écrire un décodeur custom

Implémentez `IResourceDecoder`, puis dirigez-y les octets avec un `IResourceFormat` enregistré via `ResourceFormat.register(...)`, ou construisez directement un `ResourceData` :

```java
public class SVGResourceDecoder implements IResourceDecoder {

    private final InputStream stream;
    private int[] pixels;
    private int width, height;

    public SVGResourceDecoder(final InputStream stream) {
        this.stream = stream;
    }

    @Override
    public void prepare(ResourceData resource) {
        resource.texture(BridgeHandler.RENDER.get().createTexture().allocate(1, 1).upload(new int[] {0}, 1, 1));
    }

    @Override
    public void decode(ResourceData resource) {
        final BufferedImage img = rasterize(stream);
        this.width = img.getWidth();
        this.height = img.getHeight();
        this.pixels = extractPixels(img);
        resource.width(width).height(height);
    }

    @Override
    public void upload(ResourceData resource) {
        resource.getTextures()[0].allocate(width, height).upload(pixels, width, height);
    }

    @Override
    public void clear(ResourceData resource) {
        this.pixels = null;
    }
}
```

Enveloppez dans un helper :

```java
public static IResourceDecoder svg(InputStream s) {
    return new SVGResourceDecoder(s);
}
```

Utilisez directement :

```java
new Resource(builder, new ResourceData(uniqueId, svg(stream)));
```

## Bonnes pratiques

- **Créez les textures via le bridge de rendu.** `createTexture()`, `allocate` et `upload` fonctionnent sur tous les backends.
- **Ne gardez pas l'InputStream indéfiniment.** Consommez-le pendant `decode` et lâchez la référence.
- **Gardez les appels de texture dans `prepare`, `upload` et `update`.** Ils tournent sur le thread de rendu ; `decode` peut tourner sur n'importe quel thread.

## Voir aussi

- [ResourceBuilder](resource-builder.md).
- [Resolvers](resolvers.md) — la couche de dispatch qui choisit le décodeur à utiliser.
- [ResourceNode](../nodes/design/resource.md).
- [VideoPlayerNode](../nodes/design/video-player.md).