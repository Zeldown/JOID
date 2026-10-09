package dev.joid.backend.vulkan.render;

import dev.joid.backend.vulkan.snapshot.SnapshotBackend;
import dev.joid.test.contract.BitmapFontContractSuite;
import dev.joid.test.snapshot.ISnapshotBackend;
import lombok.NonNull;

public class BitmapFontContractTest extends BitmapFontContractSuite {

	@Override
	protected @NonNull ISnapshotBackend createBackend() {
		return new SnapshotBackend();
	}

}