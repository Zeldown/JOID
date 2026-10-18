package dev.joid.lib.color;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.state.RenderState;
import dev.joid.lib.utils.geom.BoundingBox;
import dev.joid.lib.utils.geom.Point;
import dev.joid.lib.utils.lock.LockableValue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@EqualsAndHashCode(callSuper = false)
public final class Color extends LockableValue<Color> {

	public static final Color RED         = new Color(1F, 0F, 0F, 1F).constant("RED");
	public static final Color BLUE        = new Color(0F, 0F, 1F, 1F).constant("BLUE");
	public static final Color GRAY        = new Color(0.5F, 0.5F, 0.5F, 1F).constant("GRAY");
	public static final Color CYAN        = new Color(0F, 1F, 1F, 1F).constant("CYAN");
	public static final Color PINK        = new Color(1F, 0.7F, 0.7F, 1F).constant("PINK");
	public static final Color WHITE       = new Color(1F, 1F, 1F, 1F).constant("WHITE");
	public static final Color GREEN       = new Color(0F, 1F, 0F, 1F).constant("GREEN");
	public static final Color BLACK       = new Color(0F, 0F, 0F, 1F).constant("BLACK");
	public static final Color YELLOW      = new Color(1F, 1F, 0F, 1F).constant("YELLOW");
	public static final Color ORANGE      = new Color(1F, 0.5F, 0F, 1F).constant("ORANGE");
	public static final Color MAGENTA     = new Color(1F, 0F, 1F, 1F).constant("MAGENTA");
	public static final Color DARKGRAY    = new Color(0.3F, 0.3F, 0.3F, 1F).constant("DARKGRAY");
	public static final Color LIGHTGRAY   = new Color(0.7F, 0.7F, 0.7F, 1F).constant("LIGHTGRAY");
	public static final Color TRANSPARENT = new Color(0F, 0F, 0F, 0F).constant("TRANSPARENT");

	public static final Color RAINBOW = new Color(1F, 1F, 1F, 1F, color -> Color.RAINBOW().withAlpha(color.a)).constant("RAINBOW");
	public static final Color LOADING = new Color(1F, 1F, 1F, 1F, color -> Color.LOADING().withAlpha(color.a)).constant("LOADING");

	private float r;
	private float g;
	private float b;
	private float a;

	private int                  channels;
	private UnaryOperator<Color> update;

	@Getter
	private ColorGradient gradient;

	@EqualsAndHashCode.Exclude
	private String constant;

	private Color(final Color color) {
		this(color.r, color.g, color.b, color.a, color.update, color.isGradient() ? ColorGradient.of(color.gradient.getStartColor().copy(), color.gradient.getEndColor().copy(), color.gradient.getStartPoint(), color.gradient.getEndPoint()) : null);
		this.channels = color.channels;
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

	public static @NonNull Color rgb(final int value) {
		return new Color((value >> 16 & 0xFF) / 255F, (value >> 8 & 0xFF) / 255F, (value & 0xFF) / 255F, 1F);
	}

	public static @NonNull Color argb(final int value) {
		return new Color((value >> 16 & 0xFF) / 255F, (value >> 8 & 0xFF) / 255F, (value & 0xFF) / 255F, (value >>> 24) / 255F);
	}

	public static @NonNull Color decode(final @NonNull String nm) {
		final String text = nm.trim();
		final String lower = text.toLowerCase(Locale.ROOT);

		if ("rainbow".equals(lower.replace("#", ""))) {
			return Color.RAINBOW.copy();
		}

		if ("loading".equals(lower.replace("#", ""))) {
			return Color.LOADING.copy();
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
			final Point startPoint = parts.size() == 2 ? Point.of(0D, 0D) : Point.of(Double.parseDouble(parts.get(2)), Double.parseDouble(parts.get(3)));
			final Point endPoint = parts.size() == 2 ? Point.of(1D, 0D) : Point.of(Double.parseDouble(parts.get(4)), Double.parseDouble(parts.get(5)));
			return new Color(ColorGradient.of(Color.decode(parts.get(0)), Color.decode(parts.get(1)), startPoint, endPoint));
		}

		final String hex = text.startsWith("#") ? text : "#" + text;
		if (hex.length() == 7) {
			return Color.rgb(Integer.parseUnsignedInt(hex.substring(1), 16));
		}

		if (hex.length() == 9) {
			return Color.argb(Integer.rotateRight(Integer.parseUnsignedInt(hex.substring(1), 16), 8));
		}

		throw new NumberFormatException("The color " + text + " cannot be decoded, write it as #RRGGBB, #RRGGBBAA, rgb(r, g, b), rgba(r, g, b, a) or gradient(from, to)");
	}

	private static @NonNull Color transition(final @NonNull Color color1, final @NonNull Color color2, final float progress) {
		if (progress == 0F) {
			return color1.copy();
		}
		if (progress == 1F) {
			return color2.copy();
		}

		if (color1.update != null || color2.update != null) {
			final Color frame = Color.transition(color1.update(), color2.update(), progress);
			return new Color(frame.r, frame.g, frame.b, frame.a, color -> Color.transition(color1.update(), color2.update(), progress), frame.gradient);
		}

		final float inv = 1F - progress;

		if (color1.isGradient() && color2.isGradient()) {
			final ColorGradient g1 = color1.gradient;
			final ColorGradient g2 = color2.gradient;
			final Color start = Color.lerp(g1.getStartColor(), g2.getStartColor(), progress);
			final Color end = Color.lerp(g1.getEndColor(), g2.getEndColor(), progress);
			final Point startPoint = Point.of(g1.getStartPoint().getX() * inv + g2.getStartPoint().getX() * progress, g1.getStartPoint().getY() * inv + g2.getStartPoint().getY() * progress);
			final Point endPoint = Point.of(g1.getEndPoint().getX() * inv + g2.getEndPoint().getX() * progress, g1.getEndPoint().getY() * inv + g2.getEndPoint().getY() * progress);
			return new Color(ColorGradient.of(start, end, startPoint, endPoint));
		}

		if (color1.isGradient()) {
			final ColorGradient g = color1.gradient;
			final Color start = Color.lerp(g.getStartColor(), color2, progress);
			final Color end = Color.lerp(g.getEndColor(), color2, progress);
			return new Color(ColorGradient.of(start, end, g.getStartPoint(), g.getEndPoint()));
		}

		if (color2.isGradient()) {
			final ColorGradient g = color2.gradient;
			final Color start = Color.lerp(color1, g.getStartColor(), progress);
			final Color end = Color.lerp(color1, g.getEndColor(), progress);
			return new Color(ColorGradient.of(start, end, g.getStartPoint(), g.getEndPoint()));
		}

		return Color.lerp(color1, color2, progress);
	}

	public static @NonNull Color RAINBOW() {
		return Color.RAINBOW(BridgeHandler.CLOCK.get().currentTimeMillis());
	}

	public static @NonNull Color RAINBOW(final long time) {
		return Color.rgb(java.awt.Color.HSBtoRGB(time % 3000L / 3000F, 0.8F, 0.8F));
	}

	public static @NonNull Color LOADING() {
		final long now = BridgeHandler.CLOCK.get().currentTimeMillis();
		final float color = (float) ((Math.sin(2D * Math.PI * (now % 4000L) / 2000D) + 1D) / 50F) + 0.15F;
		return new Color(color, color, color);
	}

	public @NonNull String encode() {
		return String.format(Locale.ROOT, "#%02X%02X%02X%02X", this.getRed(), this.getGreen(), this.getBlue(), this.getAlpha());
	}

	public void bind() {
		final Color color = this.update();
		BridgeHandler.RENDER.get().getState().color(color.r, color.g, color.b, color.a);
	}

	public void bind(final @NonNull Runnable runnable, final @NonNull BoundingBox canvas) {
		this.bind(runnable, canvas, false);
	}

	public void bind(final @NonNull Runnable runnable, final @NonNull BoundingBox canvas, final boolean hasTexture) {
		final RenderState state = BridgeHandler.RENDER.get().getState();
		final Color color = this.update();
		if (color.isGradient()) {
			color.gradient.use(hasTexture, runnable, canvas);
		} else {
			final float red = state.getRed();
			final float green = state.getGreen();
			final float blue = state.getBlue();
			final float alpha = state.getAlpha();
			color.bind();
			try {
				runnable.run();
			} finally {
				state.color(red, green, blue, alpha);
			}
		}
	}

	public int getRGB() {
		return ((int)(this.a * 255F) & 0xFF) << 24 | ((int)(this.r * 255F) & 0xFF) << 16 | ((int)(this.g * 255F) & 0xFF) << 8 | ((int)(this.b * 255F) & 0xFF) << 0;
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

	public float getRedF() {
		return this.r;
	}

	public float getBlueF() {
		return this.b;
	}

	public float getAlphaF() {
		return this.a;
	}

	public float getGreenF() {
		return this.g;
	}

	public @NonNull Color darker() {
		return this.darker(0.5F);
	}

	public @NonNull Color darker(final float scale) {
		final float factor = 1F - scale;
		return this.derive(this.r * factor, this.g * factor, this.b * factor, this.a, color -> color.darker(scale));
	}

	public @NonNull Color brighter() {
		return this.brighter(0.2F);
	}

	public @NonNull Color brighter(final float scale) {
		final float factor = scale + 1F;
		return this.derive(this.r * factor, this.g * factor, this.b * factor, this.a, color -> color.brighter(scale));
	}

	public @NonNull Color multiply(final @NonNull Color c) {
		return this.derive(this.r * c.r, this.g * c.g, this.b * c.b, this.a * c.a, color -> color.multiply(c));
	}

	public @NonNull Color add(final @NonNull Color c) {
		return this.derive(this.r + c.r, this.g + c.g, this.b + c.b, this.a + c.a, color -> color.add(c));
	}

	public @NonNull Color scale(final float value) {
		return this.derive(this.r * value, this.g * value, this.b * value, this.a * value, color -> color.scale(value));
	}

	public @NonNull Color to(final @NonNull Color target, final float progress) {
		return Color.transition(this, target, progress);
	}

	public @NonNull Color toGradient(final @NonNull Color target) {
		return this.toGradient(target, Point.of(0D, 0D), Point.of(1D, 0D));
	}

	public @NonNull Color toGradient(final @NonNull Color target, final @NonNull Point startPoint, final @NonNull Point endPoint) {
		return new Color(ColorGradient.of(this, target, startPoint, endPoint));
	}

	public @NonNull Color red(final float red) {
		return super.mutate("withRed", () -> this.assign(this.withRed(red), 1));
	}

	public @NonNull Color blue(final float blue) {
		return super.mutate("withBlue", () -> this.assign(this.withBlue(blue), 4));
	}

	public @NonNull Color alpha(final float alpha) {
		return super.mutate("withAlpha", () -> this.assign(this.withAlpha(alpha), 8));
	}

	public @NonNull Color green(final float green) {
		return super.mutate("withGreen", () -> this.assign(this.withGreen(green), 2));
	}

	public @NonNull Color withRed(final float red) {
		return this.derive(red, this.g, this.b, this.a, color -> color.withRed(red));
	}

	public @NonNull Color withBlue(final float blue) {
		return this.derive(this.r, this.g, blue, this.a, color -> color.withBlue(blue));
	}

	public @NonNull Color withAlpha(final float alpha) {
		if (this.isGradient()) {
			final Color start = this.gradient.getStartColor();
			final Color end = this.gradient.getEndColor();
			final float scale = this.a > 0F ? alpha / this.a : 0F;
			return new Color(this.r, this.g, this.b, alpha, this.follow(color -> color.withAlpha(alpha)), ColorGradient.of(start.withAlpha(this.a > 0F ? start.a * scale : alpha), end.withAlpha(this.a > 0F ? end.a * scale : alpha), this.gradient.getStartPoint(), this.gradient.getEndPoint()));
		}
		return new Color(this.r, this.g, this.b, alpha, this.follow(color -> color.withAlpha(alpha)));
	}

	public @NonNull Color withGreen(final float green) {
		return this.derive(this.r, green, this.b, this.a, color -> color.withGreen(green));
	}

	@Override
	public @NonNull Color copy() {
		return new Color(this);
	}

	public @NonNull Color update() {
		if (this.update == null) {
			return this;
		}

		final Color frame = this.update.apply(this);
		final Color fixed = frame.update == null ? frame : new Color(frame.r, frame.g, frame.b, frame.a, null, frame.gradient);
		final Color red = (this.channels & 1) == 0 ? fixed : fixed.withRed(this.r);
		final Color green = (this.channels & 2) == 0 ? red : red.withGreen(this.g);
		final Color blue = (this.channels & 4) == 0 ? green : green.withBlue(this.b);
		return (this.channels & 8) == 0 ? blue : blue.withAlpha(this.a);
	}

	public boolean isGradient() {
		return this.gradient != null;
	}

	public static void reset() {
		BridgeHandler.RENDER.get().getState().color(1F, 1F, 1F, 1F);
	}

	private @NonNull Color constant(final @NonNull String name) {
		this.constant = name;
		return super.lock();
	}

	private void assign(final @NonNull Color color, final int channel) {
		this.r = color.r;
		this.g = color.g;
		this.b = color.b;
		this.a = color.a;
		this.gradient = color.gradient;
		this.channels |= this.update == null ? 0 : channel;
	}

	private @NonNull Color derive(final float r, final float g, final float b, final float a, final @NonNull UnaryOperator<Color> transform) {
		final ColorGradient gradient = this.isGradient() ? ColorGradient.of(transform.apply(this.gradient.getStartColor()), transform.apply(this.gradient.getEndColor()), this.gradient.getStartPoint(), this.gradient.getEndPoint()) : null;
		return new Color(r, g, b, a, this.follow(transform), gradient);
	}

	private UnaryOperator<Color> follow(final @NonNull UnaryOperator<Color> transform) {
		return this.update == null ? null : color -> transform.apply(this.update());
	}

	private static @NonNull Color lerp(final @NonNull Color from, final @NonNull Color to, final float progress) {
		final float inv = 1F - progress;
		return new Color(from.r * inv + to.r * progress, from.g * inv + to.g * progress, from.b * inv + to.b * progress, from.a * inv + to.a * progress);
	}

	private static float clamp(final float value) {
		return Math.max(0F, Math.min(1F, value));
	}

	private static @NonNull List<String> arguments(final @NonNull String text, final int start, final int... counts) {
		if (!text.endsWith(")")) {
			throw new NumberFormatException("The color " + text + " cannot be decoded, write it as #RRGGBB, #RRGGBBAA, rgb(r, g, b), rgba(r, g, b, a) or gradient(from, to)");
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
		throw new NumberFormatException("The color " + text + " cannot be decoded, write it as #RRGGBB, #RRGGBBAA, rgb(r, g, b), rgba(r, g, b, a) or gradient(from, to)");
	}

	@Override
	protected @NonNull String describeLocked() {
		return this.constant != null ? "Color." + this.constant + " is a constant" : super.describeLocked();
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