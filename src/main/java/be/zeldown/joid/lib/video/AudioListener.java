package be.zeldown.joid.lib.video;

import javax.vecmath.Vector3f;

import lombok.NonNull;

@FunctionalInterface
public interface AudioListener {

	public @NonNull Vector3f getListenerPosition();

}