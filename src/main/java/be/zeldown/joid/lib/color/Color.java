package be.zeldown.joid.lib.color;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;

import lombok.NonNull;

public final class Color {

	public static final Color WHITE       = new Color(1.0F, 1.0F, 1.0F, 1.0F);
	public static final Color YELLOW      = new Color(1.0F, 1.0F, 0.0F, 1.0F);
	public static final Color RED         = new Color(1.0F, 0.0F, 0.0F, 1.0F);
	public static final Color BLUE        = new Color(0.0F, 0.0F, 1.0F, 1.0F);
	public static final Color GREEN       = new Color(0.0F, 1.0F, 0.0F, 1.0F);
	public static final Color BLACK       = new Color(0.0F, 0.0F, 0.0F, 1.0F);
	public static final Color GRAY        = new Color(0.5F, 0.5F, 0.5F, 1.0F);
	public static final Color CYAN        = new Color(0.0F, 1.0F, 1.0F, 1.0F);
	public static final Color DARKGRAY    = new Color(0.3F, 0.3F, 0.3F, 1.0F);
	public static final Color LIGHTGRAY   = new Color(0.7F, 0.7F, 0.7F, 1.0F);
	public static final Color PINK        = new Color(1.0F, 0.7F, 0.7F, 1.0F);
	public static final Color ORANGE      = new Color(1.0F, 0.8F, 0.0F, 1.0F);
	public static final Color MAGENTA     = new Color(1.0F, 0.0F, 1.0F, 1.0F);
	public static final Color TRANSPARENT = new Color(0.0F, 0.0F, 0.0F, 0.0F);

	public float r = 0.0F;
	public float g = 0.0F;
	public float b = 0.0F;
	public float a = 1.0F;

	/**
	 * Constructs a new {@code Color} instance by copying the RGB and alpha components from another color.
	 *
	 * @param color The color from which to copy the RGB and alpha components.
	 * @throws NullPointerException If the provided color is {@code null}.
	 */
	public Color(final @NonNull Color color) {
		this(color.r, color.g, color.b, color.a);
	}

	/**
	 * Constructs a new {@code Color} instance using the RGB and alpha components from a {@code java.awt.Color}.
	 * <p>
	 * The RGB and alpha components are normalized to the range [0.0, 1.0], where 0.0 corresponds to 0, and 1.0 corresponds to 255.
	 * </p>
	 *
	 * @param color The {@code java.awt.Color} from which to extract the RGB and alpha components.
	 * @throws NullPointerException If the provided color is {@code null}.
	 */
	public Color(final @NonNull java.awt.Color color) {
		this(color.getRed()/255F, color.getGreen()/255F, color.getBlue()/255F, color.getAlpha()/255F);
	}

	/**
	 * Creates a color component based on the first 4 elements of a float buffer.
	 *
	 * @param buffer The buffer from which to read the color. It should contain at least 4 elements.
	 * @throws NullPointerException     If the provided buffer is {@code null}.
	 * @throws IllegalArgumentException If the buffer does not contain at least 4 elements.
	 */
	public Color(final @NonNull FloatBuffer buffer) {
		this(buffer.get(), buffer.get(), buffer.get(), buffer.get());
	}

	/**
	 * Constructs a new {@code Color} instance with the specified RGB components and default alpha of 1.0.
	 *
	 * @param r The red component, in the range [0.0, 1.0].
	 * @param g The green component, in the range [0.0, 1.0].
	 * @param b The blue component, in the range [0.0, 1.0].
	 * @throws IllegalArgumentException If any of the RGB components is outside the valid range [0.0, 1.0].
	 */
	public Color(final float r, final float g, final float b) {
		this(r, g, b, 1.0F);
	}

	/**
	 * Constructs a new {@code Color} instance with the specified RGB components and default alpha of 1.0.
	 * <p>
	 * The RGB components are normalized to the range [0.0, 1.0], where 0 corresponds to 0, and 1 corresponds to 255.
	 * </p>
	 *
	 * @param r The red component, in the range [0, 255].
	 * @param g The green component, in the range [0, 255].
	 * @param b The blue component, in the range [0, 255].
	 * @throws IllegalArgumentException If any of the RGB components is outside the valid range [0, 255].
	 */
	public Color(final int r, final int g, final int b) {
		this(r/255F, g/255F, b/255F, 1F);
	}

	/**
	 * Constructs a new {@code Color} instance with the specified RGBA components.
	 * <p>
	 * The RGB components are normalized to the range [0.0, 1.0], where 0 corresponds to 0, and 1 corresponds to 255.
	 * The alpha component is normalized to the range [0.0, 1.0], where 0 corresponds to 0, and 1 corresponds to 255.
	 * </p>
	 *
	 * @param r The red component, in the range [0, 255].
	 * @param g The green component, in the range [0, 255].
	 * @param b The blue component, in the range [0, 255].
	 * @param a The alpha component, in the range [0, 255].
	 * @throws IllegalArgumentException If any of the RGBA components is outside the valid range [0, 255].
	 */
	public Color(final int r, final int g, final int b, final int a) {
		this(r/255F, g/255F, b/255F, a/255F);
	}

	/**
	 * Constructs a new {@code Color} instance from an integer value representing RGBA components.
	 * <p>
	 * The integer value is interpreted as follows:
	 * <ul>
	 *   <li>Bits 24-31 represent the alpha component.</li>
	 *   <li>Bits 16-23 represent the red component.</li>
	 *   <li>Bits 8-15 represent the green component.</li>
	 *   <li>Bits 0-7 represent the blue component.</li>
	 * </ul>
	 * Each component is normalized to the range [0.0, 1.0], where 0 corresponds to 0, and 1 corresponds to 255.
	 * </p>
	 *
	 * @param value The integer value representing RGBA components.
	 * @throws IllegalArgumentException If the integer value is outside the valid range.
	 */
	public Color(final int value) {
		final int r = (value & 0x00FF0000) >> 16;
		final int g = (value & 0x0000FF00) >> 8;
		final int b = (value & 0x000000FF);
		int a = (value & 0xFF000000) >> 24;

		if (a < 0) {
			a += 256;
		}
		if (a == 0) {
			a = 255;
		}

		this.r = r / 255.0f;
		this.g = g / 255.0f;
		this.b = b / 255.0f;
		this.a = a / 255.0f;
	}

	/**
	 * Constructs a new {@code Color} instance with the specified RGBA components.
	 * <p>
	 * The RGB components are clamped to the range [0.0, 1.0], ensuring that they do not exceed this range.
	 * The alpha component is also clamped to the range [0.0, 1.0], ensuring that it does not exceed this range.
	 * </p>
	 *
	 * @param r The red component, in the range [0.0, 1.0].
	 * @param g The green component, in the range [0.0, 1.0].
	 * @param b The blue component, in the range [0.0, 1.0].
	 * @param a The alpha component, in the range [0.0, 1.0].
	 */
	public Color(final float r, final float g, final float b, final float a) {
		this.r = Math.min(r, 1);
		this.g = Math.min(g, 1);
		this.b = Math.min(b, 1);
		this.a = Math.min(a, 1);
	}

	/**
	 * Creates a new {@code Color} instance by decoding the specified string representation of an RGB color.
	 * <p>
	 * The string representation should be in hexadecimal format, optionally prefixed with a '#' character.
	 * If the string does not start with '#', the method adds the prefix before decoding.
	 * </p>
	 *
	 * @param nm The string representation of an RGB color in hexadecimal format.
	 * @return A new {@code Color} instance representing the decoded RGB color.
	 * @throws NumberFormatException If the string does not represent a valid hexadecimal color.
	 * @throws IllegalArgumentException If the decoded integer value is outside the valid range.
	 * @throws NullPointerException If the provided string is {@code null}.
	 */
	public static @NonNull Color decode(@NonNull String nm) {
		if (!nm.startsWith("#")) {
			nm = "#" + nm;
		}

		if (nm.length() == 7) {
			return new Color(Integer.decode(nm.substring(0, 7)));
		} else if (nm.length() == 9) {
			final int intval = Integer.decode(nm);
			final int red    = (intval >> 24) & 0xFF;
			final int green  = (intval >> 16) & 0xFF;
			final int blue   = (intval >>  8) & 0xFF;
			final int alpha  = (intval >>  0) & 0xFF;
			return new Color(red, green, blue, alpha);
		} else {
			throw new NumberFormatException("Invalid color: " + nm);
		}
	}

	/**
	 * Encodes the RGB components of this {@code Color} instance into a hexadecimal color code.
	 * <p>
	 * The method converts the red, green, and blue components to their hexadecimal representation and pads
	 * them with zeros if necessary. The resulting color code is prefixed with '#' and returned in uppercase.
	 * </p>
	 *
	 * @return The hexadecimal color code representing the RGB components of this {@code Color} instance.
	 */
	public @NonNull String encode() {
		String hexRed = Integer.toHexString(this.getRed());
		String hexGreen = Integer.toHexString(this.getGreen());
		String hexBlue = Integer.toHexString(this.getBlue());
		String hexAlpha = Integer.toHexString(this.getAlpha());

		hexRed = this.padZero(hexRed);
		hexGreen = this.padZero(hexGreen);
		hexBlue = this.padZero(hexBlue);
		hexAlpha = this.padZero(hexAlpha);

		final String hexCode = "#" + hexRed + hexGreen + hexBlue + hexAlpha;
		return hexCode.toUpperCase();
	}

	private @NonNull String padZero(final @NonNull String str) {
		return (str.length() == 1) ? "0" + str : str;
	}

	/**
	 * Creates a new {@code Color} instance with the same red, green, and blue components, using the specified grayscale intensity.
	 * <p>
	 * The method sets the red, green, and blue components to the specified grayscale intensity value, creating a monochromatic color.
	 * </p>
	 *
	 * @param color The grayscale intensity value to use for all RGB components.
	 * @return A new {@code Color} instance with the specified grayscale intensity.
	 */
	public static @NonNull Color fill(final float color) {
		return new Color(color, color, color);
	}

	/**
	 * Creates a new {@code Color} instance with the same red, green, and blue components, using the specified grayscale intensity.
	 * <p>
	 * The method sets the red, green, and blue components to the specified grayscale intensity value, creating a monochromatic color.
	 * </p>
	 *
	 * @param color The grayscale intensity value to use for all RGB components. Must be in the range [0, 255].
	 * @return A new {@code Color} instance with the specified grayscale intensity.
	 * @throws IllegalArgumentException If the specified grayscale intensity is outside the valid range [0, 255].
	 */
	public static @NonNull Color fill(final int color) {
		return new Color(color, color, color);
	}

	/**
	 * Binds the color values to the OpenGL color state.
	 * <p>
	 * This method sets the current OpenGL color to the values of the {@code Color} instance, affecting subsequent rendering operations.
	 * </p>
	 */
	public void bind() {
		GL11.glColor4f(this.r, this.g, this.b, this.a);
	}

	/**
	 * Returns the RGB representation of this color as a single integer.
	 * <p>
	 * The RGB value is packed into the integer as follows:
	 * <ul>
	 *   <li>Bits 24-31 represent the alpha channel, where 0xFF corresponds to 100% opacity.</li>
	 *   <li>Bits 16-23 represent the red channel.</li>
	 *   <li>Bits 8-15 represent the green channel.</li>
	 *   <li>Bits 0-7 represent the blue channel.</li>
	 * </ul>
	 * </p>
	 *
	 * @return The RGB representation of this color as a single integer.
	 */
	public int getRGB() {
		return (((int)(this.a * 255) & 0xFF) << 24) |
				(((int)(this.r * 255) & 0xFF) << 16) |
				(((int)(this.g * 255) & 0xFF) << 8)  |
				(((int)(this.b * 255) & 0xFF) << 0);
	}

	/**
	 * Creates a new {@code Color} instance that is a darker shade of the current color.
	 * <p>
	 * This method darkens the color by reducing the intensity of each RGB component.
	 * </p>
	 *
	 * @return A new {@code Color} instance representing a darker shade of the current color.
	 */
	public @NonNull Color darker() {
		return this.darker(0.5f);
	}

	/**
	 * Creates a new {@code Color} instance that is a darker shade of the current color.
	 * <p>
	 * This method darkens the color by reducing the intensity of each RGB component based on the specified scale.
	 * </p>
	 *
	 * @param scale The scale factor for darkening the color, ranging from 0 to 1. A scale of 0 produces the same color, while a scale of 1 produces a completely dark color.
	 * @return A new {@code Color} instance representing a darker shade of the current color.
	 * @throws IllegalArgumentException If the scale is outside the valid range [0, 1].
	 */
	public @NonNull Color darker(float scale) {
		scale = 1 - scale;

		return new Color(this.r * scale,this.g * scale,this.b * scale,this.a);
	}

	/**
	 * Creates a new {@code Color} instance that is a brighter shade of the current color.
	 * <p>
	 * This method brightens the color by increasing the intensity of each RGB component.
	 * </p>
	 *
	 * @return A new {@code Color} instance representing a brighter shade of the current color.
	 */
	public @NonNull Color brighter() {
		return this.brighter(0.2f);
	}

	/**
	 * Gets the red component of this color as an integer in the range [0, 255].
	 *
	 * @return The red component of this color.
	 */
	public int getRed() {
		return (int) (this.r * 255);
	}

	/**
	 * Gets the green component of this color as an integer in the range [0, 255].
	 *
	 * @return The green component of this color.
	 */
	public int getGreen() {
		return (int) (this.g * 255);
	}

	/**
	 * Gets the blue component of this color as an integer in the range [0, 255].
	 *
	 * @return The blue component of this color.
	 */
	public int getBlue() {
		return (int) (this.b * 255);
	}

	/**
	 * Gets the alpha (transparency) component of this color as an integer in the range [0, 255].
	 *
	 * @return The alpha component of this color.
	 */
	public int getAlpha() {
		return (int) (this.a * 255);
	}

	/**
	 * Gets the red component of this color as an integer in the range [0, 255].
	 *
	 * @return The red component of this color.
	 */
	public int getRedByte() {
		return (int) (this.r * 255);
	}

	/**
	 * Gets the green component of this color as an integer in the range [0, 255].
	 *
	 * @return The green component of this color.
	 */
	public int getGreenByte() {
		return (int) (this.g * 255);
	}

	/**
	 * Gets the blue component of this color as an integer in the range [0, 255].
	 *
	 * @return The blue component of this color.
	 */
	public int getBlueByte() {
		return (int) (this.b * 255);
	}

	/**
	 * Gets the alpha component of this color as an integer in the range [0, 255].
	 *
	 * @return The alpha component of this color.
	 */
	public int getAlphaByte() {
		return (int) (this.a * 255);
	}

	/**
	 * Creates a new color that is brighter than this color by the specified scale factor.
	 *
	 * @param scale The scale factor to adjust the brightness. Should be greater than 0.
	 * @return A new color with adjusted brightness.
	 * @throws IllegalArgumentException If the scale is not greater than 0.
	 */
	public @NonNull Color brighter(float scale) {
		scale += 1;
		return new Color(this.r * scale,this.g * scale,this.b * scale,this.a);
	}

	/**
	 * Creates a new color by multiplying each component of this color with the corresponding component of the given color.
	 *
	 * @param c The color to multiply with.
	 * @return A new color resulting from the component-wise multiplication.
	 * @throws NullPointerException If the provided color is {@code null}.
	 */
	public @NonNull Color multiply(final @NonNull Color c) {
		return new Color(this.r * c.r, this.g * c.g, this.b * c.b, this.a * c.a);
	}

	/**
	 * Adds the components of the given color to the corresponding components of this color.
	 *
	 * @param c The color to add.
	 * @throws NullPointerException If the provided color is {@code null}.
	 */
	public void add(final @NonNull Color c) {
		this.r += c.r;
		this.g += c.g;
		this.b += c.b;
		this.a += c.a;
	}

	/**
	 * Scales each component of the color by the specified factor.
	 *
	 * @param value The scaling factor.
	 */
	public void scale(final float value) {
		this.r *= value;
		this.g *= value;
		this.b *= value;
		this.a *= value;
	}

	/**
	 * Creates a new color by adding the components of the specified color to a copy of this color.
	 *
	 * @param c The color to add.
	 * @return A new color resulting from the addition.
	 */
	public @NonNull Color addToCopy(final @NonNull Color c) {
		final Color copy = new Color(this.r,this.g,this.b,this.a);
		copy.r += c.r;
		copy.g += c.g;
		copy.b += c.b;
		copy.a += c.a;

		return copy;
	}

	/**
	 * Creates a new color by scaling the components of this color by the specified value.
	 *
	 * @param value The scaling factor.
	 * @return A new color resulting from the scaling operation.
	 */
	public @NonNull Color scaleCopy(final float value) {
		final Color copy = new Color(this.r,this.g,this.b,this.a);
		copy.r *= value;
		copy.g *= value;
		copy.b *= value;
		copy.a *= value;

		return copy;
	}

	/**
	 * Generates an intermediate color between this color and a target color based on the specified progress.
	 * <p>
	 * The resulting color is determined by interpolating between the RGB components of this color and the target color
	 * according to the specified progress. The progress should be in the range [0.0, 1.0], where 0.0 represents
	 * this color, 1.0 represents the target color, and values in between represent the intermediate colors.
	 * </p>
	 *
	 * @param target The target color to interpolate towards.
	 * @param progress The progress of the interpolation, in the range [0.0, 1.0].
	 * @return The interpolated color between this color and the target color.
	 * @throws NullPointerException If the target color is {@code null}.
	 */
	public @NonNull Color to(final @NonNull Color target, final float progress) {
		return Color.transition(this, target, progress);
	}

	/**
	 * Converts the RGB color components of this {@code Color} instance to the equivalent
	 * HSB (Hue, Saturation, Brightness) representation.
	 *
	 * @param hsbvals An optional array to store the resulting HSB values (Hue, Saturation, Brightness).
	 *                If {@code null}, a new array will be created.
	 * @return An array containing the HSB values (Hue, Saturation, Brightness) of this RGB color.
	 * @throws IllegalArgumentException If any of the RGB components of this color are outside the valid range [0, 255].
	 */
	public @NonNull float[] RGBtoHSB(float[] hsbvals) {
		float hue, saturation, brightness;
		if (hsbvals == null) {
			hsbvals = new float[3];
		}

		int cmax = (this.getRed() > this.getGreen()) ? this.getRed() : this.getGreen();
		if (this.getBlue() > cmax) {
			cmax = this.getBlue();
		}

		int cmin = (this.getRed() < this.getGreen()) ? this.getRed() : this.getGreen();
		if (this.getBlue() < cmin) {
			cmin = this.getBlue();
		}

		brightness = (cmax) / 255.0f;
		if (cmax != 0) {
			saturation = ((float) (cmax - cmin)) / ((float) cmax);
		} else {
			saturation = 0;
		}

		if (saturation == 0) {
			hue = 0;
		} else {
			final float redc = ((float) (cmax - this.getRed())) / ((float) (cmax - cmin));
			final float greenc = ((float) (cmax - this.getGreen())) / ((float) (cmax - cmin));
			final float bluec = ((float) (cmax - this.getBlue())) / ((float) (cmax - cmin));
			if (this.getRed() == cmax) {
				hue = bluec - greenc;
			} else if (this.getGreen() == cmax) {
				hue = 2.0f + redc - bluec;
			} else {
				hue = 4.0f + greenc - redc;
			}

			hue = hue / 6.0f;
			if (hue < 0) {
				hue = hue + 1.0f;
			}
		}
		hsbvals[0] = hue;
		hsbvals[1] = saturation;
		hsbvals[2] = brightness;
		return hsbvals;
	}

	/**
	 * Creates a copy of this {@code Color} instance.
	 * @return A new {@code Color} instance with the same RGB and alpha components as this color.
	 */
	public @NonNull Color copy() {
		return new Color(this.r, this.g, this.b, this.a);
	}

	/**
	 * Creates a copy of this {@code Color} instance with the specified alpha
	 * component.
	 *
	 * @param alpha The alpha component of the new color.
	 * @return A new {@code Color} instance with the same RGB components and the specified alpha component.
	 */
	public @NonNull Color copyAlpha(final float alpha) {
		return new Color(this.r, this.g, this.b, alpha);
	}

	/**
	 * Creates a copy of this {@code Color} instance with the specified red
	 * component.
	 *
	 * @param red The red component of the new color.
	 * @return A new {@code Color} instance with the specified red component and the same green, blue, and alpha components.
	 */
	public @NonNull Color copyRed(final float red) {
		return new Color(red, this.g, this.b, this.a);
	}

	/**
	 * Creates a copy of this {@code Color} instance with the specified green
	 * component.
	 *
	 * @param green The green component of the new color.
	 * @return A new {@code Color} instance with the same red, blue, and alpha components and the specified green component.
	 */
	public @NonNull Color copyGreen(final float green) {
		return new Color(this.r, green, this.b, this.a);
	}

	/**
	 * Creates a copy of this {@code Color} instance with the specified blue
	 * component.
	 *
	 * @param blue The blue component of the new color.
	 * @return A new {@code Color} instance with the same red, green, and alpha components and the specified blue component.
	 */
	public @NonNull Color copyBlue(final float blue) {
		return new Color(this.r, this.g, blue, this.a);
	}

	/**
	 * Converts the RGB color components to the equivalent HSB (Hue, Saturation, Brightness) representation.
	 *
	 * @param r The red component of the RGB color (0-255).
	 * @param g The green component of the RGB color (0-255).
	 * @param b The blue component of the RGB color (0-255).
	 * @param hsbvals An optional array to store the resulting HSB values (Hue, Saturation, Brightness).
	 *                If {@code null}, a new array will be created.
	 * @return An array containing the HSB values (Hue, Saturation, Brightness) of the input RGB color.
	 * @throws IllegalArgumentException If any of the RGB components are outside the valid range [0, 255].
	 */
	public static @NonNull float[] RGBtoHSB(final int r, final int g, final int b, float[] hsbvals) {
		float hue, saturation, brightness;
		if (hsbvals == null) {
			hsbvals = new float[3];
		}

		int cmax = (r > g) ? r : g;
		if (b > cmax) {
			cmax = b;
		}

		int cmin = (r < g) ? r : g;
		if (b < cmin) {
			cmin = b;
		}

		brightness = (cmax) / 255.0f;
		if (cmax != 0) {
			saturation = ((float) (cmax - cmin)) / ((float) cmax);
		} else {
			saturation = 0;
		}

		if (saturation == 0) {
			hue = 0;
		} else {
			final float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
			final float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
			final float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
			if (r == cmax) {
				hue = bluec - greenc;
			} else if (g == cmax) {
				hue = 2.0f + redc - bluec;
			} else {
				hue = 4.0f + greenc - redc;
			}

			hue = hue / 6.0f;
			if (hue < 0) {
				hue = hue + 1.0f;
			}
		}
		hsbvals[0] = hue;
		hsbvals[1] = saturation;
		hsbvals[2] = brightness;
		return hsbvals;
	}

	/**
	 * Resets the OpenGL color to the default white color with full opacity (alpha).
	 * <p>
	 * This method sets the OpenGL color to (1.0, 1.0, 1.0, 1.0), representing white with full opacity.
	 * </p>
	 */
	public static void reset() {
		GL11.glColor4f(1F, 1F, 1F, 1F);
	}

	/**
	 * Performs a linear color transition between two colors based on the specified progress.
	 * <p>
	 * The progress parameter should be a value between 0.0 and 1.0, where 0.0 represents the
	 * initial color (color1) and 1.0 represents the final color (color2). Values between 0.0
	 * and 1.0 represent a linear interpolation between the two colors.
	 * </p>
	 *
	 * @param color1 The starting color of the transition.
	 * @param color2 The ending color of the transition.
	 * @param progress The progress of the transition, ranging from 0.0 to 1.0.
	 * @return The color resulting from the linear transition.
	 * @throws NullPointerException If either color1 or color2 is {@code null}.
	 */
	public static @NonNull Color transition(final @NonNull Color color1, final @NonNull Color color2, final float progress) {
		if (progress == 0) {
			return color1;
		} else if (progress == 1) {
			return color2;
		}

		return new Color(
				((color1.r * (1F - progress)) + (color2.r * progress)),
				((color1.g * (1F - progress)) + (color2.g * progress)),
				((color1.b * (1F - progress)) + (color2.b * progress)),
				((color1.a * (1F - progress)) + (color2.a * progress))
				);
	}

	/**
	 * Creates a new color that is a rainbow color based on the current system time.
	 * @return A new color representing a rainbow color.
	 */
	public static @NonNull Color RAINBOW() {
		return new Color(java.awt.Color.HSBtoRGB((System.currentTimeMillis() % 3000L) / 3000.0F, 0.8F, 0.8F));
	}

	/**
	 * Creates a new color that is a loading color based on the current system time.
	 * @return A new color representing a loading color.
	 */
	public static @NonNull Color LOADING() {
		final long now = System.currentTimeMillis();
		final float color = (float)(((Math.sin(2 * Math.PI * (now%4000) / 2000) + 1) / 50F)) + 0.15F;
		return new Color(color, color, color);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public @NonNull String toString() {
		return String.format(
				"Color(%d, %d, %d, %d) [%s]",
				this.getRed(), this.getGreen(), this.getBlue(), this.getAlpha(),
				this.encode()
				);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return ((int) (this.r+this.g+this.b+this.a)*255);
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object other) {
		if (other instanceof Color) {
			final Color o = (Color) other;
			return ((o.r == this.r) && (o.g == this.g) && (o.b == this.b) && (o.a == this.a));
		}

		return false;
	}

}