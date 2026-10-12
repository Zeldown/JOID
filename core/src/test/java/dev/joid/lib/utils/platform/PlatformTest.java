package dev.joid.lib.utils.platform;

import java.io.File;
import java.util.Collections;
import java.util.Map;

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

	@Test
	public void cachesInTheLocalApplicationDataOfWindows() {
		Assert.assertEquals(new File("C:/Users/me/AppData/Local"), Platform.WINDOWS.getCacheDir("C:/Users/me", Collections.singletonMap("LOCALAPPDATA", "C:/Users/me/AppData/Local")::get));
		Assert.assertEquals(new File("D:/Profile/AppData/Local"), Platform.WINDOWS.getCacheDir("D:/Profile", Collections.<String, String>emptyMap()::get));
	}

	@Test
	public void cachesInTheLibraryOfMacOs() {
		Assert.assertEquals(new File("/Users/me/Library/Caches"), Platform.MACOS.getCacheDir("/Users/me", Collections.singletonMap("XDG_CACHE_HOME", "/tmp/cache")::get));
	}

	@Test
	public void cachesWhereTheXdgVariableSaysOnLinux() {
		final Map<String, String> environment = Collections.singletonMap("XDG_CACHE_HOME", "/var/cache/me");
		Assert.assertEquals(new File("/var/cache/me"), Platform.LINUX.getCacheDir("/home/me", environment::get));
		Assert.assertEquals(new File("/home/me/.cache"), Platform.LINUX.getCacheDir("/home/me", Collections.<String, String>emptyMap()::get));
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