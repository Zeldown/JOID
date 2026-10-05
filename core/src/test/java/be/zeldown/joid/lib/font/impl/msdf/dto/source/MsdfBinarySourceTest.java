package be.zeldown.joid.lib.font.impl.msdf.dto.source;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.font.FontWeight;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfBounds;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfFace;

public class MsdfBinarySourceTest {

	private static MsdfFace face;

	@BeforeClass
	public static void read() throws IOException {
		MsdfBinarySourceTest.face = MsdfBinarySource.of(JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat-Regular/font.msdf")).read();
	}

	@Test
	public void readsTheAtlasMetrics() {
		Assert.assertTrue(MsdfBinarySourceTest.face.getAtlas().getDistanceRange() > 0F);
		Assert.assertTrue(MsdfBinarySourceTest.face.getAtlas().getSize() > 0F);
		Assert.assertTrue(MsdfBinarySourceTest.face.getAtlas().getWidth() > 0);
		Assert.assertTrue(MsdfBinarySourceTest.face.getMetrics().getAscender() > 0F);
		Assert.assertTrue(MsdfBinarySourceTest.face.getMetrics().getDescender() < 0F);
	}

	@Test
	public void readsTheStyle() throws IOException {
		Assert.assertSame(FontWeight.REGULAR, MsdfBinarySourceTest.face.getWeight());
		Assert.assertFalse(MsdfBinarySourceTest.face.isItalic());
		Assert.assertSame(FontWeight.BLACK, MsdfBinarySource.of(JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat-Black/font.msdf")).read().getWeight());
	}

	@Test
	public void overridesTheStyle() throws IOException {
		final MsdfFace face = MsdfBinarySource.of(JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat-Regular/font.msdf")).weight(FontWeight.LIGHT).italic(true).read();
		Assert.assertSame(FontWeight.LIGHT, face.getWeight());
		Assert.assertTrue(face.isItalic());
		Assert.assertEquals(MsdfBinarySourceTest.face.getGlyphs().size(), face.getGlyphs().size());
	}

	@Test
	public void readsEveryGlyph() {
		Assert.assertTrue(MsdfBinarySourceTest.face.getGlyphs().size() > 200);
		Assert.assertNotNull(MsdfBinarySourceTest.face.getGlyph('A'));
		Assert.assertTrue(MsdfBinarySourceTest.face.getGlyph('A').getAdvance() > 0F);
		Assert.assertNotNull(MsdfBinarySourceTest.face.getGlyph('A').getPlaneBounds());
		Assert.assertNull(MsdfBinarySourceTest.face.getGlyph(' ').getPlaneBounds());
		Assert.assertNull(MsdfBinarySourceTest.face.getGlyph(0x4E00));
	}

	@Test
	public void placesTheAtlasBoundsOnTexelEdges() {
		final MsdfBounds bounds = MsdfBinarySourceTest.face.getGlyph('A').getAtlasBounds();
		Assert.assertEquals(Math.rint(bounds.getLeft()), bounds.getLeft(), 0F);
		Assert.assertEquals(Math.rint(bounds.getBottom()), bounds.getBottom(), 0F);
		Assert.assertEquals(Math.rint(bounds.getRight()), bounds.getRight(), 0F);
		Assert.assertEquals(Math.rint(bounds.getTop()), bounds.getTop(), 0F);
		Assert.assertTrue(bounds.getRight() > bounds.getLeft() && bounds.getTop() > bounds.getBottom());
	}

	@Test
	public void readsTheKerningPairs() {
		Assert.assertTrue("The atlas must declare kerning pairs", MsdfBinarySourceTest.face.getKerningPairs().size() > 1000);
		Assert.assertTrue("AV must kern tighter", MsdfBinarySourceTest.face.getKerning('A', 'V') < 0F);
		Assert.assertTrue("AW must kern tighter", MsdfBinarySourceTest.face.getKerning('A', 'W') < 0F);
		Assert.assertTrue("To must kern tighter", MsdfBinarySourceTest.face.getKerning('T', 'o') < 0F);
	}

	@Test
	public void ignoresUnknownPairs() {
		Assert.assertEquals(0F, MsdfBinarySourceTest.face.getKerning('H', 'H'), 0F);
		Assert.assertEquals(0F, MsdfBinarySourceTest.face.getKerning('o', 'o'), 0F);
		Assert.assertEquals(0F, MsdfBinarySourceTest.face.getKerning(0, 0), 0F);
	}

	@Test
	public void keepsPositiveAndNegativePairs() {
		Assert.assertTrue("LT must kern tighter", MsdfBinarySourceTest.face.getKerning('L', 'T') < -0.05F);
		Assert.assertTrue("AA must kern wider", MsdfBinarySourceTest.face.getKerning('A', 'A') > 0F);
	}

	@Test(expected = IOException.class)
	public void refusesAForeignFile() throws IOException {
		MsdfBinarySource.of(new ByteArrayInputStream("not a joid font".getBytes(StandardCharsets.UTF_8))).read();
	}

}