---
summary: Extracts the native libraries of LWJGL 2 and OpenAL into the cache folder, unless the application already provides them.
---

Learn: [Start the other backends](/use/learn/running/backends/#start-the-other-backends), [Natives](/build/backends/lwjgl2/#natives).

## install()

Extracts the natives of the current system into the `lwjgl` area of the cache folder, once per run, and points `org.lwjgl.librarypath` at them.
Does nothing when `org.lwjgl.librarypath` is set or when `java.library.path` already holds every library.
Call it before the `Display` exists; throws an `IllegalStateException` when the cache folder cannot be written.
