# Installation

JOID is shipped as a fat JAR through GitHub Releases. No Maven Central, no package manager — just drop the JAR in your classpath and point the JVM at the native libraries.

## Download

Grab the latest release from [github.com/Zeldown/JOID/releases](https://github.com/Zeldown/JOID/releases).

Two artifacts are published per version:

| Artifact | Contents | Use when |
|---|---|---|
| `joid-X.Y.Z-prod.jar` | Library only, `assets/dev/*` and `assets/demo/*` stripped | Shipping your app |
| `joid-X.Y.Z-dev.jar` | Includes demo fonts, demo textures, sample videos | Learning / developing |

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

JOID shades its small utility dependencies (Guava, Gson, commons-lang3, commons-compress, commons-io, vecmath) into the fat JAR. **Larger runtime dependencies stay external** so you can pick the exact classifiers you need and avoid bloating your artifact.

Add these to your `build.gradle` alongside JOID:

```groovy
dependencies {
    compile files('libs/joid-6.0.0-prod.jar')

    compile 'org.projectlombok:lombok:1.18.34'
    annotationProcessor 'org.projectlombok:lombok:1.18.34'

    compile 'org.lwjgl.lwjgl:lwjgl:2.9.1'

    compile('org.bytedeco:javacv:1.5.9') { transitive = false }
    compile 'org.bytedeco:javacpp:1.5.9'
    compile('org.bytedeco:ffmpeg:6.0-1.5.9') { transitive = false }

    compile 'org.bytedeco:ffmpeg:6.0-1.5.9:windows-x86_64'
    compile 'org.bytedeco:ffmpeg:6.0-1.5.9:macosx-x86_64'
    compile 'org.bytedeco:ffmpeg:6.0-1.5.9:macosx-arm64'
    compile 'org.bytedeco:ffmpeg:6.0-1.5.9:linux-x86_64'
}
```

The same Maven block:

```xml
<dependencies>
    <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><version>1.18.34</version><scope>provided</scope></dependency>
    <dependency><groupId>org.lwjgl.lwjgl</groupId><artifactId>lwjgl</artifactId><version>2.9.1</version></dependency>
    <dependency><groupId>org.bytedeco</groupId><artifactId>javacv</artifactId><version>1.5.9</version><exclusions><exclusion><groupId>*</groupId><artifactId>*</artifactId></exclusion></exclusions></dependency>
    <dependency><groupId>org.bytedeco</groupId><artifactId>javacpp</artifactId><version>1.5.9</version></dependency>
    <dependency><groupId>org.bytedeco</groupId><artifactId>ffmpeg</artifactId><version>6.0-1.5.9</version><exclusions><exclusion><groupId>*</groupId><artifactId>*</artifactId></exclusion></exclusions></dependency>
    <dependency><groupId>org.bytedeco</groupId><artifactId>ffmpeg</artifactId><version>6.0-1.5.9</version><classifier>windows-x86_64</classifier></dependency>
    <dependency><groupId>org.bytedeco</groupId><artifactId>ffmpeg</artifactId><version>6.0-1.5.9</version><classifier>macosx-x86_64</classifier></dependency>
    <dependency><groupId>org.bytedeco</groupId><artifactId>ffmpeg</artifactId><version>6.0-1.5.9</version><classifier>macosx-arm64</classifier></dependency>
    <dependency><groupId>org.bytedeco</groupId><artifactId>ffmpeg</artifactId><version>6.0-1.5.9</version><classifier>linux-x86_64</classifier></dependency>
</dependencies>
```

### What each dependency does

| Dependency | Purpose | Required |
|---|---|---|
| `lombok` | Code generation (`@Getter`, `@Setter`, `@NonNull`) | Compile-only — not shipped |
| `lwjgl 2.9.1` | OpenGL + OpenAL bindings | Always |
| `javacv` + `javacpp` | Java bindings for FFmpeg | Only if using `VideoPlayerNode` |
| `ffmpeg:6.0-1.5.9` (base) | FFmpeg API classes | Only if using `VideoPlayerNode` |
| `ffmpeg:…:<platform>` | Native `.dll` / `.so` / `.dylib` for the target OS | Only ship the ones you target |

> NOTE: `transitive = false` on `javacv` and `ffmpeg` prevents Gradle from pulling every classifier for every platform (~700 MB). Declare only the classifiers you actually ship.

### Shipping for production

**Don't fat-JAR your app.** The recommended layout is a thin application JAR that references every dependency JAR from a sibling `libraries/` folder, loaded via the classpath. This keeps the application artifact small, lets you swap or patch a single dependency without rebuilding, and avoids the corner cases where `shadowJar` / Maven Shade rewrites classes inside `javacpp` / `ffmpeg` and breaks native extraction.

Example layout for a distributable zip:

```
my-app/
├── my-app.jar                           # your code only — no shading
├── libraries/                           # every compile dependency, one JAR per artifact
│   ├── joid-6.0.0-prod.jar
│   ├── lwjgl-2.9.1.jar
│   ├── javacv-1.5.9.jar
│   ├── javacpp-1.5.9.jar
│   ├── ffmpeg-6.0-1.5.9.jar
│   ├── ffmpeg-6.0-1.5.9-windows-x86_64.jar
│   ├── ffmpeg-6.0-1.5.9-macosx-x86_64.jar
│   ├── ffmpeg-6.0-1.5.9-macosx-arm64.jar
│   └── ffmpeg-6.0-1.5.9-linux-x86_64.jar
├── native/                              # LWJGL 2 natives (OpenGL + OpenAL)
│   ├── lwjgl64.dll
│   ├── OpenAL64.dll
│   └── …
└── run.bat                              # launcher
```

A minimal launcher (`run.bat` on Windows, `run.sh` on *nix):

```bat
java -Djava.library.path=./native -cp "my-app.jar;libraries/*" com.myapp.Main
```

```bash
#!/bin/sh
java -Djava.library.path=./native -cp "my-app.jar:libraries/*" com.myapp.Main
```

The `libraries/*` wildcard expands to every JAR inside the folder — the JVM picks them up without listing each one. The FFmpeg classifier JARs contain the native `.dll` / `.so` / `.dylib` binaries; JavaCPP extracts them from the classpath at runtime, so they just need to be present in `libraries/`.

To produce this layout with Gradle:

```groovy
task dist(type: Copy) {
    into 'build/dist/libraries'
    from configurations.runtimeClasspath    // every resolved dep
}

task distApp(type: Jar) {
    archiveBaseName = 'my-app'
    destinationDirectory = file('build/dist')
    from sourceSets.main.output
    manifest { attributes 'Main-Class': 'com.myapp.Main' }
}

dist.dependsOn distApp
```

After `gradle dist`, zip `build/dist/` together with your `native/` folder and launcher.

If you drop video support, you can remove `javacv`, `javacpp`, and every `ffmpeg` entry from `libraries/` — `VideoPlayerNode` is the only consumer, and JOID fails gracefully if its classes are missing at runtime.

## Verification

Drop this in a `main`:

```java
public static void main(String[] args) {
    System.out.println("JOID version: " + JOID.VERSION);
}
```

If it prints the version you installed, you're ready. Continue with [Quick Start](quick-start.md).
