package dev.joid.backend.lwjgl2.render;

import dev.joid.backend.lwjgl2.snapshot.Lwjgl2SnapshotBackend;
import dev.joid.test.contract.RenderBridgeContractSuite;
import dev.joid.test.snapshot.ISnapshotBackend;
import lombok.NonNull;

public class RenderBridgeContractTest extends RenderBridgeContractSuite {

	@Override
	protected @NonNull ISnapshotBackend createBackend() {
		return new Lwjgl2SnapshotBackend();
	}

}