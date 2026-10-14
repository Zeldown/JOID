package dev.joid.backend.vulkan.state;

import dev.joid.test.contract.IStateGuardBackend;
import dev.joid.test.contract.StateGuardContractSuite;
import lombok.NonNull;

public class StateGuardContractTest extends StateGuardContractSuite {

	@Override
	protected @NonNull IStateGuardBackend createBackend() {
		return new VulkanStateGuardBackend();
	}

}