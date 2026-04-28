package be.zeldown.joid.lib.font.impl.custom;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.color.ColorGradient;
import be.zeldown.joid.lib.font.FontProvider;
import be.zeldown.joid.lib.font.dto.atlas.Atlas;
import be.zeldown.joid.lib.font.dto.atlas.AtlasBounds;
import be.zeldown.joid.lib.font.dto.data.Glyph;
import be.zeldown.joid.lib.font.dto.data.Metrics;
import be.zeldown.joid.lib.font.dto.data.PlaneBounds;
import be.zeldown.joid.lib.font.dto.font.Font;
import be.zeldown.joid.lib.font.dto.font.FontBounds;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.shader.GLShader;
import be.zeldown.joid.lib.shader.IGLShader;
import be.zeldown.joid.lib.shader.blend.ShaderBlendState;
import be.zeldown.joid.lib.shader.uniform.Float2Uniform;
import be.zeldown.joid.lib.shader.uniform.Float4Uniform;
import be.zeldown.joid.lib.shader.uniform.FloatUniform;
import be.zeldown.joid.lib.shader.uniform.IntUniform;
import be.zeldown.joid.lib.shader.uniform.SamplerUniform;
import be.zeldown.joid.lib.tessellator.T9R;
import lombok.NonNull;

public class CustomFontProvider implements FontProvider {

	private static final CustomFontProvider INSTANCE = new CustomFontProvider();

	private static final Map<Integer, Color> COLOR_MAP = new HashMap<>();

	private static final Random RANDOM = new Random();

	private static final char CHAR_OPERATOR         = '\u00a7'; /* § */
	private static final String CHAR_OPERATOR_ATLAS = "0123456789abcdefklmnopr";
	private static final String AZ_ATLAS            = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private static final IGLShader      SHADER;
	private static final SamplerUniform MSDF_UNIFORM;
	private static final FloatUniform   DOFFSET_UNIFORM;
	private static final FloatUniform   BLEND_UNIFORM;
	private static final Float2Uniform  TEXEL_UNIFORM;
	private static final Float4Uniform  COLOR_UNIFORM;

	private static final IntUniform     HAS_GRADIENT_UNIFORM;
	private static final Float4Uniform  GRADIENT_START_UNIFORM;
	private static final Float4Uniform  GRADIENT_END_UNIFORM;
	private static final Float2Uniform  GRADIENT_START_POS_UNIFORM;
	private static final Float2Uniform  GRADIENT_END_POS_UNIFORM;
	private static final Float4Uniform  GRADIENT_CANVAS_UNIFORM;

	static {
		CustomFontProvider.COLOR_MAP.put(0, Color.BLACK);
		CustomFontProvider.COLOR_MAP.put(1, new Color(0, 0, 170));
		CustomFontProvider.COLOR_MAP.put(2, new Color(0, 170, 0));
		CustomFontProvider.COLOR_MAP.put(3, new Color(0, 170, 170));
		CustomFontProvider.COLOR_MAP.put(4, new Color(170, 0, 0));
		CustomFontProvider.COLOR_MAP.put(5, new Color(170, 0, 170));
		CustomFontProvider.COLOR_MAP.put(6, new Color(255, 170, 0));
		CustomFontProvider.COLOR_MAP.put(7, new Color(170, 170, 170));
		CustomFontProvider.COLOR_MAP.put(8, new Color(85, 85, 85));
		CustomFontProvider.COLOR_MAP.put(9, new Color(85, 85, 255));
		CustomFontProvider.COLOR_MAP.put(10, new Color(85, 255, 85));
		CustomFontProvider.COLOR_MAP.put(11, new Color(85, 255, 255));
		CustomFontProvider.COLOR_MAP.put(12, new Color(255, 85, 85));
		CustomFontProvider.COLOR_MAP.put(13, new Color(255, 85, 255));
		CustomFontProvider.COLOR_MAP.put(14, new Color(255, 255, 85));
		CustomFontProvider.COLOR_MAP.put(15, new Color(255, 255, 255));

		CustomFontProvider.COLOR_MAP.put(16, Color.BLACK);
		CustomFontProvider.COLOR_MAP.put(17, new Color(0, 0, 42));
		CustomFontProvider.COLOR_MAP.put(18, new Color(0, 42, 0));
		CustomFontProvider.COLOR_MAP.put(19, new Color(0, 42, 42));
		CustomFontProvider.COLOR_MAP.put(20, new Color(42, 0, 0));
		CustomFontProvider.COLOR_MAP.put(21, new Color(42, 0, 42));
		CustomFontProvider.COLOR_MAP.put(22, new Color(42, 42, 0));
		CustomFontProvider.COLOR_MAP.put(23, new Color(42, 42, 42));
		CustomFontProvider.COLOR_MAP.put(24, new Color(21, 21, 21));
		CustomFontProvider.COLOR_MAP.put(25, new Color(21, 21, 63));
		CustomFontProvider.COLOR_MAP.put(26, new Color(21, 63, 21));
		CustomFontProvider.COLOR_MAP.put(27, new Color(21, 63, 63));
		CustomFontProvider.COLOR_MAP.put(28, new Color(63, 21, 21));
		CustomFontProvider.COLOR_MAP.put(29, new Color(63, 21, 63));
		CustomFontProvider.COLOR_MAP.put(30, new Color(63, 63, 21));
		CustomFontProvider.COLOR_MAP.put(31, new Color(63, 63, 63));

		InputStream vert = null;
		InputStream frag = null;
		try {
			vert = JOID.class.getResourceAsStream("/assets/shaders/font/font.vsh");
			frag = JOID.class.getResourceAsStream("/assets/shaders/font/font.fsh");
		} catch (final Exception e) {
			e.printStackTrace();
		}

		SHADER = GLShader.from(vert, frag, ShaderBlendState.NORMAL);

		MSDF_UNIFORM    = CustomFontProvider.SHADER.getSamplerUniform("msdf");
		DOFFSET_UNIFORM = CustomFontProvider.SHADER.getFloatUniform("doffset");
		BLEND_UNIFORM   = CustomFontProvider.SHADER.getFloatUniform("blend");
		COLOR_UNIFORM   = CustomFontProvider.SHADER.getFloat4Uniform("color");
		TEXEL_UNIFORM   = CustomFontProvider.SHADER.getFloat2Uniform("texel");

		HAS_GRADIENT_UNIFORM       = CustomFontProvider.SHADER.getIntUniform("u_HasGradient");
		GRADIENT_START_UNIFORM     = CustomFontProvider.SHADER.getFloat4Uniform("u_GradientStart");
		GRADIENT_END_UNIFORM       = CustomFontProvider.SHADER.getFloat4Uniform("u_GradientEnd");
		GRADIENT_START_POS_UNIFORM = CustomFontProvider.SHADER.getFloat2Uniform("u_GradientStartPos");
		GRADIENT_END_POS_UNIFORM   = CustomFontProvider.SHADER.getFloat2Uniform("u_GradientEndPos");
		GRADIENT_CANVAS_UNIFORM    = CustomFontProvider.SHADER.getFloat4Uniform("u_GradientCanvas");
	}

	private void draw(final double x, final double y, final @NonNull String text, final @NonNull TextInfo info, final double runX, final double runY, final double runWidth, final double runHeight) {
		if (!CustomFontProvider.SHADER.isActive()) {
			throw new RuntimeException("FontRenderer shader is not usable");
		}

		Color.reset();
		CustomFontProvider.SHADER.bind();

		final CustomFont font = (CustomFont) info.getFont();
		final float fontSize = info.getFontSize();
		final float letterSpacing = info.getLetterSpacing();

		final Font regularFont = font.getRegular();
		final Font boldFont = font.getBold();

		Font activeFont = regularFont;
		Color activeColor = info.getColor();

		this.bindFont(activeFont);
		this.bindColor(activeColor);
		this.bindGradient(activeColor, runX, runY, runWidth, runHeight);

		CustomFontProvider.DOFFSET_UNIFORM.setValue(3.5F / fontSize);

		boolean obfuscated = false;
		boolean italic = info.isItalic();

		double currentX = x;
		Color lastColor = null;
		for (int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);
			if (c == CustomFontProvider.CHAR_OPERATOR && i + 1 < text.length()) {
				final int j = CustomFontProvider.CHAR_OPERATOR_ATLAS.indexOf(text.charAt(i + 1));
				if (j < 16) {
					activeFont = regularFont;

					obfuscated = false;
					italic = false;

					if (j < 0) {
						activeColor = CustomFontProvider.COLOR_MAP.get(15);
					} else {
						activeColor = CustomFontProvider.COLOR_MAP.get(j);
					}

					this.bindFont(activeFont);
					this.bindColor(activeColor);
				} else if (j == 16) {
					obfuscated = true;
				} else if (j == 17) {
					activeFont = boldFont;
					this.bindFont(activeFont);
				} else if (j == 20) {
					italic = true;
				} else if (j == 21) {
					activeColor = Color.RAINBOW();
					this.bindColor(activeColor);
				} else {
					activeFont = regularFont;
					activeColor = info.getColor();

					obfuscated = false;
					italic = false;

					this.bindFont(activeFont);
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

			Glyph glyph = activeFont.getFontInfo().getGlyphMap().get((int)c);
			if (glyph == null) {
				continue;
			}

			if (obfuscated && c != ' ') {
				final int tmpChar = CustomFontProvider.AZ_ATLAS.charAt(CustomFontProvider.RANDOM.nextInt(CustomFontProvider.AZ_ATLAS.length()));
				final Glyph tmpGlyph = activeFont.getFontInfo().getGlyphMap().get(tmpChar);
				if (tmpGlyph != null && tmpGlyph != glyph) {
					glyph = tmpGlyph;
				}
			}

			final PlaneBounds planeBounds = glyph.getPlaneBounds();
			if (planeBounds != null) {
				final Metrics metrics =  activeFont.getFontInfo().getMetrics();
				final double baseline = y + (metrics.getLineHeight() + metrics.getDescender()) * fontSize;

				final double width = (planeBounds.getRight() - planeBounds.getLeft()) * fontSize;
				final double height = (planeBounds.getTop() - planeBounds.getBottom()) * fontSize;
				final double hintedY = baseline - planeBounds.getTop() * fontSize;

				if (info.isColored()) {
					activeColor.a = info.getColor().a;
				} else {
					activeColor = info.getColor();
				}

				this.drawGlyph(activeFont, glyph, activeColor, currentX, hintedY, width, height, fontSize, italic);
			}

			currentX += glyph.getAdvance() * fontSize + letterSpacing;
		}

		CustomFontProvider.SHADER.unbind();
	}

	private void drawGlyph(final @NonNull Font font, final @NonNull Glyph glyph, final @NonNull Color color, final double x, final double y, final double width, final double height, final float fontSize, final boolean italic) {
		final AtlasBounds atlasBounds = glyph.getAtlasBounds();
		if (atlasBounds == null) {
			return;
		}

		final Atlas atlas = font.getFontInfo().getAtlas();
		final double textureTop = 1D - atlasBounds.getTop() / atlas.getHeight();
		final double textureBottom = 1D - atlasBounds.getBottom() / atlas.getHeight();
		final double textureLeft = atlasBounds.getLeft() / atlas.getWidth();
		final double textureRight = atlasBounds.getRight() / atlas.getWidth();
		final double topOffset = italic ? fontSize / 5D : 0D;

		CustomFontProvider.COLOR_UNIFORM.setValue(color.r, color.g, color.b, color.a);

		final T9R tess = T9R.inst();
		tess.start(GL11.GL_QUADS);
		tess.addVertexWithUV(x, y + height, 0D, textureLeft, textureBottom);
		tess.addVertexWithUV(x + width, y + height, 0D, textureRight, textureBottom);
		tess.addVertexWithUV(x + width + topOffset, y, 0D, textureRight, textureTop);
		tess.addVertexWithUV(x + topOffset, y, 0D, textureLeft, textureTop);
		tess.draw();
	}

	private void bindFont(final @NonNull Font font) {
		font.getTexture().bindTextureOnly();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

		CustomFontProvider.MSDF_UNIFORM.setValue(font.getTexture().getTextureId());
		CustomFontProvider.TEXEL_UNIFORM.setValue(1F / font.getFontInfo().getAtlas().getWidth(), 1F / font.getFontInfo().getAtlas().getHeight());
	}

	private void bindGradient(final @NonNull Color color, final double runX, final double runY, final double runWidth, final double runHeight) {
		if (color.isGradient()) {
			final ColorGradient grad = color.gradient;
			CustomFontProvider.HAS_GRADIENT_UNIFORM.setValue(1);
			CustomFontProvider.GRADIENT_START_UNIFORM.setValue(grad.getStartColor().r, grad.getStartColor().g, grad.getStartColor().b, grad.getStartColor().a);
			CustomFontProvider.GRADIENT_END_UNIFORM.setValue(grad.getEndColor().r, grad.getEndColor().g, grad.getEndColor().b, grad.getEndColor().a);
			CustomFontProvider.GRADIENT_START_POS_UNIFORM.setValue(grad.getDirection().x, grad.getDirection().y);
			CustomFontProvider.GRADIENT_END_POS_UNIFORM.setValue(grad.getDirection().z, grad.getDirection().w);
			CustomFontProvider.GRADIENT_CANVAS_UNIFORM.setValue((float) runX, (float) runY, (float) (runX + runWidth), (float) (runY + runHeight));
		} else {
			CustomFontProvider.HAS_GRADIENT_UNIFORM.setValue(0);
		}
	}

	private void bindColor(final @NonNull Color color) {
		color.update();
		CustomFontProvider.BLEND_UNIFORM.setValue(Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[2]);
	}

	/* [ Provider Section ] */
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
		return ((CustomFont) info.getFont()).getBold().getFontInfo().getMetrics().getLineHeight() * info.getFontSize() + info.getLineHeight();
	}

	private FontBounds getBounds(final @NonNull String text, final @NonNull TextInfo info) {
		final CustomFont font = (CustomFont) info.getFont();
		final float fontSize = info.getFontSize();
		final float letterSpacing = info.getLetterSpacing();

		Font activeFont = font.getRegular();
		double totalWidth = 0D;

		for (int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);
			if (c == CustomFontProvider.CHAR_OPERATOR && i + 1 < text.length()) {
				if (text.charAt(i + 1) == 'l') {
					activeFont = font.getBold();
				} else {
					activeFont = font.getRegular();
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

			final Glyph glyph = activeFont.getFontInfo().getGlyphMap().get((int) c);
			if (glyph == null) {
				i++;
				continue;
			}

			totalWidth += glyph.getAdvance() * fontSize + letterSpacing;
		}

		totalWidth -= letterSpacing;
		return new FontBounds(totalWidth, this.getLineHeight(info));
	}

	/* [ Instance Section ] */
	public static CustomFontProvider inst() {
		return CustomFontProvider.INSTANCE;
	}

}