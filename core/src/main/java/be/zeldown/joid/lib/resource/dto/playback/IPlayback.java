package be.zeldown.joid.lib.resource.dto.playback;

import lombok.NonNull;

public interface IPlayback {

	public @NonNull IPlayback play();
	public @NonNull IPlayback stop();
	public @NonNull IPlayback pause();
	public @NonNull IPlayback resume();
	public @NonNull IPlayback seek(final double seconds);

	public @NonNull IPlayback loop(final boolean loop);
	public @NonNull IPlayback autoplay(final boolean autoplay);

	public boolean isLoop();
	public boolean isPaused();
	public boolean isPlaying();
	public boolean isAutoplay();

	public double getDuration();
	public double getProgress();
	public double getCurrentTime();

}