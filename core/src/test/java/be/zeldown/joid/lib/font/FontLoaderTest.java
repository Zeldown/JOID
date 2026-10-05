package be.zeldown.joid.lib.font;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionException;

import org.junit.Assert;
import org.junit.Test;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.asset.Asset;
import be.zeldown.joid.lib.font.impl.custom.CustomFont;

public class FontLoaderTest {

	@Test(timeout = 10000L)
	public void handsTheFailureBackToTheCaller() {
		try {
			FontLoader.load(new ByteArrayInputStream("not a font at all".getBytes(StandardCharsets.UTF_8))).join();
			Assert.fail("A corrupt font must not complete");
		} catch (final CompletionException expected) {
			Assert.assertNotNull(expected.getCause());
		}
	}

	@Test(timeout = 30000L)
	public void loadsFromAnyHandle() {
		try (InputStream stream = JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat-Regular/font.msdf")) {
			final CustomFont font = FontLoader.load(stream).join();
			Assert.assertNotNull(font.getRegular());
			Assert.assertSame(font.getRegular(), font.getBold());
		} catch (final Exception exception) {
			throw new IllegalStateException(exception);
		}
	}

	@Test(timeout = 30000L)
	public void loadsFromAnAsset() {
		final Asset asset = Asset.of(JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat-Regular/font.msdf"));
		Assert.assertTrue(FontLoader.load(asset).join().getRegular().getFontInfo().getGlyphMap().size() > 200);
	}

}