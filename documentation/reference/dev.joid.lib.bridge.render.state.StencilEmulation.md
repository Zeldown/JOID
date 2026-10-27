---
summary: Stencil test and update done in the fragment shader, for a backend whose targets have no stencil buffer.
---

`StencilEmulation` turns the [[StencilState]] of the render state into what a shader translated with a stencil pass needs: whether to test, whether to write, and the values of its uniforms.
Draw the `WRITE` pass with the pipeline of [[PipelineKey#stencil]] and the stencil fragment of the [[ShaderTranslation]], then the draw itself.
No backend of JOID uses it: OpenGL and Vulkan have a stencil buffer on the window.

Learn: [Stencil emulation](/build/shader-translation/#stencil-emulation).

## from(RenderState,boolean)

Reads the stencil of a render state.
Pass `screen` as `true` when the target is the window: off screen, nothing is tested nor written, as the core never uses a stencil in a framebuffer.

## write(UniformBlock)

Writes the stencil uniforms of [[GlslShaderTranslator]] (`STENCIL_TEST`, `STENCIL_FUNCTION`, `STENCIL_REFERENCE`, `STENCIL_MASK`, `STENCIL_FAIL` and `STENCIL_PASS`) into the block of the shader.
Call it before the block is sent for a draw; it throws `IllegalArgumentException` on a shader translated without stencil emulation.

## isTest()

If `true`, the draw tests the stencil: the target is the window and the stencil of the state is on.

## isWrite()

If `true`, the draw also changes the stencil, so it needs the `WRITE` pass first: the test is on and the fail or the pass operation is not `KEEP`.

## getMask()

Returns the mask of the state, reduced to 8 bits.

## getReference()

Returns the reference of the state, reduced to 8 bits.

## getFunction()

Returns the comparison of the state.

## getFail()

Returns the operation when the comparison fails.

## getPass()

Returns the operation when the comparison passes; the emulation has no depth-fail case.
