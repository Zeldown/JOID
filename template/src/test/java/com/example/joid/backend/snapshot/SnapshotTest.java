package com.example.joid.backend.snapshot;

import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotSuite;

public class SnapshotTest extends SnapshotSuite {

	@Override
	protected ISnapshotBackend createBackend() {
		return new ExampleSnapshotBackend();
	}

}