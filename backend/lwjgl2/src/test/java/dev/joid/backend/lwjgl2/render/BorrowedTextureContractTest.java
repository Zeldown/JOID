package dev.joid.backend.lwjgl2.render;

import dev.joid.backend.lwjgl2.snapshot.Lwjgl2SnapshotBackend;
import dev.joid.base.lwjgl2.opengl.Lwjgl2GlBinding;
import dev.joid.base.opengl.snapshot.GlBorrowedTextureBackend;
import dev.joid.test.contract.BorrowedTextureContractSuite;
import dev.joid.test.contract.IBorrowedTextureBackend;
import lombok.NonNull;

public class BorrowedTextureContractTest extends BorrowedTextureContractSuite {

	@Override
	protected @NonNull IBorrowedTextureBackend createBackend() {
		return new GlBorrowedTextureBackend(new Lwjgl2SnapshotBackend(), Lwjgl2GlBinding::inst);
	}

}