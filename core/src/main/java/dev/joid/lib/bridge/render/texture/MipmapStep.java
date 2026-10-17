package dev.joid.lib.bridge.render.texture;

@FunctionalInterface
public interface MipmapStep {

	public void copy(final int level, final int sourceWidth, final int sourceHeight, final int targetWidth, final int targetHeight);

}