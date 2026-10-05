package be.zeldown.joid.lib.font.impl.msdf;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.render.shader.IShader;
import be.zeldown.joid.lib.bridge.render.shader.source.ShaderSource;
import be.zeldown.joid.lib.bridge.render.shader.source.ShaderStage;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float2Uniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.Float4Uniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.FloatUniform;
import be.zeldown.joid.lib.bridge.render.shader.uniform.IntUniform;
import be.zeldown.joid.lib.bridge.render.state.BlendState;
import be.zeldown.joid.lib.bridge.render.texture.TextureWrap;
import be.zeldown.joid.lib.bridge.render.vertex.DrawMode;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.color.ColorGradient;
import be.zeldown.joid.lib.font.IFontProvider;
import be.zeldown.joid.lib.font.dto.FontBounds;
import be.zeldown.joid.lib.font.dto.TextInfo;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfAtlas;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfBounds;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfFace;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfGlyph;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfMetrics;
import be.zeldown.joid.lib.render.tessellator.Tessellator;
import lombok.NonNull;

public class MsdfFontProvider implements IFontProvider {

	private static final MsdfFontProvider INSTANCE = new MsdfFontProvider();

	private static final Map<Integer, Color> COLOR_MAP = new HashMap<>();

	private static final Random RANDOM = new Random();

	private static final float BASELINE_LIFT = 0.025F;

	private static final char CHAR_OPERATOR         = '\u00a7';
	private static final String CHAR_OPERATOR_ATLAS = "0123456789abcdefklmnopr";
	private static final String AZ_ATLAS            = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private static final IShader       SHADER;
	private static final Float2Uniform TEXEL_UNIFORM;
	private static final FloatUniform  PX_RANGE_UNIFORM;
	private static final Float4Uniform COLOR_UNIFORM;

	private static final IntUniform    HAS_GRADIENT_UNIFORM;
	private static final Float4Uniform GRADIENT_START_UNIFORM;
	private static final Float4Uniform GRADIENT_END_UNIFORM;
	private static final Float2Uniform GRADIENT_START_POS_UNIFORM;
	private static final Float2Uniform GRADIENT_END_POS_UNIFORM;
	private static final Float4Uniform GRADIENT_CANVAS_UNIFORM;

	static {
		MsdfFontProvider.COLOR_MAP.put(0, Color.BLACK);
		MsdfFontProvider.COLOR_MAP.put(1, new Color(0, 0, 170));
		MsdfFontProvider.COLOR_MAP.put(2, new Color(0, 170, 0));
		MsdfFontProvider.COLOR_MAP.put(3, new Color(0, 170, 170));
		MsdfFontProvider.COLOR_MAP.put(4, new Color(170, 0, 0));
		MsdfFontProvider.COLOR_MAP.put(5, new Color(170, 0, 170));
		MsdfFontProvider.COLOR_MAP.put(6, new Color(255, 170, 0));
		MsdfFontProvider.COLOR_MAP.put(7, new Color(170, 170, 170));
		MsdfFontProvider.COLOR_MAP.put(8, new Color(85, 85, 85));
		MsdfFontProvider.COLOR_MAP.put(9, new Color(85, 85, 255));
		MsdfFontProvider.COLOR_MAP.put(10, new Color(85, 255, 85));
		MsdfFontProvider.COLOR_MAP.put(11, new Color(85, 255, 255));
		MsdfFontProvider.COLOR_MAP.put(12, new Color(255, 85, 85));
		MsdfFontProvider.COLOR_MAP.put(13, new Color(255, 85, 255));
		MsdfFontProvider.COLOR_MAP.put(14, new Color(255, 255, 85));
		MsdfFontProvider.COLOR_MAP.put(15, new Color(255, 255, 255));

		MsdfFontProvider.COLOR_MAP.put(16, Color.BLACK);
		MsdfFontProvider.COLOR_MAP.put(17, new Color(0, 0, 42));
		MsdfFontProvider.COLOR_MAP.put(18, new Color(0, 42, 0));
		MsdfFontProvider.COLOR_MAP.put(19, new Color(0, 42, 42));
		MsdfFontProvider.COLOR_MAP.put(20, new Color(42, 0, 0));
		MsdfFontProvider.COLOR_MAP.put(21, new Color(42, 0, 42));
		MsdfFontProvider.COLOR_MAP.put(22, new Color(42, 42, 0));
		MsdfFontProvider.COLOR_MAP.put(23, new Color(42, 42, 42));
		MsdfFontProvider.COLOR_MAP.put(24, new Color(21, 21, 21));
		MsdfFontProvider.COLOR_MAP.put(25, new Color(21, 21, 63));
		MsdfFontProvider.COLOR_MAP.put(26, new Color(21, 63, 21));
		MsdfFontProvider.COLOR_MAP.put(27, new Color(21, 63, 63));
		MsdfFontProvider.COLOR_MAP.put(28, new Color(63, 21, 21));
		MsdfFontProvider.COLOR_MAP.put(29, new Color(63, 21, 63));
		MsdfFontProvider.COLOR_MAP.put(30, new Color(63, 63, 21));
		MsdfFontProvider.COLOR_MAP.put(31, new Color(63, 63, 63));

		InputStream vert = null;
		InputStream frag = null;
		try {
			vert = JOID.class.getResourceAsStream("/assets/shaders/font/font.vsh");
			frag = JOID.class.getResourceAsStream("/assets/shaders/font/font.fsh");
		} catch (final Exception e) {
			e.printStackTrace();
		}

		SHADER = BridgeHandler.RENDER.get().createShader(ShaderSource.read(ShaderStage.VERTEX, vert), ShaderSource.read(ShaderStage.FRAGMENT, frag), BlendState.NORMAL);

		TEXEL_UNIFORM    = MsdfFontProvider.SHADER.getFloat2Uniform("texel");
		PX_RANGE_UNIFORM = MsdfFontProvider.SHADER.getFloatUniform("pxRange");
		COLOR_UNIFORM    = MsdfFontProvider.SHADER.getFloat4Uniform("color");

		HAS_GRADIENT_UNIFORM       = MsdfFontProvider.SHADER.getIntUniform("u_HasGradient");
		GRADIENT_START_UNIFORM     = MsdfFontProvider.SHADER.getFloat4Uniform("u_GradientStart");
		GRADIENT_END_UNIFORM       = MsdfFontProvider.SHADER.getFloat4Uniform("u_GradientEnd");
		GRADIENT_START_POS_UNIFORM = MsdfFontProvider.SHADER.getFloat2Uniform("u_GradientStartPos");
		GRADIENT_END_POS_UNIFORM   = MsdfFontProvider.SHADER.getFloat2Uniform("u_GradientEndPos");
		GRADIENT_CANVAS_UNIFORM    = MsdfFontProvider.SHADER.getFloat4Uniform("u_GradientCanvas");
	}

	private void draw(final double x, final double y, final @NonNull String text, final @NonNull TextInfo info, final double runX, final double runY, final double runWidth, final double runHeight) {
		if (!MsdfFontProvider.SHADER.isActive()) {
			throw new RuntimeException("FontRenderer shader is not usable");
		}

		Color.reset();
		MsdfFontProvider.SHADER.bind();

		final MsdfFont font = (MsdfFont) info.getFont();
		final float fontSize = info.getFontSize();
		final float letterSpacing = info.getLetterSpacing();

		final MsdfFace regularFace = font.getRegular();
		final MsdfFace boldFace = font.getBold();

		MsdfFace activeFace = regularFace;
		Color activeColor = info.getColor();

		this.bindFace(activeFace);
		this.bindColor(activeColor);
		this.bindGradient(activeColor, runX, runY, runWidth, runHeight);

		boolean obfuscated = false;
		boolean italic = info.isItalic();

		double currentX = x;
		int previousChar = -1;
		Color lastColor = null;
		for (int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);
			if (c == MsdfFontProvider.CHAR_OPERATOR && i + 1 < text.length()) {
				final int j = MsdfFontProvider.CHAR_OPERATOR_ATLAS.indexOf(text.charAt(i + 1));
				if (j < 16) {
					activeFace = regularFace;

					obfuscated = false;
					italic = false;

					if (j < 0) {
						activeColor = MsdfFontProvider.COLOR_MAP.get(15);
					} else {
						activeColor = MsdfFontProvider.COLOR_MAP.get(j);
					}

					this.bindFace(activeFace);
					this.bindColor(activeColor);
				} else if (j == 16) {
					obfuscated = true;
				} else if (j == 17) {
					activeFace = boldFace;
					this.bindFace(activeFace);
				} else if (j == 20) {
					italic = true;
				} else if (j == 21) {
					activeColor = Color.RAINBOW();
					this.bindColor(activeColor);
				} else {
					activeFace = regularFace;
					activeColor = info.getColor();

					obfuscated = false;
					italic = false;

					this.bindFace(activeFace);
					this.bindColor(activeColor);
				}
				i++;
				continue;
			}

			if (c == '[') {
				final int endIndex = text.indexOf(']', i);
				if (endIndex != -1) {
					final String colorCode = text.substring(i + 1, endIndex);
					if ("#reset".equals(colorCode)) {
						if (lastColor != null) {
							activeColor = lastColor.copy();
						}
						i = endIndex;
						continue;
					}

					try {
						final Color color = Color.decode(colorCode);
						if (color != null) {
							lastColor = activeColor.copy();
							activeColor = color.copy();
						}
						i = endIndex;
						continue;
					} catch (final NumberFormatException silent) {}
				}
			}

			MsdfGlyph glyph = activeFace.getGlyph(c);
			if (glyph == null) {
				continue;
			}

			if (obfuscated && c != ' ') {
				final int tmpChar = MsdfFontProvider.AZ_ATLAS.charAt(MsdfFontProvider.RANDOM.nextInt(MsdfFontProvider.AZ_ATLAS.length()));
				final MsdfGlyph tmpGlyph = activeFace.getGlyph(tmpChar);
				if (tmpGlyph != null && tmpGlyph != glyph) {
					glyph = tmpGlyph;
				}
			}

			if (previousChar != -1) {
				currentX += activeFace.getKerning(previousChar, c) * fontSize;
			}

			final MsdfBounds planeBounds = glyph.getPlaneBounds();
			if (planeBounds != null) {
				final MsdfMetrics metrics = activeFace.getMetrics();
				final double baseline = y + (metrics.getLineHeight() + metrics.getDescender()) * fontSize;

				final double width = (planeBounds.getRight() - planeBounds.getLeft()) * fontSize;
				final double height = (planeBounds.getTop() - planeBounds.getBottom()) * fontSize;
				final double hintedY = baseline - (planeBounds.getTop() + MsdfFontProvider.BASELINE_LIFT) * fontSize;

				if (info.isColored()) {
					activeColor.a = info.getColor().a;
				} else {
					activeColor = info.getColor();
				}

				this.drawGlyph(activeFace, glyph, activeColor, currentX, hintedY, width, height, fontSize, italic);
			}

			currentX += glyph.getAdvance() * fontSize + letterSpacing;
			previousChar = (int) c;
		}

		MsdfFontProvider.SHADER.unbind();
	}

	private void drawGlyph(final @NonNull MsdfFace face, final @NonNull MsdfGlyph glyph, final @NonNull Color color, final double x, final double y, final double width, final double height, final float fontSize, final boolean italic) {
		final MsdfBounds atlasBounds = glyph.getAtlasBounds();
		if (atlasBounds == null) {
			return;
		}

		final MsdfAtlas atlas = face.getAtlas();
		final double textureTop = 1D - (atlasBounds.getTop() - 0.5D) / atlas.getHeight();
		final double textureBottom = 1D - (atlasBounds.getBottom() + 0.5D) / atlas.getHeight();
		final double textureLeft = (atlasBounds.getLeft() + 0.5D) / atlas.getWidth();
		final double textureRight = (atlasBounds.getRight() - 0.5D) / atlas.getWidth();
		final double topOffset = italic ? fontSize / 5D : 0D;

		final double x2 = x + width;
		final double y2 = y + height;
		final double x1Top = x + topOffset;
		final double x2Top = x + width + topOffset;

		MsdfFontProvider.COLOR_UNIFORM.setValue(color.r, color.g, color.b, color.a);

		final Tessellator tess = Tessellator.inst();
		tess.start(DrawMode.QUADS);
		tess.addVertexWithUV(x, y2, 0D, textureLeft, textureBottom);
		tess.addVertexWithUV(x2, y2, 0D, textureRight, textureBottom);
		tess.addVertexWithUV(x2Top, y, 0D, textureRight, textureTop);
		tess.addVertexWithUV(x1Top, y, 0D, textureLeft, textureTop);
		tess.draw();
	}

	private void bindFace(final @NonNull MsdfFace face) {
		face.getTexture().bindTextureOnly(TextureWrap.CLAMP_TO_EDGE);

		MsdfFontProvider.TEXEL_UNIFORM.setValue(1F / face.getAtlas().getWidth(), 1F / face.getAtlas().getHeight());
		MsdfFontProvider.PX_RANGE_UNIFORM.setValue(face.getAtlas().getDistanceRange());
	}

	private void bindGradient(final @NonNull Color color, final double runX, final double runY, final double runWidth, final double runHeight) {
		if (color.isGradient()) {
			final ColorGradient grad = color.gradient;
			MsdfFontProvider.HAS_GRADIENT_UNIFORM.setValue(1);
			MsdfFontProvider.GRADIENT_START_UNIFORM.setValue(grad.getStartColor().r, grad.getStartColor().g, grad.getStartColor().b, grad.getStartColor().a);
			MsdfFontProvider.GRADIENT_END_UNIFORM.setValue(grad.getEndColor().r, grad.getEndColor().g, grad.getEndColor().b, grad.getEndColor().a);
			MsdfFontProvider.GRADIENT_START_POS_UNIFORM.setValue(grad.getDirection().x, grad.getDirection().y);
			MsdfFontProvider.GRADIENT_END_POS_UNIFORM.setValue(grad.getDirection().z, grad.getDirection().w);
			MsdfFontProvider.GRADIENT_CANVAS_UNIFORM.setValue((float) runX, (float) runY, (float) (runX + runWidth), (float) (runY + runHeight));
		} else {
			MsdfFontProvider.HAS_GRADIENT_UNIFORM.setValue(0);
		}
	}

	private void bindColor(final @NonNull Color color) {
		color.update();
	}

	@Override
	public @NonNull FontBounds drawText(final double x, final double y, final @NonNull String text, final @NonNull TextInfo info) {
		final FontBounds bounds = this.getBounds(text, info);
		return this.drawText(x, y, text, info, x, y, bounds.getWidth(), bounds.getHeight());
	}

	@Override
	public @NonNull FontBounds drawText(final double x, final double y, final @NonNull String text, final @NonNull TextInfo info, final double runX, final double runY, final double runWidth, final double runHeight) {
		if (text.isEmpty()) {
			return FontBounds.empty();
		}

		if (info.getShadowColor() != null) {
			this.draw(x + info.getShadowX(), y + info.getShadowY(), text, info.copy().color(info.getShadowColor()).colored(false), runX + info.getShadowX(), runY + info.getShadowY(), runWidth, runHeight);
		}

		this.draw(x, y, text, info, runX, runY, runWidth, runHeight);
		return this.getBounds(text, info);
	}

	@Override
	public double getWidth(final @NonNull String text, final @NonNull TextInfo info) {
		return this.getBounds(text, info).getWidth();
	}

	@Override
	public double getHeight(final @NonNull String text, final @NonNull TextInfo info) {
		return this.getBounds(text, info).getHeight();
	}

	@Override
	public double getLineHeight(final @NonNull TextInfo info) {
		return ((MsdfFont) info.getFont()).getBold().getMetrics().getLineHeight() * info.getFontSize() + info.getLineHeight();
	}

	private FontBounds getBounds(final @NonNull String text, final @NonNull TextInfo info) {
		final MsdfFont font = (MsdfFont) info.getFont();
		final float fontSize = info.getFontSize();
		final float letterSpacing = info.getLetterSpacing();

		MsdfFace activeFace = font.getRegular();
		double totalWidth = 0D;
		int previousChar = -1;

		for (int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);
			if (c == MsdfFontProvider.CHAR_OPERATOR && i + 1 < text.length()) {
				if (text.charAt(i + 1) == 'l') {
					activeFace = font.getBold();
				} else {
					activeFace = font.getRegular();
				}

				i++;
				continue;
			}

			if (c == '[') {
				final int endIndex = text.indexOf(']', i);
				if (endIndex != -1) {
					final String colorCode = text.substring(i + 1, endIndex);
					if ("#reset".equals(colorCode)) {
						i = endIndex;
						continue;
					}

					try {
						Color.decode(colorCode);
						i = endIndex;
						continue;
					} catch (final NumberFormatException silent) {}
				}
			}

			final MsdfGlyph glyph = activeFace.getGlyph(c);
			if (glyph == null) {
				continue;
			}

			if (previousChar != -1) {
				totalWidth += activeFace.getKerning(previousChar, c) * fontSize;
			}
			totalWidth += glyph.getAdvance() * fontSize + letterSpacing;
			previousChar = (int) c;
		}

		totalWidth -= letterSpacing;
		return new FontBounds(totalWidth, this.getLineHeight(info));
	}

	public static @NonNull MsdfFontProvider inst() {
		return MsdfFontProvider.INSTANCE;
	}

}