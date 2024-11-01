package be.zeldown.joid.lib.tessellator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import be.zeldown.joid.lib.color.Color;
import lombok.Getter;
import lombok.NonNull;

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

	/**
	 * The `draw` method in Java handles drawing operations by setting up client states and drawing arrays
	 * based on various attributes.
	 * 
	 * @return The method `draw()` returns the size of the raw buffer index multiplied by 4.
	 */
	public int draw() {
		if (!this.isDrawing) {
			throw new IllegalStateException("You must start drawing before you can finish!");
		}

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

		if (this.rawBufferSize > 0x20000 && this.rawBufferIndex < this.rawBufferSize << 3) {
			this.rawBufferSize = 0x10000;
			this.rawBuffer = new int[this.rawBufferSize];
		}

		this.reset();
		return this.rawBufferIndex * 4;
	}

	/**
	 * The `reset` function resets various variables and clears a byte buffer in a Java class.
	 */
	private void reset() {
		this.vertexCount = 0;
		T9R.byteBuffer.clear();
		this.rawBufferIndex = 0;
		this.addedVertices = 0;
	}

	/**
	 * The function "quads" starts drawing quads using OpenGL in Java.
	 */
	public void quads() {
		this.start(GL11.GL_QUADS);
	}

	/**
	 * The `start` method in Java sets up the drawing mode and initializes various flags for a drawing
	 * operation, throwing an exception if drawing is already in progress.
	 * 
	 * @param drawMode The `drawMode` parameter in the `start` method is used to specify the mode in which
	 * drawing will be performed. It is an integer value that determines how the drawing operations will
	 * be carried out. The specific values and their meanings would depend on the context and
	 * implementation of the `start` method
	 */
	public void start(final int drawMode) {
		if (this.isDrawing) {
			throw new IllegalStateException("You must call draw() before you can start another draw!");
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

	/**
	 * The function `uv` sets the texture coordinates `u` and `v` for a 3D object in Java.
	 * 
	 * @param u The parameter `u` represents the U coordinate of a texture mapping. It is a double value
	 * that specifies the horizontal position within the texture.
	 * @param v The parameter `v` represents the vertical texture coordinate in a UV mapping system. It is
	 * used to specify the position of a texture on the vertical axis of a 3D model.
	 */
	public void uv(final double u, final double v) {
		this.hasTexture = true;
		this.textureU = u;
		this.textureV = v;
	}

	/**
	 * The function sets the brightness level for an object and marks that it has a brightness value.
	 * 
	 * @param brightness The `brightness` parameter is an integer value that represents the level of
	 * brightness.
	 */
	public void brightness(final int brightness) {
		this.hasBrightness = true;
		this.brightness = brightness;
	}

	public void color(final @NonNull Color color) {
		this.color(color.r, color.g, color.b, color.a);
	}

	/**
	 * The `color` method sets the color values using the RGBA components of a given Color object.
	 * 
	 * @param color The `color` parameter is an object of type `Color`, which likely contains properties
	 * for red, green, blue, and alpha values. The method `color` takes this `Color` object as a parameter
	 * and sets the color values accordingly.
	 */
	public void color(final int r, final int g, final int b) {
		this.color(r, g, b, 255F);
	}

	/**
	 * The function `color` converts RGB values from floats to integers by scaling them from 0-1 to 0-255.
	 * 
	 * @param r The parameter `r` represents the red component of the color in the RGB color model.
	 * @param g The parameters `r`, `g`, and `b` represent the red, green, and blue components of a color
	 * respectively. In the provided code snippet, these values are expected to be floating-point numbers
	 * in the range [0.0, 1.0], where 0.0 represents
	 * @param b The parameter `b` in the `color` method represents the blue component of the color in the
	 * RGB color model. It is a float value ranging from 0.0 to 1.0, where 0.0 represents no blue and 1.0
	 * represents full blue intensity in the
	 */
	public void color(final float r, final float g, final float b) {
		this.color((int) (r * 255F), (int) (g * 255F), (int) (b * 255F));
	}

	/**
	 * The function `color` takes in float values for red, green, blue, and alpha components, converts
	 * them to integer values in the range 0-255, and then calls another `color` method with these integer
	 * values.
	 * 
	 * @param r The parameter `r` represents the red component of the color in the RGBA color model. It is
	 * a float value ranging from 0.0 to 1.0, where 0.0 represents no red and 1.0 represents full red
	 * intensity.
	 * @param g The parameter `g` in the `color` method represents the green component of the color in the
	 * RGBA color model. It is a float value ranging from 0.0 to 1.0, where 0.0 represents no green and
	 * 1.0 represents full green intensity.
	 * @param b The parameter `b` in the `color` method represents the blue component of the color in the
	 * RGBA color model. It is a float value ranging from 0.0 to 1.0, where 0.0 represents no blue and 1.0
	 * represents full blue intensity in the
	 * @param a The `a` parameter in the `color` method represents the alpha value of the color, which
	 * determines the transparency of the color. It is a float value ranging from 0.0 (completely
	 * transparent) to 1.0 (completely opaque). The method then converts this float value
	 */
	public void color(final float r, final float g, final float b, final float a) {
		this.color((int) (r * 255F), (int) (g * 255F), (int) (b * 255F), (int) (a * 255F));
	}

	/**
	 * The function ensures that the RGB values are within the valid range of 0 to 255.
	 * 
	 * @param r The parameter `r` represents the red component of the color.
	 * @param g The `g` parameter represents the green component of the color in the `color` method. It is
	 * a byte value that is being bitwise ANDed with 255 to ensure it is within the valid range of 0 to
	 * 255 for a color component.
	 * @param n The `n` parameter in the `color` method is a byte representing the blue component of the
	 * color.
	 */
	public void color(final byte r, final byte g, final byte n) {
		this.color(r & 255, g & 255, n & 255);
	}

	/**
	 * The `color` method takes an RGB integer value, extracts the red, green, and blue components, and
	 * then calls another method to set the color using these components.
	 * 
	 * @param rgb The `rgb` parameter is an integer value representing a color in RGB format. The color is
	 * encoded in the integer value where the red, green, and blue components are packed into the integer.
	 * The red component is stored in bits 16-23, the green component in bits 8-15
	 */
	public void color(final int rgb) {
		final int r = rgb >> 16 & 255;
		final int g = rgb >> 8 & 255;
		final int b = rgb & 255;
		this.color(r, g, b);
	}

	/**
	 * The function takes an RGB color value and an alpha value, extracts the individual color components,
	 * and then calls another function to set the color with the specified alpha value.
	 * 
	 * @param rgb The `rgb` parameter is an integer value representing a color in RGB format. The color is
	 * encoded as a 32-bit integer where the least significant 24 bits represent the red, green, and blue
	 * components of the color, and the most significant 8 bits are typically used for the alpha channel
	 * @param a The parameter `a` in the `color` method represents the alpha value of the color. In
	 * computer graphics, the alpha value determines the transparency of the color. A value of 0 means
	 * fully transparent, while a value of 255 means fully opaque.
	 */
	public void color(final int rgb, final int a) {
		final int r = rgb >> 16 & 255;
		final int g = rgb >> 8 & 255;
		final int b = rgb & 255;
		this.color(r, g, b, a);
	}

	/**
	 * The `color` method sets the color values (RGBA) with bounds checking and endianness consideration.
	 * 
	 * @param r The parameter `r` in the `color` method represents the red component of the color in the
	 * RGBA (Red, Green, Blue, Alpha) color model. It specifies the intensity of the red color in the
	 * range of 0 to 255, where 0 is no red and 255
	 * @param g The parameter `g` in the `color` method represents the green component of the color in RGB
	 * format. It is an integer value ranging from 0 to 255, where 0 represents no green and 255
	 * represents full green intensity.
	 * @param b The parameter `b` in the `color` method represents the blue component of the color in the
	 * RGBA (Red, Green, Blue, Alpha) color model. It specifies the intensity of the blue color in the
	 * range of 0 to 255, where 0 is no blue and 255
	 * @param a The parameter `a` in the `color` method represents the alpha value of the color. In
	 * computer graphics, the alpha value determines the transparency of a color. A value of 0 means fully
	 * transparent, while a value of 255 means fully opaque.
	 */
	public void color(int r, int g, int b, int a) {
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

	/**
	 * The function `vertexUV` sets the texture coordinates (u, v) and vertex position (x, y, z) for a
	 * vertex in Java.
	 * 
	 * @param x The parameter `x` represents the x-coordinate of the vertex in 3D space.
	 * @param y The `y` parameter in the `vertexUV` method represents the vertical position of the vertex
	 * in a 3D space. It is used to specify the y-coordinate of the vertex where it will be rendered in
	 * the graphics system.
	 * @param z The parameter `z` in the `vertexUV` method represents the z-coordinate of the vertex in a
	 * 3D space. It specifies the position of the vertex along the z-axis.
	 * @param u The parameter `u` represents the U coordinate of the texture mapping for the vertex. It is
	 * used to specify how the texture is mapped onto the geometry at the given vertex in the 3D space.
	 * @param v The parameter `v` in the `vertexUV` method represents the vertical texture coordinate for
	 * the vertex being processed. It is used to specify how the texture is mapped onto the 3D model at
	 * the given vertex position.
	 */
	public void vertexUV(final double x, final double y, final double z, final double u, final double v) {
		this.uv(u, v);
		this.vertex(x, y, z);
	}

	/**
	 * The `vertex` method adds a vertex with specified coordinates and attributes to a buffer, resizing
	 * the buffer if needed.
	 * 
	 * @param x The parameter `x` in the `vertex` method represents the x-coordinate of the vertex in a 3D
	 * space. It specifies the position of the vertex along the horizontal axis.
	 * @param y The `y` parameter in the `vertex` method represents the y-coordinate of the vertex being
	 * added to the buffer. It specifies the vertical position of the vertex in the coordinate system.
	 * @param v The parameter `v` in the `vertex` method represents the z-coordinate of the vertex being
	 * added to the buffer. It is used to specify the position of the vertex along the z-axis in a 3D
	 * space.
	 */
	public void vertex(final double x, final double y, final double v) {
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

	/**
	 * The function `disableColor()` sets the `isColorDisabled` variable to true.
	 */
	public void disableColor() {
		this.isColorDisabled = true;
	}

	/**
	 * The `normals` function calculates and sets the normal vector components based on the input x, y,
	 * and z values.
	 * 
	 * @param x The `x`, `y`, and `z` parameters in the `normals` method represent the components of a
	 * normal vector in 3D space. These components are used to calculate the byte values for the x, y, and
	 * z components of the normal vector. The method then combines these
	 * @param y The `normals` method takes in three parameters `x`, `y`, and `z`, which represent the
	 * components of a normal vector in 3D space. In the context of this method, `y` represents the
	 * y-component of the normal vector.
	 * @param z The `normals` method takes in three parameters `x`, `y`, and `z`, which represent the
	 * components of a normal vector in 3D space. The method calculates the byte values for each component
	 * by scaling the input values and then combines them into a single integer value to represent the
	 */
	public void normals(final float x, final float y, final float z) {
		this.hasNormals = true;
		final byte normalX = (byte) (int) (x * 127F);
		final byte normalY = (byte) (int) (y * 127F);
		final byte normalZ = (byte) (int) (z * 127F);
		this.normal = normalX & 255 | (normalY & 255) << 8 | (normalZ & 255) << 16;
	}

	/**
	 * The `translate` function in Java updates the offsets by adding the specified values to the current
	 * x, y, and z offsets.
	 * 
	 * @param x The `x` parameter represents the amount by which the object should be translated along the
	 * x-axis.
	 * @param y The parameter `y` in the `translate` method represents the amount by which the object will
	 * be translated along the y-axis.
	 * @param z The parameter `z` in the `translate` method represents the amount by which the object will
	 * be translated along the z-axis. It is used to move the object in the third dimension, perpendicular
	 * to both the x-axis and y-axis.
	 */
	public void translate(final float x, final float y, final float z) {
		this.xOffset += x;
		this.yOffset += y;
		this.zOffset += z;
	}

	public static @NonNull T9R inst() {
		return T9R.INSTANCE;
	}

	public @NonNull T9R copy() {
		return new T9R();
	}

}