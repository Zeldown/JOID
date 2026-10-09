package dev.joid.lib.video;

import javax.vecmath.Vector3f;

import lombok.NonNull;

@FunctionalInterface
public interface AudioListenerPosition {

	public @NonNull Vector3f getListenerPosition();

}