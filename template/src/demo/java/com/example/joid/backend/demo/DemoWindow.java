package com.example.joid.backend.demo;

import dev.joid.demo.DemoUIBridge;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;

import com.example.joid.backend.ExampleBackend;

public class DemoWindow extends DemoUIBridge {

	protected DemoWindow() {}

	public static DemoWindow create() {
		return new DemoWindow();
	}

	public void run(final ExampleBackend backend) {
		super.start();

		while (true) {
			backend.frame();
		}
	}

	public static void main(final String[] args) {
		final DemoWindow window = DemoWindow.create();
		JOID.onReady(() -> BridgeHandler.UI.register(window));
		window.run(ExampleBackend.create().register());
	}

}