package dev.joid.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import dev.joid.demo.DemoFont;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.ui.IUIBridge;
import dev.joid.lib.cache.FileCache;
import dev.joid.lib.font.impl.msdf.MsdfFontCache;
import dev.joid.lib.resource.decoder.impl.VideoResourceDecoder;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.hook.property.UIPropertyHook;
import dev.joid.lib.ui.core.hook.store.UIStoreHook;
import dev.joid.lib.utils.thread.ThreadUtils;
import dev.joid.lib.video.VideoNatives;
import lombok.Getter;
import lombok.NonNull;

public final class JOID {

	public static final String VERSION = JOID.readVersion();

	private static final String DEVELOPMENT_VERSION = "development";

	private static JOID instance;

	private final List<Runnable> readyList;

	@Getter
	private boolean ready;

	protected JOID() {
		this.readyList = new ArrayList<>();
	}

	public static @NonNull JOID inst() {
		if (JOID.instance == null) {
			JOID.instance = new JOID();
		}
		return JOID.instance;
	}

	public @NonNull JOID load() {
		synchronized (this.readyList) {
			if (this.ready) {
				return this;
			}
		}

		final String mode = JOIDEnv.isDemoMode() ? JOIDEnv.isDevMode() ? " in dev and demo mode" : " in demo mode" : JOIDEnv.isDevMode() ? " in dev mode" : "";
		JOIDLog.info(JOID.VERSION + " loaded" + mode + ", data in " + JOIDEnv.getDataDir().getAbsolutePath() + ", cache in " + JOIDEnv.getCacheDir().getAbsolutePath());

		JOID.initialize(MsdfFontCache.class, VideoNatives.class, VideoResourceDecoder.class);
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

		readyList.forEach(ThreadUtils::runOnRenderThread);
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

	public static boolean isOpen(final @NonNull UI ui) {
		final IUIBridge bridge = BridgeHandler.UI.get(ui);
		return bridge != null && bridge.isOpen(ui);
	}

	public static boolean isOpen(final @NonNull Class<? extends UI> uiClass) {
		return JOID.getUi(uiClass) != null;
	}

	public static <T extends UI> T getUi(final @NonNull Class<T> uiClass) {
		final IUIBridge bridge = BridgeHandler.UI.get(uiClass);
		if (bridge != null) {
			for (final UI ui : bridge.getUiList()) {
				if (uiClass.isInstance(ui)) {
					return (T) ui;
				}
			}
		}

		return null;
	}

	public static void close(final @NonNull UI ui) {
		final IUIBridge bridge = BridgeHandler.UI.get(ui);
		if (bridge == null) {
			return;
		}

		ThreadUtils.runOnRenderThread(() -> {
			if (bridge.isOpen(ui) && ui.fireClose()) {
				bridge.close(ui);
			}
		});
	}

	public static void close(final @NonNull UI ui, final boolean force) {
		if (!force) {
			JOID.close(ui);
			return;
		}

		final IUIBridge bridge = BridgeHandler.UI.get(ui);
		if (bridge == null) {
			return;
		}

		ThreadUtils.runOnRenderThread(() -> {
			if (bridge.isOpen(ui)) {
				ui.dispose();
				bridge.close(ui);
			}
		});
	}

	public static @NonNull IUIBridge open(final @NonNull UI ui) {
		final IUIBridge bridge = JOID.getOpeningBridge(ui);
		ThreadUtils.runOnRenderThread(() -> bridge.open(ui));
		return bridge;
	}

	public static @NonNull IUIBridge open(final @NonNull UI ui, final boolean force) {
		if (!force) {
			return JOID.open(ui);
		}

		final IUIBridge bridge = JOID.getOpeningBridge(ui);
		ThreadUtils.runOnRenderThread(() -> {
			for (final UI currentUi : new ArrayList<>(bridge.getUiList())) {
				currentUi.dispose();
				bridge.close(currentUi);
			}

			bridge.open(ui);
		});
		return bridge;
	}

	private static void shutdown() {
		for (final IUIBridge bridge : new ArrayList<>(BridgeHandler.UI.getBridgeList())) {
			for (final UI ui : new ArrayList<>(bridge.getUiList())) {
				UIPropertyHook.save(ui);
			}
		}
		UIStoreHook.shutdown();
	}

	private static @NonNull IUIBridge getOpeningBridge(final @NonNull UI ui) {
		final IUIBridge bridge = BridgeHandler.UI.get(ui);
		if (bridge == null) {
			throw new IllegalStateException("No IUIBridge can open " + ui.getClass().getSimpleName() + ": register one whose canHandle accepts it");
		}
		return bridge;
	}

	private static void initialize(final Class<?>... classes) {
		for (final Class<?> clazz : classes) {
			try {
				Class.forName(clazz.getName(), true, clazz.getClassLoader());
			} catch (final ClassNotFoundException exception) {
				throw new IllegalStateException("The class " + clazz.getName() + " of JOID cannot be loaded, check that the JOID jar is complete", exception);
			}
		}
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

	private static String readVersion() {
		try (final InputStream input = JOID.class.getResourceAsStream("joid.properties")) {
			return JOID.readVersion(input);
		} catch (final IOException exception) {
			return JOID.DEVELOPMENT_VERSION;
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