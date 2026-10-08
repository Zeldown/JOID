package dev.joid.base.openal;

import dev.joid.base.openal.binding.IAlBinding;
import dev.joid.lib.bridge.audio.IAudioBridge;
import dev.joid.lib.bridge.audio.IAudioSource;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class AlAudioBridge implements IAudioBridge {

	private final IAlBinding binding;

	private IAudioGain hostGain;

	private AlAudioBridge(final IAlBinding binding) {
		this.binding  = binding;
		this.hostGain = gain -> gain;
	}

	public static @NonNull AlAudioBridge create(final @NonNull IAlBinding binding) {
		return new AlAudioBridge(binding);
	}

	public @NonNull AlAudioBridge hostGain(final @NonNull IAudioGain hostGain) {
		this.hostGain = hostGain;
		return this;
	}

	@Override
	public @NonNull IAudioSource createSource(final int sampleRate, final int channels) {
		if (this.binding.getCurrentContext() == null) {
			this.binding.createContext();
			Runtime.getRuntime().addShutdownHook(new Thread(this.binding::destroyContext, "joid-al-shutdown"));
		}

		return AlAudioSource.create(this.binding, this.hostGain, sampleRate, channels);
	}

}