---
summary: Holds every uniform value of a shader at its std140 offset, tracks which ones changed, and packs or hands them to the backend before a draw.
---

Each [[Shader]] owns one block, created by [[GlslShaderTranslator#createBlock]] from the uniforms of both stages.
`IShader.uniform` writes the values of the shader author, [[Shader#builtins]] writes those of JOID, and the backend sends them as its [[UniformLayout]] requires.

Learn: [Pick a uniform layout](/build/shader-translation/#pick-a-uniform-layout).

## create(List,String)

Lays out the variables one after the other with the std140 rules and allocates the buffer, rounded up to a multiple of 16 bytes and at least 16 bytes.
`code` is the GLSL of both stages, where JOID reads the `#define` that gives an array length written as a name.
Throws `IllegalArgumentException` for a type outside [[UniformType]] or a length that is neither a number nor a `#define`.

## value(String,boolean)

Writes a boolean into the member `name`.
Throws `IllegalArgumentException` when the block has no such member, or when the member is not a single `bool` or `int`.

## value(String,int[])

Writes integers into the member `name`; a float member receives them as floats.
Throws `IllegalArgumentException` when the block has no such member, or when the count does not fit its type.

## value(String,float[])

Writes floats into the member `name`.
Throws `IllegalArgumentException` when the block has no such member, when the member stores integers, or when the count does not fit its type.

## pack()

Copies the changed members into `getData()` at their offsets, matrix columns and array elements on their strides, and marks them as sent.
Returns `true` when at least one member changed, so that the backend knows whether to upload the buffer again.

## upload(Consumer)

Gives each changed member to `uploader`, then marks it as sent.

## getSize()

Returns the size of the std140 buffer in bytes, a multiple of 16.

## getMemberMap()

Returns the members by name, in block order, as a view that cannot be changed.

## getMember(String)

Returns the member called `name`, or `null` when the block has none.

## requireMember(String)

Returns the member called `name`.
Throws `IllegalArgumentException` with the names of the block when it has none.

## getData()

Returns the std140 buffer, in native byte order, that `pack()` fills.
