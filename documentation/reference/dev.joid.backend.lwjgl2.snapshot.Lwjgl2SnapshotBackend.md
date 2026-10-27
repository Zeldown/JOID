---
summary: The snapshot backend of LWJGL 2, which renders in an offscreen OpenGL context.
---

It renders into a pixel buffer, or into a `Display` on Linux, and ships in the dev and demo jars of the backend.

Learn: [Give the testkit a surface](/build/testing-a-backend/#give-the-testkit-a-surface).

## create(int,int)

Installs the natives, creates the OpenGL context of that size in pixels, with 24-bit depth and 8-bit stencil, and registers the backend without audio.
Throws an `IllegalStateException` when LWJGL 2 cannot create the context.

## destroy()

Destroys the context and gives the registries back the bridges they had before `create`.

## present()

Does nothing: the capture reads the back buffer.

## capture(int,int)

Returns the pixels of the back buffer.

## getRenderer()

Returns the renderer string of the driver.

## getBuffer()

Returns the pixel buffer of the context, or `null` when it renders into the `Display`.

## getBridge()

Returns the OpenGL render bridge.
