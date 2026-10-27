---
summary: The JOID GLSL shaders of the core, which draw text, shapes, lines and the built-in effects on every backend.
---

Each constant names a folder of `/assets/shaders/` in the core jar, with a `.vsh` vertex stage and a `.fsh` fragment stage written in JOID GLSL.
A backend translates and compiles all of them: `compilesCoreShaders` of the render contract suite checks it.

Learn: [Shader translation](/build/shader-translation/#from-source-to-program).

## open(ShaderStage)

Opens the source file of one stage from the classpath.
Throws `IllegalStateException` when the file is missing, which means an incomplete JOID jar.

## read(ShaderStage)

Reads the source of one stage into a [[ShaderSource]].

## create(BlendState)

Creates the shader through the render bridge of `BridgeHandler.RENDER`, with this blending.

## BITMAP

Draws the cells of a bitmap font.

## BLUR

Blurs a texture along one axis, for [[BlurNodeEffect]] and its shader pass.

## BORDER

Outlines the opaque pixels of a texture, outside or inside their edge, for [[BorderNodeEffect]] and its shader pass.

## CIRCLE

Draws an antialiased disc filled with a color or a gradient, and cuts a texture to a disc for [[CircleNodeEffect]].

## DEFAULT

Draws vertices with their color and the texture of the render state; the render bridge uses it when no shader is bound.

## FONT

Draws MSDF text.

## GRADIENT

Fills a shape with a linear gradient.

## LINE

Draws the wide and the smooth lines, through its line variant.

## ROUNDED

Cuts a texture, a color or a gradient to a rounded rectangle, filled or outlined, for the rounded shapes and [[RoundedNodeEffect]].

## SHADOW

Draws the blurred shadow of a rounded rectangle, for [[ShadowNodeEffect]].
