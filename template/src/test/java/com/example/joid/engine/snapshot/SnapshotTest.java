package com.example.joid.engine.snapshot;

import be.zeldown.joid.test.snapshot.ISnapshotBackend;
import be.zeldown.joid.test.snapshot.SnapshotSuite;

public class SnapshotTest extends SnapshotSuite {

	@Override
	protected ISnapshotBackend createBackend() {
		return new SnapshotBackend();
	}

}