---
summary: The attributes of a JOID vertex, each with its shader input, its location, its byte offset and its format.
---

A backend declares its vertex input from these values: one binding of `VertexBuffer.STRIDE` bytes, one attribute per constant.
[[GlslShaderTranslator]] writes the same locations with `layout(location = n)` for a dialect with explicit locations; for the other dialects, the backend binds them when it links the program.

Learn: [Vertices](/build/pipeline/#vertices).

## of(ShaderBuiltin)

Returns the attribute that feeds a built-in input of the shader, or `null` when the built-in is not a vertex attribute.

## getBuiltin()

Returns the built-in input of the shader that receives the attribute.

## getLocation()

Returns the location of the attribute in the vertex input of the shader.

## getOffset()

Returns the offset of the attribute in a vertex, in bytes.

## getComponents()

Returns the number of components of the attribute.

## getComponent()

Returns the type of each component.

## isNormalized()

If `true`, the integer components reach the shader scaled to `0` to `1` when unsigned, or `-1` to `1` when signed.

## POSITION

The position: three floats at offset `0`, location `0`.

## TEXTURE_COORDINATE

The texture coordinate: two floats at offset `12`, location `1`.

## COLOR

The color: four normalized unsigned bytes, red, green, blue then alpha, at offset `20`, location `2`.

## NORMAL

The normal: three normalized signed bytes at offset `24`, location `3`.
