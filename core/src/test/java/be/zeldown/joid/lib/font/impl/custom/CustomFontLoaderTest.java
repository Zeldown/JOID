package be.zeldown.joid.lib.font.impl.custom;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletionException;

import org.junit.Assert;
import org.junit.Test;

public class CustomFontLoaderTest {

	@Test(timeout = 10000L)
	public void failsInsteadOfHangingOnACorruptFont() {
		try {
			CustomFontLoader.load(new ByteArrayInputStream("not a font at all".getBytes(StandardCharsets.UTF_8))).join();
			Assert.fail("A corrupt font must not complete");
		} catch (final CompletionException expected) {
			Assert.assertNotNull(expected.getCause());
		}
	}

}