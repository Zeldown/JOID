---
summary: How a translated program declares its uniforms, in one std140 block or one by one.
---

A backend chooses it when it creates its [[GlslShaderTranslator]].
In both layouts, the [[Shader]] keeps the values in the same [[UniformBlock]]; only the way the backend sends them changes.

Backend note: the OpenGL backends use `LOOSE`, Vulkan uses `BLOCK`.

Learn: [Pick a uniform layout](/build/shader-translation/#pick-a-uniform-layout).

## BLOCK

Declares every uniform in one std140 block; the backend packs it with [[UniformBlock#pack]] and sends the whole buffer.
It needs a dialect with uniform blocks.

## LOOSE

Declares each uniform on its own; the backend sends each changed member with [[UniformBlock#upload]].
It works in every dialect.
