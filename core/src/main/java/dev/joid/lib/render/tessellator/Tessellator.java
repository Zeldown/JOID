package dev.joid.lib.render.tessellator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.shader.source.CoreShader;
import dev.joid.lib.bridge.render.state.BlendState;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Tessellator {

	private static final Tessellator INSTANCE = new Tessellator();

	private static ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096 * VertexBuffer.STRIDE).order(ByteOrder.nativeOrder());
	private static IntBuffer  intBuffer  = Tessellator.byteBuffer.asIntBuffer();

	private int[] rawBuffer;
	private int   rawBufferSize;
	private int   rawBufferIndex;

	private int normal;
	private int vertexCount;

	private double textureU;
	private double textureV;

	private int color;

	private boolean hasColor;
	private boolean hasTexture;
	private boolean hasNormals;
	private boolean isColorDisabled;

	private double xOffset;
	private double yOffset;
	private double zOffset;

	private DrawMode drawMode;
	private boolean  isDrawing;

	public static Tessellator inst() {
		return Tessellator.INSTANCE;
	}

	public void start(final @NonNull DrawMode drawMode) {
		if (this.isDrawing) {
			throw new IllegalStateException("Already tesselating!");
		}

		this.isDrawing = true;
		this.reset();
		this.drawMode = drawMode;
		this.hasNormals = false;
		this.hasColor = false;
		this.hasTexture = false;
		this.isColorDisabled = false;
		this.xOffset = 0D;
		this.yOffset = 0D;
		this.zOffset = 0D;
	}

	public void draw() {
		if (!this.isDrawing) {
			throw new IllegalStateException("Not tesselating!");
		}

		this.isDrawing = false;

		final int count = Tessellator.getOutputCount(this.drawMode, this.vertexCount);
		final IRenderBridge render = BridgeHandler.RENDER.get();
		if (count > 0 && this.isLineMode() && render.isLineSmooth() && render.getShader() == null) {
			this.drawSmoothLines(render, count / 2);
		} else if (count > 0) {
			Tessellator.ensureCapacity(count);
			Tessellator.intBuffer.clear();

			switch (this.drawMode) {
			case QUADS:
				for (int quad = 0; quad + 3 < this.vertexCount; quad += 4) {
					this.putVertex(quad);
					this.putVertex(quad + 1);
					this.putVertex(quad + 2);
					this.putVertex(quad);
					this.putVertex(quad + 2);
					this.putVertex(quad + 3);
				}
				break;
			case POLYGON:
				for (int i = 1; i + 1 < this.vertexCount; i++) {
					this.putVertex(0);
					this.putVertex(i);
					this.putVertex(i + 1);
				}
				break;
			case LINE_STRIP:
				for (int i = 0; i + 1 < this.vertexCount; i++) {
					this.putVertex(i);
					this.putVertex(i + 1);
				}
				break;
			case LINE_LOOP:
				for (int i = 0; i < this.vertexCount; i++) {
					this.putVertex(i);
					this.putVertex((i + 1) % this.vertexCount);
				}
				break;
			default:
				Tessellator.intBuffer.put(this.rawBuffer, 0, this.vertexCount * 8);
				break;
			}

			Tessellator.byteBuffer.position(0);
			Tessellator.byteBuffer.limit(count * VertexBuffer.STRIDE);

			render.draw(this.isLineMode() ? Primitive.LINES : Primitive.TRIANGLES, VertexBuffer.create(Tessellator.byteBuffer, count, this.hasTexture, this.hasColor, this.hasNormals));
		}

		if (this.rawBufferSize > 0x20000 && this.rawBufferIndex < this.rawBufferSize << 3) {
			this.rawBufferSize = 0x10000;
			this.rawBuffer = new int[this.rawBufferSize];
		}

		this.reset();
	}

	public void quads() {
		this.start(DrawMode.QUADS);
	}

	public void setColor(final int rgb) {
		final int j = rgb >> 16 & 255;
		final int k = rgb >> 8 & 255;
		final int l = rgb & 255;
		this.setColor(j, k, l);
	}

	public void setColor(final int rgb, final int a) {
		final int k = rgb >> 16 & 255;
		final int l = rgb >> 8 & 255;
		final int i1 = rgb & 255;
		this.setColor(k, l, i1, a);
	}

	public void setColor(final int r, final int g, final int b) {
		this.setColor(r, g, b, 255);
	}

	public void setColor(final byte r, final byte g, final byte n) {
		this.setColor(r & 255, g & 255, n & 255);
	}

	public void setColor(final float r, final float g, final float b) {
		this.setColor((int) (r * 255F), (int) (g * 255F), (int) (b * 255F));
	}

	public void setColor(final int r, final int g, final int b, final int a) {
		if (!this.isColorDisabled) {
			final int red = Math.max(0, Math.min(255, r));
			final int green = Math.max(0, Math.min(255, g));
			final int blue = Math.max(0, Math.min(255, b));
			final int alpha = Math.max(0, Math.min(255, a));

			this.hasColor = true;

			if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
				this.color = alpha << 24 | blue << 16 | green << 8 | red;
			} else {
				this.color = red << 24 | green << 16 | blue << 8 | alpha;
			}
		}
	}

	public void setColor(final float r, final float g, final float b, final float a) {
		this.setColor((int) (r * 255F), (int) (g * 255F), (int) (b * 255F), (int) (a * 255F));
	}

	public void setTextureUV(final double u, final double v) {
		this.hasTexture = true;
		this.textureU = u;
		this.textureV = v;
	}

	public void setNormal(final float x, final float y, final float z) {
		this.hasNormals = true;
		final byte normalX = (byte) (int) (x * 127F);
		final byte normalY = (byte) (int) (y * 127F);
		final byte normalZ = (byte) (int) (z * 127F);
		this.normal = normalX & 255 | (normalY & 255) << 8 | (normalZ & 255) << 16;
	}

	public void addVertex(final double x, final double y, final double v) {
		if (this.rawBufferIndex >= this.rawBufferSize - 32) {
			if (this.rawBufferSize == 0) {
				this.rawBufferSize = 0x10000;
				this.rawBuffer = new int[this.rawBufferSize];
			} else {
				this.rawBufferSize *= 2;
				this.rawBuffer = Arrays.copyOf(this.rawBuffer, this.rawBufferSize);
			}
		}

		if (this.hasTexture) {
			this.rawBuffer[this.rawBufferIndex + 3] = Float.floatToRawIntBits((float) this.textureU);
			this.rawBuffer[this.rawBufferIndex + 4] = Float.floatToRawIntBits((float) this.textureV);
		}

		if (this.hasColor) {
			this.rawBuffer[this.rawBufferIndex + 5] = this.color;
		}

		if (this.hasNormals) {
			this.rawBuffer[this.rawBufferIndex + 6] = this.normal;
		}

		this.rawBuffer[this.rawBufferIndex + 0] = Float.floatToRawIntBits((float) (x + this.xOffset));
		this.rawBuffer[this.rawBufferIndex + 1] = Float.floatToRawIntBits((float) (y + this.yOffset));
		this.rawBuffer[this.rawBufferIndex + 2] = Float.floatToRawIntBits((float) (v + this.zOffset));
		this.rawBufferIndex += 8;
		this.vertexCount++;
	}

	public void addVertexWithUV(final double x, final double y, final double z, final double u, final double v) {
		this.setTextureUV(u, v);
		this.addVertex(x, y, z);
	}

	public void disableColor() {
		this.isColorDisabled = true;
	}

	public void translate(final float x, final float y, final float z) {
		this.xOffset += x;
		this.yOffset += y;
		this.zOffset += z;
	}

	public Tessellator copy() {
		return new Tessellator();
	}

	private void reset() {
		this.vertexCount = 0;
		this.rawBufferIndex = 0;
	}

	private boolean isLineMode() {
		return this.drawMode == DrawMode.LINES || this.drawMode == DrawMode.LINE_STRIP || this.drawMode == DrawMode.LINE_LOOP;
	}

	private void putVertex(final int index) {
		Tessellator.intBuffer.put(this.rawBuffer, index * 8, 8);
	}

	private int getSegmentVertex(final int segment, final int point) {
		switch (this.drawMode) {
		case LINE_STRIP:
			return segment + point;
		case LINE_LOOP:
			return (segment + point) % this.vertexCount;
		default:
			return segment * 2 + point;
		}
	}

	private void drawSmoothLines(final IRenderBridge render, final int segments) {
		Tessellator.ensureCapacity(segments * 6);
		Tessellator.intBuffer.clear();
		for (int segment = 0; segment < segments; segment++) {
			final int start = this.getSegmentVertex(segment, 0);
			final int end = this.getSegmentVertex(segment, 1);
			this.putLineVertex(start, end, -1, -1);
			this.putLineVertex(start, end, 1, -1);
			this.putLineVertex(end, start, 1, 1);
			this.putLineVertex(start, end, -1, -1);
			this.putLineVertex(end, start, 1, 1);
			this.putLineVertex(end, start, -1, 1);
		}

		Tessellator.byteBuffer.position(0);
		Tessellator.byteBuffer.limit(segments * 6 * VertexBuffer.STRIDE);

		LineShader.SHADER.bind();
		LineShader.SHADER
		.uniform("u_Width", render.getLineWidth())
		.uniform("u_Viewport", render.getViewportWidth(), render.getViewportHeight());
		render.draw(Primitive.TRIANGLES, VertexBuffer.create(Tessellator.byteBuffer, segments * 6, true, this.hasColor, true));
		LineShader.SHADER.unbind();
	}

	private void putLineVertex(final int vertex, final int other, final int side, final int end) {
		final int offset = vertex * 8;
		Tessellator.intBuffer.put(this.rawBuffer, offset, 3);
		Tessellator.intBuffer.put(this.rawBuffer[other * 8]);
		Tessellator.intBuffer.put(this.rawBuffer[other * 8 + 1]);
		Tessellator.intBuffer.put(this.rawBuffer[offset + 5]);
		Tessellator.intBuffer.put(side * 127 & 255 | (end * 127 & 255) << 8);
		Tessellator.intBuffer.put(this.rawBuffer[offset + 7]);
	}

	private static int getOutputCount(final @NonNull DrawMode drawMode, final int vertexCount) {
		switch (drawMode) {
		case QUADS:
			return vertexCount / 4 * 6;
		case POLYGON:
			return Math.max(0, vertexCount - 2) * 3;
		case LINE_STRIP:
			return Math.max(0, vertexCount - 1) * 2;
		case LINE_LOOP:
			return vertexCount < 2 ? 0 : vertexCount * 2;
		default:
			return vertexCount;
		}
	}

	private static void ensureCapacity(final int count) {
		final int bytes = count * VertexBuffer.STRIDE;
		if (Tessellator.byteBuffer.capacity() >= bytes) {
			return;
		}

		int capacity = Tessellator.byteBuffer.capacity();
		while (capacity < bytes) {
			capacity *= 2;
		}

		Tessellator.byteBuffer = ByteBuffer.allocateDirect(capacity).order(ByteOrder.nativeOrder());
		Tessellator.intBuffer = Tessellator.byteBuffer.asIntBuffer();
	}

	private static final class LineShader {

		private static final IShader SHADER = CoreShader.LINE.create(BlendState.NORMAL);

	}

}