---
summary: The state guard backend of the OpenGL of LWJGL 3, over any snapshot backend.
---

It sets the traps and draws like a host with the `GL11C` to `GL33C` classes of LWJGL 3, and `GL11` for the fixed-function traps, so an engine on LWJGL 3 runs the state guard suite with its own snapshot backend.
It ships in the `snapshot` jar of base-lwjgl3-opengl and in the dev and demo jars of the LWJGL 3 backend.

Learn: [Snapshot tools](/build/base-modules/lwjgl3-opengl/#snapshot-tools).

## Lwjgl3StateGuardBackend(ISnapshotBackend)

Creates the backend over that snapshot backend.

## drawExternal()

Draws like a host: additive blending, a scissor and a viewport of its own, a texture on unit 0, unit 1 active, and a current color on a compatibility context.

## fill(int,int,int,int,int)

Clears that rectangle to that ARGB color through the scissor test, then turns the scissor test off.

