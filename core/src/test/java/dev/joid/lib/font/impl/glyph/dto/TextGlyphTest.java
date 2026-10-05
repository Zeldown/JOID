package dev.joid.lib.font.impl.glyph.dto;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.dto.TextStyle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

public class TextGlyphTest {

	private static final Face      ITALIC  = new Face(FontWeight.REGULAR, true);
	private static final Face      UPRIGHT = new Face(FontWeight.REGULAR, false);
	private static final TextStyle PLAIN   = TextStyle.create(FontWeight.REGULAR, false, Color.WHITE);
	private static final TextStyle SLANTED = TextStyle.create(FontWeight.REGULAR, true, Color.WHITE);

	@Test
	public void keepsItsPlacement() {
		final TextGlyph<Face> glyph = TextGlyph.create(TextGlyphTest.UPRIGHT, 3, 'A', TextGlyphTest.PLAIN, 10D, 100D, 20D, 9D, Color.RED);
		Assert.assertSame(TextGlyphTest.UPRIGHT, glyph.getFace());
		Assert.assertSame(TextGlyphTest.PLAIN, glyph.getStyle());
		Assert.assertEquals(3, glyph.getIndex());
		Assert.assertEquals('A', glyph.getCodepoint());
		Assert.assertEquals(10D, glyph.getX(), 0D);
		Assert.assertEquals(100D, glyph.getBaseline(), 0D);
		Assert.assertEquals(20D, glyph.getSize(), 0D);
		Assert.assertEquals(9D, glyph.getAdvance(), 0D);
		Assert.assertSame(Color.RED, glyph.getColor());
		Assert.assertFalse(glyph.isShadow());
	}

	@Test
	public void changesWhatIsDrawn() {
		final TextGlyph<Face> glyph = TextGlyph.create(TextGlyphTest.UPRIGHT, 0, 'A', TextGlyphTest.PLAIN, 0D, 0D, 20D, 9D, Color.WHITE);
		Assert.assertSame(glyph, glyph.codepoint('B').color(Color.BLUE).offset(1D, -2D));
		Assert.assertEquals('B', glyph.getCodepoint());
		Assert.assertSame(Color.BLUE, glyph.getColor());
		Assert.assertEquals(1D, glyph.getOffsetX(), 0D);
		Assert.assertEquals(-2D, glyph.getOffsetY(), 0D);
	}

	@Test
	public void scalesTheFaceMetrics() {
		final TextGlyph<Face> glyph = TextGlyph.create(TextGlyphTest.UPRIGHT, 3, 'A', TextGlyphTest.PLAIN, 10D, 100D, 20D, 9D, Color.WHITE);
		Assert.assertEquals(16D, glyph.getAscender(), 1E-6D);
		Assert.assertEquals(-4D, glyph.getDescender(), 1E-6D);
		Assert.assertEquals(102D, glyph.getUnderlineY(), 1E-6D);
		Assert.assertEquals(1D, glyph.getUnderlineThickness(), 1E-6D);
		Assert.assertEquals(12D, glyph.getAdvance('W'), 1E-6D);
		Assert.assertTrue(glyph.hasGlyph('W'));
		Assert.assertFalse(glyph.hasGlyph('#'));
	}

	@Test
	public void castsAShadowFromTheChangedGlyph() {
		final TextGlyph<Face> glyph = TextGlyph.create(TextGlyphTest.UPRIGHT, 4, 'A', TextGlyphTest.PLAIN, 10D, 100D, 20D, 9D, Color.WHITE).codepoint('C').offset(1D, 2D);
		final TextGlyph<Face> shadow = glyph.shadow(3D, 4D, Color.BLACK);
		Assert.assertTrue(shadow.isShadow());
		Assert.assertEquals('C', shadow.getCodepoint());
		Assert.assertEquals(4, shadow.getIndex());
		Assert.assertEquals(13D, shadow.getX(), 0D);
		Assert.assertEquals(104D, shadow.getBaseline(), 0D);
		Assert.assertEquals(1D, shadow.getOffsetX(), 0D);
		Assert.assertEquals(2D, shadow.getOffsetY(), 0D);
		Assert.assertEquals(9D, shadow.getAdvance(), 0D);
		Assert.assertSame(Color.BLACK, shadow.getColor());
	}

	@Test
	public void slantsOnlyAnUprightFaceAskedInItalic() {
		Assert.assertFalse(TextGlyph.create(TextGlyphTest.UPRIGHT, 0, 'A', TextGlyphTest.PLAIN, 0D, 0D, 1D, 1D, Color.WHITE).isSlanted());
		Assert.assertTrue(TextGlyph.create(TextGlyphTest.UPRIGHT, 0, 'A', TextGlyphTest.SLANTED, 0D, 0D, 1D, 1D, Color.WHITE).isSlanted());
		Assert.assertFalse(TextGlyph.create(TextGlyphTest.ITALIC, 0, 'A', TextGlyphTest.SLANTED, 0D, 0D, 1D, 1D, Color.WHITE).isSlanted());
		Assert.assertFalse(TextGlyph.create(TextGlyphTest.ITALIC, 0, 'A', TextGlyphTest.PLAIN, 0D, 0D, 1D, 1D, Color.WHITE).isSlanted());
	}

	@Getter
	@AllArgsConstructor
	private static final class Face implements IFontFace {

		private final FontWeight weight;
		private final boolean    italic;

		@Override
		public @NonNull String getName() {
			return "Test";
		}

		@Override
		public float getAscender() {
			return 0.8F;
		}

		@Override
		public float getDescender() {
			return -0.2F;
		}

		@Override
		public float getLineHeight() {
			return 1.2F;
		}

		@Override
		public float getUnderlineY() {
			return -0.1F;
		}

		@Override
		public float getUnderlineThickness() {
			return 0.05F;
		}

		@Override
		public boolean hasGlyph(final int codepoint) {
			return codepoint != '#';
		}

		@Override
		public float getAdvance(final int codepoint) {
			return 0.6F;
		}

		@Override
		public float getKerning(final int previous, final int current) {
			return 0F;
		}

	}

}