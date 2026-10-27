---
summary: A GLSL feature that only some dialects accept, with the first desktop and ES versions that have it and the pattern that finds it in a source.
---

[[ShaderSource]] finds the features of each stage.
[[GlslShaderTranslator]] refuses a stage that uses a feature its [[GlslDialect]] lacks, and in dev mode the constructor of [[Shader]] warns once about each feature beyond GLSL 1.20, since such a shader does not compile on every backend.

Learn: [Pick a dialect](/build/shader-translation/#pick-a-dialect).

## find(CharSequence)

Returns the features that a piece of GLSL code uses.
The code is searched as it is: strip the comments first, or a word in a comment counts.

## getDescription()

Returns the name of the feature in messages, such as `flat varyings`.

## getGlsl()

Returns the first desktop GLSL version that accepts the feature.

## getEssl()

Returns the first GLSL ES version that accepts the feature.

## getPattern()

Returns the regular expression that finds the feature in GLSL code.

## FLAT_VARYINGS

The `flat` qualifier, which turns off the interpolation of a varying.

## UNSIGNED_INTEGERS

The `uint` and `uvec` types.

## BITWISE_OPERATORS

The `<<`, `>>`, `&`, `|`, `^`, `~` and `%` operators.

## SWITCH

The `switch` statement.

## TEXEL_FETCH

The `texelFetch` and `texelFetchOffset` functions, which read a texel at integer coordinates.

## TEXTURE_SIZE

The `textureSize` function.

## DERIVATIVES

The `dFdx`, `dFdy` and `fwidth` functions: in every desktop dialect, and from ESSL 3.00 in GLSL ES.
