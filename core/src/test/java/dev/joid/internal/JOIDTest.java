package dev.joid.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import dev.joid.demo.DemoFont;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.thread.QueueThreadBridge;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.popup.UIDataPopup;
import dev.joid.lib.ui.core.data.popup.UIDataPopup.PopupTransition;
import dev.joid.lib.ui.core.hook.property.UIProperty;
import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.UIStoreHook;
import dev.joid.lib.ui.core.hook.store.data.UIStoreData;
import dev.joid.lib.ui.core.hook.store.scope.StoreScope;
import dev.joid.lib.ui.node.Node;
import lombok.NonNull;

public class JOIDTest {

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private File    dataDir;
	private boolean devMode;
	private boolean demoMode;

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void rememberTheSettings() {
		this.dataDir = JOIDEnv.getDataDir();
		this.devMode = JOIDEnv.isDevMode();
		this.demoMode = JOIDEnv.isDemoMode();
	}

	@After
	public void restoreTheSettings() {
		JOIDEnv.dataDir(this.dataDir);
		JOIDEnv.devMode(this.devMode);
		JOIDEnv.demoMode(this.demoMode);
	}

	@Test
	public void keepsOneInstance() {
		Assert.assertSame(JOID.inst(), JOID.inst());
	}

	@Test
	public void printsItsSettingsOnLoad() {
		final JOID joid = new JOID();
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
		Assert.assertTrue(text, text.contains(" - DataDir: " + JOIDEnv.getDataDir().getAbsolutePath()));
		Assert.assertTrue(text, text.contains(" - CacheDir: " + JOIDEnv.getCacheDir().getAbsolutePath()));
		Assert.assertTrue(text, text.contains(" - DevMode: true"));
		Assert.assertTrue(text, text.contains(" - DemoMode: true"));
		Assert.assertTrue(text, text.contains(" - Version: " + JOID.VERSION));
	}

	@Test
	public void loadsNoFontOutsideTheDevAndDemoModes() {
		final MsdfFont previous = DevFont.MONTSERRAT;
		DevFont.MONTSERRAT = null;
		try {
			JOIDEnv.devMode(false);
			JOIDEnv.demoMode(false);
			JOIDTest.quietly(new JOID());
			Assert.assertNull(DevFont.MONTSERRAT);
			Assert.assertNull(DemoFont.MONTSERRAT);
		} finally {
			DevFont.MONTSERRAT = previous;
		}
	}

	@Test
	public void loadsTheInternalFontInDevMode() {
		final MsdfFont previous = DevFont.MONTSERRAT;
		DevFont.MONTSERRAT = null;
		try {
			JOIDEnv.demoMode(false);
			JOIDTest.quietly(new JOID());
			Assert.assertNotNull(DevFont.MONTSERRAT);
			Assert.assertNull(DemoFont.MONTSERRAT);
		} finally {
			DevFont.MONTSERRAT = previous;
		}
	}

	@Test
	public void loadsTheDemoFontsInDemoMode() {
		final MsdfFont previous = DevFont.MONTSERRAT;
		try {
			JOIDEnv.devMode(false);
			JOIDTest.quietly(new JOID());
			Assert.assertTrue(DemoFont.isLoaded());
			Assert.assertSame(DevFont.MONTSERRAT, DemoFont.MONTSERRAT);
		} finally {
			DevFont.MONTSERRAT = previous;
			DemoFont.MONTSERRAT = null;
			DemoFont.PACIFICO = null;
			DemoFont.PLAYFAIR_DISPLAY = null;
		}
	}

	@Test
	public void savesTheOpenedUisAndTheSharedStoresOnShutdown() throws ReflectiveOperationException, IOException {
		JOIDEnv.dataDir(this.folder.getRoot());
		final SettingsUI settings = new SettingsUI();
		this.bridges.open(settings);
		final ExitStore store = UIStoreHook.useStore(ExitStore.class);
		settings.volume = 3;
		store.value = "kept";

		final Method shutdown = JOID.class.getDeclaredMethod("shutdown");
		shutdown.setAccessible(true);
		shutdown.invoke(null);

		final File property = new File(new File(this.folder.getRoot(), "property"), SettingsUI.class.getName() + ".property");
		final File saved = new File(new File(this.folder.getRoot(), "store"), "exit.store");
		Assert.assertEquals("{\"volume\":3}", new String(Files.readAllBytes(property.toPath()), StandardCharsets.UTF_8));
		Assert.assertEquals("kept", new String(Files.readAllBytes(saved.toPath()), StandardCharsets.UTF_8));
		Assert.assertTrue(store.destroyed);
		Assert.assertTrue(this.bridges.getUi().isOpen(settings));
	}

	@Test
	public void runsInDevelopmentOutsideAJar() {
		Assert.assertEquals("development", JOID.VERSION);
	}

	@Test
	public void comparesNothingInDevelopment() throws Throwable {
		JOID.checkVersion("70.0.1");
		JOIDTest.checkVersion("development", "8.0.0");
	}

	@Test
	public void acceptsABackendOfTheSameMajorVersion() throws Throwable {
		JOIDTest.checkVersion("8.0.0", "8.0.0");
		JOIDTest.checkVersion("8.99.3", "8.0.0");
		JOIDTest.checkVersion("8", "8.0.0");
	}

	@Test
	public void refusesABackendOfAnotherMajorVersion() throws Throwable {
		try {
			JOIDTest.checkVersion("70.0.1", "8.0.0");
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("The backend was built for JOID 70.x, but JOID 8.0.0 is loaded: update the backend", exception.getMessage());
		}
	}

	@Test
	public void findsAnOpenedUiByItsClass() {
		final MenuUI menu = new MenuUI();
		Assert.assertFalse(JOID.isOpen(MenuUI.class));
		Assert.assertNull(JOID.getUi(MenuUI.class));
		this.bridges.open(menu);
		Assert.assertTrue(JOID.isOpen(MenuUI.class));
		Assert.assertSame(menu, JOID.getUi(MenuUI.class));
		Assert.assertSame(menu, JOID.getUi(UI.class));
		Assert.assertFalse(JOID.isOpen(PopupUI.class));
	}

	@Test
	public void findsNoUiWithoutBridge() {
		this.bridges.open(new MenuUI());
		BridgeHandler.UI.unregister(this.bridges.getUi());
		try {
			Assert.assertFalse(JOID.isOpen(MenuUI.class));
			Assert.assertNull(JOID.getUi(MenuUI.class));
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
	public void closesNothingWhenTheUiIsNotOpen() {
		final MenuUI menu = new MenuUI();
		JOID.close(menu);
		JOID.close(menu, true);
		Assert.assertEquals(0, menu.closes);
		Assert.assertEquals(0, menu.detaches);
		Assert.assertFalse(menu.isClosed());
	}

	@Test
	public void closesAUiThatEscapeCannotClose() {
		final MenuUI menu = new MenuUI();
		menu.getData().setCloseable(false);
		JOID.open(menu);
		JOID.close(menu);
		Assert.assertFalse(this.bridges.getUi().isOpen(menu));
	}

	@Test
	public void keepsAUiThatRefusesToClose() {
		final MenuUI menu = new MenuUI();
		menu.closeable = false;
		JOID.open(menu);
		JOID.close(menu, false);
		Assert.assertTrue(this.bridges.getUi().isOpen(menu));
		Assert.assertEquals(0, menu.detaches);
	}

	@Test
	public void forcesAUiToClose() {
		final MenuUI menu = new MenuUI();
		menu.closeable = false;
		JOID.open(menu);
		JOID.close(menu, true);
		Assert.assertFalse(this.bridges.getUi().isOpen(menu));
		Assert.assertEquals(0, menu.closes);
		Assert.assertEquals(1, menu.detaches);
	}

	@Test
	public void opensAndClosesOnTheRenderThreadFromAnotherThread() {
		final QueueThreadBridge thread = new QueueThreadBridge();
		final MenuUI menu = new MenuUI();
		BridgeHandler.THREAD.register(thread);
		try {
			Assert.assertSame(this.bridges.getUi(), JOID.open(menu));
			Assert.assertFalse(this.bridges.getUi().isOpen(menu));
			thread.run();
			Assert.assertTrue(this.bridges.getUi().isOpen(menu));
			JOID.close(menu);
			Assert.assertEquals(0, menu.closes);
			thread.run();
			Assert.assertFalse(this.bridges.getUi().isOpen(menu));
			Assert.assertEquals(1, menu.closes);
			JOID.open(menu, true);
			JOID.close(menu, true);
			thread.run();
			Assert.assertFalse(this.bridges.getUi().isOpen(menu));
		} finally {
			BridgeHandler.THREAD.unregister(thread);
		}
	}

	@Test
	public void opensAndClosesAtOnceOnTheRenderThread() {
		final QueueThreadBridge thread = new QueueThreadBridge();
		thread.renderThread(true);
		final MenuUI menu = new MenuUI();
		BridgeHandler.THREAD.register(thread);
		try {
			JOID.open(menu);
			Assert.assertTrue(this.bridges.getUi().isOpen(menu));
			JOID.close(menu);
			Assert.assertFalse(this.bridges.getUi().isOpen(menu));
			Assert.assertTrue(thread.isIdle());
		} finally {
			BridgeHandler.THREAD.unregister(thread);
		}
	}

	@Test
	public void runsOnTheCallingThreadByDefault() {
		final List<String> trace = new ArrayList<>();
		Assert.assertTrue(BridgeHandler.THREAD.get().isRenderThread());
		BridgeHandler.THREAD.get().execute(() -> trace.add("run"));
		Assert.assertEquals(Collections.singletonList("run"), trace);
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
		Assert.assertTrue(this.bridges.getUi().isOpen(menu));
		Assert.assertEquals(0, menu.closes);
	}

	@Test(expected = NullPointerException.class)
	public void refusesAMissingVersion() {
		JOID.checkVersion(null);
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
		JOID.getUi(null);
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
	public void createsNoDataFolderOnLoad() {
		final File data = new File(this.folder.getRoot(), "joid");
		JOIDEnv.dataDir(data);
		JOIDTest.quietly(new JOID());
		Assert.assertFalse(data.exists());
	}

	@Test
	public void loadsOnce() {
		final JOID joid = new JOID();
		JOIDTest.quietly(joid);
		final PrintStream previous = System.out;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setOut(new PrintStream(output, true));
		try {
			Assert.assertSame(joid, joid.load());
		} finally {
			System.setOut(previous);
		}
		Assert.assertTrue(joid.isReady());
		Assert.assertEquals(0, output.size());
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
		Assert.assertTrue(this.bridges.getUi().isOpen(menu));
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

	private static void checkVersion(final String version, final String loaded) throws Throwable {
		final Method check = JOID.class.getDeclaredMethod("checkVersion", String.class, String.class);
		check.setAccessible(true);
		try {
			check.invoke(null, version, loaded);
		} catch (final InvocationTargetException exception) {
			throw exception.getCause();
		}
	}

	public static final class MenuUI extends UI {

		private int     inits;
		private int     closes;
		private int     detaches;
		private boolean closeable = true;

		public MenuUI() {
			super.beforeClose(event -> {
				this.closes++;
				if (!this.closeable) {
					event.cancel();
				}
			});
		}

		@Override
		public void init() {
			this.inits++;
			new CountingNode(this).attach(this);
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

	public static final class SettingsUI extends UI {

		@UIProperty
		private int volume = 1;

	}

	@UIStoreData(id = "exit", scope = StoreScope.PERMANENT)
	public static final class ExitStore extends UIStore {

		private String  value = "default";
		private boolean destroyed;

		@Override
		public void destroy() {
			this.destroyed = true;
		}

		@Override
		public @NonNull String toJson() {
			return this.value;
		}

	}

	public static final class StubbornUI extends UI {

		public StubbornUI() {
			super.beforeClose(event -> event.cancel());
		}

	}

}