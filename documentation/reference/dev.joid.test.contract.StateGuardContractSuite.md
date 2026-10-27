---
summary: The contract tests of a render bridge that shares its graphics context with a host: every frame must look the same and give the host its state back, whatever the host left set.
---

Each test draws a frame on a clean state, sets one [[StateTrap]], draws the frame again and compares both, pixel for pixel; it also checks the state of the host before, during and after a drawing of the host.
A trap that the context lacks is skipped.

Learn: [The suites](/build/testing-a-backend/#the-suites), [Check the guard](/build/embedding-in-a-host/#check-the-guard).

## StateGuardContractSuite()

Creates the suite, for a subclass.

## drawsThroughTheScissorSetBeforeJoid()

Checks the frame with a scissor test left on by the host.

## drawsThroughTheLogicOperationSetBeforeJoid()

Checks the frame with a logic operation left on by the host.

## drawsThroughTheAlphaTestSetBeforeJoid()

Checks the frame with an alpha test left on by the host.

## drawsWhateverTheReadFrameBufferSetBeforeJoid()

Checks the frame with a read framebuffer bound by the host.

## rastersANativeDrawingUprightAtItsPixelSize()

Checks that a drawing of the host inside `DrawUtils.RASTER` lands upright at the pixel size of its area.

## givesTheHostItsStateBackAroundARasterNestedInItsDrawing()

Checks that a raster nested in a drawing of the host gives the host its state back.

## startBackend()

Creates a new snapshot backend before each test, at 64×64 pixels.

## stopBackend()

Releases the pooled framebuffers and destroys the snapshot backend after each test.

## leavesACleanStateAsItWas()

Checks that a frame of JOID leaves a clean state of the host as it found it.

## restoresTheBlendingSetBeforeJoid()

Checks the frame with a blending set by the host.

## cullsWhateverTheFrontFaceSetBeforeJoid()

Checks the frame with the front face of the host reversed.

## uploadsWhateverThePixelStoreSetBeforeJoid()

Checks the uploads with pixel storage parameters set by the host.

## uploadsWhateverThePixelBufferSetBeforeJoid()

Checks the uploads with a pixel buffer bound by the host.

## fillsWhateverThePolygonModeSetBeforeJoid()

Checks the frame with a polygon mode set by the host.

## keepsTheVertexArraySetBeforeJoid()

Checks the frame with a vertex array bound by the host, and that the host gets it back.

## keepsTheClientArraysSetBeforeJoid()

Checks the frame with client vertex arrays set by the host, and that the host gets them back.

## keepsTheMaterialSetBeforeJoid()

Checks the frame with color material left on by the host.

## testsTheDepthWhateverTheDepthFunctionSetBeforeJoid()

Checks the depth test with a depth function set by the host.

## samplesWhateverTheSamplerObjectsSetBeforeJoid()

Checks the sampling with sampler objects bound by the host.

## keepsTheTextureParametersSetBeforeJoid()

Checks that the parameters of a texture of the host survive the frame.

## survivesEveryTrapAtOnce()

Checks the frame with every trap set at once.

## masksWhateverTheStencilMaskSetBeforeJoid()

Checks the frame with a stencil write mask set by the host.

## createBackend()

JOID calls it before every test.
Return a new snapshot backend of your backend that implements [[IStateGuardBackend]].
