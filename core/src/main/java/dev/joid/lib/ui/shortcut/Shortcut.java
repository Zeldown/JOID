package dev.joid.lib.ui.shortcut;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import dev.joid.internal.JOIDEnv;
import dev.joid.lib.input.key.Key;
import dev.joid.lib.input.key.resolver.KeyResolver;
import dev.joid.lib.ui.core.UI;
import dev.joid.lib.ui.event.EventBus;
import dev.joid.lib.ui.event.EventListener;
import dev.joid.lib.ui.event.EventPriority;
import dev.joid.lib.ui.event.EventType;
import dev.joid.lib.ui.node.Node;
import lombok.Getter;
import lombok.NonNull;

@Getter
public final class Shortcut {

	private static final Set<Object>              WARNED  = ConcurrentHashMap.newKeySet();
	private static final EventType<ShortcutEvent> TRIGGER = EventType.create("onTrigger");

	private final Object   binding;
	private final EventBus eventBus;

	private boolean alt;
	private boolean ctrl;
	private boolean shift;
	private boolean overrideFocus;

	private UI   ui;
	private Node node;

	private Shortcut(final Object binding) {
		if (JOIDEnv.isDevMode() && !KeyResolver.supports(binding) && Shortcut.WARNED.add(binding)) {
			System.err.println("[JOID] No key resolver knows the shortcut binding " + binding + " of type " + binding.getClass().getName() + ", the shortcut never fires: pass a Key or register an IKeyResolver first");
		}

		this.binding = binding;
		this.eventBus = EventBus.create(() -> "Shortcut");
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

	public @NonNull Shortcut onTrigger(final @NonNull EventListener<ShortcutEvent> listener) {
		return this.onTrigger(EventPriority.NORMAL, listener);
	}

	public @NonNull Shortcut onTrigger(final @NonNull EventPriority priority, final @NonNull EventListener<ShortcutEvent> listener) {
		this.eventBus.listen(Shortcut.TRIGGER, priority, listener);
		return this;
	}

	public void fireTrigger(final @NonNull Key key) {
		if (this.eventBus.has(Shortcut.TRIGGER)) {
			this.eventBus.fire(Shortcut.TRIGGER, ShortcutEvent.create(key, this));
		}
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
		return this.isAttached() && KeyResolver.supports(this.binding) && KeyResolver.resolve(this.binding) == key && this.alt == UI.isAltKeyDown() && this.ctrl == UI.isCtrlKeyDown() && this.shift == UI.isShiftKeyDown();
	}

}