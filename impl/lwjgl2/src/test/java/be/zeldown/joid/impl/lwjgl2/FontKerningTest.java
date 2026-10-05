package be.zeldown.joid.impl.lwjgl2;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import be.zeldown.joid.impl.lwjgl2.snapshot.SnapshotBackend;
import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.font.dto.text.TextInfo;
import be.zeldown.joid.lib.font.impl.custom.CustomFont;
import be.zeldown.joid.lib.font.impl.custom.CustomFontLoader;

public class FontKerningTest {

	private static final float SIZE = 100F;

	private static SnapshotBackend backend;
	private static CustomFont      font;

	@BeforeClass
	public static void load() {
		FontKerningTest.backend = new SnapshotBackend();
		FontKerningTest.backend.create(64, 64);
		FontKerningTest.font = CustomFontLoader.load(JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat-Regular/font.msdf")).join();
	}

	@AfterClass
	public static void destroy() {
		FontKerningTest.backend.destroy();
		FontKerningTest.backend = null;
	}

	@Test
	public void tightensKernedPairs() {
		final TextInfo info = TextInfo.create(FontKerningTest.font, FontKerningTest.SIZE, Color.WHITE);
		final double pair = info.getWidth("AV");
		final double separate = info.getWidth("A") + info.getWidth("V");
		Assert.assertTrue("AV must be narrower than A plus V (" + pair + " vs " + separate + ")", pair < separate);
	}

	@Test
	public void measuresTheExactKerningOffset() {
		final TextInfo info = TextInfo.create(FontKerningTest.font, FontKerningTest.SIZE, Color.WHITE);
		final float kerning = FontKerningTest.font.getRegular().getFontInfo().getKerning('A', 'V');
		Assert.assertEquals(info.getWidth("A") + info.getWidth("V") + kerning * FontKerningTest.SIZE, info.getWidth("AV"), 0.001D);
	}

	@Test
	public void leavesUnkernedPairsUntouched() {
		final TextInfo info = TextInfo.create(FontKerningTest.font, FontKerningTest.SIZE, Color.WHITE);
		Assert.assertEquals(info.getWidth("H") + info.getWidth("H"), info.getWidth("HH"), 0.001D);
	}

	@Test
	public void accumulatesKerningOverAWord() {
		final TextInfo info = TextInfo.create(FontKerningTest.font, FontKerningTest.SIZE, Color.WHITE);
		final float kerning = FontKerningTest.font.getRegular().getFontInfo().getKerning('A', 'V') + FontKerningTest.font.getRegular().getFontInfo().getKerning('V', 'A');
		Assert.assertEquals(info.getWidth("A") * 2D + info.getWidth("V") + kerning * FontKerningTest.SIZE, info.getWidth("AVA"), 0.001D);
	}

}