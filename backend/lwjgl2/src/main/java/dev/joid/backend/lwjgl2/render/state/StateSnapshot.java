package dev.joid.backend.lwjgl2.render.state;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import dev.joid.backend.lwjgl2.render.RenderBridge;
import lombok.NonNull;

public final class StateSnapshot {

	private static final IntBuffer   INT_BUFFER   = BufferUtils.createIntBuffer(16);
	private static final ByteBuffer  BYTE_BUFFER  = BufferUtils.createByteBuffer(16);
	private static final FloatBuffer FLOAT_BUFFER = BufferUtils.createFloatBuffer(16);

	private final int     program;
	private final int     texture;
	private final int[]   viewport;
	private final int     frameBuffer;
	private final boolean texture2D;

	private final float[]       color;
	private final float[]       clearColor;
	private final boolean[]     colorMask;
	private final BlendSnapshot blend;

	private final boolean cull;
	private final boolean depthTest;
	private final boolean depthWrite;

	private final boolean light0;
	private final boolean light1;
	private final int     shadeModel;
	private final float[] ambient;
	private final boolean lighting;
	private final boolean normalize;
	private final boolean colorMaterial;

	private final float   lineWidth;
	private final boolean alphaTest;
	private final int     alphaFunction;
	private final boolean lineSmooth;
	private final float   alphaReference;

	private final int     stencilMask;
	private final int     stencilFail;
	private final int     stencilPass;
	private final boolean stencilTest;
	private final int     stencilFunction;
	private final int     stencilReference;
	private final int     stencilDepthFail;

	private StateSnapshot() {
		this.program     = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
		this.frameBuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
		this.viewport    = StateSnapshot.getIntegers(GL11.GL_VIEWPORT, 4);
		this.texture2D   = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
		this.texture     = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

		this.color      = StateSnapshot.getFloats(GL11.GL_CURRENT_COLOR, 4);
		this.clearColor = StateSnapshot.getFloats(GL11.GL_COLOR_CLEAR_VALUE, 4);
		this.colorMask  = StateSnapshot.getBooleans(GL11.GL_COLOR_WRITEMASK, 4);
		this.blend      = BlendSnapshot.capture();

		this.depthTest  = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
		this.depthWrite = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
		this.cull       = GL11.glIsEnabled(GL11.GL_CULL_FACE);

		this.lighting      = GL11.glIsEnabled(GL11.GL_LIGHTING);
		this.light0        = GL11.glIsEnabled(GL11.GL_LIGHT0);
		this.light1        = GL11.glIsEnabled(GL11.GL_LIGHT1);
		this.normalize     = GL11.glIsEnabled(GL11.GL_NORMALIZE);
		this.colorMaterial = GL11.glIsEnabled(GL11.GL_COLOR_MATERIAL);
		this.shadeModel    = GL11.glGetInteger(GL11.GL_SHADE_MODEL);
		this.ambient       = StateSnapshot.getFloats(GL11.GL_LIGHT_MODEL_AMBIENT, 4);

		this.alphaTest      = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
		this.alphaFunction  = GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC);
		this.alphaReference = GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF);
		this.lineWidth      = GL11.glGetFloat(GL11.GL_LINE_WIDTH);
		this.lineSmooth     = GL11.glIsEnabled(GL11.GL_LINE_SMOOTH);

		this.stencilTest      = GL11.glIsEnabled(GL11.GL_STENCIL_TEST);
		this.stencilFunction  = GL11.glGetInteger(GL11.GL_STENCIL_FUNC);
		this.stencilReference = GL11.glGetInteger(GL11.GL_STENCIL_REF);
		this.stencilMask      = GL11.glGetInteger(GL11.GL_STENCIL_VALUE_MASK);
		this.stencilFail      = GL11.glGetInteger(GL11.GL_STENCIL_FAIL);
		this.stencilDepthFail = GL11.glGetInteger(GL11.GL_STENCIL_PASS_DEPTH_FAIL);
		this.stencilPass      = GL11.glGetInteger(GL11.GL_STENCIL_PASS_DEPTH_PASS);
	}

	public static @NonNull StateSnapshot capture() {
		return new StateSnapshot();
	}

	public void restore() {
		GL20.glUseProgram(this.program);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, this.frameBuffer);
		GL11.glViewport(this.viewport[0], this.viewport[1], this.viewport[2], this.viewport[3]);
		RenderBridge.toggle(GL11.GL_TEXTURE_2D, this.texture2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.texture);

		GL11.glColor4f(this.color[0], this.color[1], this.color[2], this.color[3]);
		GL11.glClearColor(this.clearColor[0], this.clearColor[1], this.clearColor[2], this.clearColor[3]);
		GL11.glColorMask(this.colorMask[0], this.colorMask[1], this.colorMask[2], this.colorMask[3]);
		this.blend.restore();

		RenderBridge.toggle(GL11.GL_DEPTH_TEST, this.depthTest);
		GL11.glDepthMask(this.depthWrite);
		RenderBridge.toggle(GL11.GL_CULL_FACE, this.cull);

		RenderBridge.toggle(GL11.GL_LIGHTING, this.lighting);
		RenderBridge.toggle(GL11.GL_LIGHT0, this.light0);
		RenderBridge.toggle(GL11.GL_LIGHT1, this.light1);
		RenderBridge.toggle(GL11.GL_NORMALIZE, this.normalize);
		RenderBridge.toggle(GL11.GL_COLOR_MATERIAL, this.colorMaterial);
		GL11.glShadeModel(this.shadeModel);
		StateSnapshot.FLOAT_BUFFER.clear();
		StateSnapshot.FLOAT_BUFFER.put(this.ambient).flip();
		GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, StateSnapshot.FLOAT_BUFFER);

		RenderBridge.toggle(GL11.GL_ALPHA_TEST, this.alphaTest);
		GL11.glAlphaFunc(this.alphaFunction, this.alphaReference);
		GL11.glLineWidth(this.lineWidth);
		RenderBridge.toggle(GL11.GL_LINE_SMOOTH, this.lineSmooth);

		RenderBridge.toggle(GL11.GL_STENCIL_TEST, this.stencilTest);
		GL11.glStencilFunc(this.stencilFunction, this.stencilReference, this.stencilMask);
		GL11.glStencilOp(this.stencilFail, this.stencilDepthFail, this.stencilPass);
	}

	private static float[] getFloats(final int name, final int count) {
		StateSnapshot.FLOAT_BUFFER.clear();
		GL11.glGetFloat(name, StateSnapshot.FLOAT_BUFFER);
		final float[] values = new float[count];
		StateSnapshot.FLOAT_BUFFER.get(values);
		return values;
	}

	private static int[] getIntegers(final int name, final int count) {
		StateSnapshot.INT_BUFFER.clear();
		GL11.glGetInteger(name, StateSnapshot.INT_BUFFER);
		final int[] values = new int[count];
		StateSnapshot.INT_BUFFER.get(values);
		return values;
	}

	private static boolean[] getBooleans(final int name, final int count) {
		StateSnapshot.BYTE_BUFFER.clear();
		GL11.glGetBoolean(name, StateSnapshot.BYTE_BUFFER);
		final boolean[] values = new boolean[count];
		for (int i = 0; i < count; i++) {
			values[i] = StateSnapshot.BYTE_BUFFER.get(i) != 0;
		}
		return values;
	}

}