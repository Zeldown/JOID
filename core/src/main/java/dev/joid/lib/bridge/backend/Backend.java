package dev.joid.lib.bridge.backend;

import java.io.File;

import dev.joid.internal.JOID;
import dev.joid.internal.JOIDEnv;
import dev.joid.internal.JOIDLog;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.render.IRenderBridge;
import dev.joid.lib.bridge.thread.FrameThreadBridge;
import dev.joid.lib.bridge.thread.IThreadBridge;
import dev.joid.lib.bridge.ui.WindowUIBridge;
import dev.joid.lib.bridge.window.IWindowBridge;
import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class Backend {

	private final String version;

	private boolean audio;
	private File    dataDir;
	private File    cacheDir;
	private boolean registered;

	protected Backend() {
		this.version = JOID.readVersion(this.getClass(), "backend.properties");
		this.audio   = true;
	}

	public final <T extends Backend> @NonNull T audio(final boolean audio) {
		this.audio = audio;
		return (T) this;
	}

	public final <T extends Backend> @NonNull T dataDir(final @NonNull File dataDir) {
		this.dataDir = dataDir;
		return (T) this;
	}

	public final <T extends Backend> @NonNull T cacheDir(final @NonNull File cacheDir) {
		this.cacheDir = cacheDir;
		return (T) this;
	}

	public final <T extends Backend> @NonNull T register() {
		if (this.registered) {
			throw new IllegalStateException(JOIDLog.nameOf(this.getClass()) + " is already registered, call register() only once");
		}

		JOID.checkVersion(this.version);
		if (this.dataDir != null) {
			JOIDEnv.dataDir(this.dataDir);
		}

		if (this.cacheDir != null) {
			JOIDEnv.cacheDir(this.cacheDir);
		}

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

		this.registered = true;
		JOID.inst().load();
		return (T) this;
	}

	public final <T extends Backend> @NonNull T frame() {
		BridgeHandler.UI.frame();
		return (T) this;
	}

	protected abstract @NonNull IWindowBridge createWindowBridge();
	protected abstract @NonNull IRenderBridge createRenderBridge();

	protected IAudioBridge createAudioBridge() {
		return null;
	}

	protected IThreadBridge createThreadBridge() {
		return new FrameThreadBridge();
	}

	protected void registerDefaults() {
		BridgeHandler.UI.registerDefault(WindowUIBridge.create());
	}

	protected void attachInput() {}

}