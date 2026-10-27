---
summary: The contract tests of a render bridge, run on your snapshot backend in a few seconds.
---

Extend it in the tests of your backend and return your snapshot backend from `createBackend()`; every test then runs on your render bridge.
Each test name states the rule it checks; the rules are those of the render contract.

Learn: [The suites](/build/testing-a-backend/#the-suites), [Render bridge](/build/bridges/render/#guarantees).

## drawsVertexColors()

Checks that a quad takes the colors of its vertices.

## drawsWithTheCurrentColor()

Checks that a quad without vertex colors takes the color of the render state.

## drawsEveryFrameBegunBeforeThePresent()

Checks that two `beginFrame()`/`endFrame()` pairs before one present both reach the target.

## refusesToDrawOutsideAFrame()

Checks that `draw`, `clearColor`, `clearDepth` and `raster` throw an `IllegalStateException` outside a frame.

## skipsTheDrawsOfAShaderThatFailsToCompile()

Checks that a shader that does not compile reports `isActive() == false`, prints one `[JOID]` warning that names the compilation, and draws nothing while the next draws go on.

## samplesEveryTextureOfTheFirstDrawOfABackend()

Checks that the first draw of a backend binds a texture on every sampler, the 1×1 white texture on those without one.

## readsTheLightingOfEachDraw()

Checks that the `uLighting` built-in follows the render state at each draw.

## appliesTheStencilStateOfEachDraw()

Checks that a draw writes the stencil and a later draw is masked by it.

## drawsLinesOnePixelWide()

Checks that `LINES` are one pixel wide.

## drawsAWideLineAsWideAsItsWidth()

Checks that a line of width 6 covers 6 pixels.

## drawsAWideLineWithTheBoundShader()

Checks that a wide line draws with the shader bound by the application, through its line variant.

## drawsTheScreenAfterAFrameBufferDeletedDuringItsPass()

Checks that the window still draws after a framebuffer is deleted in the middle of its own pass.

## rastersADrawingUprightAtItsPixelSize()

Checks that `DrawUtils.RASTER` runs the drawing at the pixel size of its area, upright.

## startBackend()

Creates the snapshot backend at the first test of the class, at 64×64 pixels, and resets the matrices and the state before each test.

## stopBackend()

Destroys the snapshot backend after the last test of the class.

## refusesATextureLargerThanTheBackend()

Checks that allocating a texture larger than the API allows throws an `IllegalArgumentException` that starts with `A texture of 1048576x1 exceeds the maximum size`.

## compilesCoreShaders()

Checks that every built-in shader of JOID compiles.

## appliesTheUniformsSetBeforeTheShaderIsBound()

Checks that a uniform set before `bind()` reaches the draw.

## appliesEveryUniformType()

Checks the floats, vectors, integers, booleans, matrices and arrays of uniforms.

## samplesTheTextureOfASampler()

Checks that a shader samples the texture set on its sampler.

## litsAFaceTheSameAtAnyScale()

Checks that the lighting of a face does not change with the scale of the modelview.

## disablesTheAlphaTestAtZero()

Checks that an alpha cutoff discards transparent fragments, and that a cutoff of `0F` discards none.

## cullsTheBackFacesLikeOpenGl()

Checks that culling removes the faces that OpenGL removes, on the window.

## cullsTheBackFacesInFrameBuffersLikeOpenGl()

Checks that culling removes the same faces in a framebuffer.

## exposesTheLineState()

Checks that the render state keeps the line width and the smooth flag.

## joinsTheSegmentsOfAWideLine()

Checks that the segments of a wide line strip join without a gap at the corner.

## uploadsArgbTextures()

Checks that an uploaded ARGB texture keeps its colors and its rows from the top.

## minifiesThroughMipmaps()

Checks that a mipmapped texture, mipmapped before or after its upload, minifies to the average of its texels.

## samplesATransparentBorderBeyondTheTexture()

Checks that `CLAMP_TO_BORDER` fades to transparent beyond the edges, native or emulated.

## deletesTexturesTwice()

Checks that deleting a texture twice does not throw.

## restoresTheStateOnPop()

Checks that `popState()` restores the framebuffer, the shader, the viewport and the lines.

## rendersIntoFrameBuffers()

Checks that a framebuffer is cleared and drawn into, then sampled on the window.

## hidesFarFacesBehindNearOnes()

Checks the depth test: a far quad drawn after a near one stays hidden.

## clearsTheDepth()

Checks that `clearDepth()` lets a far quad draw again.

## testsTheDepthInFrameBuffers()

Checks the depth test inside a framebuffer.

## keepsTheFrameBufferDepthAcrossTargets()

Checks that the depth of a framebuffer survives a switch to the window and back.

## followsTheOpenGlProjection()

Checks that the projection of `screen` puts `(0, 0)` at the top-left corner of the window.

## resetsToAnOpaqueWhiteTexture()

Checks that a state without texture samples opaque white.

## keepsTranslationsExact()

Checks that a translation after a scale reaches the pixel grid without rounding.

## quantizesAMotionToWholePixels()

Checks that `quantize` lands a motion on whole screen pixels.

## restoresTheMatrixAfterATransformation()

Checks that applying then removing a `Transformation` gives the modelview back exactly.

## createBackend()

JOID calls it once, at the first test of the class.
Return a new snapshot backend of your backend, not created yet.
