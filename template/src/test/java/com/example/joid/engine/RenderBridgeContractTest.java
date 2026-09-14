package com.example.joid.engine;

import be.zeldown.joid.test.contract.RenderBridgeContractSuite;
import be.zeldown.joid.test.snapshot.ISnapshotBackend;

public class RenderBridgeContractTest extends RenderBridgeContractSuite {

	@Override
	protected ISnapshotBackend createBackend() {
		return new SnapshotBackend();
	}

}