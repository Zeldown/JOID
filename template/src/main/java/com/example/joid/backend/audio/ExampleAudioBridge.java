package com.example.joid.backend.audio;

import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.audio.IAudioSource;

public final class ExampleAudioBridge implements IAudioBridge {

	@Override
	public IAudioSource createSource(final int sampleRate, final int channels) {
		throw new UnsupportedOperationException();
	}

}