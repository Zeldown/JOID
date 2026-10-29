package dev.joid.backend.lwjgl2;

import org.lwjgl.input.Mouse;

import dev.joid.base.lwjgl2.openal.Lwjgl2AlBinding;
import dev.joid.base.lwjgl2.opengl.Lwjgl2GlBinding;
import dev.joid.base.lwjgl2.window.Lwjgl2WindowBridge;
import dev.joid.base.openal.AlAudioBridge;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.backend.Backend;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl2Backend extends Backend {

	public static @NonNull Lwjgl2Backend create() {
		return new Lwjgl2Backend();
	}

	@Override
	protected @NonNull IWindowBridge createWindowBridge() {
		return new Lwjgl2WindowBridge();
	}

	@Override
	protected @NonNull IRenderBridge createRenderBridge() {
		return new GlRenderBridge(Lwjgl2GlBinding.inst());
	}

	@Override
	protected @NonNull IAudioBridge createAudioBridge() {
		return AlAudioBridge.create(Lwjgl2AlBinding.inst());
	}

	@Override
	protected void attachInput() {
		Mouse.setClipMouseCoordinatesToWindow(false);
	}

}