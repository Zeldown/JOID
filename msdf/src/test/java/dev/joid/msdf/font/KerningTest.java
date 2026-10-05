package dev.joid.msdf.font;

import java.awt.Font;

import org.junit.Assert;
import org.junit.Test;

import dev.joid.msdf.MsdfFonts;

public class KerningTest {

	@Test
	public void readsThePairsOfTheGposTable() throws Exception {
		final byte[] data = MsdfFonts.read(MsdfFonts.REGULAR);
		final Font font = Glyphs.load(data);
		final Kerning kerning = Kerning.read(FontFile.read(data), font, new int[] {'A', 'V', 'x'});
		Assert.assertEquals(1000, kerning.getUnitsPerEm());
		Assert.assertEquals(1, kerning.getKerning().size());
		Assert.assertEquals(Integer.valueOf(-80), kerning.getKerning().get((long) 'A' << 32 | 'V'));
	}

	@Test
	public void keepsOnlyThePairsOfTheCharset() throws Exception {
		final byte[] data = MsdfFonts.read(MsdfFonts.BOLD_ITALIC);
		Assert.assertTrue(Kerning.read(FontFile.read(data), Glyphs.load(data), new int[] {'A', 'x'}).getKerning().isEmpty());
	}

}