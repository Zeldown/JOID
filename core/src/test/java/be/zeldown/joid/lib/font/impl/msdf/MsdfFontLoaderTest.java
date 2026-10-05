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
import be.zeldown.joid.lib.font.FontWeight;
import be.zeldown.joid.lib.font.impl.msdf.dto.MsdfFontFace;
import be.zeldown.joid.lib.font.impl.msdf.dto.source.IMsdfSource;
import be.zeldown.joid.lib.font.impl.msdf.dto.source.MsdfBinarySource;

public class MsdfFontLoaderTest {

	private static final String[] WEIGHTS = {"Thin", "ExtraLight", "Light", "Regular", "Medium", "SemiBold", "Bold", "ExtraBold", "Black"};

	@Test(timeout = 30000L)
	public void restylesASource() {
		final MsdfFont font = MsdfFontLoader.load(MsdfFontLoaderTest.stream("Regular"), MsdfBinarySource.of(MsdfFontLoaderTest.stream("Regular")).italic(true)).join();
		Assert.assertFalse(font.getFace(FontWeight.REGULAR, false).isItalic());
		Assert.assertTrue(font.getFace(FontWeight.REGULAR, true).isItalic());
	}

	@Test(timeout = 30000L)
	public void loadsFromAnAsset() {
		final Asset asset = Asset.of(MsdfFontLoaderTest.stream("Regular"));
		Assert.assertTrue(MsdfFontLoader.load(asset).join().getFace(FontWeight.REGULAR, false).getGlyphs().size() > 200);
	}

	@Test(timeout = 10000L)
	public void refusesAnEmptyFamily() {
		MsdfFontLoaderTest.fails(IllegalArgumentException.class);
	}

	@Test(timeout = 10000L)
	public void refusesAnUnknownHandle() {
		MsdfFontLoaderTest.fails(IllegalArgumentException.class, new Object());
	}

	@Test(timeout = 60000L)
	public void loadsEveryWeightOfAFamily() {
		final Object[] handles = new Object[MsdfFontLoaderTest.WEIGHTS.length];
		for (int i = 0; i < handles.length; i++) {
			handles[handles.length - 1 - i] = MsdfFontLoaderTest.stream(MsdfFontLoaderTest.WEIGHTS[i]);
		}

		final MsdfFont font = MsdfFontLoader.load(handles).join();
		Assert.assertEquals(FontWeight.values().length, font.getFamily().getFaces().size());
		for (final FontWeight weight : FontWeight.values()) {
			Assert.assertSame(weight, font.getFace(weight, false).getWeight());
		}
	}

	@Test(timeout = 10000L)
	public void handsTheFailureBackToTheCaller() {
		MsdfFontLoaderTest.fails(IOException.class, new ByteArrayInputStream("not a font at all".getBytes(StandardCharsets.UTF_8)));
	}

	@Test(timeout = 30000L)
	public void refusesTwoFacesOfTheSameWeight() {
		MsdfFontLoaderTest.fails(IllegalArgumentException.class, MsdfFontLoaderTest.stream("Regular"), MsdfFontLoaderTest.stream("Regular"));
	}

	@Test(timeout = 30000L)
	public void loadsFromAnyHandle() throws IOException {
		try (InputStream stream = MsdfFontLoaderTest.stream("Regular")) {
			final MsdfFont font = MsdfFontLoader.load(stream).join();
			Assert.assertEquals(1, font.getFamily().getFaces().size());
			Assert.assertSame(FontWeight.REGULAR, font.getFace(FontWeight.BOLD, false).getWeight());
		}
	}

	@Test(timeout = 30000L)
	public void acceptsAnyMsdfSource() throws IOException {
		final MsdfFontFace face = MsdfBinarySource.of(MsdfFontLoaderTest.stream("Regular")).read();
		final IMsdfSource source = () -> face;
		Assert.assertSame(face, MsdfFontLoader.load(source).join().getFace(FontWeight.REGULAR, false));
	}

	private static InputStream stream(final String weight) {
		return JOID.class.getResourceAsStream("/assets/dev/fonts/Montserrat-" + weight + "/font.msdf");
	}

	private static void fails(final Class<? extends Throwable> cause, final Object... faces) {
		try {
			MsdfFontLoader.load(faces).join();
			Assert.fail("The font must not load");
		} catch (final CompletionException expected) {
			Assert.assertTrue(String.valueOf(expected.getCause()), cause.isInstance(expected.getCause()));
		}
	}

}