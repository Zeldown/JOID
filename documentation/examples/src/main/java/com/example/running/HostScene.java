package com.example.running;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import dev.joid.lib.color.Color;

public class HostScene {

	private static final Color ACCENT     = new Color(91, 140, 255).lock();
	private static final Color BACKGROUND = new Color(14, 16, 20).lock();

	private double time;

	public void update(final double seconds) {
		this.time += seconds;
	}

	public void draw(final long window) {
		final int[] width = new int[1];
		final int[] height = new int[1];
		GLFW.glfwGetFramebufferSize(window, width, height);
		final int x = (int) Math.round(400D + 280D * Math.sin(this.time * 2D));

		HostScene.fill(0, 0, width[0], height[0], HostScene.BACKGROUND);
		HostScene.fill(x, height[0] - 600, 160, 160, HostScene.ACCENT);
	}

	private static void fill(final int x, final int y, final int width, final int height, final Color color) {
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(x, y, width, height);
		GL11.glClearColor(color.getRedF(), color.getGreenF(), color.getBlueF(), 1F);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
	}

}