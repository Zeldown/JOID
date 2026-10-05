package be.zeldown.joid.test.snapshot;

import lombok.NonNull;

public interface ISnapshotBackend {

	public void destroy();
	public void present();

	public @NonNull String getRenderer();
	public void frame(final @NonNull Runnable draw);
	public void create(final int width, final int height);

	public @NonNull SnapshotImage capture(final int width, final int height);

}