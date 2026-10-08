package dev.joid.lib.utils.platform;

import org.junit.Assert;
import org.junit.Test;

public class PlatformTest {

	@Test
	public void readsThePlatformFromTheSystemName() {
		Assert.assertEquals(Platform.WINDOWS, PlatformTest.platform("Windows 11"));
		Assert.assertEquals(Platform.MACOS, PlatformTest.platform("Mac OS X"));
		Assert.assertEquals(Platform.MACOS, PlatformTest.platform("Darwin"));
		Assert.assertEquals(Platform.LINUX, PlatformTest.platform("Linux"));
		Assert.assertEquals(Platform.LINUX, PlatformTest.platform("FreeBSD"));
	}

	@Test
	public void readsTheArchitectureWidth() {
		final String architecture = System.getProperty("os.arch");
		try {
			System.setProperty("os.arch", "amd64");
			Assert.assertTrue(Platform.is64Bit());
			System.setProperty("os.arch", "x86");
			Assert.assertFalse(Platform.is64Bit());
		} finally {
			System.setProperty("os.arch", architecture);
		}
	}

	private static Platform platform(final String name) {
		final String system = System.getProperty("os.name");
		try {
			System.setProperty("os.name", name);
			return Platform.current();
		} finally {
			System.setProperty("os.name", system);
		}
	}

}