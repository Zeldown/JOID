package dev.joid.backend.lwjgl3.render;

import dev.joid.backend.lwjgl3.snapshot.Lwjgl3SnapshotBackend;
import dev.joid.base.lwjgl3.opengl.Lwjgl3GlBinding;
import dev.joid.base.opengl.render.texture.GlBorrowedTextureBackend;
import dev.joid.test.contract.BorrowedTextureContractSuite;
import dev.joid.test.contract.IBorrowedTextureBackend;
import lombok.NonNull;

public class BorrowedTextureContractTest extends BorrowedTextureContractSuite {

	@Override
	protected @NonNull IBorrowedTextureBackend createBackend() {
		return new GlBorrowedTextureBackend(new Lwjgl3SnapshotBackend(), Lwjgl3GlBinding::inst);
	}

}