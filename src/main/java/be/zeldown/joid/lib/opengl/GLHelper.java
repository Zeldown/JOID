package be.zeldown.joid.lib.opengl;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.color.Color;
import lombok.NonNull;

public class GLHelper {

	public static void pushMatrix() {
		GL11.glPushMatrix();
	}

	public static void popMatrix() {
		GL11.glPopMatrix();
	}

	public static void pushAttrib(final Integer... attributes) {
		int combinedAttribs = 0;
		if (attributes != null && attributes.length > 0) {
			for (final Integer attrib : attributes) {
				if (attrib != null && (combinedAttribs & attrib) == 0) {
					combinedAttribs |= attrib;
				}
			}
		} else {
			combinedAttribs = GL11.GL_ALL_ATTRIB_BITS;
		}

		GL11.glPushAttrib(combinedAttribs);
	}

	public static void popAttrib() {
		GL11.glPopAttrib();
	}

	public static void push() {
		GLHelper.popColor();
		GLHelper.pushMatrix();
		GLHelper.pushAttrib();
	}

	public static void push(final Integer... attributes) {
		GLHelper.popColor();
		GLHelper.pushMatrix();
		GLHelper.pushAttrib(attributes);
	}

	public static void pop() {
		GLHelper.popAttrib();
		GLHelper.popMatrix();
		GLHelper.popColor();
	}

	public static void popColor() {
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}

	public static void popOpacity() {
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}

	public static void pushOpacity(final float opacity) {
		GL11.glColor4f(1F, 1F, 1F, opacity);
	}

	public static void pushAlpha(final float alpha) {
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_GREATER, alpha);
	}

	public static void popAlpha() {
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
	}

	public static void pushTexture(final int textureId) {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
	}

	public static void pushTexture(final int textureId, final boolean linear) {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		if (linear) {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		} else {
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		}
	}

	public static void popTexture() {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
	}

	public static void clear(final int mask) {
		GL11.glClear(mask);
	}

	public static void enable(final @NonNull Integer @NonNull... capabilities) {
		for (final Integer capability : capabilities) {
			GL11.glEnable(capability);
		}
	}

	public static void disable(final @NonNull Integer @NonNull... capabilities) {
		for (final Integer capability : capabilities) {
			GL11.glDisable(capability);
		}
	}

	public static boolean isEnabled(final @NonNull Integer @NonNull... capabilities) {
		for (final Integer capability : capabilities) {
			if (!GL11.glIsEnabled(capability)) {
				return false;
			}
		}
		return true;
	}

	public static void blend() {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}

	public static void blend(final int sfactor, final int dfactor) {
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(sfactor, dfactor);
	}

	public static void color(final @NonNull Color color) {
		GL11.glColor4f(color.r, color.g, color.b, color.a);
	}

	public static void color(final float red, final float green, final float blue, final float alpha) {
		GL11.glColor4f(red, green, blue, alpha);
	}

	public static void lineWidth(final float width) {
		GL11.glLineWidth(width);
	}

	public static void colorMaterial(final int face, final int mode) {
		GL11.glColorMaterial(face, mode);
	}

	public static void colorMaterial() {
		GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
	}

	public static void shadeModel(final int mode) {
		GL11.glShadeModel(mode);
	}

	public static void lightModel(final int pname, final @NonNull FloatBuffer params) {
		GL11.glLightModel(pname, params);
	}

	public static void depthMask(final boolean flag) {
		GL11.glDepthMask(flag);
	}

	public static void depthFunc(final int func) {
		GL11.glDepthFunc(func);
	}

	public static void translate(final double x, final double y, final double z) {
		GL11.glTranslated(x, y, z);
	}

	public static void translate(final double x, final double y) {
		GL11.glTranslated(x, y, 0);
	}

	public static void translateX(final double x) {
		GL11.glTranslated(x, 0, 0);
	}

	public static void translateY(final double y) {
		GL11.glTranslated(0, y, 0);
	}

	public static void translateZ(final double z) {
		GL11.glTranslated(0, 0, z);
	}

	public static void scale(final double x, final double y, final double z) {
		GL11.glScaled(x, y, z);
	}

	public static void scale(final double x, final double y) {
		GL11.glScaled(x, y, 1);
	}

	public static void scaleX(final double x) {
		GL11.glScaled(x, 1, 1);
	}

	public static void scaleY(final double y) {
		GL11.glScaled(1, y, 1);
	}

	public static void scaleZ(final double z) {
		GL11.glScaled(1, 1, z);
	}

	public static void rotate(final double angle, final double x, final double y, final double z) {
		GL11.glRotated(angle, x, y, z);
	}

	public static void rotateX(final double angle) {
		GL11.glRotated(angle, 1, 0, 0);
	}

	public static void rotateY(final double angle) {
		GL11.glRotated(angle, 0, 1, 0);
	}

	public static void rotateZ(final double angle) {
		GL11.glRotated(angle, 0, 0, 1);
	}

	public static class GlAttrib {

		public static int GL_ALPHA_FUNC        = GL11.GL_COLOR_BUFFER_BIT;
		public static int GL_BLEND_FUNC        = GL11.GL_COLOR_BUFFER_BIT;
		public static int GL_DITHER_FUNC       = GL11.GL_COLOR_BUFFER_BIT;
		public static int GL_DRAW_BUFFER       = GL11.GL_COLOR_BUFFER_BIT;
		public static int GL_COLOR_LOGIC_FUNC  = GL11.GL_COLOR_BUFFER_BIT;
		public static int GL_INDEX_LOGIC_FUNC  = GL11.GL_COLOR_BUFFER_BIT;
		public static int GL_BLEND_COLOR       = GL11.GL_COLOR_BUFFER_BIT;
		public static int GL_BLEND_EQUATION    = GL11.GL_COLOR_BUFFER_BIT;
		public static int GL_COLOR_CLEAR_VALUE = GL11.GL_COLOR_BUFFER_BIT;
		public static int GL_COLOR_WRITEMASK   = GL11.GL_COLOR_BUFFER_BIT;

		public static int GL_COLOR                         = GL11.GL_CURRENT_BIT;
		public static int GL_COLOR_INDEX                   = GL11.GL_CURRENT_BIT;
		public static int GL_NORMAL                        = GL11.GL_CURRENT_BIT;
		public static int GL_TEXTURE_COORDS                = GL11.GL_CURRENT_BIT;
		public static int GL_RASTER_POSITION               = GL11.GL_CURRENT_BIT;
		public static int GL_CURRENT_RASTER_POSITION_VALID = GL11.GL_CURRENT_BIT;
		public static int GL_RASTER_COLOR                  = GL11.GL_CURRENT_BIT;
		public static int GL_RASTER_INDEX                  = GL11.GL_CURRENT_BIT;
		public static int GL_RASTER_TEXTURE_COORDS         = GL11.GL_CURRENT_BIT;
		public static int GL_EDGE_FLAG                     = GL11.GL_CURRENT_BIT;

		public static int GL_DEPTH_FUNC        = GL11.GL_DEPTH_BUFFER_BIT;
		public static int GL_DEPTH_CLEAR_VALUE = GL11.GL_DEPTH_BUFFER_BIT;
		public static int GL_DEPTH_MASK        = GL11.GL_DEPTH_BUFFER_BIT;

		public static int GL_ALPHA_TEST           = GL11.GL_ENABLE_BIT;
		public static int GL_AUTO_NORMAL          = GL11.GL_ENABLE_BIT;
		public static int GL_BLEND                = GL11.GL_ENABLE_BIT;
		public static int GL_CLIP_PLANE           = GL11.GL_ENABLE_BIT;
		public static int GL_COLOR_MATERIAL       = GL11.GL_ENABLE_BIT;
		public static int GL_CULL_FACE            = GL11.GL_ENABLE_BIT;
		public static int GL_DEPTH_TEST           = GL11.GL_ENABLE_BIT;
		public static int GL_DITHER               = GL11.GL_ENABLE_BIT;
		public static int GL_FOG                  = GL11.GL_ENABLE_BIT;
		public static int GL_LIGHT                = GL11.GL_ENABLE_BIT;
		public static int GL_LIGHTING             = GL11.GL_ENABLE_BIT;
		public static int GL_LINE_SMOOTH          = GL11.GL_ENABLE_BIT;
		public static int GL_LINE_STIPPLE         = GL11.GL_ENABLE_BIT;
		public static int GL_COLOR_LOGIC_OP       = GL11.GL_ENABLE_BIT;
		public static int GL_INDEX_LOGIC_OP       = GL11.GL_ENABLE_BIT;
		public static int GL_MAP                  = GL11.GL_ENABLE_BIT;
		public static int GL_MULTISAMPLE          = GL11.GL_ENABLE_BIT;
		public static int GL_NORMALIZE            = GL11.GL_ENABLE_BIT;
		public static int GL_POINT_SMOOTH         = GL11.GL_ENABLE_BIT;
		public static int GL_POLYGON_OFFSET_LINE  = GL11.GL_ENABLE_BIT;
		public static int GL_POLYGON_OFFSET_FILL  = GL11.GL_ENABLE_BIT;
		public static int GL_POLYGON_OFFSET_POINT = GL11.GL_ENABLE_BIT;
		public static int GL_POLYGON_SMOOTH       = GL11.GL_ENABLE_BIT;
		public static int GL_POLYGON_STIPPLE      = GL11.GL_ENABLE_BIT;
		public static int GL_SAMPLE_ALPHA         = GL11.GL_ENABLE_BIT;
		public static int GL_SAMPLE_COVERAGE      = GL11.GL_ENABLE_BIT;
		public static int GL_SCISSOR_TEST         = GL11.GL_ENABLE_BIT;
		public static int GL_STENCIL_TEST         = GL11.GL_ENABLE_BIT;
		public static int GL_TEXTURE_1D           = GL11.GL_ENABLE_BIT;
		public static int GL_TEXTURE_2D           = GL11.GL_ENABLE_BIT;
		public static int GL_TEXTURE_3D           = GL11.GL_ENABLE_BIT;
		public static int GL_TEXTURE_GEN_S        = GL11.GL_ENABLE_BIT;
		public static int GL_TEXTURE_GEN_T        = GL11.GL_ENABLE_BIT;
		public static int GL_TEXTURE_GEN_R        = GL11.GL_ENABLE_BIT;
		public static int GL_TEXTURE_GEN_Q        = GL11.GL_ENABLE_BIT;

		public static int GL_MAP_FUNC         = GL11.GL_EVAL_BIT;
		public static int GL_AUTO_NORMAL_FUNC = GL11.GL_EVAL_BIT;
		public static int GL_GRID_1D          = GL11.GL_EVAL_BIT;
		public static int GL_GRID_2D          = GL11.GL_EVAL_BIT;

		public static int GL_FOG_FUNC    = GL11.GL_FOG_BIT;
		public static int GL_FOG_COLOR   = GL11.GL_FOG_BIT;
		public static int GL_FOG_DENSITY = GL11.GL_FOG_BIT;
		public static int GL_FOG_START   = GL11.GL_FOG_BIT;
		public static int GL_FOG_END     = GL11.GL_FOG_BIT;
		public static int GL_FOG_INDEX   = GL11.GL_FOG_BIT;
		public static int GL_FOG_MODE    = GL11.GL_FOG_BIT;

		public static int GL_PERSPECTIVE_CORRECTION_HINT = GL11.GL_HINT_BIT;
		public static int GL_POINT_SMOOTH_HINT           = GL11.GL_HINT_BIT;
		public static int GL_LINE_SMOOTH_HINT            = GL11.GL_HINT_BIT;
		public static int GL_POLYGON_SMOOTH_HINT         = GL11.GL_HINT_BIT;
		public static int GL_FOG_HINT                    = GL11.GL_HINT_BIT;
		public static int GL_GENERATE_MIPMAP_HINT        = GL11.GL_HINT_BIT;
		public static int GL_TEXTURE_COMPRESSION_HINT    = GL11.GL_HINT_BIT;

		public static int GL_COLOR_MATERIAL_FACE         = GL11.GL_LIGHTING_BIT;
		public static int GL_COLOR_MATERIAL_PARAMETER    = GL11.GL_LIGHTING_BIT;
		public static int GL_LIGHT_MODEL_AMBIENT         = GL11.GL_LIGHTING_BIT;
		public static int GL_LIGHT_MODEL_LOCAL_VIEWER    = GL11.GL_LIGHTING_BIT;
		public static int GL_LIGHT_MODEL_TWO_SIDE        = GL11.GL_LIGHTING_BIT;
		public static int GL_LIGHT_PROPERTIES            = GL11.GL_LIGHTING_BIT;
		public static int GL_MATERIAL_PROPERTIES         = GL11.GL_LIGHTING_BIT;
		public static int GL_SHADE_MODEL                 = GL11.GL_LIGHTING_BIT;

		public static int GL_LINE_STIPPLE_PATTERN = GL11.GL_LINE_BIT;
		public static int GL_LINE_WIDTH           = GL11.GL_LINE_BIT;

		public static int GL_RED_BIAS     = GL11.GL_PIXEL_MODE_BIT;
		public static int GL_RED_SCALE    = GL11.GL_PIXEL_MODE_BIT;
		public static int GL_GREEN_BIAS   = GL11.GL_PIXEL_MODE_BIT;
		public static int GL_GREEN_SCALE  = GL11.GL_PIXEL_MODE_BIT;
		public static int GL_BLUE_BIAS    = GL11.GL_PIXEL_MODE_BIT;
		public static int GL_BLUE_SCALE   = GL11.GL_PIXEL_MODE_BIT;
		public static int GL_ALPHA_BIAS   = GL11.GL_PIXEL_MODE_BIT;
		public static int GL_ALPHA_SCALE  = GL11.GL_PIXEL_MODE_BIT;
		public static int GL_DEPTH_BIAS   = GL11.GL_PIXEL_MODE_BIT;
		public static int GL_DEPTH_SCALE  = GL11.GL_PIXEL_MODE_BIT;
		public static int GL_INDEX_OFFSET = GL11.GL_PIXEL_MODE_BIT;
		public static int GL_INDEX_SHIFT  = GL11.GL_PIXEL_MODE_BIT;
		public static int GL_MAP_COLOR    = GL11.GL_PIXEL_MODE_BIT;
		public static int GL_MAP_STENCIL  = GL11.GL_PIXEL_MODE_BIT;
		public static int GL_ZOOM_X       = GL11.GL_PIXEL_MODE_BIT;
		public static int GL_ZOOM_Y       = GL11.GL_PIXEL_MODE_BIT;
		public static int GL_READ_BUFFER  = GL11.GL_PIXEL_MODE_BIT;

		public static int GL_CULL_FACE_MODE        = GL11.GL_POLYGON_BIT;
		public static int GL_FRONT_FACE            = GL11.GL_POLYGON_BIT;
		public static int GL_POLYGON_MODE          = GL11.GL_POLYGON_BIT;
		public static int GL_POLYGON_OFFSET_FACTOR = GL11.GL_POLYGON_BIT;
		public static int GL_POLYGON_OFFSET_UNITS  = GL11.GL_POLYGON_BIT;

		public static int GL_STENCIL_FUNC            = GL11.GL_STENCIL_BUFFER_BIT;
		public static int GL_STENCIL_VALUE_MASK      = GL11.GL_STENCIL_BUFFER_BIT;
		public static int GL_STENCIL_FAIL            = GL11.GL_STENCIL_BUFFER_BIT;
		public static int GL_STENCIL_PASS_DEPTH_FAIL = GL11.GL_STENCIL_BUFFER_BIT;
		public static int GL_STENCIL_PASS_DEPTH_PASS = GL11.GL_STENCIL_BUFFER_BIT;
		public static int GL_STENCIL_CLEAR_VALUE     = GL11.GL_STENCIL_BUFFER_BIT;
		public static int GL_STENCIL_WRITEMASK       = GL11.GL_STENCIL_BUFFER_BIT;

		public static int GL_TEXTURE_BORDER_COLOR = GL11.GL_TEXTURE_BIT;
		public static int GL_TEXTURE_MIN_FILTER   = GL11.GL_TEXTURE_BIT;
		public static int GL_TEXTURE_MAG_FILTER   = GL11.GL_TEXTURE_BIT;
		public static int GL_TEXTURE_WRAP         = GL11.GL_TEXTURE_BIT;
		public static int GL_TEXTURE_ENV_COLOR    = GL11.GL_TEXTURE_BIT;
		public static int GL_TEXTURE_ENV_MODE     = GL11.GL_TEXTURE_BIT;
		public static int GL_TEXTURE_GEN_MODE     = GL11.GL_TEXTURE_BIT;
		public static int GL_TEXTURE_GEN_PLANE    = GL11.GL_TEXTURE_BIT;
		public static int GL_TEXTURE_BINDING      = GL11.GL_TEXTURE_BIT;

		public static int GL_CLIPPING_PLANES    = GL11.GL_TRANSFORM_BIT;
		public static int GL_MATRIX_MODE        = GL11.GL_TRANSFORM_BIT;
		public static int GL_RESCALE_NORMAL     = GL11.GL_TRANSFORM_BIT;

		public static int GL_DEPTH_RANGE = GL11.GL_VIEWPORT_BIT;
		public static int GL_VIEWPORT    = GL11.GL_VIEWPORT_BIT;

		public static int GL_LIST_BASE               = GL11.GL_LIST_BIT;
		public static int GL_POINT_SIZE              = GL11.GL_POINT_BIT;
		public static int GL_ACCUMULATION            = GL11.GL_ACCUM_BUFFER_BIT;
		public static int GL_POLYGON_STIPPLE_PATTERN = GL11.GL_POLYGON_STIPPLE_BIT;
		public static int GL_SCISSOR_BOX             = GL11.GL_SCISSOR_BIT;

	}

}