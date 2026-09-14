package be.zeldown.joid.impl.openal;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;

import be.zeldown.joid.lib.bridge.audio.IAudioBridge;
import be.zeldown.joid.lib.bridge.audio.IAudioSource;
import lombok.NonNull;

public final class OpenALAudioBridge implements IAudioBridge {

	private long device;
	private long context;

	@Override
	public @NonNull IAudioSource createSource(final int sampleRate, final int channels) {
		if (ALC10.alcGetCurrentContext() == 0L) {
			this.createContext();
		}

		return new OpenALAudioSource(sampleRate, channels);
	}

	private void createContext() {
		this.device = ALC10.alcOpenDevice((ByteBuffer) null);
		if (this.device == 0L) {
			throw new IllegalStateException("Unable to open the default OpenAL device");
		}

		this.context = ALC10.alcCreateContext(this.device, (IntBuffer) null);
		ALC10.alcMakeContextCurrent(this.context);
		AL.createCapabilities(ALC.createCapabilities(this.device));
		Runtime.getRuntime().addShutdownHook(new Thread(this::destroyContext, "joid-al-shutdown"));
	}

	private void destroyContext() {
		ALC10.alcMakeContextCurrent(0L);
		ALC10.alcDestroyContext(this.context);
		ALC10.alcCloseDevice(this.device);
	}

}