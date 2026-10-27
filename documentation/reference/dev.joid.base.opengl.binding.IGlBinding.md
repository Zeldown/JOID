---
summary: The OpenGL calls of the OpenGL module, one method per OpenGL function, implemented once per library.
---

[[GlRenderBridge]] calls OpenGL only through a binding, so base-opengl compiles against no LWJGL version: pass [[Lwjgl3GlBinding]], [[Lwjgl2GlBinding]] or the OpenGL functions of your engine.
The arguments are the values of [[GlConstants]]; a binding passes them to OpenGL unchanged and adds no check.
Every method runs on the render thread, with the context of JOID current.

Learn: [Create the bridge](/build/base-modules/opengl/#create-the-bridge).

## enable(int)

Turns a capability on.
Maps to `glEnable`.

## disable(int)

Turns a capability off.
Maps to `glDisable`.

## isEnabled(int)

If `true`, the capability is on.
Maps to `glIsEnabled`.

## getInteger(int)

Returns the integer value of a state.
Maps to `glGetInteger`.

## getString(int)

Returns a string of the context, such as its version, or `null` when no context is current.
Maps to `glGetString`.

## getString(int,int)

Returns one string of an indexed list, such as one extension.
Maps to `glGetStringi`.

## getFloats(int,float[])

Reads the float values of a state into the array.
Maps to `glGetFloatv`.

## getIntegers(int,int[])

Reads the integer values of a state into the array.
Maps to `glGetIntegerv`.

## getVertexAttribi(int,int)

Returns an integer state of a vertex attribute.
Maps to `glGetVertexAttribiv`.

## getVertexAttribPointer(int,int)

Returns the offset or the client pointer of a vertex attribute.
Maps to `glGetVertexAttribPointerv`.

## getVertexAttribFloats(int,int,float[])

Reads the float values of a vertex attribute state, such as its current value, into the array.
Maps to `glGetVertexAttribfv`.

## clear(int)

Clears the buffers named by the mask of the bound framebuffer.
Maps to `glClear`.

## readBuffer(int)

Selects the color buffer that `readPixels` reads.
Maps to `glReadBuffer`.

## readPixels(int,int,int,int,int,int,ByteBuffer)

Reads a rectangle of pixels of the read framebuffer into the buffer.
Maps to `glReadPixels`.

## clearDepth(double)

Sets the depth that `clear` writes.
Maps to `glClearDepth`.

## clearStencil(int)

Sets the stencil value that `clear` writes.
Maps to `glClearStencil`.

## viewport(int,int,int,int)

Sets the viewport, in pixels from the bottom-left corner.
Maps to `glViewport`.

## clearColor(float,float,float,float)

Sets the color that `clear` writes, each channel from `0F` to `1F`.
Maps to `glClearColor`.

## colorMask(boolean,boolean,boolean,boolean)

Sets which color channels draws and clears write.
Maps to `glColorMask`.

## blendEquationSeparate(int,int)

Sets the blend equations of the color and of the alpha.
Maps to `glBlendEquationSeparate`.

## blendFuncSeparate(int,int,int,int)

Sets the blend factors of the color and of the alpha.
Maps to `glBlendFuncSeparate`.

## depthFunc(int)

Sets the comparison of the depth test.
Maps to `glDepthFunc`.

## depthMask(boolean)

If `true`, draws and clears write the depth.
Maps to `glDepthMask`.

## stencilMask(int)

Sets the bits of the stencil that draws and clears write, on both faces.
Maps to `glStencilMask`.

## stencilMaskSeparate(int,int)

Sets the stencil write mask of one face.
Maps to `glStencilMaskSeparate`.

## stencilOp(int,int,int)

Sets the stencil operations on both faces.
Maps to `glStencilOp`.

## stencilFunc(int,int,int)

Sets the stencil test on both faces.
Maps to `glStencilFunc`.

## stencilOpSeparate(int,int,int,int)

Sets the stencil operations of one face.
Maps to `glStencilOpSeparate`.

## stencilFuncSeparate(int,int,int,int)

Sets the stencil test of one face.
Maps to `glStencilFuncSeparate`.

## cullFace(int)

Sets which faces culling removes.
Maps to `glCullFace`.

## frontFace(int)

Sets the winding of the front faces.
Maps to `glFrontFace`.

## lineWidth(float)

Sets the width of lines, in pixels.
Maps to `glLineWidth`.

## polygonMode(int,int)

Sets how polygons are rasterized: filled, lines or points.
Maps to `glPolygonMode`.

## pixelStorei(int,int)

Sets a pixel storage parameter of uploads and reads.
Maps to `glPixelStorei`.

## genBuffer()

Creates a buffer object and returns its name.
Maps to `glGenBuffers`.

## bindBuffer(int,int)

Binds a buffer object to a target.
Maps to `glBindBuffer`.

## bufferData(int,ByteBuffer,int)

Fills the buffer bound to the target with the remaining bytes of the data.
Maps to `glBufferData`.

## genVertexArray()

Creates a vertex array object and returns its name.
Maps to `glGenVertexArrays`.

## bindVertexArray(int)

Binds a vertex array object, `0` for none.
Maps to `glBindVertexArray`.

## enableVertexAttribArray(int)

Reads the attribute from the vertex buffer.
Maps to `glEnableVertexAttribArray`.

## disableVertexAttribArray(int)

Stops reading the attribute from the vertex buffer: it takes its current value.
Maps to `glDisableVertexAttribArray`.

## vertexAttrib2f(int,float,float)

Sets the current value of an attribute with two components.
Maps to `glVertexAttrib2f`.

## vertexAttrib3f(int,float,float,float)

Sets the current value of an attribute with three components.
Maps to `glVertexAttrib3f`.

## vertexAttrib4f(int,float,float,float,float)

Sets the current value of an attribute with four components.
Maps to `glVertexAttrib4f`.

## vertexAttribPointer(int,int,int,boolean,int,long)

Points an attribute at the bound array buffer, with its size, type, stride and byte offset.
Maps to `glVertexAttribPointer`.

## drawArrays(int,int,int)

Draws vertices of the bound buffer as lines or triangles.
Maps to `glDrawArrays`.

## createProgram()

Creates a program and returns its name.
Maps to `glCreateProgram`.

## deleteProgram(int)

Deletes a program.
Maps to `glDeleteProgram`.

## useProgram(int)

Makes a program current, `0` for none.
Maps to `glUseProgram`.

## linkProgram(int)

Links a program from its attached shaders.
Maps to `glLinkProgram`.

## getProgrami(int,int)

Returns an integer state of a program, such as its link status.
Maps to `glGetProgramiv`.

## getProgramInfoLog(int)

Returns the link log of a program.
Maps to `glGetProgramInfoLog`.

## createShader(int)

Creates a vertex or fragment shader and returns its name.
Maps to `glCreateShader`.

## deleteShader(int)

Deletes a shader.
Maps to `glDeleteShader`.

## compileShader(int)

Compiles a shader from its source.
Maps to `glCompileShader`.

## getShaderi(int,int)

Returns an integer state of a shader, such as its compile status.
Maps to `glGetShaderiv`.

## getShaderInfoLog(int)

Returns the compile log of a shader.
Maps to `glGetShaderInfoLog`.

## shaderSource(int,String)

Sets the GLSL source of a shader.
Maps to `glShaderSource`.

## attachShader(int,int)

Attaches a shader to a program.
Maps to `glAttachShader`.

## detachShader(int,int)

Detaches a shader from a program.
Maps to `glDetachShader`.

## bindAttribLocation(int,int,String)

Binds an attribute name to a location, before the link.
Maps to `glBindAttribLocation`.

## bindFragDataLocation(int,int,String)

Binds a fragment output name to a color number, before the link.
Maps to `glBindFragDataLocation`.

## uniform1i(int,int)

Sets an `int` or sampler uniform of the current program.
Maps to `glUniform1i`.

## getUniformLocation(int,String)

Returns the location of a uniform, or `-1` when the program has none of that name.
Maps to `glGetUniformLocation`.

## uniformiv(int,int,IntBuffer)

Sets an integer uniform, or an array of them, of 1 to 4 components.
Maps to `glUniform1iv` to `glUniform4iv`.

## uniformuiv(int,int,IntBuffer)

Sets an unsigned integer uniform, or an array of them, of 1 to 4 components.
Maps to `glUniform1uiv` to `glUniform4uiv`.

## uniformfv(int,int,FloatBuffer)

Sets a float uniform, or an array of them, of 1 to 4 components.
Maps to `glUniform1fv` to `glUniform4fv`.

## uniformMatrixfv(int,int,FloatBuffer)

Sets a square matrix uniform of 2, 3 or 4 columns, column-major and not transposed.
Maps to `glUniformMatrix2fv` to `glUniformMatrix4fv`.

## genTexture()

Creates a texture and returns its name.
Maps to `glGenTextures`.

## deleteTexture(int)

Deletes a texture.
Maps to `glDeleteTextures`.

## activeTexture(int)

Selects the texture unit, from `TEXTURE0`.
Maps to `glActiveTexture`.

## bindSampler(int,int)

Binds a sampler object to a unit, `0` to sample with the parameters of the texture.
Maps to `glBindSampler`.

## bindTexture(int,int)

Binds a texture to a target of the active unit.
Maps to `glBindTexture`.

## texParameteri(int,int,int)

Sets a parameter of the bound texture, such as its filter or its wrap.
Maps to `glTexParameteri`.

## texImage2D(int,int,int,int,int,int,int)

Allocates one level of the bound texture, without pixels.
Maps to `glTexImage2D`.

## texSubImage2D(int,int,int,int,int,int,int,int,int[])

Uploads a rectangle of pixels into one level of the bound texture.
Maps to `glTexSubImage2D`.

## isTexture(int)

If `true`, the name is a texture of the context.
Maps to `glIsTexture`.

## getTexParameteri(int,int)

Returns an integer parameter of the bound texture.
Maps to `glGetTexParameteriv`.

## getTexLevelParameteri(int,int,int)

Returns an integer parameter of one level of the bound texture, such as its width.
Maps to `glGetTexLevelParameteriv`.

## getFrameBufferBinding(GlFrameBufferFamily)

Returns the framebuffer calls of that family: the core functions, or the functions of `GL_EXT_framebuffer_object`.

## canPointClientMemory()

If `true`, the binding can hand OpenGL a pointer into client memory; `true` by default.
Return `false` when it cannot: the state guard then leaves the client vertex arrays of a compatibility host as they are.
