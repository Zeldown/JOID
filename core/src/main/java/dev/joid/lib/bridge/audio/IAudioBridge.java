package dev.joid.lib.bridge.audio;

import dev.joid.lib.bridge.IBridge;
import lombok.NonNull;

public interface IAudioBridge extends IBridge {

	public @NonNull IAudioSource createSource(final int sampleRate, final int channels);

}