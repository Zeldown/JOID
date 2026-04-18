# Installation

JOID is shipped as a fat JAR through GitHub Releases. No Maven Central, no package manager — just drop the JAR in your classpath and point the JVM at the native libraries.

## Download

Grab the latest release from [github.com/Zeldown/JOID/releases](https://github.com/Zeldown/JOID/releases).

Two artifacts are published per version:

| Artifact | Contents | Use when |
|---|---|---|
| `joid-X.Y.Z-prod.jar` | Library only, `assets/dev/*` and `assets/test/*` stripped | Shipping your app |
| `joid-X.Y.Z-dev.jar` | Includes demo fonts, test textures, sample videos | Learning / developing |

## Gradle

```groovy
dependencies {
    compile files('libs/joid-6.0.0-prod.jar')
}
```

Legacy Gradle uses `compile`; modern Gradle uses `implementation`. Both work.

## Maven

```xml
<dependency>
    <groupId>be.zeldown.joid</groupId>
    <artifactId>joid</artifactId>
    <version>6.0.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/libs/joid-6.0.0-prod.jar</systemPath>
</dependency>
```

## Native libraries

JOID relies on LWJGL 2 for OpenGL/OpenAL. The `native/` folder in the JOID repo contains the runtime natives (`lwjgl64.dll`, `OpenAL64.dll`, and their platform variants).

Copy that folder next to your project and launch with:

```
-Djava.library.path=./native
```

> TIP: If you already have LWJGL 2 natives from another project, they're compatible. Just make sure `OpenAL64.dll` (or `libopenal.so` / `libopenal.dylib`) is present — it's needed for `VideoPlayerNode` audio.

## Dependencies

JOID shades most of its runtime deps into the fat JAR. FFmpeg natives for video decoding are pulled via `org.bytedeco:ffmpeg` classifiers — add only the ones you target to keep binary size down:

```groovy
dependencies {
    compile 'org.bytedeco:ffmpeg:6.0-1.5.9:windows-x86_64'
    // Or macosx-x86_64 / macosx-arm64 / linux-x86_64
}
```

If you're not using video, you can skip FFmpeg entirely — `VideoPlayerNode` is the only consumer.

## Verification

Drop this in a `main`:

```java
public static void main(String[] args) {
    System.out.println("JOID version: " + JOID.VERSION);
}
```

If it prints `6.0.0`, you're ready. Continue with [Quick Start](quick-start.md).
