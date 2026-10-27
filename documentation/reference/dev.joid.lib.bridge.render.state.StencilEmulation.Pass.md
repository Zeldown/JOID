---
summary: Which stencil code a translated fragment stage carries.
---

`stencil(...)` of [[GlslShaderTranslator]] takes it to add the code of [[StencilEmulation]] to the fragment stage.

Learn: [Stencil emulation](/build/shader-translation/#stencil-emulation).

## NONE

No stencil code: the backend uses its own stencil buffer, or none.

## TEST

The fragment stage discards the fragments that fail the comparison against the stencil texture.
A translator set to `TEST` also gives the `WRITE` stage in [[ShaderTranslation#getStencilFragment]].

## WRITE

The fragment stage outputs, in its red channel, the stencil value after the fail or the pass operation, to draw into the stencil texture.
