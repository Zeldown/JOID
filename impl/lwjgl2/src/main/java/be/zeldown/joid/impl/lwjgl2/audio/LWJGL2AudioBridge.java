package be.zeldown.joid.impl.lwjgl2.audio;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;

import be.zeldown.joid.lib.bridge.audio.IAudioBridge;
import be.zeldown.joid.lib.bridge.audio.IAudioSource;
import lombok.NonNull;

public final class LWJGL2AudioBridge implements IAudioBridge {

	@Override
	public @NonNull IAudioSource createSource(final int sampleRate, final int channels) {
		if (!AL.isCreated()) {
			LWJGL2AudioBridge.createContext();
		}

		return new LWJGL2AudioSource(sampleRate, channels);
	}

	private static void createContext() {
		LWJGL2AudioBridge.loadLibrary();
		try {
			AL.create();
		} catch (final LWJGLException e) {
			throw new IllegalStateException("Unable to create the OpenAL context", e);
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (AL.isCreated()) {
				AL.destroy();
			}
		}, "joid-al-shutdown"));
	}

	private static void loadLibrary() {
		final String os = System.getProperty("os.name", "").toLowerCase();
		final boolean is64 = System.getProperty("os.arch", "").contains("64");
		try {
			System.loadLibrary(os.contains("win") ? is64 ? "OpenAL64" : "OpenAL32" : "openal");
		} catch (final Throwable ignored) {}
	}

}