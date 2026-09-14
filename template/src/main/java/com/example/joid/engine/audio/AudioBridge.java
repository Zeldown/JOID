package com.example.joid.engine.audio;

import be.zeldown.joid.lib.bridge.audio.IAudioBridge;
import be.zeldown.joid.lib.bridge.audio.IAudioSource;

public final class AudioBridge implements IAudioBridge {

	@Override
	public IAudioSource createSource(final int sampleRate, final int channels) {
		throw new UnsupportedOperationException();
	}

}