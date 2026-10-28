---
summary: The base class of a render bridge, which keeps the matrices, the render state and the frame in Java and leaves the GPU calls to its subclass.
---

A subclass implements the clears, `drawPrimitive` and the three `create...` methods, and calls `requireFrame()` before it touches the GPU in a frame.
Its `drawPrimitive` reads the render state and the samplers that the base class resolved, then draws, as [Implement the render bridge](/build/writing-a-backend/#implement-the-render-bridge) shows.

Learn: [Render bridge](/build/bridges/render/), [Implement the render bridge](/build/writing-a-backend/#implement-the-render-bridge).

## RenderBridge()

Creates the bridge with empty matrix stacks and a default render state.

## getStateDepth()

Returns the number of saved render states.

## getPixelGrid()

Returns the screen pixel grid of the current matrices and viewport ([Pixel alignment](/use/in-depth/pixel-alignment/)).

## getModelView()

Returns the modelview matrix stack.

## getProjection()

Returns the projection matrix stack.

## getState()

Returns the live render state; `popState()` loads into this same instance, so a reference to it stays valid.

## isFrameActive()

If `true`, `beginFrame()` ran and `endFrame()` has not.

## screen(int,int)

Targets the window: no framebuffer, a viewport of that size in screen pixels, and an orthographic projection from the top-left corner with depth from `0` to `10000`.

## quantize(double,double)

Translates the modelview by the distance between that motion and the same motion rounded to screen pixels; does nothing for no motion.

## pushState()

Pushes a copy of the render state.

## popState()

Loads the last pushed copy into the render state.

## clearDepth()

Calls `clearDepthBuffer()`.

## clearStencil()

Calls `clearStencilBuffer()` when the window is the target; framebuffers have no stencil.

## clearColor(float,float,float,float)

Calls `clearColorBuffer` while color writes are on.

## draw(Primitive,VertexBuffer)

Picks the shader of the state, or the default shader, and calls `drawPrimitive`, unless the shader is not active, the buffer is empty or the viewport has no area.

## beginFrame()

Marks the frame as begun and calls `beginFrameCommands()`.
Throws an `IllegalStateException` when a frame is already begun.

## endFrame()

Calls `submitFrameCommands()` and marks the frame as ended.
Throws an `IllegalStateException` when no frame is begun.

## beginFrameCommands()

JOID calls it at each `beginFrame()`; it does nothing by default.
Override it to start recording the commands of a frame, or to save the state of a host.

## submitFrameCommands()

JOID calls it at each `endFrame()`, even when the frame failed; it does nothing by default.
Override it to submit the commands of the frame, or to give a host its state back.

## clearDepthBuffer()

JOID calls it from `clearDepth()`.
Clear the depth of the current target to `1`, with depth writes on.

## clearStencilBuffer()

JOID calls it from `clearStencil()`, only when the window is the target.
Clear the stencil to `0`, with every bit writable.

## clearColorBuffer(float,float,float,float)

JOID calls it from `clearColor`, only while color writes are on.
Clear the color of the current target to that color.

## drawPrimitive(Primitive,VertexBuffer,IShader)

JOID calls it from `draw` with a shader that is active and a buffer that is not empty.
Apply the whole current state, bind the samplers of the shader and draw the vertices; call `requireFrame()` first.

## requireFrame()

Throws an `IllegalStateException` when no frame is begun, with the message that the render contract expects.

## resolveTexture()

Returns the texture of the state with its filter and wrap, or a 1×1 opaque white texture when the state has none.

## resolveSampler(UniformSampler)

Returns the texture of the sampler with its filter and wrap, or else `resolveTexture()`; a wrap that `canWrap` refuses becomes `CLAMP_TO_EDGE`.

## getDefaultShader()

Returns the shader that `draw` uses when the state has none, created on first use.

## getEmptyTexture()

Returns the 1×1 opaque white texture, created on first use.
