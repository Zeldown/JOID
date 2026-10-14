package dev.joid.backend.lwjgl2.snapshot;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

import dev.joid.backend.lwjgl2.Lwjgl2Backend;
import dev.joid.backend.lwjgl2.Lwjgl2Natives;
import dev.joid.backend.lwjgl2.binding.Lwjgl2GlBinding;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.base.opengl.snapshot.GlSnapshotCapture;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.utils.platform.Platform;
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
		if (this.buffer == null) {
			Display.destroy();
			return;
		}

		this.buffer.destroy();
	}

	@Override
	public void create(final int width, final int height) {
		final Lwjgl2Backend backend = Lwjgl2Backend.create().audio(false);
		Lwjgl2Natives.install();
		final PixelFormat format = new PixelFormat().withDepthBits(24).withStencilBits(8);
		try {
			if (Platform.current() == Platform.LINUX) {
				Display.setDisplayMode(new DisplayMode(width, height));
				Display.create(format);
			} else {
				this.buffer = new Pbuffer(width, height, format, null);
				this.buffer.makeCurrent();
			}
		} catch (final LWJGLException e) {
			throw new IllegalStateException("LWJGL 2 cannot create the " + width + "x" + height + " OpenGL context of the snapshots (" + e.getMessage() + "), check that the graphics driver supports OpenGL", e);
		}

		backend.register();
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