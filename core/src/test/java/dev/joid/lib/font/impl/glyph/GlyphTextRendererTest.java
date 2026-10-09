package dev.joid.lib.font.impl.glyph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.lib.bridge.render.CapturingRenderBridge;
import dev.joid.lib.bridge.render.RecordingShader;
import dev.joid.lib.color.Color;
import dev.joid.lib.font.FontBounds;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.ITextRenderer;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.effect.ITextGlyph;
import dev.joid.lib.font.impl.msdf.MsdfTextRenderer;
import dev.joid.lib.font.markup.ITextMarkup;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class GlyphTextRendererTest {

	private static final Face     BOLD     = new Face(FontWeight.BOLD, false, 2F, false);
	private static final Face     ITALIC   = new Face(FontWeight.REGULAR, true, 1F, false);
	private static final Face     SPACED   = new Face(FontWeight.REGULAR, false, 1F, true);
	private static final Face     REGULAR  = new Face(FontWeight.REGULAR, false, 1F, false);
	private static final Renderer ALIGNED  = new Renderer(true);
	private static final Renderer RENDERER = new Renderer(false);

	private static final Font OTHER = new Font(FontFamily.of(GlyphTextRendererTest.SPACED));

	private static final ITextMarkup FONT = (text, index, style) -> {
		if (text.charAt(index) != '^') {
			return 0;
		}

		style.font(style.getFont() == GlyphTextRendererTest.OTHER ? style.getBase().getFont() : GlyphTextRendererTest.OTHER);
		return 1;
	};

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

	@Rule
	public final CapturingRenderBridge render = new CapturingRenderBridge(1920, 1080);

	private final List<String> events = new ArrayList<>();

	private final ITextEffect effect = new ITextEffect() {

		@Override
		public void apply(final ITextGlyph glyph) {
			GlyphTextRendererTest.this.events.add("apply " + (char) glyph.getCodepoint());
			if (glyph.getCodepoint() == 'x') {
				glyph.codepoint('y');
			}
		}

		@Override
		public void background(final ITextGlyph glyph) {
			GlyphTextRendererTest.this.events.add("background " + (char) glyph.getCodepoint());
		}

		@Override
		public void decorate(final ITextGlyph glyph) {
			GlyphTextRendererTest.this.events.add("decorate " + (char) glyph.getCodepoint() + (glyph.isShadow() ? " shadow" : ""));
		}

	};

	@Before
	public void reset() {
		for (final Renderer renderer : new Renderer[] {GlyphTextRendererTest.RENDERER, GlyphTextRendererTest.ALIGNED}) {
			renderer.events = this.events;
			renderer.drawn.clear();
			renderer.runs.clear();
		}
	}

	@Test
	public void prefersAnItalicFace() {
		GlyphTextRendererTest.draw("*A", GlyphTextRendererTest.info().italic(true));
		Assert.assertSame(GlyphTextRendererTest.ITALIC, GlyphTextRendererTest.RENDERER.drawn.get(0).getFace());
		Assert.assertFalse(GlyphTextRendererTest.RENDERER.drawn.get(0).isSlanted());
	}

	@Test
	public void slantsAnUprightFace() {
		GlyphTextRendererTest.draw("*A", GlyphTextRendererTest.info(GlyphTextRendererTest.REGULAR, GlyphTextRendererTest.BOLD).italic(true));
		Assert.assertSame(GlyphTextRendererTest.BOLD, GlyphTextRendererTest.RENDERER.drawn.get(0).getFace());
		Assert.assertTrue(GlyphTextRendererTest.RENDERER.drawn.get(0).isSlanted());
	}

	@Test
	public void returnsTheDrawnBounds() {
		final FontBounds bounds = GlyphTextRendererTest.RENDERER.drawText(0D, 0D, "AV", GlyphTextRendererTest.info().letterSpacing(0.2F).lineHeight(1.5F));
		Assert.assertEquals(11D, bounds.getWidth(), 1E-6D);
		Assert.assertEquals(15D, bounds.getHeight(), 1E-6D);
	}

	@Test
	public void runsTheEffectsInOrder() {
		GlyphTextRendererTest.draw("AB", GlyphTextRendererTest.info().shadow(Color.BLACK).effects(this.effect));
		Assert.assertEquals(Arrays.asList(
				"apply A", "apply B",
				"background A", "background B",
				"begin", "draw A shadow", "draw B shadow", "end", "decorate A shadow", "decorate B shadow",
				"begin", "draw A", "draw B", "end", "decorate A", "decorate B"), this.events);
	}

	@Test
	public void drawsInsideTheGivenRun() {
		final FontBounds bounds = GlyphTextRendererTest.RENDERER.drawText(5D, 6D, "AB", GlyphTextRendererTest.info().shadow(Color.BLACK).shadow(2F, 3F), 1D, 2D, 300D, 40D);
		Assert.assertEquals(10D, bounds.getWidth(), 1E-6D);
		Assert.assertEquals(12D, bounds.getHeight(), 1E-6D);
		Assert.assertArrayEquals(new double[] {3D, 5D, 300D, 40D}, GlyphTextRendererTest.RENDERER.runs.get(0), 1E-6D);
		Assert.assertArrayEquals(new double[] {1D, 2D, 300D, 40D}, GlyphTextRendererTest.RENDERER.runs.get(1), 1E-6D);
	}

	@Test
	public void drawsNothingWithoutGlyph() {
		final FontBounds bounds = GlyphTextRendererTest.RENDERER.drawText(0D, 0D, "##", GlyphTextRendererTest.info().shadow(Color.BLACK));
		Assert.assertEquals(0D, bounds.getWidth(), 0D);
		Assert.assertEquals(0D, bounds.getHeight(), 0D);
		Assert.assertTrue(this.events.isEmpty());
	}

	@Test
	public void wrapsAPlainTextInItsOwnRun() {
		GlyphTextRendererTest.RENDERER.drawText(5D, 6D, "AB", GlyphTextRendererTest.info());
		Assert.assertArrayEquals(new double[] {5D, 6D, 10D, 12D}, GlyphTextRendererTest.RENDERER.runs.get(0), 1E-6D);
		Assert.assertEquals(5D, GlyphTextRendererTest.RENDERER.drawn.get(0).getX(), 1E-6D);
		Assert.assertEquals(10D, GlyphTextRendererTest.RENDERER.drawn.get(1).getX(), 1E-6D);
	}

	@Test
	public void resolvesTheRequestedWeight() {
		Assert.assertEquals(20D, GlyphTextRendererTest.info().weight(FontWeight.BLACK).getWidth("AB"), 1E-6D);
	}

	@Test
	public void measuresNothingWithoutGlyph() {
		Assert.assertEquals(0D, GlyphTextRendererTest.info().letterSpacing(0.2F).getWidth(""), 0D);
		Assert.assertEquals(0D, GlyphTextRendererTest.info().letterSpacing(0.2F).getWidth("##"), 0D);
	}

	@Test
	public void shadowsTheTransformedGlyphs() {
		GlyphTextRendererTest.draw("x", GlyphTextRendererTest.info().shadow(Color.BLACK).shadow(2F, 3F).effects(this.effect));
		final TextGlyph<Face> shadow = GlyphTextRendererTest.RENDERER.drawn.get(0);
		final TextGlyph<Face> glyph = GlyphTextRendererTest.RENDERER.drawn.get(1);
		Assert.assertEquals('y', shadow.getCodepoint());
		Assert.assertSame(Color.BLACK, shadow.getColor());
		Assert.assertEquals(glyph.getX() + 2D, shadow.getX(), 1E-6D);
		Assert.assertEquals(glyph.getBaseline() + 3D, shadow.getBaseline(), 1E-6D);
	}

	@Test
	public void tintsTheShadowOfEachGlyphWithItsColor() {
		GlyphTextRendererTest.draw("A~A", GlyphTextRendererTest.info().color(Color.WHITE).shadow(Color.BLUE).shadowTint(0.25F));
		final TextGlyph<Face> white = GlyphTextRendererTest.RENDERER.drawn.get(0);
		final TextGlyph<Face> red = GlyphTextRendererTest.RENDERER.drawn.get(1);
		Assert.assertTrue(white.isShadow());
		Assert.assertArrayEquals(new float[] {0.25F, 0.25F, 0.25F, 1F}, new float[] {white.getColor().r, white.getColor().g, white.getColor().b, white.getColor().a}, 1E-6F);
		Assert.assertArrayEquals(new float[] {0.25F, 0F, 0F, 1F}, new float[] {red.getColor().r, red.getColor().g, red.getColor().b, red.getColor().a}, 1E-6F);
		Assert.assertSame(Color.RED, GlyphTextRendererTest.RENDERER.drawn.get(3).getColor());
	}

	@Test
	public void drawsNoShadowWithoutColorOrTint() {
		GlyphTextRendererTest.draw("A", GlyphTextRendererTest.info().shadow(Color.BLACK).shadow(null).shadowTint(null));
		Assert.assertEquals(Arrays.asList("begin", "draw A", "end"), this.events);
	}

	@Test
	public void switchesTheFontThroughMarkup() {
		final GlyphLayout<Face> layout = GlyphTextRendererTest.layout("A^A^A", GlyphTextRendererTest.info().markups(GlyphTextRendererTest.FONT));
		Assert.assertSame(GlyphTextRendererTest.REGULAR, layout.getPlacements().get(0).getFace());
		Assert.assertSame(GlyphTextRendererTest.SPACED, layout.getPlacements().get(1).getFace());
		Assert.assertSame(GlyphTextRendererTest.REGULAR, layout.getPlacements().get(2).getFace());
		Assert.assertSame(GlyphTextRendererTest.OTHER, layout.getPlacements().get(1).getFont());
	}

	@Test
	public void handsEachGlyphItsFont() {
		final TextInfo info = GlyphTextRendererTest.info().markups(GlyphTextRendererTest.FONT);
		GlyphTextRendererTest.draw("A^A", info);
		Assert.assertSame(info.getFont(), GlyphTextRendererTest.RENDERER.drawn.get(0).getFont());
		Assert.assertSame(GlyphTextRendererTest.OTHER, GlyphTextRendererTest.RENDERER.drawn.get(1).getFont());
	}

	@Test
	public void keepsItsFontForAFontOfAnotherRenderer() {
		final ITextMarkup foreign = (text, index, style) -> {
			if (text.charAt(index) != '%') {
				return 0;
			}

			style.font(MsdfTextRenderer::inst);
			return 1;
		};
		final GlyphLayout<Face> layout = GlyphTextRendererTest.layout("%A", GlyphTextRendererTest.info().markups(foreign));
		Assert.assertSame(GlyphTextRendererTest.REGULAR, layout.getPlacements().get(0).getFace());
	}

	@Test
	public void writesTheColorUniformsOfAGlyph() {
		final RecordingShader shader = new RecordingShader();
		GlyphTextRendererTest.RENDERER.shader = shader;
		try {
			GlyphTextRendererTest.RENDERER.drawText(5D, 6D, "A", GlyphTextRendererTest.info().color(Color.RED), 1D, 2D, 300D, 40D);
			Assert.assertEquals(0, shader.getValues().get("u_HasGradient"));
			Assert.assertArrayEquals(new float[] {1F, 0F, 0F, 1F}, (float[]) shader.getValues().get("color"), 0F);
			GlyphTextRendererTest.RENDERER.drawText(5D, 6D, "A", GlyphTextRendererTest.info().color(Color.RED.toGradient(Color.BLUE)), 1D, 2D, 300D, 40D);
			Assert.assertEquals(1, shader.getValues().get("u_HasGradient"));
			Assert.assertArrayEquals(new float[] {0F, 0F, 1F, 1F}, (float[]) shader.getValues().get("u_GradientEnd"), 0F);
			Assert.assertArrayEquals(new float[] {1F, 2F, 301F, 42F}, (float[]) shader.getValues().get("u_GradientCanvas"), 0F);
		} finally {
			GlyphTextRendererTest.RENDERER.shader = null;
		}
	}
	@Test
	public void switchesTheFaceThroughMarkup() {
		final GlyphLayout<Face> layout = GlyphTextRendererTest.layout("*A*A", GlyphTextRendererTest.info());
		Assert.assertSame(GlyphTextRendererTest.BOLD, layout.getPlacements().get(0).getFace());
		Assert.assertSame(GlyphTextRendererTest.REGULAR, layout.getPlacements().get(1).getFace());
		Assert.assertEquals(15D, layout.getWidth(), 1E-6D);
	}

	@Test
	public void restartsKerningOnAFaceSwitch() {
		Assert.assertEquals(15D, GlyphTextRendererTest.info().getWidth("A*V"), 1E-6D);
	}

	@Test
	public void spansEachGlyphUpToTheNextOne() {
		GlyphTextRendererTest.draw("AV", GlyphTextRendererTest.info().letterSpacing(0.2F));
		Assert.assertEquals(6D, GlyphTextRendererTest.RENDERER.drawn.get(0).getAdvance(), 1E-6D);
		Assert.assertEquals(5D, GlyphTextRendererTest.RENDERER.drawn.get(1).getAdvance(), 1E-6D);
	}

	@Test
	public void colorsTheGlyphsThroughMarkup() {
		GlyphTextRendererTest.draw("A~A", GlyphTextRendererTest.info().color(Color.WHITE.copyAlpha(0.5F)));
		Assert.assertEquals(0.5F, GlyphTextRendererTest.RENDERER.drawn.get(0).getColor().a, 0F);
		Assert.assertEquals(Color.RED.r, GlyphTextRendererTest.RENDERER.drawn.get(1).getColor().r, 0F);
		Assert.assertEquals(0.5F, GlyphTextRendererTest.RENDERER.drawn.get(1).getColor().a, 0F);
	}

	@Test
	public void drawsMarkupAsTextWhenDisabled() {
		Assert.assertEquals(10D, GlyphTextRendererTest.info().markups().getWidth("*A"), 1E-6D);
	}

	@Test
	public void placesEveryGlyphOnTheBaseline() {
		GlyphTextRendererTest.draw("*A*A", GlyphTextRendererTest.info());
		Assert.assertEquals(10D, GlyphTextRendererTest.RENDERER.drawn.get(0).getBaseline(), 1E-6D);
		Assert.assertEquals(10D, GlyphTextRendererTest.RENDERER.drawn.get(1).getBaseline(), 1E-6D);
	}

	@Test
	public void pointsEachGlyphBackToItsSource() {
		final List<GlyphPlacement<Face>> placements = GlyphTextRendererTest.layout("*A\uD83D\uDE00B", GlyphTextRendererTest.info()).getPlacements();
		Assert.assertEquals(3, placements.size());
		Assert.assertEquals(1, placements.get(0).getIndex());
		Assert.assertEquals(0x1F600, placements.get(1).getCodepoint());
		Assert.assertEquals(4, placements.get(2).getIndex());
	}

	@Test
	public void measuresTheLineHeightForAnyText() {
		Assert.assertEquals(15D, GlyphTextRendererTest.RENDERER.getHeight("AV", GlyphTextRendererTest.info().lineHeight(1.5F)), 1E-6D);
		Assert.assertEquals(15D, GlyphTextRendererTest.RENDERER.getHeight("", GlyphTextRendererTest.info().lineHeight(1.5F)), 1E-6D);
	}

	@Test
	public void scalesTheLineHeightWithTheFontSize() {
		Assert.assertEquals(30D, GlyphTextRendererTest.info().fontSize(20F).lineHeight(1.5F).getHeight(), 1E-6D);
	}

	@Test
	public void centersTheGlyphsInTheLineHeight() {
		GlyphTextRendererTest.draw("A", GlyphTextRendererTest.info().lineHeight(1.6F));
		Assert.assertEquals(12D, GlyphTextRendererTest.RENDERER.drawn.get(0).getBaseline(), 1E-6D);
	}

	@Test
	public void keepsTheTextColorWhenNotColored() {
		GlyphTextRendererTest.draw("~A", GlyphTextRendererTest.info().color(Color.WHITE).colored(false));
		Assert.assertSame(Color.WHITE, GlyphTextRendererTest.RENDERER.drawn.get(0).getColor());
	}

	@Test
	public void measuresAdvancesKerningAndSpacing() {
		Assert.assertEquals(11D, GlyphTextRendererTest.info().letterSpacing(0.2F).getWidth("AV"), 1E-6D);
		Assert.assertEquals(10D, GlyphTextRendererTest.info().getWidth("AB"), 1E-6D);
	}

	@Test
	public void scalesTheLetterSpacingWithTheFontSize() {
		Assert.assertEquals(22D, GlyphTextRendererTest.info().fontSize(20F).letterSpacing(0.2F).getWidth("AV"), 1E-6D);
	}

	@Test
	public void skipsMissingGlyphsWithoutBreakingKerning() {
		final TextInfo info = GlyphTextRendererTest.info().letterSpacing(0.2F);
		Assert.assertEquals(info.getWidth("AV"), info.getWidth("A#V"), 1E-6D);
	}

	@Test
	public void advancesASpaceMissingFromTheFaceByAQuarterOfEm() {
		final TextInfo info = GlyphTextRendererTest.info();
		Assert.assertEquals(2.5D, info.getWidth(" "), 1E-6D);
		Assert.assertEquals(12.5D, info.getWidth("A B"), 1E-6D);
		Assert.assertEquals(7.5D, GlyphTextRendererTest.layout("A B", info).getPlacements().get(2).getX(), 1E-6D);
	}

	@Test
	public void advancesANonBreakingSpaceMissingFromTheFaceLikeAMissingSpace() {
		final TextInfo info = GlyphTextRendererTest.info().letterSpacing(0.2F);
		Assert.assertEquals(2.5D, info.getWidth(" "), 1E-6D);
		Assert.assertEquals(info.getWidth("A B"), info.getWidth("A B"), 1E-6D);
		Assert.assertEquals(3, GlyphTextRendererTest.layout("A B", info).getPlacements().size());
	}

	@Test
	public void advancesANonBreakingSpaceMissingFromTheFaceLikeTheSpaceOfTheFace() {
		final TextInfo info = GlyphTextRendererTest.info(GlyphTextRendererTest.SPACED);
		Assert.assertEquals(3D, info.getWidth(" "), 1E-6D);
		Assert.assertEquals(info.getWidth("A B"), info.getWidth("A B"), 1E-6D);
	}

	@Test
	public void namesTheRendererOfAFontItCannotDraw() {
		final ITextRenderer other = MsdfTextRenderer.inst();
		try {
			GlyphTextRendererTest.RENDERER.getWidth("A", TextInfo.create(() -> other, 10F));
			Assert.fail("A font of another renderer must be refused");
		} catch (final IllegalArgumentException expected) {
			Assert.assertTrue(expected.getMessage(), expected.getMessage().startsWith(Renderer.class.getName() + " cannot draw the font "));
			Assert.assertTrue(expected.getMessage(), expected.getMessage().endsWith(", it is drawn by " + other.getClass().getName() + ": draw it with info.getFont().getTextRenderer()"));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAGlyphFontOfAnotherRenderer() {
		MsdfTextRenderer.inst().getWidth("A", GlyphTextRendererTest.info());
	}

	@Test
	public void measuresAGridAlignedFontAtItsSize() {
		final TextInfo info = GlyphTextRendererTest.aligned(12F);
		this.render.scale(1.2676D, 1.2676D, 1D);
		Assert.assertArrayEquals(new double[] {12D, 14.4D}, new double[] {info.getWidth("AB"), info.getHeight()}, 1E-4D);
		Assert.assertArrayEquals(new double[] {20D, 24D}, new double[] {GlyphTextRendererTest.aligned(20F).getWidth("AB"), GlyphTextRendererTest.aligned(20F).getHeight()}, 1E-4D);
	}

	@Test
	public void drawsAGridAlignedFontAtTheSizeItMeasures() {
		final TextInfo info = GlyphTextRendererTest.aligned(10F).letterSpacing(0.25F);
		this.render.scale(0.75D, 0.75D, 1D);
		final FontBounds bounds = GlyphTextRendererTest.ALIGNED.drawText(0D, 0D, "AVB", info);
		Assert.assertEquals(info.getWidth("AVB"), bounds.getWidth(), 0D);
		Assert.assertEquals(info.getHeight(), bounds.getHeight(), 0D);
		for (final TextGlyph<Face> glyph : GlyphTextRendererTest.ALIGNED.drawn) {
			Assert.assertEquals(10D, glyph.getSize(), 0D);
		}
	}

	@Test
	public void snapsTheOriginOfAGridAlignedFontOnThePixelGrid() {
		final TextInfo info = GlyphTextRendererTest.aligned(8F).letterSpacing(0.3F);
		GlyphTextRendererTest.ALIGNED.drawText(0.3D, 0.4D, "AVB", info);
		final List<TextGlyph<Face>> drawn = GlyphTextRendererTest.ALIGNED.drawn;
		final List<GlyphPlacement<Face>> placements = GlyphTextRendererTest.ALIGNED.layout("AVB", info).getPlacements();
		for (int i = 0; i < drawn.size(); i++) {
			Assert.assertEquals(placements.get(i).getX(), drawn.get(i).getX(), 1E-6D);
			Assert.assertEquals(8D, drawn.get(i).getBaseline(), 1E-6D);
		}
	}

	@Test
	public void spacesTheGlyphsOfAGridAlignedFontEvenlyAtAFractionalScale() {
		this.render.scale(1.2676D, 1.2676D, 1D);
		GlyphTextRendererTest.ALIGNED.drawText(10.3D, 0.4D, "AAAA", GlyphTextRendererTest.aligned(16F));
		final List<TextGlyph<Face>> drawn = GlyphTextRendererTest.ALIGNED.drawn;
		final double origin = drawn.get(0).getX() * 1.2676D;
		Assert.assertEquals(Math.rint(origin), origin, 1E-5D);
		for (int i = 1; i < drawn.size(); i++) {
			Assert.assertEquals(8D, drawn.get(i).getX() - drawn.get(i - 1).getX(), 1E-6D);
		}
	}

	@Test
	public void keepsTheGlyphsOfAFontOffTheGridByDefault() {
		GlyphTextRendererTest.RENDERER.drawText(0.3D, 0.4D, "AB", GlyphTextRendererTest.info());
		Assert.assertEquals(0.3D, GlyphTextRendererTest.RENDERER.drawn.get(0).getX(), 1E-6D);
		Assert.assertEquals(5.3D, GlyphTextRendererTest.RENDERER.drawn.get(1).getX(), 1E-6D);
		Assert.assertEquals(10.4D, GlyphTextRendererTest.RENDERER.drawn.get(0).getBaseline(), 1E-6D);
	}

	@Test
	public void roundsTheEffectOffsetsOfAGridAlignedFont() {
		final ITextEffect shake = new ITextEffect() {

			@Override
			public void apply(final ITextGlyph glyph) {
				glyph.offset(0.4D, -0.6D);
			}

		};
		GlyphTextRendererTest.draw("A", GlyphTextRendererTest.aligned(8F).effects(shake));
		GlyphTextRendererTest.draw("A", GlyphTextRendererTest.info().effects(shake));
		Assert.assertEquals(0D, GlyphTextRendererTest.ALIGNED.drawn.get(0).getOffsetX(), 1E-6D);
		Assert.assertEquals(-1D, GlyphTextRendererTest.ALIGNED.drawn.get(0).getOffsetY(), 1E-6D);
		Assert.assertEquals(0.4D, GlyphTextRendererTest.RENDERER.drawn.get(0).getOffsetX(), 1E-6D);
		Assert.assertEquals(-0.6D, GlyphTextRendererTest.RENDERER.drawn.get(0).getOffsetY(), 1E-6D);
	}

	@Test
	public void keepsTheShadowOfAGridAlignedFontAtLeastOnePixelAway() {
		GlyphTextRendererTest.draw("A", GlyphTextRendererTest.aligned(8F).shadow(Color.BLACK).shadow(0.2F, -0.3F));
		GlyphTextRendererTest.draw("A", GlyphTextRendererTest.aligned(8F).shadow(Color.BLACK).shadow(2.6F, 0F));
		final List<TextGlyph<Face>> drawn = GlyphTextRendererTest.ALIGNED.drawn;
		Assert.assertEquals(drawn.get(1).getX() + 1D, drawn.get(0).getX(), 1E-6D);
		Assert.assertEquals(drawn.get(1).getBaseline() - 1D, drawn.get(0).getBaseline(), 1E-6D);
		Assert.assertEquals(drawn.get(3).getX() + 3D, drawn.get(2).getX(), 1E-6D);
		Assert.assertEquals(drawn.get(3).getBaseline(), drawn.get(2).getBaseline(), 1E-6D);
	}

	@Test
	public void leavesAGridAlignedFontOffTheGridUnderARotation() {
		this.render.rotate(30D, 0D, 0D, 1D);
		GlyphTextRendererTest.ALIGNED.drawText(0.3D, 0.4D, "A", GlyphTextRendererTest.aligned(8F).shadow(Color.BLACK).shadow(0.2F, 0.2F));
		final List<TextGlyph<Face>> drawn = GlyphTextRendererTest.ALIGNED.drawn;
		Assert.assertEquals(0.5D, drawn.get(0).getX(), 1E-6D);
		Assert.assertEquals(0.3D, drawn.get(1).getX(), 1E-6D);
		Assert.assertEquals(8.4D, drawn.get(1).getBaseline(), 1E-6D);
	}

	private static TextInfo info() {
		return GlyphTextRendererTest.info(GlyphTextRendererTest.REGULAR, GlyphTextRendererTest.BOLD, GlyphTextRendererTest.ITALIC);
	}

	private static TextInfo info(final Face... faces) {
		return TextInfo.create(new Font(FontFamily.of(faces)), 10F).markups(GlyphTextRendererTest.MARKUP);
	}

	private static TextInfo aligned(final float size) {
		return TextInfo.create(new Font(FontFamily.of(GlyphTextRendererTest.REGULAR, GlyphTextRendererTest.BOLD, GlyphTextRendererTest.ITALIC), GlyphTextRendererTest.ALIGNED), size).markups(GlyphTextRendererTest.MARKUP);
	}

	private static void draw(final String text, final TextInfo info) {
		info.getFont().getTextRenderer().drawText(0D, 0D, text, info);
	}

	private static GlyphLayout<Face> layout(final String text, final TextInfo info) {
		return GlyphTextRendererTest.RENDERER.layout(text, info);
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

		private final Renderer renderer;

		private Font(final FontFamily<Face> family) {
			this(family, GlyphTextRendererTest.RENDERER);
		}

		private Font(final FontFamily<Face> family, final Renderer renderer) {
			super(family);
			this.renderer = renderer;
		}

		@Override
		public ITextRenderer getTextRenderer() {
			return this.renderer;
		}

	}

	@RequiredArgsConstructor
	private static final class Renderer extends GlyphTextRenderer<Face> {

		private final boolean               aligned;
		private final List<double[]>        runs  = new ArrayList<>();
		private final List<TextGlyph<Face>> drawn = new ArrayList<>();

		private List<String>    events = new ArrayList<>();
		private RecordingShader shader;

		@Override
		protected void end() {
			this.events.add("end");
		}

		@Override
		protected void drawGlyph(final TextGlyph<Face> glyph) {
			this.drawn.add(glyph);
			this.events.add("draw " + (char) glyph.getCodepoint() + (glyph.isShadow() ? " shadow" : ""));
			if (this.shader != null) {
				super.uniformColor(this.shader, glyph.getColor());
			}
		}

		@Override
		protected void begin(final double runX, final double runY, final double runWidth, final double runHeight) {
			this.events.add("begin");
			this.runs.add(new double[] {runX, runY, runWidth, runHeight});
		}

		@Override
		protected boolean isGridAligned() {
			return this.aligned;
		}

	}

}