package be.zeldown.joid.lib.font.impl.glyph.dto;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import be.zeldown.joid.lib.font.FontWeight;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class FontFamilyTest {

	@Test
	public void sortsTheFacesByWeight() {
		final Face bold = new Face(FontWeight.BOLD, false);
		final Face thin = new Face(FontWeight.THIN, false);
		final Face regular = new Face(FontWeight.REGULAR, false);
		Assert.assertEquals(Arrays.asList(thin, regular, bold), FontFamily.of(bold, thin, regular).getFaces());
	}

	@Test
	public void resolvesTheExactWeight() {
		final FontFamily<Face> family = FontFamilyTest.family(300, 400, 600, 700);
		for (final int weight : new int[] {300, 400, 600, 700}) {
			Assert.assertEquals(weight, family.resolve(FontWeight.of(weight), false).getWeight().getValue());
		}
	}

	@Test
	public void looksUpToMediumFirstForNormalWeights() {
		Assert.assertEquals(500, FontFamilyTest.family(300, 500, 600).resolve(FontWeight.REGULAR, false).getWeight().getValue());
		Assert.assertEquals(300, FontFamilyTest.family(300, 600).resolve(FontWeight.REGULAR, false).getWeight().getValue());
		Assert.assertEquals(300, FontFamilyTest.family(300, 600).resolve(FontWeight.MEDIUM, false).getWeight().getValue());
		Assert.assertEquals(600, FontFamilyTest.family(600, 900).resolve(FontWeight.REGULAR, false).getWeight().getValue());
	}

	@Test
	public void looksLighterFirstForLightWeights() {
		Assert.assertEquals(200, FontFamilyTest.family(100, 200, 400).resolve(FontWeight.LIGHT, false).getWeight().getValue());
		Assert.assertEquals(400, FontFamilyTest.family(400, 700).resolve(FontWeight.THIN, false).getWeight().getValue());
	}

	@Test
	public void looksHeavierFirstForBoldWeights() {
		Assert.assertEquals(900, FontFamilyTest.family(400, 900).resolve(FontWeight.SEMI_BOLD, false).getWeight().getValue());
		Assert.assertEquals(700, FontFamilyTest.family(400, 700).resolve(FontWeight.BLACK, false).getWeight().getValue());
	}

	@Test
	public void prefersTheRequestedStyle() {
		final Face upright = new Face(FontWeight.REGULAR, false);
		final Face italic = new Face(FontWeight.BOLD, true);
		final FontFamily<Face> family = FontFamily.of(upright, italic);
		Assert.assertSame(italic, family.resolve(FontWeight.REGULAR, true));
		Assert.assertSame(upright, family.resolve(FontWeight.BOLD, false));
	}

	@Test
	public void fallsBackToTheOtherStyle() {
		final Face upright = new Face(FontWeight.REGULAR, false);
		Assert.assertSame(upright, FontFamily.of(upright).resolve(FontWeight.REGULAR, true));
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesTwoFacesOfTheSameStyle() {
		FontFamily.of(new Face(FontWeight.BOLD, false), new Face(FontWeight.BOLD, false));
	}

	@Test
	public void acceptsTheSameWeightInBothStyles() {
		Assert.assertEquals(2, FontFamily.of(new Face(FontWeight.BOLD, false), new Face(FontWeight.BOLD, true)).getFaces().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAnEmptyFamily() {
		FontFamily.of();
	}

	private static FontFamily<Face> family(final int... weights) {
		final Face[] faces = new Face[weights.length];
		for (int i = 0; i < weights.length; i++) {
			faces[i] = new Face(FontWeight.of(weights[i]), false);
		}
		return FontFamily.of(faces);
	}

	@Getter
	@AllArgsConstructor
	private static final class Face implements IGlyphFace {

		private final FontWeight weight;
		private final boolean    italic;

		@Override
		public float getAscender() {
			return 0F;
		}

		@Override
		public float getDescender() {
			return 0F;
		}

		@Override
		public float getLineHeight() {
			return 0F;
		}

		@Override
		public float getUnderlineY() {
			return 0F;
		}

		@Override
		public float getUnderlineThickness() {
			return 0F;
		}

		@Override
		public boolean hasGlyph(final int codepoint) {
			return false;
		}

		@Override
		public float getAdvance(final int codepoint) {
			return 0F;
		}

		@Override
		public float getKerning(final int previous, final int current) {
			return 0F;
		}

	}

}