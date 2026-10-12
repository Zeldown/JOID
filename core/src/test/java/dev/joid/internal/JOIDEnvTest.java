package dev.joid.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JOIDEnvTest {

	private boolean devMode;
	private boolean demoMode;

	@Before
	public void rememberTheModes() {
		this.devMode = JOIDEnv.isDevMode();
		this.demoMode = JOIDEnv.isDemoMode();
	}

	@After
	public void restoreTheModes() {
		JOIDEnv.devMode(this.devMode);
		JOIDEnv.demoMode(this.demoMode);
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
	public void switchesTheModesForTheTests() {
		JOIDEnv.devMode(false);
		JOIDEnv.demoMode(false);
		Assert.assertFalse(JOIDEnv.isDevMode());
		Assert.assertFalse(JOIDEnv.isDemoMode());
		JOIDEnv.devMode(true);
		JOIDEnv.demoMode(true);
		Assert.assertTrue(JOIDEnv.isDevMode());
		Assert.assertTrue(JOIDEnv.isDemoMode());
	}

	@Test(expected = IllegalStateException.class)
	public void refusesTheDevModeWithoutTheDevClasses() throws Throwable {
		JOIDEnvTest.switchOn("devMode", "DevNode.class");
	}

	@Test(expected = IllegalStateException.class)
	public void refusesTheDemoModeWithoutTheDemoClasses() throws Throwable {
		JOIDEnvTest.switchOn("demoMode", "DemoFont.class");
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

				try (InputStream input = super.getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
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