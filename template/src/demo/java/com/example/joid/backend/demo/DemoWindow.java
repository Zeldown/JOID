package com.example.joid.backend.demo;

import com.example.joid.backend.ExampleBackend;

import dev.joid.demo.DemoUIBridge;
import dev.joid.lib.bridge.BridgeHandler;

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
		BridgeHandler.UI.register(window);
		window.run(ExampleBackend.create().register());
	}

}