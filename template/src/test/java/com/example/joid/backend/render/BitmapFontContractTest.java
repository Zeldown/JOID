package com.example.joid.backend.render;

import com.example.joid.backend.snapshot.ExampleSnapshotBackend;

import dev.joid.test.contract.BitmapFontContractSuite;
import dev.joid.test.snapshot.ISnapshotBackend;

public class BitmapFontContractTest extends BitmapFontContractSuite {

	@Override
	protected ISnapshotBackend createBackend() {
		return new ExampleSnapshotBackend();
	}

}