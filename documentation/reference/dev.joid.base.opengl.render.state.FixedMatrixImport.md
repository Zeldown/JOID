---
summary: Loads the projection and modelview matrices of the fixed pipeline of a compatibility host into the matrix stacks of JOID.
---

Learn: [Import the matrices of a fixed-function host](/build/embedding-in-a-host/#import-the-matrices-of-a-fixed-function-host).

## create(GlRenderBridge)

Creates the import for the bridge.
Throws an `IllegalStateException` when the context is not a compatibility context, which has no fixed-function matrices.

## apply()

Reads the projection and the modelview matrices of OpenGL and loads them into the projection and modelview stacks of the bridge.
