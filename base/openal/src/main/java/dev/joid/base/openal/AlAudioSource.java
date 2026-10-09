package dev.joid.base.openal;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import dev.joid.base.openal.binding.IAlBinding;
import dev.joid.lib.bridge.audio.AudioDownmix;
import dev.joid.lib.bridge.audio.IAudioSource;
import lombok.NonNull;

public final class AlAudioSource implements IAudioSource {

	private final IAlBinding     binding;
	private final IAudioGain     audioGain;
	private final int            channels;
	private final int            sampleRate;
	private final List<Integer>  bufferList;
	private final Deque<Integer> freeBufferQueue;
	private final Deque<Integer> sampleCountQueue;

	private int     source;
	private float   gain;
	private Object  group;
	private Object  context;
	private boolean playing;
	private float   appliedGain;
	private int     bufferedSamples;

	private AlAudioSource(final IAlBinding binding, final IAudioGain audioGain, final int sampleRate, final int channels) {
		this.binding          = binding;
		this.audioGain         = audioGain;
		this.channels         = channels;
		this.sampleRate       = sampleRate;
		this.bufferList       = new ArrayList<>();
		this.freeBufferQueue  = new ArrayDeque<>();
		this.sampleCountQueue = new ArrayDeque<>();
		this.gain             = 1F;
	}

	public static @NonNull AlAudioSource create(final @NonNull IAlBinding binding, final @NonNull IAudioGain audioGain, final int sampleRate, final int channels) {
		return new AlAudioSource(binding, audioGain, sampleRate, channels);
	}

	@Override
	public void play() {
		this.playing = true;
		if (this.isAvailable()) {
			this.binding.play(this.source);
		}
	}

	@Override
	public void stop() {
		this.playing = false;
		if (!this.isAvailable()) {
			return;
		}

		this.binding.stop(this.source);
		final int queued = this.binding.getQueuedBuffers(this.source);
		for (int i = 0; i < queued; i++) {
			this.freeBufferQueue.push(this.binding.unqueueBuffer(this.source));
		}

		this.sampleCountQueue.clear();
		this.bufferedSamples = 0;
	}

	@Override
	public void pause() {
		this.playing = false;
		if (this.isAvailable()) {
			this.binding.pause(this.source);
		}
	}

	@Override
	public void gain(final float gain) {
		this.gain = gain;
		this.isAvailable();
	}

	@Override
	public void group(final Object group) {
		this.group = group;
		this.isAvailable();
	}

	@Override
	public void write(final @NonNull short[] samples) {
		if (!this.isAvailable()) {
			return;
		}

		this.reclaim();
		final int buffer = this.nextBuffer();
		this.binding.bufferData(buffer, Math.min(this.channels, 2), AudioDownmix.stereo(samples, this.channels), this.sampleRate);
		this.binding.queueBuffer(this.source, buffer);
		this.sampleCountQueue.add(samples.length);
		this.bufferedSamples += samples.length;

		if (this.playing && !this.binding.isPlaying(this.source)) {
			this.binding.play(this.source);
		}
	}

	@Override
	public boolean isPlaying() {
		return this.playing;
	}

	@Override
	public int getBufferedSamples() {
		if (!this.isAvailable()) {
			return 0;
		}

		this.reclaim();
		return this.bufferedSamples;
	}

	@Override
	public void delete() {
		if (this.context != null && this.context.equals(this.binding.getCurrentContext())) {
			this.binding.stop(this.source);
			this.binding.deleteSource(this.source);
			for (final int buffer : this.bufferList) {
				this.binding.deleteBuffer(buffer);
			}
		}

		this.context = null;
		this.playing = false;
		this.bufferList.clear();
		this.freeBufferQueue.clear();
		this.sampleCountQueue.clear();
		this.bufferedSamples = 0;
	}

	private boolean isAvailable() {
		final Object current = this.binding.getCurrentContext();
		if (current == null) {
			return false;
		}

		if (!current.equals(this.context)) {
			this.context = current;
			this.bufferList.clear();
			this.freeBufferQueue.clear();
			this.sampleCountQueue.clear();
			this.bufferedSamples = 0;
			this.source = this.binding.genSource();
			this.appliedGain = Float.NaN;
		}

		final float applied = this.audioGain.apply(this.gain, this.group);
		if (Float.compare(applied, this.appliedGain) != 0) {
			this.appliedGain = applied;
			this.binding.gain(this.source, applied);
		}
		return true;
	}

	private void reclaim() {
		final int processed = this.binding.getProcessedBuffers(this.source);
		for (int i = 0; i < processed; i++) {
			this.freeBufferQueue.push(this.binding.unqueueBuffer(this.source));
			this.bufferedSamples -= this.sampleCountQueue.poll();
		}
	}

	private int nextBuffer() {
		if (!this.freeBufferQueue.isEmpty()) {
			return this.freeBufferQueue.pop();
		}

		final int buffer = this.binding.genBuffer();
		this.bufferList.add(buffer);
		return buffer;
	}

}