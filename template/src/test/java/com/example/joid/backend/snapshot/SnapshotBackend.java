package com.example.joid.backend.snapshot;

import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotImage;

public final class SnapshotBackend implements ISnapshotBackend {

	@Override
	public void destroy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void present() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getRenderer() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void create(final int width, final int height) {
		throw new UnsupportedOperationException();
	}

	@Override
	public SnapshotImage capture(final int width, final int height) {
		throw new UnsupportedOperationException();
	}

}