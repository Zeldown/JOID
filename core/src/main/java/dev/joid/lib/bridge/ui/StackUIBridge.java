package dev.joid.lib.bridge.ui;

import java.util.ArrayList;

import dev.joid.internal.JOID;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.ui.core.UI;
import lombok.NonNull;

public abstract class StackUIBridge extends UIBridge {

	private boolean replacing;

	@Override
	public void open(final @NonNull UI ui) {
		if (ui.getPopup().active() || ui.getOverlay().active()) {
			this.add(ui);
			return;
		}

		this.replacing = super.hasScreen();
		try {
			for (final UI current : new ArrayList<>(super.getUiList().ordered())) {
				if (current.getOverlay().active()) {
					continue;
				}

				final boolean result = current.onClose();
				if (current.getTransition() != null && current.getTransition().getOut() != null && current.getTransition().getOut().isRunning()) {
					current.getTransition().getOut().getAnimator().setCallback(tween -> JOID.open(ui));
					return;
				}

				if (!result) {
					return;
				}

				this.close(current);
			}

			this.add(ui);
		} finally {
			this.replacing = false;
		}
	}

	@Override
	public void close(final @NonNull UI ui) {
		this.remove(ui);
	}

	@Override
	public void add(final @NonNull UI ui) {
		final boolean screen = !ui.getOverlay().active() && !super.hasScreen();
		super.getUiList().add(ui);
		ui.load(BridgeHandler.WINDOW.get().getWidth(), BridgeHandler.WINDOW.get().getHeight());
		if (screen && !this.replacing) {
			this.onFirstScreenOpen();
		}
	}

	@Override
	public void remove(final @NonNull UI ui) {
		final boolean screen = super.getUiList().contains(ui) && !ui.getOverlay().active();
		super.getUiList().remove(ui);
		if (screen && !super.hasScreen() && !this.replacing) {
			this.onLastScreenClose();
		}
	}

	public void closeAll() {
		for (final UI ui : new ArrayList<>(super.getUiList().ordered())) {
			ui.dispose();
			this.remove(ui);
		}
	}

	@Override
	public boolean canHandle(final @NonNull UI ui) {
		return true;
	}

	@Override
	public boolean canHandle(final @NonNull Class<? extends UI> clazz) {
		return true;
	}

	protected void onFirstScreenOpen() {}

	protected void onLastScreenClose() {}

}