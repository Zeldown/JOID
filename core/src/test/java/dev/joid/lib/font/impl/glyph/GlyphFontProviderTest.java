package dev.joid.lib.font.impl.glyph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.IFontProvider;
import dev.joid.lib.font.dto.FontBounds;
import dev.joid.lib.font.dto.TextInfo;
import dev.joid.lib.font.dto.effect.ITextEffect;
import dev.joid.lib.font.dto.effect.ITextGlyph;
import dev.joid.lib.font.dto.markup.ITextMarkup;
import dev.joid.lib.font.impl.glyph.dto.FontFamily;
import dev.joid.lib.font.impl.glyph.dto.GlyphLayout;
import dev.joid.lib.font.impl.glyph.dto.GlyphPlacement;
import dev.joid.lib.font.impl.glyph.dto.IFontFace;
import dev.joid.lib.font.impl.glyph.dto.TextGlyph;
import dev.joid.lib.font.impl.msdf.MsdfFontProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

public class GlyphFontProviderTest {

	private static final Face     BOLD     = new Face(FontWeight.BOLD, false, 2F, false);
	private static final Face     ITALIC   = new Face(FontWeight.REGULAR, true, 1F, false);
	private static final Face     SPACED   = new Face(FontWeight.REGULAR, false, 1F, true);
	private static final Face     REGULAR  = new Face(FontWeight.REGULAR, false, 1F, false);
	private static final Provider PROVIDER = new Provider();

	private static final ITextMarkup MARKUP = (text, index, style) -> {
		switch (text.charAt(index)) {
		case '*':
			style.weight(style.getWeight() == FontWeight.BOLD ? style.getBase().getWeight() : FontWeight.BOLD);
			return 1;
		case '~':
			style.color(Color.RED);
			return 1;
		default:
			return 0;
		}
	};

	private final List<String> events = new ArrayList<>();

	private final ITextEffect effect = new ITextEffect() {

		@Override
		public void apply(final ITextGlyph glyph) {
			GlyphFontProviderTest.this.events.add("apply " + (char) glyph.getCodepoint());
			if (glyph.getCodepoint() == 'x') {
				glyph.codepoint('y');
			}
		}

		@Override
		public void background(final ITextGlyph glyph) {
			GlyphFontProviderTest.this.events.add("background " + (char) glyph.getCodepoint());
		}

		@Override
		public void decorate(final ITextGlyph glyph) {
			GlyphFontProviderTest.this.events.add("decorate " + (char) glyph.getCodepoint() + (glyph.isShadow() ? " shadow" : ""));
		}

	};

	@Before
	public void reset() {
		GlyphFontProviderTest.PROVIDER.events = this.events;
		GlyphFontProviderTest.PROVIDER.drawn.clear();
		GlyphFontProviderTest.PROVIDER.runs.clear();
	}

	@Test
	public void prefersAnItalicFace() {
		GlyphFontProviderTest.draw("*A", GlyphFontProviderTest.info().italic(true));
		Assert.assertSame(GlyphFontProviderTest.ITALIC, GlyphFontProviderTest.PROVIDER.drawn.get(0).getFace());
		Assert.assertFalse(GlyphFontProviderTest.PROVIDER.drawn.get(0).isSlanted());
	}

	@Test
	public void slantsAnUprightFace() {
		GlyphFontProviderTest.draw("*A", GlyphFontProviderTest.info(GlyphFontProviderTest.REGULAR, GlyphFontProviderTest.BOLD).italic(true));
		Assert.assertSame(GlyphFontProviderTest.BOLD, GlyphFontProviderTest.PROVIDER.drawn.get(0).getFace());
		Assert.assertTrue(GlyphFontProviderTest.PROVIDER.drawn.get(0).isSlanted());
	}

	@Test
	public void returnsTheDrawnBounds() {
		final FontBounds bounds = GlyphFontProviderTest.PROVIDER.drawText(0D, 0D, "AV", GlyphFontProviderTest.info().letterSpacing(0.2F).lineHeight(1.5F));
		Assert.assertEquals(11D, bounds.getWidth(), 1E-6D);
		Assert.assertEquals(15D, bounds.getHeight(), 1E-6D);
	}

	@Test
	public void runsTheEffectsInOrder() {
		GlyphFontProviderTest.draw("AB", GlyphFontProviderTest.info().shadow(Color.BLACK).effects(this.effect));
		Assert.assertEquals(Arrays.asList(
				"apply A", "apply B",
				"background A", "background B",
				"begin", "draw A shadow", "draw B shadow", "end", "decorate A shadow", "decorate B shadow",
				"begin", "draw A", "draw B", "end", "decorate A", "decorate B"), this.events);
	}

	@Test
	public void drawsInsideTheGivenRun() {
		final FontBounds bounds = GlyphFontProviderTest.PROVIDER.drawText(5D, 6D, "AB", GlyphFontProviderTest.info().shadow(Color.BLACK).shadow(2F, 3F), 1D, 2D, 300D, 40D);
		Assert.assertEquals(10D, bounds.getWidth(), 1E-6D);
		Assert.assertEquals(12D, bounds.getHeight(), 1E-6D);
		Assert.assertArrayEquals(new double[] {3D, 5D, 300D, 40D}, GlyphFontProviderTest.PROVIDER.runs.get(0), 1E-6D);
		Assert.assertArrayEquals(new double[] {1D, 2D, 300D, 40D}, GlyphFontProviderTest.PROVIDER.runs.get(1), 1E-6D);
	}

	@Test
	public void drawsNothingWithoutGlyph() {
		final FontBounds bounds = GlyphFontProviderTest.PROVIDER.drawText(0D, 0D, "##", GlyphFontProviderTest.info().shadow(Color.BLACK));
		Assert.assertEquals(0D, bounds.getWidth(), 0D);
		Assert.assertEquals(0D, bounds.getHeight(), 0D);
		Assert.assertTrue(this.events.isEmpty());
	}

	@Test
	public void wrapsAPlainTextInItsOwnRun() {
		GlyphFontProviderTest.PROVIDER.drawText(5D, 6D, "AB", GlyphFontProviderTest.info());
		Assert.assertArrayEquals(new double[] {5D, 6D, 10D, 12D}, GlyphFontProviderTest.PROVIDER.runs.get(0), 1E-6D);
		Assert.assertEquals(5D, GlyphFontProviderTest.PROVIDER.drawn.get(0).getX(), 1E-6D);
		Assert.assertEquals(10D, GlyphFontProviderTest.PROVIDER.drawn.get(1).getX(), 1E-6D);
	}

	@Test
	public void resolvesTheRequestedWeight() {
		Assert.assertEquals(20D, GlyphFontProviderTest.info().weight(FontWeight.BLACK).getWidth("AB"), 1E-6D);
	}

	@Test
	public void measuresNothingWithoutGlyph() {
		Assert.assertEquals(0D, GlyphFontProviderTest.info().letterSpacing(0.2F).getWidth(""), 0D);
		Assert.assertEquals(0D, GlyphFontProviderTest.info().letterSpacing(0.2F).getWidth("##"), 0D);
	}

	@Test
	public void shadowsTheTransformedGlyphs() {
		GlyphFontProviderTest.draw("x", GlyphFontProviderTest.info().shadow(Color.BLACK).shadow(2F, 3F).effects(this.effect));
		final TextGlyph<Face> shadow = GlyphFontProviderTest.PROVIDER.drawn.get(0);
		final TextGlyph<Face> glyph = GlyphFontProviderTest.PROVIDER.drawn.get(1);
		Assert.assertEquals('y', shadow.getCodepoint());
		Assert.assertSame(Color.BLACK, shadow.getColor());
		Assert.assertEquals(glyph.getX() + 2D, shadow.getX(), 1E-6D);
		Assert.assertEquals(glyph.getBaseline() + 3D, shadow.getBaseline(), 1E-6D);
	}

	@Test
	public void switchesTheFaceThroughMarkup() {
		final GlyphLayout<Face> layout = GlyphFontProviderTest.layout("*A*A", GlyphFontProviderTest.info());
		Assert.assertSame(GlyphFontProviderTest.BOLD, layout.getPlacements().get(0).getFace());
		Assert.assertSame(GlyphFontProviderTest.REGULAR, layout.getPlacements().get(1).getFace());
		Assert.assertEquals(15D, layout.getWidth(), 1E-6D);
	}

	@Test
	public void restartsKerningOnAFaceSwitch() {
		Assert.assertEquals(15D, GlyphFontProviderTest.info().getWidth("A*V"), 1E-6D);
	}

	@Test
	public void spansEachGlyphUpToTheNextOne() {
		GlyphFontProviderTest.draw("AV", GlyphFontProviderTest.info().letterSpacing(0.2F));
		Assert.assertEquals(6D, GlyphFontProviderTest.PROVIDER.drawn.get(0).getAdvance(), 1E-6D);
		Assert.assertEquals(5D, GlyphFontProviderTest.PROVIDER.drawn.get(1).getAdvance(), 1E-6D);
	}

	@Test
	public void colorsTheGlyphsThroughMarkup() {
		GlyphFontProviderTest.draw("A~A", GlyphFontProviderTest.info().color(Color.WHITE.copyAlpha(0.5F)));
		Assert.assertEquals(0.5F, GlyphFontProviderTest.PROVIDER.drawn.get(0).getColor().a, 0F);
		Assert.assertEquals(Color.RED.r, GlyphFontProviderTest.PROVIDER.drawn.get(1).getColor().r, 0F);
		Assert.assertEquals(0.5F, GlyphFontProviderTest.PROVIDER.drawn.get(1).getColor().a, 0F);
	}

	@Test
	public void drawsMarkupAsTextWhenDisabled() {
		Assert.assertEquals(10D, GlyphFontProviderTest.info().markups().getWidth("*A"), 1E-6D);
	}

	@Test
	public void placesEveryGlyphOnTheBaseline() {
		GlyphFontProviderTest.draw("*A*A", GlyphFontProviderTest.info());
		Assert.assertEquals(10D, GlyphFontProviderTest.PROVIDER.drawn.get(0).getBaseline(), 1E-6D);
		Assert.assertEquals(10D, GlyphFontProviderTest.PROVIDER.drawn.get(1).getBaseline(), 1E-6D);
	}

	@Test
	public void pointsEachGlyphBackToItsSource() {
		final List<GlyphPlacement<Face>> placements = GlyphFontProviderTest.layout("*A\uD83D\uDE00B", GlyphFontProviderTest.info()).getPlacements();
		Assert.assertEquals(3, placements.size());
		Assert.assertEquals(1, placements.get(0).getIndex());
		Assert.assertEquals(0x1F600, placements.get(1).getCodepoint());
		Assert.assertEquals(4, placements.get(2).getIndex());
	}

	@Test
	public void measuresTheLineHeightForAnyText() {
		Assert.assertEquals(15D, GlyphFontProviderTest.PROVIDER.getHeight("AV", GlyphFontProviderTest.info().lineHeight(1.5F)), 1E-6D);
		Assert.assertEquals(15D, GlyphFontProviderTest.PROVIDER.getHeight("", GlyphFontProviderTest.info().lineHeight(1.5F)), 1E-6D);
	}

	@Test
	public void scalesTheLineHeightWithTheFontSize() {
		Assert.assertEquals(30D, GlyphFontProviderTest.info().fontSize(20F).lineHeight(1.5F).getHeight(), 1E-6D);
	}

	@Test
	public void centersTheGlyphsInTheLineHeight() {
		GlyphFontProviderTest.draw("A", GlyphFontProviderTest.info().lineHeight(1.6F));
		Assert.assertEquals(12D, GlyphFontProviderTest.PROVIDER.drawn.get(0).getBaseline(), 1E-6D);
	}

	@Test
	public void keepsTheTextColorWhenNotColored() {
		GlyphFontProviderTest.draw("~A", GlyphFontProviderTest.info().color(Color.WHITE).colored(false));
		Assert.assertSame(Color.WHITE, GlyphFontProviderTest.PROVIDER.drawn.get(0).getColor());
	}

	@Test
	public void measuresAdvancesKerningAndSpacing() {
		Assert.assertEquals(11D, GlyphFontProviderTest.info().letterSpacing(0.2F).getWidth("AV"), 1E-6D);
		Assert.assertEquals(10D, GlyphFontProviderTest.info().getWidth("AB"), 1E-6D);
	}

	@Test
	public void scalesTheLetterSpacingWithTheFontSize() {
		Assert.assertEquals(22D, GlyphFontProviderTest.info().fontSize(20F).letterSpacing(0.2F).getWidth("AV"), 1E-6D);
	}

	@Test
	public void skipsMissingGlyphsWithoutBreakingKerning() {
		final TextInfo info = GlyphFontProviderTest.info().letterSpacing(0.2F);
		Assert.assertEquals(info.getWidth("AV"), info.getWidth("A#V"), 1E-6D);
	}

	@Test
	public void advancesASpaceMissingFromTheFaceByAQuarterOfEm() {
		final TextInfo info = GlyphFontProviderTest.info();
		Assert.assertEquals(2.5D, info.getWidth(" "), 1E-6D);
		Assert.assertEquals(12.5D, info.getWidth("A B"), 1E-6D);
		Assert.assertEquals(7.5D, GlyphFontProviderTest.layout("A B", info).getPlacements().get(2).getX(), 1E-6D);
	}

	@Test
	public void advancesANonBreakingSpaceMissingFromTheFaceLikeAMissingSpace() {
		final TextInfo info = GlyphFontProviderTest.info().letterSpacing(0.2F);
		Assert.assertEquals(2.5D, info.getWidth(" "), 1E-6D);
		Assert.assertEquals(info.getWidth("A B"), info.getWidth("A B"), 1E-6D);
		Assert.assertEquals(3, GlyphFontProviderTest.layout("A B", info).getPlacements().size());
	}

	@Test
	public void advancesANonBreakingSpaceMissingFromTheFaceLikeTheSpaceOfTheFace() {
		final TextInfo info = GlyphFontProviderTest.info(GlyphFontProviderTest.SPACED);
		Assert.assertEquals(3D, info.getWidth(" "), 1E-6D);
		Assert.assertEquals(info.getWidth("A B"), info.getWidth("A B"), 1E-6D);
	}

	@Test
	public void namesTheProviderOfAFontItCannotDraw() {
		final IFontProvider other = MsdfFontProvider.inst();
		try {
			GlyphFontProviderTest.PROVIDER.getWidth("A", TextInfo.create(() -> other, 10F));
			Assert.fail("A font of another provider must be refused");
		} catch (final IllegalArgumentException expected) {
			Assert.assertTrue(expected.getMessage(), expected.getMessage().startsWith(Provider.class.getName() + " cannot draw the font "));
			Assert.assertTrue(expected.getMessage(), expected.getMessage().endsWith(", it is drawn by " + other.getClass().getName() + ": draw it with info.getFont().getFontProvider()"));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAGlyphFontOfAnotherProvider() {
		MsdfFontProvider.inst().getWidth("A", GlyphFontProviderTest.info());
	}

	private static TextInfo info() {
		return GlyphFontProviderTest.info(GlyphFontProviderTest.REGULAR, GlyphFontProviderTest.BOLD, GlyphFontProviderTest.ITALIC);
	}

	private static TextInfo info(final Face... faces) {
		return TextInfo.create(new Font(FontFamily.of(faces)), 10F).markups(GlyphFontProviderTest.MARKUP);
	}

	private static void draw(final String text, final TextInfo info) {
		GlyphFontProviderTest.PROVIDER.drawText(0D, 0D, text, info);
	}

	private static GlyphLayout<Face> layout(final String text, final TextInfo info) {
		return GlyphFontProviderTest.PROVIDER.layout(text, info);
	}

	@Getter
	@AllArgsConstructor
	private static final class Face implements IFontFace {

		private final FontWeight weight;
		private final boolean    italic;
		private final float      scale;
		private final boolean    spaced;

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
			return codepoint != '#' && codepoint != ' ' && (codepoint != ' ' || this.spaced);
		}

		@Override
		public float getAdvance(final int codepoint) {
			return codepoint == ' ' ? this.scale * 0.3F : this.scale * 0.5F;
		}

		@Override
		public float getKerning(final int previous, final int current) {
			return previous == 'A' && current == 'V' ? -0.1F : 0F;
		}

	}

	private static final class Font extends GlyphFont<Face> {

		private Font(final FontFamily<Face> family) {
			super(family);
		}

		@Override
		public IFontProvider getFontProvider() {
			return GlyphFontProviderTest.PROVIDER;
		}

	}

	private static final class Provider extends GlyphFontProvider<Face> {

		private final List<double[]>        runs  = new ArrayList<>();
		private final List<TextGlyph<Face>> drawn = new ArrayList<>();

		private List<String> events = new ArrayList<>();

		@Override
		protected void end() {
			this.events.add("end");
		}

		@Override
		protected void drawGlyph(final TextGlyph<Face> glyph) {
			this.drawn.add(glyph);
			this.events.add("draw " + (char) glyph.getCodepoint() + (glyph.isShadow() ? " shadow" : ""));
		}

		@Override
		protected void begin(final double runX, final double runY, final double runWidth, final double runHeight) {
			this.events.add("begin");
			this.runs.add(new double[] {runX, runY, runWidth, runHeight});
		}

	}

}