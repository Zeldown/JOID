package dev.joid.backend.lwjgl2.demo;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import dev.joid.backend.lwjgl2.Backend;
import dev.joid.backend.lwjgl2.Natives;
import dev.joid.backend.lwjgl2.window.WindowBridge;
import dev.joid.demo.DemoUIBridge;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.utils.click.ClickType;

public class DemoWindow extends DemoUIBridge {

	public DemoWindow() throws LWJGLException {
		Natives.install();
		Display.setDisplayMode(new DisplayMode(1920, 1080));
		Display.setResizable(true);
		Display.setTitle("JOID - Demo (LWJGL 2)");
		Display.create(new PixelFormat().withDepthBits(24).withStencilBits(8));
		Backend.register();

		super.resize(Display.getWidth(), Display.getHeight());
	}

	public static void main(final String[] args) throws LWJGLException {
		final DemoWindow window = new DemoWindow();
		BridgeHandler.UI.register(window);
		JOID.inst().setDevMode(true).setDemoMode(true).load();
		window.run();
	}

	public void run() {
		super.start();
		this.loop();
	}

	public void loop() {
		while (!Display.isCloseRequested()) {
			if (!Display.isVisible()) {
				try {
					Thread.sleep(100);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
				Display.update();
				continue;
			}

			while (Mouse.next()) {
				final int scroll = Mouse.getEventDWheel();
				final int button = Mouse.getEventButton();
				final boolean state = Mouse.getEventButtonState();

				if (state && button != -1) {
					super.mousePressed(ClickType.from(button));
				} else if (button != -1) {
					super.mouseReleased(ClickType.from(button));
				} else {
					super.mouseMoved();
				}

				if (scroll != 0) {
					super.mouseScroll(scroll / 120D);
				}
			}

			while (Keyboard.next()) {
				if (Keyboard.getEventKeyState()) {
					super.keyTyped(Keyboard.getEventCharacter(), WindowBridge.getKey(Keyboard.getEventKey()));
				}
			}

			super.frame();
			Display.update();

			if (Display.wasResized()) {
				super.resize(Display.getWidth(), Display.getHeight());
			}
		}

		Display.destroy();
		System.exit(0);
	}

}