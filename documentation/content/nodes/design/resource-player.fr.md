# ResourcePlayerNode

Lecteur vidéo complet. Lit MP4, MOV, WebM, MKV, AVI via FFmpeg, ainsi que les animations GIF et APNG avec les mêmes contrôles de lecture. Audio streamé via le bridge audio, synchronisé à la vidéo.

## Créer

```java
final Resource video = Resource.of(getClass().getResourceAsStream("/intro.mp4"));

ResourcePlayerNode.create(0, 0, 854, 480)
    .resource(video)
    .loop(true)
    .volume(1F)
    .autoplay(true)
    .attach(parent);
```

> NOTE: rien n'est lu à ce stade. Le flux devient un [asset](../../resources/assets.md), et la copie dont FFmpeg a besoin s'écrit sur le worker de ressources quand le décodeur tourne — une source illisible ressort là, pas ici.

## Contrôle de lecture

```java
node.play();
node.pause();
node.resume();
node.stop();
node.seek(double seconds);
node.seekTo(double seconds);   // alias
node.restart();                 // stop + seek(0) + play
```

Requêter l'état :

```java
node.isPlaying();
node.isPaused();
node.getDuration();             // secondes
node.getProgress();             // 0.0 → 1.0, le début de l'image affichée : un peu moins de 1.0 à la fin
node.getPlayback();             // Optional<IResourcePlayback>, pour vidéos et animations
node.getVideo();                // Optional<VideoResourceDecoder>, pour les vidéos seulement
node.getVideo().map(VideoResourceDecoder::getFrameRate).orElse(0D);
```

## Configuration

```java
node.loop(boolean);
node.volume(float);              // 0.0 → 1.0
node.autoplay(boolean);          // démarre immédiatement au load de la ressource
node.stretch(StretchType);       // STRETCH (défaut), CONTAIN ou COVER
node.resource(Resource);         // change la vidéo — libère le décodeur précédent
```

## Callbacks

```java
node.onPlay(player -> { /* démarré */ });
node.onPause(player -> { /* pausé */ });
node.onEnd(player -> { /* fin de lecture (seulement quand loop=false) */ });
node.onProgress((player, progress, currentTime) -> {
    // Se déclenche à chaque avancement de frame
});
```

## Audio spatial 3D

L'audio de la vidéo peut fader selon la distance au listener. Utile pour les UIs dans le monde :

```java
node.location(float x, float y, float z);          // position de la source
node.referenceDistance(float);                      // volume plein jusqu'à cette distance
node.maxDistance(float);                            // muet au-delà

// La position du listener est définie globalement :
VideoAudioPlayer.setAudioListener(() -> new Vector3f(listenerX, listenerY, listenerZ));
```

L'`AudioListener` est une interface fonctionnelle dont `getListenerPosition()` renvoie un `Vector3f` — vous fournissez votre propre logique de position de listener (position du joueur, de la caméra, etc.). S'il n'est pas défini, le fade est skipé et le volume est constant.

## Exemple — toggle plein écran

```java
final ResourcePlayerNode player = ResourcePlayerNode.create(100, 100, 640, 360)
    .resource(video)
    .loop(true)
    .attach(this);

this.keybind(() -> {
    if (player.getWidth() == 640) {
        player.position(0, 0).size(1920, 1080);
    } else {
        player.position(100, 100).size(640, 360);
    }
}, Key.F);
```

## Exemple — barre de progression cliquable

```java
final ResourcePlayerNode player = ResourcePlayerNode.create(0, 0, 854, 480)
    .resource(video)
    .attach(this);

ProgressNode.create(0, 490, 854, 8)
    .color(Color.decode("#374151"), Color.decode("#3b82f6"))
    .progress(() -> (float) player.getProgress())
    .onClick((bar, mx, my, ct) -> {
        final double t = (mx - bar.getAbsoluteX()) / bar.getWidth() * player.getDuration();
        player.seekTo(t);
    })
    .effect(RoundedNodeEffect.create(4F))
    .attach(this);
```

## Cycle de vie & cleanup

`ResourcePlayerNode` override `detach()` pour libérer son décodeur automatiquement — pas de leak à la fermeture de l'UI ou au retrait via `clearChildren()`.

Changer la ressource avec `.resource(newResource)` libère le grabber, l'audio player et le thread du décodeur précédent avant de switcher.

## Formats pris en charge

Les vidéos — MP4, MOV, WebM, MKV, AVI — se lisent via FFmpeg, avec leur son. GIF, APNG et WebP animé se lisent avec les mêmes contrôles et callbacks, sans son. Un WebM VP8 ou VP9 avec canal alpha garde sa transparence. Voir [Formats](../../resources/formats.md).

## Bonnes pratiques

- **Changez de ressource via `.resource(newResource)`.** Le décodeur détient un thread, un fichier temp et une source audio : `.resource(...)` libère le précédent, tout comme le retrait du nœud.
- **Mettez `autoplay(false)` pour une lecture déclenchée par l'utilisateur.** Sinon la vidéo démarre dès le décodage.
- **Préférez `.location(x, y, z) + setAudioListener` pour l'audio monde.** Régler le volume manuellement à chaque frame est moins efficient.
- **Ne gardez pas de référence à un décodeur disposé.** Appelez `getPlayback()` ou `getVideo()` à chaque fois — ils sont vides une fois la ressource partie.

## Voir aussi

- [Decoders](../../resources/decoders.md) — internals du `VideoResourceDecoder`.
- [ResourceBuilder](../../resources/resource-builder.md) — auto-détection vidéo depuis streams.