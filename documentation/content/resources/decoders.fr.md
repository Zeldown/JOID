# Decoders

Les objets bas niveau qui transforment des octets en textures GPU. Vous les instanciez rarement directement — `ResourceFormat.decoder(asset)` en choisit un d'après les premiers octets d'un [asset](assets.md), voir [Formats](formats.md) — mais connaître l'API aide pour écrire des décodeurs custom.

Un décodeur porte son asset, pas ses octets : rien n'est lu avant que `decode(...)` tourne, sur le worker de ressources.

## `IResourceDecoder`

Le contrat :

```java
public interface IResourceDecoder {
    default public void init(final @NonNull ResourceData resource) {}
    default public void prepare(final @NonNull ResourceData resource) {}
    default public void decode(final @NonNull ResourceData resource) {}
    default public void upload(final @NonNull ResourceData resource) {}
    default public void request(final @NonNull ResourceData resource, final int width, final int height, final boolean async) {}
    default public void update(final @NonNull ResourceData resource) {}
    default public void clear(final @NonNull ResourceData resource) {}
    default public boolean isSettled() { return true; }
}
```

Cycle de vie :

1. **`init`** — juste après construction, avec le `ResourceData` parent attaché.
2. **`prepare`** — appelée sur le thread de rendu avant decode. Créez ici les textures placeholder avec `BridgeHandler.RENDER.get().createTexture()`.
3. **`decode`** — décode les octets en pixels. Peut tourner sur un thread de fond (mode async).
4. **`upload`** — thread de rendu. Upload les pixels décodés via `ITexture.allocate` et `upload`.
5. **`request`** — thread de rendu, avant chaque dessin. Reçoit la taille couverte à l'écran par la ressource, en pixels, transformations comprises, et si la ressource est async. Les décodeurs raster l'ignorent ; le décodeur vectoriel rend à cette taille.
6. **`update`** — à chaque frame avant le rendu de la ressource. Remplacez la texture courante avec `resource.texture(...)` quand elle change (images de vidéo et d'animation).
7. **`clear`** — libère les ressources GPU quand le cache évince ou le nœud est détruit.

`isSettled()` indique si le décodeur a encore du travail qui changera l'image — une image vidéo pas encore rattrapée, une taille vectorielle encore en rendu. Les tests de snapshot attendent que chaque décodeur soit stable avant une capture.

## Décodeurs intégrés

### `RasterResourceDecoder`

Images fixes : PNG, JPG, BMP et tout format lu par ImageIO, plus le WebP fixe via un lecteur TwelveMonkeys embarqué. Texture unique.

```java
new RasterResourceDecoder(Asset);                    // lu avec ImageIO, paresseusement, dans decode()
new RasterResourceDecoder(Asset, ImageReaderSpi);    // lu avec un lecteur ImageIO donné
new RasterResourceDecoder(BufferedImage);            // depuis une image pré-décodée
```

Les pixels transparents prennent la couleur de leur pixel visible le plus proche : le filtrage linéaire n'assombrit jamais les bords de l'image.

### `AnimatedResourceDecoder`

GIF, APNG et WebP animé. Un `IResourceAnimationReader` décode une fois le fichier en `ResourceAnimation` — ses images composées et leurs durées — que le décodeur lit en mémoire et upload dans une seule texture. Il implémente `IResourcePlayback`.

```java
new AnimatedResourceDecoder(Asset, new GifResourceAnimationReader());
new AnimatedResourceDecoder(Asset, new ApngResourceAnimationReader());
new AnimatedResourceDecoder(Asset, new WebpResourceAnimationReader());
```

Un lecteur maison compose ses images avec `ResourceAnimationCanvas`, qui applique le mélange (`SOURCE`, `OVER`) et la disposition (`NONE`, `BACKGROUND`, `PREVIOUS`) de chaque image.

### `VectorResourceDecoder`

SVG via JSVG. Le document est analysé une fois, puis rendu à la taille reçue par `request(...)`, avec un cache des dernières tailles. Voir [Formats](formats.md#svg).

### `VideoResourceDecoder`

Formats vidéo — MP4, MOV, WebM, MKV, AVI. Backed par FFmpeg via JavaCV. Un WebM VP8 ou VP9 avec canal alpha garde sa transparence.

Fonctionnalités :
- Queue de frames en ring buffer (5 frames).
- Textures ping-pong pour une lecture sans tearing.
- Streaming audio via le bridge audio, synchronisé à la vidéo.
- Seek, pause, resume, loop via `IResourcePlayback`.
- Spatial audio 3D optionnel.

```java
new VideoResourceDecoder(Asset);
new VideoResourceDecoder(File);
```

Pour le contrôle de la lecture, enveloppez dans un [VideoPlayerNode](../nodes/design/video-player.md) — il expose `play / pause / seek / volume` et des callbacks de progression.

Pilotez la lecture de toute ressource animée via `Resource.getPlayback()` :

```java
resource.getPlayback().ifPresent(playback -> playback.seek(10D).play());
double progress = resource.getPlayback().map(IResourcePlayback::getProgress).orElse(0D);
```

## Écrire un décodeur custom

Implémentez `IResourceDecoder`, puis dirigez-y les octets avec un `IResourceFormat` enregistré via `ResourceFormat.register(...)` — voir [Formats](formats.md#ajouter-un-format) :

```java
public class QoiResourceDecoder implements IResourceDecoder {

    private final Asset asset;
    private int[] pixels;
    private int width, height;

    public QoiResourceDecoder(final Asset asset) {
        this.asset = asset;
    }

    @Override
    public void prepare(final ResourceData resource) {
        resource.texture(BridgeHandler.RENDER.get().createTexture().allocate(1, 1).upload(new int[] {0}, 1, 1));
    }

    @Override
    public void decode(final ResourceData resource) {
        try (InputStream stream = this.asset.open()) {
            final QoiImage image = Qoi.read(stream);
            this.width = image.getWidth();
            this.height = image.getHeight();
            this.pixels = image.getArgb();
        } catch (final IOException exception) {
            throw new RuntimeException("Unable to read " + this.asset.getUniqueId(), exception);
        }
        resource.width(this.width).height(this.height).data(new int[][] {this.pixels});
    }

    @Override
    public void upload(final ResourceData resource) {
        resource.getTextures()[0].allocate(this.width, this.height).upload(this.pixels, this.width, this.height);
    }

    @Override
    public void clear(final ResourceData resource) {
        this.pixels = null;
    }

}
```

## Bonnes pratiques

- **Créez les textures via le bridge de rendu.** `createTexture()`, `allocate` et `upload` fonctionnent sur tous les backends.
- **Ouvrez l'asset dans `decode`.** Lisez-le là et lâchez le flux ; le décodeur garde l'asset, pas ses octets.
- **Gardez les appels de texture dans `prepare`, `upload`, `request` et `update`.** Ils tournent sur le thread de rendu ; `decode` peut tourner sur n'importe quel thread.

## Voir aussi

- [Formats](formats.md) — chaque format pris en charge et comment il est reconnu.
- [ResourceBuilder](resource-builder.md).
- [Resolvers](resolvers.md) — la couche de dispatch qui choisit le décodeur à utiliser.
- [ResourceNode](../nodes/design/resource.md).
- [VideoPlayerNode](../nodes/design/video-player.md).