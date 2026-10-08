package dev.joid.backend.lwjgl3.snapshot;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import dev.joid.backend.lwjgl3.Backend;
import dev.joid.backend.lwjgl3.GlContextRequest;
import dev.joid.backend.lwjgl3.binding.Lwjgl3GlBinding;
import dev.joid.base.glfw.snapshot.GlfwSnapshotWindow;
import dev.joid.base.opengl.snapshot.GlSnapshotCapture;
import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.NonNull;

public final class SnapshotBackend implements ISnapshotBackend {

	private GlfwSnapshotWindow window;

	@Override
	public void destroy() {
		this.window.destroy();
	}

	@Override
	public void create(final int width, final int height) {
		this.window = GlfwSnapshotWindow.create(width, height, GlContextRequest.CORE_33::apply);
		GLFW.glfwMakeContextCurrent(this.window.getWindow());
		GLFW.glfwSwapInterval(0);
		GL.createCapabilities();
		Backend.register(this.window.getWindow());
	}

	@Override
	public void present() {
		GLFW.glfwSwapBuffers(this.window.getWindow());
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