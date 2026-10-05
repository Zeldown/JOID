package be.zeldown.joid.lib.font.impl.msdf.dto;

import java.awt.image.BufferedImage;
import java.util.Map;

import be.zeldown.joid.lib.resource.Resource;
import be.zeldown.joid.lib.resource.ResourceBuilder;
import lombok.Getter;

@Getter
public class MsdfFace {

	private static final ResourceBuilder BUILDER = ResourceBuilder.create().cache(null).blocking().linear();

	private final MsdfAtlas               atlas;
	private final Resource                texture;
	private final MsdfMetrics             metrics;
	private final Map<Long, Float>        kerningPairs;
	private final Map<Integer, MsdfGlyph> glyphs;

	public MsdfFace(final MsdfAtlas atlas, final MsdfMetrics metrics, final Map<Integer, MsdfGlyph> glyphs, final Map<Long, Float> kerningPairs, final BufferedImage image) {
		this.atlas = atlas;
		this.metrics = metrics;
		this.glyphs = glyphs;
		this.kerningPairs = kerningPairs;
		this.texture = MsdfFace.BUILDER.of(image);
	}

	public MsdfGlyph getGlyph(final int codepoint) {
		return this.glyphs.get(codepoint);
	}

	public float getKerning(final int previous, final int current) {
		final Float value = this.kerningPairs.get(MsdfFace.pair(previous, current));
		return value == null ? 0F : value;
	}

	public static long pair(final int previous, final int current) {
		return (long) previous << 32 | current & 0xFFFFFFFFL;
	}

}