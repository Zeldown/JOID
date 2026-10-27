---
summary: The GLSL types that a uniform can have, with the component and column counts that set its std140 size and the values that it accepts.
---

[[UniformMember]] uses the type to lay the member out and to check each value: the integer types take `int` values, `bool` and `int` also take `boolean` values, and the float types take floats or integers.
Samplers are not in this list: they become [[UniformSampler]] objects.

Learn: [Pick a uniform layout](/build/shader-translation/#pick-a-uniform-layout).

## of(String)

Returns the type with this GLSL name.
Throws `IllegalArgumentException` for a type outside this list, such as a struct.

## isMatrix()

If `true`, the type is a square matrix, laid out as columns of 16 bytes.

## getCount()

Returns the number of 32-bit values in one element: the components times the columns, `16` for a `mat4`.

## getIdentifier()

Returns the GLSL name of the type, such as `vec3`.

## getComponents()

Returns the number of components of a scalar or a vector, or the number of rows of a matrix.

## getColumns()

Returns the number of columns of a matrix, or `0` for a scalar or a vector.

## isInteger()

If `true`, the type stores integers (`int`, `uint`, `bool` and their vectors) and refuses floats.

## INT

`int`, a signed integer.

## UINT

`uint`, an unsigned integer, which needs GLSL 1.30.

## BOOL

`bool`, stored as the integer `0` or `1`.

## FLOAT

`float`.

## VEC2

`vec2`, two floats.

## VEC3

`vec3`, three floats, aligned on 16 bytes.

## VEC4

`vec4`, four floats.

## IVEC2

`ivec2`, two signed integers.

## IVEC3

`ivec3`, three signed integers.

## IVEC4

`ivec4`, four signed integers.

## UVEC2

`uvec2`, two unsigned integers.

## UVEC3

`uvec3`, three unsigned integers.

## UVEC4

`uvec4`, four unsigned integers.

## BVEC2

`bvec2`, two booleans stored as integers.

## BVEC3

`bvec3`, three booleans stored as integers.

## BVEC4

`bvec4`, four booleans stored as integers.

## MAT2

`mat2`, a 2×2 float matrix, written column by column.

## MAT3

`mat3`, a 3×3 float matrix, written column by column.

## MAT4

`mat4`, a 4×4 float matrix, written column by column.
