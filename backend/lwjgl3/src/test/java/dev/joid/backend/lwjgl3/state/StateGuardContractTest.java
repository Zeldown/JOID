package dev.joid.backend.lwjgl3.state;

import dev.joid.backend.lwjgl3.snapshot.Lwjgl3SnapshotBackend;
import dev.joid.base.lwjgl3.opengl.snapshot.Lwjgl3StateGuardBackend;
import dev.joid.test.contract.IStateGuardBackend;
import dev.joid.test.contract.StateGuardContractSuite;
import lombok.NonNull;

public class StateGuardContractTest extends StateGuardContractSuite {

	@Override
	protected @NonNull IStateGuardBackend createBackend() {
		return new Lwjgl3StateGuardBackend(new Lwjgl3SnapshotBackend());
	}

}