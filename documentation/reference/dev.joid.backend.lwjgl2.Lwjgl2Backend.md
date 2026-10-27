---
summary: The backend for LWJGL 2, which draws with OpenGL into the `Display` and plays sound with the OpenAL of LWJGL 2.
---

Call [[Lwjgl2Natives#install]] before the `Display` exists, register the backend once it exists, and call [[Lwjgl2InputForwarder#poll]] at the start of every frame: LWJGL 2 has no input callbacks.

Learn: [Start the other backends](/use/learn/running/backends/#start-the-other-backends), [LWJGL 2 backend](/build/backends/lwjgl2/).

## create()

Creates the backend for the `Display` of LWJGL 2.
