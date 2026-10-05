# Formats

`Resource.of(...)` lit images, animations, vidéos et vectoriels avec le même appel : JOID regarde les premiers octets de l'[asset](assets.md), jamais son extension, et choisit le décodeur de son format. Un nœud dessine tous les formats de la même façon.

```java
ResourceNode.create(0, 0, 64, 64).resource(Resource.of("/icons/check.svg")).attach(flex);
ResourceNode.create(0, 0, 160, 160).resource(Resource.of("/textures/spinner.webp")).attach(flex);
ResourceNode.create(0, 0, 256, 256).resource(Resource.of("/videos/overlay.webm")).attach(flex);
```

## Formats pris en charge

| Format | Reconnu par | Décodeur |
|---|---|---|
| SVG | une racine `<svg>`, après un BOM, une déclaration XML, des commentaires et un doctype optionnels | `VectorResourceDecoder` |
| WebP fixe | `RIFF` … `WEBP` | `RasterResourceDecoder` |
| WebP animé | `RIFF` … `WEBP` avec le drapeau d'animation de son chunk `VP8X` | `AnimatedResourceDecoder` |
| GIF | `GIF87a` / `GIF89a` | `AnimatedResourceDecoder` |
| APNG | un PNG avec un chunk `acTL` avant son premier `IDAT` | `AnimatedResourceDecoder` |
| MP4, MOV | `ftyp` à l'offset 4 | `VideoResourceDecoder` |
| WebM, MKV | l'en-tête EBML `1A 45 DF A3` | `VideoResourceDecoder` |
| AVI | `RIFF` … `AVI ` | `VideoResourceDecoder` |
| PNG, JPG, BMP et tout autre format ImageIO | tout le reste | `RasterResourceDecoder` |

`ResourceFormat.decoder(asset)` lit les 512 premiers octets de l'asset via `peek(...)`, sans le consommer, et demande à chaque format enregistré s'il les reconnaît, le dernier enregistré en premier.

## SVG

Un vectoriel est rendu à la **taille réelle en pixels de chaque dessin** : la taille du nœud, multipliée par l'échelle d'interface, le zoom, la résolution de la fenêtre et toutes les transformations du nœud — l'échelle d'un `TransformNodeEffect` comprise. Un texel de la texture tombe sur un pixel de l'écran : l'image reste nette à 24 px comme à ×8.

- **Taille intrinsèque** — le `width` / `height` du document, ou son `viewBox`. Un `ResourceNode` sans taille la prend, comme la taille d'une image.
- **Étirement** — dessiné dans un rectangle d'un autre ratio, le document est étiré comme une image.
- **Taille qui change** — tant que la taille bouge, pendant une animation de zoom par exemple, le document est rendu au palier ×1,25 supérieur plutôt qu'à chaque frame, puis à la taille exacte quand la taille n'a plus bougé depuis 200 ms. Les 8 dernières tailles restent en cache sous forme de textures.
- **Threads** — une ressource async rend sur un thread de fond et garde la texture précédente affichée en attendant. Une ressource bloquante rend en synchrone : chaque dessin montre la taille exacte.
- **Contenu** — JSVG rend dégradés, traits, clips, masques, motifs, texte et les filtres courants. Les ressources externes ne sont pas chargées.

## Animations

GIF, APNG et WebP animé partagent `AnimatedResourceDecoder`. Le fichier est décodé une fois en images composées — la disposition et le mélange de chaque image sont appliqués — puis lu en mémoire sur l'horloge du bridge : les snapshots restent déterministes.

- **Boucles** — l'animation se joue autant de fois que son fichier l'indique, à l'infini pour la plupart des GIF. `loop(true)` ou `loop(false)` le remplace.
- **Durées** — une image de moins de 10 ms dure 100 ms, comme dans les navigateurs.
- **Mémoire** — les images restent en mémoire : largeur × hauteur × 4 octets par image. Pour une animation longue ou grande, préférez un WebM, qui lit ses images en flux.

## Lecture

Animations et vidéos implémentent `IResourcePlayback`, accessible via `Resource.getPlayback()` :

```java
resource.getPlayback().ifPresent(playback -> playback.loop(false).seek(0D).play());
double progress = resource.getPlayback().map(IResourcePlayback::getProgress).orElse(0D);
```

| Méthode | Effet |
|---|---|
| `play()` | Démarre depuis le début |
| `stop()` | S'arrête sur l'image courante |
| `pause()` / `resume()` | Fige puis reprend la lecture |
| `seek(seconds)` | Saute à un instant |
| `loop(boolean)` / `autoplay(boolean)` | Répète la lecture, la démarre au chargement |
| `isPlaying()`, `isPaused()`, `isLoop()`, `isAutoplay()` | État de la lecture |
| `getDuration()`, `getCurrentTime()`, `getProgress()` | Durée et position, en secondes et de 0 à 1 |

Un [VideoPlayerNode](../nodes/design/video-player.md) les pilote toutes avec les mêmes contrôles et callbacks : un WebP animé se lit, se met en pause et boucle comme une vidéo.

## WebM transparent

Un WebM VP8 ou VP9 avec canal alpha garde sa transparence : quand le flux annonce son alpha, JOID le décode avec libvpx, car les décodeurs propres à FFmpeg l'abandonnent. Pour en encoder un :

```
ffmpeg -i input.mov -c:v libvpx-vp9 -pix_fmt yuva420p overlay.webm
```

## Ajouter un format

Implémentez `IResourceFormat` et enregistrez-le une fois, au démarrage :

```java
public class QoiResourceFormat implements IResourceFormat {

    @Override
    public boolean supports(final @NonNull byte[] header) {
        return header.length >= 4 && header[0] == 'q' && header[1] == 'o' && header[2] == 'i' && header[3] == 'f';
    }

    @Override
    public @NonNull IResourceDecoder decoder(final @NonNull Asset asset, final @NonNull byte[] header) {
        return new QoiResourceDecoder(asset);
    }

}

ResourceFormat.register(new QoiResourceFormat());
```

Un format enregistré plus tard est interrogé en premier : il peut aussi remplacer un format intégré. Voir [Decoders](decoders.md) pour écrire le décodeur lui-même.

## Bibliothèques embarquées

Chaque JAR JOID embarque ce dont ces formats ont besoin : FFmpeg avec ses natives, JSVG et TwelveMonkeys ImageIO. JOID n'enregistre rien dans ImageIO. Voir [Installation](../getting-started/installation.md).

## Voir aussi

- [ResourceBuilder](resource-builder.md).
- [Decoders](decoders.md).
- [ResourceNode](../nodes/design/resource.md).
- [VideoPlayerNode](../nodes/design/video-player.md).