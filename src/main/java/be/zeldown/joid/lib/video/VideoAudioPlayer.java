package be.zeldown.joid.lib.video;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.ArrayBlockingQueue;

import javax.vecmath.Vector3f;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;

import lombok.NonNull;

public final class VideoAudioPlayer {

	private static final int BUFFER_COUNT = 8;
	private static final float BASE_VOLUME = 0.3F;
	private static final int SAMPLES_PER_BUFFER = 4096;

	private static AudioListener audioListener;
	private static boolean openALLoaded = false;
	private static boolean shutdownHookRegistered = false;

	private final int alFormat;
	private final int sampleRate;
	private final ArrayBlockingQueue<short[]> sampleQueue = new ArrayBlockingQueue<>(128);

	private volatile float volume = 1F;

	private int source = -1;
	private int[] buffers;
	private boolean initialized;
	private boolean playing;
	private boolean buffersQueued;
	private ByteBuffer uploadBuffer;

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
		this.alFormat = channels > 1 ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16;
	}

	public void play() {
		if (!this.initialized) {
			this.initAL();
		}

		this.playing = true;
		this.buffersQueued = false;
		this.needsFlush = true;
	}

	public void pause() {
		this.playing = false;
		if (this.source != -1) {
			AL10.alSourcePause(this.source);
		}
	}

	public void resume() {
		this.playing = true;
		if (this.source != -1) {
			AL10.alSourcePlay(this.source);
		}
	}

	public void stop() {
		this.playing = false;
		if (this.source != -1) {
			AL10.alSourceStop(this.source);
		}
	}

	public void flush() {
		this.sampleQueue.clear();
		this.pendingSamples = null;
		this.pendingOffset = 0;
		this.buffersQueued = false;
		if (this.source != -1) {
			AL10.alSourceStop(this.source);
			final int queued = AL10.alGetSourcei(this.source, AL10.AL_BUFFERS_QUEUED);
			for (int i = 0; i < queued; i++) {
				AL10.alSourceUnqueueBuffers(this.source);
			}
		}
	}

	public void update() {
		if (!this.initialized || !this.playing || this.source == -1) {
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
					this.fillBuffer(this.buffers[i], merged);
					AL10.alSourceQueueBuffers(this.source, this.buffers[i]);
				}
			}

			this.buffersQueued = true;
			AL10.alSourcef(this.source, AL10.AL_GAIN, this.volume * VideoAudioPlayer.BASE_VOLUME);
			AL10.alSourcePlay(this.source);
			return;
		}

		final int processed = AL10.alGetSourcei(this.source, AL10.AL_BUFFERS_PROCESSED);
		for (int i = 0; i < processed; i++) {
			final short[] merged = this.mergeNextChunk();
			if (merged == null) {
				break;
			}
			final int buffer = AL10.alSourceUnqueueBuffers(this.source);
			this.fillBuffer(buffer, merged);
			AL10.alSourceQueueBuffers(this.source, buffer);
		}

		final int state = AL10.alGetSourcei(this.source, AL10.AL_SOURCE_STATE);
		if (state != AL10.AL_PLAYING && this.playing) {
			final int queued = AL10.alGetSourcei(this.source, AL10.AL_BUFFERS_QUEUED);
			if (queued > 0) {
				AL10.alSourcePlay(this.source);
			}
		}

		AL10.alSourcef(this.source, AL10.AL_GAIN, this.volume * VideoAudioPlayer.BASE_VOLUME);

		if (this.positional) {
			final float distanceVolume = this.computeDistanceVolume();
			if (distanceVolume <= 0.001F) {
				final int currentState = AL10.alGetSourcei(this.source, AL10.AL_SOURCE_STATE);
				if (currentState == AL10.AL_PLAYING) {
					AL10.alSourcePause(this.source);
				}
				this.sampleQueue.clear();
				return;
			}

			AL10.alSourcef(this.source, AL10.AL_GAIN, this.volume * VideoAudioPlayer.BASE_VOLUME * distanceVolume);
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
		if (this.source != -1) {
			AL10.alSourceStop(this.source);
			AL10.alDeleteSources(this.source);
			this.source = -1;
		}

		if (this.buffers != null) {
			for (final int buffer : this.buffers) {
				AL10.alDeleteBuffers(buffer);
			}
			this.buffers = null;
		}

		this.sampleQueue.clear();
		this.pendingSamples = null;
		this.initialized = false;
	}

	/* [ Internal Section ] */
	private void initAL() {
		if (!AL.isCreated()) {
			try {
				VideoAudioPlayer.ensureOpenALLoaded();
				AL.create();
				VideoAudioPlayer.registerShutdownHook();
			} catch (final LWJGLException e) {
				e.printStackTrace();
				return;
			}
		}

		this.source = AL10.alGenSources();
		AL10.alSourcei(this.source, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
		AL10.alSource3f(this.source, AL10.AL_POSITION, 0F, 0F, 0F);

		this.buffers = new int[VideoAudioPlayer.BUFFER_COUNT];
		for (int i = 0; i < VideoAudioPlayer.BUFFER_COUNT; i++) {
			this.buffers[i] = AL10.alGenBuffers();
		}

		this.initialized = true;
	}

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

	private void fillBuffer(final int bufferId, final @NonNull short[] samples) {
		final int needed = samples.length * 2;
		if (this.uploadBuffer == null || this.uploadBuffer.capacity() < needed) {
			this.uploadBuffer = ByteBuffer.allocateDirect(needed).order(ByteOrder.nativeOrder());
		}

		this.uploadBuffer.clear();
		this.uploadBuffer.asShortBuffer().put(samples);
		this.uploadBuffer.limit(needed);
		this.uploadBuffer.position(0);

		AL10.alBufferData(bufferId, this.alFormat, this.uploadBuffer, this.sampleRate);
	}

	/* [ Static Section ] */
	private static void registerShutdownHook() {
		if (VideoAudioPlayer.shutdownHookRegistered) {
			return;
		}
		VideoAudioPlayer.shutdownHookRegistered = true;
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (AL.isCreated()) {
				AL.destroy();
			}
		}, "joid-al-shutdown"));
	}

	private static void ensureOpenALLoaded() {
		if (VideoAudioPlayer.openALLoaded) {
			return;
		}

		final String os = System.getProperty("os.name", "").toLowerCase();
		final boolean is64 = System.getProperty("os.arch", "").contains("64");

		String libName = null;
		if (os.contains("win")) {
			libName = is64 ? "OpenAL64" : "OpenAL32";
		} else if (os.contains("mac") || os.contains("linux")) {
			libName = "openal";
		}

		if (libName == null) {
			return;
		}

		try {
			System.loadLibrary(libName);
			VideoAudioPlayer.openALLoaded = true;
		} catch (final Throwable ignored) {}
	}

	public static AudioListener getAudioListener() {
		return VideoAudioPlayer.audioListener;
	}

	public static void setAudioListener(final AudioListener audioListener) {
		VideoAudioPlayer.audioListener = audioListener;
	}

}