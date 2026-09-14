package be.zeldown.joid.lib.video;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.ArrayBlockingQueue;

import javax.vecmath.Vector3f;

import be.zeldown.joid.lib.bridge.BridgeHandler;
import be.zeldown.joid.lib.bridge.audio.IAudioSource;
import lombok.NonNull;

public final class VideoAudioPlayer {

	private static final int BUFFER_COUNT = 8;
	private static final float BASE_VOLUME = 0.3F;
	private static final int SAMPLES_PER_BUFFER = 4096;

	private static AudioListener audioListener;

	private final int channels;
	private final int sampleRate;
	private final ArrayBlockingQueue<short[]> sampleQueue = new ArrayBlockingQueue<>(128);

	private volatile float volume = 1F;

	private IAudioSource source;
	private boolean initialized;
	private boolean playing;
	private boolean buffersQueued;

	private int pendingOffset;
	private short[] pendingSamples;
	private boolean needsFlush = true;

	private float posX;
	private float posY;
	private float posZ;
	private boolean positional;
	private float maxDistance = 50F;
	private float referenceDistance = 5F;

	public VideoAudioPlayer(final int sampleRate, final int channels) {
		this.sampleRate = sampleRate;
		this.channels = channels;
	}

	public void play() {
		if (!this.initialized) {
			try {
				this.source = BridgeHandler.getAudio().createSource(this.sampleRate, this.channels);
				this.initialized = true;
			} catch (final Exception e) {
				e.printStackTrace();
				return;
			}
		}

		this.playing = true;
		this.buffersQueued = false;
		this.needsFlush = true;
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

	public void stop() {
		this.playing = false;
		if (this.source != null) {
			this.source.stop();
		}
	}

	public void flush() {
		this.sampleQueue.clear();
		this.pendingSamples = null;
		this.pendingOffset = 0;
		this.buffersQueued = false;
		if (this.source != null) {
			this.source.clear();
		}
	}

	public void update() {
		if (!this.initialized || !this.playing || this.source == null) {
			return;
		}

		if (!this.buffersQueued) {
			if (this.needsFlush) {
				this.sampleQueue.clear();
				this.pendingSamples = null;
				this.pendingOffset = 0;
				this.needsFlush = false;
				return;
			}

			if (this.sampleQueue.size() < VideoAudioPlayer.BUFFER_COUNT * 2) {
				return;
			}

			for (int i = 0; i < VideoAudioPlayer.BUFFER_COUNT; i++) {
				final short[] merged = this.mergeNextChunk();
				if (merged != null) {
					this.source.queue(merged);
				}
			}

			this.buffersQueued = true;
			this.source.gain(this.volume * VideoAudioPlayer.BASE_VOLUME);
			this.source.play();
			return;
		}

		final int processed = this.source.getProcessedBuffers();
		for (int i = 0; i < processed; i++) {
			final short[] merged = this.mergeNextChunk();
			if (merged == null) {
				break;
			}
			this.source.queue(merged);
		}

		if (!this.source.isPlaying() && this.playing && this.source.getQueuedBuffers() > 0) {
			this.source.play();
		}

		this.source.gain(this.volume * VideoAudioPlayer.BASE_VOLUME);

		if (this.positional) {
			final float distanceVolume = this.computeDistanceVolume();
			if (distanceVolume <= 0.001F) {
				if (this.source.isPlaying()) {
					this.source.pause();
				}
				this.sampleQueue.clear();
				return;
			}

			this.source.gain(this.volume * VideoAudioPlayer.BASE_VOLUME * distanceVolume);
		}
	}

	public void pushSamples(final @NonNull Buffer[] samples) {
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
					final float val = Math.max(-1F, Math.min(1F, ((FloatBuffer) samples[ch]).get())) * 0.6F;
					interleaved[i * channelCount + ch] = (short) (val * 32767F);
				}
			}

			this.sampleQueue.offer(interleaved);
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

				this.sampleQueue.offer(interleaved);
			} else {
				final ShortBuffer sb = (ShortBuffer) samples[0];
				final short[] data = new short[sb.remaining()];
				sb.get(data);
				this.sampleQueue.offer(data);
			}
		}
	}

	public int getQueueSize() {
		return this.sampleQueue.size();
	}

	public void setVolume(final float volume) {
		this.volume = volume;
	}

	public void setLocation(final float x, final float y, final float z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.positional = true;
	}

	public void setReferenceDistance(final float distance) {
		this.referenceDistance = distance;
	}

	public void setMaxDistance(final float distance) {
		this.maxDistance = distance;
	}

	public void cleanup() {
		if (this.source != null) {
			this.source.stop();
			this.source.delete();
			this.source = null;
		}

		this.sampleQueue.clear();
		this.pendingSamples = null;
		this.initialized = false;
	}

	/* [ Internal Section ] */
	private float computeDistanceVolume() {
		if (VideoAudioPlayer.audioListener == null) {
			return 1F;
		}

		final Vector3f listenerPos = VideoAudioPlayer.audioListener.getListenerPosition();
		final double dx = listenerPos.x - this.posX;
		final double dy = listenerPos.y - this.posY;
		final double dz = listenerPos.z - this.posZ;
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

	private short[] mergeNextChunk() {
		final short[] chunk = new short[VideoAudioPlayer.SAMPLES_PER_BUFFER];
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

		if (offset < chunk.length) {
			final short[] trimmed = new short[offset];
			System.arraycopy(chunk, 0, trimmed, 0, offset);
			return trimmed;
		}

		return chunk;
	}

	/* [ Static Section ] */
	public static AudioListener getAudioListener() {
		return VideoAudioPlayer.audioListener;
	}

	public static void setAudioListener(final AudioListener audioListener) {
		VideoAudioPlayer.audioListener = audioListener;
	}

}