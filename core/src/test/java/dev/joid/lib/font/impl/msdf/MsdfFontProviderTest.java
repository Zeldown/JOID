package dev.joid.lib.font.impl.msdf;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector4f;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.render.CapturingRenderBridge;
import dev.joid.lib.bridge.render.CapturingRenderBridge.Capture;
import dev.joid.lib.bridge.render.texture.TextureFilter;
import dev.joid.lib.bridge.render.texture.TextureWrap;
import dev.joid.lib.bridge.render.vertex.Primitive;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;
import dev.joid.lib.font.FontBounds;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.TextInfo;
import dev.joid.lib.font.effect.ITextEffect;
import dev.joid.lib.font.effect.ITextGlyph;
import dev.joid.lib.font.markup.ITextMarkup;

public class MsdfFontProviderTest {

	private static MsdfFont font;
	private static MsdfFontFace regular;
	private static MsdfFont sample;

	@Rule
	public final CapturingRenderBridge render = new CapturingRenderBridge(1920, 1080);

	@BeforeClass
	public static void load() {
		MsdfFontProviderTest.font = MsdfFontLoader.load(JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat/Montserrat-Regular.ttf"), JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat/Montserrat-Bold.ttf")).join();
		MsdfFontProviderTest.regular = MsdfFontProviderTest.font.getFace(FontWeight.REGULAR, false);
	}

	@BeforeClass
	public static void createTheSample() {
		MsdfFontProviderTest.sample = MsdfFont.create(MsdfFontProviderTest.face(4F, FontWeight.REGULAR), MsdfFontProviderTest.face(6F, FontWeight.BOLD));
	}

	@Test
	public void tightensKernedPairs() {
		final TextInfo info = MsdfFontProviderTest.info();
		final double pair = info.getWidth("AV");
		final double separate = info.getWidth("A") + info.getWidth("V");
		Assert.assertTrue("AV must be narrower than A plus V (" + pair + " vs " + separate + ")", pair < separate);
	}

	@Test
	public void measuresTheRequestedWeight() {
		final double regular = MsdfFontProviderTest.info().getWidth("Hello");
		final double bold = MsdfFontProviderTest.info().weight(FontWeight.BOLD).getWidth("Hello");
		Assert.assertTrue("Bold must be wider than regular (" + bold + " vs " + regular + ")", bold > regular);
		Assert.assertEquals(bold, MsdfFontProviderTest.info().weight(FontWeight.BLACK).getWidth("Hello"), 0D);
	}

	@Test
	public void accumulatesKerningOverAWord() {
		final TextInfo info = MsdfFontProviderTest.info();
		final float kerning = MsdfFontProviderTest.regular.getKerning('A', 'V') + MsdfFontProviderTest.regular.getKerning('V', 'A');
		Assert.assertEquals(info.getWidth("A") * 2D + info.getWidth("V") + kerning * 100F, info.getWidth("AVA"), 0.001D);
	}

	@Test
	public void keepsTheLineHeightOfTheFace() {
		Assert.assertEquals(MsdfFontProviderTest.regular.getLineHeight() * 100F, MsdfFontProviderTest.info().getHeight(), 0.001D);
	}

	@Test
	public void leavesUnkernedPairsUntouched() {
		final TextInfo info = MsdfFontProviderTest.info();
		Assert.assertEquals(info.getWidth("H") + info.getWidth("H"), info.getWidth("HH"), 0.001D);
	}

	@Test
	public void measuresTheExactKerningOffset() {
		final TextInfo info = MsdfFontProviderTest.info();
		final float kerning = MsdfFontProviderTest.regular.getKerning('A', 'V');
		Assert.assertEquals(info.getWidth("A") + info.getWidth("V") + kerning * 100F, info.getWidth("AV"), 0.001D);
	}

	@Test
	public void sharesOneProvider() {
		Assert.assertSame(MsdfFontProvider.inst(), MsdfFontProvider.inst());
		Assert.assertSame(MsdfFontProvider.inst(), MsdfFontProviderTest.sample.getFontProvider());
	}

	@Test
	public void drawsOneQuadPerGlyph() {
		final FontBounds bounds = MsdfFontProvider.inst().drawText(100D, 100D, "Ax", MsdfFontProviderTest.sampleInfo());
		Assert.assertEquals(2, this.render.getCaptures().size());
		for (final Capture capture : this.render.getCaptures()) {
			Assert.assertSame(Primitive.TRIANGLES, capture.getPrimitive());
			Assert.assertEquals(6, capture.getCount());
			Assert.assertTrue(capture.isTexture());
		}
		Assert.assertEquals(42.5D, bounds.getWidth(), 1E-9D);
		Assert.assertEquals(50D, bounds.getHeight(), 1E-9D);
	}

	@Test
	public void placesAGlyphOnItsPlaneBounds() {
		MsdfFontProvider.inst().drawText(100D, 100D, "Ax", MsdfFontProviderTest.sampleInfo());
		final Capture capture = this.render.getCaptures().get(0);
		Assert.assertEquals(103.125D, capture.getLeft(), 1E-3D);
		Assert.assertEquals(121.875D, capture.getRight(), 1E-3D);
		Assert.assertEquals(110.625D, capture.getTop(), 1E-3D);
		Assert.assertEquals(144.375D, capture.getBottom(), 1E-3D);
	}

	@Test
	public void kernsTheNextGlyph() {
		MsdfFontProvider.inst().drawText(100D, 100D, "Ax", MsdfFontProviderTest.sampleInfo());
		final Capture capture = this.render.getCaptures().get(1);
		Assert.assertEquals(123.125D, capture.getLeft(), 1E-3D);
		Assert.assertEquals(141.875D, capture.getRight(), 1E-3D);
		Assert.assertEquals(117.5D, capture.getTop(), 1E-3D);
		Assert.assertEquals(139.375D, capture.getBottom(), 1E-3D);
	}

	@Test
	public void readsTheGlyphInsideTheAtlas() {
		MsdfFontProvider.inst().drawText(100D, 100D, "A", MsdfFontProviderTest.sampleInfo());
		final Capture capture = this.render.getCaptures().get(0);
		Assert.assertEquals(144.375F, capture.getY(0), 1E-3F);
		Assert.assertEquals(8.5F / 64F, capture.getU(0), 1E-6F);
		Assert.assertEquals(1F - 8.5F / 64F, capture.getV(0), 1E-6F);
		Assert.assertEquals(23.5F / 64F, capture.getU(2), 1E-6F);
		Assert.assertEquals(1F - 31.5F / 64F, capture.getV(2), 1E-6F);
	}

	@Test
	public void advancesOverAGlyphWithoutOutline() {
		MsdfFontProvider.inst().drawText(100D, 100D, "A x", MsdfFontProviderTest.sampleInfo());
		Assert.assertEquals(2, this.render.getCaptures().size());
		Assert.assertEquals(135.625D, this.render.getLast().getLeft(), 1E-3D);
	}

	@Test
	public void skipsACharacterMissingFromTheFace() {
		MsdfFontProvider.inst().drawText(100D, 100D, "AZx", MsdfFontProviderTest.sampleInfo());
		Assert.assertEquals(2, this.render.getCaptures().size());
		Assert.assertEquals(123.125D, this.render.getLast().getLeft(), 1E-3D);
	}

	@Test
	public void drawsNothingForAnEmptyText() {
		final FontBounds bounds = MsdfFontProvider.inst().drawText(100D, 100D, "", MsdfFontProviderTest.sampleInfo());
		Assert.assertEquals(0D, bounds.getWidth(), 0D);
		Assert.assertEquals(0D, bounds.getHeight(), 0D);
		Assert.assertTrue(this.render.getCaptures().isEmpty());
		MsdfFontProvider.inst().drawText(100D, 100D, "  ", MsdfFontProviderTest.sampleInfo());
		Assert.assertTrue(this.render.getCaptures().isEmpty());
	}

	@Test
	public void bindsTheAtlasOfTheFace() {
		final MsdfFontFace face = MsdfFontProviderTest.sample.getFace(FontWeight.REGULAR, false);
		MsdfFontProvider.inst().drawText(100D, 100D, "A", MsdfFontProviderTest.sampleInfo());
		final Capture capture = this.render.getLast();
		Assert.assertSame(face.getTexture().getTexture(), capture.getState().getTexture());
		Assert.assertSame(TextureWrap.CLAMP_TO_EDGE, capture.getState().getTextureWrap());
		Assert.assertSame(TextureFilter.LINEAR, capture.getState().getTextureFilter());
		Assert.assertArrayEquals(new float[] {1F / 64F, 1F / 64F}, (float[]) capture.getUniforms().get("texel"), 0F);
		Assert.assertEquals(4F, (Float) capture.getUniforms().get("pxRange"), 0F);
	}

	@Test
	public void sendsTheColorOfTheText() {
		MsdfFontProvider.inst().drawText(100D, 100D, "A", MsdfFontProviderTest.sampleInfo());
		final Capture capture = this.render.getLast();
		Assert.assertArrayEquals(new float[] {0.2F, 0.4F, 0.6F, 1F}, (float[]) capture.getUniforms().get("color"), 0F);
		Assert.assertEquals(0, capture.getUniforms().get("u_HasGradient"));
	}

	@Test
	public void releasesTheShaderAfterTheText() {
		MsdfFontProvider.inst().drawText(100D, 100D, "Ax", MsdfFontProviderTest.sampleInfo());
		Assert.assertNotNull(this.render.getLast().getState().getShader());
		Assert.assertNull(this.render.getShader());
	}

	@Test
	public void sendsTheScreenSizeOfAnAtlasPixel() {
		MsdfFontProvider.inst().drawText(100D, 100D, "A", MsdfFontProviderTest.sampleInfo());
		Assert.assertArrayEquals(new float[] {0.0125F, 0.0125F}, (float[]) this.render.getLast().getUniforms().get("pixel"), 1E-6F);
		this.render.resize(1366, 768);
		this.render.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		MsdfFontProvider.inst().drawText(100D, 100D, "A", MsdfFontProviderTest.sampleInfo());
		Assert.assertArrayEquals(new float[] {0.0125F * 1920F / 1366F, 1F / 56F}, (float[]) this.render.getLast().getUniforms().get("pixel"), 1E-6F);
	}

	@Test
	public void snapsTheBaselineAndTheXHeightToTheWindowPixels() {
		this.render.resize(1366, 768);
		this.render.ortho(0D, 1920D, 1080D, 0D, 0D, 10000D);
		MsdfFontProvider.inst().drawText(100D, 100D, "x", MsdfFontProviderTest.sampleInfo());
		final Capture capture = this.render.getLast();
		Assert.assertEquals(100D, (capture.getBottom() + 0.015625D * 39.375D) * 768D / 1080D, 1E-3D);
		Assert.assertEquals(0.546875D * 39.375D, capture.getBottom() - capture.getTop(), 1E-3D);
		Assert.assertEquals(18.75D, capture.getRight() - capture.getLeft(), 1E-3D);
	}

	@Test
	public void keepsTheExactSizeUnderARotation() {
		this.render.pushMatrix();
		try {
			this.render.rotate(30D, 0D, 0D, 1D);
			MsdfFontProvider.inst().drawText(100D, 100D, "x", MsdfFontProviderTest.sampleInfo());
		} finally {
			this.render.popMatrix();
		}

		final Capture capture = this.render.getLast();
		Assert.assertEquals(117.5D, capture.getTop(), 1E-4D);
		Assert.assertEquals(139.375D, capture.getBottom(), 1E-4D);
	}

	@Test
	public void slantsAnItalicTextWithoutItalicFace() {
		MsdfFontProvider.inst().drawText(100D, 100D, "A", MsdfFontProviderTest.sampleInfo().italic(true));
		final Capture capture = this.render.getLast();
		Assert.assertEquals(102.25F, capture.getX(0), 1E-3F);
		Assert.assertEquals(121F, capture.getX(1), 1E-3F);
		Assert.assertEquals(127.75F, capture.getX(2), 1E-3F);
		Assert.assertEquals(109F, capture.getX(5), 1E-3F);
	}

	@Test
	public void drawsAnItalicFaceUpright() {
		final MsdfFontFace face = MsdfFontProviderTest.face(4F, FontWeight.REGULAR);
		MsdfFontProvider.inst().drawText(100D, 100D, "A", TextInfo.create(MsdfFont.create(face, face.style(FontWeight.REGULAR, true)), 40F).italic(true).markups());
		Assert.assertEquals(103.125D, this.render.getLast().getLeft(), 1E-3D);
		Assert.assertEquals(121.875D, this.render.getLast().getRight(), 1E-3D);
	}

	@Test
	public void drawsTheRequestedWeight() {
		final MsdfFontFace bold = MsdfFontProviderTest.sample.getFace(FontWeight.BOLD, false);
		MsdfFontProvider.inst().drawText(100D, 100D, "A", MsdfFontProviderTest.sampleInfo().weight(FontWeight.BOLD));
		final Capture capture = this.render.getLast();
		Assert.assertEquals(6F, (Float) capture.getUniforms().get("pxRange"), 0F);
		Assert.assertSame(bold.getTexture().getTexture(), capture.getState().getTexture());
	}

	@Test
	public void switchesTheFaceWithAMarkup() {
		final ITextMarkup markup = (text, index, style) -> {
			if (text.charAt(index) != '*') {
				return 0;
			}
			style.weight(FontWeight.BOLD);
			return 1;
		};
		MsdfFontProvider.inst().drawText(100D, 100D, "A*A", MsdfFontProviderTest.sampleInfo().markups(markup));
		final List<Capture> captures = this.render.getCaptures();
		Assert.assertEquals(2, captures.size());
		Assert.assertEquals(4F, (Float) captures.get(0).getUniforms().get("pxRange"), 0F);
		Assert.assertEquals(6F, (Float) captures.get(1).getUniforms().get("pxRange"), 0F);
		Assert.assertNotSame(captures.get(0).getState().getTexture(), captures.get(1).getState().getTexture());
	}

	@Test
	public void colorsTheGlyphsAfterAMarkup() {
		final ITextMarkup markup = (text, index, style) -> {
			if (text.charAt(index) != '#') {
				return 0;
			}
			style.color(new Color(0.1F, 0.9F, 0.3F, 1F));
			return 1;
		};
		MsdfFontProvider.inst().drawText(100D, 100D, "A#x", MsdfFontProviderTest.sampleInfo().markups(markup));
		final List<Capture> captures = this.render.getCaptures();
		Assert.assertArrayEquals(new float[] {0.2F, 0.4F, 0.6F, 1F}, (float[]) captures.get(0).getUniforms().get("color"), 0F);
		Assert.assertArrayEquals(new float[] {0.1F, 0.9F, 0.3F, 1F}, (float[]) captures.get(1).getUniforms().get("color"), 0F);
	}

	@Test
	public void paintsAGradientOverTheRun() {
		final Color gradient = new Color(0.2F, 0.4F, 0.6F, 1F).toGradient(new Color(0.8F, 0.6F, 0.4F, 1F), new Vector4f(0F, 0F, 1F, 1F));
		MsdfFontProvider.inst().drawText(100D, 100D, "Ax", MsdfFontProviderTest.sampleInfo().color(gradient));
		final Map<String, Object> uniforms = this.render.getLast().getUniforms();
		Assert.assertEquals(1, uniforms.get("u_HasGradient"));
		Assert.assertArrayEquals(new float[] {0.2F, 0.4F, 0.6F, 1F}, (float[]) uniforms.get("u_GradientStart"), 0F);
		Assert.assertArrayEquals(new float[] {0.8F, 0.6F, 0.4F, 1F}, (float[]) uniforms.get("u_GradientEnd"), 0F);
		Assert.assertArrayEquals(new float[] {0F, 0F}, (float[]) uniforms.get("u_GradientStartPos"), 0F);
		Assert.assertArrayEquals(new float[] {1F, 1F}, (float[]) uniforms.get("u_GradientEndPos"), 0F);
		Assert.assertArrayEquals(new float[] {100F, 100F, 142.5F, 150F}, (float[]) uniforms.get("u_GradientCanvas"), 1E-4F);
	}

	@Test
	public void spreadsTheGradientOverTheGivenRun() {
		final Color gradient = Color.BLUE.toGradient(Color.GREEN);
		MsdfFontProvider.inst().drawText(100D, 100D, "Ax", MsdfFontProviderTest.sampleInfo().color(gradient), 50D, 60D, 300D, 80D);
		Assert.assertArrayEquals(new float[] {50F, 60F, 350F, 140F}, (float[]) this.render.getLast().getUniforms().get("u_GradientCanvas"), 0F);
		Assert.assertEquals(103.125D, this.render.getCaptures().get(0).getLeft(), 1E-3D);
	}

	@Test
	public void dropsTheShadowBeforeTheText() {
		MsdfFontProvider.inst().drawText(100D, 100D, "A", MsdfFontProviderTest.sampleInfo().shadow(new Color(0.1F, 0.1F, 0.1F, 1F)).shadow(4F, 6F));
		final List<Capture> captures = this.render.getCaptures();
		Assert.assertEquals(2, captures.size());
		Assert.assertEquals(107.125D, captures.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(116.625D, captures.get(0).getTop(), 1E-3D);
		Assert.assertArrayEquals(new float[] {0.1F, 0.1F, 0.1F, 1F}, (float[]) captures.get(0).getUniforms().get("color"), 0F);
		Assert.assertEquals(103.125D, captures.get(1).getLeft(), 1E-3D);
		Assert.assertEquals(110.625D, captures.get(1).getTop(), 1E-3D);
		Assert.assertArrayEquals(new float[] {0.2F, 0.4F, 0.6F, 1F}, (float[]) captures.get(1).getUniforms().get("color"), 0F);
	}

	@Test
	public void shiftsTheGradientOfTheShadowWithIt() {
		MsdfFontProvider.inst().drawText(100D, 100D, "A", MsdfFontProviderTest.sampleInfo().shadow(Color.BLUE.toGradient(Color.GREEN)).shadow(4F, 6F));
		final List<Capture> captures = this.render.getCaptures();
		Assert.assertEquals(1, captures.get(0).getUniforms().get("u_HasGradient"));
		Assert.assertArrayEquals(new float[] {104F, 106F, 129F, 156F}, (float[]) captures.get(0).getUniforms().get("u_GradientCanvas"), 1E-4F);
		Assert.assertEquals(0, captures.get(1).getUniforms().get("u_HasGradient"));
	}

	@Test
	public void spacesTheLettersByAFractionOfTheSize() {
		final FontBounds bounds = MsdfFontProvider.inst().drawText(100D, 100D, "Ax", MsdfFontProviderTest.sampleInfo().letterSpacing(0.1F));
		Assert.assertEquals(127.125D, this.render.getLast().getLeft(), 1E-3D);
		Assert.assertEquals(46.5D, bounds.getWidth(), 1E-4D);
	}

	@Test
	public void movesAGlyphOffsetByAnEffect() {
		MsdfFontProvider.inst().drawText(100D, 100D, "A", MsdfFontProviderTest.sampleInfo().effects(new ITextEffect() {

			@Override
			public void apply(final ITextGlyph glyph) {
				glyph.offset(3D, 5.5D);
			}

		}));
		Assert.assertEquals(106.125D, this.render.getLast().getLeft(), 1E-3D);
		Assert.assertEquals(116.125D, this.render.getLast().getTop(), 1E-3D);
	}

	@Test
	public void drawsTheCharacterChosenByAnEffect() {
		MsdfFontProvider.inst().drawText(100D, 100D, "AA", MsdfFontProviderTest.sampleInfo().effects(new ITextEffect() {

			@Override
			public void apply(final ITextGlyph glyph) {
				glyph.codepoint(glyph.getIndex() == 0 ? 'x' : 'Z');
			}

		}));
		Assert.assertEquals(1, this.render.getCaptures().size());
		Assert.assertEquals(117.5D, this.render.getLast().getTop(), 1E-3D);
	}

	@Test
	public void recolorsAGlyphFromAnEffect() {
		MsdfFontProvider.inst().drawText(100D, 100D, "A", MsdfFontProviderTest.sampleInfo().effects(new ITextEffect() {

			@Override
			public void apply(final ITextGlyph glyph) {
				glyph.color(new Color(0.1F, 0.9F, 0.3F, 1F));
			}

		}));
		Assert.assertArrayEquals(new float[] {0.1F, 0.9F, 0.3F, 1F}, (float[]) this.render.getLast().getUniforms().get("color"), 0F);
	}

	@Test
	public void drawsTheBackgroundAndTheDecorationAroundTheGlyphs() {
		MsdfFontProvider.inst().drawText(100D, 100D, "A", MsdfFontProviderTest.sampleInfo().effects(new ITextEffect() {

			@Override
			public void background(final ITextGlyph glyph) {
				DrawUtils.SHAPE.drawRect(glyph.getX(), glyph.getBaseline() - glyph.getAscender(), glyph.getAdvance(), glyph.getAscender(), new Color(0.9F, 0.9F, 0.1F, 1F));
			}

			@Override
			public void decorate(final ITextGlyph glyph) {
				DrawUtils.SHAPE.drawRect(glyph.getX(), glyph.getUnderlineY(), glyph.getAdvance(), glyph.getUnderlineThickness(), new Color(0.1F, 0.9F, 0.9F, 1F));
			}

		}));
		final List<Capture> captures = this.render.getCaptures();
		Assert.assertEquals(3, captures.size());
		Assert.assertEquals(0.9F, captures.get(0).getState().getRed(), 0F);
		Assert.assertEquals(100D, captures.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(125D, captures.get(0).getRight(), 1E-3D);
		Assert.assertEquals(100D, captures.get(0).getTop(), 1E-3D);
		Assert.assertTrue(captures.get(1).isTexture());
		Assert.assertEquals(0.1F, captures.get(2).getState().getRed(), 0F);
		Assert.assertNull(captures.get(2).getState().getShader());
		Assert.assertEquals(145D, captures.get(2).getTop(), 1E-3D);
	}

	@Test
	public void drawsARealTextGlyphByGlyph() {
		final TextInfo info = MsdfFontProviderTest.info();
		final FontBounds bounds = MsdfFontProvider.inst().drawText(100D, 100D, "Hello", info);
		final List<Capture> captures = this.render.getCaptures();
		Assert.assertEquals(5, captures.size());
		for (int i = 1; i < captures.size(); i++) {
			Assert.assertTrue(captures.get(i).getLeft() > captures.get(i - 1).getLeft());
		}
		Assert.assertEquals(info.getWidth("Hello"), bounds.getWidth(), 0D);
		Assert.assertEquals(info.getHeight(), bounds.getHeight(), 0D);
		Assert.assertSame(MsdfFontProviderTest.regular.getTexture().getTexture(), captures.get(0).getState().getTexture());
	}

	@Test
	public void setsRealGlyphsOnOneBaseline() {
		MsdfFontProvider.inst().drawText(100D, 100D, "Hxo", MsdfFontProviderTest.info());
		final List<Capture> captures = this.render.getCaptures();
		Assert.assertEquals(captures.get(0).getBottom(), captures.get(1).getBottom(), 1E-3D);
		Assert.assertTrue(captures.get(2).getBottom() > captures.get(0).getBottom());
		Assert.assertTrue(captures.get(0).getTop() < captures.get(1).getTop());
	}

	@Test
	public void drawsTheBoldFaceOfARealFont() {
		MsdfFontProvider.inst().drawText(100D, 100D, "H", MsdfFontProviderTest.info());
		MsdfFontProvider.inst().drawText(100D, 100D, "H", MsdfFontProviderTest.info().weight(FontWeight.BOLD));
		final Capture regular = this.render.getCaptures().get(0);
		final Capture bold = this.render.getCaptures().get(1);
		Assert.assertTrue(bold.getRight() - bold.getLeft() > regular.getRight() - regular.getLeft());
		Assert.assertSame(MsdfFontProviderTest.font.getFace(FontWeight.BOLD, false).getTexture().getTexture(), bold.getState().getTexture());
	}

	@Test
	public void spacesRealLettersApart() {
		MsdfFontProvider.inst().drawText(100D, 100D, "Hello", MsdfFontProviderTest.info());
		MsdfFontProvider.inst().drawText(100D, 100D, "Hello", MsdfFontProviderTest.info().letterSpacing(-0.02F));
		for (int i = 0; i < 5; i++) {
			Assert.assertEquals(this.render.getCaptures().get(i).getLeft() - 2D * i, this.render.getCaptures().get(i + 5).getLeft(), 1E-3D);
		}
	}

	@Test
	public void dropsTheShadowOfARealText() {
		MsdfFontProvider.inst().drawText(100D, 100D, "Hi", MsdfFontProviderTest.info().shadow().shadow(3F, 4F));
		final List<Capture> captures = this.render.getCaptures();
		Assert.assertEquals(4, captures.size());
		Assert.assertEquals(captures.get(2).getLeft() + 3D, captures.get(0).getLeft(), 1E-3D);
		Assert.assertEquals(captures.get(2).getBottom() + 4D, captures.get(0).getBottom(), 1E-3D);
	}

	private static TextInfo info() {
		return TextInfo.create(MsdfFontProviderTest.font, 100F, Color.WHITE);
	}

	private static TextInfo sampleInfo() {
		return TextInfo.create(MsdfFontProviderTest.sample, 40F, new Color(0.2F, 0.4F, 0.6F, 1F)).markups();
	}

	private static MsdfFontFace face(final float range, final FontWeight weight) {
		final Map<Integer, MsdfGlyph> glyphs = new HashMap<>();
		glyphs.put((int) 'A', new MsdfGlyph('A', 0.625F, new MsdfBounds(0.0625F, -0.125F, 0.5625F, 0.75F), new MsdfBounds(8F, 8F, 24F, 32F)));
		glyphs.put((int) 'x', new MsdfGlyph('x', 0.5F, new MsdfBounds(0F, 0F, 0.5F, 0.578125F), new MsdfBounds(32F, 8F, 48F, 24F)));
		glyphs.put((int) ' ', new MsdfGlyph(' ', 0.25F, null, null));
		return MsdfFontFace.create(new MsdfAtlas(64, 64, 32F, range), new MsdfMetrics(1.25F, 1F, -0.25F, -0.125F, 0.0625F), glyphs, Collections.singletonMap(MsdfFontFace.pair('A', 'x'), -0.0625F), new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB), "Sample", weight, false);
	}

}