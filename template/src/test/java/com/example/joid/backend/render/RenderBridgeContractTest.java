package com.example.joid.backend.render;

import com.example.joid.backend.snapshot.ExampleSnapshotBackend;

import dev.joid.test.contract.RenderBridgeContractSuite;
import dev.joid.test.snapshot.ISnapshotBackend;

public class RenderBridgeContractTest extends RenderBridgeContractSuite {

	@Override
	protected ISnapshotBackend createBackend() {
		return new ExampleSnapshotBackend();
	}

}