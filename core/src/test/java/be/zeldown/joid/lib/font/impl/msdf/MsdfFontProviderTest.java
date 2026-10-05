package be.zeldown.joid.lib.font.impl.msdf;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.color.Color;
import be.zeldown.joid.lib.font.FontWeight;
import be.zeldown.joid.lib.font.dto.TextInfo;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfFace;

public class MsdfFontProviderTest {

	private static final float SIZE = 100F;

	private static MsdfFont font;
	private static MsdfFace regular;

	@BeforeClass
	public static void load() {
		MsdfFontProviderTest.font = MsdfFontLoader.load(JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat-Regular/font.msdf"), JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat-Bold/font.msdf")).join();
		MsdfFontProviderTest.regular = MsdfFontProviderTest.font.getFace(FontWeight.REGULAR, false);
	}

	@Test
	public void tightensKernedPairs() {
		final TextInfo info = MsdfFontProviderTest.info();
		final double pair = info.getWidth("AV");
		final double separate = info.getWidth("A") + info.getWidth("V");
		Assert.assertTrue("AV must be narrower than A plus V (" + pair + " vs " + separate + ")", pair < separate);
	}

	@Test
	public void measuresTheExactKerningOffset() {
		final TextInfo info = MsdfFontProviderTest.info();
		final float kerning = MsdfFontProviderTest.regular.getKerning('A', 'V');
		Assert.assertEquals(info.getWidth("A") + info.getWidth("V") + kerning * MsdfFontProviderTest.SIZE, info.getWidth("AV"), 0.001D);
	}

	@Test
	public void leavesUnkernedPairsUntouched() {
		final TextInfo info = MsdfFontProviderTest.info();
		Assert.assertEquals(info.getWidth("H") + info.getWidth("H"), info.getWidth("HH"), 0.001D);
	}

	@Test
	public void accumulatesKerningOverAWord() {
		final TextInfo info = MsdfFontProviderTest.info();
		final float kerning = MsdfFontProviderTest.regular.getKerning('A', 'V') + MsdfFontProviderTest.regular.getKerning('V', 'A');
		Assert.assertEquals(info.getWidth("A") * 2D + info.getWidth("V") + kerning * MsdfFontProviderTest.SIZE, info.getWidth("AVA"), 0.001D);
	}

	@Test
	public void measuresTheRequestedWeight() {
		final double regular = MsdfFontProviderTest.info().getWidth("Hello");
		final double bold = MsdfFontProviderTest.info().weight(FontWeight.BOLD).getWidth("Hello");
		Assert.assertTrue("Bold must be wider than regular (" + bold + " vs " + regular + ")", bold > regular);
		Assert.assertEquals(bold, MsdfFontProviderTest.info().weight(FontWeight.BLACK).getWidth("Hello"), 0D);
	}

	@Test
	public void keepsTheLineHeightOfTheFace() {
		Assert.assertEquals(MsdfFontProviderTest.regular.getLineHeight() * MsdfFontProviderTest.SIZE, MsdfFontProviderTest.info().getHeight(), 0.001D);
	}

	private static TextInfo info() {
		return TextInfo.create(MsdfFontProviderTest.font, MsdfFontProviderTest.SIZE, Color.WHITE);
	}

}