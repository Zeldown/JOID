---
summary: A hidden, undecorated GLFW window of a fixed size, on which the snapshot backends render.
---

It ships in the dev and demo jars of the backends; the published `joid-base-glfw` jar does not hold it.

Learn: [Hidden windows](/build/base-modules/glfw/#hidden-windows).

## create(int,int,Runnable)

Initializes GLFW, runs `hints` to set the window hints of the backend, then creates a hidden window of that size in GLFW window coordinates; on a high-density macOS screen, its framebuffer is larger.
Throws an `IllegalStateException` when GLFW cannot start, often for lack of a display, or when the driver refuses the window.

## destroy()

Destroys the window.

## getWindow()

Returns the GLFW handle of the window.
