package dev.joid.lib.bridge.render.state;

import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class RenderState {

	private float red;
	private float blue;
	private float green;
	private float alpha;

	private boolean      cull;
	private float        lineWidth;
	private BlendState   blend;
	private boolean      lighting;
	private boolean      depthTest;
	private float        alphaCutoff;
	private boolean      colorWrite;
	private boolean      depthWrite;
	private boolean      lineSmooth;
	private StencilState stencil;

	private int viewportX;
	private int viewportY;
	private int viewportWidth;
	private int viewportHeight;

	private IShader       shader;
	private ITexture      texture;
	private TextureWrap   textureWrap;
	private IFrameBuffer  frameBuffer;
	private TextureFilter textureFilter;

	public RenderState() {
		this.red   = 1F;
		this.green = 1F;
		this.blue  = 1F;
		this.alpha = 1F;

		this.blend      = BlendState.DISABLED;
		this.depthWrite = true;
		this.colorWrite = true;
		this.lineWidth  = 1F;
		this.stencil    = StencilState.DISABLED;

		this.textureFilter = TextureFilter.NEAREST;
		this.textureWrap   = TextureWrap.REPEAT;
	}

	public @NonNull RenderState color(final float red, final float green, final float blue, final float alpha) {
		this.red   = red;
		this.green = green;
		this.blue  = blue;
		this.alpha = alpha;
		return this;
	}

	public @NonNull RenderState cull(final boolean cull) {
		this.cull = cull;
		return this;
	}

	public @NonNull RenderState lighting(final boolean lighting) {
		this.lighting = lighting;
		return this;
	}

	public @NonNull RenderState lineWidth(final float lineWidth) {
		this.lineWidth = lineWidth;
		return this;
	}

	public @NonNull RenderState depthTest(final boolean depthTest) {
		this.depthTest = depthTest;
		return this;
	}

	public @NonNull RenderState colorWrite(final boolean colorWrite) {
		this.colorWrite = colorWrite;
		return this;
	}

	public @NonNull RenderState depthWrite(final boolean depthWrite) {
		this.depthWrite = depthWrite;
		return this;
	}

	public @NonNull RenderState lineSmooth(final boolean lineSmooth) {
		this.lineSmooth = lineSmooth;
		return this;
	}

	public @NonNull RenderState alphaCutoff(final float alphaCutoff) {
		this.alphaCutoff = alphaCutoff;
		return this;
	}

	public @NonNull RenderState blend(final @NonNull BlendState blend) {
		this.blend = blend;
		return this;
	}

	public @NonNull RenderState stencil(final @NonNull StencilState stencil) {
		this.stencil = stencil;
		return this;
	}

	public @NonNull RenderState viewport(final int x, final int y, final int width, final int height) {
		this.viewportX      = x;
		this.viewportY      = y;
		this.viewportWidth  = width;
		this.viewportHeight = height;
		return this;
	}

	public @NonNull RenderState shader(final IShader shader) {
		this.shader = shader;
		return this;
	}

	public @NonNull RenderState texture(final ITexture texture) {
		this.texture = texture;
		return this;
	}

	public @NonNull RenderState frameBuffer(final IFrameBuffer frameBuffer) {
		this.frameBuffer = frameBuffer;
		return this;
	}

	public @NonNull RenderState textureWrap(final @NonNull TextureWrap textureWrap) {
		this.textureWrap = textureWrap;
		return this;
	}

	public @NonNull RenderState textureFilter(final @NonNull TextureFilter textureFilter) {
		this.textureFilter = textureFilter;
		return this;
	}

	public @NonNull RenderState copy() {
		return new RenderState().load(this);
	}

	public @NonNull RenderState load(final @NonNull RenderState state) {
		this.red            = state.red;
		this.green          = state.green;
		this.blue           = state.blue;
		this.alpha          = state.alpha;
		this.blend          = state.blend;
		this.depthTest      = state.depthTest;
		this.depthWrite     = state.depthWrite;
		this.cull           = state.cull;
		this.lighting       = state.lighting;
		this.colorWrite     = state.colorWrite;
		this.alphaCutoff    = state.alphaCutoff;
		this.lineWidth      = state.lineWidth;
		this.lineSmooth     = state.lineSmooth;
		this.stencil        = state.stencil;
		this.viewportX      = state.viewportX;
		this.viewportY      = state.viewportY;
		this.viewportWidth  = state.viewportWidth;
		this.viewportHeight = state.viewportHeight;
		this.frameBuffer    = state.frameBuffer;
		this.texture        = state.texture;
		this.textureFilter  = state.textureFilter;
		this.textureWrap    = state.textureWrap;
		this.shader         = state.shader;
		return this;
	}

}