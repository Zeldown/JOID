package dev.joid.backend.lwjgl3;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.Platform;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum GlContextRequest {

	COMPATIBILITY(0, 0, false),
	CORE_32_FORWARD_COMPATIBLE(3, 2, true),
	CORE_33(3, 3, false);

	private final int     major;
	private final int     minor;
	private final boolean forwardCompatible;

	public void apply() {
		if (this.major > 0) {
			GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, this.major);
			GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, this.minor);
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
			GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, this.forwardCompatible || Platform.get() == Platform.MACOSX ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
		}
		GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, 24);
		GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 8);
	}

}