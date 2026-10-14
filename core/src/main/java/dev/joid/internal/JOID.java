package dev.joid.internal;

import java.util.ArrayList;
import java.util.List;

import dev.joid.demo.DemoFont;
import dev.joid.internal.font.DevFont;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.ui.IUIBridge;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.core.hook.property.UIPropertyHook;
import dev.joid.lib.ui.core.hook.store.UIStoreHook;
import dev.joid.lib.utils.thread.ThreadUtils;
import lombok.Getter;
import lombok.NonNull;

public final class JOID {

	public static final String VERSION = "8.0.0";

	private static JOID instance;

	private final List<Runnable> readyList;

	@Getter
	private boolean ready;

	public JOID() {
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

		System.out.println("=================================");
		System.out.println("");
		System.out.println("         _  ____ _____ _____    ");
		System.out.println("        | |/ __ \\_   _|  __ \\ ");
		System.out.println("        | | |  | || | | |  | |  ");
		System.out.println("    _   | | |  | || | | |  | |  ");
		System.out.println("   | |__| | |__| || |_| |__| |  ");
		System.out.println("    \\____/ \\____/_____|_____/ ");
		System.out.println("");
		System.out.println("[JOID] Loading JOID with parameters:");
		System.out.println(" - DataDir: " + JOIDEnv.getDataDir().getAbsolutePath());
		System.out.println(" - CacheDir: " + JOIDEnv.getCacheDir().getAbsolutePath());
		System.out.println(" - DevMode: " + JOIDEnv.isDevMode());
		System.out.println(" - DemoMode: " + JOIDEnv.isDemoMode());
		System.out.println(" - Version: " + JOID.VERSION);
		System.out.println("");
		System.out.println("=================================");

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

	public static boolean checkVersion(final @NonNull String version) {
		final boolean compatible = version.split("[.]")[0].equals(JOID.VERSION.split("[.]")[0]);
		if (!compatible) {
			JOIDLog.warn("This backend targets JOID " + version + " but JOID " + JOID.VERSION + " is loaded, use a backend built for JOID " + JOID.VERSION.split("[.]")[0] + ".x");
		}
		return compatible;
	}

	public static boolean isOpen(final @NonNull UI ui) {
		final IUIBridge bridge = BridgeHandler.UI.get(ui);
		return bridge != null && bridge.isOpen(ui);
	}

	public static boolean isOpen(final @NonNull Class<? extends UI> uiClass) {
		return JOID.getUi(uiClass) != null;
	}

	@SuppressWarnings("unchecked")
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
			if (ui.fireClose()) {
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
			ui.dispose();
			bridge.close(ui);
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
			for (final UI currentUi : new ArrayList<>(bridge.getUiList().ordered())) {
				currentUi.dispose();
				bridge.close(currentUi);
			}

			bridge.open(ui);
		});
		return bridge;
	}

	private static void shutdown() {
		for (final IUIBridge bridge : new ArrayList<>(BridgeHandler.UI.getBridgeList())) {
			for (final UI ui : new ArrayList<>(bridge.getUiList().ordered())) {
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

}