package be.zeldown.joid.lib.font.impl.msdf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionException;

import org.junit.Assert;
import org.junit.Test;

import be.zeldown.joid.internal.JOID;
import be.zeldown.joid.lib.asset.Asset;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfFace;
import be.zeldown.joid.lib.font.impl.msdf.dto.source.IMsdfSource;
import be.zeldown.joid.lib.font.impl.msdf.dto.source.MsdfBinarySource;

public class MsdfFontLoaderTest {

	private static final String FONT = "/assets/dev/fonts/Montserrat-Regular/font.msdf";

	@Test(timeout = 10000L)
	public void handsTheFailureBackToTheCaller() {
		try {
			MsdfFontLoader.load(new ByteArrayInputStream("not a font at all".getBytes(StandardCharsets.UTF_8))).join();
			Assert.fail("A corrupt font must not complete");
		} catch (final CompletionException expected) {
			Assert.assertTrue(expected.getCause() instanceof IOException);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void refusesAnUnknownHandle() {
		MsdfFontLoader.load(new Object());
	}

	@Test(timeout = 30000L)
	public void loadsFromAnyHandle() throws IOException {
		try (InputStream stream = JOID.class.getResourceAsStream(MsdfFontLoaderTest.FONT)) {
			final MsdfFont font = MsdfFontLoader.load(stream).join();
			Assert.assertNotNull(font.getRegular());
			Assert.assertSame(font.getRegular(), font.getBold());
		}
	}

	@Test(timeout = 30000L)
	public void loadsFromAnAsset() {
		final Asset asset = Asset.of(JOID.class.getResourceAsStream(MsdfFontLoaderTest.FONT));
		Assert.assertTrue(MsdfFontLoader.load(asset).join().getRegular().getGlyphs().size() > 200);
	}

	@Test(timeout = 30000L)
	public void readsAHandleUsedForBothWeightsOnce() throws IOException {
		try (InputStream stream = JOID.class.getResourceAsStream(MsdfFontLoaderTest.FONT)) {
			final MsdfFont font = MsdfFontLoader.load(stream, stream).join();
			Assert.assertSame(font.getRegular(), font.getBold());
		}
	}

	@Test(timeout = 30000L)
	public void acceptsAnyMsdfSource() throws IOException {
		final MsdfFace face = MsdfBinarySource.of(JOID.class.getResourceAsStream(MsdfFontLoaderTest.FONT)).read();
		final IMsdfSource source = () -> face;
		Assert.assertSame(face, MsdfFontLoader.load(source).join().getRegular());
	}

}