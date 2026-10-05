package be.zeldown.joid.impl.lwjgl2.snapshot;

import be.zeldown.joid.test.snapshot.ISnapshotBackend;
import be.zeldown.joid.test.snapshot.SnapshotSuite;
import lombok.NonNull;

public class SnapshotTest extends SnapshotSuite {

	@Override
	protected @NonNull ISnapshotBackend createBackend() {
		return new SnapshotBackend();
	}

}