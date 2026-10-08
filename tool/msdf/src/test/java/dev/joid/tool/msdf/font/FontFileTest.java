package dev.joid.tool.msdf.font;

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.tool.msdf.MsdfFonts;

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

	@Test
	public void fallsBackToARegularStyleWithoutStyleTables() {
		final FontFile file = FontFile.read(MsdfFonts.build(Collections.emptyMap()));
		Assert.assertTrue(file.getTables().isEmpty());
		Assert.assertEquals(400, file.getWeight());
		Assert.assertFalse(file.isItalic());
		Assert.assertEquals(1000, file.getUnitsPerEm());
	}

	@Test
	public void readsTheItalicFlagOfTheHeadTable() {
		final int[] head = new int[23];
		head[9] = 2048;
		head[22] = 2;
		final FontFile file = FontFile.read(MsdfFonts.build("head", head));
		Assert.assertTrue(file.isItalic());
		Assert.assertEquals(2048, file.getUnitsPerEm());
		Assert.assertEquals(400, file.getWeight());
	}

	@Test
	public void readsTheObliqueFlagOfTheOs2Table() {
		final int[] os2 = new int[32];
		os2[2] = 300;
		os2[31] = 1 << 9;
		final FontFile file = FontFile.read(MsdfFonts.build("OS/2", os2));
		Assert.assertTrue(file.isItalic());
		Assert.assertEquals(300, file.getWeight());
	}

}