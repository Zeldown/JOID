# Installation

JOID 8.0.0 ships as jars attached to each [GitHub release](https://github.com/Zeldown/JOID/releases); it is not published to a Maven repository. You add one backend jar, declare the libraries JOID shares with your application, and add the LWJGL modules of that backend. Your UI code is the same on every backend.

## Minimal setup

Put a backend jar in a `libs/` folder and declare the libraries next to it. For LWJGL 3 with Gradle:

```groovy
plugins {
	id 'java'
}

sourceCompatibility = 1.8
targetCompatibility = 1.8

repositories {
	mavenCentral()
}

def lwjglVersion = '3.3.4'
def os = System.getProperty('os.name').toLowerCase()
def lwjglNatives = os.contains('win') ? 'natives-windows' : os.contains('mac') ? (System.getProperty('os.arch').startsWith('aarch64') ? 'natives-macos-arm64' : 'natives-macos') : 'natives-linux'

dependencies {
	implementation files('libs/joid-backend-lwjgl3-8.0.0-dev.jar')

	implementation 'com.google.guava:guava:15.0'
	implementation 'com.google.code.gson:gson:2.2.4'
	implementation 'org.apache.commons:commons-lang3:3.1'
	implementation 'commons-io:commons-io:2.4'
	implementation 'java3d:vecmath:1.3.1'

	['lwjgl', 'lwjgl-glfw', 'lwjgl-openal', 'lwjgl-opengl'].each { module ->
		implementation "org.lwjgl:${module}:${lwjglVersion}"
		runtimeOnly "org.lwjgl:${module}:${lwjglVersion}:${lwjglNatives}"
	}
}
```

![The LWJGL 3 dev jar with what it embeds, and the libraries you declare next to it](../images/diagram-install-jars.png "A backend jar carries the core, the backend and the media libraries; the shared libraries and LWJGL are yours to declare")

A backend jar already contains the core and the media libraries (SVG, WebP, video). Put exactly one backend jar on the classpath, never a backend jar and `joid-core` together. Lombok is not needed to use JOID.

## Requirements

| Requirement | Value |
| --- | --- |
| Java | Java 8 or later. |
| LWJGL 2 or LWJGL 3 backend | An OpenGL 2.0 to 4.6 context, compatibility or core, with framebuffer objects. |
| Vulkan backend | A Vulkan 1.3 device with swapchain support. |
| Graphics context | A depth buffer and an 8-bit stencil buffer: masks and clipped overflow use the stencil. |

## Choosing a backend

| Jar | Renders with | LWJGL libraries to declare |
| --- | --- | --- |
| `joid-backend-lwjgl3-8.0.0-dev.jar` | OpenGL through LWJGL 3 and GLFW | `lwjgl`, `lwjgl-glfw`, `lwjgl-openal`, `lwjgl-opengl` `3.3.4`, with their natives |
| `joid-backend-vulkan-8.0.0-dev.jar` | Vulkan through LWJGL 3 and GLFW | `lwjgl`, `lwjgl-glfw`, `lwjgl-openal`, `lwjgl-shaderc` `3.3.4` with their natives, `lwjgl-vulkan` (natives on macOS only) |
| `joid-backend-lwjgl2-8.0.0-dev.jar` | OpenGL through LWJGL 2 | `org.lwjgl.lwjgl:lwjgl:2.9.1`; the natives are inside the jar |
| `joid-core-8.0.0-dev.jar` | Nothing: the core alone | For [writing your own backend](../integration/writing-a-backend.md) |

For Vulkan, replace the LWJGL lines of the minimal setup with:

```groovy
dependencies {
	['lwjgl', 'lwjgl-glfw', 'lwjgl-openal', 'lwjgl-shaderc'].each { module ->
		implementation "org.lwjgl:${module}:${lwjglVersion}"
		runtimeOnly "org.lwjgl:${module}:${lwjglVersion}:${lwjglNatives}"
	}

	implementation "org.lwjgl:lwjgl-vulkan:${lwjglVersion}"
	if (os.contains('mac')) {
		runtimeOnly "org.lwjgl:lwjgl-vulkan:${lwjglVersion}:${lwjglNatives}"
	}
}
```

For LWJGL 2, a single line replaces them; `Backend.register()` extracts the natives embedded in the jar:

```groovy
dependencies {
	implementation 'org.lwjgl.lwjgl:lwjgl:2.9.1'
}
```

## Dev and prod jars

Every backend jar and the core come in two flavours. Develop with `-dev`, ship `-prod`.

| Flavour | Contains |
| --- | --- |
| `-dev` | Everything, plus the developer tools (inspector, profiler, hot reload), the demo UIs and their assets. |
| `-prod` | What your application needs at runtime. `JOID.inst().setDevMode(true)` throws an `IllegalStateException` on it. |

## Maven

Install the jar in your local repository once, then declare it with the same libraries as in Gradle:

```
mvn install:install-file -Dfile=libs/joid-backend-lwjgl3-8.0.0-dev.jar -DgroupId=dev.joid -DartifactId=joid-backend-lwjgl3 -Dversion=8.0.0 -Dclassifier=dev -Dpackaging=jar
```

```xml
<dependency>
	<groupId>dev.joid</groupId>
	<artifactId>joid-backend-lwjgl3</artifactId>
	<version>8.0.0</version>
	<classifier>dev</classifier>
</dependency>
```

## Verifying the setup

```java
import dev.joid.internal.JOID;

public final class Check {

	public static void main(final String[] args) {
		System.out.println("JOID " + JOID.VERSION);
	}

}
```

It prints `JOID 8.0.0`. Continue with the [Quick Start](quick-start.md) for a running window.

## Building from source

| Command | Result |
| --- | --- |
| `./gradlew build -x test` | Builds the `-prod` jars into `build/libs` |
| `./gradlew build -x test -Pdev` | Builds the `-dev` jars |
| `./gradlew :backend-lwjgl3:runDemo` | Opens the demo window (also `:backend-lwjgl2:runDemo`, `:backend-vulkan:runDemo`) |

Run the wrapper with a JDK 8.

## Good to know

- On macOS, LWJGL 3 and Vulkan need the `-XstartOnFirstThread` JVM option.
- The natives classifier above is picked for the machine that builds. When you package for several platforms, add the classifier of each one.
- Switch to the `-prod` jar for the build you ship, and remove any `setDevMode(true)` call.

## See also

- [Quick Start](quick-start.md)
- [Frame Loop and Dev Tools](../concepts/frame-loop.md)
- [Bridges and Backends](../integration/backends.md)
- [Writing a Backend](../integration/writing-a-backend.md)