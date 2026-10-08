package dev.joid.backend.lwjgl2.binding;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC10;

import dev.joid.base.openal.binding.IAlBinding;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl2AlBinding implements IAlBinding {

	private static final Lwjgl2AlBinding INSTANCE = new Lwjgl2AlBinding();

	private ByteBuffer uploadBuffer;

	public static @NonNull Lwjgl2AlBinding inst() {
		return Lwjgl2AlBinding.INSTANCE;
	}

	@Override
	public void createContext() {
		try {
			AL.create();
		} catch (final LWJGLException e) {
			throw new IllegalStateException("Unable to create the OpenAL context", e);
		}
	}

	@Override
	public void destroyContext() {
		if (AL.isCreated()) {
			AL.destroy();
		}
	}

	@Override
	public Object getCurrentContext() {
		return AL.isCreated() ? ALC10.alcGetCurrentContext() : null;
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
		final int size = samples.length * 2;
		if (this.uploadBuffer == null || this.uploadBuffer.capacity() < size) {
			this.uploadBuffer = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
		}

		this.uploadBuffer.clear();
		this.uploadBuffer.asShortBuffer().put(samples);
		this.uploadBuffer.limit(size);
		AL10.alBufferData(buffer, channels > 1 ? AL10.AL_FORMAT_STEREO16 : AL10.AL_FORMAT_MONO16, this.uploadBuffer, sampleRate);
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