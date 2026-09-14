package be.zeldown.joid.test.snapshot;

import be.zeldown.joid.lib.bridge.audio.IAudioBridge;
import be.zeldown.joid.lib.bridge.audio.IAudioSource;
import lombok.NonNull;

public final class SnapshotAudioBridge implements IAudioBridge {

	@Override
	public @NonNull IAudioSource createSource(final int sampleRate, final int channels) {
		return new SnapshotAudioSource();
	}

}