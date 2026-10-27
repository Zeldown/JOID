---
summary: The bridge that draws every pixel of JOID: it clears targets, draws triangles and lines, and creates textures, framebuffers and shaders.
---

The core reads it from `BridgeHandler.RENDER`; [[DrawUtils]] goes through it.
A backend extends [[RenderBridge]], which implements the state, the matrices and the frame and leaves the GPU calls to write.
Every method runs on the render thread.
This node reads it to find its area in screen pixels, then lets the host draw there:

```java file="com/example/backend/HostPreviewNode.java" region="draw"
```

Learn: [Render bridge](/build/bridges/render/), [Render pipeline](/build/pipeline/).

## screen(int,int)

Targets the window: no framebuffer, a viewport of that size in screen pixels, and an orthographic projection from the top-left corner with depth from `0` to `10000`.
[[UIBridgeRegistry#frame]] calls it when the window changes size.

## quantize(double,double)

Moves the modelview so that a node that moves by that offset, in the current coordinates, lands on whole screen pixels.

## pushState()

Saves a copy of the render state.

## popState()

Loads the last saved copy back into the same render state instance.

## getStateDepth()

Returns the number of saved render states.

## clearDepth()

Clears the depth of the current target to `1`.

## clearStencil()

Clears the stencil of the window to `0`; does nothing when a framebuffer is the target.

## clearColor(float,float,float,float)

Clears the color of the current target, each channel from `0F` to `1F`; does nothing while color writes are off.

## draw(Primitive,VertexBuffer)

Draws the vertices as triangles or lines with the current shader, or the default one, and the current state.
Skips the draw when the shader is not active, the buffer is empty or the viewport has no area.

## createTexture()

Returns an empty texture, to `allocate` then `upload`.

## createFrameBuffer(int,int)

Returns a framebuffer of that size in pixels, with a color texture and a depth buffer.

## createShader(ShaderSource,ShaderSource,BlendState)

Returns a shader for the two parsed stages and that blend state.

## getState()

Returns the live render state.

## getPixelGrid()

Returns the screen pixel grid of the current matrices and viewport.

## getModelView()

Returns the modelview matrix stack.

## getProjection()

Returns the projection matrix stack.

## beginFrame()

Begins a frame; drawing and clearing happen only between `beginFrame()` and `endFrame()`.
Does nothing by default.

## endFrame()

Ends the frame and submits what it recorded; does nothing by default.

## isFrameActive()

If `true`, a frame is begun; `true` by default.

## suspend(Runnable)

Runs a drawing of the host with its own state, inside a JOID frame: the state and the pass in progress are set aside until the drawing returns.

## raster(IFrameBuffer,int,int,Runnable)

Runs a drawing of the host into a framebuffer of JOID at that pixel size, for [[DrawUtils#RASTER]].
Throws an `IllegalStateException` outside a frame.

## withState(Consumer)

Runs the body between `pushState()` and `popState()`, even when it throws.

## canWrap(TextureWrap)

If `true`, the API samples with that wrap; `true` by default.
When it returns `false`, JOID samples with `CLAMP_TO_EDGE`, and emulates `CLAMP_TO_BORDER` in the shaders.
