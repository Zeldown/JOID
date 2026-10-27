---
summary: Wraps an OpenGL binding so that the context looks like a snapshot profile: hidden extensions and an older GLSL version.
---

Learn: [OpenGL profiles](/build/backends/lwjgl3/#opengl-profiles).

## from(IGlBinding,SnapshotProfile)

Returns a binding that hides the extensions of the profile and reports its GLSL version, and passes every other call to the binding.
