---
summary: The immutable stencil settings of the render state, comparison, reference, mask and the three operations.
---

The core sets it through `stencil(...)` of the [[RenderState]] for the masks: a first draw writes a region into the stencil buffer, and the next draws pass only where the comparison holds.
A backend reads it from the state at each draw and maps each value to its API, such as `glStencilFunc` and `glStencilOp` or the stencil state of a pipeline.
Two equal states compare equal.
The stencil applies to the window only: the core never uses a stencil in a framebuffer.

Learn: [Stencil](/build/framebuffers-and-depth/#stencil).

## of(StencilFunction,int,int,StencilOperation,StencilOperation,StencilOperation)

Creates a state with the stencil on.
A fragment passes when `function` holds between `reference & mask` and the stored value `& mask`; `fail` applies when the comparison fails, `depthFail` when it passes but the depth test fails, and `pass` when both pass.

## isEnabled()

If `false`, the stencil is ignored: every fragment passes and the buffer is never written.

## getFunction()

Returns the comparison between the reference and the stored value.

## getReference()

Returns the value compared with the stored one, and written by `REPLACE`.

## getMask()

Returns the bits that the comparison reads.

## getFail()

Returns the operation applied to the stored value when the comparison fails.

## getDepthFail()

Returns the operation applied when the comparison passes and the depth test fails.

## getPass()

Returns the operation applied when the comparison and the depth test pass.

## DISABLED

The state with the stencil off, the stencil of a fresh render state.

## equals(Object)

If `true`, the other object is of the same class with the same switch, function, reference, mask and operations.

## hashCode()

Returns a hash of the values that `equals` compares.
