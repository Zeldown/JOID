package dev.joid.test.snapshot;

import lombok.NonNull;

public interface ISnapshotBackend {

	public void create(final int width, final int height);
	public void destroy();

	public void present();
	public @NonNull SnapshotImage capture(final int width, final int height);

	public @NonNull String getRenderer();

}