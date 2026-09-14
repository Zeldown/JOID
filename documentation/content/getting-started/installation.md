# Installation

JOID is shipped as a fat JAR through GitHub Releases. No Maven Central, no package manager — just drop the JAR in your classpath and point the JVM at the native libraries.

## Download

Grab the latest release from [github.com/Zeldown/JOID/releases](https://github.com/Zeldown/JOID/releases).

Each version is published for every backend. Pick the artifact matching your engine:

| Backend | Artifact prefix | Stack |
|---|---|---|
| LWJGL 2 | `joid-lwjgl2` | LWJGL 2.9.1 — OpenGL fixed pipeline, OpenAL |
| LWJGL 3 | `joid-lwjgl3` | LWJGL 3.3.4 — GLFW, OpenGL 3.3 core, OpenAL |
| Vulkan | `joid-vulkan` | LWJGL 3.3.4 — GLFW, Vulkan 1.3, shaderc, OpenAL |

Each backend comes in two flavours:

| Artifact | Contents | Use when |
|---|---|---|
| `joid-<backend>-X.Y.Z-prod.jar` | Library only, `assets/dev/*` and `assets/demo/*` stripped | Shipping your app |
| `joid-<backend>-X.Y.Z-dev.jar` | Includes demo fonts, demo textures, sample videos | Learning / developing |

Building from source with `./gradlew build` (`-Pdev` for the dev flavour) copies every release artifact — backend, core, testkit, glfw and openal jars and the backend template — into `build/libs`.

## Gradle

```groovy
dependencies {
    compile files('libs/joid-lwjgl2-6.0.0-prod.jar')
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
    <systemPath>${project.basedir}/libs/joid-lwjgl2-6.0.0-prod.jar</systemPath>
</dependency>
```

## Native libraries

Natives depend on the backend:

- **LWJGL 2** — the `impl/lwjgl2/native/` folder of the repository contains the OpenGL and OpenAL natives (`lwjgl64.dll`, `OpenAL64.dll`, and their platform variants). Copy it next to your project and launch with `-Djava.library.path=./native`.
- **LWJGL 3 and Vulkan** — natives ship as Maven classifier JARs (`natives-windows`, `natives-linux`, `natives-macos`, `natives-macos-arm64`) that LWJGL extracts at runtime. Vulkan talks to the loader installed with the GPU driver; macOS additionally needs the `lwjgl-vulkan` natives (MoltenVK).

> TIP: OpenAL is required for `VideoPlayerNode` audio on every backend.

## Dependencies

JOID shades its small utility dependencies (Guava, Gson, commons-lang3, commons-compress, commons-io, vecmath) into the fat JAR. **Larger runtime dependencies stay external** so you can pick the exact classifiers you need and avoid bloating your artifact.

Add these to your `build.gradle` alongside JOID:

```groovy
dependencies {
    compile files('libs/joid-lwjgl2-6.0.0-prod.jar')

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

### LWJGL 3 and Vulkan backends

Replace the `lwjgl 2.9.1` line with the LWJGL 3 modules of your backend and their natives:

```groovy
dependencies {
    compile 'org.lwjgl:lwjgl:3.3.4'
    compile 'org.lwjgl:lwjgl-glfw:3.3.4'
    compile 'org.lwjgl:lwjgl-openal:3.3.4'
    compile 'org.lwjgl:lwjgl-opengl:3.3.4'
    compile 'org.lwjgl:lwjgl-vulkan:3.3.4'
    compile 'org.lwjgl:lwjgl-shaderc:3.3.4'

    runtime 'org.lwjgl:lwjgl:3.3.4:natives-windows'
    runtime 'org.lwjgl:lwjgl-glfw:3.3.4:natives-windows'
    runtime 'org.lwjgl:lwjgl-openal:3.3.4:natives-windows'
    runtime 'org.lwjgl:lwjgl-opengl:3.3.4:natives-windows'
    runtime 'org.lwjgl:lwjgl-shaderc:3.3.4:natives-windows'
}
```

`lwjgl-opengl` is only needed by the LWJGL 3 backend, `lwjgl-vulkan` and `lwjgl-shaderc` only by the Vulkan backend. Use the natives classifier of each platform you ship.

### What each dependency does

| Dependency | Purpose | Required |
|---|---|---|
| `lombok` | Code generation (`@Getter`, `@Setter`, `@NonNull`) | Compile-only — not shipped |
| `lwjgl` | Engine bindings of the chosen backend | Always |
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
│   ├── joid-lwjgl2-6.0.0-prod.jar
│   ├── lwjgl-2.9.1.jar
│   ├── javacv-1.5.9.jar
│   ├── javacpp-1.5.9.jar
│   ├── ffmpeg-6.0-1.5.9.jar
│   ├── ffmpeg-6.0-1.5.9-windows-x86_64.jar
│   ├── ffmpeg-6.0-1.5.9-macosx-x86_64.jar
│   ├── ffmpeg-6.0-1.5.9-macosx-arm64.jar
│   └── ffmpeg-6.0-1.5.9-linux-x86_64.jar
├── native/                              # LWJGL 2 backend natives (OpenGL + OpenAL)
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
