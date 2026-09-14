package be.zeldown.joid.impl.lwjgl2;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;

import be.zeldown.joid.test.snapshot.ISnapshotBackend;
import be.zeldown.joid.test.snapshot.SnapshotImage;
import lombok.NonNull;

public final class SnapshotBackend implements ISnapshotBackend {

	private Pbuffer buffer;
	private int     width;
	private int     height;

	@Override
	public void create(final int width, final int height) {
		try {
			this.buffer = new Pbuffer(width, height, new PixelFormat().withDepthBits(24).withStencilBits(8), null);
			this.buffer.makeCurrent();
		} catch (final LWJGLException e) {
			throw new IllegalStateException("Unable to create the LWJGL 2 pbuffer", e);
		}

		this.width  = width;
		this.height = height;
		Backend.register();
	}

	@Override
	public void frame(final @NonNull Runnable draw) {
		draw.run();
	}

	@Override
	public @NonNull SnapshotImage capture() {
		final ByteBuffer pixels = BufferUtils.createByteBuffer(this.width * this.height * 4);
		GL11.glReadPixels(0, 0, this.width, this.height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
		return SnapshotImage.fromBytes(pixels, this.width, this.height, true, false);
	}

	@Override
	public void present() {
		GL11.glFlush();
	}

	@Override
	public void destroy() {
		this.buffer.destroy();
	}

}