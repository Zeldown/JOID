package dev.joid.msdf.font;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.msdf.MsdfFonts;

public class FontFileTest {

	@Test
	public void readsTheStyleOfATrueTypeFont() throws Exception {
		final FontFile file = FontFile.read(MsdfFonts.read(MsdfFonts.REGULAR));
		Assert.assertEquals(400, file.getWeight());
		Assert.assertFalse(file.isItalic());
		Assert.assertEquals(1000, file.getUnitsPerEm());
		Assert.assertTrue(file.getTables().containsKey("GPOS"));
	}

	@Test
	public void readsTheStyleOfAnOpenTypeFont() throws Exception {
		final FontFile file = FontFile.read(MsdfFonts.read(MsdfFonts.BOLD_ITALIC));
		Assert.assertEquals(700, file.getWeight());
		Assert.assertTrue(file.isItalic());
		Assert.assertTrue(file.getTables().containsKey("CFF "));
	}

	@Test
	public void readsTheFirstFontOfACollection() throws Exception {
		final FontFile file = FontFile.read(MsdfFonts.read(MsdfFonts.COLLECTION));
		Assert.assertEquals(400, file.getWeight());
		Assert.assertTrue(file.getTables().containsKey("glyf"));
	}

	@Test
	public void readsBigEndianNumbers() {
		final FontFile file = FontFile.read(new byte[] {0, 0, 0, 0, 0, 0, (byte) 0xFF, (byte) 0xFE, 0x12, 0x34, 0x56, 0x78});
		Assert.assertEquals(-2, file.signed(6));
		Assert.assertEquals(0xFFFE, file.unsigned(6));
		Assert.assertEquals(0x12345678, file.integer(8));
	}

}