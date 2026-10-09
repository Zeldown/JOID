package dev.joid.backend.lwjgl2.snapshot;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

import dev.joid.backend.lwjgl2.Backend;
import dev.joid.backend.lwjgl2.Natives;
import dev.joid.backend.lwjgl2.binding.Lwjgl2GlBinding;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.base.opengl.snapshot.GlSnapshotCapture;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Lwjgl2SnapshotBackend implements ISnapshotBackend {

	private Pbuffer        buffer;
	private GlRenderBridge bridge;

	@Override
	public void destroy() {
		this.buffer.destroy();
	}

	@Override
	public void create(final int width, final int height) {
		Natives.install();
		try {
			this.buffer = new Pbuffer(width, height, new PixelFormat().withDepthBits(24).withStencilBits(8), null);
			this.buffer.makeCurrent();
		} catch (final LWJGLException e) {
			throw new IllegalStateException("Unable to create the LWJGL 2 pbuffer", e);
		}

		Backend.register();
		this.bridge = (GlRenderBridge) BridgeHandler.RENDER.get();
	}

	@Override
	public void present() {}

	@Override
	public @NonNull SnapshotImage capture(final int width, final int height) {
		return GlSnapshotCapture.capture(this.bridge, width, height);
	}

	@Override
	public @NonNull String getRenderer() {
		return GlSnapshotCapture.getRenderer(Lwjgl2GlBinding.inst());
	}

}