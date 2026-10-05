package be.zeldown.joid.lib.font.impl.msdf.dto.source;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import be.zeldown.joid.lib.font.FontWeight;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfBounds;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfFace;

public class MsdfJsonSourceTest {

	private static final String JSON = "{\"atlas\":{\"type\":\"msdf\",\"distanceRange\":4,\"size\":32,\"width\":2,\"height\":2,\"yOrigin\":\"bottom\"},"
			+ "\"metrics\":{\"emSize\":1,\"lineHeight\":1.25,\"ascender\":0.875,\"descender\":-0.25,\"underlineY\":-0.125,\"underlineThickness\":0.0625},"
			+ "\"glyphs\":[{\"unicode\":32,\"advance\":0.25},{\"unicode\":65,\"advance\":0.625,\"planeBounds\":{\"left\":0,\"bottom\":-0.125,\"right\":0.625,\"top\":0.75},\"atlasBounds\":{\"left\":0.5,\"bottom\":0.5,\"right\":1.5,\"top\":1.5}}],"
			+ "\"kerning\":[{\"unicode1\":65,\"unicode2\":86,\"advance\":-0.0625}]}";

	private static MsdfFace face;

	@BeforeClass
	public static void read() throws IOException {
		MsdfJsonSourceTest.face = MsdfJsonSource.of(MsdfJsonSourceTest.stream(MsdfJsonSourceTest.JSON), new ByteArrayInputStream(MsdfJsonSourceTest.png())).read();
	}

	@Test
	public void readsTheAtlas() {
		Assert.assertEquals(4F, MsdfJsonSourceTest.face.getAtlas().getDistanceRange(), 0F);
		Assert.assertEquals(32F, MsdfJsonSourceTest.face.getAtlas().getSize(), 0F);
		Assert.assertEquals(2, MsdfJsonSourceTest.face.getAtlas().getWidth());
		Assert.assertEquals(2, MsdfJsonSourceTest.face.getAtlas().getHeight());
	}

	@Test
	public void readsTheMetrics() {
		Assert.assertEquals(1.25F, MsdfJsonSourceTest.face.getMetrics().getLineHeight(), 0F);
		Assert.assertEquals(0.875F, MsdfJsonSourceTest.face.getMetrics().getAscender(), 0F);
		Assert.assertEquals(-0.25F, MsdfJsonSourceTest.face.getMetrics().getDescender(), 0F);
		Assert.assertEquals(-0.125F, MsdfJsonSourceTest.face.getMetrics().getUnderlineY(), 0F);
		Assert.assertEquals(0.0625F, MsdfJsonSourceTest.face.getMetrics().getUnderlineThickness(), 0F);
	}

	@Test
	public void readsTheGlyphs() {
		Assert.assertEquals(2, MsdfJsonSourceTest.face.getGlyphs().size());
		Assert.assertEquals(0.625F, MsdfJsonSourceTest.face.getGlyph('A').getAdvance(), 0F);
		Assert.assertNull(MsdfJsonSourceTest.face.getGlyph(' ').getPlaneBounds());
		Assert.assertNull(MsdfJsonSourceTest.face.getGlyph(' ').getAtlasBounds());
	}

	@Test
	public void keepsThePlaneBoundsAsWritten() {
		final MsdfBounds bounds = MsdfJsonSourceTest.face.getGlyph('A').getPlaneBounds();
		Assert.assertEquals(-0.125F, bounds.getBottom(), 0F);
		Assert.assertEquals(0.75F, bounds.getTop(), 0F);
	}

	@Test
	public void keepsTheAtlasBoundsAsWritten() {
		final MsdfBounds bounds = MsdfJsonSourceTest.face.getGlyph('A').getAtlasBounds();
		Assert.assertEquals(0.5F, bounds.getLeft(), 0F);
		Assert.assertEquals(0.5F, bounds.getBottom(), 0F);
		Assert.assertEquals(1.5F, bounds.getRight(), 0F);
		Assert.assertEquals(1.5F, bounds.getTop(), 0F);
	}

	@Test
	public void startsRegularAndUpright() {
		Assert.assertSame(FontWeight.REGULAR, MsdfJsonSourceTest.face.getWeight());
		Assert.assertFalse(MsdfJsonSourceTest.face.isItalic());
	}

	@Test
	public void takesTheStyleOfTheSource() throws IOException {
		final MsdfFace face = MsdfJsonSource.of(MsdfJsonSourceTest.stream(MsdfJsonSourceTest.JSON), new ByteArrayInputStream(MsdfJsonSourceTest.png())).weight(FontWeight.BOLD).italic(true).read();
		Assert.assertSame(FontWeight.BOLD, face.getWeight());
		Assert.assertTrue(face.isItalic());
	}

	@Test
	public void readsTheKerning() {
		Assert.assertEquals(-0.0625F, MsdfJsonSourceTest.face.getKerning('A', 'V'), 0F);
		Assert.assertEquals(0F, MsdfJsonSourceTest.face.getKerning('V', 'A'), 0F);
	}

	@Test(expected = IOException.class)
	public void refusesAnUndecodableTexture() throws IOException {
		MsdfJsonSource.of(MsdfJsonSourceTest.stream(MsdfJsonSourceTest.JSON), MsdfJsonSourceTest.stream("not an image")).read();
	}

	private static ByteArrayInputStream stream(final String text) {
		return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
	}

	private static byte[] png() throws IOException {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", output);
		return output.toByteArray();
	}

}