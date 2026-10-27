---
summary: The snapshot backend of LWJGL 3, which renders in a hidden GLFW window with the OpenGL profile of the test run.
---

The system property `joid.snapshot.profile` picks a [[SnapshotProfile]]; it ships in the dev and demo jars of the backend.

Learn: [Give the testkit a surface](/build/testing-a-backend/#give-the-testkit-a-surface), [OpenGL profiles](/build/backends/lwjgl3/#opengl-profiles).

## Lwjgl3SnapshotBackend()

Creates the snapshot backend; the window and the surface are created when a run starts.

## create(int,int)

Opens a hidden window of that size in pixels with the context of the profile, registers the backend without audio and without vsync, and checks that the context matches the profile.

## destroy()

Destroys the window and gives the registries back the bridges they had before `create`.

## present()

Swaps the buffers of the window.

## capture(int,int)

Returns the pixels of the back buffer.

## getRenderer()

Returns the renderer string of the driver.

## getProfile()

Returns the profile of the run.

## getBridge()

Returns the OpenGL render bridge.

## getWindow()

Returns the hidden window.
