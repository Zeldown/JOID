package be.zeldown.joid.lib.render.tessellator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;

import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.render.vertex.DrawMode;
import be.zeldown.joid.lib.bridge.render.vertex.VertexBuffer;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Tessellator {

	private static final Tessellator INSTANCE = new Tessellator();

	private static ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096 * VertexBuffer.STRIDE).order(ByteOrder.nativeOrder());
	private static IntBuffer  intBuffer  = Tessellator.byteBuffer.asIntBuffer();

	private int   rawBufferSize;
	private int   rawBufferIndex;
	private int[] rawBuffer;

	private int vertexCount;
	private int normal;

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

	public void draw() {
		if (!this.isDrawing) {
			throw new IllegalStateException("Not tesselating!");
		}

		this.isDrawing = false;

		final int count = Tessellator.getOutputCount(this.drawMode, this.vertexCount);
		if (count > 0) {
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

			final DrawMode mode = this.drawMode == DrawMode.LINES || this.drawMode == DrawMode.LINE_STRIP || this.drawMode == DrawMode.LINE_LOOP ? DrawMode.LINES : DrawMode.TRIANGLES;
			BridgeHandler.RENDER.get().draw(mode, VertexBuffer.create(Tessellator.byteBuffer, count, this.hasTexture, this.hasColor, this.hasNormals));
		}

		if (this.rawBufferSize > 0x20000 && this.rawBufferIndex < this.rawBufferSize << 3) {
			this.rawBufferSize = 0x10000;
			this.rawBuffer = new int[this.rawBufferSize];
		}

		this.reset();
	}

	private void reset() {
		this.vertexCount = 0;
		this.rawBufferIndex = 0;
	}

	public void quads() {
		this.start(DrawMode.QUADS);
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
	}

	public void setTextureUV(final double u, final double v) {
		this.hasTexture = true;
		this.textureU = u;
		this.textureV = v;
	}

	public void setColor(final int r, final int g, final int b) {
		this.setColor(r, g, b, 255);
	}

	public void setColor(final float r, final float g, final float b) {
		this.setColor((int) (r * 255.0F), (int) (g * 255.0F), (int) (b * 255.0F));
	}

	public void setColor(final float r, final float g, final float b, final float a) {
		this.setColor((int) (r * 255.0F), (int) (g * 255.0F), (int) (b * 255.0F), (int) (a * 255.0F));
	}

	public void setColor(final byte r, final byte g, final byte n) {
		this.setColor(r & 255, g & 255, n & 255);
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

	public void setColor(int r, int g, int b, int a) {
		if (!this.isColorDisabled) {
			if (r > 255) {
				r = 255;
			}

			if (g > 255) {
				g = 255;
			}

			if (b > 255) {
				b = 255;
			}

			if (a > 255) {
				a = 255;
			}

			if (r < 0) {
				r = 0;
			}

			if (g < 0) {
				g = 0;
			}

			if (b < 0) {
				b = 0;
			}

			if (a < 0) {
				a = 0;
			}

			this.hasColor = true;

			if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
				this.color = a << 24 | b << 16 | g << 8 | r;
			} else {
				this.color = r << 24 | g << 16 | b << 8 | a;
			}
		}
	}

	public void addVertexWithUV(final double x, final double y, final double z, final double u, final double v) {
		this.setTextureUV(u, v);
		this.addVertex(x, y, z);
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

	public void disableColor() {
		this.isColorDisabled = true;
	}

	public void setNormal(final float x, final float y, final float z) {
		this.hasNormals = true;
		final byte normalX = (byte) (int) (x * 127.0F);
		final byte normalY = (byte) (int) (y * 127.0F);
		final byte normalZ = (byte) (int) (z * 127.0F);
		this.normal = normalX & 255 | (normalY & 255) << 8 | (normalZ & 255) << 16;
	}

	public void translate(final float x, final float y, final float z) {
		this.xOffset += x;
		this.yOffset += y;
		this.zOffset += z;
	}

	public static Tessellator inst() {
		return Tessellator.INSTANCE;
	}

	public Tessellator copy() {
		return new Tessellator();
	}

	private void putVertex(final int index) {
		Tessellator.intBuffer.put(this.rawBuffer, index * 8, 8);
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

}