package be.zeldown.joid.lib.ui.core;

import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

public interface IUI {

	/**
	 * Initializes the interface.
	 * This method is called when the interface is created.
	 */
	default public void init() {}

	/**
	 * Called when a mouse button is pressed.
	 *
	 * @param mouseX The x-coordinate of the mouse position.
	 * @param mouseY The y-coordinate of the mouse position.
	 * @param clickType The type of mouse click (e.g., left, right, middle).
	 * @param cancelled Whether the click event was triggered by a Node.
	 */
	default public void mousePressed(final double mouseX, final double mouseY, final int clickType, final @NonNull InternalContext context) {}

	/**
	 * Called when the mouse is dragged.
	 *
	 * @param mouseX
	 * @param mouseY
	 * @param clickType
	 * @param deltaTime
	 * @param cancelled
	 * @return {@code true} if the action should be cancelled, and {@code false} otherwise.
	 */
	default public void mouseDragged(final double mouseX, final double mouseY, final int clickType, final long deltaTime, final @NonNull InternalContext context) {}

	/**
	 * Called when a mouse button is released.
	 *
	 * @param mouseX The x-coordinate of the mouse position.
	 * @param mouseY The y-coordinate of the mouse position.
	 * @param clickType The type of mouse click (e.g., left, right, middle).
	 * @param cancelled Whether the click event was triggered by a Node.
	 */
	default public void mouseReleased(final double mouseX, final double mouseY, final int clickType, final @NonNull InternalContext context) {}

	/**
	 * Called when the mouse wheel is scrolled.
	 *
	 * @param mouseX The x-coordinate of the mouse position.
	 * @param mouseY The y-coordinate of the mouse position.
	 * @param value The value of the scroll wheel.
	 * @param cancelled Whether the scroll event was triggered by a Node.
	 */
	default public void mouseScroll(final double mouseX, final double mouseY, final int value, final @NonNull InternalContext context) {}

	/**
	 * Called when a key is pressed.
	 *
	 * @param c The character of the pressed key.
	 * @param keyCode The code of the pressed key.
	 * @param cancelled Whether the key event was triggered by a Node.
	 */
	default public void keyPressed(final char c, final int keyCode, final @NonNull InternalContext context) {}

	/**
	 * Called before drawing the mapped projection view.
	 * This method provides an opportunity to draw before the mapped projection [1920x1080].
	 * This method is called each frame.
	 *
	 * @param mouseX The x-coordinate of the mouse position.
	 * @param mouseY The y-coordinate of the mouse position.
	 * @param ticks The time elapsed in ticks.
	 */
	default public void drawBackground(final double mouseX, final double mouseY) {}

	/**
	 * Called before drawing a node with a z-index greater than or equal to 0.
	 * This method provides an opportunity to perform pre-drawing operations.
	 * This method is called each frame.
	 *
	 * @param mouseX The x-coordinate of the mouse position.
	 * @param mouseY The y-coordinate of the mouse position.
	 * @param ticks The time elapsed in ticks.
	 */
	default public void preDraw(final double mouseX, final double mouseY) {}

	/**
	 * Called after drawing a node with a z-index less than 100.
	 * This method provides an opportunity to perform post-drawing operations.
	 * This method is called each frame.
	 *
	 * @param mouseX The x-coordinate of the mouse position.
	 * @param mouseY The y-coordinate of the mouse position.
	 * @param ticks The time elapsed in ticks.
	 */
	default public void postDraw(final double mouseX, final double mouseY) {}

	/**
	 * Updates the interface.
	 * This method is called each client-tick.
	 */
	default public void update() {}

	/**
	 * Checks whether the GUI can be closed. This method is typically used in response to
	 * user actions, such as pressing the Escape key.
	 *
	 * @return {@code true} if the GUI can be closed, and {@code false} otherwise.
	 */
	default public boolean close() {
		return true;
	}

}