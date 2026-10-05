package be.zeldown.joid.lib.font.impl.msdf.dto;

import java.awt.image.BufferedImage;
import java.util.Map;

import be.zeldown.joid.lib.font.FontWeight;
import be.zeldown.joid.lib.font.impl.glyph.dto.IFontFace;
import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.resource.ResourceBuilder;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class MsdfFontFace implements IFontFace {

	private static final ResourceBuilder BUILDER = ResourceBuilder.create().cache(null).blocking().linear().mipmap(false);

	private final boolean                 italic;
	private final MsdfAtlas               atlas;
	private final Resource                texture;
	private final FontWeight              weight;
	private final MsdfMetrics             metrics;
	private final Map<Long, Float>        kerningPairs;
	private final Map<Integer, MsdfGlyph> glyphs;

	private MsdfFontFace(final MsdfAtlas atlas, final MsdfMetrics metrics, final Map<Integer, MsdfGlyph> glyphs, final Map<Long, Float> kerningPairs, final Resource texture, final FontWeight weight, final boolean italic) {
		this.atlas = atlas;
		this.metrics = metrics;
		this.glyphs = glyphs;
		this.kerningPairs = kerningPairs;
		this.texture = texture;
		this.weight = weight;
		this.italic = italic;
	}

	public static @NonNull MsdfFontFace create(final @NonNull MsdfAtlas atlas, final @NonNull MsdfMetrics metrics, final @NonNull Map<Integer, MsdfGlyph> glyphs, final @NonNull Map<Long, Float> kerningPairs, final @NonNull BufferedImage image, final @NonNull FontWeight weight, final boolean italic) {
		return new MsdfFontFace(atlas, metrics, glyphs, kerningPairs, MsdfFontFace.BUILDER.of(image), weight, italic);
	}

	@Override
	public float getAscender() {
		return this.metrics.getAscender();
	}

	@Override
	public float getDescender() {
		return this.metrics.getDescender();
	}

	@Override
	public float getLineHeight() {
		return this.metrics.getLineHeight();
	}

	@Override
	public float getUnderlineY() {
		return this.metrics.getUnderlineY();
	}

	@Override
	public float getUnderlineThickness() {
		return this.metrics.getUnderlineThickness();
	}

	@Override
	public float getAdvance(final int codepoint) {
		final MsdfGlyph glyph = this.glyphs.get(codepoint);
		return glyph == null ? 0F : glyph.getAdvance();
	}

	public MsdfGlyph getGlyph(final int codepoint) {
		return this.glyphs.get(codepoint);
	}

	@Override
	public float getKerning(final int previous, final int current) {
		final Float value = this.kerningPairs.get(MsdfFontFace.pair(previous, current));
		return value == null ? 0F : value;
	}

	@Override
	public boolean hasGlyph(final int codepoint) {
		return this.glyphs.containsKey(codepoint);
	}

	public static long pair(final int previous, final int current) {
		return (long) previous << 32 | current & 0xFFFFFFFFL;
	}

	public @NonNull MsdfFontFace style(final @NonNull FontWeight weight, final boolean italic) {
		return new MsdfFontFace(this.atlas, this.metrics, this.glyphs, this.kerningPairs, this.texture, weight, italic);
	}

}