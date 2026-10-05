package be.zeldown.joid.impl.vulkan.render;

import be.zeldown.joid.impl.vulkan.snapshot.SnapshotBackend;
import be.zeldown.joid.test.contract.RenderBridgeContractSuite;
import be.zeldown.joid.test.snapshot.ISnapshotBackend;
import lombok.NonNull;

public class RenderBridgeContractTest extends RenderBridgeContractSuite {

	@Override
	protected @NonNull ISnapshotBackend createBackend() {
		return new SnapshotBackend();
	}

}