---
summary: The OpenGL enum values that the OpenGL module passes to its binding, named after the OpenGL constants without their `GL_` prefix.
---

They let [[IGlBinding]] take plain `int` values, so that base-opengl compiles against no LWJGL version.
A binding passes them to OpenGL unchanged.

Learn: [base-opengl](/build/base-modules/opengl/).

## ONE

The value of `GL_ONE`, the integer `1`, as a blend factor or a boolean.

## ZERO

The value of `GL_ZERO`, the integer `1`, as a blend factor or a boolean.

## TRUE

The value of `GL_TRUE`, the integer `1`, as a blend factor or a boolean.

## VERSION

The value of `GL_VERSION`, a name read with `glGetString` or `glGetInteger` to learn what the context offers.

## RENDERER

The value of `GL_RENDERER`, a name read with `glGetString` or `glGetInteger` to learn what the context offers.

## EXTENSIONS

The value of `GL_EXTENSIONS`, a name read with `glGetString` or `glGetInteger` to learn what the context offers.

## CONTEXT_FLAGS

The value of `GL_CONTEXT_FLAGS`, a name read with `glGetString` or `glGetInteger` to learn what the context offers.

## NUM_EXTENSIONS

The value of `GL_NUM_EXTENSIONS`, a name read with `glGetString` or `glGetInteger` to learn what the context offers.

## MAX_TEXTURE_SIZE

The value of `GL_MAX_TEXTURE_SIZE`, a name read with `glGetString` or `glGetInteger` to learn what the context offers.

## CONTEXT_PROFILE_MASK

The value of `GL_CONTEXT_PROFILE_MASK`, a name read with `glGetString` or `glGetInteger` to learn what the context offers.

## SHADING_LANGUAGE_VERSION

The value of `GL_SHADING_LANGUAGE_VERSION`, a name read with `glGetString` or `glGetInteger` to learn what the context offers.

## CONTEXT_CORE_PROFILE_BIT

The value of `GL_CONTEXT_CORE_PROFILE_BIT`, a bit of the profile mask or of the context flags.

## CONTEXT_FLAG_FORWARD_COMPATIBLE_BIT

The value of `GL_CONTEXT_FLAG_FORWARD_COMPATIBLE_BIT`, a bit of the profile mask or of the context flags.

## BLEND

The value of `GL_BLEND`, a capability switched with `glEnable` and `glDisable`.

## CULL_FACE

The value of `GL_CULL_FACE`, a capability switched with `glEnable` and `glDisable`.

## DEPTH_TEST

The value of `GL_DEPTH_TEST`, a capability switched with `glEnable` and `glDisable`.

## LINE_SMOOTH

The value of `GL_LINE_SMOOTH`, a capability switched with `glEnable` and `glDisable`.

## STENCIL_TEST

The value of `GL_STENCIL_TEST`, a capability switched with `glEnable` and `glDisable`.

## SCISSOR_TEST

The value of `GL_SCISSOR_TEST`, a capability switched with `glEnable` and `glDisable`.

## COLOR_BUFFER_BIT

The value of `GL_COLOR_BUFFER_BIT`, a buffer bit of `glClear`.

## DEPTH_BUFFER_BIT

The value of `GL_DEPTH_BUFFER_BIT`, a buffer bit of `glClear`.

## STENCIL_BUFFER_BIT

The value of `GL_STENCIL_BUFFER_BIT`, a buffer bit of `glClear`.

## MIN

The value of `GL_MIN`, a blend equation.

## MAX

The value of `GL_MAX`, a blend equation.

## FUNC_ADD

The value of `GL_FUNC_ADD`, a blend equation.

## FUNC_SUBTRACT

The value of `GL_FUNC_SUBTRACT`, a blend equation.

## FUNC_REVERSE_SUBTRACT

The value of `GL_FUNC_REVERSE_SUBTRACT`, a blend equation.

## SRC_COLOR

The value of `GL_SRC_COLOR`, a blend factor.

## DST_COLOR

The value of `GL_DST_COLOR`, a blend factor.

## SRC_ALPHA

The value of `GL_SRC_ALPHA`, a blend factor.

## DST_ALPHA

The value of `GL_DST_ALPHA`, a blend factor.

## ONE_MINUS_SRC_COLOR

The value of `GL_ONE_MINUS_SRC_COLOR`, a blend factor.

## ONE_MINUS_DST_COLOR

The value of `GL_ONE_MINUS_DST_COLOR`, a blend factor.

## ONE_MINUS_SRC_ALPHA

The value of `GL_ONE_MINUS_SRC_ALPHA`, a blend factor.

## ONE_MINUS_DST_ALPHA

The value of `GL_ONE_MINUS_DST_ALPHA`, a blend factor.

## LESS

The value of `GL_LESS`, a comparison function of the depth or stencil test.

## NEVER

The value of `GL_NEVER`, a comparison function of the depth or stencil test.

## EQUAL

The value of `GL_EQUAL`, a comparison function of the depth or stencil test.

## LEQUAL

The value of `GL_LEQUAL`, a comparison function of the depth or stencil test.

## GEQUAL

The value of `GL_GEQUAL`, a comparison function of the depth or stencil test.

## ALWAYS

The value of `GL_ALWAYS`, a comparison function of the depth or stencil test.

## GREATER

The value of `GL_GREATER`, a comparison function of the depth or stencil test.

## NOTEQUAL

The value of `GL_NOTEQUAL`, a comparison function of the depth or stencil test.

## KEEP

The value of `GL_KEEP`, a stencil operation.

## INCR

The value of `GL_INCR`, a stencil operation.

## DECR

The value of `GL_DECR`, a stencil operation.

## INVERT

The value of `GL_INVERT`, a stencil operation.

## REPLACE

The value of `GL_REPLACE`, a stencil operation.

## LINES

The value of `GL_LINES`, a primitive of `glDrawArrays`.

## TRIANGLES

The value of `GL_TRIANGLES`, a primitive of `glDrawArrays`.

## BYTE

The value of `GL_BYTE`, a component type of vertices or pixels.

## FLOAT

The value of `GL_FLOAT`, a component type of vertices or pixels.

## UNSIGNED_BYTE

The value of `GL_UNSIGNED_BYTE`, a component type of vertices or pixels.

## RGBA

The value of `GL_RGBA`, a pixel format or internal format of a texture.

## BGRA

The value of `GL_BGRA`, a pixel format or internal format of a texture.

## RGBA8

The value of `GL_RGBA8`, a pixel format or internal format of a texture.

## UNSIGNED_INT_8_8_8_8_REV

The value of `GL_UNSIGNED_INT_8_8_8_8_REV`, a pixel format or internal format of a texture.

## TEXTURE0

The value of `GL_TEXTURE0`, a texture unit, target or parameter.

## TEXTURE_2D

The value of `GL_TEXTURE_2D`, a texture unit, target or parameter.

## TEXTURE_WIDTH

The value of `GL_TEXTURE_WIDTH`, a texture unit, target or parameter.

## TEXTURE_HEIGHT

The value of `GL_TEXTURE_HEIGHT`, a texture unit, target or parameter.

## TEXTURE_WRAP_S

The value of `GL_TEXTURE_WRAP_S`, a texture unit, target or parameter.

## TEXTURE_WRAP_T

The value of `GL_TEXTURE_WRAP_T`, a texture unit, target or parameter.

## TEXTURE_MAX_LEVEL

The value of `GL_TEXTURE_MAX_LEVEL`, a texture unit, target or parameter.

## TEXTURE_BASE_LEVEL

The value of `GL_TEXTURE_BASE_LEVEL`, a texture unit, target or parameter.

## TEXTURE_MIN_FILTER

The value of `GL_TEXTURE_MIN_FILTER`, a texture unit, target or parameter.

## TEXTURE_MAG_FILTER

The value of `GL_TEXTURE_MAG_FILTER`, a texture unit, target or parameter.

## LINEAR

The value of `GL_LINEAR`, a texture filter.

## NEAREST

The value of `GL_NEAREST`, a texture filter.

## LINEAR_MIPMAP_LINEAR

The value of `GL_LINEAR_MIPMAP_LINEAR`, a texture filter.

## REPEAT

The value of `GL_REPEAT`, a texture wrap mode.

## CLAMP_TO_EDGE

The value of `GL_CLAMP_TO_EDGE`, a texture wrap mode.

## CLAMP_TO_BORDER

The value of `GL_CLAMP_TO_BORDER`, a texture wrap mode.

## STREAM_DRAW

The value of `GL_STREAM_DRAW`, a buffer target or usage.

## ARRAY_BUFFER

The value of `GL_ARRAY_BUFFER`, a buffer target or usage.

## LINK_STATUS

The value of `GL_LINK_STATUS`, a shader stage or a status of shader compilation and linking.

## VERTEX_SHADER

The value of `GL_VERTEX_SHADER`, a shader stage or a status of shader compilation and linking.

## COMPILE_STATUS

The value of `GL_COMPILE_STATUS`, a shader stage or a status of shader compilation and linking.

## FRAGMENT_SHADER

The value of `GL_FRAGMENT_SHADER`, a shader stage or a status of shader compilation and linking.

## BACK

The value of `GL_BACK`, a framebuffer, renderbuffer or attachment name.

## FRAMEBUFFER

The value of `GL_FRAMEBUFFER`, a framebuffer, renderbuffer or attachment name.

## RENDERBUFFER

The value of `GL_RENDERBUFFER`, a framebuffer, renderbuffer or attachment name.

## READ_FRAMEBUFFER

The value of `GL_READ_FRAMEBUFFER`, a framebuffer, renderbuffer or attachment name.

## DRAW_FRAMEBUFFER

The value of `GL_DRAW_FRAMEBUFFER`, a framebuffer, renderbuffer or attachment name.

## DEPTH_ATTACHMENT

The value of `GL_DEPTH_ATTACHMENT`, a framebuffer, renderbuffer or attachment name.

## COLOR_ATTACHMENT0

The value of `GL_COLOR_ATTACHMENT0`, a framebuffer, renderbuffer or attachment name.

## DEPTH_COMPONENT24

The value of `GL_DEPTH_COMPONENT24`, a framebuffer, renderbuffer or attachment name.

## READ_FRAMEBUFFER_BINDING

The value of `GL_READ_FRAMEBUFFER_BINDING`, a framebuffer, renderbuffer or attachment name.

## DRAW_FRAMEBUFFER_BINDING

The value of `GL_DRAW_FRAMEBUFFER_BINDING`, a framebuffer, renderbuffer or attachment name.

## READ_BUFFER

The value of `GL_READ_BUFFER`, a binding read with `glGetInteger` to save the state of a host.

## ACTIVE_TEXTURE

The value of `GL_ACTIVE_TEXTURE`, a binding read with `glGetInteger` to save the state of a host.

## CURRENT_PROGRAM

The value of `GL_CURRENT_PROGRAM`, a binding read with `glGetInteger` to save the state of a host.

## SAMPLER_BINDING

The value of `GL_SAMPLER_BINDING`, a binding read with `glGetInteger` to save the state of a host.

## TEXTURE_BINDING_2D

The value of `GL_TEXTURE_BINDING_2D`, a binding read with `glGetInteger` to save the state of a host.

## ARRAY_BUFFER_BINDING

The value of `GL_ARRAY_BUFFER_BINDING`, a binding read with `glGetInteger` to save the state of a host.

## RENDERBUFFER_BINDING

The value of `GL_RENDERBUFFER_BINDING`, a binding read with `glGetInteger` to save the state of a host.

## VERTEX_ARRAY_BINDING

The value of `GL_VERTEX_ARRAY_BINDING`, a binding read with `glGetInteger` to save the state of a host.

## PIXEL_PACK_BUFFER_BINDING

The value of `GL_PIXEL_PACK_BUFFER_BINDING`, a binding read with `glGetInteger` to save the state of a host.

## PIXEL_UNPACK_BUFFER_BINDING

The value of `GL_PIXEL_UNPACK_BUFFER_BINDING`, a binding read with `glGetInteger` to save the state of a host.

## PIXEL_PACK_BUFFER

The value of `GL_PIXEL_PACK_BUFFER`, a pixel buffer target.

## PIXEL_UNPACK_BUFFER

The value of `GL_PIXEL_UNPACK_BUFFER`, a pixel buffer target.

## DEPTH_CLAMP

The value of `GL_DEPTH_CLAMP`, a capability that a host may leave on, saved and reset around JOID.

## COLOR_LOGIC_OP

The value of `GL_COLOR_LOGIC_OP`, a capability that a host may leave on, saved and reset around JOID.

## FRAMEBUFFER_SRGB

The value of `GL_FRAMEBUFFER_SRGB`, a capability that a host may leave on, saved and reset around JOID.

## PRIMITIVE_RESTART

The value of `GL_PRIMITIVE_RESTART`, a capability that a host may leave on, saved and reset around JOID.

## RASTERIZER_DISCARD

The value of `GL_RASTERIZER_DISCARD`, a capability that a host may leave on, saved and reset around JOID.

## POLYGON_OFFSET_FILL

The value of `GL_POLYGON_OFFSET_FILL`, a capability that a host may leave on, saved and reset around JOID.

## BLEND_SRC_RGB

The value of `GL_BLEND_SRC_RGB`, a blend state read with `glGetInteger`.

## BLEND_DST_RGB

The value of `GL_BLEND_DST_RGB`, a blend state read with `glGetInteger`.

## BLEND_SRC_ALPHA

The value of `GL_BLEND_SRC_ALPHA`, a blend state read with `glGetInteger`.

## BLEND_DST_ALPHA

The value of `GL_BLEND_DST_ALPHA`, a blend state read with `glGetInteger`.

## BLEND_EQUATION_RGB

The value of `GL_BLEND_EQUATION_RGB`, a blend state read with `glGetInteger`.

## BLEND_EQUATION_ALPHA

The value of `GL_BLEND_EQUATION_ALPHA`, a blend state read with `glGetInteger`.

## VIEWPORT

The value of `GL_VIEWPORT`, a rasterizer state read with `glGet`.

## LINE_WIDTH

The value of `GL_LINE_WIDTH`, a rasterizer state read with `glGet`.

## FRONT_FACE

The value of `GL_FRONT_FACE`, a rasterizer state read with `glGet`.

## POLYGON_MODE

The value of `GL_POLYGON_MODE`, a rasterizer state read with `glGet`.

## CULL_FACE_MODE

The value of `GL_CULL_FACE_MODE`, a rasterizer state read with `glGet`.

## COLOR_WRITEMASK

The value of `GL_COLOR_WRITEMASK`, a rasterizer state read with `glGet`.

## COLOR_CLEAR_VALUE

The value of `GL_COLOR_CLEAR_VALUE`, a rasterizer state read with `glGet`.

## DEPTH_FUNC

The value of `GL_DEPTH_FUNC`, a depth state read with `glGet`.

## DEPTH_WRITEMASK

The value of `GL_DEPTH_WRITEMASK`, a depth state read with `glGet`.

## DEPTH_CLEAR_VALUE

The value of `GL_DEPTH_CLEAR_VALUE`, a depth state read with `glGet`.

## STENCIL_REF

The value of `GL_STENCIL_REF`, a stencil state read with `glGetInteger`.

## STENCIL_FUNC

The value of `GL_STENCIL_FUNC`, a stencil state read with `glGetInteger`.

## STENCIL_FAIL

The value of `GL_STENCIL_FAIL`, a stencil state read with `glGetInteger`.

## STENCIL_BACK_REF

The value of `GL_STENCIL_BACK_REF`, a stencil state read with `glGetInteger`.

## STENCIL_WRITEMASK

The value of `GL_STENCIL_WRITEMASK`, a stencil state read with `glGetInteger`.

## STENCIL_BACK_FUNC

The value of `GL_STENCIL_BACK_FUNC`, a stencil state read with `glGetInteger`.

## STENCIL_BACK_FAIL

The value of `GL_STENCIL_BACK_FAIL`, a stencil state read with `glGetInteger`.

## STENCIL_VALUE_MASK

The value of `GL_STENCIL_VALUE_MASK`, a stencil state read with `glGetInteger`.

## STENCIL_CLEAR_VALUE

The value of `GL_STENCIL_CLEAR_VALUE`, a stencil state read with `glGetInteger`.

## STENCIL_BACK_WRITEMASK

The value of `GL_STENCIL_BACK_WRITEMASK`, a stencil state read with `glGetInteger`.

## STENCIL_PASS_DEPTH_FAIL

The value of `GL_STENCIL_PASS_DEPTH_FAIL`, a stencil state read with `glGetInteger`.

## STENCIL_PASS_DEPTH_PASS

The value of `GL_STENCIL_PASS_DEPTH_PASS`, a stencil state read with `glGetInteger`.

## STENCIL_BACK_VALUE_MASK

The value of `GL_STENCIL_BACK_VALUE_MASK`, a stencil state read with `glGetInteger`.

## STENCIL_BACK_PASS_DEPTH_FAIL

The value of `GL_STENCIL_BACK_PASS_DEPTH_FAIL`, a stencil state read with `glGetInteger`.

## STENCIL_BACK_PASS_DEPTH_PASS

The value of `GL_STENCIL_BACK_PASS_DEPTH_PASS`, a stencil state read with `glGetInteger`.

## CCW

The value of `GL_CCW`, a face or polygon mode value.

## FILL

The value of `GL_FILL`, a face or polygon mode value.

## FRONT

The value of `GL_FRONT`, a face or polygon mode value.

## FRONT_AND_BACK

The value of `GL_FRONT_AND_BACK`, a face or polygon mode value.

## PACK_ALIGNMENT

The value of `GL_PACK_ALIGNMENT`, a pixel storage parameter of `glPixelStorei`.

## PACK_SKIP_ROWS

The value of `GL_PACK_SKIP_ROWS`, a pixel storage parameter of `glPixelStorei`.

## PACK_ROW_LENGTH

The value of `GL_PACK_ROW_LENGTH`, a pixel storage parameter of `glPixelStorei`.

## PACK_SKIP_PIXELS

The value of `GL_PACK_SKIP_PIXELS`, a pixel storage parameter of `glPixelStorei`.

## UNPACK_ALIGNMENT

The value of `GL_UNPACK_ALIGNMENT`, a pixel storage parameter of `glPixelStorei`.

## UNPACK_SKIP_ROWS

The value of `GL_UNPACK_SKIP_ROWS`, a pixel storage parameter of `glPixelStorei`.

## UNPACK_ROW_LENGTH

The value of `GL_UNPACK_ROW_LENGTH`, a pixel storage parameter of `glPixelStorei`.

## UNPACK_SKIP_PIXELS

The value of `GL_UNPACK_SKIP_PIXELS`, a pixel storage parameter of `glPixelStorei`.

## CURRENT_VERTEX_ATTRIB

The value of `GL_CURRENT_VERTEX_ATTRIB`, a vertex attribute state read with `glGetVertexAttrib`.

## VERTEX_ATTRIB_ARRAY_SIZE

The value of `GL_VERTEX_ATTRIB_ARRAY_SIZE`, a vertex attribute state read with `glGetVertexAttrib`.

## VERTEX_ATTRIB_ARRAY_TYPE

The value of `GL_VERTEX_ATTRIB_ARRAY_TYPE`, a vertex attribute state read with `glGetVertexAttrib`.

## VERTEX_ATTRIB_ARRAY_STRIDE

The value of `GL_VERTEX_ATTRIB_ARRAY_STRIDE`, a vertex attribute state read with `glGetVertexAttrib`.

## VERTEX_ATTRIB_ARRAY_ENABLED

The value of `GL_VERTEX_ATTRIB_ARRAY_ENABLED`, a vertex attribute state read with `glGetVertexAttrib`.

## VERTEX_ATTRIB_ARRAY_POINTER

The value of `GL_VERTEX_ATTRIB_ARRAY_POINTER`, a vertex attribute state read with `glGetVertexAttrib`.

## VERTEX_ATTRIB_ARRAY_NORMALIZED

The value of `GL_VERTEX_ATTRIB_ARRAY_NORMALIZED`, a vertex attribute state read with `glGetVertexAttrib`.

## VERTEX_ATTRIB_ARRAY_BUFFER_BINDING

The value of `GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING`, a vertex attribute state read with `glGetVertexAttrib`.

## FOG

The value of `GL_FOG`, a capability of the fixed pipeline, reset for a host that uses it.

## LIGHTING

The value of `GL_LIGHTING`, a capability of the fixed pipeline, reset for a host that uses it.

## ALPHA_TEST

The value of `GL_ALPHA_TEST`, a capability of the fixed pipeline, reset for a host that uses it.

## COLOR_MATERIAL

The value of `GL_COLOR_MATERIAL`, a capability of the fixed pipeline, reset for a host that uses it.

## MODELVIEW_MATRIX

The value of `GL_MODELVIEW_MATRIX`, a matrix of the fixed pipeline read with `glGetFloat`.

## PROJECTION_MATRIX

The value of `GL_PROJECTION_MATRIX`, a matrix of the fixed pipeline read with `glGetFloat`.
