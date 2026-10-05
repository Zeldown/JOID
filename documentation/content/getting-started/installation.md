# Installation

JOID is shipped as JARs through GitHub Releases. They contain JOID and every library it decodes media with: add the JAR of your backend and the few libraries listed in *Dependencies* to your classpath, and point the JVM at the native libraries of your engine. No Maven Central, no package manager.

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
| `joid-<backend>-X.Y.Z-prod.jar` | Library only, `assets/demo/*` stripped | Shipping your app |
| `joid-<backend>-X.Y.Z-dev.jar` | Includes demo fonts, demo textures, sample videos | Learning / developing |

A backend JAR contains the core, the JOID modules it uses — `joid-glfw` and `joid-openal` for LWJGL 3 and Vulkan — and the media libraries:

- **FFmpeg** through JavaCV and JavaCPP, with its natives for Windows x64, Linux x64, macOS Intel and macOS ARM, extracted at runtime — about 95 MB of the JAR. It keeps its `org.bytedeco` packages, which JavaCPP needs to find its natives.
- **JSVG** (SVG) and **TwelveMonkeys ImageIO** (WebP), relocated under `dev.joid.shaded`, so they never clash with copies your application or its host ships. JOID instantiates its WebP reader itself and registers nothing in ImageIO.

Building from source with `./gradlew build` (`-Pdev` for the dev flavour) copies every release artifact — backend, core, testkit, glfw and openal jars and the backend template — into `build/libs`.

## Gradle

```groovy
dependencies {
    compile files('libs/joid-lwjgl2-6.0.0-prod.jar')
}
```

Legacy Gradle uses `compile`; modern Gradle uses `implementation`. Both work. Add the libraries listed in *Dependencies* as well.

## Maven

```xml
<dependency>
    <groupId>dev.joid</groupId>
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

> TIP: OpenAL is required for `ResourcePlayerNode` audio on every backend.

## Dependencies

**Your project declares the libraries JOID shares with its host**, at the versions JOID is built and tested with: the ones Minecraft 1.7.10 already ships, some of which appear in the JOID API (`ResourceBuilder` caches are Guava caches, stores read Gson objects), and the LWJGL of your backend. Everything else is inside the JAR.

Add these to your `build.gradle` alongside JOID:

```groovy
dependencies {
    compile files('libs/joid-lwjgl2-6.0.0-prod.jar')

    compile 'org.projectlombok:lombok:1.18.34'
    annotationProcessor 'org.projectlombok:lombok:1.18.34'

    compile 'com.google.guava:guava:15.0'
    compile 'com.google.code.gson:gson:2.2.4'
    compile 'org.apache.commons:commons-lang3:3.1'
    compile 'org.apache.commons:commons-compress:1.8.1'
    compile 'commons-io:commons-io:2.4'
    compile 'java3d:vecmath:1.3.1'

    compile 'org.lwjgl.lwjgl:lwjgl:2.9.1'

}
```

The same Maven block:

```xml
<dependencies>
    <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><version>1.18.34</version><scope>provided</scope></dependency>
    <dependency><groupId>com.google.guava</groupId><artifactId>guava</artifactId><version>15.0</version></dependency>
    <dependency><groupId>com.google.code.gson</groupId><artifactId>gson</artifactId><version>2.2.4</version></dependency>
    <dependency><groupId>org.apache.commons</groupId><artifactId>commons-lang3</artifactId><version>3.1</version></dependency>
    <dependency><groupId>org.apache.commons</groupId><artifactId>commons-compress</artifactId><version>1.8.1</version></dependency>
    <dependency><groupId>commons-io</groupId><artifactId>commons-io</artifactId><version>2.4</version></dependency>
    <dependency><groupId>java3d</groupId><artifactId>vecmath</artifactId><version>1.3.1</version></dependency>
    <dependency><groupId>org.lwjgl.lwjgl</groupId><artifactId>lwjgl</artifactId><version>2.9.1</version></dependency>
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
| `guava`, `gson`, `commons-lang3`, `commons-compress`, `commons-io`, `vecmath` | Collections and caches, JSON, text, archives, file monitoring and vector math used by the core | Always — a host that already ships them provides them |
| `lwjgl` | Engine bindings of the chosen backend | Always |

### Shipping for production

**Don't fat-JAR your app.** The recommended layout is a thin application JAR that references every dependency JAR from a sibling `libraries/` folder, loaded via the classpath. This keeps the application artifact small, lets you swap or patch a single dependency without rebuilding, and never shades the JOID JAR a second time: JavaCPP finds the FFmpeg natives through their original `org.bytedeco` packages.

Example layout for a distributable zip:

```
my-app/
├── my-app.jar                           # your code only — no shading
├── libraries/                           # every compile dependency, one JAR per artifact
│   ├── joid-lwjgl2-6.0.0-prod.jar
│   ├── guava-15.0.jar
│   ├── gson-2.2.4.jar
│   ├── commons-lang3-3.1.jar
│   ├── commons-compress-1.8.1.jar
│   ├── commons-io-2.4.jar
│   ├── vecmath-1.3.1.jar
│   └── lwjgl-2.9.1.jar
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

## Verification

Drop this in a `main`:

```java
public static void main(String[] args) {
    System.out.println("JOID version: " + JOID.VERSION);
}
```

If it prints the version you installed, you're ready. Continue with [Quick Start](quick-start.md).
