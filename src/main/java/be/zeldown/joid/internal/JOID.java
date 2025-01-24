package be.zeldown.joid.internal;

import java.io.File;

import org.lwjgl.LWJGLException;

import be.zeldown.joid.demo.DemoFont;
import be.zeldown.joid.demo.DemoWindow;
import be.zeldown.joid.internal.font.InternalFont;
import be.zeldown.joid.lib.ui.bridge.BridgeHandler;
import be.zeldown.joid.lib.ui.bridge.IUIBridge;
import be.zeldown.joid.lib.ui.core.UI;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class JOID {

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
		System.out.println(" - Version: " + JOID.class.getPackage().getImplementationVersion());
		System.out.println("");
		System.out.println("=================================");

		InternalFont.load();
		if (this.demoMode) {
			DemoFont.load();
		}

		return this;
	}

	/* [ Setter Section ] */
	public JOID setConfigDir(final @NonNull File configDir) {
		this.configDir = configDir;
		return this;
	}

	public JOID setDevMode(final boolean devMode) {
		this.devMode = devMode;
		return this;
	}

	public JOID setDemoMode(final boolean demoMode) {
		this.demoMode = demoMode;
		return this;
	}

	/* [ UI Section ] */
	public static boolean isOpen(final @NonNull Class<? extends UI> uiClass) {
		return JOID.getUI(uiClass) != null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends UI> T getUI(final @NonNull Class<T> uiClass) {
		final IUIBridge bridge = BridgeHandler.get(uiClass);
		if (bridge != null) {
			for (final UI ui : bridge.getUiList()) {
				if (uiClass.isInstance(ui)) {
					return (T) ui;
				}
			}
		}

		return null;
	}

	public static IUIBridge open(final @NonNull UI ui) {
		final IUIBridge bridge = BridgeHandler.get(ui);
		if (bridge == null) {
			return null;
		}

		JOID.open(ui, bridge);
		return bridge;
	}

	public static void open(final @NonNull UI ui, final @NonNull IUIBridge bridge) {
		bridge.open(ui);
	}

	public static IUIBridge open(final @NonNull UI ui, final boolean force) {
		if (!force || ui.getData().popup().active()) {
			return JOID.open(ui);
		}

		final IUIBridge bridge = BridgeHandler.get(ui);
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

	public static void close(final @NonNull UI ui) {
		final IUIBridge bridge = BridgeHandler.get(ui);
		if (bridge != null && ui.onClose()) {
			bridge.close(ui);
		}
	}

	public static void close(final @NonNull UI ui, final boolean force) {
		if (!force) {
			JOID.close(ui);
			return;
		}

		final IUIBridge bridge = BridgeHandler.get(ui);
		ui.properlyClose();
		bridge.close(ui);
	}

	/* [ Singleton Section ] */
	public static @NonNull JOID inst() {
		if (JOID.instance == null) {
			JOID.instance = new JOID();
		}
		return JOID.instance;
	}

	/* [ Demo Section ] */
	public static void main(final String[] args) {
		JOID.inst().setDevMode(true).setDemoMode(true).load();
		try {
			final DemoWindow window = new DemoWindow();
			BridgeHandler.register(window);
			window.run();
		} catch (final LWJGLException e) {
			e.printStackTrace();
		}
	}

}