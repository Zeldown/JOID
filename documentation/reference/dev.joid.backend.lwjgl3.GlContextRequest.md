---
summary: The OpenGL context that a GLFW window asks for, set as window hints before the window exists.
---

Every request also asks for a 24-bit depth buffer and an 8-bit stencil buffer.

Learn: [Start a backend](/use/learn/running/backends/#start-a-backend), [Context requests](/build/backends/lwjgl3/#context-requests).

## apply()

Sets the GLFW window hints of the request; call it after `glfwInit()` and before `glfwCreateWindow`.
On macOS, a core profile is always forward-compatible.

## getMajor()

Returns the major OpenGL version asked for, `0` for the default context of the driver.

## getMinor()

Returns the minor OpenGL version asked for.

## isForwardCompatible()

If `true`, the request asks for a forward-compatible context.

## COMPATIBILITY

The default context of the driver, for an application that also draws with the fixed pipeline.

## CORE_32_FORWARD_COMPATIBLE

An OpenGL 3.2 forward-compatible core profile, for a driver that offers no other core profile.

## CORE_33

An OpenGL 3.3 core profile, the usual request.
