package dev.joid.lib.ui.shortcut;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.key.resolver.KeyResolver;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Shortcut {

	private final Object                        binding;
	private final List<Consumer<ShortcutEvent>> triggerList;

	private boolean alt;
	private boolean ctrl;
	private boolean shift;
	private boolean overrideFocus;

	private UI   ui;
	private Node node;

	private Shortcut(final Object binding) {
		if (!KeyResolver.supports(binding)) {
			throw new IllegalArgumentException("No key resolver found for a binding of type " + binding.getClass().getName());
		}

		this.binding = binding;
		this.triggerList = new CopyOnWriteArrayList<>();
	}

	public static @NonNull Shortcut create(final @NonNull Key key) {
		return new Shortcut(key);
	}

	public static @NonNull Shortcut create(final @NonNull Object binding) {
		return new Shortcut(binding);
	}

	public @NonNull Shortcut alt(final boolean alt) {
		this.alt = alt;
		return this;
	}

	public @NonNull Shortcut ctrl(final boolean ctrl) {
		this.ctrl = ctrl;
		return this;
	}

	public @NonNull Shortcut shift(final boolean shift) {
		this.shift = shift;
		return this;
	}

	public @NonNull Shortcut overrideFocus(final boolean overrideFocus) {
		this.overrideFocus = overrideFocus;
		return this;
	}

	public @NonNull Shortcut onTrigger(final @NonNull Consumer<@NonNull ShortcutEvent> listener) {
		this.triggerList.add(listener);
		return this;
	}

	public void fireTrigger(final @NonNull Key key) {
		final ShortcutEvent event = new ShortcutEvent(key, this);
		this.triggerList.forEach(listener -> listener.accept(event));
	}

	public @NonNull Shortcut attach(final @NonNull UI ui) {
		this.detach();
		this.ui = ui;
		ui.getShortcutList().add(this);
		return this;
	}

	public @NonNull Shortcut attach(final @NonNull Node node) {
		this.detach();
		this.node = node;
		node.getShortcutList().add(this);
		return this;
	}

	public @NonNull Shortcut detach() {
		if (this.ui != null) {
			this.ui.getShortcutList().remove(this);
			this.ui = null;
		}

		if (this.node != null) {
			this.node.getShortcutList().remove(this);
			this.node = null;
		}
		return this;
	}

	public boolean isAttached() {
		return this.ui != null || this.node != null;
	}

	public boolean isTriggeredBy(final @NonNull Key key) {
		return this.isAttached() && KeyResolver.resolve(this.binding) == key && this.alt == UI.isAltKeyDown() && this.ctrl == UI.isCtrlKeyDown() && this.shift == UI.isShiftKeyDown();
	}

}