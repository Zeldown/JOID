# Decoders

Les objets bas niveau qui transforment des octets en textures GPU. Vous les instanciez rarement directement — `ResourceBuilder` choisit le bon selon les magic bytes — mais connaître l'API aide pour écrire des décodeurs custom.

## `IResourceDecoder`

Le contrat :

```java
public interface IResourceDecoder {
    default public void init(final @NonNull ResourceData resource) {}
    default public void prepare(final @NonNull ResourceData resource) {}
    default public void decode(final @NonNull ResourceData resource) {}
    default public void upload(final @NonNull ResourceData resource) {}
    default public void bind(final @NonNull ResourceData resource) {}
    default public void clear(final @NonNull ResourceData resource) {}
}
```

Cycle de vie :

1. **`init`** — juste après construction, avec le `ResourceData` parent attaché.
2. **`prepare`** — appelée sur le thread GL avant decode. Allouez les textures placeholder ici.
3. **`decode`** — décode les octets en pixels. Peut tourner sur un thread de fond (mode async).
4. **`upload`** — thread GL. Upload les pixels décodés au GPU.
5. **`bind`** — à chaque frame quand la ressource est rendue. Bind la texture courante.
6. **`clear`** — libère les ressources GPU quand le cache évince ou le nœud est détruit.

## Décodeurs intégrés

### `ImageResourceDecoder`

Formats d'image statique (PNG, JPG, BMP). Utilise le `ImageIO` de Java. Texture unique, pas d'animation.

```java
ResourceDecoder.image(InputStream);      // depuis un stream
ResourceDecoder.image(BufferedImage);    // depuis une image pré-décodée
```

### `VideoResourceDecoder`

Formats animés — MP4, MOV, WebM, MKV, AVI, GIF, APNG. Backed par FFmpeg via JavaCV.

Fonctionnalités :
- Queue de frames en ring buffer (5 frames).
- Textures ping-pong pour une lecture sans tearing.
- Streaming audio OpenAL synchronisé à la vidéo.
- Seek, pause, resume, loop.
- Spatial audio 3D optionnel.

```java
ResourceDecoder.video(InputStream);
ResourceDecoder.video(InputStream, boolean loopByDefault);
ResourceDecoder.video(File);
```

Pour le contrôle de la lecture, enveloppez dans un [VideoPlayerNode](../nodes/design/video-player.md) — il expose `play / pause / seek / volume` et des callbacks de progression.

Accès aux internals du décodeur via `Resource.getDecoder()` :

```java
VideoResourceDecoder vrd = (VideoResourceDecoder) resource.getDecoder();
vrd.play();
vrd.seek(10D);
double progress = vrd.getProgress();
```

## Helpers magic bytes

Utilitaires de détection statiques sur `VideoResourceDecoder` :

```java
VideoResourceDecoder.isVideoHeader(byte[] header, int read);   // true pour MP4/MOV/WebM/MKV/AVI/GIF
VideoResourceDecoder.isLoopByDefault(byte[] header, int read); // true pour GIF (loop on)
```

`ResourceBuilder.of(InputStream)` les utilise pour router vers le bon décodeur.

## Écrire un décodeur custom

Implémentez `IResourceDecoder` et enregistrez-le manuellement via `ResourceDecoder` ou en construisant directement un `ResourceData` :

```java
public class SVGResourceDecoder implements IResourceDecoder {

    private final InputStream stream;
    private int[] pixels;
    private int width, height;

    public SVGResourceDecoder(final InputStream stream) {
        this.stream = stream;
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
        final int tex = GL11.glGenTextures();
        AllocatedTextureUtil.allocateTexture(tex, width, height);
        AllocatedTextureUtil.uploadTexture(tex, pixels, width, height);
        resource.textureId(tex);
    }

    @Override
    public void bind(ResourceData resource) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, resource.getTextureId()[0]);
    }

    @Override
    public void clear(ResourceData resource) {
        if (resource.getTextureId() != null) {
            GL11.glDeleteTextures(resource.getTextureId()[0]);
        }
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

- **Utilisez `AllocatedTextureUtil`.** Gère l'allocation de texture GL et le setup mipmap de manière cohérente.
- **Ne gardez pas l'InputStream indéfiniment.** Consommez-le pendant `decode` et lâchez la référence.
- **Gardez les appels GL dans `upload` et `clear` sur le thread GL.** Ces deux tournent sur le thread de rendu ; les autres peuvent être sur n'importe quel thread.

## Voir aussi

- [ResourceBuilder](resource-builder.md).
- [ResourceNode](../nodes/design/resource.md).
- [VideoPlayerNode](../nodes/design/video-player.md).