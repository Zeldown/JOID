package dev.joid.lib.font.impl.glyph;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.matrix.PixelGrid;
import dev.joid.lib.bridge.render.shader.IShader;
import dev.joid.lib.color.Color;
import dev.joid.lib.color.ColorGradient;
import dev.joid.lib.font.FontBounds;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.ITextRenderer;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.TextStyle;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.markup.ITextMarkup;
import dev.joid.lib.font.markup.TextMarkup;
import lombok.NonNull;

@SuppressWarnings("unchecked")
public abstract class GlyphTextRenderer<F extends IFontFace> implements ITextRenderer {

	private static final Set<Class<?>> WARNED_FONTS = ConcurrentHashMap.newKeySet();

	private double runX;
	private double runY;
	private double runWidth;
	private double runHeight;

	@Override
	public final @NonNull FontBounds drawText(final double x, final double y, final @NonNull String text, final @NonNull TextInfo info) {
		final GlyphLayout<F> layout = this.layout(text, info);
		return this.draw(layout, x, y, info, x, y, layout.getWidth(), this.getLineHeight(info));
	}

	@Override
	public final @NonNull FontBounds drawText(final double x, final double y, final @NonNull String text, final @NonNull TextInfo info, final double runX, final double runY, final double runWidth, final double runHeight) {
		return this.draw(this.layout(text, info), x, y, info, runX, runY, runWidth, runHeight);
	}

	@Override
	public final double getLineHeight(final @NonNull TextInfo info) {
		return info.getLineHeight() > 0F ? info.getLineHeight() * info.getFontSize() : this.getFace(info).getLineHeight() * info.getFontSize();
	}

	@Override
	public final double getWidth(final @NonNull String text, final @NonNull TextInfo info) {
		return this.layout(text, info).getWidth();
	}

	@Override
	public final double getHeight(final @NonNull String text, final @NonNull TextInfo info) {
		return this.getLineHeight(info);
	}

	public final @NonNull GlyphLayout<F> layout(final @NonNull String text, final @NonNull TextInfo info) {
		final GlyphFont<F> font = this.getFont(info);
		final List<ITextMarkup> markups = info.getMarkups();
		final List<GlyphPlacement<F>> placements = new ArrayList<>();
		final TextStyle style = info.getStyle().derive();
		final float size = info.getFontSize();
		final float spacing = info.getLetterSpacing() * size;

		TextStyle snapshot = style.copy();
		GlyphFont<F> current = this.getFont(font, style);
		F face = current.getFace(style.getWeight(), style.isItalic());
		double pen = 0D;
		int previous = -1;
		for (int index = 0; index < text.length();) {
			final int consumed = TextMarkup.parse(markups, text, index, style);
			if (consumed > 0) {
				index += consumed;
				snapshot = style.copy();
				current = this.getFont(font, style);
				final F next = current.getFace(style.getWeight(), style.isItalic());
				if (next != face) {
					face = next;
					previous = -1;
				}
				continue;
			}

			final int codepoint = text.codePointAt(index);
			final int start = index;
			index += Character.charCount(codepoint);
			final boolean drawn = face.hasGlyph(codepoint);
			if (!drawn && codepoint != ' ' && codepoint != '\u00A0') {
				continue;
			}

			if (previous != -1) {
				pen += face.getKerning(previous, codepoint) * size;
			}

			placements.add(new GlyphPlacement<>(start, codepoint, current, face, pen, snapshot));
			pen += (drawn ? face.getAdvance(codepoint) : GlyphTextRenderer.space(face)) * size + spacing;
			previous = codepoint;
		}

		return new GlyphLayout<>(placements, placements.isEmpty() ? 0D : pen - spacing);
	}

	protected abstract void end();
	protected abstract void begin(final double runX, final double runY, final double runWidth, final double runHeight);

	protected abstract void drawGlyph(final @NonNull TextGlyph<F> glyph);

	protected boolean isGridAligned() {
		return false;
	}

	protected final void uniformColor(final @NonNull IShader shader, final @NonNull Color color) {
		final Color current = color.update();
		shader.uniform("color", current.r, current.g, current.b, current.a);
		if (!current.isGradient()) {
			shader.uniform("u_HasGradient", 0);
			return;
		}

		final ColorGradient gradient = current.gradient;
		shader
		.uniform("u_HasGradient", 1)
		.uniform("u_GradientStart", gradient.getStartColor().r, gradient.getStartColor().g, gradient.getStartColor().b, gradient.getStartColor().a)
		.uniform("u_GradientEnd", gradient.getEndColor().r, gradient.getEndColor().g, gradient.getEndColor().b, gradient.getEndColor().a)
		.uniform("u_GradientStartPos", gradient.getDirection().x, gradient.getDirection().y)
		.uniform("u_GradientEndPos", gradient.getDirection().z, gradient.getDirection().w)
		.uniform("u_GradientCanvas", (float) this.runX, (float) this.runY, (float) (this.runX + this.runWidth), (float) (this.runY + this.runHeight));
	}

	private @NonNull F getFace(final @NonNull TextInfo info) {
		return this.getFont(info).getFace(info.getWeight(), info.isItalic());
	}

	private @NonNull GlyphFont<F> getFont(final @NonNull TextInfo info) {
		final IFont font = info.getFont();
		if (!(font instanceof GlyphFont) || font.getTextRenderer().getClass() != this.getClass()) {
			throw new IllegalArgumentException(this.getClass().getName() + " cannot draw the font " + font.getClass().getName() + ", it is drawn by " + font.getTextRenderer().getClass().getName() + ": draw it with info.getFont().getTextRenderer()");
		}

		return (GlyphFont<F>) font;
	}

	private @NonNull GlyphFont<F> getFont(final @NonNull GlyphFont<F> font, final @NonNull TextStyle style) {
		final IFont styleFont = style.getFont();
		if (styleFont == null || styleFont == font) {
			return font;
		}

		if (!(styleFont instanceof GlyphFont) || styleFont.getTextRenderer().getClass() != this.getClass()) {
			if (JOID.inst().isDevMode() && GlyphTextRenderer.WARNED_FONTS.add(styleFont.getClass())) {
				System.err.println("[JOID] The font " + styleFont.getClass().getName() + " of a text style is not drawn by " + this.getClass().getSimpleName() + ", the text keeps its own font");
			}
			return font;
		}

		return (GlyphFont<F>) styleFont;
	}

	private @NonNull FontBounds draw(final @NonNull GlyphLayout<F> layout, final double x, final double y, final @NonNull TextInfo info, final double runX, final double runY, final double runWidth, final double runHeight) {
		if (layout.getPlacements().isEmpty()) {
			return FontBounds.empty();
		}

		final PixelGrid grid = this.isGridAligned() ? BridgeHandler.RENDER.get().getPixelGrid() : null;
		final List<TextGlyph<F>> glyphs = this.glyphs(layout, x, y, info, grid);
		for (final TextGlyph<F> glyph : glyphs) {
			for (final ITextEffect effect : glyph.getStyle().getEffects()) {
				effect.background(glyph);
			}
		}

		final Color shadow = info.getShadowColor();
		final Float tint = info.getShadowTint();
		if (shadow != null || tint != null) {
			final double shadowX = grid == null ? info.getShadowX() : GlyphTextRenderer.shadow(info.getShadowX(), grid.getUnitX(), grid.isAligned());
			final double shadowY = grid == null ? info.getShadowY() : GlyphTextRenderer.shadow(info.getShadowY(), grid.getUnitY(), grid.isAligned());
			final List<TextGlyph<F>> shadows = new ArrayList<>(glyphs.size());
			for (final TextGlyph<F> glyph : glyphs) {
				shadows.add(glyph.shadow(shadowX, shadowY, tint != null ? GlyphTextRenderer.tint(glyph.getColor(), tint) : shadow));
			}
			this.render(shadows, runX + shadowX, runY + shadowY, runWidth, runHeight);
		}

		this.render(glyphs, runX, runY, runWidth, runHeight);
		return new FontBounds(layout.getWidth(), this.getLineHeight(info));
	}

	private @NonNull List<TextGlyph<F>> glyphs(final @NonNull GlyphLayout<F> layout, final double x, final double y, final @NonNull TextInfo info, final PixelGrid grid) {
		final List<GlyphPlacement<F>> placements = layout.getPlacements();
		final List<TextGlyph<F>> glyphs = new ArrayList<>(placements.size());
		final F base = this.getFace(info);
		final float size = info.getFontSize();
		final double exact = y + (base.getLineHeight() + base.getDescender()) * size + (this.getLineHeight(info) - base.getLineHeight() * size) / 2D;
		final double baseline = grid == null ? exact : grid.snapY(exact);
		final double origin = grid == null ? x : grid.snapX(x);
		for (int i = 0; i < placements.size(); i++) {
			final GlyphPlacement<F> placement = placements.get(i);
			final double advance = (i + 1 < placements.size() ? placements.get(i + 1).getX() : layout.getWidth()) - placement.getX();
			final TextGlyph<F> glyph = TextGlyph.create(placement.getFont(), placement.getFace(), placement.getIndex(), placement.getCodepoint(), placement.getStyle(), origin + placement.getX(), baseline, size, advance, GlyphTextRenderer.color(placement.getStyle(), info));
			for (final ITextEffect effect : placement.getStyle().getEffects()) {
				effect.apply(glyph);
			}
			if (grid != null) {
				glyph.offset(grid.quantizeX(glyph.getOffsetX()), grid.quantizeY(glyph.getOffsetY()));
			}
			glyphs.add(glyph);
		}

		return glyphs;
	}

	private void render(final @NonNull List<TextGlyph<F>> glyphs, final double runX, final double runY, final double runWidth, final double runHeight) {
		this.runX = runX;
		this.runY = runY;
		this.runWidth = runWidth;
		this.runHeight = runHeight;
		this.begin(runX, runY, runWidth, runHeight);
		for (final TextGlyph<F> glyph : glyphs) {
			this.drawGlyph(glyph);
		}
		this.end();

		for (final TextGlyph<F> glyph : glyphs) {
			for (final ITextEffect effect : glyph.getStyle().getEffects()) {
				effect.decorate(glyph);
			}
		}
	}

	private static float space(final @NonNull IFontFace face) {
		return face.hasGlyph(' ') ? face.getAdvance(' ') : 0.25F;
	}

	private static double shadow(final double offset, final double unit, final boolean aligned) {
		if (!aligned || offset == 0D) {
			return offset;
		}

		return Math.signum(offset * unit) * Math.max(1D, Math.floor(Math.abs(offset * unit) + 0.5D + 1E-6D)) / unit;
	}

	private static @NonNull Color tint(final @NonNull Color color, final float tint) {
		final Color current = color.update();
		return new Color(current.r * tint, current.g * tint, current.b * tint, current.a);
	}

	private static @NonNull Color color(final @NonNull TextStyle style, final @NonNull TextInfo info) {
		if (!info.isColored()) {
			return info.getColor();
		}

		final Color color = style.getColor();
		return color.a == info.getColor().a ? color : color.copyAlpha(info.getColor().a);
	}

}