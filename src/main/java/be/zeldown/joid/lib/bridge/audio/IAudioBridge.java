package be.zeldown.joid.lib.bridge.audio;

import lombok.NonNull;

public interface IAudioBridge {

	public @NonNull IAudioSource createSource(final int sampleRate, final int channels);

}