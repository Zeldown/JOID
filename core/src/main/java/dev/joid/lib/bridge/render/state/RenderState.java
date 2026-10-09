package dev.joid.lib.bridge.render.state;

import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
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

	public @NonNull RenderState copy() {
		final RenderState copy = new RenderState();
		copy.red            = this.red;
		copy.green          = this.green;
		copy.blue           = this.blue;
		copy.alpha          = this.alpha;
		copy.blend          = this.blend;
		copy.depthTest      = this.depthTest;
		copy.depthWrite     = this.depthWrite;
		copy.cull           = this.cull;
		copy.lighting       = this.lighting;
		copy.colorWrite     = this.colorWrite;
		copy.alphaCutoff    = this.alphaCutoff;
		copy.lineWidth      = this.lineWidth;
		copy.lineSmooth     = this.lineSmooth;
		copy.stencil        = this.stencil;
		copy.viewportX      = this.viewportX;
		copy.viewportY      = this.viewportY;
		copy.viewportWidth  = this.viewportWidth;
		copy.viewportHeight = this.viewportHeight;
		copy.frameBuffer    = this.frameBuffer;
		copy.texture        = this.texture;
		copy.textureFilter  = this.textureFilter;
		copy.textureWrap    = this.textureWrap;
		copy.shader         = this.shader;
		return copy;
	}

}