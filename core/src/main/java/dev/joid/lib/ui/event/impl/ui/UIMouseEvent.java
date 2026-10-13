package dev.joid.lib.ui.event.impl.ui;

import dev.joid.lib.input.mouse.MouseButton;
import dev.joid.lib.ui.core.UI;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class UIMouseEvent<U extends UI> extends UIInputEvent<U> {

	private final double      mouseX;
	private final double      mouseY;
	private final long        deltaTime;
	private final MouseButton button;

	private UIMouseEvent(final U ui, final double mouseX, final double mouseY, final MouseButton button, final long deltaTime) {
		super(ui);
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.button = button;
		this.deltaTime = deltaTime;
	}

	public static <U extends UI> @NonNull UIMouseEvent<U> create(final @NonNull U ui, final double mouseX, final double mouseY, final @NonNull MouseButton button, final long deltaTime) {
		return new UIMouseEvent<>(ui, mouseX, mouseY, button, deltaTime);
	}

}