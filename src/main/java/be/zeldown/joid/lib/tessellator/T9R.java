package be.zeldown.joid.lib.tessellator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import lombok.Getter;

@Getter
public final class T9R {

	/* Static */
	private static int nativeBufferSize = 0x200000;

	private static ByteBuffer  byteBuffer  = ByteBuffer.allocateDirect(T9R.nativeBufferSize * 4).order(ByteOrder.nativeOrder());
	private static IntBuffer   intBuffer   = T9R.byteBuffer.asIntBuffer();
	private static FloatBuffer floatBuffer = T9R.byteBuffer.asFloatBuffer();
	private static ShortBuffer shortBuffer = T9R.byteBuffer.asShortBuffer();

	private static final T9R INSTANCE = new T9R();

	/* Local */
	private int rawBufferSize;
	private int rawBufferIndex;
	private int bufferSize;
	private int[] rawBuffer;

	private int vertexCount;
	private int addedVertices;
	private int normal;

	private double textureU;
	private double textureV;

	private int color;
	private int brightness;

	private boolean hasColor;
	private boolean hasTexture;
	private boolean hasBrightness;
	private boolean hasNormals;
	private boolean isColorDisabled;

	private double xOffset;
	private double yOffset;
	private double zOffset;

	private int drawMode;
	private boolean isDrawing;

	public int draw() {
		if (!this.isDrawing) {
			throw new IllegalStateException("Not tesselating!");
		} else {
			this.isDrawing = false;

			int offs = 0;
			while (offs < this.vertexCount) {
				final int vtc = Math.min(this.vertexCount - offs, T9R.nativeBufferSize >> 5);
				T9R.intBuffer.clear();
				T9R.intBuffer.put(this.rawBuffer, offs * 8, vtc * 8);
				T9R.byteBuffer.position(0);
				T9R.byteBuffer.limit(vtc * 32);
				offs += vtc;

				if (this.hasTexture) {
					T9R.floatBuffer.position(3);
					GL11.glTexCoordPointer(2, 32, T9R.floatBuffer);
					GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				}

				if (this.hasBrightness) {
					T9R.shortBuffer.position(14);
					GL11.glTexCoordPointer(2, 32, T9R.shortBuffer);
					GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				}

				if (this.hasColor) {
					T9R.byteBuffer.position(20);
					GL11.glColorPointer(4, true, 32, T9R.byteBuffer);
					GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
				}

				if (this.hasNormals) {
					T9R.byteBuffer.position(24);
					GL11.glNormalPointer(32, T9R.byteBuffer);
					GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
				}

				T9R.floatBuffer.position(0);
				GL11.glVertexPointer(3, 32, T9R.floatBuffer);
				GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
				GL11.glDrawArrays(this.drawMode, 0, vtc);
				GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

				if (this.hasTexture || this.hasBrightness) {
					GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				}

				if (this.hasColor || this.hasNormals) {
					GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
				}
			}

			if (this.rawBufferSize > 0x20000 && this.rawBufferIndex < (this.rawBufferSize << 3)) {
				this.rawBufferSize = 0x10000;
				this.rawBuffer = new int[this.rawBufferSize];
			}

			final int i = this.rawBufferIndex * 4;
			this.reset();
			return i;
		}
	}

	private void reset() {
		this.vertexCount = 0;
		T9R.byteBuffer.clear();
		this.rawBufferIndex = 0;
		this.addedVertices = 0;
	}

	public void quads() {
		this.start(7);
	}

	public void start(final int drawMode) {
		if (this.isDrawing) {
			throw new IllegalStateException("Already tesselating!");
		}

		this.isDrawing = true;
		this.reset();
		this.drawMode = drawMode;
		this.hasNormals = false;
		this.hasColor = false;
		this.hasTexture = false;
		this.hasBrightness = false;
		this.isColorDisabled = false;
	}

	public void setTextureUV(final double u, final double v) {
		this.hasTexture = true;
		this.textureU = u;
		this.textureV = v;
	}

	public void setBrightness(final int brightness) {
		this.hasBrightness = true;
		this.brightness = brightness;
	}

	public void setColor(final int r, final int g, final int b) {
		this.setColor(r, g, b, 255);
	}

	public void setColor(final float r, final float g, final float b) {
		this.setColor((int)(r * 255.0F), (int)(g * 255.0F), (int)(b * 255.0F));
	}

	public void setColor(final float r, final float g, final float b, final float a) {
		this.setColor((int)(r * 255.0F), (int)(g * 255.0F), (int)(b * 255.0F), (int)(a * 255.0F));
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

		this.addedVertices++;

		if (this.hasTexture) {
			this.rawBuffer[this.rawBufferIndex + 3] = Float.floatToRawIntBits((float)this.textureU);
			this.rawBuffer[this.rawBufferIndex + 4] = Float.floatToRawIntBits((float)this.textureV);
		}

		if (this.hasBrightness) {
			this.rawBuffer[this.rawBufferIndex + 7] = this.brightness;
		}

		if (this.hasColor) {
			this.rawBuffer[this.rawBufferIndex + 5] = this.color;
		}

		if (this.hasNormals) {
			this.rawBuffer[this.rawBufferIndex + 6] = this.normal;
		}

		this.rawBuffer[this.rawBufferIndex + 0] = Float.floatToRawIntBits((float)(x + this.xOffset));
		this.rawBuffer[this.rawBufferIndex + 1] = Float.floatToRawIntBits((float)(y + this.yOffset));
		this.rawBuffer[this.rawBufferIndex + 2] = Float.floatToRawIntBits((float)(v + this.zOffset));
		this.rawBufferIndex += 8;
		this.vertexCount++;
	}

	public void disableColor() {
		this.isColorDisabled = true;
	}

	public void setNormal(final float x, final float y, final float z) {
		this.hasNormals = true;
		final byte normalX = (byte)((int)(x * 127.0F));
		final byte normalY = (byte)((int)(y * 127.0F));
		final byte normalZ = (byte)((int)(z * 127.0F));
		this.normal = normalX & 255 | (normalY & 255) << 8 | (normalZ & 255) << 16;
	}

	public void translate(final float x, final float y, final float z) {
		this.xOffset += x;
		this.yOffset += y;
		this.zOffset += z;
	}

	public static T9R inst() {
		return T9R.INSTANCE;
	}

	public T9R copy() {
		return new T9R();
	}

}