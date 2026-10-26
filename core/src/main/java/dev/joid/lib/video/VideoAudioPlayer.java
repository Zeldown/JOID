package dev.joid.lib.video;

import java.nio.Buffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.audio.IAudioSource;
import dev.joid.lib.resource.decoder.impl.VideoResourceDecoder;
import dev.joid.lib.utils.geom.Point;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class VideoAudioPlayer {

	private static AudioListenerPosition audioListenerPosition;

	private final int                         channels;
	private final int                         sampleRate;
	private final VideoResourceDecoder        owner;
	private final ArrayBlockingQueue<short[]> sampleQueue = new ArrayBlockingQueue<>(128);

	private Object       group;
	private boolean      playing;
	private IAudioSource source;
	private boolean      initialized;
	private boolean      buffersQueued;

	private long    dequeued;
	private int     pendingOffset;
	private short[] pendingSamples;

	public static void audioListenerPosition(final AudioListenerPosition audioListenerPosition) {
		VideoAudioPlayer.audioListenerPosition = audioListenerPosition;
	}

	public void cleanup() {
		if (this.source != null) {
			this.source.stop();
			this.source.delete();
			this.source = null;
		}

		this.clearQueue();
		this.initialized = false;
	}

	public void play() {
		if (!this.initialized) {
			this.initialized = true;
			try {
				final IAudioSource source = BridgeHandler.AUDIO.get().createSource(this.sampleRate, this.channels);
				this.group = this.owner.getAudioGroup();
				source.group(this.group);
				this.source = source;
			} catch (final Exception exception) {
				final String name = this.owner.getAsset().getUniqueId();
				JOIDLog.warnOnce("VideoAudioPlayer.source." + name, "The video " + name + " plays without sound (" + exception + "), register an audio bridge and check the audio device");
			}
		}

		this.playing = true;
		this.buffersQueued = false;
		this.clearQueue();
	}

	public void stop() {
		this.playing = false;
		if (this.source != null) {
			this.source.stop();
		}
	}

	public void pause() {
		this.playing = false;
		if (this.source != null) {
			this.source.pause();
		}
	}

	public void resume() {
		this.playing = true;
		if (this.source != null) {
			this.source.play();
		}
	}

	public void flush() {
		this.clearQueue();
		this.buffersQueued = false;
		if (this.source != null) {
			this.source.stop();
		}
	}

	public void update(final double elapsed) {
		if (!this.playing || this.source == null) {
			return;
		}

		final Object group = this.owner.getAudioGroup();
		if (group != this.group) {
			this.group = group;
			this.source.group(group);
		}

		final Point location = this.owner.getLocation();
		this.source.gain(this.owner.getVolume() * 0.3F * (location == null ? 1F : this.computeDistanceVolume(location)));
		if (!this.buffersQueued) {
			if (this.sampleQueue.remainingCapacity() > 0 && this.getQueuedSamples() < this.sampleRate * this.channels / 5) {
				return;
			}

			this.skip((int) (elapsed * this.sampleRate) * this.channels);
			this.fill();
			this.buffersQueued = true;
			this.source.play();
			return;
		}

		if (!this.source.isPlaying() && this.source.getBufferedSamples() == 0) {
			this.skip((int) ((long) (elapsed * this.sampleRate) * this.channels - this.dequeued));
		}

		this.fill();
		if (!this.source.isPlaying()) {
			this.source.play();
		}
	}

	public void pushSamples(final Buffer[] samples, final int skip) {
		final ShortBuffer buffer = (ShortBuffer) samples[0];
		final short[] data = new short[buffer.remaining()];
		buffer.get(data);
		this.offer(data, skip * this.channels);
	}

	public boolean isFull() {
		return this.source != null && this.sampleQueue.remainingCapacity() == 0;
	}

	private void clearQueue() {
		this.sampleQueue.clear();
		this.pendingSamples = null;
		this.pendingOffset = 0;
		this.dequeued = 0L;
	}

	private int getQueuedSamples() {
		int samples = 0;
		for (final short[] chunk : this.sampleQueue) {
			samples += chunk.length;
		}
		return samples;
	}

	private void offer(final short[] samples, final int skip) {
		if (skip <= 0) {
			this.sampleQueue.offer(samples);
		} else if (skip < samples.length) {
			this.sampleQueue.offer(Arrays.copyOfRange(samples, skip, samples.length));
		}
	}

	private void skip(final int samples) {
		int remaining = samples;
		while (remaining > 0 && !this.sampleQueue.isEmpty()) {
			final short[] next = this.sampleQueue.poll();
			if (next.length > remaining) {
				this.pendingSamples = next;
				this.pendingOffset = remaining;
				this.dequeued += remaining;
				return;
			}

			remaining -= next.length;
			this.dequeued += next.length;
		}
	}

	private float computeDistanceVolume(final Point location) {
		final Point listener = VideoAudioPlayer.audioListenerPosition == null ? Point.of(0D, 0D) : VideoAudioPlayer.audioListenerPosition.getListenerPosition();
		final double dx = listener.getX() - location.getX();
		final double dy = listener.getY() - location.getY();
		final double dz = listener.getZ() - location.getZ();
		final double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		final float referenceDistance = this.owner.getReferenceDistance();
		final float maxDistance = this.owner.getMaxDistance();

		if (distance <= referenceDistance) {
			return 1F;
		}

		if (distance >= maxDistance) {
			return 0F;
		}

		final float t = (float) ((distance - referenceDistance) / (maxDistance - referenceDistance));
		return (1F - t) * (1F - t);
	}

	private void fill() {
		final int target = 8 * (4096 / this.channels) * Math.min(this.channels, 2);
		while (this.source.getBufferedSamples() < target) {
			final short[] merged = this.mergeNextChunk();
			if (merged == null) {
				return;
			}
			this.source.write(merged);
		}
	}

	private short[] mergeNextChunk() {
		final short[] chunk = new short[4096 / this.channels * this.channels];
		int offset = 0;

		if (this.pendingSamples != null) {
			final int remaining = this.pendingSamples.length - this.pendingOffset;
			final int toCopy = Math.min(remaining, chunk.length);
			System.arraycopy(this.pendingSamples, this.pendingOffset, chunk, 0, toCopy);
			offset += toCopy;
			this.pendingOffset += toCopy;
			if (this.pendingOffset >= this.pendingSamples.length) {
				this.pendingSamples = null;
				this.pendingOffset = 0;
			}
		}

		while (offset < chunk.length) {
			final short[] samples = this.sampleQueue.poll();
			if (samples == null) {
				break;
			}

			final int toCopy = Math.min(samples.length, chunk.length - offset);
			System.arraycopy(samples, 0, chunk, offset, toCopy);
			offset += toCopy;

			if (toCopy < samples.length) {
				this.pendingSamples = samples;
				this.pendingOffset = toCopy;
			}
		}

		if (offset == 0) {
			return null;
		}

		this.dequeued += offset;
		return offset < chunk.length ? Arrays.copyOf(chunk, offset) : chunk;
	}

}