package dev.joid.lib.video;

import dev.joid.lib.utils.point.Point;
import lombok.NonNull;

@FunctionalInterface
public interface AudioListenerPosition {

	public @NonNull Point getListenerPosition();

}