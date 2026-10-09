package dev.joid.base.openal;

@FunctionalInterface
public interface IAudioGain {

	public float apply(final float gain, final Object group);

}