package dev.joid.lib.bridge.backend;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import dev.joid.internal.ConsoleCapture;
import dev.joid.internal.TestUtils;
import dev.joid.lib.JOID;
import dev.joid.lib.JOIDEnv;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.HeadlessBridges;
import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.render.RecordingRenderBridge;
import dev.joid.lib.bridge.thread.FrameThreadBridge;
import dev.joid.lib.bridge.thread.IThreadBridge;
import dev.joid.lib.bridge.thread.QueueThreadBridge;
import dev.joid.lib.bridge.ui.UIBridge;
import dev.joid.lib.bridge.ui.WindowUIBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.bridge.window.RecordingWindowBridge;
import dev.joid.lib.color.Color;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

public class BackendTest {

	@Rule
	public final ConsoleCapture console = new ConsoleCapture();

	private final List<String> trace = new ArrayList<>();

	private boolean        ready;
	private boolean        devMode;
	private boolean        demoMode;
	private List<Runnable> readyList;

	private File dataDir;
	private File cacheDir;

	private TraceBackend backend;

	@Before
	public void startAFreshJOID() throws ReflectiveOperationException {
		this.ready = JOID.inst().isReady();
		this.readyList = new ArrayList<>(BackendTest.readyList());
		this.devMode = JOIDEnv.isDevMode();
		this.demoMode = JOIDEnv.isDemoMode();
		this.dataDir = JOIDEnv.getDataDir();
		this.cacheDir = JOIDEnv.getCacheDir();
		JOIDEnv.devMode(false);
		TestUtils.demoMode(false);
		BackendTest.ready(false, Collections.emptyList());
	}

	@After
	public void restoreJOID() throws ReflectiveOperationException {
		if (this.backend != null) {
			BridgeHandler.AUDIO.unregister(this.backend.audio);
			BridgeHandler.WINDOW.unregister(this.backend.window);
			BridgeHandler.RENDER.unregister(this.backend.render);
			BridgeHandler.THREAD.unregister(this.backend.thread);
			for (final UIBridge bridge : BridgeHandler.UI.getBridgeList()) {
				if (bridge instanceof WindowUIBridge) {
					BridgeHandler.UI.unregister(bridge);
				}
			}
		}

		BackendTest.ready(this.ready, this.readyList);
		HeadlessBridges.forgetTheCursor();
		HeadlessBridges.forgetTheScreenSize();
		JOIDEnv.devMode(this.devMode);
		TestUtils.demoMode(this.demoMode);
		JOIDEnv.dataDir(this.dataDir);
		JOIDEnv.cacheDir(this.cacheDir);
	}

	@Test
	public void registersInOneOrder() {
		JOID.onReady(() -> this.trace.add("ready"));
		this.backend().register();
		Assert.assertEquals(Arrays.asList("audio", "window", "render", "thread", "defaults", "input", "ready"), this.trace);
	}

	@Test
	public void registersTheBridgesItCreates() {
		final TraceBackend backend = this.backend().register();
		Assert.assertSame(backend.audio, BridgeHandler.AUDIO.get());
		Assert.assertSame(backend.window, BridgeHandler.WINDOW.get());
		Assert.assertSame(backend.render, BridgeHandler.RENDER.get());
		Assert.assertSame(backend.thread, BridgeHandler.THREAD.get());
		Assert.assertTrue(backend.isRegistered());
		Assert.assertTrue(JOID.inst().isReady());
		Assert.assertTrue(this.console.getOut(), this.console.getOut().startsWith("[JOID] " + JOID.VERSION + " loaded, data in "));
	}

	@Test
	public void givesTheDefaultsTheBridgesBeforeThem() {
		final TraceBackend backend = this.backend();
		backend.defaults = () -> {
			Assert.assertSame(backend.window, BridgeHandler.WINDOW.get());
			Assert.assertSame(backend.render, BridgeHandler.RENDER.get());
			Assert.assertFalse(JOID.inst().isReady());
		};
		backend.register();
		Assert.assertTrue(this.trace.contains("defaults"));
	}

	@Test
	public void registersNoAudioWhenTurnedOff() {
		final TraceBackend backend = this.backend().audio(false).register();
		Assert.assertFalse(backend.isAudio());
		Assert.assertFalse(this.trace.contains("audio"));
		Assert.assertFalse(BridgeHandler.AUDIO.getBridgeList().contains(backend.audio));
	}

	@Test
	public void turnsTheAudioOnByDefault() {
		Assert.assertTrue(this.backend().isAudio());
	}

	@Test
	public void registersNoThreadWithoutOne() {
		final TraceBackend backend = this.backend();
		backend.threaded = false;
		backend.register();
		Assert.assertFalse(this.trace.contains("thread"));
		Assert.assertFalse(BridgeHandler.THREAD.getBridgeList().contains(backend.thread));
	}

	@Test
	public void setsItsFoldersBeforeCreatingTheBridges() {
		final TraceBackend backend = this.backend().dataDir(new File("data")).cacheDir(new File("cache"));
		backend.windowed = () -> this.trace.add(JOIDEnv.getDataDir() + " " + JOIDEnv.getCacheDir());
		backend.register();
		Assert.assertEquals(this.trace.indexOf("window") - 1, this.trace.indexOf("data cache"));
	}

	@Test
	public void setsItsFoldersBeforeLoading() {
		final File data = new File("data");
		final File cache = new File("cache");
		final TraceBackend backend = this.backend().dataDir(data).cacheDir(cache);
		JOID.onReady(() -> this.trace.add(JOIDEnv.getDataDir() + " " + JOIDEnv.getCacheDir()));
		backend.register();
		Assert.assertEquals("data cache", this.trace.get(this.trace.size() - 1));
		Assert.assertSame(data, backend.getDataDir());
		Assert.assertSame(cache, backend.getCacheDir());
	}

	@Test
	public void keepsTheFoldersOfTheEnvironmentByDefault() {
		JOIDEnv.dataDir(new File("property/data"));
		JOIDEnv.cacheDir(new File("property/cache"));
		final TraceBackend backend = this.backend().register();
		Assert.assertNull(backend.getDataDir());
		Assert.assertEquals(new File("property/data"), JOIDEnv.getDataDir());
		Assert.assertEquals(new File("property/cache"), JOIDEnv.getCacheDir());
	}

	@Test
	public void chainsItsSettings() {
		final TraceBackend backend = this.backend();
		Assert.assertSame(backend, backend.dataDir(new File("data")));
		Assert.assertSame(backend, backend.cacheDir(new File("cache")));
		Assert.assertSame(backend, backend.audio(true));
		Assert.assertSame(backend, backend.register());
		Assert.assertSame(backend, backend.frame());
	}

	@Test
	public void refusesASecondRegister() {
		final TraceBackend backend = this.backend().register();
		try {
			backend.register();
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("TraceBackend is already registered, call register() only once", exception.getMessage());
		}
		Assert.assertEquals(1, Collections.frequency(this.trace, "window"));
	}

	@Test
	public void registersAgainAfterAFailedRegister() {
		final TraceBackend backend = this.backend();
		backend.windowed = () -> {
			throw new IllegalStateException("No window");
		};
		try {
			backend.register();
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("No window", exception.getMessage());
		}
		Assert.assertFalse(backend.isRegistered());

		backend.windowed = () -> {};
		Assert.assertTrue(backend.register().isRegistered());
	}

	@Test
	public void runsTheReadyCallbacksOnceInTheirOrder() {
		JOID.onReady(() -> this.trace.add("first"));
		JOID.onReady(() -> this.trace.add("second"));
		Assert.assertFalse(this.trace.contains("first"));
		this.backend().register();
		JOID.inst().load();
		Assert.assertEquals(Arrays.asList("first", "second"), this.trace.subList(this.trace.size() - 2, this.trace.size()));
		Assert.assertEquals(1, Collections.frequency(this.trace, "first"));
	}

	@Test
	public void keepsRunningTheReadyCallbacksAfterOneThrows() {
		JOID.onReady(() -> this.trace.add("first"));
		JOID.onReady(() -> {
			throw new IllegalStateException("broken");
		});
		JOID.onReady(() -> this.trace.add("third"));
		final String text = TestUtils.captureErr(() -> this.backend().register());
		Assert.assertEquals(Arrays.asList("first", "third"), this.trace.subList(this.trace.size() - 2, this.trace.size()));
		Assert.assertTrue(text, text.startsWith("[JOID] A JOID.onReady callback failed (java.lang.IllegalStateException: broken), the next callbacks still run: catch the exception inside the callback to handle it"));
	}

	@Test
	public void runsAReadyCallbackAtOnceAfterTheRegister() {
		this.backend().register();
		JOID.onReady(() -> this.trace.add("late"));
		Assert.assertEquals("late", this.trace.get(this.trace.size() - 1));
	}

	@Test
	public void runsALateReadyCallbackOnTheRenderThread() {
		final TraceBackend backend = this.backend().register();
		backend.thread.renderThread(false);
		JOID.onReady(() -> this.trace.add("late"));
		Assert.assertFalse(this.trace.contains("late"));
		backend.thread.run();
		Assert.assertTrue(this.trace.contains("late"));
	}

	@Test
	public void refusesABackendOfAnotherMajorVersion() throws ReflectiveOperationException {
		this.backend = TraceBackend.create(this.trace);
		final Field version = Backend.class.getDeclaredField("version");
		version.setAccessible(true);
		version.set(this.backend, "70.0.0");
		try {
			this.backend.register();
			Assert.fail();
		} catch (final IllegalStateException exception) {
			Assert.assertEquals("The backend was built for JOID 70.x, but JOID " + JOID.VERSION + " is loaded: update the backend", exception.getMessage());
		}
		Assert.assertFalse(this.backend.isRegistered());
	}

	@Test
	public void framesTheBridgesOfTheRegistry() {
		final TraceBackend backend = this.backend().register();
		BridgeHandler.UI.register(WindowUIBridge.create().background(Color.RED), Integer.MAX_VALUE);
		backend.window.setWidth(64);
		backend.window.setHeight(32);
		backend.frame();
		Assert.assertEquals(Color.BLACK, backend.render.getClearedColor());
		Assert.assertEquals(64, backend.render.getState().getViewportWidth());
		Assert.assertEquals(32, backend.render.getState().getViewportHeight());
	}

	@Test
	public void readsTheVersionItWasBuiltForFromItsResource() {
		Assert.assertEquals("development", this.backend().getVersion());
		Assert.assertEquals(JOID.VERSION, JOID.readVersion(JOID.class, "joid.properties"));
	}

	@Test
	public void runsTheTasksOfOtherThreadsAtEachFrameByDefault() {
		Assert.assertTrue(this.backend().createDefaultThreadBridge() instanceof FrameThreadBridge);
	}

	private TraceBackend backend() {
		this.backend = TraceBackend.create(this.trace);
		return this.backend;
	}

	private static List<Runnable> readyList() throws ReflectiveOperationException {
		final Field field = JOID.class.getDeclaredField("readyList");
		field.setAccessible(true);
		return (List<Runnable>) field.get(JOID.inst());
	}

	private static void ready(final boolean ready, final List<Runnable> readyList) throws ReflectiveOperationException {
		final Field field = JOID.class.getDeclaredField("ready");
		field.setAccessible(true);
		field.setBoolean(JOID.inst(), ready);
		final List<Runnable> current = BackendTest.readyList();
		current.clear();
		current.addAll(readyList);
	}

	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static final class TraceBackend extends Backend {

		private final List<String>          trace;
		private final IAudioBridge          audio  = (sampleRate, channels) -> null;
		private final QueueThreadBridge     thread = new QueueThreadBridge().renderThread(true);
		private final RecordingWindowBridge window = new RecordingWindowBridge();
		private final RecordingRenderBridge render = new RecordingRenderBridge();

		private boolean  threaded = true;
		private Runnable defaults = () -> {};
		private Runnable windowed = () -> {};

		public static @NonNull TraceBackend create(final @NonNull List<String> trace) {
			return new TraceBackend(trace);
		}

		@Override
		protected @NonNull IWindowBridge createWindowBridge() {
			this.windowed.run();
			this.trace.add("window");
			return this.window;
		}

		@Override
		protected @NonNull IRenderBridge createRenderBridge() {
			this.trace.add("render");
			return this.render;
		}

		@Override
		protected IAudioBridge createAudioBridge() {
			this.trace.add("audio");
			return this.audio;
		}

		@Override
		protected IThreadBridge createThreadBridge() {
			if (!this.threaded) {
				return null;
			}

			this.trace.add("thread");
			return this.thread;
		}

		@Override
		protected void registerDefaults() {
			this.trace.add("defaults");
			this.defaults.run();
			super.registerDefaults();
		}

		@Override
		protected void attachInput() {
			this.trace.add("input");
		}

		private IThreadBridge createDefaultThreadBridge() {
			return super.createThreadBridge();
		}

	}

}