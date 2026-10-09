package dev.joid.lib.color;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

import javax.vecmath.Vector4f;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.bridge.render.state.RenderState;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode
public final class Color {

	public static final Color RED         = new Color(1F, 0F, 0F, 1F);
	public static final Color BLUE        = new Color(0F, 0F, 1F, 1F);
	public static final Color GRAY        = new Color(0.5F, 0.5F, 0.5F, 1F);
	public static final Color CYAN        = new Color(0F, 1F, 1F, 1F);
	public static final Color PINK        = new Color(1F, 0.7F, 0.7F, 1F);
	public static final Color WHITE       = new Color(1F, 1F, 1F, 1F);
	public static final Color GREEN       = new Color(0F, 1F, 0F, 1F);
	public static final Color BLACK       = new Color(0F, 0F, 0F, 1F);
	public static final Color YELLOW      = new Color(1F, 1F, 0F, 1F);
	public static final Color ORANGE      = new Color(1F, 0.8F, 0F, 1F);
	public static final Color MAGENTA     = new Color(1F, 0F, 1F, 1F);
	public static final Color DARKGRAY    = new Color(0.3F, 0.3F, 0.3F, 1F);
	public static final Color LIGHTGRAY   = new Color(0.7F, 0.7F, 0.7F, 1F);
	public static final Color TRANSPARENT = new Color(0F, 0F, 0F, 0F);

	public static final Color RAINBOW = new Color(1F, 1F, 1F, 1F, color -> Color.RAINBOW().copyAlpha(color.a));
	public static final Color LOADING = new Color(1F, 1F, 1F, 1F, color -> Color.LOADING().copyAlpha(color.a));

	public final float r;
	public final float g;
	public final float b;
	public final float a;

	@EqualsAndHashCode.Exclude public final UnaryOperator<Color> update;
	@EqualsAndHashCode.Exclude public final ColorGradient        gradient;

	public Color(final int value) {
		this((value >> 16 & 0xFF) / 255F, (value >> 8 & 0xFF) / 255F, (value & 0xFF) / 255F, (value >>> 24) / 255F);
	}

	public Color(final Color color) {
		this(color.r, color.g, color.b, color.a);
	}

	public Color(final FloatBuffer buffer) {
		this(buffer.get(), buffer.get(), buffer.get(), buffer.get());
	}

	public Color(final java.awt.Color color) {
		this(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
	}

	public Color(final ColorGradient gradient) {
		this(gradient.getStartColor().r, gradient.getStartColor().g, gradient.getStartColor().b, gradient.getStartColor().a, null, gradient);
	}

	public Color(final int r, final int g, final int b) {
		this(r / 255F, g / 255F, b / 255F, 1F);
	}

	public Color(final float r, final float g, final float b) {
		this(r, g, b, 1F);
	}

	public Color(final int r, final int g, final int b, final int a) {
		this(r / 255F, g / 255F, b / 255F, a / 255F);
	}

	public Color(final float r, final float g, final float b, final float a) {
		this(r, g, b, a, null, null);
	}

	public Color(final float r, final float g, final float b, final float a, final UnaryOperator<Color> update) {
		this(r, g, b, a, update, null);
	}

	private Color(final float r, final float g, final float b, final float a, final UnaryOperator<Color> update, final ColorGradient gradient) {
		this.r = Color.clamp(r);
		this.g = Color.clamp(g);
		this.b = Color.clamp(b);
		this.a = Color.clamp(a);

		this.update = update;
		this.gradient = gradient;
	}

	public static @NonNull Color decode(final @NonNull String nm) {
		final String text = nm.trim();
		final String lower = text.toLowerCase(Locale.ROOT);

		if ("rainbow".equals(lower.replace("#", ""))) {
			return Color.RAINBOW;
		}

		if ("loading".equals(lower.replace("#", ""))) {
			return Color.LOADING;
		}

		if (lower.startsWith("rgb(")) {
			final List<String> parts = Color.arguments(text, 4, 3);
			return new Color(Integer.parseInt(parts.get(0)), Integer.parseInt(parts.get(1)), Integer.parseInt(parts.get(2)));
		}

		if (lower.startsWith("rgba(")) {
			final List<String> parts = Color.arguments(text, 5, 4);
			return new Color(Integer.parseInt(parts.get(0)) / 255F, Integer.parseInt(parts.get(1)) / 255F, Integer.parseInt(parts.get(2)) / 255F, Float.parseFloat(parts.get(3)));
		}

		if (lower.startsWith("gradient(")) {
			final List<String> parts = Color.arguments(text, 9, 2, 6);
			final Vector4f direction = parts.size() == 2 ? new Vector4f(0F, 0F, 1F, 0F) : new Vector4f(Float.parseFloat(parts.get(2)), Float.parseFloat(parts.get(3)), Float.parseFloat(parts.get(4)), Float.parseFloat(parts.get(5)));
			return new Color(new ColorGradient(Color.decode(parts.get(0)), Color.decode(parts.get(1)), direction));
		}

		final String hex = text.startsWith("#") ? text : "#" + text;
		if (hex.length() == 7) {
			return new Color(0xFF000000 | Integer.parseUnsignedInt(hex.substring(1), 16));
		}

		if (hex.length() == 9) {
			final int intval = Integer.parseUnsignedInt(hex.substring(1), 16);
			final int red    = intval >> 24 & 0xFF;
			final int green  = intval >> 16 & 0xFF;
			final int blue   = intval >>  8 & 0xFF;
			final int alpha  = intval >>  0 & 0xFF;
			return new Color(red, green, blue, alpha);
		}

		throw new NumberFormatException("Invalid color: " + hex);
	}

	public static @NonNull Color fill(final int color) {
		return new Color(color, color, color);
	}

	public static @NonNull Color fill(final float color) {
		return new Color(color, color, color);
	}

	public static @NonNull Color transition(final @NonNull Color color1, final @NonNull Color color2, final float progress) {
		if (progress == 0) {
			return color1;
		}
		if (progress == 1) {
			return color2;
		}

		final float inv = 1F - progress;

		if (color1.isGradient() && color2.isGradient()) {
			final ColorGradient g1 = color1.gradient;
			final ColorGradient g2 = color2.gradient;
			final Color start = new Color(
					g1.getStartColor().r * inv + g2.getStartColor().r * progress,
					g1.getStartColor().g * inv + g2.getStartColor().g * progress,
					g1.getStartColor().b * inv + g2.getStartColor().b * progress,
					g1.getStartColor().a * inv + g2.getStartColor().a * progress
					);
			final Color end = new Color(
					g1.getEndColor().r * inv + g2.getEndColor().r * progress,
					g1.getEndColor().g * inv + g2.getEndColor().g * progress,
					g1.getEndColor().b * inv + g2.getEndColor().b * progress,
					g1.getEndColor().a * inv + g2.getEndColor().a * progress
					);
			final Vector4f direction = new Vector4f(
					g1.getDirection().x * inv + g2.getDirection().x * progress,
					g1.getDirection().y * inv + g2.getDirection().y * progress,
					g1.getDirection().z * inv + g2.getDirection().z * progress,
					g1.getDirection().w * inv + g2.getDirection().w * progress
					);
			return new Color(new ColorGradient(start, end, direction));
		}

		if (color1.isGradient()) {
			final ColorGradient g = color1.gradient;
			final Color start = new Color(g.getStartColor().r * inv + color2.r * progress, g.getStartColor().g * inv + color2.g * progress, g.getStartColor().b * inv + color2.b * progress, g.getStartColor().a * inv + color2.a * progress);
			final Color end = new Color(g.getEndColor().r * inv + color2.r * progress, g.getEndColor().g * inv + color2.g * progress, g.getEndColor().b * inv + color2.b * progress, g.getEndColor().a * inv + color2.a * progress);
			return new Color(new ColorGradient(start, end, g.getDirection()));
		}

		if (color2.isGradient()) {
			final ColorGradient g = color2.gradient;
			final Color start = new Color(color1.r * inv + g.getStartColor().r * progress, color1.g * inv + g.getStartColor().g * progress, color1.b * inv + g.getStartColor().b * progress, color1.a * inv + g.getStartColor().a * progress);
			final Color end = new Color(color1.r * inv + g.getEndColor().r * progress, color1.g * inv + g.getEndColor().g * progress, color1.b * inv + g.getEndColor().b * progress, color1.a * inv + g.getEndColor().a * progress);
			return new Color(new ColorGradient(start, end, g.getDirection()));
		}

		return new Color(
				color1.r * inv + color2.r * progress,
				color1.g * inv + color2.g * progress,
				color1.b * inv + color2.b * progress,
				color1.a * inv + color2.a * progress,
				progress > 0.5F ? color2.update : color1.update
				);
	}

	public static @NonNull Color gradient(final @NonNull Color color1, final @NonNull Color color2, final @NonNull Vector4f direction) {
		return new Color(new ColorGradient(color1, color2, direction));
	}

	public static @NonNull Color RAINBOW() {
		return new Color(java.awt.Color.HSBtoRGB(BridgeHandler.CLOCK.get().currentTimeMillis() % 3000L / 3000F, 0.8F, 0.8F));
	}

	public static @NonNull Color RAINBOW(final long time) {
		return new Color(java.awt.Color.HSBtoRGB(time % 3000L / 3000F, 0.8F, 0.8F));
	}

	public static @NonNull Color LOADING() {
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		final float color = (float) ((Math.sin(2 * Math.PI * (now % 4000) / 2000) + 1) / 50F) + 0.15F;
		return new Color(color, color, color);
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

	public void bind() {
		final Color color = this.update();
		BridgeHandler.RENDER.get().getState().color(color.r, color.g, color.b, color.a);
	}

	public void bind(final @NonNull Runnable runnable, final @NonNull Vector4f canvas) {
		this.bind(runnable, canvas, false);
	}

	public void bind(final @NonNull Runnable runnable, final @NonNull Vector4f canvas, final boolean hasTexture) {
		final RenderState state = BridgeHandler.RENDER.get().getState();
		if (this.isGradient()) {
			final IShader previousShader = state.getShader();
			try {
				this.gradient.use(hasTexture, runnable, canvas);
			} finally {
				state.shader(previousShader);
			}
		} else {
			final float red = state.getRed();
			final float green = state.getGreen();
			final float blue = state.getBlue();
			final float alpha = state.getAlpha();
			this.bind();
			try {
				runnable.run();
			} finally {
				state.color(red, green, blue, alpha);
			}
		}
	}

	public int getRGB() {
		return ((int)(this.a * 255) & 0xFF) << 24 | ((int)(this.r * 255) & 0xFF) << 16 | ((int)(this.g * 255) & 0xFF) << 8 | ((int)(this.b * 255) & 0xFF) << 0;
	}

	public int getRed() {
		return (int) (this.r * 255F);
	}

	public int getBlue() {
		return (int) (this.b * 255F);
	}

	public int getAlpha() {
		return (int) (this.a * 255F);
	}

	public int getGreen() {
		return (int) (this.g * 255F);
	}

	public @NonNull Color darker() {
		return this.darker(0.5F);
	}

	public @NonNull Color darker(final float scale) {
		final float factor = 1 - scale;
		return new Color(this.r * factor, this.g * factor, this.b * factor, this.a);
	}

	public @NonNull Color brighter() {
		return this.brighter(0.2F);
	}

	public @NonNull Color brighter(final float scale) {
		final float factor = scale + 1F;
		return new Color(this.r * factor, this.g * factor, this.b * factor, this.a);
	}

	public @NonNull Color multiply(final @NonNull Color c) {
		return new Color(this.r * c.r, this.g * c.g, this.b * c.b, this.a * c.a);
	}

	public @NonNull Color addToCopy(final @NonNull Color c) {
		return new Color(this.r + c.r, this.g + c.g, this.b + c.b, this.a + c.a, this.update);
	}

	public @NonNull Color scaleCopy(final float value) {
		return new Color(this.r * value, this.g * value, this.b * value, this.a * value, this.update);
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

	public @NonNull float[] RGBtoHSB(final float[] hsbvals) {
		final float[] values = hsbvals == null ? new float[3] : hsbvals;
		float hue, saturation, brightness;

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
		values[0] = hue;
		values[1] = saturation;
		values[2] = brightness;
		return values;
	}

	public static @NonNull float[] RGBtoHSB(final int r, final int g, final int b, final float[] hsbvals) {
		final float[] values = hsbvals == null ? new float[3] : hsbvals;
		float hue, saturation, brightness;

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
		values[0] = hue;
		values[1] = saturation;
		values[2] = brightness;
		return values;
	}

	public @NonNull Color copy() {
		if (this.isGradient()) {
			return new Color(this.r, this.g, this.b, this.a, this.update, new ColorGradient(this.gradient.getStartColor().copy(), this.gradient.getEndColor().copy(), new Vector4f(this.gradient.getDirection())));
		}
		return new Color(this.r, this.g, this.b, this.a, this.update);
	}

	public @NonNull Color copyAlpha(final float alpha) {
		if (this.isGradient()) {
			final Color start = this.gradient.getStartColor();
			final Color end = this.gradient.getEndColor();
			final float scale = this.a > 0F ? alpha / this.a : 0F;
			return new Color(this.r, this.g, this.b, alpha, this.update, new ColorGradient(start.copyAlpha(this.a > 0F ? start.a * scale : alpha), end.copyAlpha(this.a > 0F ? end.a * scale : alpha), new Vector4f(this.gradient.getDirection())));
		}
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
		return this.update == null ? this : this.update.apply(this);
	}

	public boolean isGradient() {
		return this.gradient != null;
	}

	public static void reset() {
		BridgeHandler.RENDER.get().getState().color(1F, 1F, 1F, 1F);
	}

	private @NonNull String padZero(final @NonNull String str) {
		return str.length() == 1 ? "0" + str : str;
	}

	private static float clamp(final float value) {
		return Math.max(0F, Math.min(1F, value));
	}

	private static @NonNull List<String> arguments(final @NonNull String text, final int start, final int... counts) {
		if (!text.endsWith(")")) {
			throw new NumberFormatException("Invalid color: " + text);
		}

		final List<String> arguments = new ArrayList<>();
		int depth = 0;
		int from = start;
		for (int i = start; i < text.length() - 1; i++) {
			final char character = text.charAt(i);
			if (character == '(') {
				depth++;
			} else if (character == ')') {
				depth--;
			} else if (character == ',' && depth == 0) {
				arguments.add(text.substring(from, i).trim());
				from = i + 1;
			}
		}
		arguments.add(text.substring(from, text.length() - 1).trim());

		for (final int count : counts) {
			if (arguments.size() == count) {
				return arguments;
			}
		}
		throw new NumberFormatException("Invalid color: " + text);
	}

	@Override
	public @NonNull String toString() {
		return String.format(
				"Color(%d, %d, %d, %d) [%s]",
				this.getRed(), this.getGreen(), this.getBlue(), this.getAlpha(),
				this.encode()
				);
	}

}