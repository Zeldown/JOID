package dev.joid.impl.lwjgl2.snapshot;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

import dev.joid.impl.lwjgl2.Backend;
import dev.joid.impl.lwjgl2.Natives;
import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.NonNull;

public final class SnapshotBackend implements ISnapshotBackend {

	private Pbuffer buffer;

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
	}

	@Override
	public void present() {
		GL11.glFlush();
	}

	@Override
	public void frame(final @NonNull Runnable draw) {
		draw.run();
	}

	@Override
	public @NonNull SnapshotImage capture(final int width, final int height) {
		final ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
		GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
		return SnapshotImage.fromBytes(pixels, width, height, true, false);
	}

	@Override
	public @NonNull String getRenderer() {
		return GL11.glGetString(GL11.GL_RENDERER);
	}

}