package be.zeldown.joid.lib.ui.node;

import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.utils.context.InternalContext;
import be.zeldown.joid.lib.utils.list.RecursiveIndexedElement;
import lombok.NonNull;

public interface INode extends RecursiveIndexedElement {

	/**
	 * Initializes the UI component.
	 * This method is called when the UI component is created.
	 *
	 * @param ui The UI component to initialize.
	 * @throws NullPointerException If either the provided UI component is {@code null}.
	 */
	default public void init(final @NonNull UI ui) {}

	/**
	 * Draws the UI component.
	 * This method is called each frame to render the UI component.
	 *
	 * @param mouseX The x-coordinate of the mouse position.
	 * @param mouseY The y-coordinate of the mouse position.
	 * @param ticks The time elapsed in ticks.
	 */
	default public void draw(final double mouseX, final double mouseY) {}

	/**
	 * Draws the skeleton of the UI component.
	 * This method is called each frame to render the skeleton of the UI component.
	 * It provides an opportunity to draw essential outlines or visual guides.
	 *
	 * @param mouseX The x-coordinate of the mouse position.
	 * @param mouseY The y-coordinate of the mouse position.
	 * @param ticks The time elapsed in ticks.
	 */
	default public void drawSkeleton(final double mouseX, final double mouseY) {}

	/**
	 * Updates the interface.
	 * This method is called each client-tick.
	 */
	default public void update() {}

	/**
	 * Called when a mouse button is pressed.
	 *
	 * @param mouseX The x-coordinate of the mouse position.
	 * @param mouseY The y-coordinate of the mouse position.
	 * @param clickType The type of mouse click (e.g., left, right, middle).
	 * @param cancelled {@code true} if the action was cancelled, and {@code false} otherwise.
	 *
	 * @return {@code true} if the action should be cancelled, and {@code false} otherwise.
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
	 * @param cancelled {@code true} if the action was cancelled, and {@code false} otherwise.
	 *
	 * @return {@code true} if the action should be cancelled, and {@code false} otherwise.
	 */
	default public void mouseReleased(final double mouseX, final double mouseY, final int clickType, final @NonNull InternalContext context) {}

	/**
	 * Called when the mouse wheel is scrolled.
	 *
	 * @param mouseX The x-coordinate of the mouse position.
	 * @param mouseY The y-coordinate of the mouse position.
	 * @param value The amount scrolled.
	 * @param cancelled {@code true} if the action was cancelled, and {@code false} otherwise.
	 *
	 * @return {@code true} if the action should be cancelled, and {@code false} otherwise.
	 */
	default public void mouseScroll(final double mouseX, final double mouseY, final int value, final @NonNull InternalContext context) {}

	/**
	 * Called when a key is pressed.
	 *
	 * @param c The character of the pressed key.
	 * @param keyCode The code of the pressed key.
	 * @param cancelled {@code true} if the action was cancelled, and {@code false} otherwise.
	 *
	 * @return {@code true} if the action should be cancelled, and {@code false} otherwise.
	 */
	default public void keyPressed(final char c, final int keyCode, final @NonNull InternalContext context) {}

}