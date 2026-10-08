package dev.joid.backend.lwjgl3.snapshot;

import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotSuite;
import lombok.NonNull;

public class SnapshotTest extends SnapshotSuite {

	@Override
	protected @NonNull ISnapshotBackend createBackend() {
		return new SnapshotBackend();
	}

}