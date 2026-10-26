package dev.joid.lib.bridge.render;

import java.util.function.Consumer;

import dev.joid.lib.bridge.IBridge;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.matrix.MatrixStack;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.NonNull;

public interface IRenderBridge extends IBridge {

	public void screen(final int width, final int height);
	public void quantize(final double motionX, final double motionY);

	public void pushState();
	public void popState();

	public int getStateDepth();

	public void clearDepth();
	public void clearStencil();
	public void clearColor(final float red, final float green, final float blue, final float alpha);

	public void draw(final @NonNull Primitive primitive, final @NonNull VertexBuffer buffer);

	public @NonNull ITexture createTexture();
	public @NonNull IFrameBuffer createFrameBuffer(final int width, final int height);
	public @NonNull IShader createShader(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment, final @NonNull BlendState blend);

	public @NonNull RenderState getState();
	public @NonNull PixelGrid getPixelGrid();
	public @NonNull MatrixStack getModelView();
	public @NonNull MatrixStack getProjection();

	public default void beginFrame() {}
	public default void endFrame() {}

	public default boolean isFrameActive() {
		return true;
	}

	public default void suspend(final @NonNull Runnable draw) {
		draw.run();
	}

	public default void raster(final @NonNull IFrameBuffer target, final int width, final int height, final @NonNull Runnable draw) {
		this.suspend(draw);
	}

	public default void withState(final @NonNull Consumer<IRenderBridge> body) {
		this.pushState();
		try {
			body.accept(this);
		} finally {
			this.popState();
		}
	}

	public default boolean canWrap(final @NonNull TextureWrap wrap) {
		return true;
	}

}