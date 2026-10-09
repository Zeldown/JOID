package com.example.joid.backend;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;

import com.example.joid.backend.audio.AudioBridge;
import com.example.joid.backend.render.RenderBridge;
import com.example.joid.backend.window.WindowBridge;

public final class Backend {

	public static final String JOID_VERSION = "@JOID_VERSION@";

	private Backend() {}

	public static void register() {
		JOID.checkVersion(Backend.JOID_VERSION);
		BridgeHandler.AUDIO.register(new AudioBridge());
		BridgeHandler.WINDOW.register(new WindowBridge());
		BridgeHandler.RENDER.register(new RenderBridge());
	}

}