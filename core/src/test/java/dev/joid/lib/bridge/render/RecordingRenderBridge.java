package dev.joid.lib.bridge.render;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.bridge.render.vertex.VertexAttribute;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class RecordingRenderBridge extends RenderBridge {

	private final List<Draw> draws = new ArrayList<>();

	private int depthClears;

	@Override
	public void clearDepth() {
		this.depthClears++;
	}

	@Override
	public void clearStencil() {}

	@Override
	public void clear(final float red, final float green, final float blue, final float alpha) {}

	@Override
	protected void drawPrimitive(final @NonNull Primitive primitive, final @NonNull VertexBuffer buffer, final @NonNull IShader shader) {
		final PixelGrid grid = super.getPixelGrid();
		final double[] xs = new double[buffer.getCount()];
		final double[] ys = new double[buffer.getCount()];
		for (int i = 0; i < buffer.getCount(); i++) {
			xs[i] = grid.toScreenX(buffer.getBuffer().getFloat(i * VertexBuffer.STRIDE + VertexAttribute.POSITION.getOffset()));
			ys[i] = super.getViewportHeight() - grid.toScreenY(buffer.getBuffer().getFloat(i * VertexBuffer.STRIDE + VertexAttribute.POSITION.getOffset() + 4));
		}

		final RenderState state = super.getState();
		this.draws.add(new Draw(primitive, xs, ys, state.getRed(), state.getGreen(), state.getBlue(), state.getAlpha(), state.getLineWidth(), state.getShader()));
	}

	@Override
	public @NonNull ITexture createTexture() {
		return new RecordingTexture();
	}

	@Override
	public @NonNull IFrameBuffer createFrameBuffer(final int width, final int height) {
		return new RecordingFrameBuffer(width, height);
	}

	@Override
	public @NonNull IShader createShader(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment, final @NonNull BlendState blend) {
		return new RecordingShader();
	}

	public @NonNull List<@NonNull Draw> getDraws(final float red, final float green, final float blue) {
		return this.draws.stream().filter(draw -> draw.red == red && draw.green == green && draw.blue == blue).collect(Collectors.toList());
	}

	@Getter
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class Draw {

		private final Primitive primitive;
		private final double[]  xs;
		private final double[]  ys;
		private final float     red;
		private final float     green;
		private final float     blue;
		private final float     alpha;
		private final float     lineWidth;
		private final IShader   shader;

		public double getLeft() {
			double value = Double.MAX_VALUE;
			for (final double x : this.xs) {
				value = Math.min(value, x);
			}
			return value;
		}

		public double getRight() {
			double value = -Double.MAX_VALUE;
			for (final double x : this.xs) {
				value = Math.max(value, x);
			}
			return value;
		}

		public double getTop() {
			double value = Double.MAX_VALUE;
			for (final double y : this.ys) {
				value = Math.min(value, y);
			}
			return value;
		}

		public double getBottom() {
			double value = -Double.MAX_VALUE;
			for (final double y : this.ys) {
				value = Math.max(value, y);
			}
			return value;
		}

	}

}