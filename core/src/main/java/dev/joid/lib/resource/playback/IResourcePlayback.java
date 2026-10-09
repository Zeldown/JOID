package dev.joid.lib.resource.playback;

import lombok.NonNull;

public interface IResourcePlayback {

	public @NonNull IResourcePlayback play();
	public @NonNull IResourcePlayback stop();
	public @NonNull IResourcePlayback pause();
	public @NonNull IResourcePlayback resume();
	public @NonNull IResourcePlayback seek(final double seconds);

	public @NonNull IResourcePlayback loop(final boolean loop);
	public @NonNull IResourcePlayback autoplay(final boolean autoplay);

	public boolean isLoop();
	public boolean isPaused();
	public boolean isPlaying();
	public boolean isAutoplay();

	public double getDuration();
	public double getProgress();
	public double getCurrentTime();

	public default @NonNull IResourcePlayback restart() {
		return this.stop().seek(0D).play();
	}

}