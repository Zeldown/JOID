package dev.joid.lib.video;

import dev.joid.lib.utils.geom.Point;
import lombok.NonNull;

@FunctionalInterface
public interface IAudioListenerPosition {

	public @NonNull Point getListenerPosition();

}