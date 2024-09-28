package be.zeldown.joid.lib.font.impl.custom;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.font.FontProvider;
import be.zeldown.joid.lib.font.dto.atlas.Atlas;
import be.zeldown.joid.lib.font.dto.atlas.AtlasBounds;
import be.zeldown.joid.lib.font.dto.data.Glyph;
import be.zeldown.joid.lib.font.dto.data.Metrics;
import be.zeldown.joid.lib.font.dto.data.PlaneBounds;
import be.zeldown.joid.lib.font.dto.font.Font;
import be.zeldown.joid.lib.font.dto.font.FontBounds;
import be.zeldown.joid.lib.font.dto.font.impl.CustomFont;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.shader.GLShader;
import be.zeldown.joid.lib.shader.IGLShader;
import be.zeldown.joid.lib.shader.blend.ShaderBlendState;
import be.zeldown.joid.lib.shader.uniform.Float2Uniform;
import be.zeldown.joid.lib.shader.uniform.Float4Uniform;
import be.zeldown.joid.lib.shader.uniform.FloatUniform;
import be.zeldown.joid.lib.shader.uniform.SamplerUniform;
import be.zeldown.joid.lib.tessellator.T9R;
import lombok.NonNull;

public class CustomFontRenderer implements FontProvider {

	private static final CustomFontRenderer INSTANCE = new CustomFontRenderer();

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

	static {
		CustomFontRenderer.COLOR_MAP.put(0, Color.BLACK);
		CustomFontRenderer.COLOR_MAP.put(1, new Color(0, 0, 170));
		CustomFontRenderer.COLOR_MAP.put(2, new Color(0, 170, 0));
		CustomFontRenderer.COLOR_MAP.put(3, new Color(0, 170, 170));
		CustomFontRenderer.COLOR_MAP.put(4, new Color(170, 0, 0));
		CustomFontRenderer.COLOR_MAP.put(5, new Color(170, 0, 170));
		CustomFontRenderer.COLOR_MAP.put(6, new Color(255, 170, 0));
		CustomFontRenderer.COLOR_MAP.put(7, new Color(170, 170, 170));
		CustomFontRenderer.COLOR_MAP.put(8, new Color(85, 85, 85));
		CustomFontRenderer.COLOR_MAP.put(9, new Color(85, 85, 255));
		CustomFontRenderer.COLOR_MAP.put(10, new Color(85, 255, 85));
		CustomFontRenderer.COLOR_MAP.put(11, new Color(85, 255, 255));
		CustomFontRenderer.COLOR_MAP.put(12, new Color(255, 85, 85));
		CustomFontRenderer.COLOR_MAP.put(13, new Color(255, 85, 255));
		CustomFontRenderer.COLOR_MAP.put(14, new Color(255, 255, 85));
		CustomFontRenderer.COLOR_MAP.put(15, new Color(255, 255, 255));

		CustomFontRenderer.COLOR_MAP.put(16, Color.BLACK);
		CustomFontRenderer.COLOR_MAP.put(17, new Color(0, 0, 42));
		CustomFontRenderer.COLOR_MAP.put(18, new Color(0, 42, 0));
		CustomFontRenderer.COLOR_MAP.put(19, new Color(0, 42, 42));
		CustomFontRenderer.COLOR_MAP.put(20, new Color(42, 0, 0));
		CustomFontRenderer.COLOR_MAP.put(21, new Color(42, 0, 42));
		CustomFontRenderer.COLOR_MAP.put(22, new Color(42, 42, 0));
		CustomFontRenderer.COLOR_MAP.put(23, new Color(42, 42, 42));
		CustomFontRenderer.COLOR_MAP.put(24, new Color(21, 21, 21));
		CustomFontRenderer.COLOR_MAP.put(25, new Color(21, 21, 63));
		CustomFontRenderer.COLOR_MAP.put(26, new Color(21, 63, 21));
		CustomFontRenderer.COLOR_MAP.put(27, new Color(21, 63, 63));
		CustomFontRenderer.COLOR_MAP.put(28, new Color(63, 21, 21));
		CustomFontRenderer.COLOR_MAP.put(29, new Color(63, 21, 63));
		CustomFontRenderer.COLOR_MAP.put(30, new Color(63, 63, 21));
		CustomFontRenderer.COLOR_MAP.put(31, new Color(63, 63, 63));

		InputStream vert = null;
		InputStream frag = null;
		try {
			vert = JOID.class.getResourceAsStream("/assets/shaders/font/font.vsh");
			frag = JOID.class.getResourceAsStream("/assets/shaders/font/font.fsh");
		} catch (final Exception e) {
			throw new RuntimeException("Failed to load font shaders", e);
		}

		SHADER = GLShader.from(vert, frag, ShaderBlendState.NORMAL);

		MSDF_UNIFORM    = CustomFontRenderer.SHADER.getSamplerUniform("msdf");
		DOFFSET_UNIFORM = CustomFontRenderer.SHADER.getFloatUniform("doffset");
		BLEND_UNIFORM   = CustomFontRenderer.SHADER.getFloatUniform("blend");
		COLOR_UNIFORM   = CustomFontRenderer.SHADER.getFloat4Uniform("color");
		TEXEL_UNIFORM   = CustomFontRenderer.SHADER.getFloat2Uniform("texel");
	}

	private void draw(final double x, final double y, final String text, final TextInfo info, final boolean colored) {
		if (!CustomFontRenderer.SHADER.isActive()) {
			throw new RuntimeException("FontRenderer shader is not usable");
		}

		Color.reset();
		CustomFontRenderer.SHADER.bind();

		final CustomFont font = (CustomFont) info.getFont();
		final int fontSize = info.getFontSize();
		final float letterSpacing = info.getLetterSpacing();

		final Font regularFont = font.getRegular();
		final Font boldFont = font.getBold();

		Font activeFont = regularFont;
		Color activeColor = info.getColor();

		this.bindFont(activeFont);
		this.bindColor(activeColor);

		CustomFontRenderer.DOFFSET_UNIFORM.setValue(3.5F / fontSize);

		boolean obfuscated = false;
		boolean italic = false;

		double currentX = x;
		for (int i=0;i<text.length();i++) {
			final char c = text.charAt(i);
			if (c == CustomFontRenderer.CHAR_OPERATOR && i + 1 < text.length()) {
				final int j = CustomFontRenderer.CHAR_OPERATOR_ATLAS.indexOf(text.charAt(i + 1));

				if (j < 16) {
					activeFont = regularFont;

					obfuscated = false;
					italic = false;

					if (j < 0) {
						activeColor = CustomFontRenderer.COLOR_MAP.get(15);
					} else {
						activeColor = CustomFontRenderer.COLOR_MAP.get(j);
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

			Glyph glyph = activeFont.getFontInfo().getGlyphMap().get((int)c);
			if (glyph == null) {
				continue;
			}

			if (obfuscated && c != ' ') {
				final int tmpChar = CustomFontRenderer.AZ_ATLAS.charAt(CustomFontRenderer.RANDOM.nextInt(CustomFontRenderer.AZ_ATLAS.length()));
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

				if (colored) {
					activeColor.a = info.getColor().a;
				} else {
					activeColor = info.getColor();
				}
				this.drawGlyph(
						activeFont,
						glyph,
						activeColor,
						currentX,
						hintedY,
						width,
						height,
						fontSize,
						italic
						);
			}

			currentX += glyph.getAdvance() * fontSize + letterSpacing;
		}

		CustomFontRenderer.SHADER.unbind();
	}

	private void drawGlyph(final Font font, final Glyph glyph, final Color color, final double x, final double y, final double width, final double height, final int fontSize, final boolean italic) {
		final AtlasBounds atlasBounds = glyph.getAtlasBounds();
		if (atlasBounds == null) {
			return;
		}

		final Atlas atlas = font.getFontInfo().getAtlas();
		final double textureTop = 1D - atlasBounds.getTop() / atlas.getHeight();
		final double textureBottom = 1D - atlasBounds.getBottom() / atlas.getHeight();
		final double textureLeft = atlasBounds.getLeft() / atlas.getWidth();
		final double textureRight = atlasBounds.getRight() / atlas.getWidth();

		CustomFontRenderer.COLOR_UNIFORM.setValue(
				color.getRed()   / 255F,
				color.getGreen() / 255F,
				color.getBlue()  / 255F,
				color.getAlpha() / 255F
				);

		final double topOffset = italic ? fontSize / 5D : 0D;

		final T9R tess = T9R.inst();
		tess.start(GL11.GL_QUADS);
		tess.addVertexWithUV(x, y + height, 0D, textureLeft, textureBottom);
		tess.addVertexWithUV(x + width, y + height, 0D, textureRight, textureBottom);
		tess.addVertexWithUV(x + width + topOffset, y, 0D, textureRight, textureTop);
		tess.addVertexWithUV(x + topOffset, y, 0D, textureLeft, textureTop);
		tess.draw();
	}

	private void bindFont(final Font font) {
		font.getTexture().bindTextureOnly();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

		CustomFontRenderer.MSDF_UNIFORM.setValue(font.getTexture().getTextureId());
		CustomFontRenderer.TEXEL_UNIFORM.setValue(1F / font.getFontInfo().getAtlas().getWidth(), 1F / font.getFontInfo().getAtlas().getHeight());
	}

	private void bindColor(final Color color) {
		final float amt = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[2];
		CustomFontRenderer.BLEND_UNIFORM.setValue(amt);
	}

	private FontBounds getBounds(final String text, final TextInfo info) {
		final CustomFont font = (CustomFont) info.getFont();
		final int fontSize = info.getFontSize();
		final float letterSpacing = info.getLetterSpacing();

		Font activeFont = font.getRegular();
		double totalWidth = 0D;

		for (int i=0;i<text.length();i++) {
			final char c = text.charAt(i);

			if (c == CustomFontRenderer.CHAR_OPERATOR && i + 1 < text.length()) {
				if (text.charAt(i + 1) == 'l') {
					activeFont = font.getBold();
				} else {
					activeFont = font.getRegular();
				}

				i++;
				continue;
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

	/* [ Provider Section ] */
	@Override
	public FontBounds drawText(final double x, final double y, @NonNull String text, final @NonNull TextInfo info) {
		text = info.modify(text);

		if (info.getFont() == null) {
			return null;
		}

		if (info.getShadowColor() != null) {
			this.draw(x + info.getShadowX(), y + info.getShadowY(), text, info.copy().color(info.getShadowColor()), false);
		}

		this.draw(x, y, text, info, info.isColored());
		return this.getBounds(text, info);
	}

	@Override
	public double getWidth(final @NonNull String text, final @NonNull TextInfo info) {
		return this.getBounds(info.modify(text), info).getWidth();
	}

	@Override
	public double getHeight(final @NonNull String text, final @NonNull TextInfo info) {
		return this.getBounds(info.modify(text), info).getHeight();
	}

	@Override
	public double getLineHeight(final @NonNull TextInfo info) {
		return ((CustomFont)info.getFont()).getBold().getFontInfo().getMetrics().getLineHeight() * info.getFontSize() + info.getLineHeight();
	}

	/* [ Instance Section ] */
	public static CustomFontRenderer inst() {
		return CustomFontRenderer.INSTANCE;
	}

}