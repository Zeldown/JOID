package be.zeldown.joid.impl.openal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.lwjgl.openal.AL10;

import be.zeldown.joid.lib.bridge.audio.IAudioSource;
import lombok.NonNull;

public final class AudioSource implements IAudioSource {

	private final int source;
	private final int format;
	private final int sampleRate;

	private final List<Integer>  bufferList;
	private final Deque<Integer> freeBufferQueue;

	public AudioSource(final int sampleRate, final int channels) {
		this.source          = AL10.alGenSources();
		this.format          = channels > 1 ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16;
		this.sampleRate      = sampleRate;
		this.bufferList      = new ArrayList<>();
		this.freeBufferQueue = new ArrayDeque<>();

		AL10.alSourcei(this.source, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
		AL10.alSource3f(this.source, AL10.AL_POSITION, 0F, 0F, 0F);
	}

	@Override
	public void play() {
		AL10.alSourcePlay(this.source);
	}

	@Override
	public void pause() {
		AL10.alSourcePause(this.source);
	}

	@Override
	public void stop() {
		AL10.alSourceStop(this.source);
	}

	@Override
	public void clear() {
		AL10.alSourceStop(this.source);
		final int queued = AL10.alGetSourcei(this.source, AL10.AL_BUFFERS_QUEUED);
		for (int i = 0; i < queued; i++) {
			this.freeBufferQueue.push(AL10.alSourceUnqueueBuffers(this.source));
		}
	}

	@Override
	public void gain(final float gain) {
		AL10.alSourcef(this.source, AL10.AL_GAIN, gain);
	}

	@Override
	public void queue(final @NonNull short[] samples) {
		final int buffer = this.nextBuffer();
		AL10.alBufferData(buffer, this.format, samples, this.sampleRate);
		AL10.alSourceQueueBuffers(this.source, buffer);
	}

	@Override
	public boolean isPlaying() {
		return AL10.alGetSourcei(this.source, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
	}

	@Override
	public int getQueuedBuffers() {
		return AL10.alGetSourcei(this.source, AL10.AL_BUFFERS_QUEUED);
	}

	@Override
	public int getProcessedBuffers() {
		return AL10.alGetSourcei(this.source, AL10.AL_BUFFERS_PROCESSED);
	}

	@Override
	public void delete() {
		AL10.alSourceStop(this.source);
		AL10.alDeleteSources(this.source);
		for (final int buffer : this.bufferList) {
			AL10.alDeleteBuffers(buffer);
		}

		this.bufferList.clear();
		this.freeBufferQueue.clear();
	}

	private int nextBuffer() {
		if (AL10.alGetSourcei(this.source, AL10.AL_BUFFERS_PROCESSED) > 0) {
			return AL10.alSourceUnqueueBuffers(this.source);
		}

		if (!this.freeBufferQueue.isEmpty()) {
			return this.freeBufferQueue.pop();
		}

		final int buffer = AL10.alGenBuffers();
		this.bufferList.add(buffer);
		return buffer;
	}

}