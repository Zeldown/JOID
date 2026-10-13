package dev.joid.lib.ui.event.impl.ui;

import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.ConsumableEvent;
import dev.joid.lib.ui.event.UIEvent;

public abstract class UIInputEvent<U extends UI> extends UIEvent<U> implements ConsumableEvent {

	private boolean consumed;

	protected UIInputEvent(final U ui) {
		super(ui);
	}

	@Override
	public final void consume() {
		super.checkWritable("consume");
		this.consumed = true;
	}

	@Override
	public final boolean isConsumed() {
		return this.consumed;
	}

}