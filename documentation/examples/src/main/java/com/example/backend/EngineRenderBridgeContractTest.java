package com.example.backend;

import dev.joid.test.contract.RenderBridgeContractSuite;
import dev.joid.test.snapshot.ISnapshotBackend;

// #region contract
public class EngineRenderBridgeContractTest extends RenderBridgeContractSuite {

	@Override
	protected ISnapshotBackend createBackend() {
		return new EngineSnapshotBackend();
	}

}
// #endregion