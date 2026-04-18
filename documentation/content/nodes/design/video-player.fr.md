# VideoPlayerNode

Lecteur vidéo complet. Lit MP4, MOV, WebM, MKV, AVI, GIF, APNG — tout ce que FFmpeg peut décoder. Audio streamé via OpenAL, synchronisé à la vidéo.

## Créer

```java
try {
    final Resource video = Resource.of(getClass().getResourceAsStream("/intro.mp4"));

    VideoPlayerNode.create(0, 0, 854, 480)
        .resource(video)
        .loop(true)
        .volume(1F)
        .autoplay(true)
        .attach(parent);
} catch (IOException e) {
    e.printStackTrace();
}
```

> NOTE: `Resource.of(InputStream)` throw `IOException` pour les streams vidéo — wrappez en try/catch ou propagez.

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
node.getProgress();             // 0.0 → 1.0
node.getDecoder().getCurrentVideoTime();  // secondes
node.getDecoder().getFrameRate();
```

## Configuration

```java
node.loop(boolean);
node.volume(float);              // 0.0 → 1.0
node.autoplay(boolean);          // démarre immédiatement au load de la ressource
node.stretch(StretchType);       // STRETCH (défaut) ou CONTAIN
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

L'`AudioListener` est un `Supplier<Vector3f>` — vous fournissez votre propre logique de position de listener (position du joueur, de la caméra, etc.). S'il n'est pas défini, le fade est skipé et le volume est constant.

## Exemple — toggle plein écran

```java
final VideoPlayerNode player = VideoPlayerNode.create(100, 100, 640, 360)
    .resource(video)
    .loop(true)
    .attach(this);

this.keybind(() -> {
    if (player.getWidth() == 640) {
        player.position(0, 0).size(1920, 1080);
    } else {
        player.position(100, 100).size(640, 360);
    }
}, Keyboard.KEY_F);
```

## Exemple — barre de progression cliquable

```java
final VideoPlayerNode player = VideoPlayerNode.create(0, 0, 854, 480)
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

`VideoPlayerNode` override `detach()` pour libérer son décodeur automatiquement — pas de leak à la fermeture de l'UI ou au retrait via `clearChildren()`.

Changer la ressource avec `.resource(newResource)` libère le grabber, l'audio player et le thread du décodeur précédent avant de switcher.

## Formats supportés

Détectés via magic bytes (en-tête), pas l'extension :

- **MP4 / MOV** — box `ftyp`
- **WebM / MKV** — en-tête EBML
- **AVI** — `RIFF` + `AVI`
- **GIF** — `GIF87a` / `GIF89a`
- **APNG** — PNG avec chunk `acTL` (détecté par FFmpeg)

Pour GIF/APNG, le loop est activé par défaut (`isLoopByDefault`).

## Bonnes pratiques

- **Utilisez `release()` avant de swap plusieurs fois.** Le décodeur détient un thread, un fichier temp et une source OpenAL — le cleanup compte pour les apps longues.
- **Mettez `autoplay(false)` pour une lecture déclenchée par l'utilisateur.** Sinon la vidéo démarre dès le décodage.
- **Préférez `.location(x, y, z) + setAudioListener` pour l'audio monde.** Régler le volume manuellement à chaque frame est moins efficient.
- **Ne gardez pas de référence à un décodeur disposé.** Appelez `getDecoder()` à chaque fois — il retourne `null` après release.

## Voir aussi

- [Decoders](../../resources/decoders.md) — internals du `VideoResourceDecoder`.
- [ResourceBuilder](../../resources/resource-builder.md) — auto-détection vidéo depuis streams.