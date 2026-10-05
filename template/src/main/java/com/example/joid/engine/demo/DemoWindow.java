package com.example.joid.engine.demo;

import dev.joid.demo.DemoUIBridge;
import dev.joid.demo.ui.UIDemoChoice;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;

import com.example.joid.engine.Backend;
import com.example.joid.engine.render.RenderBridge;

public class DemoWindow extends DemoUIBridge {

	public void run() {
		final IWindowBridge windowBridge = BridgeHandler.WINDOW.get();
		final RenderBridge render = (RenderBridge) BridgeHandler.RENDER.get();
		render.ortho(0D, windowBridge.getWidth(), windowBridge.getHeight(), 0D, 0D, 10000D);
		render.viewport(0, 0, windowBridge.getWidth(), windowBridge.getHeight());
		JOID.open(new UIDemoChoice());
		super.load();

		while (true) {
			super.update();
			render.beginFrame();
			render.clear(0F, 0F, 0F, 0F);
			DrawUtils.SHAPE.drawRect(0, 0, windowBridge.getWidth(), windowBridge.getHeight(), new Color(50, 50, 50));
			super.draw();
			render.endFrame();
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