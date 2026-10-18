package dev.joid.backend.vulkan.demo;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Configuration;

import dev.joid.backend.vulkan.VulkanBackend;
import dev.joid.backend.vulkan.host.WindowVulkanHost;
import dev.joid.base.glfw.demo.GlfwDemoWindow;
import dev.joid.lib.bridge.backend.Backend;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DemoWindow extends GlfwDemoWindow {

	private VulkanBackend    backend;
	private WindowVulkanHost host;

	public static @NonNull DemoWindow create() {
		return new DemoWindow();
	}

	public static void main(final String[] args) {
		Configuration.STACK_SIZE.set(1024);
		DemoWindow.create().run();
	}

	@Override
	protected String getBackendName() {
		return "Vulkan";
	}

	@Override
	protected void configureWindow() {
		GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
	}

	@Override
	protected @NonNull Backend registerBackend(final long window) {
		this.host    = WindowVulkanHost.create(window);
		this.backend = VulkanBackend.create(window, this.host).register();
		this.host.beginFrame();
		return this.backend;
	}

	@Override
	protected void present() {
		this.host.endFrame();
		if (!GLFW.glfwWindowShouldClose(super.getWindow())) {
			this.host.beginFrame();
		}
	}

	@Override
	protected void destroyBackend() {
		this.backend.destroy();
		this.host.destroy();
	}

}