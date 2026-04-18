package be.zeldown.joid.lib.ui.node;

import be.zeldown.joid.lib.ui.core.UI;
import be.zeldown.joid.lib.utils.click.ClickType;
import be.zeldown.joid.lib.utils.context.InternalContext;
import be.zeldown.joid.lib.utils.list.RecursiveIndexedElement;
import lombok.NonNull;

public interface INode extends RecursiveIndexedElement, Cloneable {

	default public void init(final @NonNull UI ui) {}

	default public void draw(final double mouseX, final double mouseY) {}

	default public void drawSkeleton(final double mouseX, final double mouseY) {}

	default public void update() {}

	default public void mousePressed(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {}

	default public void mouseDragged(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final long deltaTime, final @NonNull InternalContext context) {}

	default public void mouseReleased(final double mouseX, final double mouseY, final @NonNull ClickType clickType, final @NonNull InternalContext context) {}

	default public void mouseScroll(final double mouseX, final double mouseY, final int value, final @NonNull InternalContext context) {}

	default public void keyPressed(final char c, final int keyCode, final @NonNull InternalContext context) {}

}