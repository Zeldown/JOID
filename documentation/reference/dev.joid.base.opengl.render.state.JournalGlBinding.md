---
summary: An OpenGL binding that notes in a `GlStateJournal` every state that JOID changes, before passing the call on.
---

[[GlStateGuard]] wraps the binding of the backend in it, and [[GlRenderBridge#getBinding]] returns it, so that every change that JOID makes during a frame can be given back to the host.
Queries and creations pass through unchanged.

Learn: [The state journal](/build/embedding-in-a-host/#the-state-journal).

## JournalGlBinding(IGlBinding,GlStateJournal)

Wraps the binding, and its two framebuffer bindings, around the journal.

## enable(int)

Notes that capability in the journal, then calls the wrapped binding.

## disable(int)

Notes that capability in the journal, then calls the wrapped binding.

## isEnabled(int)

Calls the wrapped binding.

## getInteger(int)

Calls the wrapped binding.

## getString(int)

Calls the wrapped binding.

## getString(int,int)

Calls the wrapped binding.

## getFloats(int,float[])

Calls the wrapped binding.

## getIntegers(int,int[])

Calls the wrapped binding.

## getVertexAttribi(int,int)

Calls the wrapped binding.

## getVertexAttribPointer(int,int)

Calls the wrapped binding.

## getVertexAttribFloats(int,int,float[])

Calls the wrapped binding.

## clear(int)

Calls the wrapped binding.

## readBuffer(int)

Notes the read framebuffer and its read buffer in the journal, then calls the wrapped binding.

## readPixels(int,int,int,int,int,int,ByteBuffer)

Calls the wrapped binding.

## clearDepth(double)

Notes the clear depth in the journal, then calls the wrapped binding.

## clearStencil(int)

Notes the clear stencil value in the journal, then calls the wrapped binding.

## viewport(int,int,int,int)

Notes the viewport in the journal, then calls the wrapped binding.

## clearColor(float,float,float,float)

Notes the clear color in the journal, then calls the wrapped binding.

## colorMask(boolean,boolean,boolean,boolean)

Notes the color mask in the journal, then calls the wrapped binding.

## blendEquationSeparate(int,int)

Notes the blend equations in the journal, then calls the wrapped binding.

## blendFuncSeparate(int,int,int,int)

Notes the blend factors in the journal, then calls the wrapped binding.

## depthFunc(int)

Notes the depth function in the journal, then calls the wrapped binding.

## depthMask(boolean)

Notes the depth mask in the journal, then calls the wrapped binding.

## stencilMask(int)

Notes the stencil masks in the journal, then calls the wrapped binding.

## stencilMaskSeparate(int,int)

Notes the stencil masks in the journal, then calls the wrapped binding.

## stencilOp(int,int,int)

Notes the stencil operations in the journal, then calls the wrapped binding.

## stencilFunc(int,int,int)

Notes the stencil functions in the journal, then calls the wrapped binding.

## stencilOpSeparate(int,int,int,int)

Notes the stencil operations in the journal, then calls the wrapped binding.

## stencilFuncSeparate(int,int,int,int)

Notes the stencil functions in the journal, then calls the wrapped binding.

## cullFace(int)

Notes the culled face in the journal, then calls the wrapped binding.

## frontFace(int)

Notes the front face in the journal, then calls the wrapped binding.

## lineWidth(float)

Notes the line width in the journal, then calls the wrapped binding.

## polygonMode(int,int)

Notes the polygon mode in the journal, then calls the wrapped binding.

## pixelStorei(int,int)

Notes that pixel storage parameter in the journal, then calls the wrapped binding.

## genBuffer()

Calls the wrapped binding.

## bindBuffer(int,int)

Notes the buffer bound to that target in the journal, then calls the wrapped binding.

## bufferData(int,ByteBuffer,int)

Calls the wrapped binding.

## genVertexArray()

Creates a vertex array through the wrapped binding and excludes it from the journal: the attributes of an array of JOID are never restored.

## bindVertexArray(int)

Notes the bound vertex array in the journal, then calls the wrapped binding.

## enableVertexAttribArray(int)

Notes that vertex attribute in the journal, then calls the wrapped binding.

## disableVertexAttribArray(int)

Notes that vertex attribute in the journal, then calls the wrapped binding.

## vertexAttrib2f(int,float,float)

Notes the current value of that attribute in the journal, then calls the wrapped binding.

## vertexAttrib3f(int,float,float,float)

Notes the current value of that attribute in the journal, then calls the wrapped binding.

## vertexAttrib4f(int,float,float,float,float)

Notes the current value of that attribute in the journal, then calls the wrapped binding.

## vertexAttribPointer(int,int,int,boolean,int,long)

Notes that vertex attribute in the journal, then calls the wrapped binding.

## drawArrays(int,int,int)

Calls the wrapped binding.

## createProgram()

Calls the wrapped binding.

## deleteProgram(int)

Calls the wrapped binding.

## useProgram(int)

Notes the current program in the journal, then calls the wrapped binding.

## linkProgram(int)

Calls the wrapped binding.

## getProgrami(int,int)

Calls the wrapped binding.

## getProgramInfoLog(int)

Calls the wrapped binding.

## createShader(int)

Calls the wrapped binding.

## deleteShader(int)

Calls the wrapped binding.

## compileShader(int)

Calls the wrapped binding.

## getShaderi(int,int)

Calls the wrapped binding.

## getShaderInfoLog(int)

Calls the wrapped binding.

## shaderSource(int,String)

Calls the wrapped binding.

## attachShader(int,int)

Calls the wrapped binding.

## detachShader(int,int)

Calls the wrapped binding.

## bindAttribLocation(int,int,String)

Calls the wrapped binding.

## bindFragDataLocation(int,int,String)

Calls the wrapped binding.

## uniform1i(int,int)

Calls the wrapped binding.

## getUniformLocation(int,String)

Calls the wrapped binding.

## uniformiv(int,int,IntBuffer)

Calls the wrapped binding.

## uniformuiv(int,int,IntBuffer)

Calls the wrapped binding.

## uniformfv(int,int,FloatBuffer)

Calls the wrapped binding.

## uniformMatrixfv(int,int,FloatBuffer)

Calls the wrapped binding.

## genTexture()

Creates a texture through the wrapped binding and excludes it from the journal: the parameters of a texture of JOID are never restored.

## deleteTexture(int)

Deletes the texture through the wrapped binding and gives its name back to the journal, for a host that reuses it.

## activeTexture(int)

Notes the active unit in the journal, then calls the wrapped binding.

## bindSampler(int,int)

Notes the sampler of that unit in the journal, then calls the wrapped binding.

## bindTexture(int,int)

Notes the texture of the active unit in the journal, then calls the wrapped binding.

## texParameteri(int,int,int)

Notes that parameter of the bound texture in the journal, then calls the wrapped binding.

## texImage2D(int,int,int,int,int,int,int)

Calls the wrapped binding.

## texSubImage2D(int,int,int,int,int,int,int,int,int[])

Calls the wrapped binding.

## isTexture(int)

Calls the wrapped binding.

## getTexParameteri(int,int)

Calls the wrapped binding.

## getTexLevelParameteri(int,int,int)

Calls the wrapped binding.

## getFrameBufferBinding(GlFrameBufferFamily)

Returns the journaled framebuffer binding of that family.

## canPointClientMemory()

Returns the answer of the wrapped binding.
