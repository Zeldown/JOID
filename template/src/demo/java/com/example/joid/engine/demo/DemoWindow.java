package com.example.joid.engine.demo;

import dev.joid.demo.DemoUIBridge;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.window.IWindowBridge;

import com.example.joid.engine.Backend;

public class DemoWindow extends DemoUIBridge {

	public void run() {
		final IWindowBridge window = BridgeHandler.WINDOW.get();
		super.resize(window.getWidth(), window.getHeight());
		super.start();

		while (true) {
			super.frame();
		}
	}

	public static void main(final String[] args) {
		Backend.register();
		final DemoWindow window = new DemoWindow();
		BridgeHandler.UI.register(window);
		JOID.inst().setDevMode(true).setDemoMode(true).load();
		window.run();
	}

}