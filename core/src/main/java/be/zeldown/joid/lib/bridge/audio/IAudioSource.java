package be.zeldown.joid.lib.bridge.audio;

import lombok.NonNull;

public interface IAudioSource {

	public void stop();
	public void play();
	public void pause();
	public void clear();

	public void delete();
	public boolean isPlaying();

	public int getQueuedBuffers();
	public int getProcessedBuffers();
	public void gain(final float gain);

	public void queue(final @NonNull short[] samples);

}