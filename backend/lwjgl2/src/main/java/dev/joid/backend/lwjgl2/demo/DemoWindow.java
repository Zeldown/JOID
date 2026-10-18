package dev.joid.backend.lwjgl2.demo;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import dev.joid.backend.lwjgl2.Lwjgl2Backend;
import dev.joid.backend.lwjgl2.Lwjgl2Natives;
import dev.joid.backend.lwjgl2.input.Lwjgl2InputForwarder;
import dev.joid.demo.DemoUIBridge;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DemoWindow extends DemoUIBridge {

	private final Lwjgl2Backend backend = Lwjgl2Backend.create();

	public static @NonNull DemoWindow create() {
		return new DemoWindow();
	}

	public static void main(final String[] args) throws LWJGLException {
		DemoWindow.create().run();
	}

	public void run() throws LWJGLException {
		Lwjgl2Natives.install();
		Display.setDisplayMode(new DisplayMode(1920, 1080));
		Display.setResizable(true);
		Display.setTitle("JOID - Demo (LWJGL 2)");
		Display.create(new PixelFormat().withDepthBits(24).withStencilBits(8));
		Keyboard.enableRepeatEvents(true);
		JOID.onReady(() -> BridgeHandler.UI.register(this));
		this.backend.register();

		super.start();
		this.loop();
	}

	private void loop() {
		while (!Display.isCloseRequested()) {
			if (!Display.isVisible()) {
				try {
					Thread.sleep(100);
				} catch (final InterruptedException exception) {
					Thread.currentThread().interrupt();
					break;
				}
				Display.update();
				continue;
			}

			Lwjgl2InputForwarder.poll();
			this.backend.frame();
			Display.update();
		}

		Display.destroy();
		System.exit(0);
	}

}