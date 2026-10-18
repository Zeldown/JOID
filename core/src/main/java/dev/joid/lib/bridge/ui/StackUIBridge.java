package dev.joid.lib.bridge.ui;

import com.google.common.collect.Lists;

import dev.joid.internal.JOIDEnv;
import dev.joid.internal.JOIDLog;
import dev.joid.lib.bridge.BridgeHandler;
import dev.joid.lib.ui.core.UI;
import lombok.NonNull;

public abstract class StackUIBridge extends UIBridge {

	private UI      pending;
	private UI      leaving;
	private boolean deferred;
	private boolean replacing;

	@Override
	public void open(final @NonNull UI ui) {
		if (super.isOpen(ui)) {
			if (ui.getTransition() != null && ui.getTransition().getOut().isRunning()) {
				ui.getTransition().getOut().stop();
			}

			if (ui == this.leaving) {
				this.drop();
			}

			super.remove(ui);
			super.add(ui);
			return;
		}

		if (ui.getPopup().active() || ui.getOverlay().active()) {
			this.add(ui);
			return;
		}

		this.replacing = super.hasScreen();
		try {
			for (final UI current : Lists.reverse(super.getUiList())) {
				if (current.getOverlay().active() || !this.canReplace(current)) {
					continue;
				}

				if (current == this.leaving) {
					this.deferred = true;
					this.pending  = ui;
					return;
				}

				final boolean result = current.fireClose();
				if (current.getTransition() != null && current.getTransition().getOut().isRunning()) {
					this.deferred = true;
					this.leaving  = current;
					this.pending  = ui;
					return;
				}

				if (!result) {
					if (JOIDEnv.isDevMode()) {
						JOIDLog.warn(JOIDLog.nameOf(current.getClass()) + " refused to close: a beforeClose listener cancelled the event, so " + JOIDLog.nameOf(ui.getClass()) + " is not opened: do not cancel it to let the UI close");
					}
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
		if (ui == this.pending) {
			this.pending  = null;
			this.deferred = false;
			return;
		}

		if (ui != this.leaving) {
			this.remove(ui);
			return;
		}

		final UI next = this.pending;
		this.leaving = null;
		this.pending = null;
		try {
			this.remove(ui);
			if (next != null) {
				this.open(next);
			}
		} finally {
			this.deferred = this.pending != null;
		}
	}

	@Override
	public void add(final @NonNull UI ui) {
		final boolean screen = !ui.getOverlay().active() && !super.hasScreen();
		super.add(ui);
		try {
			ui.load(BridgeHandler.WINDOW.get().getWidth(), BridgeHandler.WINDOW.get().getHeight());
		} finally {
			if (screen && !this.replacing && !this.deferred) {
				this.attachScreen();
			}
		}
	}

	@Override
	public void remove(final @NonNull UI ui) {
		final boolean screen = super.getUiList().contains(ui) && !ui.getOverlay().active();
		if (ui == this.leaving) {
			this.drop();
		}

		super.remove(ui);
		if (screen && !super.hasScreen() && !this.replacing && !this.deferred) {
			this.detachScreen();
		}
	}

	@Override
	public void forceOpen(final @NonNull UI ui) {
		this.keepScreen(() -> {
			super.closeAll();
			this.add(ui);
		});
	}

	@Override
	public void replace(final @NonNull UI ui, final @NonNull UI replacement) {
		this.keepScreen(() -> super.replace(ui, replacement));
	}

	@Override
	public boolean canHandle(final @NonNull Class<? extends UI> clazz) {
		return true;
	}

	public boolean canReplace(final @NonNull UI ui) {
		return true;
	}

	protected void attachScreen() {}

	protected void detachScreen() {}

	private void drop() {
		this.leaving  = null;
		this.pending  = null;
		this.deferred = false;
	}

	private void keepScreen(final Runnable action) {
		final boolean screen = super.hasScreen();
		this.replacing = screen;
		try {
			action.run();
		} finally {
			this.replacing = false;
		}

		if (screen && !super.hasScreen()) {
			this.detachScreen();
		}
	}

}