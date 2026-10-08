package dev.joid.backend.lwjgl3.host;

import dev.joid.test.contract.HostStateContractSuite;
import dev.joid.test.contract.IHostStateBackend;
import lombok.NonNull;

public class HostStateContractTest extends HostStateContractSuite {

	@Override
	protected @NonNull IHostStateBackend createBackend() {
		return new HostStateBackend();
	}

}