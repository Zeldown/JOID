---
summary: The OpenGL contexts on which the snapshot tests of LWJGL 3 run, each a version, a profile and the extensions it hides.
---

On Mesa, `MESA_GL_VERSION_OVERRIDE` and `MESA_GLSL_VERSION_OVERRIDE` give the version that a profile needs.

Learn: [OpenGL profiles](/build/backends/lwjgl3/#opengl-profiles).

## current()

Returns the profile named by the system property `joid.snapshot.profile`, `DEFAULT` when it is not set.

## check(GlCapabilities)

Throws an `IllegalStateException` when the context does not have the version and the profile of the profile; `DEFAULT` accepts every context.

## getVersion()

Returns the OpenGL version times 100 that the profile needs, `0` for any.

## getProfile()

Returns the OpenGL profile that the profile needs, or `null` for any.

## getRequest()

Returns the context request of the hidden window.

## getShadingLanguageVersion()

Returns the GLSL version that the binding reports instead of the driver's, or `null` to keep it.

## getHiddenExtensionList()

Returns the extensions that the binding hides, read-only.

## DEFAULT

The context of `CORE_33`, whatever its version: the profile of a plain test run.

## GL_20

OpenGL 2.0 compatibility with GLSL 1.10, which JOID refuses: it tests that registering the backend throws.

## GL_21

OpenGL 2.1 compatibility, the oldest context that JOID draws on.

## GL_21_EXT

OpenGL 2.1 without `GL_ARB_framebuffer_object` and `GL_ARB_vertex_array_object`: the EXT framebuffers and no vertex arrays.

## GL_21_EXT_NO_BLIT

`GL_21_EXT` without `GL_EXT_framebuffer_blit` either: the mipmaps are drawn.

## GL_30

OpenGL 3.0 compatibility.

## GL_32_FORWARD_COMPATIBLE

OpenGL 3.2 forward-compatible core.

## GL_33

OpenGL 3.3 core.

## GL_45_COMPATIBILITY

OpenGL 4.5 compatibility, with the fixed pipeline next to the shaders.
