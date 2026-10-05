package dev.joid.test.snapshot;

import dev.joid.lib.bridge.audio.IAudioSource;
import lombok.NonNull;

public final class SnapshotAudioSource implements IAudioSource {

	@Override
	public void stop() {}

	@Override
	public void play() {}

	@Override
	public void pause() {}

	@Override
	public void clear() {}

	@Override
	public void gain(final float gain) {}

	@Override
	public void queue(final @NonNull short[] samples) {}

	@Override
	public boolean isPlaying() {
		return false;
	}

	@Override
	public int getQueuedBuffers() {
		return 0;
	}

	@Override
	public int getProcessedBuffers() {
		return 0;
	}

	@Override
	public void delete() {}

}