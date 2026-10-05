package dev.joid.impl.lwjgl2.render;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Deque;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import dev.joid.impl.lwjgl2.render.framebuffer.FrameBuffer;
import dev.joid.impl.lwjgl2.render.shader.Shader;
import dev.joid.impl.lwjgl2.render.shader.ShaderTranslator;
import dev.joid.impl.lwjgl2.render.state.StateSnapshot;
import dev.joid.impl.lwjgl2.render.texture.Texture;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.StencilFunction;
import dev.joid.lib.bridge.render.state.StencilOperation;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.bridge.render.vertex.DrawMode;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.NonNull;

public final class RenderBridge implements IRenderBridge {

	private static final IntBuffer   VIEWPORT_BUFFER = BufferUtils.createIntBuffer(16);
	private static final FloatBuffer MATRIX_BUFFER   = BufferUtils.createFloatBuffer(16);
	private static final FloatBuffer AMBIENT_BUFFER  = (FloatBuffer) BufferUtils.createFloatBuffer(4).put(new float[] {0.6F, 0.6F, 0.6F, 1F}).flip();

	private final Deque<StateSnapshot> stateStack;

	private Texture emptyTexture;

	public RenderBridge() {
		this.stateStack = new ArrayDeque<>();
	}

	@Override
	public void popMatrix() {
		GL11.glPopMatrix();
	}

	@Override
	public void pushMatrix() {
		GL11.glPushMatrix();
	}

	@Override
	public void loadIdentity() {
		GL11.glLoadIdentity();
	}

	@Override
	public void translate(final double x, final double y, final double z) {
		GL11.glTranslated(x, y, z);
	}

	@Override
	public void scale(final double x, final double y, final double z) {
		GL11.glScaled(x, y, z);
	}

	@Override
	public void rotate(final double angle, final double x, final double y, final double z) {
		GL11.glRotated(angle, x, y, z);
	}

	@Override
	public void popProjection() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPopMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}

	@Override
	public void pushProjection() {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glPushMatrix();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}

	@Override
	public void ortho(final double left, final double right, final double bottom, final double top, final double near, final double far) {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(left, right, bottom, top, near, far);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}

	@Override
	public void popState() {
		this.stateStack.pop().restore();
	}

	@Override
	public void pushState() {
		this.stateStack.push(StateSnapshot.capture());
	}

	@Override
	public void color(final float red, final float green, final float blue, final float alpha) {
		GL11.glColor4f(red, green, blue, alpha);
	}

	@Override
	public void blend(final @NonNull BlendState state) {
		if (!state.isEnabled()) {
			GL11.glDisable(GL11.GL_BLEND);
			return;
		}

		GL11.glEnable(GL11.GL_BLEND);
		GL14.glBlendEquation(RenderBridge.equation(state.getEquation()));
		GL14.glBlendFuncSeparate(RenderBridge.factor(state.getSourceColor()), RenderBridge.factor(state.getDestinationColor()), RenderBridge.factor(state.getSourceAlpha()), RenderBridge.factor(state.getDestinationAlpha()));
	}

	@Override
	public void depth(final boolean test, final boolean write) {
		RenderBridge.toggle(GL11.GL_DEPTH_TEST, test);
		GL11.glDepthMask(write);
	}

	@Override
	public void cull(final boolean cull) {
		RenderBridge.toggle(GL11.GL_CULL_FACE, cull);
	}

	@Override
	public void lighting(final boolean lighting) {
		if (!lighting) {
			GL11.glDisable(GL11.GL_COLOR_MATERIAL);
			GL11.glDisable(GL11.GL_LIGHT1);
			GL11.glDisable(GL11.GL_LIGHT0);
			GL11.glDisable(GL11.GL_LIGHTING);
			return;
		}

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_LIGHT0);
		GL11.glEnable(GL11.GL_LIGHT1);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, RenderBridge.AMBIENT_BUFFER);
	}

	@Override
	public void colorMask(final boolean write) {
		GL11.glColorMask(write, write, write, write);
	}

	@Override
	public void alphaTest(final float threshold) {
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_GREATER, threshold);
	}

	@Override
	public void lineWidth(final float width) {
		GL11.glLineWidth(width);
	}

	@Override
	public void lineSmooth(final boolean smooth) {
		RenderBridge.toggle(GL11.GL_LINE_SMOOTH, smooth);
	}

	@Override
	public IShader getShader() {
		return Shader.fromProgram(GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM));
	}

	@Override
	public float getLineWidth() {
		return GL11.glGetFloat(GL11.GL_LINE_WIDTH);
	}

	@Override
	public int getViewportWidth() {
		GL11.glGetInteger(GL11.GL_VIEWPORT, RenderBridge.VIEWPORT_BUFFER);
		return RenderBridge.VIEWPORT_BUFFER.get(2);
	}

	@Override
	public int getViewportHeight() {
		GL11.glGetInteger(GL11.GL_VIEWPORT, RenderBridge.VIEWPORT_BUFFER);
		return RenderBridge.VIEWPORT_BUFFER.get(3);
	}

	@Override
	public @NonNull PixelGrid getPixelGrid() {
		return PixelGrid.of(RenderBridge.matrix(GL11.GL_PROJECTION_MATRIX), RenderBridge.matrix(GL11.GL_MODELVIEW_MATRIX), this.getViewportWidth(), this.getViewportHeight());
	}

	@Override
	public boolean isLineSmooth() {
		return GL11.glIsEnabled(GL11.GL_LINE_SMOOTH);
	}

	@Override
	public void stencilTest(final boolean test) {
		RenderBridge.toggle(GL11.GL_STENCIL_TEST, test);
	}

	@Override
	public void stencilFunction(final @NonNull StencilFunction function, final int reference, final int mask) {
		GL11.glStencilFunc(RenderBridge.function(function), reference, mask);
	}

	@Override
	public void stencilOperation(final @NonNull StencilOperation fail, final @NonNull StencilOperation depthFail, final @NonNull StencilOperation pass) {
		GL11.glStencilOp(RenderBridge.operation(fail), RenderBridge.operation(depthFail), RenderBridge.operation(pass));
	}

	@Override
	public void viewport(final int x, final int y, final int width, final int height) {
		GL11.glViewport(x, y, width, height);
	}

	@Override
	public void clear(final float red, final float green, final float blue, final float alpha) {
		GL11.glClearColor(red, green, blue, alpha);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void clearStencil() {
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
	}

	@Override
	public void frameBuffer(final IFrameBuffer frameBuffer) {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer == null ? 0 : ((FrameBuffer) frameBuffer).getId());
	}

	@Override
	public void texture(final @NonNull ITexture texture, final @NonNull TextureFilter filter, final @NonNull TextureWrap wrap) {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, ((Texture) texture).getId());
		RenderBridge.applyTextureParameters(filter, wrap, ((Texture) texture).isMipmapped());
	}

	@Override
	public void resetTexture() {
		if (this.emptyTexture == null) {
			this.emptyTexture = Texture.create().allocate(1, 1).upload(new int[] {0xFFFFFFFF}, 1, 1);
		}

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.emptyTexture.getId());
	}

	@Override
	public void shader(final IShader shader) {
		GL20.glUseProgram(shader == null ? 0 : ((Shader) shader).getProgram());
	}

	@Override
	public void draw(final @NonNull DrawMode mode, final @NonNull VertexBuffer buffer) {
		final ByteBuffer data = buffer.getBuffer();
		if (buffer.isTexture()) {
			data.position(VertexBuffer.TEXTURE_OFFSET);
			GL11.glTexCoordPointer(2, GL11.GL_FLOAT, VertexBuffer.STRIDE, data);
			GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		}

		if (buffer.isColor()) {
			data.position(VertexBuffer.COLOR_OFFSET);
			GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, VertexBuffer.STRIDE, data);
			GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
		}

		if (buffer.isNormal()) {
			data.position(VertexBuffer.NORMAL_OFFSET);
			GL11.glNormalPointer(GL11.GL_BYTE, VertexBuffer.STRIDE, data);
			GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
		}

		data.position(VertexBuffer.POSITION_OFFSET);
		GL11.glVertexPointer(3, GL11.GL_FLOAT, VertexBuffer.STRIDE, data);
		GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
		GL11.glDrawArrays(RenderBridge.mode(mode), 0, buffer.getCount());
		GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

		if (buffer.isTexture()) {
			GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
		}

		if (buffer.isColor()) {
			GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
		}

		if (buffer.isNormal()) {
			GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
		}
	}

	@Override
	public @NonNull ITexture createTexture() {
		return Texture.create();
	}

	@Override
	public @NonNull IFrameBuffer createFrameBuffer(final int width, final int height, final @NonNull TextureFilter filter) {
		return FrameBuffer.create(width, height, filter);
	}

	@Override
	public @NonNull IShader createShader(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment, final @NonNull BlendState blend) {
		return Shader.create(ShaderTranslator.translate(vertex), ShaderTranslator.translate(fragment), blend);
	}

	public static void toggle(final int capability, final boolean enabled) {
		if (enabled) {
			GL11.glEnable(capability);
		} else {
			GL11.glDisable(capability);
		}
	}

	public static void applyTextureParameters(final TextureFilter filter, final TextureWrap wrap) {
		RenderBridge.applyTextureParameters(filter, wrap, false);
	}

	public static void applyTextureParameters(final TextureFilter filter, final TextureWrap wrap, final boolean mipmapped) {
		final boolean linear = filter == TextureFilter.LINEAR;
		final int minFilter = linear ? (mipmapped ? GL11.GL_LINEAR_MIPMAP_LINEAR : GL11.GL_LINEAR) : GL11.GL_NEAREST;
		final int magFilter = linear ? GL11.GL_LINEAR : GL11.GL_NEAREST;
		final int textureWrap = RenderBridge.wrap(wrap);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, textureWrap);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, textureWrap);
	}

	private static int wrap(final TextureWrap wrap) {
		switch (wrap) {
		case CLAMP_TO_EDGE:
			return GL12.GL_CLAMP_TO_EDGE;
		case CLAMP_TO_BORDER:
			return GL11.GL_CLAMP;
		default:
			return GL11.GL_REPEAT;
		}
	}

	private static int mode(final DrawMode mode) {
		switch (mode) {
		case LINES:
			return GL11.GL_LINES;
		case LINE_STRIP:
			return GL11.GL_LINE_STRIP;
		case LINE_LOOP:
			return GL11.GL_LINE_LOOP;
		case QUADS:
			return GL11.GL_QUADS;
		case POLYGON:
			return GL11.GL_POLYGON;
		default:
			return GL11.GL_TRIANGLES;
		}
	}

	private static float[] matrix(final int name) {
		final float[] matrix = new float[16];
		RenderBridge.MATRIX_BUFFER.clear();
		GL11.glGetFloat(name, RenderBridge.MATRIX_BUFFER);
		RenderBridge.MATRIX_BUFFER.get(matrix);
		return matrix;
	}

	private static int equation(final BlendState.Equation equation) {
		switch (equation) {
		case SUBTRACT:
			return GL14.GL_FUNC_SUBTRACT;
		case REVERSE_SUBTRACT:
			return GL14.GL_FUNC_REVERSE_SUBTRACT;
		case MIN:
			return GL14.GL_MIN;
		case MAX:
			return GL14.GL_MAX;
		default:
			return GL14.GL_FUNC_ADD;
		}
	}

	private static int factor(final BlendState.Factor factor) {
		switch (factor) {
		case ZERO:
			return GL11.GL_ZERO;
		case SRC_COLOR:
			return GL11.GL_SRC_COLOR;
		case ONE_MINUS_SRC_COLOR:
			return GL11.GL_ONE_MINUS_SRC_COLOR;
		case DST_COLOR:
			return GL11.GL_DST_COLOR;
		case ONE_MINUS_DST_COLOR:
			return GL11.GL_ONE_MINUS_DST_COLOR;
		case SRC_ALPHA:
			return GL11.GL_SRC_ALPHA;
		case ONE_MINUS_SRC_ALPHA:
			return GL11.GL_ONE_MINUS_SRC_ALPHA;
		case DST_ALPHA:
			return GL11.GL_DST_ALPHA;
		case ONE_MINUS_DST_ALPHA:
			return GL11.GL_ONE_MINUS_DST_ALPHA;
		default:
			return GL11.GL_ONE;
		}
	}

	private static int function(final StencilFunction function) {
		switch (function) {
		case NEVER:
			return GL11.GL_NEVER;
		case LESS:
			return GL11.GL_LESS;
		case LESS_EQUAL:
			return GL11.GL_LEQUAL;
		case GREATER:
			return GL11.GL_GREATER;
		case GREATER_EQUAL:
			return GL11.GL_GEQUAL;
		case EQUAL:
			return GL11.GL_EQUAL;
		case NOT_EQUAL:
			return GL11.GL_NOTEQUAL;
		default:
			return GL11.GL_ALWAYS;
		}
	}

	private static int operation(final StencilOperation operation) {
		switch (operation) {
		case ZERO:
			return GL11.GL_ZERO;
		case REPLACE:
			return GL11.GL_REPLACE;
		case INCREMENT:
			return GL11.GL_INCR;
		case DECREMENT:
			return GL11.GL_DECR;
		case INVERT:
			return GL11.GL_INVERT;
		default:
			return GL11.GL_KEEP;
		}
	}

}