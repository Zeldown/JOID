package com.example.running;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.PixelFormat;

import dev.joid.backend.lwjgl2.Lwjgl2Backend;
import dev.joid.backend.lwjgl2.Lwjgl2Natives;
import dev.joid.backend.lwjgl2.input.Lwjgl2InputForwarder;
import dev.joid.lib.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.ui.WindowUIBridge;

public final class Lwjgl2App {

	private Lwjgl2App() {}

	public static void main(final String[] args) throws LWJGLException {
		Lwjgl2Natives.install();
		Display.setDisplayMode(new DisplayMode(1280, 720));
		Display.setTitle("My application");
		Display.create(new PixelFormat().withDepthBits(24).withStencilBits(8));

		final Lwjgl2Backend backend = Lwjgl2Backend.create().register();

		JOID.open(new RunningStartUI());
		while (!Display.isCloseRequested()) {
			Lwjgl2InputForwarder.poll();
			backend.frame();
			Display.update();
		}

		BridgeHandler.UI.getBridge(WindowUIBridge.class).closeAll();
		Display.destroy();
	}

}