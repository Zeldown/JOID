package dev.joid.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.joid.demo.DemoFont;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.ui.IUIBridge;
import dev.joid.lib.cache.FileCache;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.hook.property.UIPropertyHook;
import dev.joid.lib.ui.core.hook.store.UIStoreHook;
import dev.joid.lib.utils.thread.ThreadUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JOID {

	public static final String VERSION = JOID.readVersion(JOID.class, "joid.properties");

	private static final String DEVELOPMENT_VERSION = "development";

	private static final JOID INSTANCE = new JOID();

	private final List<Runnable> readyList = new ArrayList<>();

	@Getter
	private volatile boolean ready;

	public static @NonNull JOID inst() {
		return JOID.INSTANCE;
	}

	public synchronized @NonNull JOID load() {
		if (this.ready) {
			return this;
		}

		final String mode = JOIDEnv.isDemoMode() ? JOIDEnv.isDevMode() ? " in dev and demo mode" : " in demo mode" : JOIDEnv.isDevMode() ? " in dev mode" : "";
		JOIDLog.info(JOID.VERSION + " loaded" + mode + ", data in " + JOIDEnv.getDataDir().getAbsolutePath() + ", cache in " + JOIDEnv.getCacheDir().getAbsolutePath());

		ThreadUtils.daemonThread(FileCache::clean, "FileCache").start();

		if (JOIDEnv.isDevMode() || JOIDEnv.isDemoMode()) {
			DevFont.load();
		}

		if (JOIDEnv.isDemoMode()) {
			DemoFont.load();
		}

		Runtime.getRuntime().addShutdownHook(new Thread(JOID::shutdown, "JOID shutdown"));

		final List<Runnable> readyList;
		synchronized (this.readyList) {
			this.ready = true;
			readyList = new ArrayList<>(this.readyList);
			this.readyList.clear();
		}

		for (final Runnable callback : readyList) {
			try {
				ThreadUtils.runOnRenderThread(callback);
			} catch (final RuntimeException exception) {
				JOIDLog.warn("A JOID.onReady callback failed (" + exception + "), the next callbacks still run: catch the exception inside the callback to handle it", exception);
			}
		}
		return this;
	}

	public static void onReady(final @NonNull Runnable callback) {
		final JOID joid = JOID.inst();
		synchronized (joid.readyList) {
			if (!joid.ready) {
				joid.readyList.add(callback);
				return;
			}
		}

		ThreadUtils.runOnRenderThread(callback);
	}

	public static void checkVersion(final @NonNull String version) {
		JOID.checkVersion(version, JOID.VERSION);
	}

	public static @NonNull String readVersion(final @NonNull Class<?> owner, final @NonNull String resource) {
		try (final InputStream input = owner.getResourceAsStream(resource)) {
			return JOID.readVersion(input);
		} catch (final IOException exception) {
			return JOID.DEVELOPMENT_VERSION;
		}
	}

	public static boolean isOpen(final @NonNull UI ui) {
		final IUIBridge bridge = BridgeHandler.UI.get(ui);
		return bridge != null && bridge.isOpen(ui);
	}

	public static boolean isOpen(final @NonNull Class<? extends UI> uiClass) {
		return JOID.getUi(uiClass) != null;
	}

	public static <T extends UI> T getUi(final @NonNull Class<T> uiClass) {
		for (final IUIBridge bridge : BridgeHandler.UI.getBridgeList()) {
			for (final UI ui : bridge.getUiList()) {
				if (uiClass.isInstance(ui)) {
					return (T) ui;
				}
			}
		}

		return null;
	}

	public static void close(final @NonNull UI ui) {
		ThreadUtils.runOnRenderThread(() -> {
			final IUIBridge bridge = BridgeHandler.UI.get(ui);
			if (bridge != null && (!bridge.isOpen(ui) || ui.fireClose())) {
				bridge.close(ui);
			}
		});
	}

	public static void forceClose(final @NonNull UI ui) {
		ThreadUtils.runOnRenderThread(() -> {
			final IUIBridge bridge = BridgeHandler.UI.get(ui);
			if (bridge == null) {
				return;
			}

			if (bridge.isOpen(ui)) {
				ui.dispose();
			}
			bridge.close(ui);
		});
	}

	public static @NonNull IUIBridge open(final @NonNull UI ui) {
		final IUIBridge bridge = JOID.getOpeningBridge(ui);
		ThreadUtils.runOnRenderThread(() -> bridge.open(ui));
		return bridge;
	}

	public static @NonNull IUIBridge forceOpen(final @NonNull UI ui) {
		final IUIBridge bridge = JOID.getOpeningBridge(ui);
		ThreadUtils.runOnRenderThread(() -> bridge.forceOpen(ui));
		return bridge;
	}

	private static void shutdown() {
		final AtomicBoolean saved = new AtomicBoolean();
		final CountDownLatch latch = new CountDownLatch(1);
		ThreadUtils.runOnRenderThread(() -> {
			JOID.save(saved);
			latch.countDown();
		});

		try {
			latch.await(200L, TimeUnit.MILLISECONDS);
		} catch (final InterruptedException exception) {
			Thread.currentThread().interrupt();
		}
		JOID.save(saved);
	}

	private static void save(final AtomicBoolean saved) {
		if (!saved.compareAndSet(false, true)) {
			return;
		}

		for (final IUIBridge bridge : BridgeHandler.UI.getBridgeList()) {
			for (final UI ui : new ArrayList<>(bridge.getUiList())) {
				UIPropertyHook.save(ui);
			}
		}
		UIStoreHook.saveAll();
	}

	private static IUIBridge getOpeningBridge(final UI ui) {
		final IUIBridge bridge = BridgeHandler.UI.get(ui);
		if (bridge == null) {
			throw new IllegalStateException("No IUIBridge can open " + JOIDLog.nameOf(ui.getClass()) + ": register one whose canHandle accepts it");
		}
		return bridge;
	}

	private static void checkVersion(final String version, final String loaded) {
		if (JOID.DEVELOPMENT_VERSION.equals(version) || JOID.DEVELOPMENT_VERSION.equals(loaded)) {
			return;
		}

		final String major = version.split("[.]")[0];
		if (!major.equals(loaded.split("[.]")[0])) {
			throw new IllegalStateException("The backend was built for JOID " + major + ".x, but JOID " + loaded + " is loaded: update the backend");
		}
	}

	private static String readVersion(final InputStream input) throws IOException {
		if (input == null) {
			return JOID.DEVELOPMENT_VERSION;
		}

		final Properties properties = new Properties();
		properties.load(input);
		final String version = properties.getProperty("version");
		return version == null || version.contains("${") ? JOID.DEVELOPMENT_VERSION : version;
	}

}