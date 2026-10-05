package com.example.joid.engine.render;

import be.zeldown.joid.test.contract.RenderBridgeContractSuite;
import be.zeldown.joid.test.snapshot.ISnapshotBackend;

import com.example.joid.engine.snapshot.SnapshotBackend;

public class RenderBridgeContractTest extends RenderBridgeContractSuite {

	@Override
	protected ISnapshotBackend createBackend() {
		return new SnapshotBackend();
	}

}