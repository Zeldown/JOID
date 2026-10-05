package dev.joid.lib.font.impl.msdf.dto.source;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.DeflaterOutputStream;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import dev.joid.internal.JOID;
import dev.joid.lib.asset.Asset;
import dev.joid.lib.font.FontWeight;
import dev.joid.lib.font.impl.msdf.MsdfFontCache;
import dev.joid.lib.font.impl.msdf.dto.MsdfBounds;
import dev.joid.lib.font.impl.msdf.dto.MsdfFontFace;
import dev.joid.lib.resource.Resource;

public class MsdfBinarySourceTest {

	private static final int[][] PIXELS = {
			{10, 200, 30, 255, 0, 128},
			{12, 190, 40, 250, 5, 120},
			{90, 60, 30, 20, 10, 0},
			{1, 2, 3, 250, 251, 252},
			{128, 64, 32, 16, 8, 4}
	};

	private static MsdfFontFace face;

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
	public void ignoresUnknownPairs() {
		Assert.assertEquals(0F, MsdfBinarySourceTest.face.getKerning('H', 'H'), 0F);
		Assert.assertEquals(0F, MsdfBinarySourceTest.face.getKerning('o', 'o'), 0F);
		Assert.assertEquals(0F, MsdfBinarySourceTest.face.getKerning(0, 0), 0F);
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
	public void readsTheKerningPairs() {
		Assert.assertTrue("The atlas must declare kerning pairs", MsdfBinarySourceTest.face.getKerningPairs().size() > 1000);
		Assert.assertTrue("AV must kern tighter", MsdfBinarySourceTest.face.getKerning('A', 'V') < 0F);
		Assert.assertTrue("AW must kern tighter", MsdfBinarySourceTest.face.getKerning('A', 'W') < 0F);
		Assert.assertTrue("To must kern tighter", MsdfBinarySourceTest.face.getKerning('T', 'o') < 0F);
	}

	@Test
	public void keepsPositiveAndNegativePairs() {
		Assert.assertTrue("LT must kern tighter", MsdfBinarySourceTest.face.getKerning('L', 'T') < -0.05F);
		Assert.assertTrue("AA must kern wider", MsdfBinarySourceTest.face.getKerning('A', 'A') > 0F);
	}

	@BeforeClass
	public static void read() throws IOException {
		MsdfBinarySourceTest.face = MsdfBinarySourceTest.montserrat("Regular").read();
	}

	@Test
	public void readsTheStyle() throws IOException {
		Assert.assertSame(FontWeight.REGULAR, MsdfBinarySourceTest.face.getWeight());
		Assert.assertFalse(MsdfBinarySourceTest.face.isItalic());
		Assert.assertEquals("Montserrat Regular", MsdfBinarySourceTest.face.getName());
		Assert.assertSame(FontWeight.BLACK, MsdfBinarySourceTest.montserrat("Black").read().getWeight());
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
	public void overridesTheStyle() throws IOException {
		final MsdfFontFace face = MsdfBinarySourceTest.montserrat("Regular").weight(FontWeight.LIGHT).italic(true).read();
		Assert.assertSame(FontWeight.LIGHT, face.getWeight());
		Assert.assertTrue(face.isItalic());
		Assert.assertEquals(MsdfBinarySourceTest.face.getGlyphs().size(), face.getGlyphs().size());
	}

	@Test(expected = IOException.class)
	public void refusesAForeignFile() throws IOException {
		MsdfBinarySource.of(new ByteArrayInputStream("not a joid font".getBytes(StandardCharsets.UTF_8))).read();
	}

	@Test
	public void undoesEveryRowFilter() throws IOException {
		final Resource texture = MsdfBinarySource.of(new ByteArrayInputStream(MsdfBinarySourceTest.synthetic(4))).read().getTexture();
		texture.getDecoder().decode(texture.getResourceData());
		final int[] pixels = texture.getData();
		for (int y = 0; y < MsdfBinarySourceTest.PIXELS.length; y++) {
			for (int x = 0; x < 2; x++) {
				final int[] row = MsdfBinarySourceTest.PIXELS[y];
				Assert.assertEquals("Pixel " + x + "," + y, 255 << 24 | row[x * 3] << 16 | row[x * 3 + 1] << 8 | row[x * 3 + 2], pixels[x + y * 2]);
			}
		}
	}

	@Test(expected = IOException.class)
	public void refusesAnOlderVersion() throws IOException {
		MsdfBinarySource.of(new ByteArrayInputStream(MsdfBinarySourceTest.synthetic(3))).read();
	}

	@Test(expected = IOException.class)
	public void refusesANewerVersion() throws IOException {
		MsdfBinarySource.of(new ByteArrayInputStream(MsdfBinarySourceTest.synthetic(5))).read();
	}

	@Test
	public void readsTheNameOfTheFace() throws IOException {
		Assert.assertEquals("Synthetic Light Italic", MsdfBinarySource.of(new ByteArrayInputStream(MsdfBinarySourceTest.synthetic(4))).read().getName());
		Assert.assertEquals("Synthetic Light Italic", MsdfBinarySource.of(new ByteArrayInputStream(MsdfBinarySourceTest.synthetic(4))).weight(FontWeight.BLACK).read().getName());
	}

	@Test
	public void overridesOnlyWhatIsAsked() throws IOException {
		final MsdfFontFace weighted = MsdfBinarySource.of(new ByteArrayInputStream(MsdfBinarySourceTest.synthetic(4))).weight(FontWeight.BLACK).read();
		Assert.assertSame(FontWeight.BLACK, weighted.getWeight());
		Assert.assertTrue(weighted.isItalic());
		final MsdfFontFace upright = MsdfBinarySource.of(new ByteArrayInputStream(MsdfBinarySourceTest.synthetic(4))).italic(false).read();
		Assert.assertSame(FontWeight.LIGHT, upright.getWeight());
		Assert.assertFalse(upright.isItalic());
	}

	@Test
	public void readsEveryFieldOfTheFormat() throws IOException {
		final MsdfFontFace face = MsdfBinarySource.of(new ByteArrayInputStream(MsdfBinarySourceTest.synthetic(4))).read();
		Assert.assertSame(FontWeight.LIGHT, face.getWeight());
		Assert.assertTrue(face.isItalic());
		Assert.assertEquals(4F, face.getAtlas().getDistanceRange(), 0F);
		Assert.assertEquals(32F, face.getAtlas().getSize(), 0F);
		Assert.assertEquals(2, face.getAtlas().getWidth());
		Assert.assertEquals(5, face.getAtlas().getHeight());
		Assert.assertEquals(1.25F, face.getLineHeight(), 0F);
		Assert.assertEquals(0.0625F, face.getUnderlineThickness(), 0F);
		Assert.assertEquals(0.625F, face.getAdvance('A'), 0F);
		Assert.assertEquals(-0.1F, face.getGlyph('A').getPlaneBounds().getLeft(), 0F);
		Assert.assertEquals(0.8F, face.getGlyph('A').getPlaneBounds().getTop(), 0F);
		Assert.assertEquals(1F, face.getGlyph('A').getAtlasBounds().getBottom(), 0F);
		Assert.assertEquals(5F, face.getGlyph('A').getAtlasBounds().getTop(), 0F);
		Assert.assertNull(face.getGlyph(' ').getPlaneBounds());
		Assert.assertEquals(-0.08F, face.getKerning('A', 'V'), 1E-6F);
		Assert.assertEquals(0.04F, face.getKerning('A', 'W'), 1E-6F);
	}

	private static MsdfBinarySource montserrat(final String weight) throws IOException {
		return MsdfBinarySource.of(MsdfFontCache.resolve(Asset.of(JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat/Montserrat-" + weight + ".ttf")).read()));
	}

	private static byte[] synthetic(final int version) throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		output.write("JOIDMSDF".getBytes(StandardCharsets.US_ASCII));
		try (DataOutputStream data = new DataOutputStream(new DeflaterOutputStream(output))) {
			data.writeByte(version);
			data.writeShort(300);
			data.writeBoolean(true);
			data.writeUTF("Synthetic Light Italic");
			data.writeInt(2);
			data.writeInt(MsdfBinarySourceTest.PIXELS.length);
			data.writeFloat(4F);
			data.writeFloat(32F);
			for (final float metric : new float[] {1.25F, 0.875F, -0.25F, -0.125F, 0.0625F}) {
				data.writeFloat(metric);
			}

			data.writeInt(2);
			data.writeInt('A');
			data.writeFloat(0.625F);
			data.writeBoolean(true);
			for (final float bound : new float[] {-0.1F, -0.2F, 0.7F, 0.8F}) {
				data.writeFloat(bound);
			}
			for (final int bound : new int[] {0, 1, 2, 5}) {
				data.writeShort(bound);
			}
			data.writeInt(' ');
			data.writeFloat(0.25F);
			data.writeBoolean(false);

			data.writeShort(1000);
			for (final int value : new int[] {1, 'A', 2, 'V', -80 << 1 ^ -80 >> 31, 'W' - 'V', 40 << 1 ^ 40 >> 31}) {
				MsdfBinarySourceTest.variable(data, value);
			}

			for (int y = 0; y < MsdfBinarySourceTest.PIXELS.length; y++) {
				data.writeByte(y);
				for (int i = 0; i < MsdfBinarySourceTest.PIXELS[y].length; i++) {
					final int left = i >= 3 ? MsdfBinarySourceTest.PIXELS[y][i - 3] : 0;
					final int up = y > 0 ? MsdfBinarySourceTest.PIXELS[y - 1][i] : 0;
					final int corner = i >= 3 && y > 0 ? MsdfBinarySourceTest.PIXELS[y - 1][i - 3] : 0;
					data.writeByte(MsdfBinarySourceTest.PIXELS[y][i] - MsdfBinarySourceTest.predict(y, left, up, corner));
				}
			}
		}
		return output.toByteArray();
	}

	private static int predict(final int filter, final int left, final int up, final int corner) {
		final int estimate = left + up - corner;
		switch (filter) {
		case 1:
			return left;
		case 2:
			return up;
		case 3:
			return (left + up) / 2;
		case 4:
			return Math.abs(estimate - left) <= Math.abs(estimate - up) && Math.abs(estimate - left) <= Math.abs(estimate - corner) ? left : Math.abs(estimate - up) <= Math.abs(estimate - corner) ? up : corner;
		default:
			return 0;
		}
	}

	private static void variable(final DataOutputStream data, final int value) throws IOException {
		int rest = value;
		while (rest >= 0x80) {
			data.writeByte(rest & 0x7F | 0x80);
			rest >>>= 7;
		}
		data.writeByte(rest);
	}

}