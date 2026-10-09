package dev.joid.base.opengl.binding;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlConstants {

	public static final int ONE  = 1;
	public static final int ZERO = 0;
	public static final int TRUE = 1;

	public static final int VERSION                  = 0x1F02;
	public static final int RENDERER                 = 0x1F01;
	public static final int EXTENSIONS               = 0x1F03;
	public static final int CONTEXT_FLAGS            = 0x821E;
	public static final int NUM_EXTENSIONS           = 0x821D;
	public static final int MAX_TEXTURE_SIZE         = 0x0D33;
	public static final int CONTEXT_PROFILE_MASK     = 0x9126;
	public static final int SHADING_LANGUAGE_VERSION = 0x8B8C;

	public static final int CONTEXT_CORE_PROFILE_BIT            = 0x0001;
	public static final int CONTEXT_FLAG_FORWARD_COMPATIBLE_BIT = 0x0001;

	public static final int BLEND        = 0x0BE2;
	public static final int CULL_FACE    = 0x0B44;
	public static final int DEPTH_TEST   = 0x0B71;
	public static final int LINE_SMOOTH  = 0x0B20;
	public static final int STENCIL_TEST = 0x0B90;
	public static final int SCISSOR_TEST = 0x0C11;

	public static final int COLOR_BUFFER_BIT   = 0x4000;
	public static final int DEPTH_BUFFER_BIT   = 0x0100;
	public static final int STENCIL_BUFFER_BIT = 0x0400;

	public static final int MIN                   = 0x8007;
	public static final int MAX                   = 0x8008;
	public static final int FUNC_ADD              = 0x8006;
	public static final int FUNC_SUBTRACT         = 0x800A;
	public static final int FUNC_REVERSE_SUBTRACT = 0x800B;

	public static final int SRC_COLOR           = 0x0300;
	public static final int DST_COLOR           = 0x0306;
	public static final int SRC_ALPHA           = 0x0302;
	public static final int DST_ALPHA           = 0x0304;
	public static final int ONE_MINUS_SRC_COLOR = 0x0301;
	public static final int ONE_MINUS_DST_COLOR = 0x0307;
	public static final int ONE_MINUS_SRC_ALPHA = 0x0303;
	public static final int ONE_MINUS_DST_ALPHA = 0x0305;

	public static final int LESS     = 0x0201;
	public static final int NEVER    = 0x0200;
	public static final int EQUAL    = 0x0202;
	public static final int LEQUAL   = 0x0203;
	public static final int GEQUAL   = 0x0206;
	public static final int ALWAYS   = 0x0207;
	public static final int GREATER  = 0x0204;
	public static final int NOTEQUAL = 0x0205;

	public static final int KEEP    = 0x1E00;
	public static final int INCR    = 0x1E02;
	public static final int DECR    = 0x1E03;
	public static final int INVERT  = 0x150A;
	public static final int REPLACE = 0x1E01;

	public static final int LINES     = 0x0001;
	public static final int TRIANGLES = 0x0004;

	public static final int BYTE          = 0x1400;
	public static final int FLOAT         = 0x1406;
	public static final int UNSIGNED_BYTE = 0x1401;

	public static final int RGBA                     = 0x1908;
	public static final int BGRA                     = 0x80E1;
	public static final int RGBA8                    = 0x8058;
	public static final int UNSIGNED_INT_8_8_8_8_REV = 0x8367;

	public static final int TEXTURE0           = 0x84C0;
	public static final int TEXTURE_2D         = 0x0DE1;
	public static final int TEXTURE_WRAP_S     = 0x2802;
	public static final int TEXTURE_WRAP_T     = 0x2803;
	public static final int TEXTURE_MAX_LEVEL  = 0x813D;
	public static final int TEXTURE_BASE_LEVEL = 0x813C;
	public static final int TEXTURE_MIN_FILTER = 0x2801;
	public static final int TEXTURE_MAG_FILTER = 0x2800;

	public static final int LINEAR               = 0x2601;
	public static final int NEAREST              = 0x2600;
	public static final int LINEAR_MIPMAP_LINEAR = 0x2703;

	public static final int REPEAT          = 0x2901;
	public static final int CLAMP_TO_EDGE   = 0x812F;
	public static final int CLAMP_TO_BORDER = 0x812D;

	public static final int STREAM_DRAW  = 0x88E0;
	public static final int ARRAY_BUFFER = 0x8892;

	public static final int LINK_STATUS     = 0x8B82;
	public static final int VERTEX_SHADER   = 0x8B31;
	public static final int COMPILE_STATUS  = 0x8B81;
	public static final int FRAGMENT_SHADER = 0x8B30;

	public static final int BACK                     = 0x0405;
	public static final int FRAMEBUFFER              = 0x8D40;
	public static final int RENDERBUFFER             = 0x8D41;
	public static final int READ_FRAMEBUFFER         = 0x8CA8;
	public static final int DRAW_FRAMEBUFFER         = 0x8CA9;
	public static final int DEPTH_ATTACHMENT         = 0x8D00;
	public static final int COLOR_ATTACHMENT0        = 0x8CE0;
	public static final int DEPTH_COMPONENT24        = 0x81A6;
	public static final int READ_FRAMEBUFFER_BINDING = 0x8CAA;
	public static final int DRAW_FRAMEBUFFER_BINDING = 0x8CA6;

	public static final int READ_BUFFER                 = 0x0C02;
	public static final int ACTIVE_TEXTURE              = 0x84E0;
	public static final int CURRENT_PROGRAM             = 0x8B8D;
	public static final int SAMPLER_BINDING             = 0x8919;
	public static final int TEXTURE_BINDING_2D          = 0x8069;
	public static final int ARRAY_BUFFER_BINDING        = 0x8894;
	public static final int RENDERBUFFER_BINDING        = 0x8CA7;
	public static final int VERTEX_ARRAY_BINDING        = 0x85B5;
	public static final int PIXEL_PACK_BUFFER_BINDING   = 0x88ED;
	public static final int PIXEL_UNPACK_BUFFER_BINDING = 0x88EF;

	public static final int PIXEL_PACK_BUFFER   = 0x88EB;
	public static final int PIXEL_UNPACK_BUFFER = 0x88EC;

	public static final int DITHER              = 0x0BD0;
	public static final int DEPTH_CLAMP         = 0x864F;
	public static final int COLOR_LOGIC_OP      = 0x0BF2;
	public static final int FRAMEBUFFER_SRGB    = 0x8DB9;
	public static final int PRIMITIVE_RESTART   = 0x8F9D;
	public static final int RASTERIZER_DISCARD  = 0x8C89;
	public static final int POLYGON_OFFSET_FILL = 0x8037;

	public static final int BLEND_COLOR          = 0x8005;
	public static final int BLEND_SRC_RGB        = 0x80C9;
	public static final int BLEND_DST_RGB        = 0x80C8;
	public static final int BLEND_SRC_ALPHA      = 0x80CB;
	public static final int BLEND_DST_ALPHA      = 0x80CA;
	public static final int BLEND_EQUATION_RGB   = 0x8009;
	public static final int BLEND_EQUATION_ALPHA = 0x883D;

	public static final int VIEWPORT                = 0x0BA2;
	public static final int LINE_WIDTH              = 0x0B21;
	public static final int FRONT_FACE              = 0x0B46;
	public static final int SCISSOR_BOX             = 0x0C10;
	public static final int POLYGON_MODE            = 0x0B40;
	public static final int LOGIC_OP_MODE           = 0x0BF0;
	public static final int CULL_FACE_MODE          = 0x0B45;
	public static final int COLOR_WRITEMASK         = 0x0C23;
	public static final int COLOR_CLEAR_VALUE       = 0x0C22;
	public static final int PRIMITIVE_RESTART_INDEX = 0x8F9E;

	public static final int DEPTH_FUNC        = 0x0B74;
	public static final int DEPTH_WRITEMASK   = 0x0B72;
	public static final int DEPTH_CLEAR_VALUE = 0x0B73;

	public static final int STENCIL_REF                  = 0x0B97;
	public static final int STENCIL_FUNC                 = 0x0B92;
	public static final int STENCIL_FAIL                 = 0x0B94;
	public static final int STENCIL_BACK_REF             = 0x8CA3;
	public static final int STENCIL_WRITEMASK            = 0x0B98;
	public static final int STENCIL_BACK_FUNC            = 0x8800;
	public static final int STENCIL_BACK_FAIL            = 0x8801;
	public static final int STENCIL_VALUE_MASK           = 0x0B93;
	public static final int STENCIL_CLEAR_VALUE          = 0x0B91;
	public static final int STENCIL_BACK_WRITEMASK       = 0x8CA5;
	public static final int STENCIL_PASS_DEPTH_FAIL      = 0x0B95;
	public static final int STENCIL_PASS_DEPTH_PASS      = 0x0B96;
	public static final int STENCIL_BACK_VALUE_MASK      = 0x8CA4;
	public static final int STENCIL_BACK_PASS_DEPTH_FAIL = 0x8802;
	public static final int STENCIL_BACK_PASS_DEPTH_PASS = 0x8803;

	public static final int CCW            = 0x0901;
	public static final int FILL           = 0x1B02;
	public static final int FRONT          = 0x0404;
	public static final int FRONT_AND_BACK = 0x0408;

	public static final int PACK_ALIGNMENT     = 0x0D05;
	public static final int PACK_SKIP_ROWS     = 0x0D03;
	public static final int PACK_ROW_LENGTH    = 0x0D02;
	public static final int PACK_SKIP_PIXELS   = 0x0D04;
	public static final int UNPACK_ALIGNMENT   = 0x0CF5;
	public static final int UNPACK_SKIP_ROWS   = 0x0CF3;
	public static final int UNPACK_ROW_LENGTH  = 0x0CF2;
	public static final int UNPACK_SKIP_PIXELS = 0x0CF4;

	public static final int CURRENT_VERTEX_ATTRIB              = 0x8626;
	public static final int VERTEX_ATTRIB_ARRAY_SIZE           = 0x8623;
	public static final int VERTEX_ATTRIB_ARRAY_TYPE           = 0x8625;
	public static final int VERTEX_ATTRIB_ARRAY_STRIDE         = 0x8624;
	public static final int VERTEX_ATTRIB_ARRAY_ENABLED        = 0x8622;
	public static final int VERTEX_ATTRIB_ARRAY_POINTER        = 0x8645;
	public static final int VERTEX_ATTRIB_ARRAY_NORMALIZED     = 0x886A;
	public static final int VERTEX_ATTRIB_ARRAY_BUFFER_BINDING = 0x889F;

	public static final int FOG                 = 0x0B60;
	public static final int LIGHT0              = 0x4000;
	public static final int LIGHTING            = 0x0B50;
	public static final int ALPHA_TEST          = 0x0BC0;
	public static final int COLOR_ARRAY         = 0x8076;
	public static final int VERTEX_ARRAY        = 0x8074;
	public static final int NORMAL_ARRAY        = 0x8075;
	public static final int COLOR_MATERIAL      = 0x0B57;
	public static final int TEXTURE_COORD_ARRAY = 0x8078;

	public static final int AMBIENT                = 0x1200;
	public static final int DIFFUSE                = 0x1201;
	public static final int SHADE_MODEL            = 0x0B54;
	public static final int MATRIX_MODE            = 0x0BA0;
	public static final int CURRENT_COLOR          = 0x0B00;
	public static final int ALPHA_TEST_REF         = 0x0BC2;
	public static final int CURRENT_NORMAL         = 0x0B02;
	public static final int ALPHA_TEST_FUNC        = 0x0BC1;
	public static final int MODELVIEW_MATRIX       = 0x0BA6;
	public static final int PROJECTION_MATRIX      = 0x0BA7;
	public static final int CLIENT_ACTIVE_TEXTURE  = 0x84E1;
	public static final int CURRENT_TEXTURE_COORDS = 0x0B03;

}