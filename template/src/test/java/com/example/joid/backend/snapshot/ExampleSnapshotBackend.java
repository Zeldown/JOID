package com.example.joid.backend.snapshot;

import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotImage;

public final class ExampleSnapshotBackend implements ISnapshotBackend {

	@Override
	public void destroy() {
		throw new UnsupportedOperationException("ExampleSnapshotBackend.destroy() is not implemented, implement it with the API of your engine");
	}

	@Override
	public void present() {
		throw new UnsupportedOperationException("ExampleSnapshotBackend.present() is not implemented, implement it with the API of your engine");
	}

	@Override
	public String getRenderer() {
		throw new UnsupportedOperationException("ExampleSnapshotBackend.getRenderer() is not implemented, implement it with the API of your engine");
	}

	@Override
	public void create(final int width, final int height) {
		throw new UnsupportedOperationException("ExampleSnapshotBackend.create() is not implemented, implement it with the API of your engine");
	}

	@Override
	public SnapshotImage capture(final int width, final int height) {
		throw new UnsupportedOperationException("ExampleSnapshotBackend.capture() is not implemented, implement it with the API of your engine");
	}

}