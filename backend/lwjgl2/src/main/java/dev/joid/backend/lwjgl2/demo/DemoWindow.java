package dev.joid.backend.lwjgl2.demo;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import dev.joid.backend.lwjgl2.Backend;
import dev.joid.backend.lwjgl2.Natives;
import dev.joid.backend.lwjgl2.input.Lwjgl2InputForwarder;
import dev.joid.demo.DemoUIBridge;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;

public class DemoWindow extends DemoUIBridge {

	private final Lwjgl2InputForwarder input;

	public DemoWindow() throws LWJGLException {
		Natives.install();
		Display.setDisplayMode(new DisplayMode(1920, 1080));
		Display.setResizable(true);
		Display.setTitle("JOID - Demo (LWJGL 2)");
		Display.create(new PixelFormat().withDepthBits(24).withStencilBits(8));
		Backend.register();

		this.input = Lwjgl2InputForwarder.create(this);
		super.resize(BridgeHandler.WINDOW.get().getWidth(), BridgeHandler.WINDOW.get().getHeight());
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

			this.input.poll();
			super.frame();
			Display.update();

			if (Display.wasResized()) {
				super.resize(BridgeHandler.WINDOW.get().getWidth(), BridgeHandler.WINDOW.get().getHeight());
			}
		}

		Display.destroy();
		System.exit(0);
	}

}