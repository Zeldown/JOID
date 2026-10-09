package dev.joid.backend.vulkan.render;

import dev.joid.test.contract.BorrowedTextureContractSuite;
import dev.joid.test.contract.IBorrowedTextureBackend;
import lombok.NonNull;

public class BorrowedTextureContractTest extends BorrowedTextureContractSuite {

	@Override
	protected @NonNull IBorrowedTextureBackend createBackend() {
		return new VulkanBorrowedTextureBackend();
	}

}