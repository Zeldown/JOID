package dev.joid.lib.ui.event.impl.ui;

import dev.joid.lib.ui.core.UI;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class UICharEvent<U extends UI> extends UIInputEvent<U> {

	private final int codepoint;

	private UICharEvent(final U ui, final int codepoint) {
		super(ui);
		this.codepoint = codepoint;
	}

	public static <U extends UI> @NonNull UICharEvent<U> create(final @NonNull U ui, final int codepoint) {
		return new UICharEvent<>(ui, codepoint);
	}

}