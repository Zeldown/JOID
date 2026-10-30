---
summary: Reads the state of an OpenGL context that the state guard must preserve, by name.
---

It ships in the `snapshot` jar of base-opengl and in the dev and demo jars of the backends; the published `joid-base-opengl` jar does not hold it.

Learn: [Tools for hosts and tests](/build/base-modules/opengl/#tools-for-hosts-and-tests).

## read(IGlBinding,GlCapabilities,MaterialReader)

Returns the state of the current context, read through the binding: capabilities, bindings, blending, depth, stencil, pixel storage, viewport, scissor, the first four texture units and the vertex attributes, and the fixed-function state of a compatibility context, whose materials come from the reader.
It leaves the active texture unit as it found it.
