package dev.joid.lib.bridge.backend;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.thread.IThreadBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import lombok.Getter;
import lombok.NonNull;

@Getter
@SuppressWarnings("unchecked")
public abstract class Backend<T extends Backend<T>> {

	private final String version;

	private boolean audio;
	private boolean registered;

	protected Backend(final @NonNull String version) {
		this.version = version;
		this.audio   = true;
	}

	public final @NonNull T audio(final boolean audio) {
		this.audio = audio;
		return (T) this;
	}

	public final @NonNull T register() {
		if (this.registered) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " is already registered");
		}

		this.registered = true;
		JOID.checkVersion(this.version);

		final IAudioBridge audio = this.audio ? this.createAudioBridge() : null;
		if (audio != null) {
			BridgeHandler.AUDIO.register(audio);
		}

		BridgeHandler.WINDOW.register(this.createWindowBridge());
		BridgeHandler.RENDER.register(this.createRenderBridge());

		final IThreadBridge thread = this.createThreadBridge();
		if (thread != null) {
			BridgeHandler.THREAD.register(thread);
		}

		this.registerDefaults();
		this.attachInput();
		JOID.inst().load();
		return (T) this;
	}

	public final @NonNull T frame() {
		BridgeHandler.UI.frame();
		return (T) this;
	}

	protected abstract @NonNull IWindowBridge createWindowBridge();
	protected abstract @NonNull IRenderBridge createRenderBridge();

	protected IAudioBridge createAudioBridge() {
		return null;
	}

	protected IThreadBridge createThreadBridge() {
		return null;
	}

	protected void registerDefaults() {}

	protected void attachInput() {}

}