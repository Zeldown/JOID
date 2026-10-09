package dev.joid.lib.bridge.audio;

import lombok.NonNull;

public interface IAudioSource {

	public void play();
	public void stop();
	public void pause();

	public void gain(final float gain);
	public void group(final Object group);
	public void write(final @NonNull short[] samples);

	public boolean isPlaying();
	public int getBufferedSamples();

	public void delete();

}