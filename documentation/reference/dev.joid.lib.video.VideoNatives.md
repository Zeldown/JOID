---
summary: Installs the FFmpeg natives that videos need, from the classpath or downloaded once into the cache folder.
---

The first video that decodes installs them by itself; call `install()` at startup to download them before a video shows.
When the FFmpeg natives of the platform are on the classpath, nothing is downloaded.
Otherwise the jar of the platform (Windows x64, Linux x64, macOS x64 and arm64) is downloaded from the repository, checked against its SHA-256, and extracted into the `ffmpeg` area of the [cache folder](/use/learn/running/files/#know-what-joid-writes).

Learn: [Images and media](/use/learn/describing-the-ui/media/).

## repository(String)

Sets the Maven repository the natives are downloaded from, such as a mirror of your own.

## install()

Returns a future that completes once the natives are ready, downloading them on a background thread the first time.
It completes exceptionally with [[VideoNativesException]] when the platform has no natives, the download fails or does not match its checksum, or the cache folder cannot be written; the next call tries again.

## getRepository()

Returns the repository the natives are downloaded from, Maven Central by default.

## isInstalled()

If `true`, the natives are ready and videos can decode.
