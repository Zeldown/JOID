package dev.joid.lib.ui.event.impl.ui;

import dev.joid.lib.ui.core.UI;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class UIMouseScrollEvent<U extends UI> extends UIInputEvent<U> {

	private final double mouseX;
	private final double mouseY;
	private final double notchesX;
	private final double notchesY;

	private UIMouseScrollEvent(final U ui, final double mouseX, final double mouseY, final double notchesX, final double notchesY) {
		super(ui);
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.notchesX = notchesX;
		this.notchesY = notchesY;
	}

	public static <U extends UI> @NonNull UIMouseScrollEvent<U> create(final @NonNull U ui, final double mouseX, final double mouseY, final double notchesX, final double notchesY) {
		return new UIMouseScrollEvent<>(ui, mouseX, mouseY, notchesX, notchesY);
	}

}