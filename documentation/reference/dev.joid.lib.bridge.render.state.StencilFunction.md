---
summary: The comparison of a stencil test between the masked reference and the masked stored value.
---

Each value matches the OpenGL function of the same name, with the reference on the left: `LESS` passes when the reference is lower than the stored value.
The order of the constants is part of the contract: [[StencilEmulation]] sends their ordinal to the shader.

Learn: [Stencil](/build/framebuffers-and-depth/#stencil).

## NEVER

The test always fails.

## LESS

Passes when the reference is lower than the stored value.

## LESS_EQUAL

Passes when the reference is lower than or equal to the stored value.

## GREATER

Passes when the reference is greater than the stored value.

## GREATER_EQUAL

Passes when the reference is greater than or equal to the stored value.

## EQUAL

Passes when the reference equals the stored value.

## NOT_EQUAL

Passes when the reference differs from the stored value.

## ALWAYS

The test always passes.
