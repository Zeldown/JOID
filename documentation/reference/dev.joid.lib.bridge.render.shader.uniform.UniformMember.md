---
summary: One uniform of a block, its std140 place, its current value as raw 32-bit words, and whether it changed since the backend last sent it.
---

A member checks each value against its [[UniformType]] and throws `IllegalArgumentException`, with its declaration in the message, when the value does not fit.
It turns dirty only when a written word differs from the stored one, so writing the same value again costs no upload.

Learn: [Pick a uniform layout](/build/shader-translation/#pick-a-uniform-layout).

## create(String,UniformType,int,int)

Creates a member from its name, its type, its array length (`0` for a single value) and `start`, the first free byte of the block; the member aligns its offset from there with the std140 rules.

## value(boolean)

Writes `1` or `0` into a single `bool` or `int` member.

## value(int[])

Writes integers into an integer member: exactly as many as the type holds for a single value, or one or more whole elements of an array, from the first element.
A float member receives the integers as floats.

## value(float[])

Writes floats into a float member, with the same counts as integers.

## value(UniformMember)

Copies every value of another member of the same type and length, as the line variant of a shader does to follow its source shader.

## clean()

Marks the member as sent; the block or the backend calls it after an upload.

## getDeclaration()

Returns the GLSL declaration of the member, such as `vec4 u_Color` or `float u_Weights[3]`, for messages.

## getSize()

Returns the number of bytes that the member takes in the block.

## getLength()

Returns the array length, or `0` for a single value.

## getOffset()

Returns the byte offset of the member in the std140 block.

## getName()

Returns the name of the uniform in the GLSL source.

## getArrayStride()

Returns the distance in bytes between two elements of an array, a multiple of 16, or `0` for a single value.

## getMatrixStride()

Returns the distance in bytes between two columns of a matrix, `16`, or `0` for a type that is not a matrix.

## getType()

Returns the GLSL type of the member.

## getValues()

Returns the current values, packed as 32-bit words in native byte order: what a loose upload sends as it is.

## isDirty()

If `true`, a value changed since the last upload.
