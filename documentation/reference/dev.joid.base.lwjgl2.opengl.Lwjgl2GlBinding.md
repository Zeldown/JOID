---
summary: The OpenGL binding of LWJGL 2, which calls the classes `GL11` to `GL33`.
---

The LWJGL 2 backend gives it to [[GlRenderBridge]], and so does a host that runs on the OpenGL of LWJGL 2.

Learn: [base-lwjgl2](/build/base-modules/lwjgl2/#bindings), [Create the bridge](/build/base-modules/opengl/#create-the-bridge).

## inst()

Returns the only instance.

## enable(int)

Calls `glEnable`.

## disable(int)

Calls `glDisable`.

## isEnabled(int)

Calls `glIsEnabled`.

## getInteger(int)

Calls `glGetInteger`.

## getString(int)

Calls `glGetString`, or returns `null` when no OpenGL context is current on the calling thread.

## getString(int,int)

Calls `glGetStringi`.

## getFloats(int,float[])

Calls `glGetFloatv`.

## getIntegers(int,int[])

Calls `glGetIntegerv`.

## getVertexAttribi(int,int)

Calls `glGetVertexAttribiv`.

## getVertexAttribPointer(int,int)

Calls `glGetVertexAttribPointerv` and returns the pointer as an unsigned value on 32-bit systems too.

## getVertexAttribFloats(int,int,float[])

Calls `glGetVertexAttribfv`.

## clear(int)

Calls `glClear`.

## readBuffer(int)

Calls `glReadBuffer`.

## readPixels(int,int,int,int,int,int,ByteBuffer)

Calls `glReadPixels`.

## clearDepth(double)

Calls `glClearDepth`.

## clearStencil(int)

Calls `glClearStencil`.

## viewport(int,int,int,int)

Calls `glViewport`.

## clearColor(float,float,float,float)

Calls `glClearColor`.

## colorMask(boolean,boolean,boolean,boolean)

Calls `glColorMask`.

## blendEquationSeparate(int,int)

Calls `glBlendEquationSeparate`.

## blendFuncSeparate(int,int,int,int)

Calls `glBlendFuncSeparate`.

## depthFunc(int)

Calls `glDepthFunc`.

## depthMask(boolean)

Calls `glDepthMask`.

## stencilMask(int)

Calls `glStencilMask`.

## stencilMaskSeparate(int,int)

Calls `glStencilMaskSeparate`.

## stencilOp(int,int,int)

Calls `glStencilOp`.

## stencilFunc(int,int,int)

Calls `glStencilFunc`.

## stencilOpSeparate(int,int,int,int)

Calls `glStencilOpSeparate`.

## stencilFuncSeparate(int,int,int,int)

Calls `glStencilFuncSeparate`.

## cullFace(int)

Calls `glCullFace`.

## frontFace(int)

Calls `glFrontFace`.

## lineWidth(float)

Calls `glLineWidth`.

## polygonMode(int,int)

Calls `glPolygonMode`.

## pixelStorei(int,int)

Calls `glPixelStorei`.

## genBuffer()

Calls `glGenBuffers`.

## bindBuffer(int,int)

Calls `glBindBuffer`.

## bufferData(int,ByteBuffer,int)

Calls `glBufferData`.

## genVertexArray()

Calls `glGenVertexArrays`.

## bindVertexArray(int)

Calls `glBindVertexArray`.

## enableVertexAttribArray(int)

Calls `glEnableVertexAttribArray`.

## disableVertexAttribArray(int)

Calls `glDisableVertexAttribArray`.

## vertexAttrib2f(int,float,float)

Calls `glVertexAttrib2f`.

## vertexAttrib3f(int,float,float,float)

Calls `glVertexAttrib3f`.

## vertexAttrib4f(int,float,float,float,float)

Calls `glVertexAttrib4f`.

## vertexAttribPointer(int,int,int,boolean,int,long)

Calls `glVertexAttribPointer`.

## drawArrays(int,int,int)

Calls `glDrawArrays`.

## createProgram()

Calls `glCreateProgram`.

## deleteProgram(int)

Calls `glDeleteProgram`.

## useProgram(int)

Calls `glUseProgram`.

## linkProgram(int)

Calls `glLinkProgram`.

## getProgrami(int,int)

Calls `glGetProgramiv`.

## getProgramInfoLog(int)

Calls `glGetProgramInfoLog`.

## createShader(int)

Calls `glCreateShader`.

## deleteShader(int)

Calls `glDeleteShader`.

## compileShader(int)

Calls `glCompileShader`.

## getShaderi(int,int)

Calls `glGetShaderiv`.

## getShaderInfoLog(int)

Calls `glGetShaderInfoLog`.

## shaderSource(int,String)

Calls `glShaderSource`.

## attachShader(int,int)

Calls `glAttachShader`.

## detachShader(int,int)

Calls `glDetachShader`.

## bindAttribLocation(int,int,String)

Calls `glBindAttribLocation`.

## bindFragDataLocation(int,int,String)

Calls `glBindFragDataLocation`.

## uniform1i(int,int)

Calls `glUniform1i`.

## getUniformLocation(int,String)

Calls `glGetUniformLocation`.

## uniformiv(int,int,IntBuffer)

Calls `glUniform1iv` to `glUniform4iv`.

## uniformuiv(int,int,IntBuffer)

Calls `glUniform1uiv` to `glUniform4uiv`.

## uniformfv(int,int,FloatBuffer)

Calls `glUniform1fv` to `glUniform4fv`.

## uniformMatrixfv(int,int,FloatBuffer)

Calls `glUniformMatrix2fv` to `glUniformMatrix4fv`.

## genTexture()

Calls `glGenTextures`.

## deleteTexture(int)

Calls `glDeleteTextures`.

## activeTexture(int)

Calls `glActiveTexture`.

## bindSampler(int,int)

Calls `glBindSampler`.

## bindTexture(int,int)

Calls `glBindTexture`.

## texParameteri(int,int,int)

Calls `glTexParameteri`.

## texImage2D(int,int,int,int,int,int,int)

Calls `glTexImage2D`.

## texSubImage2D(int,int,int,int,int,int,int,int,int[])

Calls `glTexSubImage2D` through a direct buffer of a million pixels that it reuses, in bands of rows for a larger image.

## isTexture(int)

Calls `glIsTexture`.

## getTexParameteri(int,int)

Calls `glGetTexParameteriv`.

## getTexLevelParameteri(int,int,int)

Calls `glGetTexLevelParameteriv`.

## getFrameBufferBinding(GlFrameBufferFamily)

Returns the LWJGL 2 binding of `GL_EXT_framebuffer_object` for `EXT`, and the core binding otherwise.

## canPointClientMemory()

Returns `false`: LWJGL 2 cannot hand OpenGL a pointer into client memory.
