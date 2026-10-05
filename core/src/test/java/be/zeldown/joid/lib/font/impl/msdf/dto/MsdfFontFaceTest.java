package be.zeldown.joid.lib.font.impl.msdf.dto;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import be.zeldown.joid.lib.font.FontWeight;

public class MsdfFontFaceTest {

	@Test
	public void readsItsMetrics() {
		final MsdfFontFace face = MsdfFontFaceTest.face();
		Assert.assertEquals(1.25F, face.getLineHeight(), 0F);
		Assert.assertEquals(0.875F, face.getAscender(), 0F);
		Assert.assertEquals(-0.25F, face.getDescender(), 0F);
		Assert.assertEquals(-0.125F, face.getUnderlineY(), 0F);
		Assert.assertEquals(0.0625F, face.getUnderlineThickness(), 0F);
	}

	@Test
	public void packsAPairIntoOneKey() {
		Assert.assertNotEquals(MsdfFontFace.pair('A', 'V'), MsdfFontFace.pair('V', 'A'));
		Assert.assertEquals((long) 'A' << 32 | 'V', MsdfFontFace.pair('A', 'V'));
	}

	@Test
	public void answersForEveryCodepoint() {
		final MsdfFontFace face = MsdfFontFaceTest.face();
		Assert.assertTrue(face.hasGlyph('A'));
		Assert.assertEquals(0.625F, face.getAdvance('A'), 0F);
		Assert.assertFalse(face.hasGlyph('B'));
		Assert.assertNull(face.getGlyph('B'));
		Assert.assertEquals(0F, face.getAdvance('B'), 0F);
	}

	@Test
	public void kernsOnlyTheDeclaredPairs() {
		final MsdfFontFace face = MsdfFontFaceTest.face();
		Assert.assertEquals(-0.0625F, face.getKerning('A', 'V'), 0F);
		Assert.assertEquals(0F, face.getKerning('V', 'A'), 0F);
	}

	@Test
	public void restylesWithoutCopyingTheAtlas() {
		final MsdfFontFace face = MsdfFontFaceTest.face();
		final MsdfFontFace bold = face.style(FontWeight.BOLD, true);
		Assert.assertSame(FontWeight.REGULAR, face.getWeight());
		Assert.assertFalse(face.isItalic());
		Assert.assertSame(FontWeight.BOLD, bold.getWeight());
		Assert.assertTrue(bold.isItalic());
		Assert.assertSame(face.getTexture(), bold.getTexture());
		Assert.assertSame(face.getGlyphs(), bold.getGlyphs());
		Assert.assertSame(face.getKerningPairs(), bold.getKerningPairs());
		Assert.assertSame(face.getAtlas(), bold.getAtlas());
		Assert.assertSame(face.getMetrics(), bold.getMetrics());
	}

	private static MsdfFontFace face() {
		final Map<Long, Float> kerning = new HashMap<>();
		kerning.put(MsdfFontFace.pair('A', 'V'), -0.0625F);
		final MsdfGlyph glyph = new MsdfGlyph('A', 0.625F, new MsdfBounds(0F, 0F, 0.6F, 0.7F), new MsdfBounds(0F, 0F, 2F, 2F));
		return MsdfFontFace.create(new MsdfAtlas(4F, 32F, 2, 2), new MsdfMetrics(1.25F, 0.875F, -0.25F, -0.125F, 0.0625F), Collections.singletonMap((int) 'A', glyph), kerning, new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), FontWeight.REGULAR, false);
	}

}