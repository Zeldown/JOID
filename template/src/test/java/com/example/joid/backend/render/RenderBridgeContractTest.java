package com.example.joid.backend.render;

import dev.joid.test.contract.RenderBridgeContractSuite;
import dev.joid.test.snapshot.ISnapshotBackend;

import com.example.joid.backend.snapshot.ExampleSnapshotBackend;

public class RenderBridgeContractTest extends RenderBridgeContractSuite {

	@Override
	protected ISnapshotBackend createBackend() {
		return new ExampleSnapshotBackend();
	}

}