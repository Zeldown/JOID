---
summary: The names that a JOID GLSL source uses without declaring them, the vertex attributes, the matrices and the lighting that JOID provides, and the fragment output.
---

A shader reads `aPosition` or writes `fragColor` without a declaration: [[ShaderSource]] notes which built-in names each stage uses, and [[GlslShaderTranslator]] declares those, plus `fragColor` in every fragment stage.
A declaration of a built-in name in the source is ignored.
The attributes take their location and their format from [[VertexAttribute]]; [[Shader#builtins]] writes the uniforms before each draw.

Learn: [Write JOID GLSL](/use/learn/going-further/shaders/#write-joid-glsl), [Shader translation](/build/shader-translation/#from-source-to-program).

## of(String)

Returns the built-in with this GLSL name, or `null` when the name is not a built-in.

## getIdentifier()

Returns the name used in the GLSL source, such as `aPosition`.

## getType()

Returns the GLSL type, such as `vec3` or `mat4`.

## getKind()

Returns whether the built-in is a vertex attribute, a uniform or the fragment output.

## POSITION

`vec3 aPosition`: the position of the vertex, in design pixels in a UI, before the model-view and projection matrices.

## TEXTURE_COORDINATE

`vec2 aTexCoord`: the texture coordinate of the vertex.

## COLOR

`vec4 aColor`: the color of the vertex, or the current color of the render state for a buffer without colors.

## NORMAL

`vec3 aNormal`: the normal of the vertex, for lit 3D models.

## PROJECTION_MATRIX

`mat4 uProjectionMatrix`: the projection of the render bridge.

## MODEL_VIEW_MATRIX

`mat4 uModelViewMatrix`: the model-view matrix of the render bridge.

## NORMAL_MATRIX

`mat3 uNormalMatrix`: the matrix that transforms the normals for the model-view matrix.

## LIGHTING

`bool uLighting`: `true` while the render state has lighting on.

## FRAGMENT_COLOR

`vec4 fragColor`: the color that the fragment stage writes.
