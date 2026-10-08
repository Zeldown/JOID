package dev.joid.impl.lwjgl3.snapshot;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL30C;
import org.lwjgl.system.Platform;

import dev.joid.impl.lwjgl3.Backend;
import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.NonNull;

public final class SnapshotBackend implements ISnapshotBackend {

	private long window;

	@Override
	public void destroy() {
		GLFW.glfwDestroyWindow(this.window);
		GLFW.glfwTerminate();
	}

	@Override
	public void create(final int width, final int height) {
		if (!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, Platform.get() == Platform.MACOSX ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, 24);
		GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 8);

		this.window = GLFW.glfwCreateWindow(width, height, "JOID snapshot", 0L, 0L);
		if (this.window == 0L) {
			throw new IllegalStateException("Unable to create the GLFW window");
		}

		GLFW.glfwMakeContextCurrent(this.window);
		GLFW.glfwSwapInterval(0);
		GL.createCapabilities();
		Backend.register(this.window);
	}

	@Override
	public void present() {
		GLFW.glfwSwapBuffers(this.window);
	}

	@Override
	public @NonNull SnapshotImage capture(final int width, final int height) {
		final ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
		GL30C.glBindFramebuffer(GL30C.GL_FRAMEBUFFER, 0);
		GL11C.glReadBuffer(GL11C.GL_BACK);
		GL11C.glReadPixels(0, 0, width, height, GL11C.GL_RGBA, GL11C.GL_UNSIGNED_BYTE, pixels);
		return SnapshotImage.fromBytes(pixels, width, height, true, false);
	}

	@Override
	public @NonNull String getRenderer() {
		return GL11C.glGetString(GL11C.GL_RENDERER);
	}

}