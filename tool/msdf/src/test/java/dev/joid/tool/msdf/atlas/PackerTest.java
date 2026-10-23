package dev.joid.tool.msdf.atlas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.tool.msdf.geometry.Edge;
import dev.joid.tool.msdf.geometry.Shape;
import dev.joid.tool.msdf.geometry.Vector2;

public class PackerTest {

	@Test
	public void packsGlyphsWithoutOverlap() {
		final List<GlyphEntry> glyphs = PackerTest.squares(6);
		Assert.assertTrue(Packer.pack(glyphs, 16D, 2D, 64, 64));
		for (final GlyphEntry glyph : glyphs) {
			for (final GlyphEntry other : glyphs) {
				final boolean apart = glyph == other || glyph.getX() + glyph.getWidth() <= other.getX() || other.getX() + other.getWidth() <= glyph.getX() || glyph.getY() + glyph.getHeight() <= other.getY() || other.getY() + other.getHeight() <= glyph.getY();
				Assert.assertTrue(apart);
			}
		}
	}

	@Test
	public void refusesGlyphsTooLargeForTheAtlas() {
		Assert.assertFalse(Packer.pack(PackerTest.squares(1), 128D, 2D, 64, 64));
		Assert.assertFalse(Packer.pack(PackerTest.squares(40), 16D, 2D, 64, 64));
	}

	@Test
	public void fitsTheLargestSizeThatPacks() {
		final List<GlyphEntry> glyphs = PackerTest.squares(4);
		final double size = Packer.fit(glyphs, 2D, 64, 64);
		Assert.assertTrue(Packer.pack(glyphs, size, 2D, 64, 64));
		Assert.assertFalse(Packer.pack(glyphs, size + 1D, 2D, 64, 64));
	}

	@Test
	public void refusesGlyphsThatDoNotFitAtAnySize() {
		final IllegalStateException exception = Assert.assertThrows(IllegalStateException.class, () -> Packer.fit(PackerTest.squares(40), 2D, 8, 8));
		Assert.assertEquals("The glyphs do not fit in 8x8 even at 1.0px, raise --width and --height or shrink the charset", exception.getMessage());
	}

	@Test
	public void skipsGlyphsWithoutOutline() {
		final List<GlyphEntry> glyphs = new ArrayList<>(Arrays.asList(new GlyphEntry(' ', 0.25D, new Shape())));
		Assert.assertTrue(Packer.pack(glyphs, 16D, 2D, 8, 8));
	}

	private static List<GlyphEntry> squares(final int count) {
		final List<GlyphEntry> glyphs = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			final Shape shape = new Shape();
			shape.add(new ArrayList<>(Arrays.asList(PackerTest.line(0D, 0D, 1D, 0D), PackerTest.line(1D, 0D, 1D, 1D), PackerTest.line(1D, 1D, 0D, 1D), PackerTest.line(0D, 1D, 0D, 0D))));
			glyphs.add(new GlyphEntry('A' + i, 1D, shape));
		}
		return glyphs;
	}

	private static Edge line(final double x1, final double y1, final double x2, final double y2) {
		return new Edge(Arrays.asList(Vector2.of(x1, y1), Vector2.of(x2, y2)));
	}

}