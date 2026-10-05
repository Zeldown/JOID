package com.example.joid.engine.snapshot;

import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotSuite;

public class SnapshotTest extends SnapshotSuite {

	@Override
	protected ISnapshotBackend createBackend() {
		return new SnapshotBackend();
	}

}