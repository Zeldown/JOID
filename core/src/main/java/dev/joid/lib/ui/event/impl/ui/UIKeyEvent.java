package dev.joid.lib.ui.event.impl.ui;

import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.core.UI;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class UIKeyEvent<U extends UI> extends UIInputEvent<U> {

	@NonNull
	private final Key key;

	private UIKeyEvent(final U ui, final Key key) {
		super(ui);
		this.key = key;
	}

	public static <U extends UI> @NonNull UIKeyEvent<U> create(final @NonNull U ui, final @NonNull Key key) {
		return new UIKeyEvent<>(ui, key);
	}

}