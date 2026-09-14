package be.zeldown.joid.test.snapshot;

import lombok.NonNull;

public interface ISnapshotBackend {

	public void create(final int width, final int height);
	public void frame(final @NonNull Runnable draw);
	public @NonNull SnapshotImage capture(final int width, final int height);
	public void present();
	public void destroy();
	public @NonNull String getRenderer();

}