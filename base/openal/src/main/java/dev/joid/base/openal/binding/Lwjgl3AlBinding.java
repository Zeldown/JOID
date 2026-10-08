package dev.joid.base.openal.binding;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl3AlBinding implements IAlBinding {

	private static final Lwjgl3AlBinding INSTANCE = new Lwjgl3AlBinding();

	private long device;
	private long context;

	public static @NonNull Lwjgl3AlBinding inst() {
		return Lwjgl3AlBinding.INSTANCE;
	}

	@Override
	public void createContext() {
		this.device = ALC10.alcOpenDevice((ByteBuffer) null);
		if (this.device == 0L) {
			throw new IllegalStateException("Unable to open the default OpenAL device");
		}

		this.context = ALC10.alcCreateContext(this.device, (IntBuffer) null);
		ALC10.alcMakeContextCurrent(this.context);
		AL.createCapabilities(ALC.createCapabilities(this.device));
	}

	@Override
	public void destroyContext() {
		ALC10.alcMakeContextCurrent(0L);
		ALC10.alcDestroyContext(this.context);
		ALC10.alcCloseDevice(this.device);
	}

	@Override
	public Object getCurrentContext() {
		final long context = ALC10.alcGetCurrentContext();
		return context == 0L ? null : context;
	}

	@Override
	public int genSource() {
		final int source = AL10.alGenSources();
		AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
		AL10.alSource3f(source, AL10.AL_POSITION, 0F, 0F, 0F);
		return source;
	}

	@Override
	public void deleteSource(final int source) {
		AL10.alDeleteSources(source);
	}

	@Override
	public int genBuffer() {
		return AL10.alGenBuffers();
	}

	@Override
	public void deleteBuffer(final int buffer) {
		AL10.alDeleteBuffers(buffer);
	}

	@Override
	public void bufferData(final int buffer, final int channels, final @NonNull short[] samples, final int sampleRate) {
		AL10.alBufferData(buffer, channels > 1 ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16, samples, sampleRate);
	}

	@Override
	public void play(final int source) {
		AL10.alSourcePlay(source);
	}

	@Override
	public void stop(final int source) {
		AL10.alSourceStop(source);
	}

	@Override
	public void pause(final int source) {
		AL10.alSourcePause(source);
	}

	@Override
	public void gain(final int source, final float gain) {
		AL10.alSourcef(source, AL10.AL_GAIN, gain);
	}

	@Override
	public int unqueueBuffer(final int source) {
		return AL10.alSourceUnqueueBuffers(source);
	}

	@Override
	public void queueBuffer(final int source, final int buffer) {
		AL10.alSourceQueueBuffers(source, buffer);
	}

	@Override
	public boolean isPlaying(final int source) {
		return AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
	}

	@Override
	public int getQueuedBuffers(final int source) {
		return AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
	}

	@Override
	public int getProcessedBuffers(final int source) {
		return AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
	}

}