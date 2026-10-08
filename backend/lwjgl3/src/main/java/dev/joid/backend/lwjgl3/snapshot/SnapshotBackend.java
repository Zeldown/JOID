package dev.joid.backend.lwjgl3.snapshot;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import dev.joid.backend.lwjgl3.Backend;
import dev.joid.backend.lwjgl3.GlContextRequest;
import dev.joid.backend.lwjgl3.binding.Lwjgl3GlBinding;
import dev.joid.base.opengl.snapshot.GlSnapshotCapture;
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
		GlContextRequest.CORE_33.apply();

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
		return GlSnapshotCapture.capture(Lwjgl3GlBinding.inst(), width, height);
	}

	@Override
	public @NonNull String getRenderer() {
		return GlSnapshotCapture.getRenderer(Lwjgl3GlBinding.inst());
	}

}