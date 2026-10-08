package dev.joid.base.glfw;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlfwWindows {

	public static double toFramebuffer(final double position, final int windowSize, final int framebufferSize) {
		return windowSize == 0 ? position : position * framebufferSize / windowSize;
	}

}