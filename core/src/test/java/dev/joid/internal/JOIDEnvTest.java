package dev.joid.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import dev.joid.lib.utils.platform.Platform;

public class JOIDEnvTest {

	private boolean devMode;

	private File dataDir;
	private File cacheDir;

	@Before
	public void rememberTheModes() {
		this.devMode = JOIDEnv.isDevMode();
		this.dataDir = JOIDEnv.getDataDir();
		this.cacheDir = JOIDEnv.getCacheDir();
	}

	@After
	public void restoreTheModes() {
		JOIDEnv.devMode(this.devMode);
		JOIDEnv.dataDir(this.dataDir);
		JOIDEnv.cacheDir(this.cacheDir);
	}

	@Test
	public void turnsTheModesOnWithTheDemoJar() throws ReflectiveOperationException {
		Assert.assertArrayEquals(new boolean[] {true, true}, JOIDEnvTest.modes());
	}

	@Test
	public void keepsOnlyTheDevModeInTheDevJar() throws ReflectiveOperationException {
		Assert.assertArrayEquals(new boolean[] {true, false}, JOIDEnvTest.modes("DemoFont.class"));
	}

	@Test
	public void turnsTheModesOffInTheProdJar() throws ReflectiveOperationException {
		Assert.assertArrayEquals(new boolean[] {false, false}, JOIDEnvTest.modes("DevNode.class", "DemoFont.class"));
	}

	@Test
	public void switchesTheDevMode() {
		JOIDEnv.devMode(false);
		Assert.assertFalse(JOIDEnv.isDevMode());
		JOIDEnv.devMode(true);
		Assert.assertTrue(JOIDEnv.isDevMode());
	}

	@Test(expected = IllegalStateException.class)
	public void refusesTheDevModeWithoutTheDevClasses() throws Throwable {
		JOIDEnvTest.switchOn("devMode", "DevNode.class");
	}

	@Test
	public void storesTheDataInAJoidFolderByDefault() throws ReflectiveOperationException {
		Assert.assertEquals(new File("joid"), JOIDEnvTest.locate("joid.data", null, "locateDataDir"));
	}

	@Test
	public void readsTheDataFolderFromTheJoidDataProperty() throws ReflectiveOperationException {
		Assert.assertEquals(new File("run/data"), JOIDEnvTest.locate("joid.data", "run/data", "locateDataDir"));
	}

	@Test
	public void cachesInTheCacheFolderOfTheSystemByDefault() throws ReflectiveOperationException {
		final File cache = Platform.current().getCacheDir(System.getProperty("user.home"), System::getenv);
		Assert.assertEquals(new File(cache, "joid"), JOIDEnvTest.locate("joid.cache", null, "locateCacheDir"));
	}

	@Test
	public void readsTheCacheFolderFromTheJoidCacheProperty() throws ReflectiveOperationException {
		Assert.assertEquals(new File("run/cache"), JOIDEnvTest.locate("joid.cache", "run/cache", "locateCacheDir"));
	}

	@Test
	public void movesItsFolders() {
		JOIDEnv.dataDir(new File("data"));
		JOIDEnv.cacheDir(new File("cache"));
		Assert.assertEquals(new File("data"), JOIDEnv.getDataDir());
		Assert.assertEquals(new File("cache"), JOIDEnv.getCacheDir());
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingDataFolder() {
		JOIDEnv.dataDir(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingCacheFolder() {
		JOIDEnv.cacheDir(null);
	}

	private static File locate(final String property, final String value, final String locator) throws ReflectiveOperationException {
		final Method method = JOIDEnv.class.getDeclaredMethod(locator);
		method.setAccessible(true);
		final String previous = value != null ? System.setProperty(property, value) : System.clearProperty(property);
		try {
			return (File) method.invoke(null);
		} finally {
			if (previous == null) {
				System.clearProperty(property);
			} else {
				System.setProperty(property, previous);
			}
		}
	}

	private static boolean[] modes(final String... hidden) throws ReflectiveOperationException {
		final Class<?> clazz = new JarClassLoader(hidden).loadClass(JOIDEnv.class.getName());
		return new boolean[] {(boolean) clazz.getMethod("isDevMode").invoke(null), (boolean) clazz.getMethod("isDemoMode").invoke(null)};
	}

	private static void switchOn(final String mode, final String hidden) throws Throwable {
		final Class<?> clazz = new JarClassLoader(hidden).loadClass(JOIDEnv.class.getName());
		try {
			clazz.getMethod(mode, boolean.class).invoke(null, true);
		} catch (final ReflectiveOperationException exception) {
			throw exception.getCause();
		}
	}

	public static final class JarClassLoader extends ClassLoader {

		private final List<String> hiddenList;

		private JarClassLoader(final String... hidden) {
			super(JOIDEnvTest.class.getClassLoader());
			this.hiddenList = Arrays.asList(hidden);
		}

		@Override
		protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
			if (!name.equals(JOIDEnv.class.getName())) {
				return super.loadClass(name, resolve);
			}

			synchronized (super.getClassLoadingLock(name)) {
				final Class<?> loaded = super.findLoadedClass(name);
				if (loaded != null) {
					return loaded;
				}

				try (final InputStream input = super.getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
					final byte[] bytes = IOUtils.toByteArray(input);
					return super.defineClass(name, bytes, 0, bytes.length, JOIDEnv.class.getProtectionDomain());
				} catch (final IOException e) {
					throw new ClassNotFoundException(name, e);
				}
			}
		}

		@Override
		public URL getResource(final String name) {
			if (this.hiddenList.stream().anyMatch(name::endsWith)) {
				return null;
			}

			return super.getResource(name);
		}

	}

}