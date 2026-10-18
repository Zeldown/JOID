package dev.joid.lib.font.impl.msdf.source;

import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

public class OpenTypeMsdfSourceTest {

	@Test
	public void recognisesOpenTypeHeaders() {
		Assert.assertTrue(OpenTypeMsdfSource.supports(new byte[] {0, 1, 0, 0}));
		Assert.assertTrue(OpenTypeMsdfSource.supports(new byte[] {'t', 'r', 'u', 'e'}));
		Assert.assertTrue(OpenTypeMsdfSource.supports(new byte[] {'O', 'T', 'T', 'O', 0}));
		Assert.assertTrue(OpenTypeMsdfSource.supports(new byte[] {'t', 't', 'c', 'f'}));
	}

	@Test
	public void refusesAnythingElse() {
		Assert.assertFalse(OpenTypeMsdfSource.supports("JOIDMSDF".getBytes(StandardCharsets.US_ASCII)));
		Assert.assertFalse(OpenTypeMsdfSource.supports(new byte[] {0, 1, 0}));
		Assert.assertFalse(OpenTypeMsdfSource.supports(new byte[0]));
	}

}