package be.zeldown.joid.lib.font.impl.custom;

import java.io.InputStream;

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

	private static final IGLShader      SHADER;
	private static final SamplerUniform MSDF_UNIFORM;
	private static final FloatUniform   DOFFSET_UNIFORM;
	private static final FloatUniform   BLEND_UNIFORM;
	private static final Float2Uniform  TEXEL_UNIFORM;
	private static final Float4Uniform  COLOR_UNIFORM;

	static {
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

	private void draw(final double x, final double y, final @NonNull String text, final @NonNull TextInfo info) {
		if (!CustomFontRenderer.SHADER.isActive()) {
			throw new RuntimeException("FontRenderer shader is not usable");
		}

		Color.reset();
		CustomFontRenderer.SHADER.bind();

		final CustomFont customFont = (CustomFont) info.getFont();
		final Font font = customFont.getFont();

		final Color activeColor = info.getColor().copy();
		final float fontSize = info.getFontSize();
		final float letterSpacing = info.getLetterSpacing();

		this.bindFont(font);
		this.bindColor(activeColor);
		CustomFontRenderer.DOFFSET_UNIFORM.setValue(3.5F / fontSize);

		double currentX = x;
		Color lastColor = null;
		for (int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);
			final Glyph glyph = font.getFontInfo().getGlyphMap().get((int)c);
			if (glyph == null) {
				continue;
			}

			if (c == '[') {
				final int endIndex = text.indexOf(']', i);
				if (endIndex != -1) {
					final String colorCode = text.substring(i + 1, endIndex);
					if ("#reset".equals(colorCode)) {
						if (lastColor != null) {
							activeColor.r = lastColor.r;
							activeColor.g = lastColor.g;
							activeColor.b = lastColor.b;
							activeColor.a = lastColor.a;
							lastColor = null;
						}
						i = endIndex;
						continue;
					}

					try {
						final Color color = Color.decode(colorCode);
						if (color != null) {
							lastColor = activeColor.copy();
							activeColor.r = color.r;
							activeColor.g = color.g;
							activeColor.b = color.b;
							activeColor.a = color.a;
						}
						i = endIndex;
						continue;
					} catch (final NumberFormatException silent) {}
				}
			}

			final PlaneBounds planeBounds = glyph.getPlaneBounds();
			if (planeBounds != null) {
				final Metrics metrics = font.getFontInfo().getMetrics();
				final double baseline = y + (metrics.getLineHeight() + metrics.getDescender()) * fontSize;

				final double width = (planeBounds.getRight() - planeBounds.getLeft()) * fontSize;
				final double height = (planeBounds.getTop() - planeBounds.getBottom()) * fontSize;
				final double hintedY = baseline - planeBounds.getTop() * fontSize;

				activeColor.a = info.getColor().a;
				this.drawGlyph(font, glyph, activeColor, currentX, hintedY, width, height, fontSize, info.isItalic());
			}

			currentX += glyph.getAdvance() * fontSize + letterSpacing;
		}

		CustomFontRenderer.SHADER.unbind();
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

		CustomFontRenderer.COLOR_UNIFORM.setValue(color.r, color.g, color.b, color.a);

		final T9R tess = T9R.inst();
		tess.start(GL11.GL_QUADS);
		tess.vertexUV(x, y + height, 0D, textureLeft, textureBottom);
		tess.vertexUV(x + width, y + height, 0D, textureRight, textureBottom);
		tess.vertexUV(x + width + topOffset, y, 0D, textureRight, textureTop);
		tess.vertexUV(x + topOffset, y, 0D, textureLeft, textureTop);
		tess.draw();
	}

	private void bindFont(final @NonNull Font font) {
		font.getTexture().bindTextureOnly();
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);

		CustomFontRenderer.MSDF_UNIFORM.setValue(font.getTexture().getTextureId());
		CustomFontRenderer.TEXEL_UNIFORM.setValue(1F / font.getFontInfo().getAtlas().getWidth(), 1F / font.getFontInfo().getAtlas().getHeight());
	}

	private void bindColor(final @NonNull Color color) {
		CustomFontRenderer.BLEND_UNIFORM.setValue(Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null)[2]);
	}

	/* [ Provider Section ] */
	@Override
	public @NonNull FontBounds drawText(final double x, final double y, final @NonNull String text, final @NonNull TextInfo info) {
		if (text.isEmpty()) {
			return FontBounds.empty();
		}

		if (info.getShadowColor() != null) {
			this.draw(x + info.getShadowX(), y + info.getShadowY(), text, info.copy().color(info.getShadowColor()));
		}

		this.draw(x, y, text, info);
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
		return ((CustomFont) info.getFont()).getFont().getFontInfo().getMetrics().getLineHeight() * info.getFontSize() + info.getLineHeight();
	}

	private FontBounds getBounds(final @NonNull String text, final @NonNull TextInfo info) {
		final CustomFont customFont = (CustomFont) info.getFont();
		final Font font = customFont.getFont();

		final float fontSize = info.getFontSize();
		final float letterSpacing = info.getLetterSpacing();

		double totalWidth = 0D;
		for (int i = 0; i < text.length(); i++) {
			final char c = text.charAt(i);
			final Glyph glyph = font.getFontInfo().getGlyphMap().get((int) c);
			if (glyph == null) {
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

			totalWidth += glyph.getAdvance() * fontSize + letterSpacing;
		}

		totalWidth -= letterSpacing;
		return new FontBounds(totalWidth, this.getLineHeight(info));
	}

	/* [ Instance Section ] */
	public static CustomFontRenderer inst() {
		return CustomFontRenderer.INSTANCE;
	}

}