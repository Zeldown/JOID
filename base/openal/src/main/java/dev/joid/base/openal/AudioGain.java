package dev.joid.base.openal;

@FunctionalInterface
public interface AudioGain {

	public float apply(final float gain, final Object group);

}