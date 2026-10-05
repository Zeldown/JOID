package be.zeldown.joid.lib.font.dto.font;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import be.zeldown.joid.internal.JOID;

public class FontInfoTest {

	private static FontInfo info;

	@BeforeClass
	public static void load() throws Exception {
		try (InputStream stream = JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat-Regular/font.msdf")) {
			FontInfoTest.info = MsdfFile.read(stream).getFontInfo();
		}
	}

	@Test
	public void readsTheAtlasMetrics() {
		Assert.assertEquals("msdf", FontInfoTest.info.getAtlas().getType());
		Assert.assertTrue(FontInfoTest.info.getAtlas().getDistanceRange() > 0F);
		Assert.assertTrue(FontInfoTest.info.getAtlas().getSize() > 0F);
		Assert.assertTrue(FontInfoTest.info.getMetrics().getAscender() > 0F);
		Assert.assertTrue(FontInfoTest.info.getMetrics().getDescender() < 0F);
	}

	@Test
	public void readsEveryGlyph() {
		Assert.assertTrue(FontInfoTest.info.getGlyphMap().size() > 200);
		Assert.assertNotNull(FontInfoTest.info.getGlyphMap().get((int) 'A'));
		Assert.assertTrue(FontInfoTest.info.getGlyphMap().get((int) 'A').getAdvance() > 0F);
		Assert.assertNotNull(FontInfoTest.info.getGlyphMap().get((int) 'A').getPlaneBounds());
		Assert.assertNull(FontInfoTest.info.getGlyphMap().get((int) ' ').getPlaneBounds());
	}

	@Test
	public void readsTheKerningPairs() {
		Assert.assertTrue("The atlas must declare kerning pairs", FontInfoTest.info.getKerningMap().size() > 1000);
		Assert.assertTrue("AV must kern tighter", FontInfoTest.info.getKerning('A', 'V') < 0F);
		Assert.assertTrue("AW must kern tighter", FontInfoTest.info.getKerning('A', 'W') < 0F);
		Assert.assertTrue("To must kern tighter", FontInfoTest.info.getKerning('T', 'o') < 0F);
	}

	@Test
	public void ignoresUnknownPairs() {
		Assert.assertEquals(0F, FontInfoTest.info.getKerning('H', 'H'), 0F);
		Assert.assertEquals(0F, FontInfoTest.info.getKerning('o', 'o'), 0F);
		Assert.assertEquals(0F, FontInfoTest.info.getKerning(0, 0), 0F);
	}

	@Test
	public void keepsPositiveAndNegativePairs() {
		Assert.assertTrue("LT must kern tighter", FontInfoTest.info.getKerning('L', 'T') < -0.05F);
		Assert.assertTrue("AA must kern wider", FontInfoTest.info.getKerning('A', 'A') > 0F);
	}

}