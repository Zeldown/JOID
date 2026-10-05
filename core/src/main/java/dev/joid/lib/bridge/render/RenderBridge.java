package dev.joid.lib.bridge.render;

import java.util.ArrayDeque;
import java.util.Deque;

import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.matrix.MatrixStack;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.state.StencilFunction;
import dev.joid.lib.bridge.render.state.StencilOperation;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class RenderBridge implements IRenderBridge {

	private final MatrixStack        modelView;
	private final MatrixStack        projection;
	private final Deque<RenderState> stateStack;

	private RenderState state;

	protected RenderBridge() {
		this.modelView  = new MatrixStack();
		this.projection = new MatrixStack();
		this.stateStack = new ArrayDeque<>();
		this.state      = new RenderState();
	}

	@Override
	public final void popMatrix() {
		this.modelView.pop();
	}

	@Override
	public final void pushMatrix() {
		this.modelView.push();
	}

	@Override
	public final void loadIdentity() {
		this.modelView.identity();
	}

	@Override
	public final void translate(final double x, final double y, final double z) {
		this.modelView.translate(x, y, z);
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
	public final void scale(final double x, final double y, final double z) {
		this.modelView.scale(x, y, z);
	}

	@Override
	public final void rotate(final double angle, final double x, final double y, final double z) {
		this.modelView.rotate(angle, x, y, z);
	}

	@Override
	public final void popProjection() {
		this.projection.pop();
	}

	@Override
	public final void pushProjection() {
		this.projection.push();
	}

	@Override
	public final void ortho(final double left, final double right, final double bottom, final double top, final double near, final double far) {
		this.projection.ortho(left, right, bottom, top, near, far);
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
	public final void depth(final boolean test, final boolean write) {
		this.state.setDepthTest(test);
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
	public final void colorMask(final boolean write) {
		this.state.setColorMask(write);
	}

	@Override
	public final void alphaTest(final float threshold) {
		this.state.setAlphaTest(true);
		this.state.setAlphaThreshold(threshold);
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
	public final void stencilTest(final boolean test) {
		this.state.setStencilTest(test);
	}

	@Override
	public final void stencilFunction(final @NonNull StencilFunction function, final int reference, final int mask) {
		this.state.setStencilFunction(function);
		this.state.setStencilReference(reference);
		this.state.setStencilMask(mask);
	}

	@Override
	public final void stencilOperation(final @NonNull StencilOperation fail, final @NonNull StencilOperation depthFail, final @NonNull StencilOperation pass) {
		this.state.setStencilFail(fail);
		this.state.setStencilDepthFail(depthFail);
		this.state.setStencilPass(pass);
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

}