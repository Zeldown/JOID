package be.zeldown.joid.lib.color;

import java.nio.FloatBuffer;
import java.util.function.Consumer;

import javax.vecmath.Vector4f;

import be.zeldown.joid.lib.opengl.GLHelper;
import lombok.NonNull;

public final class Color {

	public static final Color WHITE       = new Color(1F, 1F, 1F, 1F);
	public static final Color YELLOW      = new Color(1F, 1F, 0F, 1F);
	public static final Color RED         = new Color(1F, 0F, 0F, 1F);
	public static final Color BLUE        = new Color(0F, 0F, 1F, 1F);
	public static final Color GREEN       = new Color(0F, 1F, 0F, 1F);
	public static final Color BLACK       = new Color(0F, 0F, 0F, 1F);
	public static final Color GRAY        = new Color(0.5F, 0.5F, 0.5F, 1F);
	public static final Color CYAN        = new Color(0F, 1F, 1F, 1F);
	public static final Color DARKGRAY    = new Color(0.3F, 0.3F, 0.3F, 1F);
	public static final Color LIGHTGRAY   = new Color(0.7F, 0.7F, 0.7F, 1F);
	public static final Color PINK        = new Color(1F, 0.7F, 0.7F, 1F);
	public static final Color ORANGE      = new Color(1F, 0.8F, 0F, 1F);
	public static final Color MAGENTA     = new Color(1F, 0F, 1F, 1F);
	public static final Color TRANSPARENT = new Color(0F, 0F, 0F, 0F);

	public static final Color RAINBOW = new Color(1F, 1F, 1F, 1F, color -> {
		final Color rainbow = Color.RAINBOW();
		color.r = rainbow.r;
		color.g = rainbow.g;
		color.b = rainbow.b;
		color.a = rainbow.a;
	});
	public static final Color LOADING = new Color(1F, 1F, 1F, 1F, color -> {
		final Color loading = Color.LOADING();
		color.r = loading.r;
		color.g = loading.g;
		color.b = loading.b;
		color.a = loading.a;
	});

	public float r = 0F;
	public float g = 0F;
	public float b = 0F;
	public float a = 1F;

	public Consumer<Color> update;
	public ColorGradient gradient;

	public Color(final @NonNull Color color) {
		this(color.r, color.g, color.b, color.a);
	}

	public Color(final @NonNull ColorGradient gradient) {
		this(gradient.getStartColor());
		this.gradient = gradient;
	}

	public Color(final @NonNull java.awt.Color color) {
		this(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
	}

	public Color(final @NonNull FloatBuffer buffer) {
		this(buffer.get(), buffer.get(), buffer.get(), buffer.get());
	}

	public Color(final float r, final float g, final float b) {
		this(r, g, b, 1F);
	}

	public Color(final int r, final int g, final int b) {
		this(r / 255F, g / 255F, b / 255F, 1F);
	}

	public Color(final int r, final int g, final int b, final int a) {
		this(r / 255F, g / 255F, b / 255F, a / 255F);
	}

	public Color(final int value) {
		final int r = (value & 0x00FF0000) >> 16;
		final int g = (value & 0x0000FF00) >> 8;
		final int b = value & 0x000000FF;
		int a = (value & 0xFF000000) >> 24;

		if (a < 0) {
			a += 256;
		}
		if (a == 0) {
			a = 255;
		}

		this.r = r / 255F;
		this.g = g / 255F;
		this.b = b / 255F;
		this.a = a / 255F;
	}

	public Color(final float r, final float g, final float b, final float a) {
		this.r = Math.min(r, 1);
		this.g = Math.min(g, 1);
		this.b = Math.min(b, 1);
		this.a = Math.min(a, 1);
	}

	public Color(final float r, final float g, final float b, final float a, final Consumer<Color> update) {
		this.r = Math.min(r, 1);
		this.g = Math.min(g, 1);
		this.b = Math.min(b, 1);
		this.a = Math.min(a, 1);

		this.update = update;
	}

	public static @NonNull Color decode(@NonNull String nm) {
		if ("rainbow".equalsIgnoreCase(nm.replace("#", ""))) {
			return Color.RAINBOW();
		}

		if ("loading".equalsIgnoreCase(nm.replace("#", ""))) {
			return Color.LOADING();
		}

		if (nm.startsWith("rgb(")) {
			final String[] parts = nm.substring(4, nm.length() - 1).split(",");
			return new Color(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim()));
		}

		if (nm.startsWith("rgba(")) {
			final String[] parts = nm.substring(5, nm.length() - 1).split(",");
			return new Color(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim()), Integer.parseInt(parts[3].trim()));
		}

		if (nm.startsWith("gradient(")) {
			final String[] parts = nm.substring(9, nm.length() - 1).split(",");
			final Vector4f direction = parts.length < 6 ? new Vector4f(0F, 0F, 1F, 0F) : new Vector4f(Float.parseFloat(parts[2].trim()), Float.parseFloat(parts[3].trim()), Float.parseFloat(parts[4].trim()), Float.parseFloat(parts[5].trim()));
			return new Color(new ColorGradient(Color.decode(parts[0].trim()), Color.decode(parts[1].trim()), direction));
		}

		if (!nm.startsWith("#")) {
			nm = "#" + nm;
		}

		if (nm.length() == 7) {
			return new Color(Integer.decode(nm.substring(0, 7)));
		}

		if (nm.length() == 9) {
			final int intval = Integer.decode(nm);
			final int red    = intval >> 24 & 0xFF;
			final int green  = intval >> 16 & 0xFF;
			final int blue   = intval >>  8 & 0xFF;
			final int alpha  = intval >>  0 & 0xFF;
			return new Color(red, green, blue, alpha);
		}

		throw new NumberFormatException("Invalid color: " + nm);
	}

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
		return str.length() == 1 ? "0" + str : str;
	}

	public static @NonNull Color fill(final float color) {
		return new Color(color, color, color);
	}

	public static @NonNull Color fill(final int color) {
		return new Color(color, color, color);
	}

	public void bind() {
		this.update();
		GLHelper.color(this);
	}

	public void bind(final @NonNull Runnable runnable, final @NonNull Vector4f canvas) {
		if (this.isGradient()) {
			this.gradient.use(runnable, canvas);
		} else {
			this.bind();
			runnable.run();
			Color.reset();
		}
	}

	public int getRGB() {
		return ((int)(this.a * 255) & 0xFF) << 24 | ((int)(this.r * 255) & 0xFF) << 16 | ((int)(this.g * 255) & 0xFF) << 8  | ((int)(this.b * 255) & 0xFF) << 0;
	}

	public @NonNull Color darker() {
		return this.darker(0.5F);
	}

	public @NonNull Color darker(float scale) {
		scale = 1 - scale;

		return new Color(this.r * scale,this.g * scale,this.b * scale,this.a);
	}

	public @NonNull Color brighter() {
		return this.brighter(0.2F);
	}

	public int getRed() {
		return (int) (this.r * 255F);
	}

	public int getGreen() {
		return (int) (this.g * 255F);
	}

	public int getBlue() {
		return (int) (this.b * 255F);
	}

	public int getAlpha() {
		return (int) (this.a * 255F);
	}

	public int getRedByte() {
		return (int) (this.r * 255F);
	}

	public int getGreenByte() {
		return (int) (this.g * 255F);
	}

	public int getBlueByte() {
		return (int) (this.b * 255F);
	}

	public int getAlphaByte() {
		return (int) (this.a * 255F);
	}

	public @NonNull Color brighter(float scale) {
		scale += 1F;
		return new Color(this.r * scale, this.g * scale, this.b * scale, this.a);
	}

	public @NonNull Color multiply(final @NonNull Color c) {
		return new Color(this.r * c.r, this.g * c.g, this.b * c.b, this.a * c.a);
	}

	public void add(final @NonNull Color c) {
		this.r += c.r;
		this.g += c.g;
		this.b += c.b;
		this.a += c.a;
	}

	public void scale(final float value) {
		this.r *= value;
		this.g *= value;
		this.b *= value;
		this.a *= value;
	}

	public @NonNull Color addToCopy(final @NonNull Color c) {
		final Color copy = new Color(this.r, this.g, this.b, this.a, this.update);
		copy.r += c.r;
		copy.g += c.g;
		copy.b += c.b;
		copy.a += c.a;
		return copy;
	}

	public @NonNull Color scaleCopy(final float value) {
		final Color copy = new Color(this.r, this.g, this.b, this.a, this.update);
		copy.r *= value;
		copy.g *= value;
		copy.b *= value;
		copy.a *= value;
		return copy;
	}

	public @NonNull Color to(final @NonNull Color target, final float progress) {
		return Color.transition(this, target, progress);
	}

	public @NonNull Color toGradient(final @NonNull Color target) {
		return this.toGradient(target, new Vector4f(0F, 0F, 1F, 0F));
	}

	public @NonNull Color toGradient(final @NonNull Color target, final @NonNull Vector4f direction) {
		return Color.gradient(this, target, direction);
	}

	public @NonNull float[] RGBtoHSB(float[] hsbvals) {
		float hue, saturation, brightness;
		if (hsbvals == null) {
			hsbvals = new float[3];
		}

		int cmax = this.getRed() > this.getGreen() ? this.getRed() : this.getGreen();
		if (this.getBlue() > cmax) {
			cmax = this.getBlue();
		}

		int cmin = this.getRed() < this.getGreen() ? this.getRed() : this.getGreen();
		if (this.getBlue() < cmin) {
			cmin = this.getBlue();
		}

		brightness = cmax / 255F;
		if (cmax != 0) {
			saturation = (float) (cmax - cmin) / (float) cmax;
		} else {
			saturation = 0;
		}

		if (saturation == 0) {
			hue = 0;
		} else {
			final float redc = (float) (cmax - this.getRed()) / (float) (cmax - cmin);
			final float greenc = (float) (cmax - this.getGreen()) / (float) (cmax - cmin);
			final float bluec = (float) (cmax - this.getBlue()) / (float) (cmax - cmin);
			if (this.getRed() == cmax) {
				hue = bluec - greenc;
			} else if (this.getGreen() == cmax) {
				hue = 2F + redc - bluec;
			} else {
				hue = 4F + greenc - redc;
			}

			hue = hue / 6F;
			if (hue < 0) {
				hue = hue + 1F;
			}
		}
		hsbvals[0] = hue;
		hsbvals[1] = saturation;
		hsbvals[2] = brightness;
		return hsbvals;
	}

	public @NonNull Color copy() {
		return new Color(this.r, this.g, this.b, this.a, this.update);
	}

	public @NonNull Color copyAlpha(final float alpha) {
		return new Color(this.r, this.g, this.b, alpha, this.update);
	}

	public @NonNull Color copyRed(final float red) {
		return new Color(red, this.g, this.b, this.a, this.update);
	}

	public @NonNull Color copyGreen(final float green) {
		return new Color(this.r, green, this.b, this.a, this.update);
	}

	public @NonNull Color copyBlue(final float blue) {
		return new Color(this.r, this.g, blue, this.a, this.update);
	}

	public @NonNull Color update() {
		if (this.update != null) {
			this.update.accept(this);
		}
		return this;
	}

	public boolean isGradient() {
		return this.gradient != null;
	}

	public static @NonNull float[] RGBtoHSB(final int r, final int g, final int b, float[] hsbvals) {
		float hue, saturation, brightness;
		if (hsbvals == null) {
			hsbvals = new float[3];
		}

		int cmax = r > g ? r : g;
		if (b > cmax) {
			cmax = b;
		}

		int cmin = r < g ? r : g;
		if (b < cmin) {
			cmin = b;
		}

		brightness = cmax / 255F;
		if (cmax != 0) {
			saturation = (float) (cmax - cmin) / (float) cmax;
		} else {
			saturation = 0;
		}

		if (saturation == 0) {
			hue = 0;
		} else {
			final float redc = (float) (cmax - r) / (float) (cmax - cmin);
			final float greenc = (float) (cmax - g) / (float) (cmax - cmin);
			final float bluec = (float) (cmax - b) / (float) (cmax - cmin);
			if (r == cmax) {
				hue = bluec - greenc;
			} else if (g == cmax) {
				hue = 2F + redc - bluec;
			} else {
				hue = 4F + greenc - redc;
			}

			hue = hue / 6F;
			if (hue < 0) {
				hue = hue + 1F;
			}
		}
		hsbvals[0] = hue;
		hsbvals[1] = saturation;
		hsbvals[2] = brightness;
		return hsbvals;
	}

	public static void reset() {
		GLHelper.popColor();
	}

	public static @NonNull Color transition(final @NonNull Color color1, final @NonNull Color color2, final float progress) {
		if (progress == 0) {
			return color1;
		}
		if (progress == 1) {
			return color2;
		}

		return new Color(
				color1.r * (1F - progress) + color2.r * progress,
				color1.g * (1F - progress) + color2.g * progress,
				color1.b * (1F - progress) + color2.b * progress,
				color1.a * (1F - progress) + color2.a * progress,
				progress > 0.5F ? color2.update : color1.update
				);
	}

	public static @NonNull Color gradient(final @NonNull Color color1, final @NonNull Color color2, final @NonNull Vector4f direction) {
		return new Color(new ColorGradient(color1, color2, direction));
	}

	public static @NonNull Color RAINBOW() {
		return new Color(java.awt.Color.HSBtoRGB(System.currentTimeMillis() % 3000L / 3000F, 0.8F, 0.8F));
	}

	public static @NonNull Color RAINBOW(final long time) {
		return new Color(java.awt.Color.HSBtoRGB(time % 3000L / 3000F, 0.8F, 0.8F));
	}

	public static @NonNull Color LOADING() {
		final long now = System.currentTimeMillis();
		final float color = (float) ((Math.sin(2 * Math.PI * (now % 4000) / 2000) + 1) / 50F) + 0.15F;
		return new Color(color, color, color);
	}

	@Override
	public @NonNull String toString() {
		return String.format(
				"Color(%d, %d, %d, %d) [%s]",
				this.getRed(), this.getGreen(), this.getBlue(), this.getAlpha(),
				this.encode()
				);
	}

	@Override
	public int hashCode() {
		return (int) (this.r + this.g + this.b + this.a) * 255;
	}

	@Override
	public boolean equals(final Object other) {
		if (other instanceof Color) {
			final Color o = (Color) other;
			return o.r == this.r && o.g == this.g && o.b == this.b && o.a == this.a;
		}
		return false;
	}

}