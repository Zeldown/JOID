package be.zeldown.joid.lib.bridge.audio;

import be.zeldown.joid.lib.bridge.IBridge;
import lombok.NonNull;

public interface IAudioBridge extends IBridge {

	public @NonNull IAudioSource createSource(final int sampleRate, final int channels);

}