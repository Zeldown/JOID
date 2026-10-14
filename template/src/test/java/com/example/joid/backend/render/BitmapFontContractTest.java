package com.example.joid.backend.render;

import dev.joid.test.contract.BitmapFontContractSuite;
import dev.joid.test.snapshot.ISnapshotBackend;

import com.example.joid.backend.snapshot.ExampleSnapshotBackend;

public class BitmapFontContractTest extends BitmapFontContractSuite {

	@Override
	protected ISnapshotBackend createBackend() {
		return new ExampleSnapshotBackend();
	}

}