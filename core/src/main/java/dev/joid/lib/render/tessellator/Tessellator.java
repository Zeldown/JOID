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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

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

	private DrawMode drawMode;
	private boolean  isDrawing;

	public static @NonNull Tessellator inst() {
		return Tessellator.INSTANCE;
	}

	public void start(final @NonNull DrawMode drawMode) {
		if (this.isDrawing) {
			throw new IllegalStateException("The Tessellator is already tessellating a " + this.drawMode + " shape, call draw() to end it before start(" + drawMode + ")");
		}

		this.isDrawing = true;
		this.reset();
		this.drawMode = drawMode;
		this.hasNormals = false;
		this.hasColor = false;
		this.hasTexture = false;
	}

	public void draw() {
		if (!this.isDrawing) {
			throw new IllegalStateException("The Tessellator has no shape to draw, call start(DrawMode) before draw()");
		}

		this.isDrawing = false;

		final int count = Tessellator.getOutputCount(this.drawMode, this.vertexCount);
		final IRenderBridge render = BridgeHandler.RENDER.get();
		if (count > 0 && this.isLineMode() && (render.getState().isLineSmooth() || render.getState().getLineWidth() != 1F)) {
			this.drawWideLines(render);
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

			render.draw(this.isLineMode() ? Primitive.LINES : Primitive.TRIANGLES, VertexBuffer.of(Tessellator.byteBuffer, count, this.hasTexture, this.hasColor, this.hasNormals));
		}

		if (this.rawBufferSize > 0x20000 && this.rawBufferIndex < this.rawBufferSize >> 3) {
			this.rawBufferSize = 0x10000;
			this.rawBuffer = new int[this.rawBufferSize];
		}

		this.reset();
	}

	public void abort() {
		this.isDrawing = false;
		this.reset();
	}

	public void setColor(final int r, final int g, final int b) {
		this.setColor(r, g, b, 255);
	}

	public void setColor(final int r, final int g, final int b, final int a) {
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

	public void addVertex(final double x, final double y, final double z) {
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

		this.rawBuffer[this.rawBufferIndex + 0] = Float.floatToRawIntBits((float) x);
		this.rawBuffer[this.rawBufferIndex + 1] = Float.floatToRawIntBits((float) y);
		this.rawBuffer[this.rawBufferIndex + 2] = Float.floatToRawIntBits((float) z);
		this.rawBufferIndex += 8;
		this.vertexCount++;
	}

	public void addVertexWithUV(final double x, final double y, final double z, final double u, final double v) {
		this.setTextureUV(u, v);
		this.addVertex(x, y, z);
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

	private boolean isSamePoint(final int vertex, final int other) {
		return this.rawBuffer[vertex * 8] == this.rawBuffer[other * 8] && this.rawBuffer[vertex * 8 + 1] == this.rawBuffer[other * 8 + 1];
	}

	private double getPointX(final int vertex) {
		return Float.intBitsToFloat(this.rawBuffer[vertex * 8]);
	}

	private double getPointY(final int vertex) {
		return Float.intBitsToFloat(this.rawBuffer[vertex * 8 + 1]);
	}

	private void drawWideLines(final IRenderBridge render) {
		Tessellator.ensureCapacity(this.vertexCount * 12 + 12);
		Tessellator.intBuffer.clear();
		if (this.drawMode == DrawMode.LINES) {
			for (int i = 0; i + 1 < this.vertexCount; i += 2) {
				this.putLineSegment(i, i + 1, true, true, null, null);
			}
		} else {
			this.putLineStrip();
		}

		final int count = Tessellator.intBuffer.position() / 8;
		if (count == 0) {
			return;
		}

		Tessellator.byteBuffer.position(0);
		Tessellator.byteBuffer.limit(count * VertexBuffer.STRIDE);

		final VertexBuffer buffer = VertexBuffer.of(Tessellator.byteBuffer, count, true, this.hasColor, true);
		final IShader shader = render.getState().getShader();
		if (shader == null) {
			LineShader.SHADER.bind();
			try {
				render.draw(Primitive.TRIANGLES, buffer);
			} finally {
				LineShader.SHADER.unbind();
			}
			return;
		}

		render.getState().shader(shader.getLineShader());
		try {
			render.draw(Primitive.TRIANGLES, buffer);
		} finally {
			render.getState().shader(shader);
		}
	}

	private void putLineStrip() {
		final int[] points = new int[this.vertexCount];
		int size = 0;
		for (int i = 0; i < this.vertexCount; i++) {
			if (size == 0 || !this.isSamePoint(points[size - 1], i)) {
				points[size] = i;
				size++;
			}
		}

		if (this.drawMode == DrawMode.LINE_LOOP && size > 1 && this.isSamePoint(points[0], points[size - 1])) {
			size--;
		}

		if (size < 2) {
			return;
		}

		final boolean closed = this.drawMode == DrawMode.LINE_LOOP && size > 2;
		final int segments = closed ? size : size - 1;
		final int[] miters = new int[this.vertexCount];
		final float[] tangents = new float[this.vertexCount * 2];
		for (int i = closed ? 0 : 1; i < segments; i++) {
			this.putLineJoint(points[(i + size - 1) % size], points[i], points[(i + 1) % size], miters, tangents);
		}

		for (int i = 0; i < segments; i++) {
			this.putLineSegment(points[i], points[(i + 1) % size], !closed && i == 0, !closed && i == segments - 1, miters, tangents);
		}
	}

	private void putLineJoint(final int previous, final int vertex, final int next, final int[] miters, final float[] tangents) {
		final double x = this.getPointX(vertex);
		final double y = this.getPointY(vertex);
		final double inX = x - this.getPointX(previous);
		final double inY = y - this.getPointY(previous);
		final double outX = this.getPointX(next) - x;
		final double outY = this.getPointY(next) - y;
		final double inLength = Math.hypot(inX, inY);
		final double outLength = Math.hypot(outX, outY);
		final double halfCosine = Math.sqrt(Math.max(0D, (1D + (inX * outX + inY * outY) / (inLength * outLength)) / 2D));
		if (halfCosine * 4D < 1D) {
			miters[vertex] = -1;
			for (final int side : new int[] {-127, 127}) {
				this.putLineVertex(vertex, previous, 0, 1, LineCorner.BODY);
				this.putLineVertex(vertex, previous, side, 1, LineCorner.BODY);
				this.putLineVertex(vertex, next, side, -1, LineCorner.BODY);
			}
			return;
		}

		final double scale = Math.min(inLength, outLength);
		miters[vertex] = (int) Math.round((1D / halfCosine - 1D) / 3D * 127D);
		tangents[vertex * 2] = (float) (x + (inX / inLength + outX / outLength) * scale);
		tangents[vertex * 2 + 1] = (float) (y + (inY / inLength + outY / outLength) * scale);
	}

	private void putLineSegment(final int start, final int end, final boolean startCap, final boolean endCap, final int[] miters, final float[] tangents) {
		if (startCap) {
			this.putLineVertex(start, end, -127, -1, LineCorner.CAP);
			this.putLineVertex(start, end, 127, -1, LineCorner.CAP);
			this.putLineVertex(start, end, 127, -1, LineCorner.INSET);
			this.putLineVertex(start, end, -127, -1, LineCorner.CAP);
			this.putLineVertex(start, end, 127, -1, LineCorner.INSET);
			this.putLineVertex(start, end, -127, -1, LineCorner.INSET);
		}

		this.putLineEnd(start, end, -1, -127, startCap, miters, tangents);
		this.putLineEnd(start, end, -1, 127, startCap, miters, tangents);
		this.putLineEnd(end, start, 1, 127, endCap, miters, tangents);
		this.putLineEnd(start, end, -1, -127, startCap, miters, tangents);
		this.putLineEnd(end, start, 1, 127, endCap, miters, tangents);
		this.putLineEnd(end, start, 1, -127, endCap, miters, tangents);

		if (endCap) {
			this.putLineVertex(end, start, -127, 1, LineCorner.INSET);
			this.putLineVertex(end, start, 127, 1, LineCorner.INSET);
			this.putLineVertex(end, start, 127, 1, LineCorner.CAP);
			this.putLineVertex(end, start, -127, 1, LineCorner.INSET);
			this.putLineVertex(end, start, 127, 1, LineCorner.CAP);
			this.putLineVertex(end, start, -127, 1, LineCorner.CAP);
		}
	}

	private void putLineEnd(final int vertex, final int other, final int end, final int side, final boolean cap, final int[] miters, final float[] tangents) {
		if (cap) {
			this.putLineVertex(vertex, other, side, end, LineCorner.INSET);
		} else if (miters[vertex] < 0) {
			this.putLineVertex(vertex, other, side, end, LineCorner.BODY);
		} else {
			this.putLineAttributes(vertex, Float.floatToRawIntBits(tangents[vertex * 2]), Float.floatToRawIntBits(tangents[vertex * 2 + 1]), side, -LineCorner.MITER.getCode(), miters[vertex]);
		}
	}

	private void putLineVertex(final int vertex, final int other, final int side, final int end, final LineCorner corner) {
		this.putLineAttributes(vertex, this.rawBuffer[other * 8], this.rawBuffer[other * 8 + 1], side, end * corner.getCode(), 0);
	}

	private void putLineAttributes(final int vertex, final int otherX, final int otherY, final int side, final int corner, final int miter) {
		final int offset = vertex * 8;
		Tessellator.intBuffer.put(this.rawBuffer, offset, 3);
		Tessellator.intBuffer.put(otherX);
		Tessellator.intBuffer.put(otherY);
		Tessellator.intBuffer.put(this.rawBuffer[offset + 5]);
		Tessellator.intBuffer.put(side & 255 | (corner & 255) << 8 | (miter & 255) << 16);
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

		private static final IShader SHADER = CoreShader.LINE.create(BlendState.NORMAL).getLineShader();

	}

	@Getter
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	private enum LineCorner {

		CAP(127),
		INSET(95),
		BODY(64),
		MITER(32);

		private final int code;

	}

}