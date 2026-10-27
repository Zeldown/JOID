package com.example.backend;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.example.backend.engine.Engine;

import dev.joid.lib.utils.image.PixelLayout;
import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotBridgeRestorer;
import dev.joid.test.snapshot.SnapshotImage;

// #region snapshot
public final class EngineSnapshotBackend implements ISnapshotBackend {

	private Engine                 engine;
	private SnapshotBridgeRestorer restorer;

	@Override
	public void create(final int width, final int height) {
		this.restorer = new SnapshotBridgeRestorer();
		this.engine   = Engine.createHidden(width, height);
		EngineBackend.create(this.engine).audio(false).register();
	}

	@Override
	public void destroy() {
		this.engine.destroy();
		this.restorer.restore();
	}

	@Override
	public void present() {
		this.engine.present();
	}

	@Override
	public SnapshotImage capture(final int width, final int height) {
		final ByteBuffer pixels = ByteBuffer.allocateDirect(width * height * 4).order(ByteOrder.nativeOrder());
		this.engine.getDevice().readPixels(0, 0, width, height, pixels);
		return SnapshotImage.from(pixels, width, height, true, PixelLayout.RGBA8);
	}

	@Override
	public String getRenderer() {
		return this.engine.getDevice().getName();
	}

}
// #endregion