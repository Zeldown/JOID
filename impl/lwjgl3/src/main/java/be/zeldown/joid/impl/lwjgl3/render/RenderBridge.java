package be.zeldown.joid.impl.lwjgl3.render;

import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL12C;
import org.lwjgl.opengl.GL13C;
import org.lwjgl.opengl.GL14C;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.opengl.GL33C;

import be.zeldown.joid.impl.lwjgl3.render.framebuffer.FrameBuffer;
import be.zeldown.joid.impl.lwjgl3.render.shader.Shader;
import be.zeldown.joid.impl.lwjgl3.render.shader.ShaderTranslator;
import be.zeldown.joid.impl.lwjgl3.render.texture.Texture;
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

public final class RenderBridge extends be.zeldown.joid.lib.bridge.render.RenderBridge {

	public static final int POSITION_LOCATION = 0;
	public static final int TEXTURE_LOCATION  = 1;
	public static final int COLOR_LOCATION    = 2;
	public static final int NORMAL_LOCATION   = 3;

	private final int     vertexArray;
	private final int     vertexBuffer;
	private final int[]   samplers;
	private final float[] aliasedLineWidthRange;
	private final float[] smoothLineWidthRange;
	private final Texture emptyTexture;
	private final Shader  fixedShader;

	public RenderBridge() {
		this.vertexArray           = GL30C.glGenVertexArrays();
		this.vertexBuffer          = GL15C.glGenBuffers();
		this.samplers              = RenderBridge.createSamplers();
		this.aliasedLineWidthRange = RenderBridge.getFloats(GL12C.GL_ALIASED_LINE_WIDTH_RANGE);
		this.smoothLineWidthRange  = RenderBridge.getFloats(GL12C.GL_SMOOTH_LINE_WIDTH_RANGE);
		this.emptyTexture          = Texture.create().allocate(1, 1).upload(new int[] {0xFFFFFFFF}, 1, 1);
		this.fixedShader           = (Shader) this.createShader(ShaderSource.read(ShaderStage.VERTEX, RenderBridge.class.getResourceAsStream("/assets/shaders/fixed/fixed.vsh")), ShaderSource.read(ShaderStage.FRAGMENT, RenderBridge.class.getResourceAsStream("/assets/shaders/fixed/fixed.fsh")), BlendState.DISABLED);

		GL30C.glBindVertexArray(this.vertexArray);
		GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.vertexBuffer);
		GL20C.glVertexAttribPointer(RenderBridge.POSITION_LOCATION, 3, GL11C.GL_FLOAT, false, VertexBuffer.STRIDE, VertexBuffer.POSITION_OFFSET);
		GL20C.glVertexAttribPointer(RenderBridge.TEXTURE_LOCATION, 2, GL11C.GL_FLOAT, false, VertexBuffer.STRIDE, VertexBuffer.TEXTURE_OFFSET);
		GL20C.glVertexAttribPointer(RenderBridge.COLOR_LOCATION, 4, GL11C.GL_UNSIGNED_BYTE, true, VertexBuffer.STRIDE, VertexBuffer.COLOR_OFFSET);
		GL20C.glVertexAttribPointer(RenderBridge.NORMAL_LOCATION, 3, GL11C.GL_BYTE, true, VertexBuffer.STRIDE, VertexBuffer.NORMAL_OFFSET);
		GL20C.glEnableVertexAttribArray(RenderBridge.POSITION_LOCATION);
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

		final Shader shader = state.getShader() == null ? this.fixedShader : (Shader) state.getShader();
		shader.use(state);

		final Texture texture = state.getTexture() == null ? this.emptyTexture : (Texture) state.getTexture();
		GL13C.glActiveTexture(GL13C.GL_TEXTURE0);
		GL11C.glBindTexture(GL11C.GL_TEXTURE_2D, texture.getId());
		GL33C.glBindSampler(0, this.getSampler(state.getTextureFilter(), state.getTextureWrap(), texture.isMipmapped()));

		GL30C.glBindVertexArray(this.vertexArray);
		GL15C.glBindBuffer(GL15C.GL_ARRAY_BUFFER, this.vertexBuffer);
		GL15C.glBufferData(GL15C.GL_ARRAY_BUFFER, buffer.getBuffer(), GL15C.GL_STREAM_DRAW);
		RenderBridge.toggleAttribute(RenderBridge.TEXTURE_LOCATION, buffer.isTexture());
		RenderBridge.toggleAttribute(RenderBridge.COLOR_LOCATION, buffer.isColor());
		RenderBridge.toggleAttribute(RenderBridge.NORMAL_LOCATION, buffer.isNormal());
		GL20C.glVertexAttrib2f(RenderBridge.TEXTURE_LOCATION, 0F, 0F);
		GL20C.glVertexAttrib4f(RenderBridge.COLOR_LOCATION, state.getRed(), state.getGreen(), state.getBlue(), state.getAlpha());
		GL20C.glVertexAttrib3f(RenderBridge.NORMAL_LOCATION, 0F, 0F, 1F);
		GL11C.glDrawArrays(RenderBridge.mode(mode), 0, buffer.getCount());
	}

	@Override
	public @NonNull ITexture createTexture() {
		return Texture.create();
	}

	@Override
	public @NonNull IFrameBuffer createFrameBuffer(final int width, final int height, final @NonNull TextureFilter filter) {
		return FrameBuffer.create(width, height);
	}

	@Override
	public @NonNull IShader createShader(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment, final @NonNull BlendState blend) {
		return Shader.create(this, ShaderTranslator.translate(vertex), ShaderTranslator.translate(fragment), blend);
	}

	public int getSampler(final TextureFilter filter, final TextureWrap wrap, final boolean mipmapped) {
		return this.samplers[RenderBridge.getSamplerIndex(filter, wrap, mipmapped)];
	}

	private void applyTarget(final RenderState state) {
		GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, state.getFrameBuffer() == null ? 0 : ((FrameBuffer) state.getFrameBuffer()).getId());
		GL11C.glViewport(state.getViewportX(), state.getViewportY(), state.getViewportWidth(), state.getViewportHeight());
	}

	private void applyPipeline(final RenderState state) {
		final BlendState blend = state.getBlend();
		RenderBridge.toggle(GL11C.GL_BLEND, blend.isEnabled());
		if (blend.isEnabled()) {
			GL14C.glBlendEquation(RenderBridge.equation(blend.getEquation()));
			GL14C.glBlendFuncSeparate(RenderBridge.factor(blend.getSourceColor()), RenderBridge.factor(blend.getDestinationColor()), RenderBridge.factor(blend.getSourceAlpha()), RenderBridge.factor(blend.getDestinationAlpha()));
		}

		RenderBridge.toggle(GL11C.GL_DEPTH_TEST, state.isDepthTest());
		GL11C.glDepthMask(state.isDepthWrite());
		RenderBridge.toggle(GL11C.GL_CULL_FACE, state.isCull());
		GL11C.glColorMask(state.isColorMask(), state.isColorMask(), state.isColorMask(), state.isColorMask());

		RenderBridge.toggle(GL11C.GL_STENCIL_TEST, state.isStencilTest());
		GL11C.glStencilFunc(RenderBridge.function(state.getStencilFunction()), state.getStencilReference(), state.getStencilMask());
		GL11C.glStencilOp(RenderBridge.operation(state.getStencilFail()), RenderBridge.operation(state.getStencilDepthFail()), RenderBridge.operation(state.getStencilPass()));

		final float[] lineWidthRange = state.isLineSmooth() ? this.smoothLineWidthRange : this.aliasedLineWidthRange;
		GL11C.glLineWidth(Math.max(lineWidthRange[0], Math.min(lineWidthRange[1], state.getLineWidth())));
		RenderBridge.toggle(GL11C.GL_LINE_SMOOTH, state.isLineSmooth());
	}

	private static int[] createSamplers() {
		final int[] samplers = new int[TextureFilter.values().length * TextureWrap.values().length * 2];
		for (final TextureFilter filter : TextureFilter.values()) {
			for (final TextureWrap wrap : TextureWrap.values()) {
				samplers[RenderBridge.getSamplerIndex(filter, wrap, false)] = RenderBridge.createSampler(filter, wrap, false);
				samplers[RenderBridge.getSamplerIndex(filter, wrap, true)] = RenderBridge.createSampler(filter, wrap, true);
			}
		}
		return samplers;
	}

	private static int createSampler(final TextureFilter filter, final TextureWrap wrap, final boolean mipmapped) {
		final boolean linear = filter == TextureFilter.LINEAR;
		final int sampler = GL33C.glGenSamplers();
		final int magFilter = linear ? GL11C.GL_LINEAR : GL11C.GL_NEAREST;
		final int minFilter = linear ? mipmapped ? GL11C.GL_LINEAR_MIPMAP_LINEAR : GL11C.GL_LINEAR : GL11C.GL_NEAREST;
		final int textureWrap = RenderBridge.wrap(wrap);
		GL33C.glSamplerParameteri(sampler, GL11C.GL_TEXTURE_MIN_FILTER, minFilter);
		GL33C.glSamplerParameteri(sampler, GL11C.GL_TEXTURE_MAG_FILTER, magFilter);
		GL33C.glSamplerParameteri(sampler, GL11C.GL_TEXTURE_WRAP_S, textureWrap);
		GL33C.glSamplerParameteri(sampler, GL11C.GL_TEXTURE_WRAP_T, textureWrap);
		return sampler;
	}

	private static int getSamplerIndex(final TextureFilter filter, final TextureWrap wrap, final boolean mipmapped) {
		return (mipmapped ? TextureFilter.values().length * TextureWrap.values().length : 0) + filter.ordinal() * TextureWrap.values().length + wrap.ordinal();
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