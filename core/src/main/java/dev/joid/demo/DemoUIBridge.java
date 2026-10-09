package dev.joid.demo;

import dev.joid.demo.ui.UIDemoChoice;
import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.ui.StackUIBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import dev.joid.lib.color.Color;
import dev.joid.lib.draw.DrawUtils;

public class DemoUIBridge extends StackUIBridge {

	private static final Color BACKGROUND = new Color(50, 50, 50);

	public void start() {
		JOID.open(new UIDemoChoice());
		super.load();
	}

	public void resize(final int width, final int height) {
		BridgeHandler.RENDER.get().screen(width, height);
		super.load();
	}

	public void frame() {
		final IWindowBridge window = BridgeHandler.WINDOW.get();
		final IRenderBridge render = BridgeHandler.RENDER.get();
		super.update();
		render.beginFrame();
		try {
			render.clearColor(0F, 0F, 0F, 0F);
			DrawUtils.SHAPE.drawRect(0, 0, window.getWidth(), window.getHeight(), DemoUIBridge.BACKGROUND);
			super.draw();
		} finally {
			render.endFrame();
		}
	}

}