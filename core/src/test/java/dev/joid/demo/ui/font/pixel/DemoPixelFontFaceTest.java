package dev.joid.demo.ui.font.pixel;

import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import dev.joid.internal.JOID;
import dev.joid.lib.font.FontWeight;

public class DemoPixelFontFaceTest {

	private static DemoPixelFontFace face;

	@BeforeClass
	public static void load() throws IOException {
		DemoPixelFontFaceTest.face = DemoPixelFontFace.create(ImageIO.read(JOID.class.getResourceAsStream("/assets/demo/fonts/Pixel/Pixel.png")));
	}

	@Test
	public void advancesEachGlyphByItsInkAndOneTexel() {
		Assert.assertEquals(6F / 8F, DemoPixelFontFaceTest.face.getAdvance('A'), 0F);
		Assert.assertEquals(6F / 8F, DemoPixelFontFaceTest.face.getAdvance('0'), 0F);
		Assert.assertEquals(5F / 8F, DemoPixelFontFaceTest.face.getAdvance('f'), 0F);
		Assert.assertEquals(4F / 8F, DemoPixelFontFaceTest.face.getAdvance('I'), 0F);
		Assert.assertEquals(3F / 8F, DemoPixelFontFaceTest.face.getAdvance('l'), 0F);
		Assert.assertEquals(2F / 8F, DemoPixelFontFaceTest.face.getAdvance('i'), 0F);
		Assert.assertEquals(2F / 8F, DemoPixelFontFaceTest.face.getAdvance('.'), 0F);
	}

	@Test
	public void advancesASpaceByHalfAnEm() {
		Assert.assertTrue(DemoPixelFontFaceTest.face.hasGlyph(' '));
		Assert.assertEquals(4F / 8F, DemoPixelFontFaceTest.face.getAdvance(' '), 0F);
	}

	@Test
	public void drawsThePrintableAsciiOnly() {
		Assert.assertTrue(DemoPixelFontFaceTest.face.hasGlyph('~'));
		Assert.assertFalse(DemoPixelFontFaceTest.face.hasGlyph(0x1F));
		Assert.assertFalse(DemoPixelFontFaceTest.face.hasGlyph(0x7F));
		Assert.assertFalse(DemoPixelFontFaceTest.face.hasGlyph('é'));
		Assert.assertEquals(0F, DemoPixelFontFaceTest.face.getAdvance('é'), 0F);
	}

	@Test
	public void ranksItsGlyphsInSixteenColumnsOfEightTexels() {
		Assert.assertEquals(0, DemoPixelFontFace.cellX(' '));
		Assert.assertEquals(0, DemoPixelFontFace.cellY(' '));
		Assert.assertEquals(8, DemoPixelFontFace.cellX('A'));
		Assert.assertEquals(16, DemoPixelFontFace.cellY('A'));
		Assert.assertEquals(112, DemoPixelFontFace.cellX('~'));
		Assert.assertEquals(40, DemoPixelFontFace.cellY('~'));
	}

	@Test
	public void keepsTheMetricsOfAnEightTexelEm() {
		Assert.assertEquals(7F / 8F, DemoPixelFontFaceTest.face.getAscender(), 0F);
		Assert.assertEquals(-2F / 8F, DemoPixelFontFaceTest.face.getDescender(), 0F);
		Assert.assertEquals(9F / 8F, DemoPixelFontFaceTest.face.getLineHeight(), 0F);
		Assert.assertEquals(-1F / 8F, DemoPixelFontFaceTest.face.getUnderlineY(), 0F);
		Assert.assertEquals(1F / 8F, DemoPixelFontFaceTest.face.getUnderlineThickness(), 0F);
		Assert.assertEquals(0F, DemoPixelFontFaceTest.face.getKerning('A', 'V'), 0F);
	}

	@Test
	public void isAnUprightRegularFace() {
		Assert.assertFalse(DemoPixelFontFaceTest.face.isItalic());
		Assert.assertSame(FontWeight.REGULAR, DemoPixelFontFaceTest.face.getWeight());
		Assert.assertEquals("Pixel", DemoPixelFontFaceTest.face.getName());
		Assert.assertNotNull(DemoPixelFontFaceTest.face.getTexture());
	}

}