package be.zeldown.joid.impl.lwjgl3.render;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL14C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL33C;

import be.zeldown.joid.impl.lwjgl3.render.framebuffer.LWJGL3FrameBuffer;
import be.zeldown.joid.impl.lwjgl3.render.shader.LWJGL3Shader;
import be.zeldown.joid.impl.lwjgl3.render.shader.LWJGL3ShaderTranslator;
import be.zeldown.joid.impl.lwjgl3.render.texture.LWJGL3Texture;
import be.zeldown.joid.lib.bridge.render.RenderBridge;
import be.zeldown.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import be.zeldown.joid.lib.bridge.render.shader.IShader;
import be.zeldown.joid.lib.bridge.render.shader.source.ShaderSource;
import be.zeldown.joid.lib.bridge.render.shader.source.ShaderStage;
import be.zeldown.joid.lib.bridge.render.state.BlendState;
import be.zeldown.joid.lib.bridge.render.state.RenderState;
import be.zeldown.joid.lib.bridge.render.state.StencilFunction;
import be.zeldown.joid.lib.bridge.render.state.StencilOperation;
import be.zeldown.joid.lib.bridge.render.texture.ITexture;
import be.zeldown.joid.lib.bridge.render.texture.TextureFilter;
import be.zeldown.joid.lib.bridge.render.texture.TextureWrap;
import be.zeldown.joid.lib.bridge.render.vertex.DrawMode;
import be.zeldown.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.NonNull;

public final class LWJGL3RenderBridge extends RenderBridge {

	public static final int POSITION_LOCATION = 0;
	public static final int TEXTURE_LOCATION  = 1;
	public static final int COLOR_LOCATION    = 2;
	public static final int NORMAL_LOCATION   = 3;

	private final int           vertexArray;
	private final int           vertexBuffer;
	private final int[]         samplers;
	private final float[]       aliasedLineWidthRange;
	private final float[]       smoothLineWidthRange;
	private final LWJGL3Texture emptyTexture;
	private final LWJGL3Shader  fixedShader;

	public LWJGL3RenderBridge() {
		this.vertexArray           = GL30C.glGenVertexArrays();
		this.vertexBuffer          = GL15C.glGenBuffers();
		this.samplers              = LWJGL3RenderBridge.createSamplers();
		this.aliasedLineWidthRange = LWJGL3RenderBridge.getFloats(GL12C.GL_ALIASED_LINE_WIDTH_RANGE);
		this.smoothLineWidthRange  = LWJGL3RenderBridge.getFloats(GL12C.GL_SMOOTH_LINE_WIDTH_RANGE);
		this.emptyTexture          = LWJGL3Texture.create().allocate(1, 1).upload(new int[] {0xFFFFFFFF}, 1, 1);
		this.fixedShader           = (LWJGL3Shader) this.createShader(ShaderSource.read(ShaderStage.VERTEX, LWJGL3RenderBridge.class.getResourceAsStream("/assets/shaders/fixed/fixed.vsh")), ShaderSource.read(ShaderStage.FRAGMENT, LWJGL3RenderBridge.class.getResourceAsStream("/assets/shaders/fixed/fixed.fsh")), BlendState.DISABLED);

		GL30C.glBindVertexArray(this.vertexArray);
		GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.vertexBuffer);
		GL20C.glVertexAttribPointer(LWJGL3RenderBridge.POSITION_LOCATION, 3, GL11C.GL_FLOAT, false, VertexBuffer.STRIDE, VertexBuffer.POSITION_OFFSET);
		GL20C.glVertexAttribPointer(LWJGL3RenderBridge.TEXTURE_LOCATION, 2, GL11C.GL_FLOAT, false, VertexBuffer.STRIDE, VertexBuffer.TEXTURE_OFFSET);
		GL20C.glVertexAttribPointer(LWJGL3RenderBridge.COLOR_LOCATION, 4, GL11C.GL_UNSIGNED_BYTE, true, VertexBuffer.STRIDE, VertexBuffer.COLOR_OFFSET);
		GL20C.glVertexAttribPointer(LWJGL3RenderBridge.NORMAL_LOCATION, 3, GL11C.GL_BYTE, true, VertexBuffer.STRIDE, VertexBuffer.NORMAL_OFFSET);
		GL20C.glEnableVertexAttribArray(LWJGL3RenderBridge.POSITION_LOCATION);
	}

	@Override
	public void clear(final float red, final float green, final float blue, final float alpha) {
		final RenderState state = super.getState();
		this.applyTarget(state);
		GL11C.glColorMask(state.isColorMask(), state.isColorMask(), state.isColorMask(), state.isColorMask());
		GL11C.glClearColor(red, green, blue, alpha);
		GL11C.glClear(GL11C.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void clearStencil() {
		this.applyTarget(super.getState());
		GL11C.glStencilMask(0xFF);
		GL11C.glClear(GL11C.GL_STENCIL_BUFFER_BIT);
	}

	@Override
	public void draw(final @NonNull DrawMode mode, final @NonNull VertexBuffer buffer) {
		final RenderState state = super.getState();
		this.applyTarget(state);
		this.applyPipeline(state);

		final LWJGL3Shader shader = state.getShader() == null ? this.fixedShader : (LWJGL3Shader) state.getShader();
		shader.use(state);

		final LWJGL3Texture texture = state.getTexture() == null ? this.emptyTexture : (LWJGL3Texture) state.getTexture();
		GL13C.glActiveTexture(GL13C.GL_TEXTURE0);
		GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, texture.getId());
		GL33C.glBindSampler(0, this.getSampler(state.getTextureFilter(), state.getTextureWrap()));

		GL30C.glBindVertexArray(this.vertexArray);
		GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.vertexBuffer);
		GL15C.glBufferData(GL15C.GL_ARRAY_BUFFER, buffer.getBuffer(), GL15C.GL_STREAM_DRAW);
		LWJGL3RenderBridge.toggleAttribute(LWJGL3RenderBridge.TEXTURE_LOCATION, buffer.isTexture());
		LWJGL3RenderBridge.toggleAttribute(LWJGL3RenderBridge.COLOR_LOCATION, buffer.isColor());
		LWJGL3RenderBridge.toggleAttribute(LWJGL3RenderBridge.NORMAL_LOCATION, buffer.isNormal());
		GL20C.glVertexAttrib2f(LWJGL3RenderBridge.TEXTURE_LOCATION, 0F, 0F);
		GL20C.glVertexAttrib4f(LWJGL3RenderBridge.COLOR_LOCATION, state.getRed(), state.getGreen(), state.getBlue(), state.getAlpha());
		GL20C.glVertexAttrib3f(LWJGL3RenderBridge.NORMAL_LOCATION, 0F, 0F, 1F);
		GL11C.glDrawArrays(LWJGL3RenderBridge.mode(mode), 0, buffer.getCount());
	}

	@Override
	public @NonNull ITexture createTexture() {
		return LWJGL3Texture.create();
	}

	@Override
	public @NonNull IFrameBuffer createFrameBuffer(final int width, final int height, final @NonNull TextureFilter filter) {
		return LWJGL3FrameBuffer.create(width, height);
	}

	@Override
	public @NonNull IShader createShader(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment, final @NonNull BlendState blend) {
		return LWJGL3Shader.create(this, LWJGL3ShaderTranslator.translate(vertex), LWJGL3ShaderTranslator.translate(fragment), blend);
	}

	public int getSampler(final TextureFilter filter, final TextureWrap wrap) {
		return this.samplers[LWJGL3RenderBridge.getSamplerIndex(filter, wrap)];
	}

	private void applyTarget(final RenderState state) {
		GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, state.getFrameBuffer() == null ? 0 : ((LWJGL3FrameBuffer) state.getFrameBuffer()).getId());
		GL11C.glViewport(state.getViewportX(), state.getViewportY(), state.getViewportWidth(), state.getViewportHeight());
	}

	private void applyPipeline(final RenderState state) {
		final BlendState blend = state.getBlend();
		LWJGL3RenderBridge.toggle(GL11C.GL_BLEND, blend.isEnabled());
		if (blend.isEnabled()) {
			GL14C.glBlendEquation(LWJGL3RenderBridge.equation(blend.getEquation()));
			GL14C.glBlendFuncSeparate(LWJGL3RenderBridge.factor(blend.getSourceColor()), LWJGL3RenderBridge.factor(blend.getDestinationColor()), LWJGL3RenderBridge.factor(blend.getSourceAlpha()), LWJGL3RenderBridge.factor(blend.getDestinationAlpha()));
		}

		LWJGL3RenderBridge.toggle(GL11C.GL_DEPTH_TEST, state.isDepthTest());
		GL11C.glDepthMask(state.isDepthWrite());
		LWJGL3RenderBridge.toggle(GL11C.GL_CULL_FACE, state.isCull());
		GL11C.glColorMask(state.isColorMask(), state.isColorMask(), state.isColorMask(), state.isColorMask());

		LWJGL3RenderBridge.toggle(GL11C.GL_STENCIL_TEST, state.isStencilTest());
		GL11C.glStencilFunc(LWJGL3RenderBridge.function(state.getStencilFunction()), state.getStencilReference(), state.getStencilMask());
		GL11C.glStencilOp(LWJGL3RenderBridge.operation(state.getStencilFail()), LWJGL3RenderBridge.operation(state.getStencilDepthFail()), LWJGL3RenderBridge.operation(state.getStencilPass()));

		final float[] lineWidthRange = state.isLineSmooth() ? this.smoothLineWidthRange : this.aliasedLineWidthRange;
		GL11C.glLineWidth(Math.max(lineWidthRange[0], Math.min(lineWidthRange[1], state.getLineWidth())));
		LWJGL3RenderBridge.toggle(GL11C.GL_LINE_SMOOTH, state.isLineSmooth());
	}

	private static int[] createSamplers() {
		final int[] samplers = new int[TextureFilter.values().length * TextureWrap.values().length];
		for (final TextureFilter filter : TextureFilter.values()) {
			for (final TextureWrap wrap : TextureWrap.values()) {
				final int sampler = GL33C.glGenSamplers();
				final int textureFilter = filter == TextureFilter.LINEAR ? GL11C.GL_LINEAR : GL11C.GL_NEAREST;
				final int textureWrap = LWJGL3RenderBridge.wrap(wrap);
				GL33C.glSamplerParameteri(sampler, GL11C.GL_TEXTURE_MIN_FILTER, textureFilter);
				GL33C.glSamplerParameteri(sampler, GL11C.GL_TEXTURE_MAG_FILTER, textureFilter);
				GL33C.glSamplerParameteri(sampler, GL11C.GL_TEXTURE_WRAP_S, textureWrap);
				GL33C.glSamplerParameteri(sampler, GL11C.GL_TEXTURE_WRAP_T, textureWrap);
				samplers[LWJGL3RenderBridge.getSamplerIndex(filter, wrap)] = sampler;
			}
		}
		return samplers;
	}

	private static int getSamplerIndex(final TextureFilter filter, final TextureWrap wrap) {
		return filter.ordinal() * TextureWrap.values().length + wrap.ordinal();
	}

	private static float[] getFloats(final int name) {
		final float[] values = new float[2];
		GL11C.glGetFloatv(name, values);
		return values;
	}

	private static void toggle(final int capability, final boolean enabled) {
		if (enabled) {
			GL11C.glEnable(capability);
		} else {
			GL11C.glDisable(capability);
		}
	}

	private static void toggleAttribute(final int location, final boolean enabled) {
		if (enabled) {
			GL20C.glEnableVertexAttribArray(location);
		} else {
			GL20C.glDisableVertexAttribArray(location);
		}
	}

	private static int wrap(final TextureWrap wrap) {
		switch (wrap) {
		case CLAMP_TO_EDGE:
			return GL12C.GL_CLAMP_TO_EDGE;
		case CLAMP_TO_BORDER:
			return GL13C.GL_CLAMP_TO_BORDER;
		default:
			return GL11C.GL_REPEAT;
		}
	}

	private static int mode(final DrawMode mode) {
		switch (mode) {
		case LINES:
			return GL11C.GL_LINES;
		case LINE_STRIP:
			return GL11C.GL_LINE_STRIP;
		case LINE_LOOP:
			return GL11C.GL_LINE_LOOP;
		case POLYGON:
			return GL11C.GL_TRIANGLE_FAN;
		case QUADS:
			throw new IllegalArgumentException("QUADS are not supported by OpenGL core, triangulate them before drawing");
		default:
			return GL11C.GL_TRIANGLES;
		}
	}

	private static int equation(final BlendState.Equation equation) {
		switch (equation) {
		case SUBTRACT:
			return GL14C.GL_FUNC_SUBTRACT;
		case REVERSE_SUBTRACT:
			return GL14C.GL_FUNC_REVERSE_SUBTRACT;
		case MIN:
			return GL14C.GL_MIN;
		case MAX:
			return GL14C.GL_MAX;
		default:
			return GL14C.GL_FUNC_ADD;
		}
	}

	private static int factor(final BlendState.Factor factor) {
		switch (factor) {
		case ZERO:
			return GL11C.GL_ZERO;
		case SRC_COLOR:
			return GL11C.GL_SRC_COLOR;
		case ONE_MINUS_SRC_COLOR:
			return GL11C.GL_ONE_MINUS_SRC_COLOR;
		case DST_COLOR:
			return GL11C.GL_DST_COLOR;
		case ONE_MINUS_DST_COLOR:
			return GL11C.GL_ONE_MINUS_DST_COLOR;
		case SRC_ALPHA:
			return GL11C.GL_SRC_ALPHA;
		case ONE_MINUS_SRC_ALPHA:
			return GL11C.GL_ONE_MINUS_SRC_ALPHA;
		case DST_ALPHA:
			return GL11C.GL_DST_ALPHA;
		case ONE_MINUS_DST_ALPHA:
			return GL11C.GL_ONE_MINUS_DST_ALPHA;
		default:
			return GL11C.GL_ONE;
		}
	}

	private static int function(final StencilFunction function) {
		switch (function) {
		case NEVER:
			return GL11C.GL_NEVER;
		case LESS:
			return GL11C.GL_LESS;
		case LESS_EQUAL:
			return GL11C.GL_LEQUAL;
		case GREATER:
			return GL11C.GL_GREATER;
		case GREATER_EQUAL:
			return GL11C.GL_GEQUAL;
		case EQUAL:
			return GL11C.GL_EQUAL;
		case NOT_EQUAL:
			return GL11C.GL_NOTEQUAL;
		default:
			return GL11C.GL_ALWAYS;
		}
	}

	private static int operation(final StencilOperation operation) {
		switch (operation) {
		case ZERO:
			return GL11C.GL_ZERO;
		case REPLACE:
			return GL11C.GL_REPLACE;
		case INCREMENT:
			return GL11C.GL_INCR;
		case DECREMENT:
			return GL11C.GL_DECR;
		case INVERT:
			return GL11C.GL_INVERT;
		default:
			return GL11C.GL_KEEP;
		}
	}

}