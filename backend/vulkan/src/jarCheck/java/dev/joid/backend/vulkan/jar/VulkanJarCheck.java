package dev.joid.backend.vulkan.jar;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Configuration;

import dev.joid.backend.vulkan.VulkanBackend;
import dev.joid.backend.vulkan.render.VulkanRenderBridge;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.test.jar.JarCheck;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VulkanJarCheck {

	public static void main(final String[] arguments) {
		Configuration.STACK_SIZE.set(1024);
		JarCheck.run(arguments[0], () -> {
			if (!GLFW.glfwInit()) {
				throw new IllegalStateException("Unable to initialize GLFW");
			}

			GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
			GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
			final long window = GLFW.glfwCreateWindow(640, 480, "JOID jar check", 0L, 0L);
			if (window == 0L) {
				throw new IllegalStateException("Unable to create the hidden GLFW window");
			}
			return VulkanBackend.create(window).audio(false).register();
		}, () -> ((VulkanRenderBridge) BridgeHandler.RENDER.get()).present());
	}

}