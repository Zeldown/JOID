package dev.joid.backend.lwjgl3.jar;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import dev.joid.backend.lwjgl3.Lwjgl3Backend;
import dev.joid.test.jar.JarCheck;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Lwjgl3JarCheck {

	public static void main(final String[] arguments) {
		JarCheck.run(arguments[0], () -> {
			if (!GLFW.glfwInit()) {
				throw new IllegalStateException("Unable to initialize GLFW");
			}

			GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
			final long window = GLFW.glfwCreateWindow(640, 480, "JOID jar check", 0L, 0L);
			if (window == 0L) {
				throw new IllegalStateException("Unable to create the hidden GLFW window");
			}

			GLFW.glfwMakeContextCurrent(window);
			GL.createCapabilities();
			return Lwjgl3Backend.create(window).audio(false).register();
		}, () -> GLFW.glfwSwapBuffers(GLFW.glfwGetCurrentContext()));
	}

}