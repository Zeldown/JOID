package dev.joid.internal;

import java.io.File;

import dev.joid.demo.DemoFont;
import dev.joid.internal.font.InternalFont;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.ui.IUIBridge;
import dev.joid.lib.ui.core.UI;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class JOID {

	public static final String VERSION = "8.0.0";

	private static JOID instance;

	private File configDir;
	private boolean devMode;
	private boolean demoMode;

	public JOID() {
		this.configDir = new File("config");
		if (!this.configDir.exists()) {
			this.configDir.mkdirs();
		}

		this.devMode = false;
		this.demoMode = false;
	}

	public static @NonNull JOID inst() {
		if (JOID.instance == null) {
			JOID.instance = new JOID();
		}
		return JOID.instance;
	}

	public JOID load() {
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
		System.out.println(" - ConfigDir: " + this.configDir.getAbsolutePath());
		System.out.println(" - DevMode: " + this.devMode);
		System.out.println(" - DemoMode: " + this.demoMode);
		System.out.println(" - Version: " + JOID.VERSION);
		System.out.println("");
		System.out.println("=================================");

		if (this.devMode || this.demoMode) {
			InternalFont.load();
		}

		if (this.demoMode) {
			DemoFont.load();
		}

		return this;
	}

	public static boolean checkVersion(final @NonNull String version) {
		final boolean compatible = version.split("[.]")[0].equals(JOID.VERSION.split("[.]")[0]);
		if (!compatible) {
			System.err.println("[JOID] This backend targets JOID " + version + " but JOID " + JOID.VERSION + " is loaded");
		}
		return compatible;
	}

	public JOID setDevMode(final boolean devMode) {
		if (devMode && JOID.class.getResource("/dev/joid/lib/ui/node/impl/dev/DevNode.class") == null) {
			throw new IllegalStateException("The dev mode is not part of the prod jar of JOID, use the dev jar of your backend");
		}

		this.devMode = devMode;
		return this;
	}

	public JOID setDemoMode(final boolean demoMode) {
		if (demoMode && JOID.class.getResource("/dev/joid/demo/DemoFont.class") == null) {
			throw new IllegalStateException("The demo mode is not part of the prod jar of JOID, use the dev jar of your backend");
		}

		this.demoMode = demoMode;
		return this;
	}

	public JOID setConfigDir(final @NonNull File configDir) {
		this.configDir = configDir;
		return this;
	}

	public static boolean isOpen(final @NonNull Class<? extends UI> uiClass) {
		return JOID.getUI(uiClass) != null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends UI> T getUI(final @NonNull Class<T> uiClass) {
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
		if (bridge != null && ui.onClose()) {
			bridge.close(ui);
		}
	}

	public static void close(final @NonNull UI ui, final boolean force) {
		if (!force) {
			JOID.close(ui);
			return;
		}

		final IUIBridge bridge = BridgeHandler.UI.get(ui);
		ui.properlyClose();
		bridge.close(ui);
	}

	public static IUIBridge open(final @NonNull UI ui) {
		final IUIBridge bridge = BridgeHandler.UI.get(ui);
		if (bridge == null) {
			return null;
		}

		bridge.open(ui);
		return bridge;
	}

	public static IUIBridge open(final @NonNull UI ui, final boolean force) {
		if (!force) {
			return JOID.open(ui);
		}

		final IUIBridge bridge = BridgeHandler.UI.get(ui);
		if (bridge == null) {
			return null;
		}

		for (final UI currentUi : bridge.getUiList()) {
			currentUi.properlyClose();
			bridge.close(currentUi);
		}

		bridge.open(ui);
		return bridge;
	}

}