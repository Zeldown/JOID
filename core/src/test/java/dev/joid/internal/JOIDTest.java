package dev.joid.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
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
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.gson.JsonObject;

import dev.joid.demo.DemoFont;
import dev.joid.demo.font.impl.bitmap.DemoPixelFont;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.thread.FrameThreadBridge;
import dev.joid.lib.bridge.thread.QueueThreadBridge;
import dev.joid.lib.bridge.ui.UIBridgeRegistryTest.RoutingBridge;
import dev.joid.lib.cache.TemporaryCache;
import dev.joid.lib.font.impl.msdf.MsdfFont;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.data.UIDataDebug;
import dev.joid.lib.ui.core.data.UIDataPopup;
import dev.joid.lib.ui.core.data.UIDataPopup.PopupTransition;
import dev.joid.lib.ui.core.hook.property.UIProperty;
import dev.joid.lib.ui.core.hook.store.UIStore;
import dev.joid.lib.ui.core.hook.store.UIStoreData;
import dev.joid.lib.ui.core.hook.store.UIStoreHook;
import dev.joid.lib.ui.core.hook.store.UIStoreScope;
import dev.joid.lib.ui.core.transition.PopTransition;
import dev.joid.lib.ui.node.Node;
import lombok.NonNull;

public class JOIDTest {

	@ClassRule
	public static final TemporaryCache CACHE = new TemporaryCache();

	@Rule
	public final HeadlessBridges bridges = new HeadlessBridges();

	private File    dataDir;
	private boolean devMode;
	private boolean demoMode;

	private MsdfFont      devFont;
	private MsdfFont      pacifico;
	private MsdfFont      montserrat;
	private DemoPixelFont pixel;
	private MsdfFont      playfairDisplay;

	@Rule
	public final TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void rememberTheSettings() {
		this.dataDir = JOIDEnv.getDataDir();
		this.devMode = JOIDEnv.isDevMode();
		this.demoMode = JOIDEnv.isDemoMode();
		this.devFont = DevFont.MONTSERRAT;
		this.pacifico = DemoFont.PACIFICO;
		this.montserrat = DemoFont.MONTSERRAT;
		this.pixel = DemoFont.PIXEL;
		this.playfairDisplay = DemoFont.PLAYFAIR_DISPLAY;
	}

	@After
	public void restoreTheSettings() {
		JOIDEnv.dataDir(this.dataDir);
		JOIDEnv.devMode(this.devMode);
		TestUtils.demoMode(this.demoMode);
		DevFont.MONTSERRAT = this.devFont;
		DemoFont.PACIFICO = this.pacifico;
		DemoFont.MONTSERRAT = this.montserrat;
		DemoFont.PIXEL = this.pixel;
		DemoFont.PLAYFAIR_DISPLAY = this.playfairDisplay;
	}

	@Test
	public void keepsOneInstance() {
		Assert.assertSame(JOID.inst(), JOID.inst());
	}

	@Test
	public void printsItsSettingsOnLoad() {
		final JOID joid = JOIDTest.create();
		final PrintStream previous = System.out;
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		System.setOut(new PrintStream(output, true));
		try {
			Assert.assertSame(joid, joid.load());
		} finally {
			System.setOut(previous);
		}
		final String text = output.toString();
		Assert.assertTrue(text, text.startsWith("[JOID] " + JOID.VERSION + " loaded in dev and demo mode, data in " + JOIDEnv.getDataDir().getAbsolutePath() + ", cache in " + JOIDEnv.getCacheDir().getAbsolutePath() + System.lineSeparator()));
	}

	@Test
	public void loadsNoFontOutsideTheDevAndDemoModes() {
		DevFont.MONTSERRAT = null;
		DemoFont.MONTSERRAT = null;
		JOIDEnv.devMode(false);
		TestUtils.demoMode(false);
		JOIDTest.quietly(JOIDTest.create());
		Assert.assertNull(DevFont.MONTSERRAT);
		Assert.assertNull(DemoFont.MONTSERRAT);
	}

	@Test
	public void loadsTheInternalFontInDevMode() {
		DevFont.MONTSERRAT = null;
		DemoFont.MONTSERRAT = null;
		TestUtils.demoMode(false);
		JOIDTest.quietly(JOIDTest.create());
		Assert.assertNotNull(DevFont.MONTSERRAT);
		Assert.assertNull(DemoFont.MONTSERRAT);
	}

	@Test
	public void loadsTheDemoFontsInDemoMode() {
		JOIDEnv.devMode(false);
		JOIDTest.quietly(JOIDTest.create());
		Assert.assertNotNull(DemoFont.MONTSERRAT);
		Assert.assertNotNull(DemoFont.PACIFICO);
		Assert.assertNotNull(DemoFont.PLAYFAIR_DISPLAY);
		Assert.assertNotNull(DemoFont.PIXEL);
		Assert.assertSame(DevFont.MONTSERRAT, DemoFont.MONTSERRAT);
	}

	@Test
	public void savesTheOpenedUisAndTheSharedStoresOnShutdown() throws ReflectiveOperationException, IOException {
		JOIDEnv.dataDir(this.folder.getRoot());
		final SettingsUI settings = new SettingsUI();
		this.bridges.open(settings);
		final ExitStore store = UIStoreHook.useStore(ExitStore.class, ExitStore::new);
		settings.volume = 3;
		store.value = "kept";

		final Method shutdown = JOID.class.getDeclaredMethod("shutdown");
		shutdown.setAccessible(true);
		try {
			shutdown.invoke(null);
			Assert.assertFalse(store.destroyed);
		} finally {
			UIStoreHook.destroyStore(store);
		}

		final File property = new File(new File(this.folder.getRoot(), "property"), SettingsUI.class.getName() + ".property");
		final File saved = new File(new File(this.folder.getRoot(), "store"), "exit.store");
		Assert.assertEquals("{\"volume\":3}", new String(Files.readAllBytes(property.toPath()), StandardCharsets.UTF_8));
		Assert.assertEquals("{\"value\":\"kept\"}", new String(Files.readAllBytes(saved.toPath()), StandardCharsets.UTF_8));
		Assert.assertTrue(this.bridges.getUi().isOpen(settings));
		this.bridges.getUi().remove(settings);
		settings.dispose();
	}

	@Test
	public void savesOnTheRenderThreadOnShutdown() throws InterruptedException {
		JOIDEnv.dataDir(this.folder.getRoot());
		final FrameThreadBridge render = new FrameThreadBridge();
		final ExitStore store = UIStoreHook.useStore(ExitStore.class, ExitStore::new);
		store.savers.clear();
		BridgeHandler.THREAD.register(render);
		try {
			final Thread shutdown = JOIDTest.shutdown();
			shutdown.start();
			final long deadline = System.currentTimeMillis() + 10000L;
			while (shutdown.isAlive()) {
				Assert.assertTrue("The shutdown did not end in 10 s", System.currentTimeMillis() < deadline);
				render.runTasks();
				shutdown.join(1L);
			}
		} finally {
			BridgeHandler.THREAD.unregister(render);
			UIStoreHook.destroyStore(store);
		}
		Assert.assertEquals(Collections.singletonList(Thread.currentThread()), store.savers);
	}

	@Test
	public void savesOnceOnShutdownWhenTheRenderThreadDoesNotAnswer() throws InterruptedException {
		JOIDEnv.dataDir(this.folder.getRoot());
		final FrameThreadBridge render = new FrameThreadBridge();
		final ExitStore store = UIStoreHook.useStore(ExitStore.class, ExitStore::new);
		store.savers.clear();
		BridgeHandler.THREAD.register(render);
		final Thread shutdown = JOIDTest.shutdown();
		try {
			shutdown.start();
			shutdown.join();
			render.runTasks();
		} finally {
			BridgeHandler.THREAD.unregister(render);
			UIStoreHook.destroyStore(store);
		}
		Assert.assertEquals(Collections.singletonList(shutdown), store.savers);
	}

	@Test
	public void readsTheVersionOfItsBuild() {
		Assert.assertEquals(System.getProperty("joid.version", "development"), JOID.VERSION);
	}

	@Test
	public void readsTheVersionOfItsResource() throws Throwable {
		Assert.assertEquals("8.1.0", JOIDTest.readVersion("version=8.1.0"));
	}

	@Test
	public void runsInDevelopmentWithoutAnExpandedVersion() throws Throwable {
		Assert.assertEquals("development", JOIDTest.readVersion(null));
		Assert.assertEquals("development", JOIDTest.readVersion(""));
		Assert.assertEquals("development", JOIDTest.readVersion("version=${version}"));
	}

	@Test
	public void comparesNothingInDevelopment() throws Throwable {
		JOIDTest.checkVersion("development", "8.0.0");
		JOIDTest.checkVersion("70.0.1", "development");
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
	public void findsAnOpenedUiInEveryBridge() {
		final RoutingBridge other = new RoutingBridge(MenuUI.class);
		final MenuUI menu = new MenuUI();
		BridgeHandler.UI.register(other, -1);
		try {
			other.add(menu);
			Assert.assertSame(menu, JOID.getUi(MenuUI.class));
			Assert.assertTrue(JOID.isOpen(MenuUI.class));
		} finally {
			BridgeHandler.UI.unregister(other);
		}
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
		Assert.assertEquals(Collections.singletonList(menu), this.bridges.getUi().getUiList());
		Assert.assertEquals(1, menu.inits);
	}

	@Test
	public void opensAUiInPlaceOfTheOpenedOne() {
		final MenuUI first = new MenuUI();
		final MenuUI second = new MenuUI();
		JOID.open(first);
		JOID.open(second);
		Assert.assertEquals(1, first.closes);
		Assert.assertEquals(Collections.singletonList(second), this.bridges.getUi().getUiList());
	}

	@Test
	public void opensAPopupAboveTheOpenedUi() {
		final MenuUI menu = new MenuUI();
		final PopupUI popup = new PopupUI();
		JOID.open(menu);
		JOID.open(popup);
		Assert.assertEquals(Arrays.asList(menu, popup), this.bridges.getUi().getUiList());
	}

	@Test
	public void forcesTheOpenedUiToClose() {
		final MenuUI menu = new MenuUI();
		final MenuUI next = new MenuUI();
		menu.closeable = false;
		JOID.open(menu);
		JOID.open(next);
		Assert.assertEquals(Collections.singletonList(menu), this.bridges.getUi().getUiList());
		Assert.assertSame(this.bridges.getUi(), JOID.forceOpen(next));
		Assert.assertEquals(Collections.singletonList(next), this.bridges.getUi().getUiList());
		Assert.assertEquals(1, menu.detaches);
	}

	@Test
	public void forcesAUiOpenWithoutOpeningTheWaitingOne() {
		final MenuUI menu = new MenuUI();
		final MenuUI waiting = new MenuUI();
		final MenuUI next = new MenuUI();
		menu.transition(PopTransition.create());
		JOID.open(menu);
		this.bridges.frames(30);
		JOID.open(waiting);
		Assert.assertTrue(menu.getTransition().getOut().isRunning());
		JOID.forceOpen(next);
		this.bridges.frames(30);
		Assert.assertEquals(Collections.singletonList(next), this.bridges.getUi().getUiList());
		Assert.assertEquals(0, waiting.inits);
	}

	@Test
	public void refusesToOpenAUiWithoutBridge() {
		BridgeHandler.UI.unregister(this.bridges.getUi());
		try {
			JOIDTest.assertNoBridge(() -> JOID.open(new MenuUI()));
			JOIDTest.assertNoBridge(() -> JOID.forceOpen(new MenuUI()));
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
		JOID.forceClose(menu);
		Assert.assertEquals(0, menu.closes);
		Assert.assertEquals(0, menu.detaches);
		Assert.assertFalse(menu.isClosed());
	}

	@Test
	public void closesAUiThatWaitsForTheExitOfTheOpenedOne() {
		final MenuUI menu = new MenuUI();
		final MenuUI waiting = new MenuUI();
		final MenuUI forced = new MenuUI();
		menu.transition(PopTransition.create());
		JOID.open(menu);
		this.bridges.frames(30);
		JOID.open(waiting);
		JOID.close(waiting);
		this.bridges.frames(30);
		Assert.assertTrue(this.bridges.getUi().getUiList().isEmpty());
		Assert.assertEquals(0, waiting.inits);
		JOID.open(menu);
		this.bridges.frames(30);
		JOID.open(forced);
		JOID.forceClose(forced);
		this.bridges.frames(30);
		Assert.assertTrue(this.bridges.getUi().getUiList().isEmpty());
		Assert.assertEquals(0, forced.inits);
	}

	@Test
	public void closesAUiThatEscapeCannotClose() {
		final MenuUI menu = new MenuUI();
		menu.getData().closeable(false);
		JOID.open(menu);
		JOID.close(menu);
		Assert.assertFalse(this.bridges.getUi().isOpen(menu));
	}

	@Test
	public void keepsAUiThatRefusesToClose() {
		final MenuUI menu = new MenuUI();
		menu.closeable = false;
		JOID.open(menu);
		JOID.close(menu);
		Assert.assertTrue(this.bridges.getUi().isOpen(menu));
		Assert.assertEquals(0, menu.detaches);
	}

	@Test
	public void forcesAUiToClose() {
		final MenuUI menu = new MenuUI();
		menu.closeable = false;
		JOID.open(menu);
		JOID.forceClose(menu);
		Assert.assertFalse(this.bridges.getUi().isOpen(menu));
		Assert.assertEquals(0, menu.closes);
		Assert.assertEquals(1, menu.detaches);
	}

	@Test
	public void opensAUiFromTheOnCloseOfTheClosedOne() {
		final MenuUI menu = new MenuUI();
		final MenuUI next = new MenuUI();
		menu.onClose(event -> JOID.open(next));
		JOID.open(menu);
		JOID.close(menu);
		Assert.assertEquals(Collections.singletonList(next), this.bridges.getUi().getUiList());
		Assert.assertEquals(1, menu.closes);
		Assert.assertEquals(1, menu.detaches);
		Assert.assertEquals(1, next.inits);
	}

	@Test
	public void opensAUiFromTheOnCloseOfAUiThatPlaysItsExit() {
		final MenuUI menu = new MenuUI();
		final MenuUI next = new MenuUI();
		menu.transition(PopTransition.create());
		menu.onClose(event -> JOID.open(next));
		for (int round = 1; round <= 2; round++) {
			JOID.open(menu);
			this.bridges.frames(30);
			JOID.close(menu);
			this.bridges.frames(30);
			Assert.assertEquals(Collections.singletonList(next), this.bridges.getUi().getUiList());
			Assert.assertFalse(menu.getTransition().getOut().isRunning());
			Assert.assertEquals(round, menu.closes);
			Assert.assertEquals(round, next.inits);
		}
	}

	@Test
	public void opensAUiFromTheOnCloseOfAForcedUi() {
		final MenuUI menu = new MenuUI();
		final MenuUI next = new MenuUI();
		final MenuUI forced = new MenuUI();
		menu.onClose(event -> JOID.open(next));
		JOID.open(menu);
		JOID.forceClose(menu);
		Assert.assertEquals(Collections.singletonList(next), this.bridges.getUi().getUiList());
		JOID.open(menu);
		JOID.forceOpen(forced);
		Assert.assertTrue(this.bridges.getUi().isOpen(forced));
		Assert.assertFalse(this.bridges.getUi().isOpen(menu));
		Assert.assertEquals(0, menu.closes);
	}

	@Test
	public void opensTheUiThatTheOnCloseOfALeavingUiOpens() {
		final MenuUI menu = new MenuUI();
		final MenuUI waiting = new MenuUI();
		final MenuUI next = new MenuUI();
		menu.transition(PopTransition.create());
		menu.onClose(event -> JOID.open(next));
		JOID.open(menu);
		this.bridges.frames(30);
		JOID.open(waiting);
		this.bridges.frames(30);
		Assert.assertEquals(Collections.singletonList(next), this.bridges.getUi().getUiList());
		Assert.assertEquals(0, waiting.inits);
		Assert.assertEquals(1, menu.closes);
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
			JOID.forceOpen(menu);
			JOID.forceClose(menu);
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
		JOID.forceOpen(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToCloseAMissingUi() {
		JOID.close(null);
	}

	@Test(expected = NullPointerException.class)
	public void refusesToForceAMissingUiClosed() {
		JOID.forceClose(null);
	}

	@Test
	public void createsNoDataFolderOnLoad() {
		final File data = new File(this.folder.getRoot(), "joid");
		JOIDEnv.dataDir(data);
		JOIDEnv.devMode(false);
		TestUtils.demoMode(false);
		JOIDTest.quietly(JOIDTest.create());
		Assert.assertFalse(data.exists());
	}

	@Test
	public void loadsOnce() {
		JOIDEnv.devMode(false);
		TestUtils.demoMode(false);
		final JOID joid = JOIDTest.create();
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
			JOID.forceClose(menu);
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
		JOID.forceOpen(next);
		Assert.assertEquals(Collections.singletonList(next), this.bridges.getUi().getUiList());
	}

	private static JOID create() {
		try {
			final Constructor<JOID> constructor = JOID.class.getDeclaredConstructor();
			constructor.setAccessible(true);
			return constructor.newInstance();
		} catch (final ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static Thread shutdown() {
		return new Thread(() -> {
			try {
				final Method shutdown = JOID.class.getDeclaredMethod("shutdown");
				shutdown.setAccessible(true);
				shutdown.invoke(null);
			} catch (final ReflectiveOperationException exception) {
				throw new IllegalStateException(exception);
			}
		});
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

	private static String readVersion(final String properties) throws Throwable {
		final Method read = JOID.class.getDeclaredMethod("readVersion", InputStream.class);
		read.setAccessible(true);
		try {
			return (String) read.invoke(null, properties == null ? null : new ByteArrayInputStream(properties.getBytes(StandardCharsets.ISO_8859_1)));
		} catch (final InvocationTargetException exception) {
			throw exception.getCause();
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

	@UIDataDebug(profiler = false, hotreload = false)
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

	@UIDataDebug(profiler = false, hotreload = false)
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

	@UIDataDebug(profiler = false, hotreload = false)
	public static final class SettingsUI extends UI {

		@UIProperty
		private int volume = 1;

	}

	@UIStoreData(id = "exit", scope = UIStoreScope.PERMANENT)
	public static final class ExitStore extends UIStore {

		private final List<Thread> savers = new ArrayList<>();

		private String  value = "default";
		private boolean destroyed;

		@Override
		public void destroy() {
			this.destroyed = true;
		}

		@Override
		public @NonNull JsonObject toJson() {
			this.savers.add(Thread.currentThread());
			final JsonObject json = new JsonObject();
			json.addProperty("value", this.value);
			return json;
		}

	}

	@UIDataDebug(profiler = false, hotreload = false)
	public static final class StubbornUI extends UI {

		public StubbornUI() {
			super.beforeClose(event -> event.cancel());
		}

	}

}