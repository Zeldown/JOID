package be.zeldown.joid.lib.bridge.render.state;

import be.zeldown.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import be.zeldown.joid.lib.bridge.render.shader.IShader;
import be.zeldown.joid.lib.bridge.render.texture.ITexture;
import be.zeldown.joid.lib.bridge.render.texture.TextureFilter;
import be.zeldown.joid.lib.bridge.render.texture.TextureWrap;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
public final class RenderState {

	private float red;
	private float green;
	private float blue;
	private float alpha;

	private BlendState blend;
	private boolean    depthTest;
	private boolean    depthWrite;
	private boolean    cull;
	private boolean    lighting;
	private boolean    colorMask;
	private boolean    alphaTest;
	private float      alphaThreshold;
	private float      lineWidth;
	private boolean    lineSmooth;

	private boolean          stencilTest;
	private StencilFunction  stencilFunction;
	private int              stencilReference;
	private int              stencilMask;
	private StencilOperation stencilFail;
	private StencilOperation stencilDepthFail;
	private StencilOperation stencilPass;

	private int viewportX;
	private int viewportY;
	private int viewportWidth;
	private int viewportHeight;

	private IFrameBuffer  frameBuffer;
	private ITexture      texture;
	private TextureFilter textureFilter;
	private TextureWrap   textureWrap;
	private IShader       shader;

	public RenderState() {
		this.red   = 1F;
		this.green = 1F;
		this.blue  = 1F;
		this.alpha = 1F;

		this.blend      = BlendState.DISABLED;
		this.depthWrite = true;
		this.colorMask  = true;
		this.lineWidth  = 1F;

		this.stencilFunction  = StencilFunction.ALWAYS;
		this.stencilMask      = 0xFF;
		this.stencilFail      = StencilOperation.KEEP;
		this.stencilDepthFail = StencilOperation.KEEP;
		this.stencilPass      = StencilOperation.KEEP;

		this.textureFilter = TextureFilter.NEAREST;
		this.textureWrap   = TextureWrap.REPEAT;
	}

	public @NonNull RenderState copy() {
		final RenderState copy = new RenderState();
		copy.red              = this.red;
		copy.green            = this.green;
		copy.blue             = this.blue;
		copy.alpha            = this.alpha;
		copy.blend            = this.blend;
		copy.depthTest        = this.depthTest;
		copy.depthWrite       = this.depthWrite;
		copy.cull             = this.cull;
		copy.lighting         = this.lighting;
		copy.colorMask        = this.colorMask;
		copy.alphaTest        = this.alphaTest;
		copy.alphaThreshold   = this.alphaThreshold;
		copy.lineWidth        = this.lineWidth;
		copy.lineSmooth       = this.lineSmooth;
		copy.stencilTest      = this.stencilTest;
		copy.stencilFunction  = this.stencilFunction;
		copy.stencilReference = this.stencilReference;
		copy.stencilMask      = this.stencilMask;
		copy.stencilFail      = this.stencilFail;
		copy.stencilDepthFail = this.stencilDepthFail;
		copy.stencilPass      = this.stencilPass;
		copy.viewportX        = this.viewportX;
		copy.viewportY        = this.viewportY;
		copy.viewportWidth    = this.viewportWidth;
		copy.viewportHeight   = this.viewportHeight;
		copy.frameBuffer      = this.frameBuffer;
		copy.texture          = this.texture;
		copy.textureFilter    = this.textureFilter;
		copy.textureWrap      = this.textureWrap;
		copy.shader           = this.shader;
		return copy;
	}

}