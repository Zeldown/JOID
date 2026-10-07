package dev.joid.lib.font.impl.glyph;

import java.util.ArrayList;
import java.util.List;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.IFont;
import dev.joid.lib.font.IFontProvider;
import dev.joid.lib.font.dto.FontBounds;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.font.dto.TextStyle;
import dev.joid.lib.font.dto.effect.ITextEffect;
import dev.joid.lib.font.dto.markup.ITextMarkup;
import dev.joid.lib.font.dto.markup.TextMarkup;
import dev.joid.lib.font.impl.glyph.dto.GlyphLayout;
import dev.joid.lib.font.impl.glyph.dto.GlyphPlacement;
import dev.joid.lib.font.impl.glyph.dto.IFontFace;
import dev.joid.lib.font.impl.glyph.dto.TextGlyph;
import lombok.NonNull;

@SuppressWarnings("unchecked")
public abstract class GlyphFontProvider<F extends IFontFace> implements IFontProvider {

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
		F face = font.getFace(style.getWeight(), style.isItalic());
		double pen = 0D;
		int previous = -1;
		for (int index = 0; index < text.length();) {
			final int consumed = TextMarkup.parse(markups, text, index, style);
			if (consumed > 0) {
				index += consumed;
				snapshot = style.copy();
				final F next = font.getFace(style.getWeight(), style.isItalic());
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
			if (!drawn && codepoint != ' ' && codepoint != ' ') {
				continue;
			}

			if (previous != -1) {
				pen += face.getKerning(previous, codepoint) * size;
			}

			placements.add(new GlyphPlacement<>(start, codepoint, face, pen, snapshot));
			pen += (drawn ? face.getAdvance(codepoint) : GlyphFontProvider.space(face)) * size + spacing;
			previous = codepoint;
		}

		return new GlyphLayout<>(placements, placements.isEmpty() ? 0D : pen - spacing);
	}

	protected abstract void end();
	protected abstract void begin(final double runX, final double runY, final double runWidth, final double runHeight);

	protected abstract void drawGlyph(final @NonNull TextGlyph<F> glyph);

	private @NonNull F getFace(final @NonNull TextInfo info) {
		return this.getFont(info).getFace(info.getWeight(), info.isItalic());
	}

	private @NonNull GlyphFont<F> getFont(final @NonNull TextInfo info) {
		final IFont font = info.getFont();
		if (!(font instanceof GlyphFont) || font.getFontProvider().getClass() != this.getClass()) {
			throw new IllegalArgumentException(this.getClass().getName() + " cannot draw the font " + font.getClass().getName() + ", it is drawn by " + font.getFontProvider().getClass().getName() + ": draw it with info.getFont().getFontProvider()");
		}

		return (GlyphFont<F>) font;
	}

	private @NonNull FontBounds draw(final @NonNull GlyphLayout<F> layout, final double x, final double y, final @NonNull TextInfo info, final double runX, final double runY, final double runWidth, final double runHeight) {
		if (layout.getPlacements().isEmpty()) {
			return FontBounds.empty();
		}

		final List<TextGlyph<F>> glyphs = this.glyphs(layout, x, y, info);
		for (final TextGlyph<F> glyph : glyphs) {
			for (final ITextEffect effect : glyph.getStyle().getEffects()) {
				effect.background(glyph);
			}
		}

		final Color shadow = info.getShadowColor();
		if (shadow != null) {
			final List<TextGlyph<F>> shadows = new ArrayList<>(glyphs.size());
			for (final TextGlyph<F> glyph : glyphs) {
				shadows.add(glyph.shadow(info.getShadowX(), info.getShadowY(), shadow));
			}
			this.render(shadows, runX + info.getShadowX(), runY + info.getShadowY(), runWidth, runHeight);
		}

		this.render(glyphs, runX, runY, runWidth, runHeight);
		return new FontBounds(layout.getWidth(), this.getLineHeight(info));
	}

	private @NonNull List<TextGlyph<F>> glyphs(final @NonNull GlyphLayout<F> layout, final double x, final double y, final @NonNull TextInfo info) {
		final List<GlyphPlacement<F>> placements = layout.getPlacements();
		final List<TextGlyph<F>> glyphs = new ArrayList<>(placements.size());
		final F base = this.getFace(info);
		final float size = info.getFontSize();
		final double baseline = y + (base.getLineHeight() + base.getDescender()) * size + (this.getLineHeight(info) - base.getLineHeight() * size) / 2D;
		for (int i = 0; i < placements.size(); i++) {
			final GlyphPlacement<F> placement = placements.get(i);
			final double advance = (i + 1 < placements.size() ? placements.get(i + 1).getX() : layout.getWidth()) - placement.getX();
			final TextGlyph<F> glyph = TextGlyph.create(placement.getFace(), placement.getIndex(), placement.getCodepoint(), placement.getStyle(), x + placement.getX(), baseline, size, advance, GlyphFontProvider.color(placement.getStyle(), info));
			for (final ITextEffect effect : placement.getStyle().getEffects()) {
				effect.apply(glyph);
			}
			glyphs.add(glyph);
		}

		return glyphs;
	}

	private void render(final @NonNull List<TextGlyph<F>> glyphs, final double runX, final double runY, final double runWidth, final double runHeight) {
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

	private static @NonNull Color color(final @NonNull TextStyle style, final @NonNull TextInfo info) {
		if (!info.isColored()) {
			return info.getColor();
		}

		final Color color = style.getColor();
		return color.a == info.getColor().a ? color : color.copyAlpha(info.getColor().a);
	}

}