package dev.joid.backend.vulkan.render;

import dev.joid.backend.vulkan.snapshot.VulkanSnapshotBackend;
import dev.joid.test.contract.RenderBridgeContractSuite;
import dev.joid.test.snapshot.ISnapshotBackend;
import lombok.NonNull;

public class RenderBridgeContractTest extends RenderBridgeContractSuite {

	@Override
	protected @NonNull ISnapshotBackend createBackend() {
		return new VulkanSnapshotBackend();
	}

}