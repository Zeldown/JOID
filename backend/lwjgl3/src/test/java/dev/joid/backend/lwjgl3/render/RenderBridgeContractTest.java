package dev.joid.backend.lwjgl3.render;

import dev.joid.backend.lwjgl3.snapshot.Lwjgl3SnapshotBackend;
import dev.joid.test.contract.RenderBridgeContractSuite;
import dev.joid.test.snapshot.ISnapshotBackend;
import lombok.NonNull;

public class RenderBridgeContractTest extends RenderBridgeContractSuite {

	@Override
	protected @NonNull ISnapshotBackend createBackend() {
		return new Lwjgl3SnapshotBackend();
	}

}