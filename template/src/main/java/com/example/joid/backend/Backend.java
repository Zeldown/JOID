package com.example.joid.backend;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.ui.WindowUIBridge;

import com.example.joid.backend.audio.ExampleAudioBridge;
import com.example.joid.backend.render.ExampleRenderBridge;
import com.example.joid.backend.window.ExampleWindowBridge;

public final class Backend {

	public static final String JOID_VERSION = "@JOID_VERSION@";

	private Backend() {}

	public static void register() {
		JOID.checkVersion(Backend.JOID_VERSION);
		BridgeHandler.AUDIO.register(new ExampleAudioBridge());
		BridgeHandler.WINDOW.register(new ExampleWindowBridge());
		BridgeHandler.RENDER.register(new ExampleRenderBridge());
		BridgeHandler.UI.registerDefault(WindowUIBridge.create());
	}

}