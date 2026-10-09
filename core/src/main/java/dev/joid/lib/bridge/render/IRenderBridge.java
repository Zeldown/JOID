package dev.joid.lib.bridge.render;

import dev.joid.lib.bridge.IBridge;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.StencilState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.NonNull;

public interface IRenderBridge extends IBridge {

	public default void endFrame() {}
	public default void beginFrame() {}

	public default void suspend(final @NonNull Runnable draw) {
		draw.run();
	}

	public default void raster(final @NonNull IFrameBuffer target, final int width, final int height, final @NonNull Runnable draw) {
		this.suspend(draw);
	}

	public void popMatrix();
	public void pushMatrix();
	public void loadIdentity();
	public void quantize(final double motionX, final double motionY);
	public void scale(final double x, final double y, final double z);
	public void translate(final double x, final double y, final double z);
	public void rotate(final double angle, final double x, final double y, final double z);

	public void popProjection();
	public void pushProjection();
	public void screen(final int width, final int height);
	public void ortho(final double left, final double right, final double bottom, final double top, final double near, final double far);

	public void popState();
	public void pushState();

	public void cull(final boolean cull);
	public void depthTest(final boolean test);
	public void lineWidth(final float width);
	public void depthWrite(final boolean write);
	public void colorWrite(final boolean write);
	public void alphaCutoff(final float cutoff);
	public void lighting(final boolean lighting);
	public void lineSmooth(final boolean smooth);
	public void blend(final @NonNull BlendState state);
	public void stencil(final @NonNull StencilState stencil);
	public void color(final float red, final float green, final float blue, final float alpha);

	public void clearDepth();
	public void clearStencil();
	public void clearColor(final float red, final float green, final float blue, final float alpha);

	public void resetTexture();
	public void shader(final IShader shader);
	public void frameBuffer(final IFrameBuffer frameBuffer);
	public void viewport(final int x, final int y, final int width, final int height);
	public void texture(final @NonNull ITexture texture, final @NonNull TextureFilter filter, final @NonNull TextureWrap wrap);

	public void draw(final @NonNull Primitive primitive, final @NonNull VertexBuffer buffer);

	public default boolean canWrap(final @NonNull TextureWrap wrap) {
		return true;
	}

	public @NonNull ITexture createTexture();
	public @NonNull IFrameBuffer createFrameBuffer(final int width, final int height);
	public @NonNull IShader createShader(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment, final @NonNull BlendState blend);

	public IShader getShader();
	public float getLineWidth();
	public boolean isLineSmooth();
	public int getViewportWidth();
	public int getViewportHeight();
	public @NonNull PixelGrid getPixelGrid();

}