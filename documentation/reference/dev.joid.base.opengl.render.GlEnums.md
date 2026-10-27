---
summary: Converts the render states of JOID to the OpenGL values that the binding takes.
---

Learn: [base-opengl](/build/base-modules/opengl/).

## mode(Primitive)

Returns `LINES` or `TRIANGLES`.

## type(VertexComponent)

Returns the OpenGL type of a vertex component: unsigned byte, byte or float.

## wrap(TextureWrap)

Returns the OpenGL wrap mode.

## magFilter(TextureSampling)

Returns `LINEAR` or `NEAREST`, after the filter of the sampling.

## minFilter(TextureSampling)

Returns `LINEAR_MIPMAP_LINEAR` for a sampling that reads mipmaps, otherwise the magnification filter.

## equation(Equation)

Returns the OpenGL blend equation.

## factor(Factor)

Returns the OpenGL blend factor.

## function(StencilFunction)

Returns the OpenGL comparison function of the stencil test.

## operation(StencilOperation)

Returns the OpenGL stencil operation.
