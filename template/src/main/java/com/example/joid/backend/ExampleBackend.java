package com.example.joid.backend;

import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.backend.Backend;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.ui.WindowUIBridge;
import dev.joid.lib.bridge.window.IWindowBridge;

import com.example.joid.backend.audio.ExampleAudioBridge;
import com.example.joid.backend.render.ExampleRenderBridge;
import com.example.joid.backend.window.ExampleWindowBridge;

public final class ExampleBackend extends Backend<ExampleBackend> {

	public static final String JOID_VERSION = "@JOID_VERSION@";

	private ExampleBackend() {
		super(ExampleBackend.JOID_VERSION);
	}

	public static ExampleBackend create() {
		return new ExampleBackend();
	}

	@Override
	protected IAudioBridge createAudioBridge() {
		return new ExampleAudioBridge();
	}

	@Override
	protected IWindowBridge createWindowBridge() {
		return new ExampleWindowBridge();
	}

	@Override
	protected IRenderBridge createRenderBridge() {
		return new ExampleRenderBridge();
	}

	@Override
	protected void registerDefaults() {
		BridgeHandler.UI.registerDefault(WindowUIBridge.create());
	}

	@Override
	protected void attachInput() {}

}