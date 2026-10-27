package com.example.running;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.ui.WindowUIBridge;

public class SceneUIBridge extends WindowUIBridge {

	protected SceneUIBridge() {}

	public static SceneUIBridge create() {
		return new SceneUIBridge();
	}

	@Override
	protected void drawBackground() {
		final IRenderBridge render = BridgeHandler.RENDER.get();
		render.clearDepth();
		render.clearStencil();
	}

}