---
summary: The render bridge of OpenGL, shared by every OpenGL backend, which calls OpenGL through a binding and gives the host its state back.
---

Create it with the context of JOID current on the calling thread: it reads the capabilities of the context at once and throws an `IllegalStateException` when there is none, or when the context is too old.
It draws into the framebuffer that was bound when the frame began, and every change it makes to the state goes through its [[GlStateGuard]].

Learn: [base-opengl](/build/base-modules/opengl/), [Render bridge](/build/bridges/render/).

## GlRenderBridge(IGlBinding)

Creates the bridge over the OpenGL binding of your library, reads the capabilities of the current context and creates its vertex input.

## getBinding()

Returns the binding wrapped by the state guard, through which every call of the bridge goes; call it rather than your own binding to draw with JOID's state.

## getGuard()

Returns the state guard of the bridge.

## getVertexInput()

Returns the vertex buffer and the vertex array that every draw uses.

## getCapabilities()

Returns what the context offers.

## getMipmapBuilder()

Returns the mipmap builder: framebuffer blits when the context can blit, drawn quads otherwise.

## getFrameBufferBinding()

Returns the journaled framebuffer binding of the family that the context offers.

## getScreenFrameBuffer()

Returns the framebuffer that was bound when the frame began, where JOID draws when no framebuffer of JOID is the target.

## createTexture()

Returns an empty [[GlTexture]].

## createFrameBuffer(int,int)

Returns a [[GlFrameBuffer]] of that size in pixels.

## createShader(ShaderSource,ShaderSource,BlendState)

Returns a [[GlShader]], translated to the GLSL dialect of the context.

## suspend(Runnable)

Gives the host its state back, runs the drawing, then takes the state again for JOID.

## raster(IFrameBuffer,int,int,Runnable)

Runs a drawing of the host into a framebuffer of JOID, with a viewport of that size in pixels, and binds the previous framebuffers and viewport back after it.

## beginFrameCommands()

Remembers the framebuffer that the host bound, then enters the state guard.

## submitFrameCommands()

Exits the state guard, which gives the host its state back.

## clearDepthBuffer()

Clears the depth of the target, with depth writes on.

## clearStencilBuffer()

Clears the stencil of the target, with every stencil bit writable.

## clearColorBuffer(float,float,float,float)

Clears the color of the target to that color, with every channel writable.

## drawPrimitive(Primitive,VertexBuffer,IShader)

Applies the whole render state, binds the samplers that the shader declares, uploads the vertices and draws them; throws an `IllegalStateException` outside a frame.
