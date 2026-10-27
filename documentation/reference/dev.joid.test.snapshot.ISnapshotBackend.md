---
summary: A hidden surface on which a backend is registered, that the testkit draws on and captures.
---

Learn: [Give the testkit a surface](/build/testing-a-backend/#give-the-testkit-a-surface), [Testing your UIs](/use/learn/running/testing/).

## create(int,int)

Creates a hidden surface of that size in pixels and registers your backend on it, without audio.

## destroy()

Releases the surface and gives the registries back the bridges they had before `create`.

## present()

Ends the frame on the surface: a buffer swap or a submission.

## capture(int,int)

Returns the pixels of the last frame, before `present()`: the area of that size at the bottom-left corner of the surface.

## getRenderer()

Returns the name of the GPU and of the driver; the references are stored per renderer.
