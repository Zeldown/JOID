package be.zeldown.joid.lib.bridge.render;

import be.zeldown.joid.lib.bridge.IBridge;
import be.zeldown.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import be.zeldown.joid.lib.bridge.render.matrix.PixelGrid;
import be.zeldown.joid.lib.bridge.render.shader.IShader;
import be.zeldown.joid.lib.bridge.render.shader.source.ShaderSource;
import be.zeldown.joid.lib.bridge.render.state.BlendState;
import be.zeldown.joid.lib.bridge.render.state.StencilFunction;
import be.zeldown.joid.lib.bridge.render.state.StencilOperation;
import be.zeldown.joid.lib.bridge.render.texture.ITexture;
import be.zeldown.joid.lib.bridge.render.texture.TextureFilter;
import be.zeldown.joid.lib.bridge.render.texture.TextureWrap;
import be.zeldown.joid.lib.bridge.render.vertex.DrawMode;
import be.zeldown.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.NonNull;

public interface IRenderBridge extends IBridge {

	public void popMatrix();
	public void pushMatrix();
	public void loadIdentity();
	public void scale(final double x, final double y, final double z);
	public void translate(final double x, final double y, final double z);
	public void rotate(final double angle, final double x, final double y, final double z);

	public void popProjection();
	public void pushProjection();
	public void ortho(final double left, final double right, final double bottom, final double top, final double near, final double far);

	public void popState();
	public void pushState();

	public float getLineWidth();
	public boolean isLineSmooth();
	public void cull(final boolean cull);
	public void lineWidth(final float width);
	public void colorMask(final boolean write);
	public void alphaTest(final float threshold);
	public void lighting(final boolean lighting);
	public void lineSmooth(final boolean smooth);
	public void blend(final @NonNull BlendState state);
	public void depth(final boolean test, final boolean write);
	public void color(final float red, final float green, final float blue, final float alpha);

	public void stencilTest(final boolean test);
	public void stencilFunction(final @NonNull StencilFunction function, final int reference, final int mask);
	public void stencilOperation(final @NonNull StencilOperation fail, final @NonNull StencilOperation depthFail, final @NonNull StencilOperation pass);

	public void clearStencil();
	public int getViewportWidth();
	public int getViewportHeight();
	public @NonNull PixelGrid getPixelGrid();
	public void viewport(final int x, final int y, final int width, final int height);
	public void clear(final float red, final float green, final float blue, final float alpha);

	public IShader getShader();
	public void resetTexture();
	public void shader(final IShader shader);
	public void frameBuffer(final IFrameBuffer frameBuffer);
	public void texture(final @NonNull ITexture texture, final @NonNull TextureFilter filter, final @NonNull TextureWrap wrap);

	public void draw(final @NonNull DrawMode mode, final @NonNull VertexBuffer buffer);

	public @NonNull ITexture createTexture();
	public @NonNull IFrameBuffer createFrameBuffer(final int width, final int height, final @NonNull TextureFilter filter);
	public @NonNull IShader createShader(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment, final @NonNull BlendState blend);

}