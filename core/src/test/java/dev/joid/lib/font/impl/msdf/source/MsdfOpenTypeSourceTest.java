package dev.joid.lib.font.impl.msdf.source;

import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

public class MsdfOpenTypeSourceTest {

	@Test
	public void recognisesOpenTypeHeaders() {
		Assert.assertTrue(MsdfOpenTypeSource.supports(new byte[] {0, 1, 0, 0}));
		Assert.assertTrue(MsdfOpenTypeSource.supports(new byte[] {'t', 'r', 'u', 'e'}));
		Assert.assertTrue(MsdfOpenTypeSource.supports(new byte[] {'O', 'T', 'T', 'O', 0}));
		Assert.assertTrue(MsdfOpenTypeSource.supports(new byte[] {'t', 't', 'c', 'f'}));
	}

	@Test
	public void refusesAnythingElse() {
		Assert.assertFalse(MsdfOpenTypeSource.supports("JOIDMSDF".getBytes(StandardCharsets.US_ASCII)));
		Assert.assertFalse(MsdfOpenTypeSource.supports(new byte[] {0, 1, 0}));
		Assert.assertFalse(MsdfOpenTypeSource.supports(new byte[0]));
	}

}