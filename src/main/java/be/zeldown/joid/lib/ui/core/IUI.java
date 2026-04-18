package be.zeldown.joid.lib.ui.core;

import be.zeldown.joid.lib.utils.click.ClickType;
import be.zeldown.joid.lib.utils.context.InternalContext;
import lombok.NonNull;

public interface IUI {

	default public void init() {}

	default public void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {}

	default public void mouseDragged(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final long deltaTime, final @NonNull InternalContext context) {}

	default public void mouseReleased(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {}

	default public void mouseScroll(final double mouseX, final double mouseY, final int value, final @NonNull InternalContext context) {}

	default public void keyPressed(final char c, final int keyCode, final @NonNull InternalContext context) {}

	default public void drawBackground(final double mouseX, final double mouseY) {}

	default public void preDraw(final double mouseX, final double mouseY) {}

	default public void postDraw(final double mouseX, final double mouseY) {}

	default public void update() {}

	default public boolean close() {
		return true;
	}

}