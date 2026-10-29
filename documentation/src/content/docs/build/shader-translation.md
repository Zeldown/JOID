---
title: "Shader translation"
description: "How the core translates JOID GLSL into the dialect of a backend, what it adds to every shader, and how the uniforms reach the GPU."
genre: build
sidebar:
  order: 2
---

JOID writes each shader once, in JOID GLSL, and the core translates it for each backend.
A backend never edits shader code: it picks a dialect and a uniform layout, compiles what the translator gives it, and sends the uniform values at each draw.
The language itself and its built-in names are on [Write JOID GLSL](/use/learn/going-further/shaders/#write-joid-glsl).

## From source to program

1. [[ShaderSource#from]] reads one stage: it drops `#version`, collects the `in`, `out` and `uniform` declarations and the samplers, notes which built-in names the code uses, and detects the [[ShaderFeature]]s it needs.
2. Your render bridge receives both stages in `createShader(vertex, fragment, blend)` and builds its [[Shader]] subclass with a [[GlslShaderTranslator]].
3. The constructor of [[Shader]] builds the [[UniformBlock]] and the samplers, numbered from unit 1, and translates both stages into a [[ShaderTranslation]]: the dialect, the vertex code, the fragment code and, with stencil emulation, a second fragment.
4. The first call of `isActive()` calls your `compileProgram(translation)`.

[[CoreShader]] lists the shaders of the core (default, font, bitmap, rounded, border, shadow, blur, circle, gradient, line), read from `/assets/shaders/<name>/<name>.vsh` and `.fsh`.

## Pick a dialect

[[GlslDialect]] covers GLSL 1.10 to 4.50 and ESSL 1.00 and 3.00.
The dialect decides how the translator writes each declaration:

| Capability | From | Below it, the translator writes |
|---|---|---|
| `in` and `out` | GLSL 1.30, ESSL 3.00 | `attribute`, `varying`, `gl_FragColor` and `texture2D` |
| Uniform blocks | GLSL 1.40, ESSL 3.00 | loose uniforms only |
| Explicit locations | GLSL 3.30, ESSL 3.00 | no `layout(location)`; the OpenGL module binds the locations before linking |
| Implicit conversions | GLSL 1.20, never on ESSL | integer literals rewritten as floats where a float is expected; an integer variable still needs `float(...)` |

The OpenGL module picks the highest desktop dialect of the context, up to GLSL 3.30.
The Vulkan backend uses GLSL 4.50 with [[VulkanShaderTranslator]], which adds the `binding` and `location` qualifiers of SPIR-V.

A shader that uses a [[ShaderFeature]] that the dialect lacks ([Write JOID GLSL](/use/learn/going-further/shaders/#write-joid-glsl) lists them), or the derivatives on ESSL 1.00, fails to translate with an `UnsupportedOperationException` that names the feature and the dialect it needs.
In dev mode, the core also warns once about any feature beyond GLSL 1.20, which every OpenGL 2.1 context accepts, so that a shader written on a recent machine still runs everywhere.

## Pick a uniform layout

| [[UniformLayout]] | Declares | Send the values with |
|---|---|---|
| `BLOCK` | One `std140` block named `JoidUniforms` | [[UniformBlock#pack]], which returns `true` when a value changed, then the bytes of [[UniformBlock#getData]] |
| `LOOSE` | One `uniform` per value | [[UniformBlock#upload]], which calls you for each changed [[UniformMember]] |

`BLOCK` needs a dialect with uniform blocks: the constructor of [[GlslShaderTranslator]] throws an `IllegalArgumentException` otherwise.
The [[UniformBlock]] computes the `std140` offsets and strides of every [[UniformType]], scalars, vectors and matrices, arrays included, and marks each member dirty when its value changes.

To change the declarations for your API, extend [[GlslShaderTranslator]] and override its hooks:

| Hook | Writes |
|---|---|
| `getBlockLayout()` | The qualifiers of the uniform block, `std140` by default (Vulkan writes `std140, binding = 0`). |
| `declareSampler(sampler, unit)` | The declaration of a sampler and its unit. |
| `declareAttribute(builtin, location)` | The declaration of a vertex attribute and its location. |
| `declareVarying(varying, location, output)` | The declaration of a varying, as an output of the vertex stage or an input of the fragment stage. |

## What the translator adds

Besides the declarations, each translated shader carries uniforms that the core fills from the state at each draw: [[Shader#builtins]] fills all of them but the stencil ones, which [[StencilEmulation#write]] fills.

| Uniform | Filled from | Effect |
|---|---|---|
| `joid_AlphaTest`, `joid_AlphaThreshold` | `alphaCutoff` | The fragment is discarded when its alpha is at most the threshold. |
| `joid_CurrentColor`, `joid_VertexColor` | `color(...)`, `isColor()` | Every read of `aColor` becomes the vertex color, or the current color when the buffer has none. |
| `joid_LineWidth`, `joid_LineViewport` | `lineWidth` multiplied by the scale of the [[PixelGrid]], viewport | The line variant of the shader widens each segment in screen space and smooths its edges. |
| `joid_Border_<sampler>` | the wrap of the sampler | With border emulation, a sample outside the texture fades to transparent. |
| `joid_Stencil…` | the stencil state | With stencil emulation, the stencil test and write in the fragment shader. |

### Lines

[[IShader#getLineShader]] derives a second shader from the same sources: its vertex stage receives both ends of each segment and expands them into a quad of the requested width.
The tessellator binds it for wide or smooth lines; a bridge receives ordinary triangles.

### Border emulation

When [[IRenderBridge#canWrap]] refuses `CLAMP_TO_BORDER`, the constructor of [[Shader]] turns on `clampToBorder` in the translator.
Each `texture(sampler, uv)` then goes through a function that returns transparent outside `[0, 1]`, with a filtered edge for a `LINEAR` sampler, so the result matches a real transparent border.

### Stencil emulation

An API without a stencil buffer on its targets can emulate it.
[[StencilEmulation.Pass#TEST]], given to `translator.stencil(...)`, makes the fragment shader read a stencil texture (`joid_Stencil`) and discard what fails the test; the translation then carries `getStencilFragment()`, the pass that writes the new stencil value as a color.
[[StencilEmulation#from]] computes the test and the operations from the state, and `write(block)` puts them into the uniforms.
The emulation needs GLSL 1.30, and no official backend uses it: OpenGL and Vulkan have a real stencil buffer on the screen.

## Check your translation

[[GlslExport]], in the testkit, writes every core shader translated into every dialect into a folder, to read them or to pass them to an external compiler such as `glslangValidator`.
The render contract suite compiles every core shader on your backend (`compilesCoreShaders`), and checks that a shader that fails to compile is skipped ([Testing a backend](/build/testing-a-backend/)).
