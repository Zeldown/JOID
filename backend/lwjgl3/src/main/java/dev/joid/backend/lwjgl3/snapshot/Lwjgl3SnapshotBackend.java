package dev.joid.backend.lwjgl3.snapshot;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import dev.joid.backend.lwjgl3.Lwjgl3Backend;
import dev.joid.backend.lwjgl3.binding.Lwjgl3GlBinding;
import dev.joid.base.lwjgl3.glfw.snapshot.GlfwSnapshotWindow;
import dev.joid.base.opengl.render.GlRenderBridge;
import dev.joid.base.opengl.snapshot.GlSnapshotCapture;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.test.snapshot.ISnapshotBackend;
import dev.joid.test.snapshot.SnapshotBridgeRestorer;
import dev.joid.test.snapshot.SnapshotImage;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Lwjgl3SnapshotBackend implements ISnapshotBackend {

	private final SnapshotProfile profile = SnapshotProfile.current();

	private GlRenderBridge     bridge;
	private GlfwSnapshotWindow window;

	@Getter(AccessLevel.NONE)
	private SnapshotBridgeRestorer restorer;

	@Override
	public void create(final int width, final int height) {
		this.restorer = new SnapshotBridgeRestorer();
		this.window = GlfwSnapshotWindow.create(width, height, this.profile.getRequest()::apply);
		GLFW.glfwMakeContextCurrent(this.window.getWindow());
		GLFW.glfwSwapInterval(0);
		GL.createCapabilities();
		Lwjgl3Backend.create(this.window.getWindow()).binding(ProfileGlBinding.from(Lwjgl3GlBinding.inst(), this.profile)).audio(false).register();
		this.bridge = (GlRenderBridge) BridgeHandler.RENDER.get();
		this.profile.check(this.bridge.getCapabilities());
	}

	@Override
	public void destroy() {
		this.window.destroy();
		this.restorer.restore();
	}

	@Override
	public void present() {
		GLFW.glfwSwapBuffers(this.window.getWindow());
	}

	@Override
	public @NonNull SnapshotImage capture(final int width, final int height) {
		return GlSnapshotCapture.capture(this.bridge, width, height);
	}

	@Override
	public @NonNull String getRenderer() {
		return this.bridge.getCapabilities().getRenderer();
	}

}