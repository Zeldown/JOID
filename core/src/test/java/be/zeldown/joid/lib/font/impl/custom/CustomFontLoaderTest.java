package be.zeldown.joid.lib.font.impl.custom;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionException;

import org.junit.Assert;
import org.junit.Test;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.asset.Asset;

public class CustomFontLoaderTest {

	@Test(timeout = 10000L)
	public void handsTheFailureBackToTheCaller() {
		try {
			CustomFontLoader.load(new ByteArrayInputStream("not a font at all".getBytes(StandardCharsets.UTF_8))).join();
			Assert.fail("A corrupt font must not complete");
		} catch (final CompletionException expected) {
			Assert.assertNotNull(expected.getCause());
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAnUnknownHandle() {
		CustomFontLoader.load(new Object());
	}

	@Test(timeout = 30000L)
	public void loadsFromAnyHandle() throws Exception {
		try (InputStream stream = JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat-Regular/font.msdf")) {
			final CustomFont font = CustomFontLoader.load(stream).join();
			Assert.assertNotNull(font.getRegular());
			Assert.assertSame(font.getRegular(), font.getBold());
		}
	}

	@Test(timeout = 30000L)
	public void loadsFromAnAsset() {
		final Asset asset = Asset.of(JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat-Regular/font.msdf"));
		Assert.assertTrue(CustomFontLoader.load(asset).join().getRegular().getFontInfo().getGlyphMap().size() > 200);
	}

}