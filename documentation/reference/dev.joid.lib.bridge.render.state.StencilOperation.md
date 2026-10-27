---
summary: What a draw does to the stored stencil value after the stencil and depth tests.
---

Each value matches the OpenGL operation of the same name, on an 8-bit stencil.
The order of the constants is part of the contract: [[StencilEmulation]] sends their ordinal to the shader.

Learn: [Stencil](/build/framebuffers-and-depth/#stencil).

## KEEP

Leaves the stored value as it is.

## ZERO

Sets the stored value to `0`.

## REPLACE

Sets the stored value to the reference of the [[StencilState]].

## INCREMENT

Adds one to the stored value, clamped at `255`.

## DECREMENT

Subtracts one from the stored value, clamped at `0`.

## INVERT

Flips every bit of the stored value.
