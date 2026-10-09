package dev.joid.backend.lwjgl2.render;

import dev.joid.backend.lwjgl2.snapshot.Lwjgl2SnapshotBackend;
import dev.joid.test.contract.BitmapFontContractSuite;
import dev.joid.test.snapshot.ISnapshotBackend;
import lombok.NonNull;

public class BitmapFontContractTest extends BitmapFontContractSuite {

	@Override
	protected @NonNull ISnapshotBackend createBackend() {
		return new Lwjgl2SnapshotBackend();
	}

}