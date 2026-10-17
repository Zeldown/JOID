package dev.joid.lib.video;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;

import dev.joid.internal.JOIDLog;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.bridge.audio.IAudioSource;
import dev.joid.lib.utils.geom.Point;
import lombok.NonNull;
import lombok.Setter;

public final class VideoAudioPlayer {

	@Setter
	private static IAudioListenerPosition audioListenerPosition;

	private final String                      name;
	private final int                         channels;
	private final int                         sampleRate;
	private final ArrayBlockingQueue<short[]> sampleQueue = new ArrayBlockingQueue<>(128);

	private volatile Object group;

	@Setter
	private volatile float volume = 1F;

	private boolean      playing;
	private IAudioSource source;
	private boolean      initialized;
	private boolean      buffersQueued;

	private int     pendingOffset;
	private short[] pendingSamples;

	private float   posX;
	private float   posY;
	private float   posZ;
	private boolean positional;

	@Setter private float maxDistance       = 50F;
	@Setter private float referenceDistance = 5F;

	public VideoAudioPlayer(final String name, final int sampleRate, final int channels) {
		this.name = name;
		this.sampleRate = sampleRate;
		this.channels = channels;
	}

	public void stop() {
		this.playing = false;
		if (this.source != null) {
			this.source.stop();
		}
	}

	public void play() {
		if (!this.initialized) {
			this.initialized = true;
			try {
				final IAudioSource source = BridgeHandler.AUDIO.get().createSource(this.sampleRate, this.channels);
				source.group(this.group);
				this.source = source;
			} catch (final Exception exception) {
				JOIDLog.warnOnce("VideoAudioPlayer.source." + this.name, "The video " + this.name + " plays without sound (" + exception + "), register an audio bridge and check the audio device");
			}
		}

		this.playing = true;
		this.buffersQueued = false;
		this.clearQueue();
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
		if (!this.initialized || !this.playing || this.source == null) {
			return;
		}

		if (!this.buffersQueued) {
			if (this.sampleQueue.size() < 16) {
				return;
			}

			this.skip((int) (elapsed * this.sampleRate) * this.channels);
			this.fill();
			this.buffersQueued = true;
			this.source.gain(this.volume * 0.3F);
			this.source.play();
			return;
		}

		this.fill();
		final float distanceVolume = this.positional ? this.computeDistanceVolume() : 1F;
		if (distanceVolume <= 0.001F) {
			if (this.source.isPlaying()) {
				this.source.pause();
			}
			this.sampleQueue.clear();
			return;
		}

		this.source.gain(this.volume * 0.3F * distanceVolume);
		if (!this.source.isPlaying()) {
			this.source.play();
		}
	}

	public void pushSamples(final @NonNull Buffer[] samples, final int skip) {
		if (samples.length == 0 || samples[0] == null) {
			return;
		}

		if (samples[0] instanceof FloatBuffer) {
			final int channelCount = samples.length;
			final FloatBuffer first = (FloatBuffer) samples[0];
			final int samplesPerChannel = first.remaining();
			final short[] interleaved = new short[samplesPerChannel * channelCount];

			for (int i = 0; i < samplesPerChannel; i++) {
				for (int ch = 0; ch < channelCount; ch++) {
					final float val = Math.max(-1F, Math.min(1F, ((FloatBuffer) samples[ch]).get()));
					interleaved[i * channelCount + ch] = (short) (val * 32767F);
				}
			}

			this.offer(interleaved, skip * this.channels);
		} else if (samples[0] instanceof ShortBuffer) {
			if (samples.length > 1) {
				final int channelCount = samples.length;
				final int samplesPerChannel = ((ShortBuffer) samples[0]).remaining();
				final short[] interleaved = new short[samplesPerChannel * channelCount];

				for (int i = 0; i < samplesPerChannel; i++) {
					for (int ch = 0; ch < channelCount; ch++) {
						interleaved[i * channelCount + ch] = ((ShortBuffer) samples[ch]).get();
					}
				}

				this.offer(interleaved, skip * channelCount);
			} else {
				final ShortBuffer sb = (ShortBuffer) samples[0];
				final short[] data = new short[sb.remaining()];
				sb.get(data);
				this.offer(data, skip * this.channels);
			}
		}
	}

	public void setGroup(final Object group) {
		this.group = group;
		if (this.source != null) {
			this.source.group(group);
		}
	}

	public void setLocation(final float x, final float y, final float z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.positional = true;
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

	private void clearQueue() {
		this.sampleQueue.clear();
		this.pendingSamples = null;
		this.pendingOffset = 0;
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
				return;
			}

			remaining -= next.length;
		}
	}

	private float computeDistanceVolume() {
		final Point listenerPos = VideoAudioPlayer.audioListenerPosition == null ? Point.create(0D, 0D) : VideoAudioPlayer.audioListenerPosition.getListenerPosition();
		final double dx = listenerPos.getX() - this.posX;
		final double dy = listenerPos.getY() - this.posY;
		final double dz = listenerPos.getZ() - this.posZ;
		final double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

		if (distance <= this.referenceDistance) {
			return 1F;
		}

		if (distance >= this.maxDistance) {
			return 0F;
		}

		final float t = (float) ((distance - this.referenceDistance) / (this.maxDistance - this.referenceDistance));
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

		return offset < chunk.length ? Arrays.copyOf(chunk, offset) : chunk;
	}

}