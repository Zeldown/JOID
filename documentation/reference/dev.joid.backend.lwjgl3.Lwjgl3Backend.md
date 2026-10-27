---
summary: The backend for LWJGL 3, which draws with OpenGL into a GLFW window and plays sound with the OpenAL of LWJGL 3.
---

Create the window and make its context current, then register the backend; it attaches the GLFW input callbacks of the window itself.

Learn: [Start a backend](/use/learn/running/backends/#start-a-backend), [LWJGL 3 backend](/build/backends/lwjgl3/).

## create(long)

Creates the backend for the GLFW window of that handle.

## binding

The OpenGL binding that the render bridge calls; set another one before `register()` to wrap or replace the OpenGL calls.
