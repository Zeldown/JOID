package dev.joid.lib.ui.event.impl.ui;

import dev.joid.lib.input.key.Key;
import dev.joid.lib.ui.core.UI;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class UIKeyEvent<U extends UI> extends UIInputEvent<U> {

	@NonNull private final Key key;
	@NonNull private final Key physicalKey;

	private UIKeyEvent(final U ui, final Key physicalKey, final Key key) {
		super(ui);
		this.key = key;
		this.physicalKey = physicalKey;
	}

	public static <U extends UI> @NonNull UIKeyEvent<U> create(final @NonNull U ui, final @NonNull Key physicalKey, final @NonNull Key key) {
		return new UIKeyEvent<>(ui, physicalKey, key);
	}

}