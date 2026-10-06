package dev.joid.lib.bridge.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.framebuffer.IFrameBuffer;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.source.ShaderSource;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.bridge.render.texture.ITexture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.vertex.DrawMode;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class CapturingRenderBridge extends RenderBridge implements TestRule {

	private final List<Capture>      captures     = new ArrayList<>();
	private final List<IFrameBuffer> frameBuffers = new ArrayList<>();

	public CapturingRenderBridge(final int width, final int height) {
		this.resize(width, height);
	}

	@Override
	public Statement apply(final Statement base, final Description description) {
		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				BridgeHandler.RENDER.register(CapturingRenderBridge.this);
				try {
					base.evaluate();
				} finally {
					BridgeHandler.RENDER.unregister(CapturingRenderBridge.this);
				}
			}

		};
	}

	public @NonNull CapturingRenderBridge resize(final int width, final int height) {
		super.viewport(0, 0, width, height);
		super.ortho(0D, width, height, 0D, 0D, 10000D);
		return this;
	}

	@Override
	public void clearStencil() {}

	@Override
	public void clear(final float red, final float green, final float blue, final float alpha) {}

	@Override
	public void draw(final @NonNull DrawMode mode, final @NonNull VertexBuffer buffer) {
		final int[] vertices = new int[buffer.getCount() * 8];
		for (int i = 0; i < vertices.length; i++) {
			vertices[i] = buffer.getBuffer().getInt(i * 4);
		}

		final IShader shader = super.getShader();
		final Map<String, Object> uniforms = shader instanceof RecordingShader ? new HashMap<>(((RecordingShader) shader).getValues()) : Collections.emptyMap();
		this.captures.add(new Capture(mode, buffer.isTexture(), buffer.isColor(), buffer.isNormal(), vertices, super.getState().copy(), uniforms));
	}

	@Override
	public @NonNull ITexture createTexture() {
		return new RecordingTexture();
	}

	@Override
	public @NonNull IFrameBuffer createFrameBuffer(final int width, final int height, final @NonNull TextureFilter filter) {
		final IFrameBuffer frameBuffer = new RecordingFrameBuffer(width, height);
		this.frameBuffers.add(frameBuffer);
		return frameBuffer;
	}

	@Override
	public @NonNull IShader createShader(final @NonNull ShaderSource vertex, final @NonNull ShaderSource fragment, final @NonNull BlendState blend) {
		return new RecordingShader();
	}

	public @NonNull Capture getLast() {
		return this.captures.get(this.captures.size() - 1);
	}

	@Getter
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class Capture {

		private final DrawMode            mode;
		private final boolean             texture;
		private final boolean             color;
		private final boolean             normal;
		private final int[]               vertices;
		private final RenderState         state;
		private final Map<String, Object> uniforms;

		public int getCount() {
			return this.vertices.length / 8;
		}

		public float getX(final int vertex) {
			return Float.intBitsToFloat(this.vertices[vertex * 8]);
		}

		public float getY(final int vertex) {
			return Float.intBitsToFloat(this.vertices[vertex * 8 + 1]);
		}

		public float getZ(final int vertex) {
			return Float.intBitsToFloat(this.vertices[vertex * 8 + 2]);
		}

		public float getU(final int vertex) {
			return Float.intBitsToFloat(this.vertices[vertex * 8 + 3]);
		}

		public float getV(final int vertex) {
			return Float.intBitsToFloat(this.vertices[vertex * 8 + 4]);
		}

		public int getColor(final int vertex) {
			return this.vertices[vertex * 8 + 5];
		}

		public int getNormal(final int vertex) {
			return this.vertices[vertex * 8 + 6];
		}

		public double getLeft() {
			double value = Double.MAX_VALUE;
			for (int i = 0; i < this.getCount(); i++) {
				value = Math.min(value, this.getX(i));
			}
			return value;
		}

		public double getRight() {
			double value = -Double.MAX_VALUE;
			for (int i = 0; i < this.getCount(); i++) {
				value = Math.max(value, this.getX(i));
			}
			return value;
		}

		public double getTop() {
			double value = Double.MAX_VALUE;
			for (int i = 0; i < this.getCount(); i++) {
				value = Math.min(value, this.getY(i));
			}
			return value;
		}

		public double getBottom() {
			double value = -Double.MAX_VALUE;
			for (int i = 0; i < this.getCount(); i++) {
				value = Math.max(value, this.getY(i));
			}
			return value;
		}

	}

}