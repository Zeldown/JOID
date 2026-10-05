package dev.joid.test.snapshot;

import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.audio.IAudioSource;
import lombok.NonNull;

public final class SnapshotAudioBridge implements IAudioBridge {

	@Override
	public @NonNull IAudioSource createSource(final int sampleRate, final int channels) {
		return new SnapshotAudioSource();
	}

}