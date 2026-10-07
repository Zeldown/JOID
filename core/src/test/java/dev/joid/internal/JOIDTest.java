package dev.joid.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.demo.DemoFont;
import dev.joid.internal.font.InternalFont;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.popup.UIDataPopup;
import dev.joid.lib.ui.core.data.popup.UIDataPopup.PopupTransition;
import dev.joid.lib.ui.node.Node;

public class JOIDTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private File    configDir;
	private boolean devMode;
	private boolean demoMode;

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void rememberTheSettings() {
		this.configDir = JOID.inst().getConfigDir();
		this.devMode = JOID.inst().isDevMode();
		this.demoMode = JOID.inst().isDemoMode();
	}

	@After
	public void restoreTheSettings() {
		JOID.inst().setConfigDir(this.configDir).setDevMode(this.devMode).setDemoMode(this.demoMode);
	}

	@Test
	public void keepsOneInstance() {
		Assert.assertSame(JOID.inst(), JOID.inst());
	}

	@Test
	public void startsInTheConfigFolderWithoutDevNorDemoMode() {
		final String property = System.clearProperty("joid.config");
		try {
			final JOID joid = new JOID();
			Assert.assertEquals(new File("config"), joid.getConfigDir());
			Assert.assertFalse(joid.isDevMode());
			Assert.assertFalse(joid.isDemoMode());
		} finally {
			if (property != null) {
				System.setProperty("joid.config", property);
			}
		}
	}

	@Test
	public void readsItsConfigFolderFromTheJoidConfigProperty() {
		final File config = new File(this.folder.getRoot(), "config");
		final String property = System.setProperty("joid.config", config.getPath());
		try {
			Assert.assertEquals(config, new JOID().getConfigDir());
		} finally {
			if (property == null) {
				System.clearProperty("joid.config");
			} else {
				System.setProperty("joid.config", property);
			}
		}
	}

	@Test
	public void chainsItsSettings() {
		final JOID joid = new JOID();
		final File folder = new File("settings");
		Assert.assertSame(joid, joid.setConfigDir(folder));
		Assert.assertSame(joid, joid.setDevMode(true));
		Assert.assertSame(joid, joid.setDemoMode(true));
		Assert.assertSame(folder, joid.getConfigDir());
		Assert.assertTrue(joid.isDevMode());
		Assert.assertTrue(joid.isDemoMode());
		joid.setDevMode(false).setDemoMode(false);
		Assert.assertFalse(joid.isDevMode());
		Assert.assertFalse(joid.isDemoMode());
	}

	@Test(expected = IllegalStateException.class)
	public void refusesTheDevModeWithoutTheDevClasses() throws Throwable {
		JOIDTest.prod("setDevMode");
	}

	@Test(expected = IllegalStateException.class)
	public void refusesTheDemoModeWithoutTheDemoClasses() throws Throwable {
		JOIDTest.prod("setDemoMode");
	}

	@Test
	public void turnsTheModesOffWithoutTheDevClasses() throws Throwable {
		Assert.assertNotNull(JOIDTest.prod("setDevMode", false));
		Assert.assertNotNull(JOIDTest.prod("setDemoMode", false));
	}

	@Test
	public void printsItsSettingsOnLoad() {
		final JOID joid = new JOID().setConfigDir(new File("loaded"));
		final PrintStream previous = System.out;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setOut(new PrintStream(output, true));
		try {
			Assert.assertSame(joid, joid.load());
		} finally {
			System.setOut(previous);
		}
		final String text = output.toString();
		Assert.assertTrue(text, text.contains("[JOID] Loading JOID with parameters:"));
		Assert.assertTrue(text, text.contains(" - ConfigDir: " + new File("loaded").getAbsolutePath()));
		Assert.assertTrue(text, text.contains(" - DevMode: false"));
		Assert.assertTrue(text, text.contains(" - DemoMode: false"));
		Assert.assertTrue(text, text.contains(" - Version: " + JOID.VERSION));
	}

	@Test
	public void loadsNoFontOutsideTheDevAndDemoModes() {
		final MsdfFont previous = InternalFont.MONTSERRAT;
		InternalFont.MONTSERRAT = null;
		try {
			JOIDTest.quietly(new JOID());
			Assert.assertNull(InternalFont.MONTSERRAT);
			Assert.assertNull(DemoFont.MONTSERRAT);
		} finally {
			InternalFont.MONTSERRAT = previous;
		}
	}

	@Test
	public void loadsTheInternalFontInDevMode() {
		final MsdfFont previous = InternalFont.MONTSERRAT;
		InternalFont.MONTSERRAT = null;
		try {
			JOIDTest.quietly(new JOID().setDevMode(true));
			Assert.assertNotNull(InternalFont.MONTSERRAT);
			Assert.assertNull(DemoFont.MONTSERRAT);
		} finally {
			InternalFont.MONTSERRAT = previous;
		}
	}

	@Test
	public void loadsTheDemoFontsInDemoMode() {
		final MsdfFont previous = InternalFont.MONTSERRAT;
		try {
			JOIDTest.quietly(new JOID().setDemoMode(true));
			Assert.assertTrue(DemoFont.isLoaded());
			Assert.assertSame(InternalFont.MONTSERRAT, DemoFont.MONTSERRAT);
		} finally {
			InternalFont.MONTSERRAT = previous;
			DemoFont.MONTSERRAT = null;
			DemoFont.PACIFICO = null;
			DemoFont.PLAYFAIR_DISPLAY = null;
		}
	}

	@Test
	public void acceptsABackendOfTheSameMajorVersion() {
		final String major = JOID.VERSION.split("[.]")[0];
		Assert.assertTrue(JOID.checkVersion(JOID.VERSION));
		Assert.assertTrue(JOID.checkVersion(major + ".99.3"));
		Assert.assertTrue(JOID.checkVersion(major));
	}

	@Test
	public void warnsAboutABackendOfAnotherMajorVersion() {
		final PrintStream previous = System.err;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setErr(new PrintStream(output, true));
		try {
			Assert.assertFalse(JOID.checkVersion("6.2.1"));
			Assert.assertFalse(JOID.checkVersion("70.0.1"));
		} finally {
			System.setErr(previous);
		}
		Assert.assertTrue(output.toString(), output.toString().startsWith("[JOID] This backend targets JOID 6.2.1 but JOID " + JOID.VERSION + " is loaded"));
		Assert.assertTrue(output.toString(), output.toString().contains("[JOID] This backend targets JOID 70.0.1 but JOID " + JOID.VERSION + " is loaded"));
	}

	@Test
	public void findsAnOpenedUiByItsClass() {
		final MenuUI menu = new MenuUI();
		Assert.assertFalse(JOID.isOpen(MenuUI.class));
		Assert.assertNull(JOID.getUI(MenuUI.class));
		this.bridges.open(menu);
		Assert.assertTrue(JOID.isOpen(MenuUI.class));
		Assert.assertSame(menu, JOID.getUI(MenuUI.class));
		Assert.assertSame(menu, JOID.getUI(UI.class));
		Assert.assertFalse(JOID.isOpen(PopupUI.class));
	}

	@Test
	public void findsNoUiWithoutBridge() {
		this.bridges.open(new MenuUI());
		BridgeHandler.UI.unregister(this.bridges.getUi());
		try {
			Assert.assertFalse(JOID.isOpen(MenuUI.class));
			Assert.assertNull(JOID.getUI(MenuUI.class));
		} finally {
			BridgeHandler.UI.register(this.bridges.getUi());
		}
	}

	@Test
	public void opensAUiThroughItsBridge() {
		final MenuUI menu = new MenuUI();
		Assert.assertSame(this.bridges.getUi(), JOID.open(menu));
		Assert.assertEquals(Collections.singletonList(menu), this.bridges.getUi().getUiList().ordered());
		Assert.assertEquals(1, menu.inits);
	}

	@Test
	public void opensAUiInPlaceOfTheOpenedOne() {
		final MenuUI first = new MenuUI();
		final MenuUI second = new MenuUI();
		JOID.open(first);
		JOID.open(second, false);
		Assert.assertEquals(1, first.closes);
		Assert.assertEquals(Collections.singletonList(second), this.bridges.getUi().getUiList().ordered());
	}

	@Test
	public void opensAPopupAboveTheOpenedUi() {
		final MenuUI menu = new MenuUI();
		final PopupUI popup = new PopupUI();
		JOID.open(menu);
		JOID.open(popup);
		Assert.assertEquals(Arrays.asList(menu, popup), this.bridges.getUi().getUiList().ordered());
	}

	@Test
	public void forcesTheOpenedUiToClose() {
		final MenuUI menu = new MenuUI();
		final MenuUI next = new MenuUI();
		menu.closeable = false;
		JOID.open(menu);
		JOID.open(next);
		Assert.assertEquals(Collections.singletonList(menu), this.bridges.getUi().getUiList().ordered());
		Assert.assertSame(this.bridges.getUi(), JOID.open(next, true));
		Assert.assertEquals(Collections.singletonList(next), this.bridges.getUi().getUiList().ordered());
		Assert.assertEquals(1, menu.detaches);
	}

	@Test
	public void refusesToOpenAUiWithoutBridge() {
		BridgeHandler.UI.unregister(this.bridges.getUi());
		try {
			JOIDTest.assertNoBridge(() -> JOID.open(new MenuUI()));
			JOIDTest.assertNoBridge(() -> JOID.open(new MenuUI(), true));
		} finally {
			BridgeHandler.UI.register(this.bridges.getUi());
		}
		Assert.assertTrue(this.bridges.getUi().getUiList().isEmpty());
	}

	@Test
	public void closesAUiThroughItsBridge() {
		final MenuUI menu = new MenuUI();
		JOID.open(menu);
		JOID.close(menu);
		Assert.assertTrue(this.bridges.getUi().getUiList().isEmpty());
		Assert.assertEquals(1, menu.closes);
		Assert.assertEquals(1, menu.detaches);
	}

	@Test
	public void closesAUiThatEscapeCannotClose() {
		final MenuUI menu = new MenuUI();
		menu.getData().setCloseable(false);
		JOID.open(menu);
		JOID.close(menu);
		Assert.assertFalse(this.bridges.getUi().isOpened(menu));
	}

	@Test
	public void keepsAUiThatRefusesToClose() {
		final MenuUI menu = new MenuUI();
		menu.closeable = false;
		JOID.open(menu);
		JOID.close(menu, false);
		Assert.assertTrue(this.bridges.getUi().isOpened(menu));
		Assert.assertEquals(0, menu.detaches);
	}

	@Test
	public void forcesAUiToClose() {
		final MenuUI menu = new MenuUI();
		menu.closeable = false;
		JOID.open(menu);
		JOID.close(menu, true);
		Assert.assertFalse(this.bridges.getUi().isOpened(menu));
		Assert.assertEquals(0, menu.closes);
		Assert.assertEquals(1, menu.detaches);
	}

	@Test
	public void closesNothingWithoutBridge() {
		final MenuUI menu = new MenuUI();
		JOID.open(menu);
		BridgeHandler.UI.unregister(this.bridges.getUi());
		try {
			JOID.close(menu);
		} finally {
			BridgeHandler.UI.register(this.bridges.getUi());
		}
		Assert.assertTrue(this.bridges.getUi().isOpened(menu));
		Assert.assertEquals(0, menu.closes);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingVersion() {
		JOID.checkVersion(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingConfigFolder() {
		new JOID().setConfigDir(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToLookForAMissingClass() {
		JOID.isOpen((Class<? extends UI>) null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToLookForAMissingUi() {
		JOID.isOpen((UI) null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToGetAMissingClass() {
		JOID.getUI(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToOpenAMissingUi() {
		JOID.open(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToForceAMissingUiOpen() {
		JOID.open(null, true);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToCloseAMissingUi() {
		JOID.close(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToForceAMissingUiClosed() {
		JOID.close(null, true);
	}

	@Test
	public void createsNoConfigFolderBeforeWritingIntoIt() {
		final File config = new File(this.folder.getRoot(), "config");
		final String property = System.setProperty("joid.config", config.getPath());
		final PrintStream previous = System.out;
		System.setOut(new PrintStream(new ByteArrayOutputStream(), true));
		try {
			new JOID().load();
			new JOID().setConfigDir(config).load();
		} finally {
			System.setOut(previous);
			if (property == null) {
				System.clearProperty("joid.config");
			} else {
				System.setProperty("joid.config", property);
			}
		}
		Assert.assertFalse(config.exists());
	}

	@Test
	public void forcesNothingToCloseWithoutBridge() {
		final StubbornUI menu = new StubbornUI();
		this.bridges.open(menu);
		BridgeHandler.UI.unregister(this.bridges.getUi());
		try {
			JOID.close(menu, true);
		} finally {
			BridgeHandler.UI.register(this.bridges.getUi());
		}
		Assert.assertTrue(this.bridges.getUi().isOpened(menu));
	}

	@Test
	public void forcesEveryOpenedUiToClose() {
		final StubbornUI menu = new StubbornUI();
		final StubbornUI hud = new StubbornUI();
		final StubbornUI next = new StubbornUI();
		this.bridges.open(menu).open(hud);
		JOID.open(next, true);
		Assert.assertEquals(Collections.singletonList(next), this.bridges.getUi().getUiList().ordered());
	}

	private static void quietly(final JOID joid) {
		final PrintStream previous = System.out;
		System.setOut(new PrintStream(new ByteArrayOutputStream(), true));
		try {
			joid.load();
		} finally {
			System.setOut(previous);
		}
	}

	private static void assertNoBridge(final Runnable open) {
		try {
			open.run();
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("No IUIBridge can open MenuUI: register one whose canHandle accepts it", exception.getMessage());
		}
	}

	private static Object prod(final String setter) throws Throwable {
		return JOIDTest.prod(setter, true);
	}

	private static Object prod(final String setter, final boolean value) throws Throwable {
		final Class<?> clazz = new ProdClassLoader().loadClass(JOID.class.getName());
		final Object joid = clazz.getConstructor().newInstance();
		try {
			return clazz.getMethod(setter, boolean.class).invoke(joid, value);
		} catch (final InvocationTargetException e) {
			throw e.getCause();
		}
	}

	public static final class MenuUI extends UI {

		private int     inits;
		private int     closes;
		private int     detaches;
		private boolean closeable = true;

		@Override
		public void init() {
			this.inits++;
			new CountingNode(this).attach(this);
		}

		@Override
		public boolean close() {
			this.closes++;
			return this.closeable;
		}

	}

	@UIDataPopup(active = true, transition = PopupTransition.NONE)
	public static final class PopupUI extends UI {}

	public static final class CountingNode extends Node {

		private final MenuUI menu;

		private CountingNode(final MenuUI menu) {
			super(0D, 0D, 10D, 10D);
			this.menu = menu;
		}

		@Override
		public void detach() {
			this.menu.detaches++;
		}

	}

	public static final class ProdClassLoader extends ClassLoader {

		private ProdClassLoader() {
			super(JOIDTest.class.getClassLoader());
		}

		@Override
		protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
			if (!name.equals(JOID.class.getName())) {
				return super.loadClass(name, resolve);
			}

			synchronized (super.getClassLoadingLock(name)) {
				final Class<?> loaded = super.findLoadedClass(name);
				if (loaded != null) {
					return loaded;
				}

				try (InputStream input = super.getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
					final byte[] bytes = IOUtils.toByteArray(input);
					return super.defineClass(name, bytes, 0, bytes.length, JOID.class.getProtectionDomain());
				} catch (final IOException e) {
					throw new ClassNotFoundException(name, e);
				}
			}
		}

		@Override
		public URL getResource(final String name) {
			if (name.endsWith("DevNode.class") || name.endsWith("DemoFont.class")) {
				return null;
			}

			return super.getResource(name);
		}

	}

	public static final class StubbornUI extends UI {

		@Override
		public boolean close() {
			return false;
		}

	}

}