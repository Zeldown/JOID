package dev.joid.backend.lwjgl2.audio;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;

import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.audio.IAudioSource;
import dev.joid.lib.utils.platform.Platform;
import lombok.NonNull;

public final class AudioBridge implements IAudioBridge {

	@Override
	public @NonNull IAudioSource createSource(final int sampleRate, final int channels) {
		if (!AL.isCreated()) {
			AudioBridge.createContext();
		}

		return new AudioSource(sampleRate, channels);
	}

	private static void createContext() {
		AudioBridge.loadLibrary();
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
		try {
			System.loadLibrary(Platform.current() == Platform.WINDOWS ? Platform.is64Bit() ? "OpenAL64" : "OpenAL32" : "openal");
		} catch (final Throwable ignored) {}
	}

}