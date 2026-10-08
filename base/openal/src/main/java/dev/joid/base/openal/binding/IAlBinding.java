package dev.joid.base.openal.binding;

import lombok.NonNull;

public interface IAlBinding {

	public void createContext();
	public void destroyContext();
	public Object getCurrentContext();

	public int genSource();
	public void deleteSource(final int source);

	public int genBuffer();
	public void deleteBuffer(final int buffer);
	public void bufferData(final int buffer, final int channels, final @NonNull short[] samples, final int sampleRate);

	public void play(final int source);
	public void stop(final int source);
	public void pause(final int source);
	public void gain(final int source, final float gain);

	public int unqueueBuffer(final int source);
	public void queueBuffer(final int source, final int buffer);

	public boolean isPlaying(final int source);
	public int getQueuedBuffers(final int source);
	public int getProcessedBuffers(final int source);

}