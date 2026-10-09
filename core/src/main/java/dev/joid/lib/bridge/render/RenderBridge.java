package dev.joid.lib.bridge.render;

import java.util.ArrayDeque;
import java.util.Deque;

import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.matrix.MatrixStack;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.SamplerBinding;
import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.shader.source.ShaderStage;
import dev.joid.lib.bridge.render.shader.uniform.UniformSampler;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.state.StencilState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureSampling;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.Getter;
import lombok.NonNull;

public abstract class RenderBridge implements IRenderBridge {

	@Getter private final MatrixStack modelView;
	@Getter private final MatrixStack projection;

	private final Deque<RenderState> stateStack;

	@Getter private RenderState state;
	@Getter private boolean     frameActive;

	private IShader  defaultShader;
	private ITexture emptyTexture;

	protected RenderBridge() {
		this.modelView  = new MatrixStack();
		this.projection = new MatrixStack();
		this.stateStack = new ArrayDeque<>();
		this.state      = new RenderState();
	}

	@Override
	public final void beginFrame() {
		if (this.frameActive) {
			throw new IllegalStateException("The JOID frame has already begun, call endFrame() first");
		}

		this.frameActive = true;
		this.beginFrameCommands();
	}

	@Override
	public final void endFrame() {
		this.requireFrame();
		try {
			this.submitFrameCommands();
		} finally {
			this.frameActive = false;
		}
	}

	@Override
	public final void quantize(final double motionX, final double motionY) {
		if (motionX == 0D && motionY == 0D) {
			return;
		}

		final PixelGrid grid = this.getPixelGrid();
		this.modelView.translate(grid.quantizeX(motionX) - motionX, grid.quantizeY(motionY) - motionY, 0D);
	}

	@Override
	public final void screen(final int width, final int height) {
		this.frameBuffer(null);
		this.viewport(0, 0, width, height);
		this.projection.ortho(0D, width, height, 0D, 0D, 10000D);
	}

	@Override
	public final void popState() {
		this.state = this.stateStack.pop();
	}

	@Override
	public final void pushState() {
		this.stateStack.push(this.state.copy());
	}

	@Override
	public final void color(final float red, final float green, final float blue, final float alpha) {
		this.state.setRed(red);
		this.state.setGreen(green);
		this.state.setBlue(blue);
		this.state.setAlpha(alpha);
	}

	@Override
	public final void blend(final @NonNull BlendState state) {
		this.state.setBlend(state);
	}

	@Override
	public final void depthTest(final boolean test) {
		this.state.setDepthTest(test);
	}

	@Override
	public final void depthWrite(final boolean write) {
		this.state.setDepthWrite(write);
	}

	@Override
	public final void cull(final boolean cull) {
		this.state.setCull(cull);
	}

	@Override
	public final void lighting(final boolean lighting) {
		this.state.setLighting(lighting);
	}

	@Override
	public final void colorWrite(final boolean write) {
		this.state.setColorWrite(write);
	}

	@Override
	public final void alphaCutoff(final float cutoff) {
		this.state.setAlphaCutoff(cutoff);
	}

	@Override
	public final void lineWidth(final float width) {
		this.state.setLineWidth(width);
	}

	@Override
	public final void lineSmooth(final boolean smooth) {
		this.state.setLineSmooth(smooth);
	}

	@Override
	public final IShader getShader() {
		return this.state.getShader();
	}

	@Override
	public final float getLineWidth() {
		return this.state.getLineWidth();
	}

	@Override
	public final int getViewportWidth() {
		return this.state.getViewportWidth();
	}

	@Override
	public final int getViewportHeight() {
		return this.state.getViewportHeight();
	}

	@Override
	public final @NonNull PixelGrid getPixelGrid() {
		return PixelGrid.of(this.projection.getMatrix(), this.modelView.getMatrix(), this.getViewportWidth(), this.getViewportHeight());
	}

	@Override
	public final boolean isLineSmooth() {
		return this.state.isLineSmooth();
	}

	@Override
	public final void stencil(final @NonNull StencilState stencil) {
		this.state.setStencil(stencil);
	}

	@Override
	public final void viewport(final int x, final int y, final int width, final int height) {
		this.state.setViewportX(x);
		this.state.setViewportY(y);
		this.state.setViewportWidth(width);
		this.state.setViewportHeight(height);
	}

	@Override
	public final void frameBuffer(final IFrameBuffer frameBuffer) {
		this.state.setFrameBuffer(frameBuffer);
	}

	@Override
	public final void texture(final @NonNull ITexture texture, final @NonNull TextureFilter filter, final @NonNull TextureWrap wrap) {
		this.state.setTexture(texture);
		this.state.setTextureFilter(filter);
		this.state.setTextureWrap(wrap);
	}

	@Override
	public final void resetTexture() {
		this.state.setTexture(null);
		this.state.setTextureFilter(TextureFilter.NEAREST);
		this.state.setTextureWrap(TextureWrap.REPEAT);
	}

	@Override
	public final void shader(final IShader shader) {
		this.state.setShader(shader);
	}

	@Override
	public final void clearDepth() {
		this.clearDepthBuffer();
	}

	@Override
	public final void clearStencil() {
		if (this.state.getFrameBuffer() == null) {
			this.clearStencilBuffer();
		}
	}

	@Override
	public final void clearColor(final float red, final float green, final float blue, final float alpha) {
		if (this.state.isColorWrite()) {
			this.clearColorBuffer(red, green, blue, alpha);
		}
	}

	@Override
	public final void draw(final @NonNull Primitive primitive, final @NonNull VertexBuffer buffer) {
		final IShader shader = this.state.getShader() == null ? this.getDefaultShader() : this.state.getShader();
		if (!shader.isActive() || buffer.getCount() == 0 || this.state.getViewportWidth() <= 0 || this.state.getViewportHeight() <= 0) {
			return;
		}

		this.drawPrimitive(primitive, buffer, shader);
	}

	protected void beginFrameCommands() {}

	protected void submitFrameCommands() {}

	protected abstract void clearDepthBuffer();
	protected abstract void clearStencilBuffer();
	protected abstract void clearColorBuffer(final float red, final float green, final float blue, final float alpha);

	protected abstract void drawPrimitive(final @NonNull Primitive primitive, final @NonNull VertexBuffer buffer, final @NonNull IShader shader);

	protected final void requireFrame() {
		if (!this.frameActive) {
			throw new IllegalStateException("The render bridge draws between beginFrame() and endFrame()");
		}
	}

	protected final @NonNull SamplerBinding resolveTexture() {
		final ITexture texture = this.state.getTexture();
		if (texture != null && texture.isAllocated()) {
			return SamplerBinding.of(texture, TextureSampling.of(this.state.getTextureFilter(), this.toSupportedWrap(this.state.getTextureWrap()), texture.isMipmapped()));
		}
		return SamplerBinding.of(this.getEmptyTexture(), TextureSampling.of(TextureFilter.NEAREST, TextureWrap.REPEAT, false));
	}

	protected final @NonNull SamplerBinding resolveSampler(final @NonNull UniformSampler sampler) {
		final ITexture texture = sampler.getTexture();
		if (texture != null && texture.isAllocated()) {
			return SamplerBinding.of(texture, TextureSampling.of(sampler.getFilter(), this.toSupportedWrap(sampler.getWrap()), texture.isMipmapped()));
		}
		return this.resolveTexture();
	}

	protected final @NonNull IShader getDefaultShader() {
		if (this.defaultShader == null) {
			this.defaultShader = this.createShader(CoreShader.DEFAULT.read(ShaderStage.VERTEX), CoreShader.DEFAULT.read(ShaderStage.FRAGMENT), BlendState.DISABLED);
		}
		return this.defaultShader;
	}

	protected final @NonNull ITexture getEmptyTexture() {
		if (this.emptyTexture == null) {
			this.emptyTexture = this.createTexture().allocate(1, 1).upload(new int[] {0xFFFFFFFF}, 1, 1);
		}
		return this.emptyTexture;
	}

	private TextureWrap toSupportedWrap(final TextureWrap wrap) {
		return this.canWrap(wrap) ? wrap : TextureWrap.CLAMP_TO_EDGE;
	}

}